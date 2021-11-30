package gargant.checkpoint.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import gargant.checkpoint.classes.Checkpoint;
import gargant.checkpoint.classes.Course;
import gargant.checkpoint.classes.CourseProgress;
import gargant.checkpoint.main.ParkourManager;
import masecla.mlib.classes.Registerable;
import masecla.mlib.main.MLib;

public class PressurePlateCheckpoint extends Registerable {

	private ParkourManager parkourManager;

	public PressurePlateCheckpoint(MLib lib, ParkourManager parkourManager) {
		super(lib);
		this.parkourManager = parkourManager;
	}

	@EventHandler
	public void onStep(PlayerInteractEvent ev) {
		Player p = ev.getPlayer();

		if (!ev.getAction().equals(Action.PHYSICAL))
			return;

		if (!this.parkourManager.isParkouring(p)) {
			Course startingCourse = parkourManager.getCourses().stream()
					.filter(c -> c.getCheckpoints().get(0).getLocation().getBlock().equals(ev.getClickedBlock()))
					.findFirst().orElseGet(null);
			if (startingCourse != null)
				this.handleStart(p, startingCourse);
			return;
		}

		Course course = this.parkourManager.getActiveCourse(p);
		CourseProgress courseProgress = this.parkourManager.getActiveCourseProgress(p);

		int lastCheckpoint = courseProgress.getCheckpoint();

		Checkpoint reachedCheckpoint = course.getCheckpoints().get(lastCheckpoint + 1);

		// not actual checkpoint
		if (!reachedCheckpoint.getLocation().getBlock().equals(ev.getClickedBlock()))
			return;
	}

	private void handleStart(Player p, Course c) {
		this.parkourManager.updateCourseProgress(p, new CourseProgress(c, 0));
	}
}
