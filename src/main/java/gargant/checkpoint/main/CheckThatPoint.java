package gargant.checkpoint.main;

import org.bukkit.plugin.java.JavaPlugin;

import gargant.checkpoint.commands.CheckpointCommand;
import masecla.mlib.main.MLib;

public class CheckThatPoint extends JavaPlugin {

	private MLib lib;
	
	private ParkourManager parkourManager;

	@Override
	public void onEnable() {
		this.lib = new MLib(this);
		this.lib.getConfigurationAPI().requireAll();
		
		this.parkourManager = new ParkourManager(lib);

		new CheckpointCommand(lib, parkourManager).register();
	}
}
