package kmeans;

import collection.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import Common.AbstractTable;
import Common.Column;
import Incentives.newTasks;

public class KmeansTable extends AbstractTable{
	private static final String tableName = "kmeans";
	
	public static final byte[] INFO_CF = "info".getBytes();
	
	public static final byte[] POIS_COL = "pois".getBytes();
	
	
	public static final byte[] CLUSTER_CF = "cluster".getBytes();
	
	public static final byte[] CENTER_COL = "center".getBytes();
	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
	public static final byte[] NEAR_COL = "nearest".getBytes();
	
	public static final byte[] INCLUDE_COL = "include".getBytes();
	
	public KmeansTable(){
		try{
			HBaseAdmin hAdmin = new HBaseAdmin(Constants.conf);
			if(hAdmin.tableExists(tableName)){
				//do nothing
			}
			else{
				HColumnDescriptor des = new HColumnDescriptor(INFO_CF);
				des.setMaxVersions(Integer.MAX_VALUE);
				HColumnDescriptor des2 = new HColumnDescriptor(CLUSTER_CF);
				HTableDescriptor t = new HTableDescriptor(tableName);
				
				t.addFamily(des);
				t.addFamily(des2);
				hAdmin.createTable(t);
				
			}
			hAdmin.close();
			
			hTable = new HTable(Constants.conf, tableName);
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public static byte[] generateRowKey(int zoom, double titleX, double titleY){
		String RowKey = zoom + Constants.SEPARATER + titleX + 
				 Constants.SEPARATER + titleY;
		return Bytes.toBytes(RowKey);
	}
	
	/**
	 * @author ytt
	 * 将不同zoom下聚合后的结果存到数据库
	 * 
	 * */
	public Boolean setCluster(int zoom, double titleX, double titleY, String cluster, String center , String near, Date timestamp){
		byte[] rowKey = generateRowKey(zoom, titleX, titleY);
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		kvs.add(new KeyValue(rowKey, CLUSTER_CF, CENTER_COL, Bytes.toBytes(center)));
		kvs.add(new KeyValue(rowKey, CLUSTER_CF, INCLUDE_COL, Bytes.toBytes(cluster)));
		kvs.add(new KeyValue(rowKey, CLUSTER_CF, NEAR_COL, Bytes.toBytes(near)));
		return this.put(rowKey, timestamp, kvs);
	}
	
	
	/**
	 * @author ytt
	 * @time 2015/5/1
	 * 根据zoom得到所有的Center
	 * @param  int zoom
	 * @return list<JSONObject> 返回zoom缩放比例下k个簇的k个质心
	 * */
	public List<JSONObject> getCenters(int zoom){
		List<JSONObject> centers = new ArrayList<JSONObject>();
		double pixelX;
		double pixelY;
		
		Scan scan = new Scan();
		Filter filter = new PrefixFilter(Bytes.toBytes(zoom + Constants.SEPARATER));
		FilterList filterList = new FilterList();
		filterList.addFilter(filter);
		List<Column> colList = new ArrayList<Column>();
		colList.add(new Column(CLUSTER_CF, CENTER_COL));
		List<byte[]> famList = new ArrayList<byte[]>(); 
		famList.add(CLUSTER_CF);
	
		ResultScanner rScanner = this.scan(scan, famList, colList,filterList);
		
		if(rScanner == null){  //result bu wei kong
			System.out.println("rscanner is null");
		} 
		
		for(Result result : rScanner){
			for(KeyValue keyValue : result.list()){
				JSONObject center = new JSONObject();
				String[] centerstring = Bytes.toString(keyValue.getValue()).split(Constants.SEPARATER);
				pixelX = Double.valueOf(centerstring[0]);
				pixelY = Double.valueOf(centerstring[1]);
				
				center.accumulate("centerx", pixelX);
				center.accumulate("centery", pixelY);
				centers.add(center);
			}
			
		}
		return centers;
		
	}
	
	
	/**
	 * @author ytt
	 * @time 2015/5/1
	 * 根据zoom得到所有的near
	 * @param  int zoom
	 * @return list<JSONObject> 返回zoom缩放比例下k个簇的k个质心最近的点
	 * */
	public List<JSONObject> getNears(int zoom){
		List<JSONObject> Nears = new ArrayList<JSONObject>();
		double pixelX;
		double pixelY;
		double lon;
		double lat;
		String poiString;
		
		Scan scan = new Scan();
		Filter filter = new PrefixFilter(Bytes.toBytes(zoom + Constants.SEPARATER));
		FilterList filterList = new FilterList();
		filterList.addFilter(filter);
		List<Column> colList = new ArrayList<Column>();
		colList.add(new Column(CLUSTER_CF, NEAR_COL));
		List<byte[]> famList = new ArrayList<byte[]>(); 
		famList.add(CLUSTER_CF);
	
		ResultScanner rScanner = this.scan(scan, famList, colList,filterList);
		
		if(rScanner == null){  //result bu wei kong
			System.out.println("rscanner is null");
		} 
		
		for(Result result : rScanner){
			for(KeyValue keyValue : result.list()){
				JSONObject near = new JSONObject();
				String[] nearstring = Bytes.toString(keyValue.getValue()).split(Constants.SEPARATER);
				pixelX = Double.valueOf(nearstring[0]);
				pixelY = Double.valueOf(nearstring[1]);
				lon = Double.valueOf(nearstring[2]);
				lat = Double.valueOf(nearstring[3]);
				poiString = nearstring[4];
				
				near.accumulate("centerx", pixelX);
				near.accumulate("centery", pixelY);
				near.accumulate("lon", lon);
				near.accumulate("lat", lat);
				near.accumulate("poiId", poiString);
				Nears.add(near);
			}
			
		}
		return Nears;
		
	}
	
	/**
	 * @author ytt
	 * 2015/5/2
	 * 在某个区域之内的某个缩放级别下的center的值
	 * @param zoom int
	 * @param minX double
	 * @parem minY double
	 * @param maxX double
	 * @param maxY double
	 * @return list<JSONObject> 即是[centerx:###,centery:###],[][][][][]]
	 * 
	 * */
	public List<JSONObject> getCenters(int zoom, double minX, double minY, double maxX, double maxY){
		List<JSONObject> centers = new ArrayList<JSONObject>();
		double pixelX;
		double pixelY;
		Scan scan = new Scan();
		Filter filter = new PrefixFilter(Bytes.toBytes(zoom + Constants.SEPARATER));
		//scan.setFilter(filter);
		FilterList filterList = new FilterList();
		filterList.addFilter(filter);
		List<Column> colList = new ArrayList<Column>();
		colList.add(new Column(CLUSTER_CF, CENTER_COL));
		List<byte[]> famList = new ArrayList<byte[]>(); 
		famList.add(CLUSTER_CF);
		
		ResultScanner rScanner = this.scan(scan, famList, colList,filterList);
		
		if(rScanner == null){  //result bu wei kong
			System.out.println("rscanner is null");
		} 
		
		for(Result result : rScanner){
			for(KeyValue keyValue : result.list()){

				JSONObject center = new JSONObject();
				String[] centerstring = Bytes.toString(keyValue.getValue()).split(Constants.SEPARATER);
				pixelX = Double.valueOf(centerstring[0]);
				pixelY = Double.valueOf(centerstring[1]);
				
				if(pixelX >= minX && pixelX <= maxX && pixelY >= minY && pixelY <= maxY){
					center.accumulate("centerx", pixelX);
					center.accumulate("centery", pixelY);
					centers.add(center);
				}
				
				
			}
			
		}
		return centers;
		
	}
	
	
	/**
	 * @author ytt
	 * 0606
	 * 根据zoom，center得到对应的nearest点
	 * */
	
	public String getNearpoiInCenterZoom(int zoom, Point center){
		String nearPoiString = null;
		byte[] rowKey = null;
		Scan scan = new Scan();
		Filter preFilter = new PrefixFilter(Bytes.toBytes(zoom + Constants.SEPARATER));
		SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter(
				CLUSTER_CF, CENTER_COL, CompareOp.EQUAL, Bytes.toBytes(center.toString()));
		FilterList filterList = new FilterList();
		filterList.addFilter(preFilter);
		filterList.addFilter(singleColumnValueFilter);
		
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(CLUSTER_CF, CENTER_COL));
		
		ResultScanner resultScanner = this.scan(scan, null, columnList, filterList);
		
		for(Result result : resultScanner){
			for(KeyValue kValue : result.list()){
				rowKey = kValue.getRow();
			}
		}
		
		columnList.clear();
		columnList.add(new Column(CLUSTER_CF, NEAR_COL));
		Result result = this.get(rowKey, null, columnList, 1);
		for(KeyValue keyValue : result.list()){
			String[] nearStrings = Bytes.toString(keyValue.getValue()).split(Constants.SEPARATER);
			nearPoiString = nearStrings[4];
		}
		 return nearPoiString;
	} 
	
	
	
	
	
	
	/**
	 * @author ytt
	 * @time 5/1
	 * 根据zoom和center得到该缩放比例下的center簇对应的poi点
	 * @param zoom {@link Integer}缩放比例
	 * @param center {@link Point}
	 * @return list<String> 返回的是String类型的poi的列表
	 * */
	public List<String> getPoisInCenterZoom(int zoom, Point center){
		List<String> poisList = new ArrayList<String>();
		byte[] rowKey = null;
		Scan scan = new Scan();
		Filter preFilter = new PrefixFilter(Bytes.toBytes(zoom + Constants.SEPARATER));
		SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter(
				CLUSTER_CF, CENTER_COL, CompareOp.EQUAL, Bytes.toBytes(center.toString()));
		FilterList filterList = new FilterList();
		filterList.addFilter(preFilter);
		filterList.addFilter(singleColumnValueFilter);
		
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(CLUSTER_CF, CENTER_COL));
		
		ResultScanner resultScanner = this.scan(scan, null, columnList, filterList);
		
		for(Result result : resultScanner){
			for(KeyValue kValue : result.list()){
				rowKey = kValue.getRow();
			}
		}
		
		columnList.clear();
		columnList.add(new Column(CLUSTER_CF, INCLUDE_COL));
		Result pointsResult = this.get(rowKey, null, columnList, 1);
		for(KeyValue kv : pointsResult.list()){
			String valueString = Bytes.toString(kv.getValue());
			String[] pointStrings = valueString.substring(1, valueString.length()- 1).
					split(",");
			for(int i = 0; i < pointStrings.length; i++){
				String[] pointString = pointStrings[i].split(Constants.SEPARATER);
				poisList.add(pointString[4]);
			} 
		}
		
		return poisList;
		
	}
	
	
	/**
	 * @author ytt
	 * 得到zoom下的center下的所有的pois的所有的信息
	 * 
	 * */
	public List<JSONObject> getPoisnfoForZoomCenter(int zoom, Point center) {
		List<JSONObject> poisList = new ArrayList<JSONObject>();
		double pixelX;
		double pixelY;
		double lon;
		double lat;
		String poiString;
		
		byte[] rowKey = null;
		Scan scan = new Scan();
		Filter preFilter = new PrefixFilter(Bytes.toBytes(zoom + Constants.SEPARATER));
		SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter(
				CLUSTER_CF, CENTER_COL, CompareOp.EQUAL, Bytes.toBytes(center.toString()));
		FilterList filterList = new FilterList();
		filterList.addFilter(preFilter);
		filterList.addFilter(singleColumnValueFilter);
		
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(CLUSTER_CF, CENTER_COL));
		
		ResultScanner resultScanner = this.scan(scan, null, columnList, filterList);
		
		for(Result result : resultScanner){
			for(KeyValue kValue : result.list()){
				rowKey = kValue.getRow();
			}
		}
		
		columnList.clear();
		columnList.add(new Column(CLUSTER_CF, INCLUDE_COL));
		Result pointsResult = this.get(rowKey, null, columnList, 1);
		for(KeyValue kv : pointsResult.list()){
			String valueString = Bytes.toString(kv.getValue());
			String[] pointStrings = valueString.substring(1, valueString.length()- 1).
					split(",");
			for(int i = 0; i < pointStrings.length; i++){
				JSONObject poiinfo = new JSONObject();
				String[] pointString = pointStrings[i].split(Constants.SEPARATER);
				pixelX = Double.valueOf(pointString[0]);
				pixelY = Double.valueOf(pointString[1]);
				lon = Double.valueOf(pointString[2]);
				lat = Double.valueOf(pointString[3]);
				poiString = pointString[4];
				
				poiinfo.accumulate("pixelX", pixelX);
				poiinfo.accumulate("pixelY", pixelY);
				poiinfo.accumulate("lon", lon);
				poiinfo.accumulate("lat", lat);
				poiinfo.accumulate("poiId", poiString);
				poisList.add(poiinfo);
			} 
		}
		
		return poisList;
	}
	
	
	
	
	//新增数据,
	public boolean setPonit(int zoom, double titleX, double titleY, String pointxy,Date timestamp){
		byte[] rowKey = generateRowKey(zoom, titleX, titleY);
		List<String> keyList = new ArrayList<String>();
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column(INFO_CF, POIS_COL));
		Get get = new Get(rowKey);
		
		try {
			if(this.rowExists(get)){
				Result rs = this.get(rowKey, null, null, columns, true);
				String keyvalue = Bytes.toString(rs.getValue(INFO_CF, POIS_COL));		
				String keyString = keyvalue.substring(1, keyvalue.length()-1);
				keyList.add(keyString);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		keyList.add(pointxy);
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		
		keyValues.add(new KeyValue(rowKey, INFO_CF, POIS_COL, Bytes.toBytes(keyList.toString())));
		return this.put(rowKey, timestamp, keyValues);
	}
	
	
	
	
	
	//得到某一个zoom下所有的点集合
	public ResultScanner getResource(int zoom){
		 
		Scan scan = new Scan();
		Filter filter = new PrefixFilter(Bytes.toBytes(zoom + Constants.SEPARATER));
		//scan.setFilter(filter);
		FilterList filterList = new FilterList();
		filterList.addFilter(filter);
		List<Column> colList = new ArrayList<Column>();
		colList.add(new Column(INFO_CF, POIS_COL));
		List<byte[]> famList = new ArrayList<byte[]>(); 
		famList.add(INFO_CF);

		
		ResultScanner rScanner = this.scan(scan, famList, colList,filterList);
		
		if(rScanner == null){  //result bu wei kong
			System.out.println("rscanner is null");
		} 
		return rScanner;
		
		
	}
	
	
	
	public Result getResourceone(int zoom, double titleX,double titleY){
		
		Result result = null;
		List<Column> colList = new ArrayList<Column>();
		colList.add(new Column(INFO_CF, POIS_COL));
		result =  this.get(generateRowKey(zoom, titleX, titleY), null, colList, 2);
		return result;
	}
	
	
	public static void main(String[] args){
		
		KmeansTable kms = new KmeansTable();
		/*
		 * 测试sePoint i是success
		 * 
		 * //kms.setPonit(2, 0.0, 0.0, "testtttt", null);
		kms.setPonit(2, 0.0, 0.0, "testMMMMM", null);
		Date timeDate = new Date(2013, 10, 1);
		kms.setPonit(2, 0.0, 0.0, "testtttt1", timeDate);
		kms.setPonit(2, 0.0, 0.0, "testttt2", timeDate);
		*/
		/*Result rone = kms.getResourceone(3, 1.0,0.0);
		for(KeyValue kValue : rone.list()){
			System.out.println("jofjs");
			String keyStrings = Bytes.toString(kValue.getValue());
			System.out.println(keyStrings);
		}*/
		
		
		
		/*@ ytt
		 * 测试getsource函数
		ResultScanner rScanner = kms.getResource(14);
		
		for(Result result : rScanner){/////######？？？为什么这句不管用,因为scanner关闭了！！
			System.out.println(result.getRow().toString());
			for(KeyValue kValue : result.list()){
				String keyStrings = Bytes.toString(kValue.getValue());
				System.out.println(keyStrings);
			}
			String keyStrings = Bytes.toString(result.getRow());
			
		}
		*/
		
		
		
		/*
		 * @time 5/1
		 * ytt
		 * 测试 getCenters(int zoom) 和 getPoisInCenterZoom
		 * 成功
		 * */
		/*List<JSONObject> centers = kms.getCenters(9);
		System.out.println("*******************yi xia shi fan hui de centers***********");
		for(int i = 0; i < centers.size(); i++){
			System.out.println(centers.get(i).getDouble("centerx") + "  " + centers.get(i).getDouble("centery"));
		}*/
		/*s
		Point center = new Point(809697,302180.5);
		List<String> poiStrings = kms.getPoisInCenterZoom(14, center);
		System.out.println("********************yi xia shi pois de xin xi*************");
		for(String poi : poiStrings){
			System.out.println(poi);
		}*/
			
		
		
		
		//kms.deleteTable("kmeans");
		
		
	}
}
