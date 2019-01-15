package Team4450.Robot11.Pathing;

/**
 * Auto Program using 2 paths to drive to the Far Scale and deliver one plus cubes.
 */
public class FarScale 
{
	// max speed for these robot paths.
	public static final double MAX_SPEED = 120;

	// used to initialize robot state to the initial position of the path group
	private final 	Pose2D startingPose;
	private Path 	path1 =  null, path2 = null;

//	// temporary place-holder command for scoring behind
//	private class ScoreBehind extends CommandGroup {
//		@Override
//		protected void initialize() {
//			LOGGER.info("initiate ScoreBehind");
//		}
//	}
//
//	// temporary place-holder command for bringing the lift and arm to collecting position
//	private class CollectPosition extends CommandGroup {
//		@Override
//		protected void initialize() {
//			LOGGER.info("initiate CollectPosition");
//		}
//	}
//
//	// temporary place-holder command for automatically collecting the next cube
//	private class AutoCollect extends CommandGroup {
//		@Override
//		protected void initialize() {
//			LOGGER.info("initiate AutoCollect");
//		}
//	}
	
	/**
	 * Reverse straight, turn down the corridor and turn back to the scale
	 */
	private class Score1stOnScale extends Path
	{
		/**
		 * Construct command with the robot reversing down the path
		 * 
		 * @param mirror - coordinate mirroring about the center lines of field
		 * @param initialPose - starting robot pose
		 */
		public Score1stOnScale(PathBuilder.Mirror mirror, Pose2D initialPose) 
		{
			super(PathBuilder.PathDirection.Reversed, mirror, initialPose);
		}
		
		/**
		 * Define the path using the pathBuilder
		 */
		@Override
		protected void createPath(PathBuilder pathBuilder) 
		{
			// drive straight up to the start of the corridor
			pathBuilder.addLine(168.0, MAX_SPEED);
			
			// curve to the left to the center of the corridor
			// slow down to prevent tipping or over driving the
			// outside wheels
			pathBuilder.addArc(90.0, 44.0, MAX_SPEED * 0.6);
			
			// down the corridor to just prior to the far scale
			pathBuilder.addLine(125.0, MAX_SPEED);	//, 			
					// add an action 80% through the line segment
					// to raise the lift and score behind the robot
					//new PathAction(new ScoreBehind(), 0.80, true)
			//);
			
			// finish the path by turning into the far scale to 
			// the a fixed point, again slow down for the tight turn
			pathBuilder.addArcToPoint(289.0, 253.0, MAX_SPEED * 0.5);
		}
	};

	/**
	 * Forward curve to 2nd Cube on corner
	 */
	private class Collect2ndCube extends Path
	{
		/**
		 * Construct command with the robot forwarding through the path
		 * 
		 * @param mirror - coordinate mirroring about the center lines of field
		 * @param initialPose - starting robot pose
		 */
		public Collect2ndCube(PathBuilder.Mirror mirror, Pose2D initialPose) 
		{
			 super(PathBuilder.PathDirection.Forward, mirror, initialPose);
		}
		
		/**
		 * Define the path using the pathBuilder
		 */
		@Override
		protected void createPath(PathBuilder pathBuilder) {
			
			// turn back towards the first cube near the switch a fixed 
			// point, again slow down for the tight turn
			pathBuilder.addArcToPoint(222.0, 236.0, MAX_SPEED * 0.5);	//, 
					// add an action 0% through the curve segment
					// to lower the lift and arm to the collect position
					//new PathAction(new CollectPosition(), 0.00, true),
					// add an action 80% through the curve segment
					// to enable auto collect mode
					//new PathAction(new AutoCollect(), 0.80, true)
			//);
		}
	};
	
	/**
	 * Construct the Far Scale Auto Program sequence of paths.
	 */
	public FarScale(PathBuilder.Mirror mirror) 
	{
		// ensure that this initial position has been mirrored as needed as the path would do
		startingPose = PathBuilder.mirrorPose2D(AutoConstants.RIGHT_POSE_REVERSED, mirror);
		
		// sequence the above path commands providing the final pose of the previous path
		// to the next path
		path1 = new Score1stOnScale(mirror, AutoConstants.RIGHT_POSE_REVERSED);
		path2 = new Collect2ndCube(mirror, path1.getFinalPose());
	}

	/**
	 * Auto Program initialize
	 */
	protected void initialize() 
	{
		// reset robot pose to the start of the path
		RobotPosition.getInstance().resetRobotPose(startingPose.x, startingPose.y, startingPose.heading);
	}

	/**
	 * Auto Program execute
	 */
	protected void execute() 
	{
		path1.execute();
		path2.execute();
	}
}
