package cData;

public class Sitepixelxy {
	private int siteId;
	private double pixelx;
	private double pixely;
	private int zoom;
	public Sitepixelxy(int siteId, double pixelx, double pixely, int zoom) {
		// TODO Auto-generated constructor stub
		this.siteId = siteId;
		this.pixelx = pixelx;
		this.pixely = pixely;
		this.zoom = zoom;
	}
	public double getPixelx() {
		return this.pixelx;
	}
	public double getPixely() {
		return this.pixely;
	}
}
