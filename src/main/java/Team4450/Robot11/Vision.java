package Team4450.Robot11;

import org.opencv.core.Mat;

import Team4450.Lib.Util;

public class Vision 
{
	private Robot robot;
	
	// This variable and method make sure this class is a singleton.
	
	public static Vision vision = null;
	
	private GripPowerUpBlockPipeline	pipeline = new GripPowerUpBlockPipeline();
	
	public static Vision getInstance(Robot robot) 
	{
		if (vision == null) vision = new Vision(robot);
		
		return vision;
	}
	
	// This is the rest of the class.
	
	private Vision(Robot robot) 
	{
		this.robot = robot;
		
		Util.consoleLog("Vision created!");
	}
	
	public void seekBlock()
	{
		Mat			currentImage;
		
		Util.consoleLog();
		
		currentImage = robot.cameraThread.getCurrentImage();

		pipeline.process(currentImage);
		
		if (pipeline.findContoursOutput().size() > 0)
			robot.cameraThread.setContours(pipeline.findContoursOutput());
		else
			robot.cameraThread.setContours(null);
	}
}
