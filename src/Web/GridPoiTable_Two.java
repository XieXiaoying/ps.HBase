package Web;



import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
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
 * @author
 * 2016-3-23
 * 
 * 
 * */
public class GridPoiTable_Two extends AbstractTable{
	Logger logger = Logger.getLogger(GridPoiTable_Two.class);
	
	private static long gridid;
	private static final byte[] INFO_CF = "info".getBytes();
	
	private static final byte[] GRIDPOI_COL = "gridPoi".getBytes();//站点固定参数
	private static final byte[] GRIDDETAIL_COL = "gridDetail".getBytes();//站点可更新参数 ：pm值，图片url，优秀子站点ID
	 
	private static final String[] family = {"info"};
	
	private static final String tableName = "gridPoi222";
	
	
	public GridPoiTable_Two(){
		try{
			createTable(Constants.conf, tableName, family);
			hTable = new HTable(Constants.conf, tableName);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 建立一条数据
	 * @param lat
	 * @param lon
	 * @param poiid
	 * @param timestamp
	 * @param latestPm
	 * @param subPoiID
	 * @param picUrl
	 * @return
	 */
	public boolean setLargeGrid(double lat, double lon, String poiid, Date timestamp,
				 String latestPm, String subPoiID,String picUrl){
			//先算的此处的小格子
//			double[] gridxy_smallGrid = computeGridxy(lat, lon);
			//再根据小格子算得大格子
			double[] gridxy = computeXy(lat, lon);
			//行key是：左上纬度/左上经度/右下纬度/右下经度
			Get get = new Get(generateRowKey(gridxy[0],gridxy[1]));
			get.addColumn(INFO_CF, GRIDPOI_COL);
			try{
				if(!this.rowExists(get)){
					Date time = timestamp;
					gridid += 1;
					if(poiid == null){
						String poiString = ComputePOI.computePOI(lon, lat);
						poiid = poiString;
						//System.out.println(poiString);
					}
					
					if(!setLargeGrid2(gridxy[0],gridxy[1],gridid,poiid,time,lat,lon,latestPm,subPoiID,picUrl)){
						
						return false;
					}
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
			return true;
	}	
	
	/**
	 * 建立大格子(所在位置经纬度，时间戳，站点ID，最近最优的pm2.5，最优的子站点缩略图url)
	 * @param latUpLeft
	 * @param lonUpLeft
	 * @param latBtmRight
	 * @param lonBtmRight
	 * @param gridid
	 * @param poiString
	 * @param timestamp
	 * @param lat
	 * @param lon
	 * @param latestPm
	 * @param subPoiID
	 * @param picUrl
	 * @return
	 */
	public boolean setLargeGrid2(double alat, double alon, long gridid, String poiString, Date timestamp,double lat,double lon,String latestPm,String subPoiID,String picUrl){
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("gridId", gridid);
			jsonObject.accumulate("poiId", poiString);
			jsonObject.accumulate("time", timestamp.getTime());
			
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.accumulate("lat", lat);
			jsonObject2.accumulate("lon", lon);
			jsonObject2.accumulate("pm2.5", latestPm);
			jsonObject2.accumulate("subPoiId", subPoiID);
			jsonObject2.accumulate("picUrl", picUrl);
			
			
			List<KeyValue> keyValues = new ArrayList<KeyValue>();
			//加入大战点的固定参数和可变参数
			keyValues.add(new KeyValue(generateRowKey(alat, alon),
					INFO_CF, GRIDPOI_COL, Bytes.toBytes(jsonObject.toString())));
			keyValues.add(new KeyValue(generateRowKey(alat, alon),
					INFO_CF, GRIDDETAIL_COL, Bytes.toBytes(jsonObject2.toString())));
			
			if(!put(generateRowKey(alat, alon), null, keyValues)){
				
				System.out.println("put is false 111");
				return false;
			}
			return true;
	}
	
	/**
	 * 更新大站点数据
	 * @param lat
	 * @param lon
	 * @param latestPm
	 * @param subPoiID
	 * @param pic_url
	 * @return
	 */
	public boolean updateLargeGrid(double lat, double lon,String PoiID,Date timestamp,String latestPm,String subPoiID,String pic_url){
		//不存在更新，应该是，删除原有的表中的所有数据，重新生成新的数据
		double[] gridxy = computeXy(lat, lon);
		
		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.accumulate("lat", lat);
		jsonObject2.accumulate("lon", lon);
		jsonObject2.accumulate("pm2.5", latestPm);
		jsonObject2.accumulate("subPoiId", subPoiID);
		jsonObject2.accumulate("picUrl", pic_url);
		
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		//可变参数
		keyValues.add(new KeyValue(generateRowKey(gridxy[0], gridxy[1]),
				INFO_CF, GRIDDETAIL_COL, Bytes.toBytes(jsonObject2.toString())));
		
		if(!put(generateRowKey(gridxy[0], gridxy[1]), null, keyValues)){
			
			System.out.println("put is false 111");
			return false;
		}
		return true;
		
		
	}
	
	/**
	 * 不需要该判断方法
	 * 如果该区域不存在，则生成一个poiID
	 * @param lat
	 * @param lon
	 * @return
	 */
//	public String regionIsExist(double lat, double lon){
//		//先算的此处的小格子
//		double[] gridxy_smallGrid = computeGridxy(lat, lon);
//		//再根据小格子算得大格子
//		double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
//		
//		System.out.println(gridxy[0] + "  " + gridxy[1] + "  " + gridxy[2] + "  " + gridxy[3] + "  ");
//		String poiString = null;
//		
//		Get get = new Get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]));
//		get.addColumn(INFO_CF, GRIDPOI_COL);
//		try{
//			if(!this.rowExists(get)){
//				poiString = ComputePOI.computePOI(lon, lat);
//			}
//			
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		System.out.println(poiString);
//		return poiString;
//	}
	

	//判断当前位置是否有站点，小格子里面，如果有，则返回该站点的ID
//	public String regionIsExistPOI(double lat, double lon) {
//		
//		double[] gridxy = computeGridxy(lat, lon);
//		System.out.println(gridxy[0] + "  " + gridxy[1] + "  " + gridxy[2] + "  " + gridxy[3] + "  ");
//		
//		String poiString = null;
//	
//		Get get = new Get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]));
//		get.addColumn(INFO_CF, GRIDPOI_COL);
//		try{
//			if(this.rowExists(get)){
//				Result rs = hTable.get(get);
//				for(KeyValue kv : rs.list()){
//					JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
//					
//					poiString =  jsonObject.getString("poiId");
//				}
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		System.out.println(poiString);
//		return poiString;
//		
//	}
	
	
	/**不需要该判断方法
	 * 判断当前所在的大格子是否有站点
	 * @param lat
	 * @param lon
	 * @return
	 */
//	public String regionIsExistLargePOI(double lat, double lon) {
//			//大格子计算方法，得出格子左上右下的经纬度，作为key去查询“gridPoi111”表里是否有该格子数据
//			//先算的此处的小格子
//			double[] gridxy_smallGrid = computeGridxy(lat, lon);
//			//再根据小格子算得大格子
//			double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
//			System.out.println(gridxy[0] + "  " + gridxy[1] + "  " + gridxy[2] + "  " + gridxy[3] + "  ");
//			
//			String poiString = null;
//		
//			Get get = new Get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]));
//			get.addColumn(INFO_CF, GRIDPOI_COL);
//			try{
//				if(this.rowExists(get)){
//					Result rs = hTable.get(get);
//					for(KeyValue kv : rs.list()){
//						JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
//						
//						poiString =  jsonObject.getString("poiId");
//					}
//				}
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			System.out.println(poiString);
//			return poiString;
//			
//	}
	
	
	
	

	public String findGridId(double lat, double lon) throws IOException{
		
		double[] gridxy = computeXy(lat, lon);
		System.out.println(gridxy[0] + "   " + gridxy[1]+ "   " + gridxy[2]+ "   " + gridxy[3]);
		//System.out.println(latUpLeft + "  " + lonUpLeft + "  " + latBtmRight + "  " + lonBtmRight);
		Result rs = get(generateRowKey(gridxy[0],gridxy[1]), 
				null, null, null, false);
		
		
		String gridId = null;
		for(KeyValue kv : rs.list()){
			gridId = Bytes.toString(kv.getValue());
		}
		
		System.out.println(gridId);
		return gridId;
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
		double latbtmup = lat-distance;
		double lonbtmup = lon+distance;
		
		
		
		//System.out.println(uplonlat[0] + "   " + uplonlat[1] + "   " + uplonlat[2] + "   " + uplonlat[3]);
		//System.out.println(btmlonlat[0] + "   " + btmlonlat[1] + "   " + btmlonlat[2] + "   " + btmlonlat[3]);
		
		Scan scan = new Scan();
		
		JSONObject jsonObject = null;
		for(double startlat = latupup; startlat >= latbtmup;){
			//注意这个在搜索gridPoi这个表时，行键的经纬度顺序为：右下角纬度/右下角经度/左上角纬度/左上角经度
			scan.setStartRow(generateRowKey(startlat, lonupup));
			scan.setStopRow(generateRowKey(startlat, lonbtmup));
			scan.addColumn(INFO_CF, GRIDPOI_COL);
			ResultScanner scanner = this.scan(scan, null, null, null);
			
			for(Result result : scanner){
				for(KeyValue kv : result.list()){
					System.out.println("find Poi in large Grid");
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
	
	
		/**
		 * 输入的参数为大格子的（左上角纬度，左上角经度，格子边长），此时格子边长为0.005度，
		 * 遍历大格子的方法
		 * 输入：当前点的经纬度，根据地图等级获得的距离值
		 * @param lat
		 * @param lon
		 * @param distance
		 * @return
		 */
		public List<JSONObject> findLargePoiNearbyGrid(double lat, double lon, double distance){
			DecimalFormat df = new DecimalFormat("0.000000");
			
			//输入为当前所在坐标点
			double[] gridxy = computeXy(lat, lon);
			//遍历开始的左上角：这个和distance有关，在当前大格子的基础上，用0.01*ditance
			//distance参数代表多少公里
			double latupup = gridxy[0]+distance*0.01;
			double lonupup = gridxy[1]-distance*0.01;
			//右下角
			double latbtmup = gridxy[0]-distance*0.01;
			double lonbtmup = gridxy[1]+distance*0.01;
			
			Scan scan = new Scan();
			
			List<JSONObject> poiList = new ArrayList<JSONObject>();
			 
//			JSONObject nearbyPoiResult = new JSONObject();
			JSONObject jsonObject = null;
//			JSONArray nearPois = new JSONArray();
//			nearbyPoiResult.accumulate("level", "mainPoi");
			
//			for(double startlat = latupup; startlat >= latbtmup;){
				scan.setStartRow(generateRowKey(latbtmup, lonbtmup));
				scan.setStopRow(generateRowKey(latupup, lonupup));
				scan.addColumn(INFO_CF, GRIDPOI_COL);
				scan.addColumn(INFO_CF, GRIDDETAIL_COL);
				ResultScanner scanner = this.scan(scan, null, null, null);
				
				for(Result result : scanner){
					JSONObject largePoiDetail = new JSONObject();
					for(KeyValue kv : result.list()){
//						largePoiDetail.accumulate("num", i++);
						
						if(Bytes.toString(kv.getQualifier()).equals("gridDetail")){
							largePoiDetail = JSONObject.fromObject(Bytes.toString(kv.getValue()));
						}
						if(Bytes.toString(kv.getQualifier()).equals("gridPoi")){
							jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
							largePoiDetail.accumulate("gridId", jsonObject.getString("gridId"));
							largePoiDetail.accumulate("poiId", jsonObject.getString("poiId"));
							largePoiDetail.accumulate("time", jsonObject.getString("time"));
						}
						
					}
					poiList.add(largePoiDetail);
				}
				try {
					scanner.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
//				startlat = Double.valueOf(Constants.DF.format(startlat - 0.005));
				
//			}
//			nearbyPoiResult.accumulate("stations", nearPois);
			return poiList;
		}
	
	
//	public List<JSONObject> findNearPoiIInfo(double lat, double lon, double distans){
//		DecimalFormat df = new DecimalFormat("0.000000");
//		List<String> nearPoi = new ArrayList<String>();
//		double[] uplonlat = computeGridxy(lat + distans, lon - distans);
//		double latupup = uplonlat[0];
//		double lonupup = uplonlat[1];
//		
//		
//		
//		double[] btmlonlat = computeGridxy(lat - distans, lon + distans);
//		double latbtmup = btmlonlat[0];
//		double lonbtmup = btmlonlat[1];
//		
//		
//		
//		//System.out.println(uplonlat[0] + "   " + uplonlat[1] + "   " + uplonlat[2] + "   " + uplonlat[3]);
//		//System.out.println(btmlonlat[0] + "   " + btmlonlat[1] + "   " + btmlonlat[2] + "   " + btmlonlat[3]);
//		
//		Scan scan = new Scan();
//		
//		JSONObject jsonObject = null;
//		for(double startlat = latupup; startlat >= latbtmup;){
//			scan.setStartRow(generateRowKey(startlat, lonupup));
//			scan.setStopRow(generateRowKey(startlat, lonbtmup));
//			scan.addColumn(INFO_CF, GRIDPOI_COL);
//			ResultScanner scanner = this.scan(scan, null, null, null);
//			
//			for(Result result : scanner){
//				for(KeyValue kv : result.list()){
//					System.out.println("mark~!!");
//					jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
//					nearPoi.add(jsonObject.getString("poiId"));
//					System.out.println(jsonObject.getString("poiId"));
//				}
//			}
//			try {
//				scanner.close();
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}
//			startlat = Double.valueOf(Constants.DF.format(startlat - 0.001));
//			
//		}
//		POITable poiTable = new POITable();
//		return poiTable.getAllPOIOnePic(nearPoi);
//		
//		
//	}
	
	
	
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
	
	
	/**
	 * 删除站点
	 * @param blon
	 * @param blat
	 * @return
	 */
	public boolean deleteLargePOIGrid(double blon, double blat){
		double[] gridxy = computeXy(blat, blon);
		
//		//double[] lonlat = ComputePOI.computeLonlat(poi);
//		
//		double[] gridxy = computeGridxy(blat,blon);
//		System.err.println(gridxy[0] + gridxy[1] + gridxy[2] + gridxy[3]);
		this.deleteColumn(generateRowKey(gridxy[0],gridxy[1]),
				INFO_CF, GRIDPOI_COL);
		this.deleteColumn(generateRowKey(gridxy[0],gridxy[1]),
				INFO_CF, GRIDDETAIL_COL);
		return true;
	}
	
	//获得所有大格子数据(还未完成)
//	public List<JSONObject> getData() {
//		/*		for(int i = 0; i < 100; i++) {
//					Get get = new Get(Bytes.toBytes("row-"+i));
//					get.addColumn(INFO_CF,TEST_COL);
//					try {
//						Result rs = hTable.get(get);
////						for(KeyValue kv : rs.list()){
////							JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
////							
////							String poiString =  jsonObject.getString("poiId");
////						}
//						byte[] val = rs.getValue(INFO_CF, TEST_COL);
//						System.out.println("test" + Bytes.toString(val));
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}*/
//		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
//		List<String> nearPoi = new ArrayList<String>();
//				try {
//					Scan scan = new Scan();
//					//scan.setTimeStamp(1452691836830L);
//					ResultScanner rs = hTable.getScanner(scan);
//					JSONObject jsonObject = new JSONObject();
//						for (Result r = rs.next(); r != null; r = rs.next()) {
//						// process result...
//							jsonObject = JSONObject.fromObject(Bytes.toString(r.getValue(INFO_CF, GRIDPOI_COL)));
//							//byte[] val = r.getValue(INFO_CF, TEST_COL);
//							System.out.println("test" + jsonObject.getString("poiId"));
//							nearPoi.add(jsonObject.getString("poiId"));
//							//jsonObject.accumulate("gridId", jsonObject.getString("gridId"));
//							//jsonObject.accumulate("poiId", jsonObject.getString("poiId"));
//							//jsonObject.accumulate("time", jsonObject.getString("time"));
//							//jsonObject.accumulate("builder", jsonObject.getString("builder"));
//							System.out.println(jsonObject);
//							jsonObjects.add(jsonObject);
//						}						 
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}				
//				POITable poiTable = new POITable();
//				return poiTable.getAllPOIOnePic(nearPoi);
//	}
	
	
	
		public byte[] generateRowKey(double lat, double lon){
			String rowKey = lat + Constants.SEPARATER + lon;
			return rowKey.getBytes();
		}
		
//		//这里左上角右下角颠倒了
//		public byte[] generateRowKey(double latUpLeft,double lonUpLeft){
//			double latBtmRight = latUpLeft;
//			double lonBtmRight = lonUpLeft;
//			return generateRowKey(latUpLeft, lonUpLeft,latBtmRight,lonBtmRight);
//		}
		

		//大格子的左上点、右下点的坐标
		//输入参数为小格子左上点的坐标
		//大格子边长500米，经纬度0.005度
		public static double[] computeXy(double lat,double lon){
			DecimalFormat df = new DecimalFormat("0.000000");
			double[] gridxy = new double[2];
			String latString = String.valueOf(lat);
			String lonString = String.valueOf(lon);
			
//			int indexlat = latString.indexOf(".") + 3;
//			int indexlon = lonString.indexOf(".") + 3;
//			
//			String latbefor = latString.substring(0, indexlat);//即截取至小数点后三位
//			String latlater = latString.substring(indexlat);
//			String lonbefor = lonString.substring(0, indexlon);
//			String lonlater = lonString.substring(indexlon);
//			double rightBtmLat;
//			double rightBtmLon;
//			double leftUpLat;
//			double leftUpLon;
//			
//			//当大于等于5713时，只能是5713，6713，8713，9713
//			if(Integer.valueOf(latlater) >= 5713){
//				rightBtmLat = Double.valueOf(latbefor + "5713");
//				leftUpLat = rightBtmLat + 0.005;
//				
//			}else{//否则就是0713，1713，2713，3713，4713
//				rightBtmLat = Double.valueOf(latbefor + "0713");
//				leftUpLat = rightBtmLat+0.005;
//			}
//			
//			if(Integer.valueOf(lonlater) >= 5413){
//				leftUpLon = Double.valueOf(lonbefor + "5413");
//				rightBtmLon = leftUpLon+0.005;
//			}
//			else{
//				leftUpLon = Double.valueOf(lonbefor + "0413");
//				rightBtmLon = leftUpLon+0.005;
//			}
//			
//			gridxy[0] = Double.valueOf(df.format(leftUpLat));
//			gridxy[1] = Double.valueOf(df.format(leftUpLon));
//			gridxy[2] = Double.valueOf(df.format(rightBtmLat));
//			gridxy[3] = Double.valueOf(df.format(rightBtmLon));
			gridxy[0] = Double.valueOf(latString);
			gridxy[1] = Double.valueOf(lonString);
			return gridxy;
		}
		
		
//		//计算基本的小格子大小
//		public static double[] computeGridxy(double lat,double lon){
//			DecimalFormat df = new DecimalFormat("0.000000");
//			double[] gridxy = new double[4];
//			String latString = String.valueOf(df.format(lat));
//			String lonString = String.valueOf(df.format(lon));
//			
////			int indexlat = latString.indexOf(".") + 4;
////			int indexlon = lonString.indexOf(".") + 4;
////			
////			String latbefor = latString.substring(0, indexlat);//即截取至小数点后三位
////			String latlater = latString.substring(indexlat);
////			String lonbefor = lonString.substring(0, indexlon);
////			String lonlater = lonString.substring(indexlon);
////			double latUpLeft;
////			double latBtmRight;
////			double lonUpLeft;
////			double lonBtmRight;
////			
////			
////			if(Integer.valueOf(latlater) >= 713){
////				latUpLeft = Double.valueOf(latbefor + "713");
////				latBtmRight = Double.valueOf(latbefor) + 0.001713;
////				
////			}
////			
////			else{
////				latUpLeft = Double.valueOf(latbefor) - 0.001 + 0.000713;
////				latBtmRight = Double.valueOf(latbefor) + 0.000713;
////			}
////			
////			
////			if(Integer.valueOf(lonlater) >= 413){
////				lonUpLeft = Double.valueOf(lonbefor) + 0.001413;
////				lonBtmRight = Double.valueOf(lonbefor) + 0.000413;
////			}
////			else{
////				lonUpLeft = Double.valueOf(lonbefor) + 0.000413;
////				lonBtmRight = Double.valueOf(lonbefor) - 0.001 + 0.000413;
////			}
////			
////			//这里上届代码彻底搞混了，实际上真正最后输出的是
////			//gridxy[0]右下角纬度lat,gridxy[1]右下角经度lon,gridxy[2]左上角纬度,gridxy[3]左上角经度
////			gridxy[0] = Double.valueOf(df.format(latUpLeft));
////			gridxy[1] = Double.valueOf(df.format(lonUpLeft));
////			gridxy[2] = Double.valueOf(df.format(latBtmRight));
////			gridxy[3] = Double.valueOf(df.format(lonBtmRight));
//			gridxy[0] = Double.valueOf(df.format(latString));
//			gridxy[1] = Double.valueOf(df.format(lonString));
//			gridxy[2] = Double.valueOf(df.format(latString));
//			gridxy[3] = Double.valueOf(df.format(lonString));
//			return gridxy;
//		}
	
	public static void main(String[] args) throws IOException{
//		GridPoiTable_Two gridTable_two = new GridPoiTable_Two();
////		GridPoiTable gridTable = new GridPoiTable();
//		Date timeDate  = new Date();
//		
//		List<JSONObject> gridTable_Result = new ArrayList<JSONObject>();
//		gridTable_Result = gridTable.getData();
		
//		for (JSONObject single_grid : gridTable_Result) {
//			double lat = single_grid.getDouble("lat");
//			double lon = single_grid.getDouble("lon");
//			String pm = single_grid.getDouble("fpm")+"/"+single_grid.getDouble("actual_fpm");
//			String subId = single_grid.getString("poi");
////			String address = single_grid.getString("address");
//			String pic_url = single_grid.getString("pic");
//			
//			String poiId = gridTable_one.regionIsExist(lat,lon);
//			if(poiId!=null){
//				gridTable_one.setLargeGrid(lat, lon, poiId, timeDate,
//						pm, subId,pic_url);
//			}else{
//				gridTable_one.updateLargeGrid(lat, lon, pm, subId,pic_url);
//			}
//			
//		}
		
		/**
		 * 查看所有上一层站点（主站站点信息）
		 */
		GridPoiTable_One gridTable111 = new GridPoiTable_One();
		GridPoiTable_Two gridTable_two = new GridPoiTable_Two();
		POITable pTable = new POITable();
		
		List<JSONObject> gridTable111_Result = new ArrayList<JSONObject>();
		gridTable111_Result = gridTable111.getData();
		System.out.println(gridTable111_Result);
		
		Map<String,Integer> station222Map =  new HashMap<String,Integer>();
		Map<String,String> subPoiMap =  new HashMap<String,String>();
		
		for (JSONObject single_grid : gridTable111_Result) {
			String address = null;
			String subPoi = null;
			if(single_grid.get("subInfo")!=null){
				double lat = single_grid.getJSONObject("subInfo").getDouble("blat");
				double lon = single_grid.getJSONObject("subInfo").getDouble("blon");
				subPoi = single_grid.getJSONObject("subInfo").getString("poi");
				if(lat!=0&&lon!=0){
					address = Geocoder.gps2SegmentAdressForGridPoiTable_Two(lat,lon);
					System.out.println("\n该站地址:"+address);
				}
			}
			
			List<String> list = new ArrayList<String>();
			list.add(subPoi);
			List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
			jsonObjects = pTable.getAllPOIAllPic(list);
			int picNums = jsonObjects.get(0).getJSONArray("pic").size();
			
			if(station222Map.containsKey(address)){
				if(station222Map.get(address)<=picNums){
					station222Map.put(address,picNums);
					subPoiMap.put(address,subPoi);
				}
			}else{
				station222Map.put(address,picNums);
				subPoiMap.put(address,subPoi);
			}
		}
		for (Map.Entry<String, String> entry : subPoiMap.entrySet()) {
			   System.out.println("key= " + entry.getKey() + " value= " + entry.getValue());
		}
		
		for (JSONObject single_grid : gridTable111_Result) {
//			String address = null;
			String subPoi = null;
			double lat = 0;
			double lon = 0;
			String pic_url = null;
			String pm = null;
			if(single_grid.get("subInfo")!=null){
//				double blat = single_grid.getJSONObject("subInfo").getDouble("blat");
//				double blon = single_grid.getJSONObject("subInfo").getDouble("blon");
				lat = single_grid.getJSONObject("subInfo").getDouble("blat");
				lon = single_grid.getJSONObject("subInfo").getDouble("blon");
				pic_url = single_grid.getJSONObject("subInfo").getString("pic");
				pm = single_grid.getJSONObject("subInfo").get("fpm").toString();
				subPoi = single_grid.getJSONObject("subInfo").getString("poi");
				
			}
			Date timeDate  = new Date();
			if(subPoiMap.containsValue(subPoi)){
				gridTable_two.setLargeGrid(lat, lon, null, timeDate,
						pm,subPoi,pic_url);
				
			}
		}
		
		
		
		
		
		
		
//		gridTable_two.updateLargeGrid(39.969723, 116.362423,null, timeDate, "111", "000", "test_3.png");
//		gridTable_two.updateLargeGrid(39.969733, 116.362433,null, timeDate, "111", "000", "test_3.png");
//		gridTable_two.updateLargeGrid(39.969743, 116.362443,null, timeDate, "111", "000", "test_3.png");
//		//找到此位置附近大站点距离为distance = 1
//		List<JSONObject> poiList = new ArrayList<JSONObject>();
//		List<JSONObject> nearbyPoi = new ArrayList<JSONObject>();
//		nearbyPoi = gridTable_two.findLargePoiNearbyGrid(39.969713, 116.362413,10);
//		System.out.println(nearbyPoi);
		
		
//		//再根据小格子算得大格子
//		double[] gridxy = computeLargeGridxy(39.969713, 116.362413);
//				
//		List<JSONObject> result = new ArrayList<JSONObject>();
//		result = gridTable.findPoiInLargeGrid(gridxy[0], gridxy[1], 0.004);
//		System.out.println(result);
		
		
		
		//gridTable.setGrid(47.456345, 268.567565,timeDate, "testfor1");
		//double[] gridxy = GridPoiTable.computeGridxy(48.456345, 136.567565);
		//System.out.println(gridxy[0] + "  " + gridxy[1] + "  " + gridxy[2] + "  " + gridxy[3] + "  ");
		
		//gridTable.regionIsExist(116.567565,39.456345);
		
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
