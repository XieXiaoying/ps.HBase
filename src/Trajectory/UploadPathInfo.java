package Trajectory;

public class UploadPathInfo {
	private long uploadtime;
	
	private double lon;
	private double lat;
	private String gridId;
	
	public UploadPathInfo(long uploadtime, double lon, double lat, String gridId){
	
		this.uploadtime = uploadtime;
		this.lon = lon;
		this.lat = lat;
		this.gridId = gridId;
	}
	
	
	public void setUploadtime(long uploadtime){
		this.uploadtime = uploadtime;
	}
	public void setLon(double lon){
		this.lon = lon;
	}
	public void setLat(double lat){
		this.lat = lat;
	}
	public void setGridId(String gridId){
		this.gridId = gridId;
	}
	
	public long getUploadtime(){
		return uploadtime;
	}
	public double getLon(){
		return lon;
	}
	public double getLat(){
		return lat;
	}
	public String getGridId(){
		return gridId;
	}
	}
	 

