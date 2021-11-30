package gargant.checkpoint.classes;

import org.bukkit.plugin.PluginDescriptionFile;

import masecla.mlib.main.MLib;

public class CheckpointAPI {

	private MLib lib;

	public CheckpointAPI(MLib lib) {
		this.lib = lib;
	}

	private static CheckpointAPI instance;

	public static CheckpointAPI getInstance() {
		return instance;
	}

	public CourseBuilder getCourseBuilder() {
		return new CourseBuilder();
	}

	public PluginDescriptionFile getPluginData() {
		return lib.getPlugin().getDescription();
	}

}
