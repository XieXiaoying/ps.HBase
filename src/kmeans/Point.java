package kmeans;

import collection.Constants;


//test for svn
public class Point {
	
		private double pixelX;
		private double pixelY;
		private double lon;
		private double lat;
		private String poiString;
		private int cid;    //所归属的k值
		
		
		public Point(){
			
		}
		public Point(double pixelX, double pixelY){
			this.pixelX = pixelX;
			this.pixelY = pixelY;
		}
		
		public Point(double pixelX, double pixelY, double lon, double lat,String poiString){
			this.pixelX = pixelX;
			this.pixelY = pixelY;
			this.lat = lat;
			this.lon = lon;
			this.poiString = poiString;
		}
		public Point(double pixelX, double pixelY,double lon, double lat,String poiString, int k){
			this.pixelX = pixelX;
			this.pixelY = pixelY;
			this.lat = lat;
			this.lon = lon;
			this.poiString = poiString;
			this.cid = k;
		}
		public void setPixelX(double pixelX){
			this.pixelX = pixelX;
		}
		
		public void setPixelY(double pixelY){
			this.pixelY = pixelY;
		}
		public double getPixelX(){ 
			return this.pixelX;
		}
		
		public double getPixelY(){
			return this.pixelY;
		}
		
		
		public double getLon(){
			return this.lon;
		}
		
		public double getlat(){
			return this.lat;
		}
		
		public String getPoiString(){
			return this.poiString;
		}
		public double[] getTitleXY(){
			double[] titleXY = new double[2];
			titleXY[0] = Math.floor(pixelX/256);
			titleXY[1] = Math.floor(pixelY/256);
			return titleXY;
		}
		
		public double[] getVector(){
			double[] vector = new double[4];
			vector[0] = this.getPixelX();
			vector[1] = this.getPixelY();
			vector[2] = this.getLon();
			vector[3] = this.getlat();
			
			return vector;
		}
		
		
		public void setCid(int index){
			this.cid = index;
		}
		
		
		public int getCid(){
			return this.cid;
		}
		
		public double getDistance(double x, double y){
			double double_x = Math.abs(this.pixelX - x);
			double double_y = Math.abs(this.pixelY - y);
			return Math.sqrt(double_x * double_x + double_y * double_y);
		}
		
		public String toString(){
			String result = this.pixelX + Constants.SEPARATER + this.pixelY + 
					Constants.SEPARATER + this.lon + Constants.SEPARATER +
					this.lat + Constants.SEPARATER +this.poiString;
			return result;
			
		}
	
		public static void main(String[] args){
			Point point = new Point(110.2343545, 39.7899887);
			//point.testForJs();
		}

}
