package gargant.checkpoint.classes;

import org.bukkit.Location;

public class Checkpoint {

	private Location location;
	private String name;
	private boolean portal;
	
	public Checkpoint(Location location, String name) {
		this.location = location;
		this.name = name;
	}
	
	public void setPortal(boolean portal) {
		this.portal = portal;
	}
	
	public boolean isPortal() {
		return this.portal;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
