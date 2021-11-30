package gargant.checkpoint.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import gargant.checkpoint.classes.Checkpoint;
import gargant.checkpoint.classes.Course;
import gargant.checkpoint.main.ParkourManager;
import masecla.mlib.annotations.RegisterableInfo;
import masecla.mlib.annotations.RequiresPlayer;
import masecla.mlib.annotations.SubcommandInfo;
import masecla.mlib.classes.Registerable;
import masecla.mlib.classes.Replaceable;
import masecla.mlib.main.MLib;

@RegisterableInfo(command = "checkpoint")
@RequiresPlayer
public class CheckpointCommand extends Registerable {
	
	private ParkourManager parkourManager;

	public CheckpointCommand(MLib lib,ParkourManager parkourManager) {
		super(lib);
		this.parkourManager = parkourManager;
	}

	private Map<UUID, Course> playersBuilding = new HashMap<>();
	
	@SubcommandInfo(subcommand = "course create", permission = "checkthatpoint.create")
	public void onCreateNoName(Player p) {
		Course course = new Course("N/A");
		lib.getMessagesAPI().sendMessage("internal.course.create", p, new Replaceable("%name%", "N/A"));
		this.playersBuilding.put(p.getUniqueId(), course);
	}

	@SubcommandInfo(subcommand = "course create", permission = "checkthatpoint.create")
	public void onCreate(Player p, String name) {
		Course course = new Course(name);
		lib.getMessagesAPI().sendMessage("internal.course.create", p, new Replaceable("%name%", name));
		this.playersBuilding.put(p.getUniqueId(), course);
	}

	@SubcommandInfo(subcommand = "course checkpoint", permission = "checkpoint.create")
	public void onCheckpoint(Player p) {
		this.handleMapScanning(p, 10);
	}

	@SubcommandInfo(subcommand = "course check", permission = "checkthatpoint.create")
	public void handleChecking(Player p) {
		Course c = this.playersBuilding.getOrDefault(p.getUniqueId(), new Course("N/A"));
		lib.getMessagesAPI().sendMessage("internal.course.present", p, new Replaceable("%name%", c.getCourseName()),
				new Replaceable("%checkpoints%", c.getCheckpoints().size()));
		for (int i = 0; i < c.getCheckpoints().size(); i++) {
			int index = i;
			Bukkit.getScheduler().scheduleSyncDelayedTask(lib.getPlugin(), () -> {
				Checkpoint checkpoint = c.getCheckpoints().get(index);
				p.teleport(checkpoint.getLocation());
				lib.getMessagesAPI().sendMessage("internal.course.presenting-checkpoint", p,
						new Replaceable("%name%", checkpoint.getName()), new Replaceable("%number%", index));
			}, i * 40);
		}
	}

	@SubcommandInfo(subcommand = "course save", permission = "checkthatpoint.create")
	public void handleSaving(Player p) {
		Course c = this.playersBuilding.getOrDefault(p.getUniqueId(), new Course("N/A"));
		lib.getMessagesAPI().sendMessage("internal.course.save", p, new Replaceable("%name%", c.getCourseName()));
		this.parkourManager.addCourse(c);
		this.parkourManager.saveCourses();
	}

	@SuppressWarnings("deprecation")
	private CompletableFuture<Boolean> handleMapScanning(Player p, int range) {
		CompletableFuture<Boolean> res = new CompletableFuture<>();
		World map = p.getWorld();
		ExecutorService threads = Executors.newFixedThreadPool(1);
		int initialX = p.getLocation().getChunk().getX();
		int initialZ = p.getLocation().getChunk().getZ();

		Queue<Block> signsToCheck = new ConcurrentLinkedQueue<>();
		AtomicInteger totalChunks = new AtomicInteger(0);
		AtomicInteger doneChunks = new AtomicInteger(0);
		for (int chunkX = -range; chunkX <= range; chunkX++)
			for (int chunkZ = -range; chunkZ <= range; chunkZ++) {
				totalChunks.incrementAndGet();
				Chunk chk = p.getWorld().getChunkAt(initialX + chunkX, initialZ + chunkZ);
				chk.load();
				ChunkSnapshot snapshot = chk.getChunkSnapshot();
				threads.submit(() -> {
					for (int x = 0; x < 16; x++)
						for (int z = 0; z < 16; z++)
							for (int y = 0; y < 256; y++) {
								try {
									if (snapshot.getBlockTypeId(x, y, z) == Material.SIGN_POST.getId()) {
										signsToCheck.add(new Location(map, snapshot.getX() << 4 | (x & 0xF), y,
												snapshot.getZ() << 4 | (z & 0xF)).getBlock());
									}
								} catch (Exception e) {
									Bukkit.getScheduler().scheduleSyncDelayedTask(lib.getPlugin(),
											() -> p.sendMessage(ChatColor.translateAlternateColorCodes('&',
													"&cSomething failed, please check logs.")));
									e.printStackTrace();
								}
							}
					Bukkit.getScheduler().scheduleSyncDelayedTask(lib.getPlugin(),
							() -> lib.getMessagesAPI().sendActionbarMessage(ChatColor.translateAlternateColorCodes('&',
									"&aFinished scanning " + doneChunks.incrementAndGet() + "/" + totalChunks.get()),
									p));
				});
			}
		CompletableFuture<Boolean> threadPoolFinish = new CompletableFuture<>();
		new Thread(() -> {
			try {
				threads.shutdown();
				threads.awaitTermination(10, TimeUnit.DAYS);
				threadPoolFinish.complete(null);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		threadPoolFinish.thenAccept(c -> {
			p.sendMessage("Found a total of " + signsToCheck.size() + " signs.");
			res.complete(true);
		});
		Map<Integer, Checkpoint> checkpoints = new HashMap<>();
		signsToCheck.forEach(c -> {
			this.handleSignChecking(p, (Sign) c.getState(), checkpoints);
		});
		List<Integer> checkpointIndexes = new ArrayList<>(checkpoints.keySet());
		checkpointIndexes.sort(Comparator.naturalOrder());
		Course course = this.playersBuilding.getOrDefault(p.getUniqueId(), new Course("N/A"));
		checkpointIndexes.forEach(c -> {
			course.getCheckpoints().add(checkpoints.get(c));
		});
		return res;
	}
	
	private void handleSignChecking(Player p, Sign sign, Map<Integer, Checkpoint> checkpoints) {
		Course course = this.playersBuilding.getOrDefault(p.getUniqueId(), new Course("N/A"));

		if (sign.getLines().length < 3)
			return;
		if (!sign.getLines()[0].equalsIgnoreCase(course.getCourseName()))
			return;

		int checkpoint = 0;

		try {
			checkpoint = Integer.parseInt(sign.getLines()[1]);
		} catch (NumberFormatException e) {
			return;
		}

		Checkpoint c = new Checkpoint(this.getSignLocation(sign.getBlock()), sign.getLines()[2]);

		if (sign.getLines().length > 3 && sign.getLines()[3].equalsIgnoreCase("portal")) {
			c.setPortal(true);
		}
		checkpoints.put(checkpoint, c);
		this.playersBuilding.put(p.getUniqueId(), course);
	}

	@SuppressWarnings("deprecation")
	private Location getSignLocation(Block b) {
		Location l = b.getLocation().clone().add(0.5, 0, 0.5);
		float angle = (360.0f / 16.0f) * b.getData();
		l.setYaw(angle);
		l.setPitch(0);
		return l;
	}
}
