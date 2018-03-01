package Team4450.Robot11;

import Team4450.Lib.*;
import Team4450.Robot11.Devices;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Grabber
{
	private final Robot	robot;
	private boolean		grabberOpen, grabberDeployed, intake, spit, autoIntake;
	private Thread		autoIntakeThread;
	
	public Grabber(Robot robot)
	{
		Util.consoleLog();
				
		this.robot = robot;
	  
		stopMotors();
		
		retract();
		
		open();
	}

	public void dispose()
	{
		Util.consoleLog();
		
		if (autoIntakeThread != null) stopAutoIntake();
	}
		
	public void open()
	{
		Util.consoleLog();

		grabberOpen = true;
		
		SmartDashboard.putBoolean("Grabber", grabberOpen);

		Devices.grabberValve.SetA();
	}
	
	public void close()
	{
		Util.consoleLog();
		
		grabberOpen =  false;
		
		Devices.grabberValve.SetB();

		updateDS();
	}
	
	public void retract()
	{
		Util.consoleLog();
		
		grabberDeployed = false;

		Devices.deployValve.SetA();
		
		updateDS();
	}
	
	public void deploy()
	{
		Util.consoleLog();
		
		grabberDeployed = true;
		
		Devices.deployValve.SetB();

		updateDS();
	}
	
	public void motorsIn(double power)
	{
		Util.consoleLog("%.2f", power);
		
		intake = true;
		spit = false;

		Devices.grabberGroup.set(power);
		
		updateDS();
	}

	public void motorsOut(double power)
	{
		Util.consoleLog("%.2f", power);
		
		intake = false;
		spit = true;

		Devices.grabberGroup.set(-power);

		updateDS();
	}
	
	public void spit(double power)
	{
		Util.consoleLog("%.2f", power);
		
		motorsOut(power);
		
		Timer.delay(2);
		
		stopMotors();
	}
	
	public void stopMotors()
	{
		Util.consoleLog();
		
		intake = false;
		spit = false;

		Devices.grabberGroup.set(0);
		
		updateDS();
	}
	
	public boolean isOpen()
	{
		return grabberOpen;
	}
	
	public boolean isDeployed()
	{
		return grabberDeployed;
	}
	
	public boolean isIntake()
	{
		return intake;
	}
	
	public boolean isSpit()
	{
		return spit;
	}
	
	private void updateDS()
	{
		SmartDashboard.putBoolean("Grabber", grabberOpen);
		SmartDashboard.putBoolean("Deployed", grabberDeployed);
		SmartDashboard.putBoolean("Intake", intake);
		SmartDashboard.putBoolean("Spit", spit);
		SmartDashboard.putBoolean("AutoGrab", autoIntake);
	}
	
	public boolean isAutoIntakeRunning()
	{
		return autoIntake;
	}
	
	/**
	 * Start auto cube Intake thread.
	 */
	public void startAutoIntake()
	{
		Util.consoleLog();
		
		if (autoIntakeThread != null) return;

		autoIntakeThread = new AutoIntake();
		autoIntakeThread.start();
	}
	
	/**
	 * Stops auto Intake thread.
	 */
	public void stopAutoIntake()
	{
		Util.consoleLog();

		if (autoIntakeThread != null) autoIntakeThread.interrupt();
		
		autoIntakeThread = null;
	}

	//----------------------------------------
	// Automatic cube Intake thread.
	
	private class AutoIntake extends Thread
	{
		AutoIntake()
		{
			Util.consoleLog();
			
			this.setName("AutoCubeIntake");
	    }
		
	    public void run()
	    {
	    	Util.consoleLog();
	    	
	    	try
	    	{
	    		autoIntake = true;
	    		
	    		updateDS();
	    		
	    		motorsIn(.50);
	    		sleep(250);
	    		
    	    	while (!isInterrupted() && Devices.intakeMotorL.getOutputCurrent() < 15.0) // 5.0
    	    	{
    	            // We sleep since JS updates come from DS every 20ms or so. We wait 50ms so this thread
    	            // does not run at the same time as the teleop thread.
    	    		LCD.printLine(9, "cube motor current=%f", Devices.intakeMotorL.getOutputCurrent());
    	            sleep(50);
    	    	}
    	    	
    	    	if (!interrupted()) Util.consoleLog("  Cube detected");
    	    	
    	    	sleep(500);
	    	}
	    	catch (InterruptedException e) { stopMotors(); }
	    	catch (Throwable e) { e.printStackTrace(Util.logPrintStream); }
	    	finally { stopMotors(); }
			
	    	autoIntake = false;
			autoIntakeThread = null;
			updateDS();
	    }
	}	// end of AutoIntake thread class.

}

