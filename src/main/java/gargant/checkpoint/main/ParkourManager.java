package gargant.checkpoint.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;

import gargant.checkpoint.classes.Course;
import gargant.checkpoint.classes.CourseProgress;
import masecla.mlib.main.MLib;

public class ParkourManager {

	private MLib lib;

	public ParkourManager(MLib lib) {
		this.lib = lib;
	}

	private Map<UUID, CourseProgress> playersOnCourse = new HashMap<>();
	private List<Course> courses;

	public void register() {
		this.loadCourses();
	}

	@SuppressWarnings("unchecked")
	private void loadCourses() {
		try {
			this.courses = (List<Course>) lib.getConfigurationAPI().getConfig("courses").get("courses",
					new ArrayList<>());
		} catch (Exception e) {
			e.printStackTrace();
			this.courses = new ArrayList<>();
		}
	}
	
	public List<Location> getInitialPositions() {
		return courses.stream().map(c -> c.getCheckpoints().get(0).getLocation()).collect(Collectors.toList());
	}
	
	public void addCourse(Course c) {
		this.courses.add(c);
	}
	
	public void saveCourses() {
		lib.getConfigurationAPI().getConfig("courses").set("courses", this.courses);
	}
}
