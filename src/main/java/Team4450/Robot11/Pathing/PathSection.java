package Team4450.Robot11.Pathing;

public class PathSection 
{
	public final double length;
	public final double angle;
	public final double radius;
	public final double maxVelocity;
	public double maxRotationalVelocity;
	public final Pose2D startPose;
	public final Pose2D endPose;	
	
	public PathSection(double length, double maxVelocity, Pose2D startPose) 
	{
		this.length = length;
		this.angle = 0.0;
		this.radius = 0.0;
		this.maxVelocity = maxVelocity;
		this.startPose = startPose;
		this.endPose =  new Pose2D(startPose);		
		
		double angleRadians = Math.toRadians(startPose.heading);
		endPose.x += length * Math.cos(angleRadians);
		endPose.y += length * Math.sin(angleRadians);
	}
	
	public PathSection(double angle, double radius, double maxVelocity, Pose2D startPose) 
	{
		double angleRadians = MathUtil.normalizeAngle(Math.toRadians(angle));
		this.length = Math.abs(angleRadians) * radius;
		this.angle = angle;
		this.radius = radius;
		this.maxVelocity = maxVelocity;
		this.startPose = startPose;
		this.endPose =  new Pose2D(startPose);		

		double chord = 2.0 * radius * Math.sin(Math.abs(angleRadians) / 2.0);
		double halfAngle = Math.toRadians(startPose.heading) + angleRadians / 2.0;
		
		endPose.heading += angle;
		endPose.x += chord * Math.cos(halfAngle);
		endPose.y += chord * Math.sin(halfAngle);
	}		
	
	@Override
	public String toString() 
	{
		return String.format("Section: %6.1f %6.1f %6.1f - start%s - end%s", length, angle, maxVelocity, startPose, endPose);
	}

}
