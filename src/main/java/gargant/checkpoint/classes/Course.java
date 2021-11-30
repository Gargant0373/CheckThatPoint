package gargant.checkpoint.classes;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class Course {

	private String courseName;
	private Location leaderboardLocation;
	private List<Checkpoint> checkpoints = new ArrayList<>();
	private String permissionToEnter;

	public Course(String courseName) {
		this.courseName = courseName;
	}

	public String getCourseName() {
		return courseName;
	}

	public Location getLeaderboardLocation() {
		return leaderboardLocation;
	}

	public List<Checkpoint> getCheckpoints() {
		return checkpoints;
	}
	
	public String getPermission() {
		return this.permissionToEnter;
	}

	public void setCheckpoints(List<Checkpoint> checkpoints) {
		this.checkpoints = checkpoints;
	}

	public void setName(String name) {
		this.courseName = name;
	}

	public void setLeaderboardLocation(Location l) {
		this.leaderboardLocation = l;
	}
	
	public void setPermission(String permission) {
		this.permissionToEnter = permission;
	}
	
	public CourseBuilder getBuilder() {
		return new CourseBuilder(this);
	}
}
