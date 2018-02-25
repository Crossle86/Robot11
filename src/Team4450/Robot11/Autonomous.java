
package Team4450.Robot11;

import Team4450.Lib.*;
import Team4450.Robot11.Devices;
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
	private final double	spitPower = 1.0;
	
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
        
        // Set NavX to heading 0.
		Devices.navx.resetYaw();

		Devices.navx.setHeading(0);

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
		}
		
		Util.consoleLog("end");
	}

	// Start from left or right and just drive across the line.
	private void startOutsideNoScore()
	{
		Util.consoleLog();
		
		autoDrive(-.50, 2490, true);	// 1620
	}

	// Start from center (offset right). Move forward a bit to get off the wall. 
	// Turn right 45, drive forward to break the line and stop.
 
	private void startCenterNoScore()
	{
		Util.consoleLog();
		
		autoDrive(-.30, 1970, true);	// 1250
		
		//autoRotate(.50, -45);
		
		//autoDrive(.50, 1000, true);
	}

	// Start from center (offset right). Evaluate game information. Determine which switch we
	// should score on. Navigate by moving a bit off the wall, turn 90 in correct direction, 
	// drive forward correct amount, turn 90 in correct direction, raise cube, drive forward 
	// to the switch wall, dump cube.

	private void startCenterScore()
	{
		Util.consoleLog(plateState.toString());
		
		// May need initial cube lift or lift to scoring height.
		// lift.setHeight(500);
		// delay?
		
		autoDrive(-.40, 925, true);		// 595
		
		switch (plateState)
		{
			case UNDEFINED:
				startCenterNoScore();
				return;
				
			case LLL: case LRL:
				autoRotate(.50, 90);
				autoDrive(-.50, 1028, true);	// 670
				autoRotate(-.50, 90);
				autoDrive(-.30, 880, true);		// 720
				break;
				
			case RRR: case RLR:
				autoRotate(-.50, 90);
				autoDrive(-.50, 1330, true);
				autoRotate(.50, 90);
				autoDrive(-.30, 880, true);
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
		
		// May need initial cube lift.
		
		if (startingLeft) 
		{
			switch (plateState)
			{
				case UNDEFINED:
					return;
					
				//case LLL: case RLR:	// Scale available.
					// Lift cube to scoring height. Assume async.
					// lift.setHeight(2000);
					//autoDrive(.50, 1000, true);
					//autoRotate(.50, -90);
					//autoDrive(.50, 1000, true);
					//break;
					
				case RRR:  case RLR:	// No plate available.
					autoDrive(-.50, 4600, true);	// 3090
					autoRotate(-.50, 90);
					autoDrive(-.50, 1470, true);	// 960
					return;
					
				case LRL: case LLL:		// Switch available.
					// Lift cube to scoring height. Assume async.
					autoDrive(-.50, 3180, true);	// 2050
					autoRotate(-.50, 90);
					autoDrive(-.30, 320, true);		// 200
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
					autoDrive(-.50, 4600, true);	// 3090
					autoRotate(.50, 90);
					autoDrive(-.50, 1470, true);	// 960
					return;
					
//				case RRR: case LRL:	// Scale available.
//					// Lift cube to scoring height. Assume async.
//					autoDrive(.50, 1000, true);
//					autoRotate(.50, 90);
//					autoDrive(.50, 1000, true);
//					break;
//					
				case RLR: case RRR:	// Switch available.
					// Lift cube to scoring height. Assume async.
					autoDrive(-.50, 3180, true);	// 2050
					autoRotate(.50, 90);
					autoDrive(-.30, 320, true);		// 200
					break;
			}
		}
		
		// Dump cube.
		
		grabber.spit(spitPower);
	}
	
	// Auto drive in set direction and power for specified encoder count. Stops
	// with or without brakes on CAN bus drive system. Uses NavX to go straight.
	
	private void autoDrive(double power, int encoderCounts, boolean enableBrakes)
	{
		int		angle;
		double	gain = .05;
		
		Util.consoleLog("pwr=%.2f, count=%d, brakes=%b", power, encoderCounts, enableBrakes);

		Devices.SetCANTalonBrakeMode(enableBrakes);

		Devices.wheelEncoder.reset();
		Devices.wheelEncoder2.reset();
		Devices.navx.resetYaw();
		
		while (isAutoActive() && Math.abs(Devices.wheelEncoder.get()) < encoderCounts) 
		{
			LCD.printLine(4, "encoder=%d", Devices.wheelEncoder.get());
			
			// Angle is negative if robot veering left, positive if veering right when going forward.
			// It is opposite when going backward. Note that for this robot, - power means forward and
			// + power means backward.
			
			angle = (int) Devices.navx.getYaw();
			
			// Invert angle for backwards.
			
			if (power > 0) angle = -angle;

			LCD.printLine(5, "angle=%d", angle);
			
			//Util.consoleLog("angle=%d", angle);
			
			// Note we invert sign on the angle because we want the robot to turn in the opposite
			// direction than it is currently going to correct it. So a + angle says robot is veering
			// right so we set the turn value to - because - is a turn left which corrects our right
			// drift.
			
			Devices.robotDrive.curvatureDrive(power, angle * gain, false);
			
			Timer.delay(.020);
		}

		Devices.robotDrive.tankDrive(0, 0, true);				
		
		Util.consoleLog("end: actual count=%d", Math.abs(Devices.wheelEncoder.get()));
	}
	
	// Auto rotate left or right the specified angle. Left/right from robots forward view.
	// Turn right, power is -
	// Turn left, power is +
	// angle of rotation is always +.
	
	private void autoRotate(double power, int angle)
	{
		Util.consoleLog("pwr=%.2f  angle=%d", power, angle);
		
		Devices.navx.resetYaw();
		
		Devices.robotDrive.tankDrive(power, -power);

		while (isAutoActive() && Math.abs((int) Devices.navx.getYaw()) < angle) {Timer.delay(.020);} 
		
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
}