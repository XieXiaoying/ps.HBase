package kmeans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.apache.hadoop.hbase.util.Bytes;

public class DataSource {
	private ArrayList<Point> objects;
	private int k;
	
	public DataSource(ArrayList<Point> objects){
		this.objects = objects;
	}
	
	
	public ArrayList<Point> getObjects(){
		return this.objects;
	}
	
	/**
	 * @author ytt
	 * 将聚合后的结果保留到k个集合中
	 * @return ArrayList<ArrayList<Point>>
	 * */
	public ArrayList<ArrayList<Point>> getResult(ArrayList<Point> objects, int k){
		ArrayList<ArrayList<Point>> reslut = new ArrayList<ArrayList<Point>>();
		for(int i = 0;i < k; i++){
			reslut.add(new ArrayList<Point>());
		}
		
		Iterator<Point> iterator = objects.iterator();
		while(iterator.hasNext()){
			Point obj = iterator.next();
			int cid = obj.getCid();
			reslut.get(cid).add(obj);
		}
		
		
		/*for(int i = 0; i < k; i++){
			System.out.println("result is di " + i + "ge cu shi ：" + reslut.get(i));
		}*/
		
		return reslut;
	}
	
	public static void main(String[] args){
		
		KmeansTable kms = new KmeansTable();
		Random random = new Random();
		ArrayList<Point> points = new ArrayList<Point>();
		for(int i = 0; i < 10; i ++){
			double pixelx = 20.0;
			double pixely = 30.89;
			int cid = new Integer(random.nextInt(34)%5);
			double lon = 39.87;
			double lat = 45.89;
			String poiString  = String.valueOf(new Double(random.nextInt(56)));
			Point point = new Point(pixelx, pixely,lon, lat,poiString);
			
			kms.setPonit(3, 45, 45.89, point.toString(), null);
			//points.add(point);
		}
		//DataSource data = new DataSource(points);
		//System.out.println("zhe shi dataSource : " + data.getObjects().toString());
		//data.getResult(data.getObjects(), 8);
		
		//kmeansTable.getResource(3);
		//kms.setCluster(1, titleX, titleY, cluster, center, timestamp)
		
	}
	
	
	
}
