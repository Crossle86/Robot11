package Team4450.Robot11.Pathing;

public class AutoConstants 
{
	public static final double START_FROM_END_WALL = 19.5;
	public static final double START_FROM_SIDE_WALL = 45;
	public static final double ROBOT_WIDTH = 35.0;
	
	public static final Pose2D RIGHT_POSE = new Pose2D(START_FROM_END_WALL, START_FROM_SIDE_WALL, 0);
	public static final Pose2D MIDDLE_POSE = new Pose2D(START_FROM_END_WALL, PathConstants.FIELD_WIDTH / 2 - ROBOT_WIDTH / 2, 0);
	public static final Pose2D LEFT_POSE = new Pose2D(START_FROM_END_WALL, PathConstants.FIELD_WIDTH - START_FROM_SIDE_WALL, 0);

	public static final Pose2D RIGHT_POSE_REVERSED = new Pose2D(START_FROM_END_WALL, START_FROM_SIDE_WALL, 180);
	public static final Pose2D MIDDLE_POSE_REVERSED = new Pose2D(START_FROM_END_WALL, PathConstants.FIELD_WIDTH / 2, 180);
	public static final Pose2D LEFT_POSE_REVERSED = new Pose2D(START_FROM_END_WALL, PathConstants.FIELD_WIDTH - START_FROM_SIDE_WALL, 180);
}
