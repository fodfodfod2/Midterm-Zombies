
public class Vector2 {
	private double x;
	private double y;
	
	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2() {
		this.x = 0;
		this.y = 0;
	}
	
	public void addX(double x) {
		this.x+=x;
	}
	public void addY(double y) {
		this.y+=y;
	}
	public void setX(double x) {
		this.x=x;
	}
	public void setY(double y) {
		this.y=y;
	}
	
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	
	public double getAngleRadians() {
		double ans = Math.atan2(y,x);

		if (ans < 0) {
			ans+=2*Math.PI;
		}
		return ans;
	}
	public double getAngleDegrees() {
		return getAngleRadians()*180/Math.PI;
	}

	public int[] getClosestOffset() {
	    
	    if (Math.abs(x) < 1e-9 && Math.abs(y) < 1e-9) {
	    	
	    	return new int[]{0, 0};
	    }

	    double angle = getAngleRadians();
	    double sector = angle / (Math.PI / 4.0);
	    int s = (int)Math.round(sector) % 8;    
	    switch (s) {
	        case 0: return new int[]{ 1,  0}; 
	        case 1: return new int[]{ 1,  1}; 
	        case 2: return new int[]{ 0,  1}; 
	        case 3: return new int[]{-1,  1}; 
	        case 4: return new int[]{-1,  0}; 
	        case 5: return new int[]{-1, -1}; 
	        case 6: return new int[]{ 0, -1}; 
	        default:return new int[]{ 1, -1}; 
	    }
	}

}
