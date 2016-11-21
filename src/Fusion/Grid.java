package Fusion;

/**
 * 区域
 * @author leeying
 *
 */
public class Grid{
	// 横坐标
	private int x;
	// 纵坐标
	private int y;
	
	// 经度
	private double lon;
	// 纬度
	private double lat;
	
	public Grid(double lon, double lat){
		this.x = -1;
		this.y = -1;
		this.lon = lon;
		this.lat = lat;
	}
	
	public Grid(int x, int y) {
		this.x = x;
		this.y = y;
		this.lon = -1;
		this.lat = -1;
	}
	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "Grid [x=" + x + ", y=" + y + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Grid other = (Grid) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
}
	
	