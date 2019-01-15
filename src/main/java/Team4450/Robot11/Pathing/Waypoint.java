package Team4450.Robot11.Pathing;

import java.util.ArrayList;
import java.util.List;

public class Waypoint
{
	
	private final List<CompletionListener> listeners = new ArrayList<>();

	public double x;
	public double y;
	public double speed;

	public Waypoint() 
	{	
	}
	
	public Waypoint(Waypoint src) 
	{
		x = src.x;
		y = src.y;
		speed = src.speed;
	}
	
	public Waypoint(Pose2D src, double speed) 
	{
		x = src.x;
		y = src.y;
		this.speed = speed;
	}
	
	public Waypoint(double x, double y, double speed) 
	{
		this(x, y, speed, null);
	}
	
	public Waypoint(double x, double y, double speed, CompletionListener listener) 
	{
		this.x = x;
		this.y = y;
		this.speed = speed;
		
		if (listener != null) this.addCompletionListener(listener);
	}
	
	public void addCompletionListener(CompletionListener listener)
	{
		listeners.add(listener);
	}

	protected void fireCaptureEvent() 
	{
		for (CompletionListener listener : listeners) listener.onCompletion();
	}

	protected Waypoint mult(double a) 
	{
		return new Waypoint(x*a, y*a, speed);
	}

	protected Waypoint add(Waypoint other) 
	{
		return new Waypoint(x+other.x,y+other.y,speed);
	}

	protected Waypoint div(double a) 
	{
		return new Waypoint(x/a, y/a, speed);
	}
	
	public double distance(Waypoint pt) 
	{
		return distance(pt.x, pt.y);
	}
	
	public double distance(double x, double y) 
	{
		double dx = x - this.x;
		double dy = y - this.y;
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	public double angle(Waypoint pt) 
	{
		return angle(pt.x, pt.y);
	}
	
	public double angle(double x, double y) 
	{
		double dx = this.x - x;
		double dy = this.y - y;
		return Math.atan2(dy, dx);
	}

	@Override
	public String toString()
	{
		return String.format("%3.3f %3.3f %3.3f", x, y, speed);
	}
}
