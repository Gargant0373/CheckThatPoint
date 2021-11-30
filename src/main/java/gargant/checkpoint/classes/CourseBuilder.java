package gargant.checkpoint.classes;

import java.util.List;

import org.bukkit.Location;

public class CourseBuilder {
	private Course course;

	public CourseBuilder() {
		this.course = new Course("N/A");
	}
	
	public CourseBuilder(Course course) {
		this.course = course;
	}

	public CourseBuilder addCheckpoint(Checkpoint c) {
		List<Checkpoint> checkpoints = course.getCheckpoints();
		checkpoints.add(c);
		course.setCheckpoints(checkpoints);
		return this;
	}

	public CourseBuilder name(String name) {
		this.course.setName(name);
		return this;
	}

	public CourseBuilder setLeaderboardLocation(Location l) {
		this.course.setLeaderboardLocation(l);
		return this;
	}
	
	public CourseBuilder setPermission(String permission) {
		this.course.setPermission(permission);
		return this;
	}
}
