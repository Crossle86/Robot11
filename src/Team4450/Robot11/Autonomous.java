
package Team4450.Robot11;

import Team4450.Lib.*;
import Team4450.Robot11.Devices;
import edu.wpi.first.wpilibj.DriverStation.MatchType;
//import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
//import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous
{
	private final Robot		robot;
	private final int		program = (int) SmartDashboard.getNumber("AutoProgramSelect",0);
	private PlateStates		plateState;
	private final Lift		lift;
	private final Grabber	grabber;
	private final GearBox	gearBox;
	private final double	spitPower = .50;
	
	Autonomous(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		gearBox = new GearBox(robot);
		
		lift = new Lift(robot);
		
		grabber = new Grabber(robot);
	}

	public void dispose()
	{
		Util.consoleLog();
		
		if (gearBox != null) gearBox.dispose();
		if (lift != null) lift.dispose();
		if (grabber != null) grabber.dispose();
	}
	
	private boolean isAutoActive()
	{
		return robot.isEnabled() && robot.isAutonomous();
	}

	public void execute()
	{
		Util.consoleLog("Alliance=%s, Location=%d, Program=%d, FMS=%b, msg=%s", robot.alliance.name(), robot.location, program, 
				Devices.ds.isFMSAttached(), robot.gameMessage);
		LCD.printLine(2, "Alliance=%s, Location=%d, FMS=%b, Program=%d, msg=%s", robot.alliance.name(), robot.location, 
				Devices.ds.isFMSAttached(), program, robot.gameMessage);
		
		// Get the randomized scoring plate state.
		
		try
		{
			plateState = PlateStates.valueOf(robot.gameMessage);
		}
		catch (Exception e) { plateState = PlateStates.UNDEFINED; }
		
		Devices.robotDrive.setSafetyEnabled(false);

		// Initialize encoder.
		Devices.wheelEncoder.reset();
        
       // Set NavX yaw tracking to 0.
		Devices.navx.resetYaw();

		// Set heading to initial angle (0 is robot pointed down the field) so
		// NavX class can track which way the robot is pointed during the match.
		Devices.navx.setHeading(0);
		
		// Set Talon ramp rate for smooth acceleration from stop.
		//Devices.SetCANTalonRampRate(0.5);
		
		// Determine which auto program to run as indicated by driver station.
		switch (program)
		{
			case 0:		// No auto program.
				break;

			case 1:		// Start outside (either side) no scoring.
				startOutsideNoScore();
				break;
			
			case 2:		// Start center no scoring.
				startCenterNoScore();
				break;
			
			case 3:		// Start center score cube.
				startCenterScore();
				break;
			
			case 4:		// Start left outside score cube.
				startOutsideScore(true);
				break;

			case 5:		// Start right outside score cube.
				startOutsideScore(false);
				break;
				
			case 6:		// Start center score cube.
				startCenterScoreFast();
				break;
				
			case 7:		// Start center score cube.
				startCenterScoreCurve();
				break;
		}
		
		
		Util.consoleLog("y=%.2f  x=%.2f", Devices.navx.getAHRS().getDisplacementY(), Devices.navx.getAHRS().getDisplacementX());
		
		Util.consoleLog("end");
	}

	// Start from left or right and just drive across the line.
	private void startOutsideNoScore()
	{
		Util.consoleLog();
		
		autoDrive(.50, 2490, true);	// 1606
	}

	// Start from center (offset right). Move forward a bit to get off the wall. 
	// Turn right 45, drive forward to break the line and stop.
 
	private void startCenterNoScore()
	{
		Util.consoleLog();
		
		autoDrive(.30, 1970, true);	// 1270
	}

	// Start from center (offset right). Evaluate game information. Determine which switch we
	// should score on. Navigate by moving a bit off the wall, turn 90 in correct direction, 
	// drive forward correct amount, turn 90 in correct direction, raise cube, drive forward 
	// to the switch wall, dump cube.

	private void startCenterScore()
	{
		Util.consoleLog(plateState.toString());
		
		// close, deploy grabber then lift.
		
		grabber.close();
		grabber.deploy();
		Timer.delay(0.5);
		
		if (robot.isClone)
			lift.setHeight(9100);
		else
			lift.setHeight(7900);
		
		autoDrive(.40, 925, true);		// 596
		
		switch (plateState)
		{
			case UNDEFINED:
				startCenterNoScore();
				return;
				
			case LLL: case LRL:
				autoRotate(-.50, 90);
				autoDrive(.60, 928, true);		// 663
				autoRotate(.50, 90);
				autoDrive(.40, 880, true);		// 567
				break;
				
			case RRR: case RLR:
				autoRotate(.50, 90);
				autoDrive(.60, 900, true);		// 857
				autoRotate(-.50, 90);
				autoDrive(.40, 880, true);		// 567
				break;
		}
		
		// Dump cube.
		
		grabber.spit(spitPower);
	}

	private void startCenterScoreFast()
	{
		Util.consoleLog(plateState.toString());
		
		// close, deploy grabber then lift.
		
		grabber.close();
		grabber.deploy();
		Timer.delay(0.5);
		
		if (robot.isClone)
			lift.setHeight(9100);
		else
			lift.setHeight(7900);
		
		autoDrive(.40, 100, true);		// 596
		
		switch (plateState)
		{
			case UNDEFINED:
				startCenterNoScore();
				return;
				
			case LLL: case LRL:
				autoRotate(-.50, 26);
				autoDrive(.60, 2100, true);	// 663
				//autoRotate(-.50, 90);
				//autoDrive(.40, 880, true);		// 567
				break;
				
			case RRR: case RLR:
				autoRotate(.50, 18);
				autoDrive(.60, 1900, true);	// 857
				//autoRotate(.50, 12);
				//autoDrive(.40, 880, true);		// 567
				break;
		}
		
		// Dump cube.
		
		grabber.spit(spitPower);
		
	}

	private void startCenterScoreCurve()
	{
		Util.consoleLog(plateState.toString());
		
		// close, deploy grabber then lift.
		
		grabber.close();
		grabber.deploy();
		Timer.delay(0.5);
		
		if (robot.isClone)
			lift.setHeight(9100);
		else
			lift.setHeight(7900);
		
		switch (plateState)
		{
			case UNDEFINED:
				startCenterNoScore();
				return;
				
			case LLL: case LRL:
				autoSCurve(.50, -.3, 30, 900);

				break;
				
			case RRR: case RLR:
				autoSCurve(.50, .3, 30, 950);

				break;
		}
		
		// Dump cube.
		
		grabber.spit(spitPower);
	}

	// Start left or right. Evaluate game information. Determine if we should score on the switch, 
	// scale, or not at all. For not at all, drive forward until aligned with the platform area, 
	// turn right 90, drive forward into the platform area as far as we can get toward the scale on 
	// opposite side of the field. For score scale drive forward raising cube until aligned with scale
	// front, turn right 90, drive to scale drop position, drop cube. For score switch drive forward
	// raising cube until aligned with switch front, turn right 90, drive to switch wall, drop cube.

	private void startOutsideScore(boolean startingLeft)
	{
		Util.consoleLog("start left=%b, plate=%s", startingLeft, plateState.toString());
		
		// close, deploy grabber then lift.
		
		grabber.close();
		grabber.deploy();
		Timer.delay(0.5);
		
		if (robot.isClone)
			lift.setHeight(9100);
		else
			lift.setHeight(7900);
		
		if (startingLeft) 
		{
			switch (plateState)
			{
				case UNDEFINED:
					return;
					
				//case LLL: case RLR:	// Scale available.
					//autoDrive(.50, 1000, true);
					//autoRotate(.50, -90);
					//autoDrive(.50, 1000, true);
					//break;
					
				case RRR:  case RLR:	// No plate available.
					autoDrive(.50, 2500, true);	// 4600/2967
					//autoRotate(-.50, 90);
					//autoDrive(-.50, 1470, true);	// 948
					
					// Drop the lift.
					lift.setHeight(-1);
//					lift.setHeight(0);
//					Timer.delay(3.0);
					return;
					
				case LRL: case LLL:		// Switch available.
					autoDrive(.50, 3180, true);	// 2051
					autoRotate(-.50, 90);
					autoDrive(.30, 320, true);		// 206
					break;
			}
		}
		else
		{
			switch (plateState)
			{
				case UNDEFINED:
					return;
					
				case LLL: case LRL:	// No plate available.
					autoDrive(.50, 2500, true);	// 4600/2967
					//autoRotate(.50, 90);
					//autoDrive(-.50, 1470, true);	// 948
					
					// Drop the lift.
					lift.setHeight(-1);
					//Timer.delay(3.0);
					return;
					
//				case RRR: case LRL:	// Scale available.
//					autoDrive(.50, 1000, true);
//					autoRotate(.50, 90);
//					autoDrive(.50, 1000, true);
//					break;
//					
				case RLR: case RRR:	// Switch available.
					autoDrive(.50, 3180, true);	// 2051
					autoRotate(.50, 90);
					autoDrive(.30, 320, true);		// 206s
					break;
			}
		}
		
		// Dump cube.
		
		grabber.spit(spitPower);
		
	}
	
	/**
	 * Auto drive straight in set direction and power for specified encoder count. Stops
	 * with or without brakes on CAN bus drive system. Uses NavX to drive straight.
	 * @param power Speed, + is forward.
	 * @param encoderCounts encoder counts to travel.
	 * @param enableBrakes True to enable brakes.
	 */
	private void autoDrive(double power, int encoderCounts, boolean enableBrakes)
	{
		int		angle;
		double	gain = .05;
		int		error = 0;
		double	power2 = 0, pFactor, kP = .0002, minPower = .10;

		// Min power is determined experimentally for each robot as the lowest power that
		// will move the robot. We don't want the pid reduction in power at the end of
		// the drive to fall below this level and cause the drive to stall before done.
		
		Util.consoleLog("pwr=%.2f, count=%d, brakes=%b", power, encoderCounts, enableBrakes);

		Devices.SetCANTalonBrakeMode(enableBrakes);

		Devices.wheelEncoder.reset();
		Devices.wheelEncoder2.reset();
		
		if (robot.isClone) Timer.delay(0.3);
		
		Util.consoleLog("before reset=%.2f  y2=%.2f", Devices.navx.getYaw(), Devices.navx.getYaw2());
			
		Devices.navx.resetYaw();
		Devices.navx.resetYaw2();
		
		//Devices.navx.resetYawWait(1, 50);
		
		Util.consoleLog("after reset=%.2f  y2=%.2f", Devices.navx.getYaw(), Devices.navx.getYaw2());
		
		while (isAutoActive() && Math.abs(Devices.wheelEncoder.get()) < encoderCounts) 
		{
			LCD.printLine(4, "wheel encoder=%d  winch encoder=%d", Devices.wheelEncoder.get(), Devices.winchEncoder.get());

			// Quick and dirty PID control to reduce power as we approach target encoder counts.
			error = encoderCounts - Math.abs(Devices.wheelEncoder.get());
			pFactor = error * kP;
			power2 = power * pFactor;
			Util.clampValue(power2, minPower, power);
			Util.consoleLog("error=%d pfactor=%.2f power2=%.2f", error, pFactor, power2);

			// Angle is negative if robot veering left, positive if veering right when going forward.
			// It is opposite when going backward. Note that for this robot, - power means forward and
			// + power means backward.
			
			angle = (int) Devices.navx.getYaw();
			
			// Invert angle for backwards.
	
			if (power < 0) angle = -angle;

			LCD.printLine(5, "angle=%d", angle);
			
			Util.consoleLog("angle=%d  y2=%.2f", angle, Devices.navx.getYaw2());
			
			// Note we invert sign on the angle because we want the robot to turn in the opposite
			// direction than it is currently going to correct it. So a + angle says robot is veering
			// right so we set the turn value to - because - is a turn left which corrects our right
			// drift.
			
			// Update: The new curvatureDrive function expects the power to be + for forward motion.
			// Since our power value is - for forward, we do not invert the sign of the angle like
			// we did with previous drive functions. This code base should be updated to fix the
			// Y axis sign to be + for forward. This would make more sense and simplify understanding
			// the code and would match what curvatureDrive expects. Will wait on that until after
			// 2018 season. After fixing that, the angle would again need to be inverted.
			
			// Done in this branch for testing 4-23-18.
			
			Devices.robotDrive.curvatureDrive(power, -angle * gain, false);
			
			Timer.delay(.010);
		}

		Devices.robotDrive.tankDrive(0, 0);				
		
		Util.consoleLog("end: actual count=%d", Math.abs(Devices.wheelEncoder.get()));
	}
	
	/**
	 * Auto rotate left or right the specified angle. Left/right from robots forward looking view.
	 * @param power Speed of rotation, + is rotate right, - is rotate left.
	 * @param angle Angle to rotate, always +.
	 */
	private void autoRotate(double power, int angle)
	{
		int		error = 0;
		double	power2 = 0, pFactor, kP = .02, minPower = .10;

		// Min power is determined experimentally for each robot as the lowest power that
		// will rotate the robot. We don't want the pid reduction in power at the end of
		// the rotation to fall below this level and cause the rotation to stall before done.
		
		Util.consoleLog("pwr=%.2f  angle=%d", power, angle);
		
		// Try to prevent over rotation.
		Devices.SetCANTalonBrakeMode(true);

		Devices.navx.resetYaw();
		Devices.navx.resetYaw2();
		
		// Start rotation.
		//Devices.robotDrive.tankDrive(power, -power);
		
		angle = navxFix(angle);

		while (isAutoActive() && Math.abs((int) Devices.navx.getYaw()) < angle) 
		{
			// Quick and dirty PID control to reduce power as we approach target angle.
			error = angle - Math.abs((int) Devices.navx.getYaw());
			pFactor = error * kP;
			power2 = power * pFactor;
			Util.clampValue(power2, minPower, power);
			Devices.robotDrive.tankDrive(power, -power);
			Util.consoleLog("angle=%f error=%d pfactor=%.2f power2=%.2f y2=%.2f", Devices.navx.getYaw(), error, pFactor, 
					power2, Devices.navx.getYaw2());
			Timer.delay(.010);
		} 

		Util.consoleLog("end angle1=%f y2=%.2f", Devices.navx.getYaw(), Devices.navx.getYaw2());
		
		// Stop rotation.
		Devices.robotDrive.tankDrive(0, 0);

		Util.consoleLog("end angle2=%f y2=%.2f", Devices.navx.getYaw(), Devices.navx.getYaw2());

		// Wait for robot to stop moving.
		while (isAutoActive() && Devices.navx.isRotating()) {Timer.delay(.010);}
		
		Util.consoleLog("end angle3=%f y2=%.2f", Devices.navx.getYaw(), Devices.navx.getYaw2());
	}
	
	/**
	 * Drive in S curve, curve one direction, drive straight, curve back to starting heading.
	 * @param power Speed to drive, + is forward.
	 * @param curve Rate of rotation (-1.0 <-> 1.0), + is right or clockwise.
	 * @param targetAngle Angle to turn, always +.
	 * @param straightEncoderCounts Counts to travel on straight leg, always +.
	 */
	private void autoSCurve(double power, double curve, int targetAngle, int straightEncoderCounts)
	{
		double	gain = .05;
		
		Util.consoleLog("pwr=%.2f  curve=%.2f  angle=%d  counts=%d", power, curve, targetAngle, straightEncoderCounts);
		
		targetAngle = navxFix(targetAngle);
		
		// We start out driving in a curve until we have turned the desired angle.
		// Then we drive straight the desired distance then curve back to starting
		// heading. Curve is + for right, - for left.
		
		Devices.robotDrive.curvatureDrive(power, curve, false);
		
		while (isAutoActive() && Math.abs((int) Devices.navx.getYaw()) < targetAngle) 
		{
			Timer.delay(.010);
			LCD.printLine(6, "angle=%.2f", Devices.navx.getYaw());
			Util.consoleLog("angle=%.2f  hdg=%.2f", Devices.navx.getYaw(), Devices.navx.getHeading());
		}

		Util.consoleLog("end angle=%.2f  hdg=%.2f", Devices.navx.getYaw(), Devices.navx.getHeading());

		autoDrive(power, straightEncoderCounts, false);
		
		Devices.navx.resetYaw();
		
		Devices.SetCANTalonBrakeMode(true);

		// Reduce target angle to stop over rotation.
		targetAngle -= 10;
		
		// Reduce power so don't slam the switch too hard.
		Devices.robotDrive.curvatureDrive(power * 0.7, -curve, false);
		
		while (isAutoActive() && Math.abs((int) Devices.navx.getYaw()) < targetAngle && (Devices.ds.getMatchType() != MatchType.None ? Devices.ds.getMatchTime() > 5 : true)) 
		{
			Timer.delay(.020);
			LCD.printLine(6, "angle=%.2f", Devices.navx.getYaw());
			Util.consoleLog("angle=%.2f  hdg=%.2f", Devices.navx.getYaw(), Devices.navx.getHeading());
		}

		Util.consoleLog("end angle=%.2f  hdg=%.2f", Devices.navx.getYaw(), Devices.navx.getHeading());

		Devices.robotDrive.tankDrive(0, 0);
	}
	
	private enum PlateStates
	{
		UNDEFINED,
		LLL,
		RRR,
		LRL,
		RLR;
 	}
	
	// Correction for clone navx which reads 15 degrees to low. This routine adjusts
	// the target angle for the error.
	
	private int navxFix(int targetAngle)
	{
		if (robot.isComp)
			return targetAngle;
		else
		{
			Util.consoleLog("ta=%d  fa=%d", targetAngle, Math.round(targetAngle * .8333f));
			return Math.round(targetAngle * .8333f);
		}
	}
}