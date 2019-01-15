package Team4450.Robot11.Pathing;

public class MathUtil 
{
	public static double TWO_PI = Math.PI * 2;
	
	public static double normalizeAngleDegrees(double a) 
	{
		 return normalizeAngleDegrees(a, 0.0);
	}

	public static double normalizeAngleDegrees(double a, double center) 
	{
        return a - 360d * Math.floor((a + 180d - center) / 360d);
	}

	public static double normalizeAngle(double a) 
	{
        return normalizeAngle(a, 0.0);
	}
	
	public static double normalizeAngle(double a, double center) 
	{
        return a - TWO_PI * Math.floor((a + Math.PI - center) / TWO_PI);
	}
}


