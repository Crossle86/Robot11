package Team4450.Robot11;

import org.opencv.core.Mat;

import Team4450.Lib.Util;

public class ExternalCameraFeed extends Thread
{
	private Robot	robot;
	
	public ExternalCameraFeed(Robot robot)
	{
		this.robot = robot;
	}

	public void run()
	{
		Mat	image;
		
		Util.consoleLog();
		
		while (!isInterrupted())
		{
			image = robot.cameraThread.getImage();
			
			if (image != null) robot.cameraThread.putImage(image);
		}
	}
}
