package Team4450.Robot11.Pathing;

import java.math.BigDecimal;

/**
 * Based on path finding training example by Bear Metal 2046 FRC Team.
 */

import java.util.function.Consumer;

import Team4450.Lib.NavX;
import Team4450.Lib.SRXMagneticEncoderRelative;
import Team4450.Lib.SRXMagneticEncoderRelative.PIDRateType;
import Team4450.Lib.Util;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Responsible for updating the state of the Robot with respect to position and movements
 * on the field.  This is where the position, pose and velocities are estimated.  Estimation
 * can be simple or more complex.  Simple is done by integrating encoder odometry and IMU 
 * gyroscope rates.  More complex add vision target to create a more accurate pose eliminating
 * errors with wheel slip, gyroscope drift and integration.
 * 
 * Thread safety: The estimation task is running in a Notifier thread.  Remote position update are
 * coming through another thread and the update (publish) is the main robot thread.  For thread safety,
 * synchronize on the <code>INSTANCE</code> object.  Use the <code>updateRobotPose</code> for changing 
 * the state.
 *
 */

@SuppressWarnings("unused")
public class RobotPosition implements Sendable
{
	public static final RobotPosition INSTANCE = new RobotPosition();
	
	private SRXMagneticEncoderRelative	leftMaster, rightMaster;
	private NavX						navx;
	private String						name = "RobotPose", subSystem = "Ungrouped";

	// used for setting the cyclic rate of the state estimate loop 
	private static final double UPDATE_PERIOD = 0.01;
	
	// robot state variable
	private final Pose2D robotPose = new Pose2D();
	private final RobotSpeed robotSpeed = new RobotSpeed();

	// used to calculate delta time
	private double time = Timer.getFPGATimestamp();
	
	// private singleton constructor
	private RobotPosition() 
	{
	}
	
	/** Called once to configure this class and start position tracking.
	 * @param leftMaster Left SRX Magnetic Encoder.
	 * @param rightMaster Right SRX Magnetic Encoder.
	 * @param navx
	 */
	public void init(SRXMagneticEncoderRelative leftMaster, SRXMagneticEncoderRelative rightMaster, NavX navx)
	{
		this.leftMaster = leftMaster;
		this.rightMaster = rightMaster;
		this.navx = navx;
		
		// Start thread to estimate state at relative fast periodic rate.
		Notifier timer = new Notifier(() -> 
		{
			estimateState();
		});
		
		timer.startPeriodic(UPDATE_PERIOD);
		
		configureRemoteReset();
	}
		
	/**
	 * Return the RobotPosition singleton instance
	 */
	public static RobotPosition getInstance() 
	{
		return INSTANCE;
	}
	
	/**
	 * This is only used to facilitate reseting or moving the robot
	 * to known position which can be called from the simulation or the dash-board
	 */
	private void configureRemoteReset() 
	{		
		// this is the only coupling to the Simulation
		SmartDashboard.getEntry("ResetRobotPoseCommand").addListener(new Consumer<EntryNotification>() 
		{
			
			private double[] reset = new double[3];
			
			@Override
			public void accept(EntryNotification t) 
			{
				NetworkTableEntry entry = t.getEntry();
				reset = entry.getDoubleArray(reset);
				
				// reset the robot pose.
				resetRobotPose(reset[0], reset[1], reset[2]);
			}
			
		}, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kLocal);	
	}
	
	/**
	 * Reset robot pose to a known position.
	 * @param x - inches (starting at 0 on blue and ending at 648 on red end).
	 * @param y - inches (starting at 0 on scoring table side and ending at 324).
	 * @param heading - degrees.
	 */
	public void resetRobotPose(double x, double y, double heading) 
	{
		Util.consoleLog(String.format("Robot Position Reset: %3.1f, %3.1f, %3.1f", x, y, heading));
		
		updateRobotPose(x, y, heading, 0, 0);
	}

	/**
	 * Robot pose should only be updated through this method for thread safety.
	 * @param x - X position in inches (starting at 0 on blue and ending at 648 on red end).
	 * @param y - Y position in inches (starting at 0 on scoring table side and ending at 324).
	 * @param heading - degrees.
	 */
	private void updateRobotPose(double x, double y, double heading, double fwd, double rot) 
	{
		// different threads can be calling this position update method so it must be thread safe.
		synchronized(INSTANCE) 
		{
			robotPose.x = x;
			robotPose.y = y;
			robotPose.heading = heading;
			robotSpeed.forward = fwd;
			robotSpeed.rotational = rot;
		}
	}
		
	/**
	 * Estimate robot state (pose and velocities).
	 */
	private void estimateState() 
	{			
		// update times
		double time = Timer.getFPGATimestamp();
		double dT = time - this.time;
		this.time = time;
		
		// sample odometry and gyroscope.
		double forwardVelocity = getVelocity();
		// Round degrees/sec to 1 dec place to get rid of noise values.
		double rotationalVelocity = Util.round(navx.getYawRate(), 1, BigDecimal.ROUND_DOWN);
		double deltaForward = forwardVelocity * dT;
		double deltaHeading = rotationalVelocity * dT;				
		
		// update state, reading and writing state in thread consistent manner
		synchronized(INSTANCE) 
		{			
			// update velocities.
			robotSpeed.forward = forwardVelocity;
			robotSpeed.rotational = rotationalVelocity;
			
			// using average heading, update position components using trigonometry.
			double averageHeading = Math.toRadians(robotPose.heading + deltaHeading / 2);
			
			//Util.consoleLog("df=%2.4f  dh=%2.4f  dt=%2.4f", deltaForward, deltaHeading, dT);
			robotPose.x += deltaForward * Math.cos(averageHeading);
			robotPose.y += deltaForward * Math.sin(averageHeading);
			
			// update heading.
			robotPose.heading = MathUtil.normalizeAngleDegrees(robotPose.heading + deltaHeading);
		}
	}
	
	/**
	 * Returns averaged left/right velocity.
	 * @return Velocity in inches/second.
	 */
	private double getVelocity() 
	{
		double leftVelocity  = leftMaster.getVelocity(PIDRateType.velocityIPS);
		double rightVelocity = rightMaster.getVelocity(PIDRateType.velocityIPS);
		double velocity = (leftVelocity + rightVelocity) / 2;
		
		// Round to 1 dec place to get rid of noise values.
		return Util.round(velocity, 1, BigDecimal.ROUND_DOWN);
	}
	
	/**
	 * Get last computed robot pose.
	 * @return Pose2D object.
	 */
	public Pose2D getPose()
	{
		if (robotPose == null) return null;
		
		Pose2D	pose = new Pose2D();
		
		synchronized(INSTANCE) 
		{
			pose.x = robotPose.x;
			pose.y = robotPose.y;
			pose.heading = robotPose.heading;
		}
		
		return pose;
	}
	
	/**
	 * Get last measured robot speed.
	 * @return RobotSpeed object.
	 */
	public RobotSpeed getSpeed()
	{
		if (robotSpeed == null) return null;
		
		RobotSpeed	speed = new RobotSpeed();
		
		synchronized(INSTANCE) 
		{
			speed.forward = robotSpeed.forward;
			speed.rotational = robotSpeed.rotational;
		}
		
		return speed;
	}
	
	/**
	 * Manually send current pose to dashboard.
	 */
	public void updateDashboard() 
	{		
		// publish estimated robot pose to dash-board with thread safety in mind. 
		Pose2D pose =  getPose();
		RobotSpeed speed  = getSpeed();

		SmartDashboard.putNumberArray("RobotPose", new double[] {pose.x, pose.y, pose.heading});
		SmartDashboard.putNumberArray("RobotVelocities", new double[] {speed.forward, speed.rotational});
		Util.consoleLog("vel f=%2.4f  rot=%2.4f", speed.forward, speed.rotational);
	}

	// Functions that implement the Sendable interface.
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName( String name )
	{
		this.name = name;
	}

	@Override
	public String getSubsystem()
	{
		return subSystem;
	}

	@Override
	public void setSubsystem( String subsystem )
	{
		subSystem = subsystem;
	}

	@Override
	public void initSendable( SendableBuilder builder )
	{
		Pose2D pose =  getPose();
		//RobotSpeed speed  = getSpeed();

		builder.setSmartDashboardType("Pose");
	    builder.addDoubleProperty("x", () -> pose.x, null);
	    builder.addDoubleProperty("y", () -> pose.y, null);
	    builder.addDoubleProperty("hdg", () -> pose.heading, null);
	}
}
