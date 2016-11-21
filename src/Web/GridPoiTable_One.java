package Web;



import java.io.IOException;
import java.text.DecimalFormat;
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
public class GridPoiTable_One extends AbstractTable{
	Logger logger = Logger.getLogger(GridPoiTable_One.class);
	
	private static long gridid;
	private static final byte[] INFO_CF = "info".getBytes();
	
	private static final byte[] GRIDPOI_COL = "gridPoi".getBytes();//站点固定参数
	private static final byte[] GRIDDETAIL_COL = "gridDetail".getBytes();//站点可更新参数 ：pm值，图片url，优秀子站点ID
	 
	private static final String[] family = {"info"};
	
	private static final String tableName = "gridPoi111";
	
	
	public GridPoiTable_One(){
		try{
			createTable(Constants.conf, tableName, family);
			hTable = new HTable(Constants.conf, tableName);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 建立大格子
	 * @param lat
	 * @param lon
	 * @param poiid
	 * @param timestamp
	 * @param latestPm
	 * @param subPoiID
	 * @param picUrl
	 * @return
	 */
	public boolean setLargeGrid(double lat, double lon, String poiid, Date timestamp,int pre_pm,
				 int actual_Pm, String subPoiID,String picUrl){
			//先算的此处的小格子
			double[] gridxy_smallGrid = computeGridxy(lat, lon);
			//再根据小格子算得大格子
			double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
			//行key是：左上纬度/左上经度/右下纬度/右下经度
			Get get = new Get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]));
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
					
					if(!setLargeGrid(gridxy[0],gridxy[1],gridxy[2],gridxy[3],gridid,poiid,time,lat,lon,pre_pm,actual_Pm,subPoiID,picUrl)){
						
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
	public boolean setLargeGrid(double latUpLeft, double lonUpLeft, double latBtmRight, 
				double lonBtmRight, long gridid, String poiString, Date timestamp,double lat,double lon,int pre_pm,int actual_pm,String subPoiID,String picUrl){
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("gridId", gridid);
			jsonObject.accumulate("poiId", poiString);
			jsonObject.accumulate("time", timestamp.getTime());
			
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.accumulate("lat", lat);
			jsonObject2.accumulate("lon", lon);
			jsonObject2.accumulate("fpm",pre_pm);
			jsonObject2.accumulate("actual_fpm",actual_pm);
			jsonObject2.accumulate("subPoiId", subPoiID);
			jsonObject2.accumulate("picUrl", picUrl);
			
			
			List<KeyValue> keyValues = new ArrayList<KeyValue>();
			//加入大战点的固定参数和可变参数
			keyValues.add(new KeyValue(generateRowKey(latUpLeft, lonUpLeft, latBtmRight, lonBtmRight),
					INFO_CF, GRIDPOI_COL, Bytes.toBytes(jsonObject.toString())));
			keyValues.add(new KeyValue(generateRowKey(latUpLeft, lonUpLeft, latBtmRight, lonBtmRight),
					INFO_CF, GRIDDETAIL_COL, Bytes.toBytes(jsonObject2.toString())));
			
			if(!put(generateRowKey(latUpLeft, lonUpLeft, latBtmRight, lonBtmRight), null, keyValues)){
				
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
	public boolean updateLargeGrid(double lat, double lon,int pre_pm,int actual_pm,String subPoiID,String pic_url){
		//先算的此处的小格子
		double[] gridxy_smallGrid = computeGridxy(lat, lon);
		//再根据小格子算得大格子
		double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
		
		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.accumulate("lat", lat);
		jsonObject2.accumulate("lon", lon);
		jsonObject2.accumulate("fpm",pre_pm);
		jsonObject2.accumulate("actual_fpm",actual_pm);
		jsonObject2.accumulate("subPoiId", subPoiID);
		jsonObject2.accumulate("picUrl", pic_url);
		
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		//可变参数
		keyValues.add(new KeyValue(generateRowKey(gridxy[0], gridxy[1], gridxy[2], gridxy[3]),
				INFO_CF, GRIDDETAIL_COL, Bytes.toBytes(jsonObject2.toString())));
		
		if(!put(generateRowKey(gridxy[0], gridxy[1], gridxy[2], gridxy[3]), null, keyValues)){
			
			System.out.println("put is false 111");
			return false;
		}
		return true;
		
		
	}
	
	/**
	 * 如果该区域不存在，则生成一个poiID
	 * @param lat
	 * @param lon
	 * @return
	 */
	public String regionIsExist(double lat, double lon){
		//先算的此处的小格子
		double[] gridxy_smallGrid = computeGridxy(lat, lon);
		//再根据小格子算得大格子
		double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
		
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
//		System.out.println(poiString);
		return poiString;
	}
	

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
	
	
	/**
	 * 判断当前所在的大格子是否有站点
	 * @param lat
	 * @param lon
	 * @return
	 */
	public String regionIsExistLargePOI(double lat, double lon) {
			//大格子计算方法，得出格子左上右下的经纬度，作为key去查询“gridPoi111”表里是否有该格子数据
			//先算的此处的小格子
			double[] gridxy_smallGrid = computeGridxy(lat, lon);
			//再根据小格子算得大格子
			double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
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
	
	
	/**
	 * 判断并返回当前所在的大格子站点
	 * @param lat
	 * @param lon
	 * @return
	 */
	public List<JSONObject> LargePoiDetail(double lat, double lon) {
			//大格子计算方法，得出格子左上右下的经纬度，作为key去查询“gridPoi111”表里是否有该格子数据
			//先算的此处的小格子
			double[] gridxy_smallGrid = computeGridxy(lat, lon);
			//再根据小格子算得大格子
			double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
			System.out.println(gridxy[0] + "  " + gridxy[1] + "  " + gridxy[2] + "  " + gridxy[3] + "  ");
		
			Get get = new Get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]));
			get.addColumn(INFO_CF, GRIDPOI_COL);
			try{
				if(this.rowExists(get)){
					return findLargePoiNearbyGrid(lat,lon,0);
				}else{
					return null;
				}
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
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
		 * 输入的参数为大格子的（左上角纬度，左上角经度，格子边长），此时格子边长为0.02度，求distance个单位大格子
		 * 遍历大格子的方法
		 * 输入：当前点的经纬度，根据地图等级获得的距离值
		 * @param lat
		 * @param lon
		 * @param distance
		 * @return
		 */
		public List<JSONObject> findLargePoiNearbyGrid(double lat, double lon, double distance){
			DecimalFormat df = new DecimalFormat("0.000000");
			
			//先算的此处的小格子
			double[] gridxy_smallGrid = computeGridxy(lat, lon);
			//再根据小格子算得大格子左上角右下角坐标
			double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
			//遍历开始的左上角：这个和distance有关，在当前大格子的基础上，用0.02*ditance,即为
			//左上角
			double latupup = gridxy[0]+distance*0.02;
			double lonupup = gridxy[1]-distance*0.02;
			//右下角
			double latbtmup = gridxy[0]-(distance+1)*0.02;
			double lonbtmup = gridxy[1]+(distance+1)*0.02;
			
			Scan scan = new Scan();
			
			List<JSONObject> poiList = new ArrayList<JSONObject>();
			 
//			JSONObject nearbyPoiResult = new JSONObject();
			JSONObject jsonObject = null;
//			JSONArray nearPois = new JSONArray();
//			nearbyPoiResult.accumulate("level", "mainPoi");
			lonupup = lonupup- 0.000001;//因为double，修正下
			for(double startlat = latupup; startlat > latbtmup;){
				scan.setStartRow(generateRowKey(startlat, lonupup,0,0));
				scan.setStopRow(generateRowKey(startlat, lonbtmup,0,0));
				System.out.println("start:"+generateRowKeyString(startlat, lonupup,0,0));
				System.out.println("stop:"+generateRowKeyString(startlat, lonbtmup,0,0));
				scan.addColumn(INFO_CF, GRIDPOI_COL);
				scan.addColumn(INFO_CF, GRIDDETAIL_COL);
				ResultScanner scanner = this.scan(scan, null, null, null);
				
				for(Result result : scanner){
					JSONObject largePoiDetail = new JSONObject();
					String subPoiId =null;
					for(KeyValue kv : result.list()){
//						largePoiDetail.accumulate("num", i++);
						
						if(Bytes.toString(kv.getQualifier()).equals("gridDetail")){
							largePoiDetail = JSONObject.fromObject(Bytes.toString(kv.getValue()));
							largePoiDetail.accumulate("time", kv.getTimestamp());
							subPoiId = JSONObject.fromObject(Bytes.toString(kv.getValue())).getString("subPoiId");
//							jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
//							long timestamp = kv.getTimestamp();
//							largePoiDetail.accumulate("lat", jsonObject.getString("lat"));
//							largePoiDetail.accumulate("lon", jsonObject.getString("lon"));
//							largePoiDetail.accumulate("pm2.5", jsonObject.getString("pm2.5"));
//							largePoiDetail.accumulate("subPoiId", jsonObject.getString("subPoiId"));
//							largePoiDetail.accumulate("picUrl", jsonObject.getString("picUrl"));
//							
//							Date latestDate = new Date(timestamp);
//							SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
//							largePoiDetail.accumulate("latestTime", sdFormat.format(latestDate));
							
						}
						
						
						if(Bytes.toString(kv.getQualifier()).equals("gridPoi")){
							jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
							largePoiDetail.accumulate("gridId", jsonObject.getString("gridId"));
							largePoiDetail.accumulate("poiId", jsonObject.getString("poiId"));
//							largePoiDetail.accumulate("time", jsonObject.getString("time"));
							
							
						}
						
					}
//					String subPoiId = largePoiDetail.getString("subPoiId");
					if(subPoiId!=null){
						POITable poiTable = new POITable();
						largePoiDetail.accumulate("subInfo", poiTable.getOnePOIOnePic(subPoiId));
					}
					System.out.println(largePoiDetail);
					poiList.add(largePoiDetail);
					
				}
				
				try {
					scanner.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				startlat = Double.valueOf(Constants.DF.format(startlat - 0.02));
				System.out.println("- 0.02");
			}
//			nearbyPoiResult.accumulate("stations", nearPois);
			return poiList;
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
		public List<JSONObject> findLargePoiAll(){
			Scan scan = new Scan();
			List<JSONObject> poiList = new ArrayList<JSONObject>();
			JSONObject jsonObject = null;
				scan.setStartRow(generateRowKey(0,0,0,0));
				scan.setStopRow(generateRowKey(45, 0,0,0));
				scan.addColumn(INFO_CF, GRIDPOI_COL);
				scan.addColumn(INFO_CF, GRIDDETAIL_COL);
				ResultScanner scanner = this.scan(scan, null, null, null);
				
				for(Result result : scanner){
					JSONObject largePoiDetail = new JSONObject();
					for(KeyValue kv : result.list()){
						if(Bytes.toString(kv.getQualifier()).equals("gridDetail")){
							largePoiDetail = JSONObject.fromObject(Bytes.toString(kv.getValue()));
							largePoiDetail.accumulate("time", kv.getTimestamp());
						}
						if(Bytes.toString(kv.getQualifier()).equals("gridPoi")){
							jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
							largePoiDetail.accumulate("gridId", jsonObject.getString("gridId"));
							largePoiDetail.accumulate("poiId", jsonObject.getString("poiId"));
//							largePoiDetail.accumulate("time", jsonObject.getString("time"));
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
			return poiList;
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
	
	
	/**
	 * 删除站点
	 * @param blon
	 * @param blat
	 * @return
	 */
	public boolean deleteLargePOIGrid(double blat, double blon){
		//先算的此处的小格子
		double[] gridxy_smallGrid = computeGridxy(blat, blon);
		//再根据小格子算得大格子
		double[] gridxy = computeLargeGridxy(gridxy_smallGrid[2], gridxy_smallGrid[3]);
		
//		//double[] lonlat = ComputePOI.computeLonlat(poi);
//		
//		double[] gridxy = computeGridxy(blat,blon);
		System.err.println(gridxy[0] + gridxy[1] + gridxy[2] + gridxy[3]);
//		this.deleteColumn(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]),
//				INFO_CF, GRIDPOI_COL);
		this.deleteColumn(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]),
				INFO_CF, GRIDDETAIL_COL);
		return true;
	}
	
	//获得所有大格子数据(还未完成)
	public List<JSONObject> getData() {

		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		List<String> nearPoi = new ArrayList<String>();
		try {
			Scan scan = new Scan();
			// scan.setTimeStamp(1452691836830L);
			ResultScanner rs = hTable.getScanner(scan);
			JSONObject jsonObject = new JSONObject();
			String subPoiId = null;
			for (Result r = rs.next(); r != null; r = rs.next()) {
				// process result...
				jsonObject = JSONObject.fromObject(Bytes.toString(r.getValue(INFO_CF, GRIDDETAIL_COL)));
				if(jsonObject!=null){
					subPoiId = jsonObject.getString("subPoiId");
				}
				
				
//				System.out.println(jsonObject);
				if (subPoiId != null) {
					POITable poiTable = new POITable();
					jsonObject.accumulate("subInfo",poiTable.getOnePOIOnePic(subPoiId));
				}
				jsonObjects.add(jsonObject);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObjects;
	}
	
	
	
		public byte[] generateRowKey(double latUpLeft, double lonUpLeft, double latBtmRight,
				double lonBtmRight){
			String rowKey = latUpLeft + Constants.SEPARATER + lonUpLeft + Constants.SEPARATER
					+ latBtmRight + Constants.SEPARATER + lonBtmRight;
			
			return rowKey.getBytes();
		}
		
		public String generateRowKeyString(double latUpLeft, double lonUpLeft, double latBtmRight,
				double lonBtmRight){
			String rowKey = latUpLeft + Constants.SEPARATER + lonUpLeft + Constants.SEPARATER
					+ latBtmRight + Constants.SEPARATER + lonBtmRight;
			
			return rowKey;
		}
		
		//这里左上角右下角颠倒了
		public byte[] generateRowKey(double latUpLeft,double lonUpLeft){
			double latBtmRight = Double.valueOf(Constants.DF.format(latUpLeft - 0.001));
			double lonBtmRight = Double.valueOf(Constants.DF.format(lonUpLeft + 0.001));
			return generateRowKey(latUpLeft, lonUpLeft,latBtmRight,lonBtmRight);
		}
		

		//大格子的左上点、右下点的坐标
		//输入参数为小格子左上点的坐标
		//大格子边长2000米，经纬度0.02度
		public static double[] computeLargeGridxy(double lat,double lon){
			DecimalFormat df = new DecimalFormat("0.000000");
			//进入计算大格子时，已经经过小格子计算，故不需再使用格式
			double[] gridxy = new double[4];
			String latString = String.valueOf(lat);
			String lonString = String.valueOf(lon);
			
			int indexlat = latString.indexOf(".") + 2;
			int indexlon = lonString.indexOf(".") + 2;
			
			String latbefor = latString.substring(0, indexlat);//即截取至小数点后一位
			String latlater = latString.substring(indexlat);
			String lonbefor = lonString.substring(0, indexlon);
			String lonlater = lonString.substring(indexlon);
			double rightBtmLat;
			double rightBtmLon;
			double leftUpLat;
			double leftUpLon;
			
			//只能是00713、20713、40713、60713、80713
			if(Integer.valueOf(latlater) <= 713){
				leftUpLat = Double.valueOf(latbefor + "00713");
				rightBtmLat = leftUpLat - 0.02;
			}else if(Integer.valueOf(latlater) > 713 && Integer.valueOf(latlater) <= 20713){
				rightBtmLat = Double.valueOf(latbefor + "00713");
				leftUpLat = rightBtmLat + 0.02;
			}else if(Integer.valueOf(latlater) > 20713 && Integer.valueOf(latlater) <= 40713){
				rightBtmLat = Double.valueOf(latbefor + "20713");
				leftUpLat = rightBtmLat + 0.02;
			}else if(Integer.valueOf(latlater) > 40713 && Integer.valueOf(latlater) <= 60713){
				rightBtmLat = Double.valueOf(latbefor + "40713");
				leftUpLat = rightBtmLat + 0.02;
			}else if(Integer.valueOf(latlater) > 60713 && Integer.valueOf(latlater) <= 80713){
				rightBtmLat = Double.valueOf(latbefor + "60713");
				leftUpLat = rightBtmLat + 0.02;
			}else{
				rightBtmLat = Double.valueOf(latbefor + "80713");
				leftUpLat = rightBtmLat + 0.02;
			}
			
			if(Integer.valueOf(lonlater) >= 413 && Integer.valueOf(lonlater) < 20413){
				leftUpLon = Double.valueOf(lonbefor + "00413");
				rightBtmLon = leftUpLon + 0.02;
			}else if(Integer.valueOf(lonlater) >= 20413 && Integer.valueOf(lonlater) < 40413){
				leftUpLon = Double.valueOf(lonbefor + "20413");
				rightBtmLon = leftUpLon + 0.02;
			}else if(Integer.valueOf(lonlater) >= 40413 && Integer.valueOf(lonlater) < 60413){
				leftUpLon = Double.valueOf(lonbefor + "40413");
				rightBtmLon = leftUpLon + 0.02;
			}else if(Integer.valueOf(lonlater) >= 60413 && Integer.valueOf(lonlater) < 80413){
				leftUpLon = Double.valueOf(lonbefor + "60413");
				rightBtmLon = leftUpLon + 0.02;
			}else{
				leftUpLon = Double.valueOf(lonbefor + "80413");
				rightBtmLon = leftUpLon + 0.02;
			}
			
			gridxy[0] = Double.valueOf(df.format(leftUpLat));
			gridxy[1] = Double.valueOf(df.format(leftUpLon));
			gridxy[2] = Double.valueOf(df.format(rightBtmLat));
			gridxy[3] = Double.valueOf(df.format(rightBtmLon));
			return gridxy;
		}
		
		
		//计算基本的小格子大小
		public static double[] computeGridxy(double lat,double lon){
			DecimalFormat df = new DecimalFormat("0.000000");
			double[] gridxy = new double[4];
			String latString = String.valueOf(df.format(lat));
			String lonString = String.valueOf(df.format(lon));
			
			int indexlat = latString.indexOf(".") + 4;
			int indexlon = lonString.indexOf(".") + 4;
			
			String latbefor = latString.substring(0, indexlat);//即截取至小数点后三位
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
			
			//这里上届代码彻底搞混了，实际上真正最后输出的是
			//gridxy[0]右下角纬度lat,gridxy[1]右下角经度lon,gridxy[2]左上角纬度,gridxy[3]左上角经度
			gridxy[0] = Double.valueOf(df.format(latUpLeft));
			gridxy[1] = Double.valueOf(df.format(lonUpLeft));
			gridxy[2] = Double.valueOf(df.format(latBtmRight));
			gridxy[3] = Double.valueOf(df.format(lonBtmRight));
			return gridxy;
		}
	
	public static void main(String[] args) throws IOException{
		
//		double lat = 42.552713;
//		double lon = 116.552413;
//		double[] largeGrid  = computeLargeGridxy(lat,lon);
//		System.out.println("largeGrid[0][1]:"+largeGrid[0]+"/"+largeGrid[1]+"\nlargeGrid[2][3]:"+largeGrid[2]+"/"+largeGrid[3]);
		/**
		 * 删除站点
		 */
//		GridPoiTable_One gridTable_one = new GridPoiTable_One();
//		gridTable_one.deleteLargePOIGrid(39.973,116.364);
		/**
		 * 重新根据GridPoiTable子站的表，更新生成主站的表
		 */
//		GridPoiTable_One gridTable_one = new GridPoiTable_One();
//		GridPoiTable gridTable = new GridPoiTable();
//		
//		POITable pTable = new POITable();
//		
//		Date timeDate  = new Date();
//		List<JSONObject> gridTable_Result = new ArrayList<JSONObject>();
//		gridTable_Result = gridTable.getData();
//		System.out.println(gridTable_Result.size());
//		System.out.println(gridTable_Result);
//		System.out.println("<-----start----->");
//		int n = 1;
//		
//		Map<String,Integer> mainStationMap =  new HashMap<String,Integer>();
//		for (JSONObject single_grid : gridTable_Result) {
//			
//			double lat = single_grid.getDouble("blat");
//			double lon = single_grid.getDouble("blon");
////			String pm = single_grid.getInt("fpm")+"/"+single_grid.getInt("actual_fpm");
//			int prePm = single_grid.getInt("fpm");
//			int actualPm = single_grid.getInt("actual_fpm");
//			String subId = single_grid.getString("poi");
//			String address = single_grid.getString("address");
//			String pic_url = single_grid.getString("pic");
////			long timstamp = single_grid.getLong("time");
//			
//			
//			List<String> list = new ArrayList<String>();
//			list.add(subId);
//			List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
//			jsonObjects = pTable.getAllPOIAllPic(list);
//			int subPoiPicNums = jsonObjects.get(0).getJSONArray("pic").size();
//			
//			System.out.println("\n该站照片:"+subPoiPicNums+"个");
//			String poiId = gridTable_one.regionIsExistLargePOI(lat,lon);
//			
//			if(poiId==null){
//				gridTable_one.setLargeGrid(lat, lon, poiId, timeDate,
//						prePm,actualPm, subId,pic_url);
////				mainStationMap.put(,subPoiPicNums);
//				System.out.println("SET新建大站点:"+n++);
//			}else{
//				if(mainStationMap.containsKey(poiId)){
//					if(mainStationMap.get(poiId)<=subPoiPicNums){
//						System.out.println("--hashMap更新:<"+subId+">"+mainStationMap.get(poiId)+"--->"+subPoiPicNums);
//						mainStationMap.put(poiId,subPoiPicNums);
//						gridTable_one.updateLargeGrid(lat, lon, prePm, actualPm,subId,pic_url);
//						System.out.println("UPDATE更新大站点:"+n++);
//					}else{
//						System.out.println("不更新大站点:"+n++);
//					}
//				}else{
//					gridTable_one.updateLargeGrid(lat, lon, prePm, actualPm,subId,pic_url);
//					mainStationMap.put(poiId,subPoiPicNums);
//					System.out.println("--hashMap注入:<"+subId+">"+subPoiPicNums);
//					System.out.println("UPDATE更新大站点:"+n++);
//				}
//			}
//		}
		//结束
//		
//		GridPoiTable_One gridTable111 = new GridPoiTable_One();
//		List<JSONObject> gridTable111_Result = new ArrayList<JSONObject>();
//		gridTable111_Result = gridTable111.getData();
//		System.out.println(gridTable111_Result);
		
		
		
//		System.out.println(gridTable111_Result);
		
		//找到此位置附近大站点距离为distance = 1
		GridPoiTable_One gridTable_one = new GridPoiTable_One();
		List<JSONObject> nearbyPoi = new ArrayList<JSONObject>();
		nearbyPoi = gridTable_one.findLargePoiNearbyGrid(40.01239399137, 116.468947595,5);
		System.out.println(nearbyPoi);
		
		
//		//再根据小格子算得大格子
//		double[] gridxysmall = GridPoiTable.computeGridxy(39.969781238497, 116.36505524901);
//		double[] gridxy = computeLargeGridxy(gridxysmall[2], gridxysmall[3]);
//		System.out.println(gridxysmall[2]+"/"+gridxysmall[3]+"\n"+gridxysmall[0]+"/"+gridxysmall[1]);
//		System.out.println(gridxy[0]+"/"+gridxy[1]+"\n"+gridxy[2]+"/"+gridxy[3]);		
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
