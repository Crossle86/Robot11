package Team4450.Robot11.Pathing;

import java.util.List;

/**
 * Abstract path command that implements the basics of path following and provides common methods 
 * needed for autonomous commanding.
 *
 */
public abstract class Path
{
	// used to help generate path
	private final PathBuilder pathBuilder;
	
	// list of way-points used for display purposes and path controllers
	private final List<Waypoint> waypoints;
	
	// final pose calculated from the initial pose and the ensuing path
	private final Pose2D finalPose;
	
	/**
	 * Create the base path command object starting with the initial pose, the robot drive
	 * direction and if the paths coordinates should be mirrored relative to the center lines
	 * of the field.
	 * 
	 * @param direction - forward or revered robot driving
	 * @param mirror - coordinate mirroring about the center lines of field
	 * @param initialPose - starting robot pose
	 */
	public Path(final PathBuilder.PathDirection direction, final PathBuilder.Mirror mirror, final Pose2D initialPose) 
	{
		// create the path with the path builder
		pathBuilder = new PathBuilder(direction, mirror, initialPose);
	
		createPath(pathBuilder);
		
		finalPose = pathBuilder.getFinalPose();
		waypoints = pathBuilder.createWaypoints();
	}

	/**
	 * Returns the final robot pose after the path is complete
	 * 
	 * @return final robot pose (x, y, hdg)
	 */
	public Pose2D getFinalPose() 
	{
		return finalPose;
	}

	/**
	 * List of way-points the represents the path.
	 * 
	 * @return list of way-points (x, y)
	 */
	public List<Waypoint> getWaypoints() 
	{
		return waypoints;
	}

	/**
	 * Path command implementation should use the provided path builder to construct the path.
	 * 
	 * @param pathBuilder - to be used in construction of the path
	 */
	protected abstract void createPath(PathBuilder pathBuilder);

	/**
	 * Initialization of the path state at the start of running
	 */
	protected void initialize() 
	{
	}

	/**
	 * Execution of the path
	 */
	protected void execute() 
	{
	}

	/**
	 * Checks for completion which returns true if path has completed or timed-out
	 */
	protected boolean isFinished() 
	{
		return false;
	}

	/**
	 * Finalization of the command state at the end of running
	 */
	protected void end() 
	{
	}
}
