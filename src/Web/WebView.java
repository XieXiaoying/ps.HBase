package Web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.crypto.Data;

import kmeans.KmeansTable;
import kmeans.KmeansTest;
import kmeans.Point;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueExcludeFilter;
import org.apache.hadoop.hbase.thrift.generated.Hbase.isTableEnabled_args;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hdfs.server.namenode.FileChecksumServlets.GetServlet;
import org.apache.log4j.Logger;
import org.junit.experimental.theories.Theories;

import cData.Sitepixelxy;
import cData.XMLBuilder;
import Common.Column;

import Pic.MonitorSiteDataUnit;
import Pic.MonitorSiteTable;
import collection.CollectionPicTable;
import collection.Pic;
import collection.UploadPicInfo;

public class WebView{
	static Logger logger = Logger.getLogger(WebView.class);
	// 图片收集表
	private CollectionPicTable cpt = null;

	// POI计算
	private POIComputeInterface pci = null;

	// POI表
	private POITable pt = null;
	
	private KmeansTable kms = null;

	private MonitorSiteTable morTable = null;
	
	public WebView(POIComputeInterface pci) {
		this.cpt = new CollectionPicTable();
		this.pt = new POITable();
		this.kms = new KmeansTable();
		this.pci = pci;
		this.morTable = new MonitorSiteTable();
		
	}
	
	
	
	
/*	public void importPOIInfo(){
		
		File file = new File("resource/txt/newpoiinfo.txt");
        BufferedReader reader = null;
        try {
        	reader = new BufferedReader(new FileReader(file));
            String tempString = null;

            while ((tempString = reader.readLine()) != null) {
                String line[] = tempString.split("\t");
                String poi = line[0];
                double lat = Double.parseDouble(line[1]);
                double lon = Double.parseDouble(line[2]);
                int min_level = Integer.parseInt(line[3]);
                int max_level = Integer.parseInt(line[4]);
                setPOIInfo(poi, lon, lat, min_level, max_level);
                System.out.println("newpoiinfo import success~~");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
        */
        
        
        //注释掉，虚拟的poi中的pm值
        //
       /* File file2 = new File("resource/txt/newpoipm.txt");
        BufferedReader reader2 = null;
        try {
            reader2 = new BufferedReader(new FileReader(file2));
            String tempString = null;
            while ((tempString = reader2.readLine()) != null) {
                String line[] = tempString.split("\t");
                String poi = line[0];
                
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
                Date date = null;
                String dateString = line[1]; 
                try {
                  date = df.parse(dateString); 
                } 
                catch (Exception e) { 
                	e.printStackTrace();
                }          
                
                int fpm = Integer.parseInt(line[2]);
                
                setDayFpm(poi, date, fpm);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	} */

	public void importFromCollectionPicTable() throws IOException {
		// 设置扫描的列族
		List<byte[]> cfList = new ArrayList<byte[]>();
		cfList.add(CollectionPicTable.INFO_CF);
		cfList.add(CollectionPicTable.POI_CF);

		ResultScanner rs = cpt.scan(null, cfList, null, null);

		if (rs != null) {
			for (Result r : rs) {
				byte[] rowkey = r.getRow();
				UploadPicInfo uploadPicInfo = cpt
						.retrieveInfoFromRowKey(rowkey);
				if (uploadPicInfo == null) {
					System.out.println("UploadPicInfo is null for "
							+ Bytes.toString(rowkey));
					continue;
				}
				double lon = uploadPicInfo.getLon();
				double lat = uploadPicInfo.getLat();
				
				
				if (lon != -1 && lat != -1) {
					String poi = Bytes.toString(r.getValue(
							CollectionPicTable.POI_CF,
							CollectionPicTable.POI_COL));
					poi = poi.trim();

					// 图片值
					byte[] content = r.getValue(CollectionPicTable.INFO_CF,
							CollectionPicTable.PIC_COL);
					// PM2.5值
					byte[] fpm = r.getValue(CollectionPicTable.INFO_CF,
							CollectionPicTable.FPM_COL);
					if (content != null && fpm != null && poi != null) {
						Date timestamp = new Date(uploadPicInfo.getUploadtime());
						// 图片和PM2.5写入到POI表中
						JSONObject pic = new JSONObject();
						pic.accumulate("pic", Bytes.toString(content));
						pic.accumulate("fpm", Bytes.toInt(fpm));
						
						if(pt.setPoiFpm(poi, timestamp, Bytes.toInt(fpm))){
							System.out.println("set poiFpm is success!!");
							
						}
						if (!setPic(poi, timestamp,Bytes.toBytes(pic.toString()))) {
							System.out.println("some error happen for poi: "
									+ poi);
						}
						
						

					}

				}
			}

			rs.close();
		}
	}
	
	/*
	*//**
	 * 计算并写入图片收集表中所有数据的POI结果
	 * 并将POI,图片和图片PM2.5值导入到POI表中
	 * @throws IOException 
	 *//*
	public void computePOI() throws IOException{
		Date before = new Date();
		// 设置扫描的列族
		List<byte[]> cfList = new ArrayList<byte[]>();
		cfList.add(CollectionPicTable.INFO_CF);
		cfList.add(CollectionPicTable.POI_CF);
		// poi值为空
		SingleColumnValueExcludeFilter f = new SingleColumnValueExcludeFilter(
				CollectionPicTable.POI_CF, CollectionPicTable.POI_COL,
				CompareFilter.CompareOp.EQUAL, (byte[])null);
		
		ResultScanner rs = cpt.scan(null,cfList, null, new FilterList(f));
		
		int rowNum = 0;
		if (rs != null) {
			for (Result r : rs) {
				rowNum++;
				byte[] rowkey = r.getRow();
				UploadPicInfo uploadPicInfo = cpt.retrieveInfoFromRowKey(rowkey);
				if(uploadPicInfo == null){
					continue;
				}
				double lon = uploadPicInfo.getLon();
				double lat = uploadPicInfo.getLat();
				if (lon != -1 && lat != -1) {
					// 计算POI,在袁龙运的webView里的有它的实现方法
					String poi = pci.computePOI(lon, lat);
					
					if(poi != null){
						List<KeyValue> kvs = new ArrayList<KeyValue>();
						kvs.add(new KeyValue(rowkey, CollectionPicTable.POI_CF,
								CollectionPicTable.POI_COL, Bytes.toBytes(poi)));
						// 写入pic表
						cpt.put(rowkey, null, kvs);

						// 写入POI表
						// 图片值
						byte[] content = r.getValue(CollectionPicTable.INFO_CF,
								CollectionPicTable.PIC_COL);
						// PM2.5值
						byte[] fpm = r.getValue(CollectionPicTable.INFO_CF,
								CollectionPicTable.FPM_COL);
						if (content != null && fpm != null && poi != null) {
							Date timestamp = new Date(uploadPicInfo.getUploadtime());
							// 精确到天
							timestamp = new Date(timestamp.getYear(),
									timestamp.getMonth(), timestamp.getDate(), 0, 0, 0);
						
							// 图片和PM2.5写入到POI表中
							JSONObject pic = new JSONObject();
							pic.accumulate("pic", Bytes.toString(content));
							pic.accumulate("fpm", Bytes.toInt(fpm));
							
							setPic(poi, timestamp, Bytes.toBytes(pic.toString()));
						}
						
					}
					

				}
			}

			rs.close();
		}

		Date after = new Date();
		
		logger.info("computePOI("+rowNum+" rows) in " + (after.getTime() - before.getTime()) + "ms");
		
		
	}*/

	/**
	 * 
	 * 计算某经纬度对应的poi_id
	 * */
	public void computerPOI(double lon, double lat){
		pci.computePOI(lon, lat);
	}
	
	
	
	/**
	 * 扫描图片收集表获得所有POI值，没有则返回null
	 * 
	 * @return
	 */
	public List<String> getAllPOI() {
		Date before = new Date();
		List<String> result =  pt.getAllPOIId();
		Date after = new Date();
		
		logger.info("getAllPOI in " + (after.getTime() - before.getTime()) + "ms");
		return result;
		
	}
	
	/**
	 * @author ytt
	 * 从POITable中所有点来聚合
	 * */
	public void kmeansAll(){
		
		List<String> poiList = pt.getAllPOIId();
		for(String poi : poiList){
			//System.out.println(poi + "%%%%%%%%%%%%%%%%5");
			pt.kmeansByPoiTable(poi);
		}
	}

	/**
	 * 从图片收集表中获得poi里的所有Map<日期，当天PM2.5值列表> 其中日期精确到天数 没有则返回null
	 * 
	 * 
	 * @param poi
	 * @return
	 */
	public Map<Date, List<Integer>> getPicFpmInPOI(String poi) {
		Date before = new Date();
		// 构造扫描所有具有POI的数据的扫描器
		SingleColumnValueExcludeFilter f = new SingleColumnValueExcludeFilter(
				CollectionPicTable.POI_CF, CollectionPicTable.POI_COL,
				CompareFilter.CompareOp.EQUAL, Bytes.toBytes(poi));
		f.setFilterIfMissing(true);
		// 设置扫描的列族
		List<byte[]> cfList = new ArrayList<byte[]>();
		cfList.add(CollectionPicTable.POI_CF);
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(CollectionPicTable.INFO_CF,CollectionPicTable.FPM_COL));
		// 扫描结果
		ResultScanner rs = cpt.scan(null, cfList, columnList, new FilterList(f));
		if (rs != null) {
			Map<Date, List<Integer>> results = new HashMap<Date, List<Integer>>();
			for (Result r : rs) {
				//PM2.5值
				byte[] fpm = r.getValue(CollectionPicTable.INFO_CF,
						CollectionPicTable.FPM_COL);
				if(fpm != null){
					UploadPicInfo uploadPicInfo = cpt.retrieveInfoFromRowKey(r.getRow());
					if(uploadPicInfo == null){
						continue;
					}
					Date timestamp = new Date(uploadPicInfo.getUploadtime());
					// 精确到天
					timestamp = new Date(timestamp.getYear(),
							timestamp.getMonth(), timestamp.getDate(), 0, 0, 0);					
					
					// 添加日期的fpm值
					List<Integer> fpms;
					if ((fpms = results.get(timestamp)) != null) {
						fpms.add(Bytes.toInt(fpm));
						results.put(timestamp, fpms);
					} else {
						fpms = new ArrayList<Integer>();
						fpms.add(Bytes.toInt(fpm));
						results.put(timestamp, fpms);
					}

				}
			}// end for
			rs.close();
			
			if(results.size() == 0){
				return null;
			}
			Date after = new Date();
			logger.info("getPicFpmInPOI in " + (after.getTime() - before.getTime()) + "ms");
			return results;
		}
		
		return null;

	}

	/**
	 * 设置某poi某天的PM2.5值
	 * @author ytt
	 * @param poi
	 * @param timestamp
	 * @param fpm
	 *            PM2.5值
	 * @return
	 * @throws IOException 
	 */
	public Boolean setPoiFpm(String poi, Date timestamp, int fpm) throws IOException {
		return pt.setPoiFpm(poi, timestamp, fpm);
	}

	/**
	 * 获得某POI某日期（精确到天）的fpm值,没有则返回-1
	 * @author ytt
	 * @param poi
	 * @param timestamp
	 * @return
	 * @throws IOException 
	 */
	public List<JSONObject> getPoiFpm(String poi, Date starttime,Date stoptime, Boolean gradeSet) throws IOException {
		Date before = new Date();
		List<JSONObject> jsonObjects  = null;
		jsonObjects =  pt.getPoiFpm(poi, starttime, stoptime, gradeSet);
		Date after = new Date();
		logger.info("getDayFpm in " + (after.getTime() - before.getTime()) + "ms");
		
		System.out.println(jsonObjects);
		return jsonObjects;
	}
	
	

	/**
	 * 添加某POI某日期（精确到天）的图片
	 * 
	 * @param poi
	 * @param timestamp
	 * @param content
	 * @return
	 * @throws IOException 
	 */
	public Boolean setPic(String poi, Date timestamp, byte[] content) throws IOException {
		return pt.setPic(poi, timestamp, content);
	}
	
    public long getPoiPicCount(String poi,Date timestamp ){
    	return pt.getPoiPicCount(poi, timestamp);
    }
	
	
	/**
	 * 获得符合poiPrefix的所有POI点某天的所有图片，没有则返回null
	 * 若timestamp==null则不需要限定某天
	 * @param poi
	 * @return
	 * @throws IOException 
	 */
	public List<JSONObject> getPic(String poiPrefix, Date timestamp) throws IOException {
		Date before = new Date();
		List<JSONObject> result =  pt.getPic(poiPrefix, timestamp, null, true);
		Date after = new Date();
		logger.info("getPic in " + (after.getTime() - before.getTime()) + "ms");
		
		return result;
	}

	
	/**
	 * 获得所有POI点的信息，没有则返回null
	 * 
	 * @param level
	 * @return
	 */
	 /*
	public Set<JSONObject> getAllPOIsInfo() {
		Date before = new Date();
		Set<JSONObject> result =  pt.getAllPOIsInfo();
		Date after = new Date();
		logger.info("getPOIsInfo in " + (after.getTime() - before.getTime()) + "ms");
		
		return result;
	}
	*/
	
	/**
	 * @author ytt
	 * 得到zoom缩放级别下的质心的信息
	 * 
	 * 
	 * */
	public List<JSONObject> getAllCenters(int zoom){
		return kms.getCenters(zoom);
	}
	
	/**
	 * @author ytt
	 * 得到某缩放级别下所有的离质心最近的点
	 * 
	 * */
	public List<JSONObject> getAllNears(int zoom){
		return kms.getNears(zoom);
	}
	
	public String getNearpoiInCenterZoom(int zoom, Point center){
		return kms.getNearpoiInCenterZoom(zoom, center);
	}
	
	/**
	 * @author ytt
	 * 返回的是某center内包含的所有的poi点的列表
	 * 
	 * */
	public List<String> getPOIsInCenterZoom(int zoom, Point center){
		return kms.getPoisInCenterZoom(zoom, center);
	}
	
	/**
	 * @author ytt
	 * 返回的是某缩放级别下某边界之内的centers
	 * */
	public List<JSONObject> getCenterInbounce(int zoom, double minX, double minY, double maxX, double maxY){
		return kms.getCenters(zoom, minX, minY, maxX, maxY);
		
		
		
	}
	
	/**
	 * @author ytt
	 * 得到某pois的所有的具体的信息
	 * 
	 * */
	public List<JSONObject> getAllPOIOnePic(List<String> pois){
		return pt.getAllPOIOnePic(pois);
	}
	
	/**
	 * @author ytt 
	 * 获取一个poi的所有的信息
	 * 
	 * */
	public JSONObject getOnePOIAllPic(String poi){
		return pt.getOnePOIAllPic(poi);
	}
	
	
	/**
	 * @author ytt
	 * 2015/7/5
	 * 得到某一个缩放级别下的某一个center的某一时间段的pm值
	 * @param int zoom 缩放等级
	 * @param Point center
	 * @param Date {@link Timestamp}
	 * @param boolean grade  是ture 或者 false ，ture表示是天，false表示是按小时
	 * @return JSONObject{address:XXXX, fpm:XX, date:XXX}
	 * 
	 * */
	public JSONObject getCenterPmInZoom(int zoom, Point center, Date timestamp,boolean grade) {
		
		//地址有待优化,暂时用neareat的地址代替
		String nearpoiString = kms.getNearpoiInCenterZoom(zoom, center);
		JSONObject nearJsonObject = pt.getOnePOIAllPic(nearpoiString);
		String addressString = nearJsonObject.getString("address");
		JSONObject centerpmJsonObject = new JSONObject();
		double priosum = 0; 
		double monitorPmsum = 0;
		double pmstationPmsum = 0;
		double centerPm =0; 
	    DecimalFormat  dFormat = new DecimalFormat("0.00");
		
		List<JSONObject> poisInfos = kms.getPoisnfoForZoomCenter(zoom, center);

		for(int i = 0; i < poisInfos.size(); i++){
			int poifpm = -1 ;
			List<JSONObject> poifpmJsonObjects= pt.getPoiFpm(poisInfos.get(i).getString("poiId"), timestamp,null,grade);
			if(poifpmJsonObjects.size() != 0){
				poifpm = poifpmJsonObjects.get(0).getInt("poi_pm_value");
			}
			if(poifpm != -1){
				//如果质心只有一个点，即只有一个站点
				if(poisInfos.size() == 1){
					
					centerPm = poifpm;
					centerpmJsonObject.accumulate("address", addressString);
					centerpmJsonObject.accumulate("fpm", centerPm);
					centerpmJsonObject.accumulate("date", timestamp);
					return centerpmJsonObject;
				}
				
				
				double poiprio = 1/(KmeansTest.getdoubleDistans(center.getPixelX(), center.getPixelY(), 
						poisInfos.get(i).getDouble("pixelX"), poisInfos.get(i).getDouble("pixelY")));
				//System.out.println("pmstationpm is " + poifpm);
				pmstationPmsum += poifpm*poiprio;
				priosum += poiprio;
			}
			//System.out.println("poifpm is null");
		}
		//首先需要判断nearest点属于哪个省市区域，选择该城市的监测点的xml文件，此处暂时预定为beijing.xml
		List<Double> prios = new ArrayList<Double>();
		List<Sitepixelxy> sitepixelxies = XMLBuilder.selectPixel("/home/ps/ytt/yttps/resource/cData/beijing.xml", zoom);
		for(Sitepixelxy sitepixelxy : sitepixelxies){
			double prio = 1/(KmeansTest.getdoubleDistans(center.getPixelX(), center.getPixelY(), sitepixelxy.getPixelx(), sitepixelxy.getPixely()));
			prios.add(prio);
		}
		for(int i = 1; i < prios.size()+1; i++){
			MonitorSiteDataUnit mornitorInfo = morTable.getNearst("beijing", i, Bytes.toBytes("fpm"), timestamp);
			if(mornitorInfo!=null){
				//System.out.println("monitorInfo is " + mornitorInfo.getFpm());
				monitorPmsum += mornitorInfo.getFpm() * prios.get(i-1);
				priosum += prios.get(i-1);
			}
			else {
				//System.out.println("monitorinfo is null");
			}
		}

		//System.out.println("the priosum is "+priosum);
		//System.out.println("pmstationpmsum is "+ pmstationPmsum);
		if(priosum == 0.0){
			//如果监测站点也没有pm值的话，先设置centerpm为-1
			centerPm = -1;
			
		}else {
			centerPm =Double.valueOf(dFormat.format((monitorPmsum + pmstationPmsum) / priosum));
		}
		
		//System.out.println("the center's fpm is " + centerPm);
		
		
		centerpmJsonObject.accumulate("address",addressString);
		centerpmJsonObject.accumulate("fpm", centerPm);
		centerpmJsonObject.accumulate("date", timestamp.getTime());
		return centerpmJsonObject;
	}
	
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		
		POIComputeInterface POIi = null;
		WebView wv = new WebView(POIi);
		//System.out.println("7878");
		
		//导入图片
		//wv.importPOIInfo();
		//wv.importFromCollectionPicTable();
		
	
		//测试单个poi的图片
		/*List<JSONObject> jsonObjects = wv.getPic("10010521",null);
		System.out.println(jsonObjects);
		*/
		
		//测试图片的张数
		
		/*long count = wv.pt.getPoiPicCount("1402356187", null);
		System.out.println(count);*/
		
		
		//测试读取照片的pm值以及照片
	/*Date stoptime = null;	
	Date starttime = null;
	String timestopString = "2015-1-7";
	String timeString  = "2015-1-1";
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	starttime = sdf.parse(timeString);
	stoptime = sdf.parse(timestopString);
	
	
	//System.out.println("starttime" + starttimestamp.getTime() + " \n" + "stoptime " + stoptime.getTime());
	System.out.println(wv.getPoiFpm("10010521", starttime, stoptime, true));*/
	
		
/*
		List<JSONObject> jsonObjects = wv.getPic("10010521", null);
		System.out.println(jsonObjects);
		Set<JSONObject> POIImgInfoJSON = new HashSet<JSONObject>();
		POIImgInfoJSON.addAll(POIImgInfoJSON);
		System.out.println(POIImgInfoJSON);*/
	
		
		//测试getAllPOIsInfo()
		/*Set<JSONObject> allPOIInfo = wv.getPOIsInfo();
		Iterator<JSONObject> joIterator = allPOIInfo.iterator();
		while(joIterator.hasNext()){
			JSONObject jo = joIterator.next();
			System.out.println(jo);
		}
		
		*/
		
		
		
		// 测试getPOIsAllInfo() yes!!!!
		/*List<String> poiStrings = new ArrayList<String>();
		poiStrings.add("904738673");
		List<JSONObject> poiinfo= wv.pt.getAllPOIAllPic(poiStrings);
		for(JSONObject jsonObject : poiinfo){
			System.out.println(jsonObject.toString());
		}
		*/
		/*ytt
		 * 测试servlet中的getallpoi方法
		 * 
		 * */
/*		List<JSONObject> cluster = new ArrayList<JSONObject>();
		List<JSONObject> viewcenterJsonObjects = wv.getCenterInbounce(12, 202115,75270, 202827, 75539);
		for(JSONObject centerJsonObject : viewcenterJsonObjects){
			JSONObject jsonObject = new JSONObject();
			Point center = new Point(centerJsonObject.getDouble("centerx"), centerJsonObject.getDouble("centery"));
			
			String nearpoiString = wv.getNearpoiInCenterZoom(12, center);
			System.out.println(nearpoiString);
			JSONObject nearpoiJsonObject = wv.getOnePOIAllPic(nearpoiString);
			
			List<String> poiStrings = wv.getPOIsInCenterZoom(12, center);
			List<JSONObject> poisInfoJsonObjects = wv.getAllPOIOnePic(poiStrings);
			
			jsonObject.accumulate("center", centerJsonObject);
			jsonObject.accumulate("near", nearpoiJsonObject);
			jsonObject.accumulate("pois", poisInfoJsonObjects);
			System.out.println(jsonObject.toString());
			cluster.add(jsonObject);
		}
		
		*/
		
		/*JSONObject pic = wv.getOnePOIAllPic("907609274");
		System.out.println(pic.toString());
		*/
	KmeansTable kmeansTable = new KmeansTable();
		kmeansTable.deleteTable("kmeans");
		wv.kmeansAll();
//		
/*		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		List<JSONObject> centers = wv.getCenterInbounce(12,202115, 75415, 202827, 75621);

		List<List<JSONObject>> centerspmLists = new ArrayList<List<JSONObject>>();
		for(JSONObject jsonObject : centers){
			
			System.out.println(jsonObject.toString());
			Point centerPoint = new Point(jsonObject.getDouble("centerx"), jsonObject.getDouble("centery"));
			Date timestamp = sdf.parse("2015-06-01  19:05:05");
			System.out.println(sdf.format(timestamp));
			int year = 1900+timestamp.getYear();
			int month = timestamp.getMonth()+1;
			int day = timestamp.getDate();
			int hours = timestamp.getHours();
			int minus = timestamp.getMinutes();
			int seconds = timestamp.getSeconds();
			System.out.println(year + "-" + month+"-" +(day+1)+" " + hours+":"+minus+":"+seconds);
			List<JSONObject> centerpmsList = new ArrayList<JSONObject>();
			for(int  i = 0; i < 7; i++){
				//默认是从开始时间向后数七天的pm值
				timestamp = sdf.parse(year + "-" + month+"-" +(day+i)+" " + hours+":"+minus+":"+seconds);
				System.out.println(sdf.format(timestamp));
				jsonObject = wv.getCenterPmInZoom(12, centerPoint, timestamp); 
				centerpmsList.add(jsonObject);
			}
			centerspmLists.add(centerpmsList);
			JSONObject centerfpm = wv.getCenterPmInZoom(12, centerPoint, timestamp);
			System.out.println(centerfpm.toString());
		}
		*/
		//测试center的pm值部分
/*		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			
			List<List<JSONObject>> centerspmLists = new ArrayList<List<JSONObject>>();
		
			List<JSONObject> viewcenterJsonObjects = wv.getCenterInbounce(12,202115, 75415, 202827, 75621);
			Date timestamp = sDateFormat.parse("2015-05-01 19:05:05");
			timestamp = new Date(timestamp.getYear(), timestamp.getMonth(), timestamp.getDate(), 0, 0, 0);
			System.out.println(sDateFormat.format(timestamp)+ "   ******************");
			int year = 1900 + timestamp.getYear();
			int month = timestamp.getMonth() +1;
			int day = timestamp.getDate();
			int hours = timestamp.getHours();
			int minus = timestamp.getMinutes();
			int seconds = timestamp.getSeconds();
			for(JSONObject centerJsonObject : viewcenterJsonObjects){
				List<JSONObject> centerpmsList = new ArrayList<JSONObject>();
				JSONObject jsonObject = new JSONObject();
				Point center = new Point(centerJsonObject.getDouble("centerx"), centerJsonObject.getDouble("centery"));
				for(int  i = 0; i < 7; i++){
					//默认是从开始时间向后数七天的pm值
					timestamp = sDateFormat.parse(year + "-" + month+"-" +(day+i)+" " + hours+":"+minus+":"+seconds);
					System.out.println(sDateFormat.format(timestamp));
					jsonObject = wv.getCenterPmInZoom(12, center, timestamp); 
					centerpmsList.add(jsonObject);
					System.out.println(jsonObject.toString());
				}
				centerspmLists.add(centerpmsList);
			}
		}catch(Exception e){
			e.printStackTrace();
		}*/
		
		
		
//		List<JSONObject> cluster = new ArrayList<JSONObject>();
//		List<JSONObject> viewcenterJsonObjects = wv.getCenterInbounce(12, 202115, 75263, 202827, 75546);
//		for(JSONObject centerJsonObject : viewcenterJsonObjects){
//			JSONObject jsonObject = new JSONObject();
//			Point center = new Point(centerJsonObject.getDouble("centerx"), centerJsonObject.getDouble("centery"));
//			
//			String nearpoiString = wv.getNearpoiInCenterZoom(12, center);
//			JSONObject nearpoiJsonObject = wv.getOnePOIAllPic(nearpoiString);
//			
//			List<String> poiStrings = wv.getPOIsInCenterZoom(12, center);
//			List<JSONObject> poisInfoJsonObjects = wv.getAllPOIOnePic(poiStrings);
//			
//			jsonObject.accumulate("center", centerJsonObject);
//			jsonObject.accumulate("near", nearpoiJsonObject);
//			jsonObject.accumulate("pois", poisInfoJsonObjects);
//			System.out.println(jsonObject);
//			cluster.add(jsonObject);
//		}
//		System.out.println("cluster:_______"+cluster.toString());
		
		
		// 这个没问题 显示七天的pm
/*		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			System.out.println("fihiodhgih");
			List<List<JSONObject>> centerspmLists = new ArrayList<List<JSONObject>>();
				
			Date starttime = new Date(2015-1900, 06, 04);
			Date stoptime = new Date(2015-1900, 06, 16);
			List<JSONObject> viewcenterJsonObjects = wv.getCenterInbounce(12,202115, 75415, 202827, 75621);
			//将starttime和stoptime改成时间的格式
		Calendar calendar = Calendar.getInstance();
			
			int year = 0;
			int month = 0;
			int day = 0;

				Date timestamp = new Date();
				int i = 0;
				
				for(JSONObject centerJsonObject : viewcenterJsonObjects){
					calendar.setTime(starttime);
					List<JSONObject> centerpmsList = new ArrayList<JSONObject>();
					JSONObject jsonObject = new JSONObject();
					Point center = new Point(centerJsonObject.getDouble("centerx"), centerJsonObject.getDouble("centery"));
					
				
					while (calendar.get(Calendar.DAY_OF_MONTH) <= stoptime.getDate()) {
						
						System.out.println("while is start!");
						
						calendar.add(Calendar.DAY_OF_MONTH, 1);
						year = calendar.get(Calendar.YEAR);
						month = calendar.get(Calendar.MONTH)+1;
						day = calendar.get(Calendar.DAY_OF_MONTH);
						System.out.println("this is the year and month and day  " + year + "  " + month + " " + day);
						timestamp = sDateFormat2.parse(year + "-" + month+"-" +day+" 00:00:00");
						System.out.println(sDateFormat2.format(timestamp)+" this is the time  ``````````````````````````");
						jsonObject = wv.getCenterPmInZoom(12, center, timestamp,true); 
						centerpmsList.add(jsonObject);
						System.out.println(jsonObject.toString());
						
					}
					System.out.println(i +   "  is  ");
					i++;
					centerspmLists.add(centerpmsList);
					
				}
			}catch(Exception e){
					e.printStackTrace();
			}*/
			
/*			Date stoptime = new Date();
			List<JSONObject> viewcenterJsonObjects = wv.getCenterInbounce(12,202115, 75415, 202827, 75621);
			Calendar calendar = Calendar.getInstance();
			Calendar calendar2 = Calendar.getInstance();
			calendar.setTime(stoptime);
			int year = 0;
			int month = 0;
			int day = 0;
			int hour =0;
				Date timestamp = new Date();
		
				for(JSONObject centerJsonObject : viewcenterJsonObjects){
					List<JSONObject> centerpmsList = new ArrayList<JSONObject>();
					JSONObject jsonObject = new JSONObject();
					Point center = new Point(centerJsonObject.getDouble("centerx"), centerJsonObject.getDouble("centery"));
					Date starttime = sDateFormat2.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" "+"00:00:00");
				
					System.out.println(sDateFormat2.format(starttime));
					System.out.println(sDateFormat2.format(stoptime)+"this is  stoptime");
					calendar.setTime(starttime);
					while(calendar.get(Calendar.HOUR_OF_DAY) < stoptime.getHours()){
						System.out.println("this is the while ");
						calendar.add(Calendar.HOUR_OF_DAY, 1);
						year = calendar.get(Calendar.YEAR);
						System.out.println("year is " + year);
						month = calendar.get(Calendar.MONTH)+1;
						day = calendar.get(Calendar.DAY_OF_MONTH);
						hour = calendar.get(Calendar.HOUR_OF_DAY);
						timestamp = sDateFormat2.parse(year + "-" + month+"-" +day+" "+hour+":00:00");
						System.out.println(sDateFormat2.format(timestamp));
						
						jsonObject = wv.getCenterPmInZoom(12, center, timestamp,false); 
						centerpmsList.add(jsonObject);
						System.out.println(jsonObject);
					}
				}*/
			
			
		
		}
}

