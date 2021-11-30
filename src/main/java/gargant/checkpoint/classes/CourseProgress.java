package gargant.checkpoint.classes;

public class CourseProgress {

	private Course course;
	private int checkpoint;

	public CourseProgress(Course course, int checkpoint) {
		this.course = course;
		this.checkpoint = checkpoint;
	}

	/**
	 * Method used to increment the checkpoint
	 * 
	 * @return true if finish, false if not
	 */
	public boolean incrementCheckpoint() {
		checkpoint++;
		if (course.getCheckpoints().size() == checkpoint)
			return true;
		return false;
	}

	public int getCheckpoint() {
		return this.checkpoint;
	}

	public Course getCourse() {
		return this.course;
	}
}
