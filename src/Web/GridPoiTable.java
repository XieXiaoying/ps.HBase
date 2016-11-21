package Web;


import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import Common.AbstractTable;
import collection.Constants;

/**
 * @author ytt
 * 2015-4-23
 * 
 * 
 * */
public class GridPoiTable extends AbstractTable{
	Logger logger = Logger.getLogger(GridPoiTable.class);
	
	private static long gridid;
	private static final byte[] INFO_CF = "info".getBytes();
	private static final byte[] GRIDPOI_COL = "gridPoiId".getBytes();
	private static final String[] family = {"info"};
	
	private static final String tableName = "gridPoi";
	
	
	
	
	public GridPoiTable(){
		try{
			createTable(Constants.conf, tableName, family);
			hTable = new HTable(Constants.conf, tableName);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	

	public byte[] generateRowKey(double latUpLeft, double lonUpLeft, double latBtmRight,
			double lonBtmRight){
		String rowKey = latUpLeft + Constants.SEPARATER + lonUpLeft + Constants.SEPARATER
				+ latBtmRight + Constants.SEPARATER + lonBtmRight;
		
		return rowKey.getBytes();
	}
	
	
	public byte[] generateRowKey(double latUpLeft,double lonUpLeft){
		double latBtmRight = Double.valueOf(Constants.DF.format(latUpLeft - 0.001));
		double lonBtmRight = Double.valueOf(Constants.DF.format(lonUpLeft + 0.001));
		return generateRowKey(latUpLeft, lonUpLeft,latBtmRight,lonBtmRight);
	}
	
	public byte[] generateStopRow(double latUpLeft, double lonUpLeft){
		String rowKey = latUpLeft + Constants.SEPARATER + lonUpLeft + Constants.SEPARATER
				+ ":" + Constants.SEPARATER + ":";
		
		return rowKey.getBytes();
	}
	
	public byte[] generateStartRow(double latUpLeft, double lonUpLeft){
		String rowKey = latUpLeft + Constants.SEPARATER + lonUpLeft + Constants.SEPARATER
				+ "#" + Constants.SEPARATER + "#";
		
		return rowKey.getBytes();
	}
	

	
	public static double[] computeGridxy(double lat,double lon){
		DecimalFormat df = new DecimalFormat("0.000000");
		double[] gridxy = new double[4];
		String latString = String.valueOf(df.format(lat));
		String lonString = String.valueOf(df.format(lon));
		
		int indexlat = latString.indexOf(".") + 4;
		int indexlon = lonString.indexOf(".") + 4;
		
		String latbefor = latString.substring(0, indexlat);
		String latlater = latString.substring(indexlat);
		String lonbefor = lonString.substring(0, indexlon);
		String lonlater = lonString.substring(indexlon);
		double latUpLeft;
		double latBtmRight;
		double lonUpLeft;
		double lonBtmRight;
		
		
		if(Integer.valueOf(latlater) >= 713){
			latUpLeft = Double.valueOf(latbefor + "713");
			latBtmRight = Double.valueOf(latbefor) + 0.001713;
			
		}
		
		else{
			latUpLeft = Double.valueOf(latbefor) - 0.001 + 0.000713;
			latBtmRight = Double.valueOf(latbefor) + 0.000713;
		}
		
		
		if(Integer.valueOf(lonlater) >= 413){
			lonUpLeft = Double.valueOf(lonbefor) + 0.001413;
			lonBtmRight = Double.valueOf(lonbefor) + 0.000413;
		}
		else{
			lonUpLeft = Double.valueOf(lonbefor) + 0.000413;
			lonBtmRight = Double.valueOf(lonbefor) - 0.001 + 0.000413;
		}
		
		gridxy[0] = Double.valueOf(df.format(latUpLeft));
		gridxy[1] = Double.valueOf(df.format(lonUpLeft));
		gridxy[2] = Double.valueOf(df.format(latBtmRight));
		gridxy[3] = Double.valueOf(df.format(lonBtmRight));
		return gridxy;
	}
	
	
	

	public boolean setGrid(double latUpLeft, double lonUpLeft, double latBtmRight, 
			double lonBtmRight, long gridid, String poiString, Date timestamp,String builderName){
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.accumulate("gridId", gridid);
		jsonObject.accumulate("poiId", poiString);
		jsonObject.accumulate("time", timestamp.getTime());
		jsonObject.accumulate("builder", builderName);
		
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		
		keyValues.add(new KeyValue(generateRowKey(latUpLeft, lonUpLeft, latBtmRight, lonBtmRight),
				INFO_CF, GRIDPOI_COL, Bytes.toBytes(jsonObject.toString())));
		if(!put(generateRowKey(latUpLeft, lonUpLeft, latBtmRight, lonBtmRight), null, keyValues)){
			
			System.out.println("put is false 111");
			return false;
		}
		return true;
		
		
	}
	

	public boolean setGrid(double lat, double lon, String poiid, Date timestamp,
			String builderName){
		
		double[] gridxy = computeGridxy(lat, lon);
		Get get = new Get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]));
		get.addColumn(INFO_CF, GRIDPOI_COL);
		try{
			if(!this.rowExists(get)){
				Date time = timestamp;
				gridid += 1;
				if(poiid == null){
					String poiString = ComputePOI.computePOI(lon, lat);
					//System.out.println(poiString);
				}
				
				if(!setGrid(gridxy[0],gridxy[1],gridxy[2],gridxy[3],gridid,poiid,time,builderName)){
					
					return false;
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}
	

	public String regionIsExist(double lat, double lon){
		double[] gridxy = computeGridxy(lat, lon);
		System.out.println(gridxy[0] + "  " + gridxy[1] + "  " + gridxy[2] + "  " + gridxy[3] + "  ");
		String poiString = null;
		
		Get get = new Get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]));
		get.addColumn(INFO_CF, GRIDPOI_COL);
		try{
			if(!this.rowExists(get)){
				poiString = ComputePOI.computePOI(lon, lat);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(poiString);
		return poiString;
	}
	

	public String regionIsExistPOI(double lat, double lon) {
		
		double[] gridxy = computeGridxy(lat, lon);
		System.out.println(gridxy[0] + "  " + gridxy[1] + "  " + gridxy[2] + "  " + gridxy[3] + "  ");
		
		String poiString = null;
	
		Get get = new Get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]));
		get.addColumn(INFO_CF, GRIDPOI_COL);
		try{
			if(this.rowExists(get)){
				Result rs = hTable.get(get);
				for(KeyValue kv : rs.list()){
					JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
					
					poiString =  jsonObject.getString("poiId");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(poiString);
		return poiString;
		
	}
	
	
	

	public String findGridId(double lat, double lon) throws IOException{
		double[] gridxy = computeGridxy(lat, lon);
		System.out.println(gridxy[0] + "   " + gridxy[1]+ "   " + gridxy[2]+ "   " + gridxy[3]);
		//System.out.println(latUpLeft + "  " + lonUpLeft + "  " + latBtmRight + "  " + lonBtmRight);
		Result rs = get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]), 
				null, null, null, false);
		
		
		String gridId = null;
		for(KeyValue kv : rs.list()){
			gridId = Bytes.toString(kv.getValue());
		}
		
		System.out.println(gridId);
		return gridId;
		
		
		
		
	}
	public List<String> getPois() {
		List<String> nearPoi = new ArrayList<String>();
				try {
					Scan scan = new Scan();
					ResultScanner rs = hTable.getScanner(scan);
					
					JSONObject jsonObject = new JSONObject();
					int i = 0;
					for (Result r = rs.next(); r != null; r = rs.next()) {
						// process result...
						jsonObject = JSONObject.fromObject(Bytes.toString(r.getValue(INFO_CF, GRIDPOI_COL)));
						//System.out.println("test" + jsonObject.getString("poiId"));
						String res = jsonObject.getString("poiId");
						//System.out.println(res);
						nearPoi.add(res);
						i++;
					}						 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				return nearPoi;
			}

	public List<JSONObject> findNearPoiIInfo(double lat, double lon, double distans){
		DecimalFormat df = new DecimalFormat("0.000000");
		List<String> nearPoi = new ArrayList<String>();
		double[] uplonlat = computeGridxy(lat + distans, lon - distans);
		double latupup = uplonlat[0];
		double lonupup = uplonlat[1];
		
		
		
		double[] btmlonlat = computeGridxy(lat - distans, lon + distans);
		double latbtmup = btmlonlat[0];
		double lonbtmup = btmlonlat[1];
		
		
		
		//System.out.println(uplonlat[0] + "   " + uplonlat[1] + "   " + uplonlat[2] + "   " + uplonlat[3]);
		//System.out.println(btmlonlat[0] + "   " + btmlonlat[1] + "   " + btmlonlat[2] + "   " + btmlonlat[3]);
		
		Scan scan = new Scan();
		
		JSONObject jsonObject = null;
		for(double startlat = latupup; startlat >= latbtmup;){
			scan.setStartRow(generateRowKey(startlat, lonupup));
			scan.setStopRow(generateRowKey(startlat, lonbtmup));
			scan.addColumn(INFO_CF, GRIDPOI_COL);
			ResultScanner scanner = this.scan(scan, null, null, null);
			
			for(Result result : scanner){
				for(KeyValue kv : result.list()){
					System.out.println("mark~!!");
					jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
					nearPoi.add(jsonObject.getString("poiId"));
					System.out.println(jsonObject.getString("poiId"));
				}
			}
			try {
				scanner.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			startlat = Double.valueOf(Constants.DF.format(startlat - 0.001));
			
		}
		POITable poiTable = new POITable();
		return poiTable.getAllPOIOnePic(nearPoi);
		
		
	}
	
	
	
/*	public boolean importSomedata(){
		
		Date timeDate = new Date();
		for(int i = 0; i < 100; i++){
			double random = Math.random();
			double lat = Math.round(random*10 + 30);
			double lon = Math.round(random*10 + 60);
			
			long randomlong = (long) (random * (timeDate.getTime()));
			Date timestamp = new Date(randomlong); 
			System.out.println("import "+ i + "   ci shu ju is lat is " + lat + "  lon is  " + lon);
			if(!setGrid(lat, lon, timestamp, "import")){
				System.out.println("di " + i + " out le ");
				return false;
			}
			
		}
		return true;
	}*/
	
	

	public boolean deletePOIGrid(double blon, double blat){
		//double[] lonlat = ComputePOI.computeLonlat(poi);
		double[] gridxy = computeGridxy(blat,blon);
		System.err.println(gridxy[0] + gridxy[1] + gridxy[2] + gridxy[3]);
		this.deleteColumn(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]),
				INFO_CF, GRIDPOI_COL);
		return true;
	}
	
	
	public List<JSONObject> getData() {
		/*		for(int i = 0; i < 100; i++) {
					Get get = new Get(Bytes.toBytes("row-"+i));
					get.addColumn(INFO_CF,TEST_COL);
					try {
						Result rs = hTable.get(get);
//						for(KeyValue kv : rs.list()){
//							JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
//							
//							String poiString =  jsonObject.getString("poiId");
//						}
						byte[] val = rs.getValue(INFO_CF, TEST_COL);
						System.out.println("test" + Bytes.toString(val));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		List<String> nearPoi = new ArrayList<String>();
				try {
					Scan scan = new Scan();
					//scan.setTimeStamp(1452691836830L);
					ResultScanner rs = hTable.getScanner(scan);
					JSONObject jsonObject = new JSONObject();
						for (Result r = rs.next(); r != null; r = rs.next()) {
						// process result...
							jsonObject = JSONObject.fromObject(Bytes.toString(r.getValue(INFO_CF, GRIDPOI_COL)));
							//byte[] val = r.getValue(INFO_CF, TEST_COL);
							System.out.println("test" + jsonObject.getString("poiId"));
							nearPoi.add(jsonObject.getString("poiId"));
							//jsonObject.accumulate("gridId", jsonObject.getString("gridId"));
							//jsonObject.accumulate("poiId", jsonObject.getString("poiId"));
							//jsonObject.accumulate("time", jsonObject.getString("time"));
							//jsonObject.accumulate("builder", jsonObject.getString("builder"));
							System.out.println(jsonObject);
							jsonObjects.add(jsonObject);
						}						 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				POITable poiTable = new POITable();
				return poiTable.getAllPOIOnePic(nearPoi);
			}
	
	//作为在某个大格子里面遍历小格子的方法
	//输入的参数为大格子的（左上角纬度，左上角经度，格子边长），此时格子边长为0.005度，
	//但distance为0.004，因为是取最后一个点的左上角，即，所有的格子都在大格子内
	//此函数放在GridPoiTable.java中
	public List<JSONObject> findPoiInLargeGrid(double lat, double lon, double distance){
		DecimalFormat df = new DecimalFormat("0.000000");
		List<String> nearPoi = new ArrayList<String>();
//		double[] uplonlat = computeGridxy(lat + distans, lon - distans);
		//左上角
		double latupup = lat;
		double lonupup = lon;
//		double[] btmlonlat = computeGridxy(lat - distans, lon + distans);
		//右下角
		BigDecimal latupup_b = new BigDecimal(latupup+"");
		BigDecimal lonupup_b = new BigDecimal(lonupup+"");
		BigDecimal a = new BigDecimal(0.001+"");
		BigDecimal b = new BigDecimal(0.02+"");
		
		double latbtmup = latupup_b.subtract(b).doubleValue();
		double lonbtmup = lonupup_b.add(b).doubleValue();
		
		//因为GridPoiTable里面的行键是颠倒的，故起始位置相应调整
		latupup = latupup_b.subtract(a).doubleValue();
		lonupup = lonupup_b.add(a).doubleValue();
		
//		latupup = latupup-0.001;
//		lonupup = lonupup+0.001;
		
		System.out.println("输入范围：lat："+lat+"  lon："+lon);
		System.out.println("搜寻小格子范围："+latupup+"  "+lonupup+"\n"+latbtmup+"  "+lonbtmup);
		//System.out.println(uplonlat[0] + "   " + uplonlat[1] + "   " + uplonlat[2] + "   " + uplonlat[3]);
		//System.out.println(btmlonlat[0] + "   " + btmlonlat[1] + "   " + btmlonlat[2] + "   " + btmlonlat[3]);
		
		Scan scan = new Scan();
		
		JSONObject jsonObject = null;
		for(double startlat = latupup; startlat >= latbtmup;){
			scan.setStartRow(generateStartRow(startlat, lonupup));//因为hbase是根据askii码来排的rowkey，ASCII排序中："#" < "0-9" < ":"
			scan.setStopRow(generateStopRow(startlat, lonbtmup));
			System.out.println("搜寻的纬度："+startlat);
			scan.addColumn(INFO_CF, GRIDPOI_COL);
			ResultScanner scanner = this.scan(scan, null, null, null);
			
			for(Result result : scanner){
				for(KeyValue kv : result.list()){
					System.out.println("find Poi in large Grid");
					jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
					nearPoi.add(jsonObject.getString("poiId"));
					System.out.println("大站下的子站有："+jsonObject.getString("poiId"));
				}
			}
			try {
				scanner.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			startlat = Double.valueOf(Constants.DF.format(startlat - 0.001));
			
		}
		POITable poiTable = new POITable();
		
		return poiTable.getAllPOIOnePic(nearPoi);
	}
	
	//主站照片墙接口，从主站所有站点里面返回若干张照片
	public List<JSONObject> findMainPoiPics(double lat, double lon, double distance,int count){
		DecimalFormat df = new DecimalFormat("0.000000");
		List<String> nearPoi = new ArrayList<String>();
//		double[] uplonlat = computeGridxy(lat + distans, lon - distans);
		//左上角
		double latupup = lat;
		double lonupup = lon;
//		double[] btmlonlat = computeGridxy(lat - distans, lon + distans);
		//右下角
		BigDecimal latupup_b = new BigDecimal(latupup+"");
		BigDecimal lonupup_b = new BigDecimal(lonupup+"");
		BigDecimal a = new BigDecimal(0.001+"");
		BigDecimal b = new BigDecimal(0.02+"");
		
		double latbtmup = latupup_b.subtract(b).doubleValue();
		double lonbtmup = lonupup_b.add(b).doubleValue();
		
		//因为GridPoiTable里面的行键是颠倒的，故起始位置相应调整
		latupup = latupup_b.subtract(a).doubleValue();
		lonupup = lonupup_b.add(a).doubleValue();
		
//		latupup = latupup-0.001;
//		lonupup = lonupup+0.001;
		
		System.out.println("输入范围：lat："+lat+"  lon："+lon);
		System.out.println("搜寻小格子范围："+latupup+"  "+lonupup+"\n"+latbtmup+"  "+lonbtmup);
		//System.out.println(uplonlat[0] + "   " + uplonlat[1] + "   " + uplonlat[2] + "   " + uplonlat[3]);
		//System.out.println(btmlonlat[0] + "   " + btmlonlat[1] + "   " + btmlonlat[2] + "   " + btmlonlat[3]);
		
		Scan scan = new Scan();
		
		JSONObject jsonObject = null;
		for(double startlat = latupup; startlat >= latbtmup;){
			scan.setStartRow(generateStartRow(startlat, lonupup));//因为hbase是根据askii码来排的rowkey，ASCII排序中："#" < "0-9" < ":"
			scan.setStopRow(generateStopRow(startlat, lonbtmup));
			System.out.println("搜寻的纬度："+startlat);
			scan.addColumn(INFO_CF, GRIDPOI_COL);
			ResultScanner scanner = this.scan(scan, null, null, null);
			
			for(Result result : scanner){
				for(KeyValue kv : result.list()){
					System.out.println("find Poi in large Grid");
					jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
					nearPoi.add(jsonObject.getString("poiId"));
					System.out.println("大站下的子站有："+jsonObject.getString("poiId"));
				}
			}
			try {
				scanner.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			startlat = Double.valueOf(Constants.DF.format(startlat - 0.001));
			
		}
		POITable poiTable = new POITable();
		
		return poiTable.getAllPoiOnePicForMainStation(nearPoi,count);
	}
	
	public String getPoiOfLevel(String TableName) {
		JSONObject message = new JSONObject();
				try {
					createTable(Constants.conf, TableName, family);
					hTable = new HTable(Constants.conf, TableName);
					Scan scan = new Scan();
					ResultScanner rs = hTable.getScanner(scan);
					for (Result r : rs) {
						String content = "";
						JSONObject JsonPoiId = JSONObject.fromObject(Bytes.toString(r.getValue(INFO_CF, "gridPoi".getBytes())));
						String poiId = JsonPoiId.getString("poiId");
						content = "{\"gridDetail\":" + Bytes.toString(r.getValue(INFO_CF, "gridDetail".getBytes())) + ",\"gridPoi\":" + Bytes.toString(r.getValue(INFO_CF, "gridPoi".getBytes()))+"}";
						message.accumulate(poiId, content);
					}
					rs.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				System.out.println(message.toString());
				return message.toString();
			}
	public static void main(String[] args) throws IOException{
		GridPoiTable gridTable = new GridPoiTable();
		
		Date timeDate  = new Date();
		//gridTable.setGrid(47.456345, 268.567565,timeDate, "testfor1");
		//double[] gridxy = GridPoiTable.computeGridxy(48.456345, 136.567565);
		//System.out.println(gridxy[0] + "  " + gridxy[1] + "  " + gridxy[2] + "  " + gridxy[3] + "  ");
		
		//gridTable.regionIsExist(116.567565,39.456345);
		String poiString = ComputePOI.computePOI(116.334564, 30.215546);
		System.out.println(poiString);
//		 
//		// 先算的此处的小格子
//		double[] gridxy_smallGrid = GridPoiTable_One.computeGridxy(39.969781238497, 116.36505524901);
//		System.out.println("largeGrid[0][1]:"+gridxy_smallGrid[0]+"/"+gridxy_smallGrid[1]+"\nlargeGrid[2][3]:"+gridxy_smallGrid[2]+"/"+gridxy_smallGrid[3]);
//		
//		
//		// 再根据小格子算得大格子
//		double[] largeGrid = GridPoiTable_One.computeLargeGridxy(
//				gridxy_smallGrid[2], gridxy_smallGrid[3]);
//		System.out.println("largeGrid[0][1]:"+largeGrid[0]+"/"+largeGrid[1]+"\nlargeGrid[2][3]:"+largeGrid[2]+"/"+largeGrid[3]);
//		// 获得这个大格子区域内所有子站点，参数为大格子的左上角纬度和经度，和大格子宽度减去一个小格子宽度的差值
//		List<JSONObject> nearByJsonObjects = gridTable.findPoiInLargeGrid(largeGrid[0],largeGrid[1], 0.005);


//		gridTable.getData();
		//gridTable.setGrid(48.456345, 136.567565,"123poi",timeDate,"12345");
		
		//gridTable.importSomedata();
		
		/*Set<JSONObject> jsonObjects = gridTable.findNearPoiIInfo(116.352285, 39.982739, 1);
		for (JSONObject jsonObject : jsonObjects) {
			System.out.println(jsonObject.toString());
		}*/
		//gridTable.findNearPoiIInfo(39.952731,116.410962, 0.01);
		//gridTable.regionIsExist(47.456345, 268.567565);
		//gridTable.deletePOIGrid(116.35044722222221,39.96302777777778);
	}
	
	
}
