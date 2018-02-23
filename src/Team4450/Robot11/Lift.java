package Team4450.Robot11;

import Team4450.Lib.*;
import Team4450.Robot11.Devices;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Lift
{
	private final Robot			robot;
	private boolean				climbWinch = true, holdingPosition, holdingHeight;
	private final PIDController	liftPidController;
	
	public Lift(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;

		liftPidController = new PIDController(0.0, 0.0, 0.0, Devices.winchEncoder, Devices.climbWinch);
		
		updateDS();
	}

	public void dispose()
	{
		Util.consoleLog();
		
		liftPidController.disable();
		liftPidController.free();
	}
	
	public boolean isClimbWinchSelected()
	{
		Util.consoleLog();
		
		return climbWinch;
	}
	
	public void selectClimbWinch()
	{
		Util.consoleLog();

		setWinchPower(0);

		climbWinch = true;
		
		updateDS();
	}
	
	public void selectLiftWinch()
	{
		Util.consoleLog();

		climbWinch = false;
		
		updateDS();
	}
	
	public void updateDS()
	{
		SmartDashboard.putBoolean("Winch", climbWinch);
	}
	
	public void setWinchPower(double power)
	{
		if (climbWinch)
			Devices.climbWinch.set(power);
		else
			Devices.liftWinch.set(power);
	}
	
	public void releaseForks()
	{
		Util.consoleLog();

	}
	
	// Automatically move lift to specified encoder count and hold it there.
	public void setHeight(int count)
	{
		Util.consoleLog("%d", count);
		
		if (count != 0)
		{
			if (isHoldingPosition()) holdPosition(0);
			
			// p,i,d values are a guess.
			// f value is the motor power to apply to move to encoder target count.
			// Setpoint is the target encoder count.
			// The idea is that the difference between the current encoder count and the
			// target count will apply power to bring the two counts together and stay there.
			liftPidController.setPID(0.01, 0.001, 0.0, .50);
			liftPidController.setSetpoint(count);
			liftPidController.setPercentTolerance(5);	// 5% error.
			liftPidController.enable();
			holdingHeight = true;
		}
		else
		{
			liftPidController.disable();
			holdingHeight = false;
		}
	}
	
	public boolean isHoldingHeight()
	{
		return holdingHeight;
	}
	
	public boolean isHoldingPosition()
	{
		return holdingPosition;
	}
	
	// Automatically hold lift position at specified power level.
	void holdPosition(double speed)
	{
		Util.consoleLog("%f", speed);
		
		if (speed != 0)
		{
			if (isHoldingHeight()) setHeight(0);
			
			// p,i,d values are a guess.
			// f value is the base motor speed, which is where (power) we want to hold position.
			// Setpoint is current encoder count.
			// The idea is that any encoder motion will alter motor base speed to hold position.
			liftPidController.setPID(0.01, 0.001, 0.0, speed);
			liftPidController.setSetpoint(Devices.winchEncoder.get());
			liftPidController.setPercentTolerance(5);	// 5% error.
			liftPidController.enable();
			holdingPosition = true;
		}
		else
		{
			liftPidController.disable();
			holdingPosition = false;
		}
	}
}

