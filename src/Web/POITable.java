package Web;

import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import kmeans.KmeansTable;
import kmeans.KmeansTest;
import kmeans.Point;
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
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import Common.AbstractTable;
import Common.Column;
import collection.CollectionPicTable;
import collection.Constants;

public class POITable extends AbstractTable {
	static Logger logger = Logger.getLogger(POITable.class);
	
	/*
	 * 改动---poi表的行健变成：poi/timestamp
	 * @author ytt  
	 * 
	 * */
	// 表名
	private static final String tableName = "poi222";
	// 图片列族名
	public static final byte[] DAYDATA_CF = "dayData".getBytes();
	// 图片列名,加上图片的fpm值
	public static final byte[] PIC_COL = "pic".getBytes();
	
	public static final byte[] COUNT_COL = "count".getBytes();

	// PM2.5列名
	public static final byte[] FPM_COL = "poi_fpm".getBytes();
	//model类型
	public static final byte[] MODEL_INFO_COL = "model_info".getBytes();
	
	
	// info列族名
	public static final byte[] INFO_CF = "info".getBytes();
	// POI信息列名
	public static final byte[] POI_INFO_COL = "poi_info".getBytes();
	

	// 图片数目
	/*public static int pic_num = 0;

	synchronized public int getPicNum() {
		return pic_num;
	}
	
*/
	
	public POITable() {
		
		try {
			HBaseAdmin hAdmin = new HBaseAdmin(Constants.conf);
			if (hAdmin.tableExists(tableName)) {
			      // do nothing	
			}
			else{
				// 设置版本保存策略
				HColumnDescriptor des = new HColumnDescriptor(DAYDATA_CF);
				des.setMaxVersions(Integer.MAX_VALUE);
				HColumnDescriptor des1 = new HColumnDescriptor(INFO_CF);
				HTableDescriptor t = new HTableDescriptor(tableName);
				t.addFamily(des);
				t.addFamily(des1);
				hAdmin.createTable(t);
			}
			hAdmin.close();
			
			hTable = new HTable(Constants.conf, tableName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
/*
 * leeying
 * 获得下一个存放图片的列名 因为图片列名的数目不能事先确定，所以需要生成下一个图片名
	
	synchronized private byte[] nextPIC_COL(String poi, Date timestamp) {
		String colName = new String(PIC_COL) + ++pic_num;
		return colName.getBytes();
	}
*/
	/***
	 * 将图片的列名设置为时间戳，按照时间戳降序排列，使得图片可以总是得到最新的图片
	 * @time 2015-04-08
	 * @author ytt 
	 * @param poi
	 * @param timestamp
	 * 
	 * @return colName
	 * *//*
	synchronized private byte[] nextPIC_COL(String poi, Date timestamp) {
		Long time = timestamp.getTime();
		String colName = Long.toString(Long.MAX_VALUE-time);
		//pic_num++;
		return Bytes.toBytes(colName);
	}
	
	*//***
	 * 将图片的列名设置为时间戳，按照时间戳降序排列，使得图片可以总是得到最新的图片
	 * @time 2015-04-08
	 * @author ytt 
	 * @param poi
	 * @param timestamp
	 * 
	 * @return colName
	 * *//*
	synchronized private byte[] nextFpm_COL(String poi, Date timestamp) {
		Long time = timestamp.getTime();
		String colName = time.toString();
		//pic_num++;
		return Bytes.toBytes(colName);
	}
	*/
	/**
	 * 生成行键
	 * 图片的行健
	 * @param poi
	 * @return
	 */
	public byte[] generateRowKey(String poi,Date timestamp) {
		String rowKey = poi + Constants.SEPARATER + (Long.MAX_VALUE-timestamp.getTime());
		
		return rowKey.getBytes();
	}

	/**
	 * 生成行键
	 * poi信息的行健
	 * @param poi
	 * @return
	 */
	public byte[] generateRowKey(String poi,Long timestamp) {
		String rowKey = poi + Constants.SEPARATER + timestamp;
		
		return rowKey.getBytes();
	}
	public byte[] generateRowKeyPoi(String poi) {
		String rowKey = poi + Constants.SEPARATER;
		
		return rowKey.getBytes();
	}
	/**
	 * 分解行健
	 * 
	 * */
	public String UploadPOIRowkey(byte[] rowkey){
		String[] rowKeys = Bytes.toString(rowkey).split(Constants.SEPARATER);
		return rowKeys[0];
	}
	
	
	/**
	 * 添加某POI某日期（精确到天）的图片
	 * 2015-04-09
	 * @author ytt
	 * @param poi
	 * @param timestamp
	 * @param fpm
	 * @return
	 * @throws IOException 
	 */
	public Boolean setPoiFpm(String poi, Date timestamp, int fpm) throws IOException {
		// 只要精确到天
		/*timestamp = new Date(timestamp.getYear(), timestamp.getMonth(),
				timestamp.getDate(), 0, 0, 0);*/
		// 存入图片的列名
		//byte[] col = nextFpm_COL(poi, timestamp);
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		kvs.add(new KeyValue(generateRowKey(poi,timestamp), DAYDATA_CF, FPM_COL, timestamp
				.getTime(), Bytes.toBytes(fpm)));
		
		 System.out.println("写入POI = " + poi + "列=" + new String(FPM_COL) +
		 " Time = " + timestamp.getTime() + "fpm= " + fpm);
		
		
		
		return this.put(generateRowKey(poi,timestamp), timestamp, kvs);

	}
	
	/**
	 * @author ytt
	 * 2015-04-10
	 * @param poi
	 * @param starttime 
	 * @param stoptime 是终止时间，
	 * @param gradeSet 当为true时，是指天为单位，当为false时，是以小时为单位
	 * @return List<JSONObject> 每一个JSONObject是{pic：XXX,fpm:XXX,poi:XXX,time:XXX}
	 * 
	 * 当starttime ！= null && stoptime == null时，指仅获取starttime那一天的值
	 * 当starttime != null && stoptime != null时，指获取获得某poi点的starttime-stoptime之间的值
	 * 当starttime == null && stoptime != null时，指获取获得某poi点的最早的图片-stoptime之间的值
	 * 当starttime == null && stoptime == null时，指获取获得某poi点的所有的值
	 * 
	 * 
	 * */
	public List<JSONObject> getPoiFpm(String poi,Date starttime, Date stoptime, boolean gradeSet) {
		List<JSONObject> joList= new ArrayList<JSONObject>();
		Date starttimestamp = null;
		Date stoptimestamp = null;
		Scan scan = new Scan();
		if(starttime != null){
			if(gradeSet){ /////默认为空时,此时按天算
				starttimestamp = new Date(starttime.getYear(), starttime.getMonth(), 
						starttime.getDate(), 0, 0);
				if(stoptime == null){
					
					stoptimestamp =new Date(starttime.getYear(), starttime.getMonth(), 
							starttime.getDate()+1, 0, 0);
				}else{
					stoptimestamp = new Date(stoptime.getYear(), stoptime.getMonth(), 
							stoptime.getDate(), 0, 0);
				}
				
				//System.out.println(starttimestamp.getTime() + "is  starttimestap!!!!");
				//System.out.println(stoptimestamp.getTime() + "is  stoptimestap!!!!");
				
				
			}
			else {
				if(stoptime == null){
					stoptimestamp = new Date(starttime.getYear(), starttime.getMonth(), 
							starttime.getDate(), starttime.getHours()+1, 0);
				}
				else {
					stoptimestamp = new Date(stoptime.getYear(), stoptime.getMonth(), 
							stoptime.getDate(), starttime.getHours(), 0);
				}
				starttimestamp = new Date(starttime.getYear(), starttime.getMonth(), 
						starttime.getDate(), starttime.getHours(), 0);
				
				
			}
			
			//System.out.println(starttimestamp + "ojoagohgighh  starttime");
			//System.out.println(stoptimestamp + "ojoagohgighh  stoptime");
			scan.setStopRow(generateRowKey(poi, starttimestamp));
			scan.setStartRow(generateRowKey(poi, stoptimestamp));
		}
		else if(starttime == null && stoptime == null){
			//当都为空时，表示获取poi的所有的pm值

			Filter filter = new PrefixFilter(Bytes.toBytes(poi));
			scan.setFilter(filter);
		}
		else if(starttime == null && stoptime != null){
			scan.setStartRow(generateRowKey(poi, stoptimestamp));
			scan.setStopRow(generateRowKey(poi, Long.MAX_VALUE));
		}
		
		
		
		List<byte[]> fList = new ArrayList<byte[]>();
		fList.add(DAYDATA_CF);
		
		List<Column> cList = new ArrayList<Column>();
		cList.add(new Column(DAYDATA_CF, FPM_COL));
		
		ResultScanner resultScanner = this.scan(scan, fList, cList, null);
		if(resultScanner != null){
			//System.out.println("into scanner????");
			for(Result rs : resultScanner){
				
				for(KeyValue kv : rs.list()){
					JSONObject jsonObject = new JSONObject();
					String[] rowKey = Bytes.toString(kv.getRow()).split(Constants.SEPARATER);
					Date timestamp = new Date(Long.MAX_VALUE-Long.parseLong(rowKey[1]));
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time = sdf.format(timestamp);
					System.out.println(time);
					int fpm = Bytes.toInt(kv.getValue());
					
					jsonObject.accumulate("poi_pm_id", poi);
					jsonObject.accumulate("poi_pm_date", time);
					jsonObject.accumulate("poi_pm_value", fpm);
					
					joList.add(jsonObject);
					
				}
				
			}
		}
		
		if(resultScanner == null){
			//System.out.println("scanner is null  11111");
			return null;
		}
		try {
			resultScanner.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			resultScanner.close();
		}
		return joList;
		
	}
	
	
	
	/**
	 * 设置某POI某日期（精确到天）的fpm值
	 * 
	 * @param poi
	 * @param timestamp
	 * @param fpm
	 * @return
	 */
	/*public Boolean setDayFpm(String poi, Date timestamp, int fpm) {
		// 只要精确到天
		timestamp = new Date(timestamp.getYear(), timestamp.getMonth(),
				timestamp.getDate(), 0, 0, 0);

		// System.out.println("保存poi = "+poi+" Timestamp = " +
		// timestamp.getTime());

		List<KeyValue> kvs = new ArrayList<KeyValue>();
		kvs.add(new KeyValue(generateRowKey(poi), DAYDATA_CF, FPM_COL, timestamp
				.getTime(), Bytes.toBytes(fpm)));
		return this.put(generateRowKey(poi), timestamp, kvs);
	}

	*/
	/**
	 * 获得某POI某日期（精确到天）的fpm值,没有则返回-1
	 * 
	 * @param poi
	 * @param timestamp
	 * @return
	 */
/*	public int getDayFpm(String poi, Date timestamp) {
		// 只要精确到天
		timestamp = new Date(timestamp.getYear(), timestamp.getMonth(),
				timestamp.getDate(), 0, 0, 0);

		// System.out.println("查询poi = "+poi+" Timestamp = " +
		// timestamp.getTime());

		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(DAYDATA_CF, FPM_COL));
		
		Result r = this.get(generateRowKey(poi,timestamp), timestamp, null, columnList,false);
		if (r != null) {
			byte[] val = null;
			if (!r.isEmpty()
					&& (val = r.getValue(DAYDATA_CF, FPM_COL)) != null) {
				return Bytes.toInt(val);
			}
		}

		return -1;
	}
	*/
	/**
	 * @author ytt
	 * 获取某poi的最新版本的pm的值
	 * @param poi
	 * @return 
	 * @vertionNum 获取哪几个版本
	 * @throws IOException 
	 * @time 2015-3-22
	 * 
	 * */
	
	/*public Result getDaysFpm(String poi, int vertionNum) throws IOException{
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column(DAYDATA_CF, FPM_COL));
		Result rs = this.get(generateRowKey(poi), null, columns, vertionNum);
		if(rs != null){
			byte[] val = null;
			if(!rs.isEmpty()&&(val = rs.getValue(DAYDATA_CF, FPM_COL)) != null){
				
				//测试
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				for(KeyValue kv : rs.list()){
					System.out.println("family: " + Bytes.toString(kv.getFamily()) + "	qulifier: " +
							Bytes.toString(kv.getQualifier()) + "	value: " + Bytes.toInt(kv.getValue()) + 
							" 	timestamp: " + sdf.format(new Date(kv.getTimestamp())));
				}
				return rs;
			}
		}
		//System.out.println("poi: " + poi + " has no fpm");
		return null;
		
		
		//return rs;
		
	}
	*/
	
	
	
	
	/**
	 * 添加某POI某日期（精确到天）的图片
	 * @author leeying
	 * @param poi
	 * @param timestamp
	 * @param content 图片的路径
	 * @return
	 * @throws IOException 
	 */
	public Boolean setPic(String poi, Date timestamp, byte[] content) throws IOException {
		// 只要精确到天
		/*timestamp = new Date(timestamp.getYear(), timestamp.getMonth(),
				timestamp.getDate(), 0, 0, 0);*/
		// 存入图片的列名
		//byte[] col = nextPIC_COL(poi, timestamp);
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		kvs.add(new KeyValue(generateRowKey(poi,timestamp), DAYDATA_CF, PIC_COL, timestamp
				.getTime(), content));
		
		 System.out.println("写入POI = " + poi + "列=" + new String(PIC_COL) +
		 " Time = " + timestamp.getTime() + "图片=" + new String(content));
		
		long count = this.countColumn(generateRowKey(poi,Long.MIN_VALUE), DAYDATA_CF, COUNT_COL,1);
		
		return this.put(generateRowKey(poi,timestamp), timestamp, kvs);

	}
	
	/**
	 * @author ytt
	 * 添加图片，为上传图片
	 * @param poiId {@link String}
	 * @param path {@link String}
	 * @param fpm {@link Integer}
	 * @param time {@link Date}
	 * @return boolean
	 * 
	 * */
	public boolean setPic(String poiId, String path, int fpm, 
			Date time){
		JSONObject jsonObject = new JSONObject();
		jsonObject.accumulate("pic", path);
		jsonObject.accumulate("fpm", fpm);
		try {
			if(!setPic(poiId, time, Bytes.toBytes(jsonObject.toString()))){
				System.out.println("setPic is error !");
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean setPic(String poiId, String path, int fpm, int actual_fpm,
			Date time,String nearPmStation){
		JSONObject jsonObject = new JSONObject();
		jsonObject.accumulate("pic", path);
		jsonObject.accumulate("fpm", fpm);
		jsonObject.accumulate("actual_fpm", actual_fpm);
		jsonObject.accumulate("nearPmStation", nearPmStation);
		try {
			if(!setPic(poiId, time, Bytes.toBytes(jsonObject.toString()))){
				System.out.println("setPic is error !");
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	/***
	 * @author ytt
	 * 2015-04-24
	 * 获取某些站点的某些图片
	 * @param list<String>poiStrings  哪些poi
	 * @param int picCount  几张图片
	 * 
	 * */
	
	public Set<JSONObject> getNewestPic(List<String> poiStrings, int picCount){
		Set<JSONObject> jsonObjects = new HashSet<JSONObject>();
		for(String poi : poiStrings){
			Scan scan =  new Scan();
			FilterList filterList = new FilterList();
			Filter filter = new RowFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(poi + Constants.SEPARATER)));
			filterList.addFilter(filter);
			List<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column(DAYDATA_CF, PIC_COL));
			ResultScanner resultScanner = this.scan(scan, null, columnList, filterList);
			int i = 0;
			Iterator<Result> iterator = resultScanner.iterator();
			while(i <= picCount || iterator.hasNext()){
				Result result = iterator.next();
				for(KeyValue kValue : result.list()){
					JSONObject JsonObject = JSONObject.fromObject(Bytes.toString(kValue.getKey()));
					jsonObjects.add(JsonObject);
				}
				i++;
			}
			
			
			
		}
		return jsonObjects;
	}
	
	
	/**
	 * @author ytt
	 * 2015-04-10
	 * @param poi
	 * @param starttime 
	 * @param stoptime 是终止时间，
	 * @param gradeSet 当为true时，是指天为单位，当为false时，是以小时为单位
	 * @return List<JSONObject> 每一个JSONObject是{pic：XXX,fpm:XXX,poi:XXX,time:XXX}
	 * 
	 * 当starttime ！= null && stoptime == null时，指仅获取starttime那一天的图片
	 * 当starttime != null && stoptime != null时，指获取获得某poi点的starttime-stoptime之间的图片
	 * 当starttime == null && stoptime != null时，指获取获得某poi点的最早的图片-stoptime之间的图片
	 * 当starttime == null && stoptime == null时，指获取获得某poi点的所有的图片
	 * 
	 * 
	 * */
	public List<JSONObject> getPic(String poi,Date starttime, Date stoptime, boolean gradeSet) throws IOException{
		List<JSONObject> joList= new ArrayList<JSONObject>();
		Date starttimestamp = null;
		Date stoptimestamp = null;
		Scan scan = new Scan();
		if(starttime != null){
			if(gradeSet){ /////默认为空时
				starttimestamp = new Date(starttime.getYear(), starttime.getMonth(), 
						starttime.getDate(), 0, 0);
				if(stoptime == null){
					
					stoptimestamp =new Date(starttime.getYear(), starttime.getMonth(), 
							starttime.getDate()+1, 0, 0);
				}else{
					stoptimestamp = new Date(stoptime.getYear(), stoptime.getMonth(), 
							stoptime.getDate(), 0, 0);
				}
				
				System.out.println(starttimestamp.getTime() + "is  starttimestap!!!!");
				System.out.println(stoptimestamp.getTime() + "is  stoptimestap!!!!");
				
				
			}
			else {
				if(stoptime == null){
					stoptimestamp = new Date(starttime.getYear(), starttime.getMonth(), 
							starttime.getDate(), starttime.getHours()+1, 0);
				}
				starttimestamp = new Date(starttime.getYear(), starttime.getMonth(), 
						starttime.getDate(), starttime.getHours(), 0);
				stoptimestamp = new Date(stoptime.getYear(), stoptime.getMonth(), 
						stoptime.getDate(), starttime.getHours(), 0);
			}
			
			//System.out.println(starttimestamp + "ojoagohgighh  starttime");
			//System.out.println(stoptimestamp + "ojoagohgighh  stoptime");
			scan.setStopRow(generateRowKey(poi, starttimestamp));
			scan.setStartRow(generateRowKey(poi, stoptimestamp));
		}
		else if(starttime == null && stoptime == null){
			//当都为空时，表示获取poi的所有的pm值
			Filter filter = new PrefixFilter(Bytes.toBytes(poi));
			scan.setFilter(filter);
		}
		else if(starttime == null && stoptime != null){
			scan.setStartRow(generateRowKey(poi, stoptimestamp));
			scan.setStopRow(generateRowKey(poi, Long.MAX_VALUE));
		}
		
		
		List<byte[]> fList = new ArrayList<byte[]>();
		fList.add(DAYDATA_CF);
		
		List<Column> cList = new ArrayList<Column>();
		cList.add(new Column(DAYDATA_CF, PIC_COL));
		
		ResultScanner resultScanner = this.scan(scan, fList, cList, null);
		
		for(Result rs : resultScanner){
			System.out.println("hfiihgr into scanner 11");
			
			for(KeyValue kv : rs.list()){
				//JSONObject jsonObject = new JSONObject();
				String[] rowKey = Bytes.toString(kv.getRow()).split(Constants.SEPARATER);
				Date timestamp = new Date(Long.MAX_VALUE-Long.parseLong(rowKey[1]));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = sdf.format(timestamp);
				
				JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
				jsonObject.accumulate("poi", poi);
				jsonObject.accumulate("date", time);
				
				joList.add(jsonObject);
				
			}			
		}
		try {
			resultScanner.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			resultScanner.close();
		}
		
		//System.out.println(joList);
		
		return joList;
		
	}
	
	
	
/*
	*//**
	 * 获得符合poiPrefix的所有POI点某天的所有图片，没有则返回null
	 * @author leeying
	 * @param poi
	 * @return
	 *//*
	public Set<byte[]> getPic(String poiPrefix, Date timestamp) {
		Date before = new Date();
		Scan scan = new Scan();
		scan.setStartRow(generateRowKey(poiPrefix,timestamp));
		scan.setStopRow(generateRowKey(Integer.parseInt(poiPrefix) + 1 + "",timestamp));
		if(timestamp != null){
			// 只要精确到天
			timestamp = new Date(timestamp.getYear(), timestamp.getMonth(),
					timestamp.getDate(), 0, 0, 0);
			scan.setTimeStamp(timestamp.getTime());
		}
		else{
			scan.setMaxVersions();
		}
		
		List<byte[]> cfList = new ArrayList<byte[]>();
		cfList.add(DAYDATA_CF);
		
	
		ResultScanner rs = this.scan(scan, cfList, null, null);
		
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd");
		if(rs != null){
			
			//HashSet不能保证顺序，不是同步的
			Set<byte[]> results = new HashSet<byte[]>();
			for (Result r : rs) {
				String poi = Bytes.toString(r.getRow());
				
				for (KeyValue kv : r.list()) {
					
					 * @ytt
					 * 因为存储的时候都是以时间戳的LONG形式存储，但是呢，当输出的时候，最好以我们的一种常用时间
					 * 格式来显示，即将long转换成yyyy-mm-dd的形式
					 * 
					 * 
					 * 
					
					
					//将dayData：pic列打出来！
					if (!Bytes.toString(kv.getQualifier()).equals(
							Bytes.toString(COUNT_COL))) {
						//时间戳的变换
						Date dt = new Date(kv.getTimestamp());
						String dtsString = sdf.format(dt);
						
						
						JSONObject jo = JSONObject.fromObject(Bytes.toString(kv
								.getValue()));
						jo.accumulate("poi", poi);
						jo.accumulate("date", dtsString);
						
						results.add(jo.toString().getBytes());
						
						//打印出来的顺利
						System.out.println("poi: " + poi + Bytes.toString(kv.getFamily()) + Bytes.toString(kv.getQualifier()) + " timestamp: " + dtsString
								+ "pic = " + Bytes.toString(kv.getValue()));
					}
				

				}
			}
			try{
				rs.close();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				rs.close();
			}
			
			Date after = new Date();
			logger.info("getPic"  + " in"
					+ (after.getTime() - before.getTime()) + "ms");
			
			return results;
		}
		
		return null;

	}
	*/
	
	/**
	 * 得到某poi的所有的图片
	 * @author ytt
	 * 记住：应该修改poi表的DAYDATA_CF：PIC_COL里的value{piccontent，fpm}！！！
	 * 
	 * */
	
	/*public Map<String, Integer> getPicFpmForModel(String poi){
		Map<String, Integer> map = null;
		if(poi != null){
			List<byte[]> fList = new ArrayList<byte[]>();
			fList.add(DAYDATA_CF);
			Scan scan = new Scan();
			scan.setStartRow(generateRowKey(poi));
			scan.setStopRow(generateRowKey(poi+1));
			
			ResultScanner rs = this.scan(scan, fList, null, null); 
			Iterator<Result> rIterator = rs.iterator();
			while(rIterator.hasNext()){
				Result result = rIterator.next();
				for(KeyValue kv : result.list()){
					
					
					JSONObject jo = JSONObject.fromObject(Bytes.toString(kv
							.getValue()));
					System.out.println(jo);
					String pic = jsonObject.getString("pic");
					Integer fpm = jsonObject.getInt("fpm");
					map.put(pic, fpm);
					System.out.println(map);
					}
					
					
				}
			return map;
			}
		return null;
	}
	*/

	/**
	 * 设置某poi的展示级别level
	 * 
	 * @param String poi
	 * @param double lat
	 * @param double lon
	 * @param String builderName
	 * @return
	 */
	public Boolean setPOIInfo(String poi, double lon,double lat, double blon, double blat, String builderName) {
		//组合成JSON字符串
		JSONObject jsonobject = new JSONObject();
		jsonobject.accumulate("poi", poi);
		jsonobject.accumulate("lon", lon);
		jsonobject.accumulate("lat", lat);
		jsonobject.accumulate("blon", blon);
		jsonobject.accumulate("blat", blat);
		jsonobject.accumulate("builder", builderName);
		jsonobject.accumulate("address", Geocoder.gps2address4road(lat, lon));
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		kvs.add(new KeyValue(generateRowKey(poi,Long.MIN_VALUE), INFO_CF, POI_INFO_COL, Bytes.toBytes(jsonobject.toString())));
		
		return this.put(generateRowKey(poi,Long.MIN_VALUE), null, kvs);

	}
	
	
	/**
	 * 计算每一天的站点内的第一张照片
	 * @return
	 */
	public static ArrayList<String> oneDayCal(Date d) {
		ArrayList<String> res = new ArrayList<String>();
		Format format = new SimpleDateFormat("yyyy-MM-dd 05:00:00");
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(d); 
		cal.add(cal.DATE, 1); 		
		String dateStart = format.format(d);
		String dateEnd = format.format(cal.getTime());
		res.add(dateStart);
		res.add(dateEnd);
	
		return res;
	}

	public List<JSONObject> getAllPOIOnePicEveryDay(List<String> poiStrings) {
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		for (String poi : poiStrings) {
			// 因为只要一张图片所以设置flag = 0，标志有了一张图片
			int flag = 0;
			ArrayList<String> date = oneDayCal(new Date());

			long timeStart = Web.testHtable.date2TimeStamp(date.get(0),
					"yyyy-MM-dd HH:mm:ss");
			long timeEnd = Web.testHtable.date2TimeStamp(date.get(1),
					"yyyy-MM-dd HH:mm:ss");
			
			int j=0;

			for (int i = 0; i < 7; ) {
				JSONObject jsonObject = new JSONObject();
				
				System.out.println(timeStart);
				System.out.println(timeEnd);
				System.out.println(new Date(timeStart));
				System.out.println(new Date(timeEnd));
				
				Scan scan = new Scan();
				try {
					scan.setMaxVersions();
					scan.setTimeRange(timeStart, timeEnd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				FilterList filterList = new FilterList();
				Filter filter = new PrefixFilter(Bytes.toBytes(poi
						+ Constants.SEPARATER));

				filterList.addFilter(filter);

				List<Column> columnList = new ArrayList<Column>();
				columnList.add(new Column(INFO_CF, POI_INFO_COL));
				columnList.add(new Column(DAYDATA_CF, PIC_COL));
				columnList.add(new Column(DAYDATA_CF, FPM_COL));

				ResultScanner resultScanner = this.scan(scan, null, columnList,
						filterList);

				Iterator<Result> iterator = resultScanner.iterator();
				while (iterator.hasNext()) {

					Result result = iterator.next();
					for (KeyValue kValue : result.list()) {

						// if(Bytes.toString(kValue.getFamily()).equals("info")){
						// jsonObject =
						// JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						//
						// }
						if (Bytes.toString(kValue.getQualifier()).equals("pic")
								&& flag == 0) {
							JSONObject jsonObject2 = JSONObject
									.fromObject(Bytes.toString(kValue
											.getValue()));

							String picString = jsonObject2.getString("pic");
							if (picString.lastIndexOf("/") != -1) {
								int index = picString.lastIndexOf("/");
								picString = picString.substring(0, index)
										+ "/compress_"
										+ picString.substring(index + 1);

							} else {
								picString = "compress_" + picString;
							}
							jsonObject.accumulate("pic", picString);
							jsonObject
									.accumulate("fpm", jsonObject2.get("fpm"));
							if (jsonObject2.get("actual_fpm") != null) {
								jsonObject.accumulate("actual_fpm",
										jsonObject2.get("actual_fpm"));
							} else {
								jsonObject.accumulate("actual_fpm", 0);
							}
							jsonObject.accumulate("nearPmStation",
									jsonObject2.get("nearPmStation"));
							jsonObject
									.accumulate("time", kValue.getTimestamp());
							i++;
//							flag = 1;
							break;
						}
						

					}
					jsonObjects.add(jsonObject);
					break;
					
				}
				// System.out.println(jsonObject);
				
				
				timeStart = timeStart - 86400000;
				timeEnd = timeEnd - 86400000;
				j++;
				if(j>300){
					break;
				}
			}
		}
		System.out.println("the result is:" + jsonObjects);
		return jsonObjects;
	}
	//主站照片墙
	public List<JSONObject> getAllPoiOnePicForMainStation(List<String> poiStrings, int count){
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		int picEveryPoi = 2;
		int poiNum = poiStrings.size();
		switch (poiNum){
		case 1:picEveryPoi = 10;break;
		case 2:picEveryPoi = 5;break;
		case 3:picEveryPoi = 4;break;
		case 4:picEveryPoi = 3;break;
		case 5:picEveryPoi = 2;break;
		case 6:picEveryPoi = 2;break;
		case 7:picEveryPoi = 2;break;
		case 8:picEveryPoi = 2;break;
		default:picEveryPoi = 1;break;
		}
		int startPic = (count-1)*picEveryPoi;
		int endPic = count*picEveryPoi;
		for(String poi : poiStrings){
			//因为只要一张图片所以设置flag = 0，标志有了一张图片
			int flag = 0,flag2 = 0;
			
			JSONObject InfoObject = new JSONObject();
			FilterList filterList = new FilterList();
			Filter filter = new PrefixFilter(Bytes.toBytes(poi + Constants.SEPARATER));
			filterList.addFilter(filter);
			
			List<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column(INFO_CF, POI_INFO_COL));
			columnList.add(new Column(DAYDATA_CF, PIC_COL));
			columnList.add(new Column(DAYDATA_CF, FPM_COL));
			
			
			ResultScanner resultScanner = this.scan(null, null, columnList, filterList);
			
			
			Iterator<Result> iterator = resultScanner.iterator();
			int m=0,n=0;
			while(iterator.hasNext()){
				Result result = iterator.next();
				for(KeyValue kValue : result.list()){
					if(Bytes.toString(kValue.getFamily()).equals("info")){
						InfoObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						flag2 = 1;
					}
					JSONObject jsonObject = new JSONObject();
					//System.out.println(Bytes.toString(kValue.getQualifier()));
					if(Bytes.toString(kValue.getQualifier()).equals("pic")){
						JSONObject jsonObject2 = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						String picString = jsonObject2.getString("pic");
						if(picString.lastIndexOf("/") != -1){
							int index = picString.lastIndexOf("/");
							picString = picString.substring(0, index) + "/compress_" + picString.substring(index+1);
							
						}
						else{
							picString = "compress_" + picString;
						}
						if(InfoObject!=null){
							jsonObject.accumulate("poiInfo", InfoObject);
						}
						jsonObject.accumulate("pic", picString);
						jsonObject.accumulate("fpm", jsonObject2.get("fpm"));
						//new
						if(jsonObject2.get("actual_fpm")!=null){
							jsonObject.accumulate("actual_fpm",jsonObject2.get("actual_fpm"));
						}else{
							jsonObject.accumulate("actual_fpm",0);
						}
						jsonObject.accumulate("nearPmStation",jsonObject2.get("nearPmStation"));
						jsonObject.accumulate("time", kValue.getTimestamp());
						flag++;
					}
					if(flag>startPic && flag<=endPic && jsonObject.size()!=0){
						jsonObjects.add(jsonObject);
					}else{
						jsonObject = null;
					}
				}
				if(flag == endPic )break;
				
			}
			//System.out.println(jsonObject);
			
			
		}
		
		return jsonObjects;
	}
	
	/**
	 * @author ytt
	 * 获取某个poi站点的信息,针对手机客户端
	 * @return Set<String>JsonObject
	 * 返回的是{{poi:XXX;lon:XXX,lat:XXX,blon:XXX, blat: XXX,address:XXXX,user:XXX,pic:(XXXX 代表路径),fpm:XXX照片的pm值 ,time：照片拍摄的时间}{}{}}
	 * 
	 * 
	 * */
	public List<JSONObject> getAllPOIOnePic(List<String> poiStrings){
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		for(String poi : poiStrings){
			//因为只要一张图片所以设置flag = 0，标志有了一张图片
			int flag = 0,flag2 = 0;
			JSONObject jsonObject = new JSONObject();
			FilterList filterList = new FilterList();
			Filter filter = new PrefixFilter(Bytes.toBytes(poi + Constants.SEPARATER));
			filterList.addFilter(filter);
			
			List<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column(INFO_CF, POI_INFO_COL));
			columnList.add(new Column(DAYDATA_CF, PIC_COL));
			columnList.add(new Column(DAYDATA_CF, FPM_COL));
			
			
			ResultScanner resultScanner = this.scan(null, null, columnList, filterList);
			
			
			Iterator<Result> iterator = resultScanner.iterator();
			
			while(iterator.hasNext()){
				Result result = iterator.next();
				
				for(KeyValue kValue : result.list()){
					if(Bytes.toString(kValue.getFamily()).equals("info")){
						jsonObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						flag2 = 1;
					}
					
					//System.out.println(Bytes.toString(kValue.getQualifier()));
					if(Bytes.toString(kValue.getQualifier()).equals("pic") && flag == 0){
						JSONObject jsonObject2 = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						String picString = jsonObject2.getString("pic");
						if(picString.lastIndexOf("/") != -1){
							int index = picString.lastIndexOf("/");
							picString = picString.substring(0, index) + "/compress_" + picString.substring(index+1);
							
						}
						else{
							picString = "compress_" + picString;
						}
						jsonObject.accumulate("pic", picString);
						jsonObject.accumulate("fpm", jsonObject2.get("fpm"));
						//new
						if(jsonObject2.get("actual_fpm")!=null){
							jsonObject.accumulate("actual_fpm",jsonObject2.get("actual_fpm"));
						}else{
							jsonObject.accumulate("actual_fpm",0);
						}
						jsonObject.accumulate("nearPmStation",jsonObject2.get("nearPmStation"));
						jsonObject.accumulate("time", kValue.getTimestamp());
						flag = 1;
					}
				}
				if(flag==1&&flag2 == 1)break;
			}
			//System.out.println(jsonObject);
			if(jsonObject.size()!=0){
				jsonObjects.add(jsonObject);
			}
			
		}
		
		return jsonObjects;
	}
	
	/**
	 * @author ytt
	 * 获取某个poi站点的信息,针对手机客户端
	 * @return Set<String>JsonObject
	 * 返回的是{{poi:XXX;lon:XXX,lat:XXX,blon:XXX, blat: XXX,address:XXXX,user:XXX,pic:(XXXX 代表路径),fpm:XXX照片的pm值 ,time：照片拍摄的时间}{}{}}
	 * 
	 * 
	 * */
	public JSONObject getOnePOIOnePic(String poi){
		
			int flag = 0,flag2 = 0;
//			int n =0,m=0;
			JSONObject jsonObject = new JSONObject();
			FilterList filterList = new FilterList();
			Filter filter = new PrefixFilter(Bytes.toBytes(poi + Constants.SEPARATER));
			filterList.addFilter(filter);
			
			List<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column(INFO_CF, POI_INFO_COL));
			columnList.add(new Column(DAYDATA_CF, PIC_COL));
			columnList.add(new Column(DAYDATA_CF, FPM_COL));
			
			
			ResultScanner resultScanner = this.scan(null, null, columnList, filterList);
			
			
			Iterator<Result> iterator = resultScanner.iterator();
			
			while(iterator.hasNext()){
				Result result = iterator.next();
//				n++;
//				m++;
//				System.out.println("itorator:"+n);
				for(KeyValue kValue : result.list()){
//					System.out.println("kValue:"+m);
				
					if(Bytes.toString(kValue.getFamily()).equals("info")){
						jsonObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						flag2 = 1;
						System.out.println("info");
//						if(flag==1)break;
					}
					
					//System.out.println(Bytes.toString(kValue.getQualifier()));
					if(Bytes.toString(kValue.getQualifier()).equals("pic") &&flag == 0){
						JSONObject jsonObject2 = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						String picString = jsonObject2.getString("pic");
						if(picString.lastIndexOf("/") != -1){
							int index = picString.lastIndexOf("/");
							picString = picString.substring(0, index) + "/compress_" + picString.substring(index+1);
							
						}
						else{
							picString = "compress_" + picString;
						}
						jsonObject.accumulate("pic", picString);
						jsonObject.accumulate("fpm", jsonObject2.get("fpm"));
						//new
						if(jsonObject2.get("actual_fpm")!=null){
							jsonObject.accumulate("actual_fpm",jsonObject2.get("actual_fpm"));
						}else{
							jsonObject.accumulate("actual_fpm",0);
						}
						jsonObject.accumulate("nearPmStation",jsonObject2.get("nearPmStation"));
						jsonObject.accumulate("time", kValue.getTimestamp());
						System.out.println("pic");
						flag = 1;
//						break;
					}
				}
				if(flag==1&&flag2 == 1)break;
			}
			//System.out.println(jsonObject);
		return jsonObject;
	}
	
	
	
	/**
	 * @author ytt
	 * 为了前端的返回poi所有信息
	 * @return list<JSONObject>
	 * 返回的是{{poi:XXX;lon:XXX,lat:XXX,blon:XXX,blat:XXX,address:XXX,user:XXX,pic:[(XXXX 代表路径)()],fpm:[XXX照片的pm值,...] ,time：[...,照片拍摄的时间]}{}{}}
	 * 
	 * 
	 * */
	public List<JSONObject> getAllPOIAllPic(List<String> poiStrings){
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		for(String poi : poiStrings){
			Scan scan = new Scan();
			scan.setMaxVersions();
			List<String> picList = new ArrayList<String>();
			List<Integer> fpmList = new ArrayList<Integer>();
			List<Integer> actualfpmList = new ArrayList<Integer>();
			List<String> nearStationList = new ArrayList<String>();
			List<Long> timeList = new ArrayList<Long>();
			
			
			JSONObject jsonObject = new JSONObject();
			FilterList filterList = new FilterList();
			Filter filter = new PrefixFilter(Bytes.toBytes(poi + Constants.SEPARATER));
			filterList.addFilter(filter);
			
			List<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column(INFO_CF, POI_INFO_COL));
			columnList.add(new Column(DAYDATA_CF, PIC_COL));
			columnList.add(new Column(DAYDATA_CF, FPM_COL));
			
			ResultScanner resultScanner = this.scan(scan, null, columnList, filterList);
			
			
			Iterator<Result> iterator = resultScanner.iterator();
			
			while(iterator.hasNext()){
				Result result = iterator.next();
				
				
				for(KeyValue kValue : result.list()){
				
					if(Bytes.toString(kValue.getFamily()).equals("info")){
						jsonObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						
					}
					
					//System.out.println(Bytes.toString(kValue.getQualifier()));
					if(Bytes.toString(kValue.getQualifier()).equals("pic")){
						JSONObject jsonObject2 = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						String picString = jsonObject2.getString("pic");
						if(picString.lastIndexOf("/") != -1){
							int index = picString.lastIndexOf("/");
							picString = picString.substring(0, index) + "/compress_" + picString.substring(index+1);
							
						}
						else{
							picString = "compress_" + picString;
						}
						
//						//new
//						if(jsonObject2.get("actual_fpm")!=null){
//							jsonObject.accumulate("actual_fpm",jsonObject2.get("actual_fpm"));
//						}else{
//							jsonObject.accumulate("actual_fpm",0);
//						}
//						jsonObject.accumulate("nearPmStation",jsonObject2.get("nearPmStation"));
//						
						
						if(jsonObject2.get("nearPmStation")!=null){
							nearStationList.add(jsonObject2.getString("nearPmStation"));
						}else{
							nearStationList.add("null");
						}
						if(jsonObject2.get("actual_fpm")!=null){
							actualfpmList.add(jsonObject2.getInt("actual_fpm"));
						}else{
							actualfpmList.add(0);
						}
						picList.add(picString);
						fpmList.add(jsonObject2.getInt("fpm"));
						timeList.add(kValue.getTimestamp());	
					}	
				}
			}
			
			jsonObject.accumulate("pic", picList);
			jsonObject.accumulate("fpm", fpmList);
			jsonObject.accumulate("time", timeList);
			
			jsonObject.accumulate("actual_fpm", actualfpmList);
			jsonObject.accumulate("nearPmStation", nearStationList);
			jsonObjects.add(jsonObject);
		}
		
		return jsonObjects;
	}
	
	/**
	 * @author ytt
	 * 为了前端的返回poi所有信息
	 * @return JSONObject
	 * 返回的是{poi:XXX;lon:XXX,lat:XXX,blon:XXX,blat:XXX,address:XXX,user:XXX,pic:[(XXXX 代表路径)()],fpm:[XXX照片的pm值,...] ,time：[...,照片拍摄的时间]}{}{}
	 * 
	 * 
	 * */
	public JSONObject getOnePOIAllPic(String poi){

			JSONObject jsonObject = new JSONObject();
			Scan scan = new Scan();
			scan.setMaxVersions();
			List<String> picList = new ArrayList<String>();
			List<Integer> fpmList = new ArrayList<Integer>();
			List<Long> timeList = new ArrayList<Long>();
			
			
			FilterList filterList = new FilterList();
			Filter filter = new PrefixFilter(Bytes.toBytes(poi + Constants.SEPARATER));
			filterList.addFilter(filter);
			
			List<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column(INFO_CF, POI_INFO_COL));
			columnList.add(new Column(DAYDATA_CF, PIC_COL));
			columnList.add(new Column(DAYDATA_CF, FPM_COL));
			
			ResultScanner resultScanner = this.scan(scan, null, columnList, filterList);
			
			
			Iterator<Result> iterator = resultScanner.iterator();
			
			while(iterator.hasNext()){
				Result result = iterator.next();
				
				
				for(KeyValue kValue : result.list()){
				
					if(Bytes.toString(kValue.getFamily()).equals("info")){
						jsonObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						
					}
					
					//System.out.println(Bytes.toString(kValue.getQualifier()));
					if(Bytes.toString(kValue.getQualifier()).equals("pic")){
						JSONObject jsonObject2 = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						String picString = jsonObject2.getString("pic");
						
						if(picString.lastIndexOf("/") != -1){
							int index = picString.lastIndexOf("/");
							picString = picString.substring(0, index) + "/compress_" + picString.substring(index+1);
							
						}
						else{
							picString = "compress_" + picString;
						}
						picList.add(picString);
						System.out.println(picString);
						fpmList.add(jsonObject2.getInt("fpm"));
						timeList.add(kValue.getTimestamp());	
					}	
				}
			}
			jsonObject.accumulate("pic", picList);
			jsonObject.accumulate("fpm", fpmList);
			jsonObject.accumulate("time", timeList);
		
		
		return jsonObject;
	}
	
	
	/**
	 * @author ytt
	 * 获得所有POI的信息，没有则返回null
	 * 
	 * @param level
	 * @return
	 */
	public List<JSONObject> getAllPOIsInfo() {
		// 过滤器
		/*SingleColumnValueFilter f = new SingleColumnValueFilter(INFO_CF,
				POI_INFO_COL, CompareFilter.CompareOp.EQUAL, Bytes.toBytes(level));
		f.setFilterIfMissing(true);
		FilterList fl = new FilterList(f);*/
		// 设置扫描列
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(INFO_CF, POI_INFO_COL));

		//扫描
		ResultScanner rs = this.scan(null, null, columnList, null);
		if (rs != null) {
			List<JSONObject> results = new ArrayList<JSONObject>();
			for (Result r : rs) {
				if (!r.isEmpty()) {
					results.add(JSONObject.fromObject(Bytes.toString(r.getValue(INFO_CF, POI_INFO_COL))));
				}
			}
			rs.close();
			// 为了返回null
			if(results.size() == 0){
				return null;
			}
			return results;
		}

		return null;

	}
	
	
	/**
	 * @author ytt
	 * 得到所有的POIId
	 * @retune list<String>
	 * */
	
	public List<String> getAllPOIId(){
		List<String> poiIds = new ArrayList<String>();
		
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(INFO_CF, POI_INFO_COL));

		//扫描
		ResultScanner rs = this.scan(null, null, columnList, null);
		for(Result result : rs){
			for(KeyValue kValue : result.list()){
				JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
				poiIds.add(jsonObject.getString("poi"));
			}
		}

		return poiIds;
	}

	/**
	 * 存储某poi的model模型
	 * @author ytt
	 * 
	 * @param poi
	 * @param modelPath
	 * @param timestamp 可以为空，当没有时
	 * @return boolean
	 * **/
	
	public boolean setModel(String poi, String modelPath,Date timestamp){
		if(modelPath != null){
			List<KeyValue> kv = new ArrayList<KeyValue>();
			kv.add(new KeyValue(generateRowKey(poi,Long.MIN_VALUE), INFO_CF, 
					MODEL_INFO_COL, Bytes.toBytes(modelPath)));
			return this.put(generateRowKey(poi,Long.MIN_VALUE), timestamp, kv);
		}
		
			return false;
	}
	
	/***
	 * 根据某poi点，来获取它的modelPath
	 * @author ytt
	 *@param String 
	 * 
	 * 
	 * */
	public String getModel(String poi){
		String modelPath = null;
		if(poi != null){
			List<Column> collist = new ArrayList<Column>();
			collist.add(new Column(INFO_CF, MODEL_INFO_COL));
			
			Result rs = this.get(generateRowKey(poi,Long.MIN_VALUE), null, null, collist, false);
			for(KeyValue kv:rs.list()){
				modelPath =  kv.getKey().toString();
				System.out.println(poi + "的model路径是" + modelPath);
			}
			return modelPath;
		}
		return modelPath;
		
	}
	
	
	/**
	 * 在导入图片的时候用到，导入poi表所有的信息
	 * 
	 * 
	 * */
	public boolean setPOI(String poiId, double lon, double lat, double blon, double blat, String userName, Date time, int fpm,String path){
		
		try {
			if(!setPOIInfo(poiId, lon, lat, blon, blat, userName)){
				System.out.println("setPoiInfo is error!!");
				return false;
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("pic", path);
			jsonObject.accumulate("fpm", fpm);
			if(!setPic(poiId, time, Bytes.toBytes(jsonObject.toString()))){
				System.out.println("setPic is error !");
				return false;
			}
			if(!setPoiFpm(poiId, time, fpm)){
				System.out.println("setPOIFpm is error!");
				return false;
			}
		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	

	
	/**
	 * 得到某一列的数目
	 * @throws IOException 
	 * @author ytt
	 * @param String poi
	 * @return long
	 * */
	/*public long getPoiPicCount(String poi,Date timestamp) {
		//System.out.println("fafhggh getPoicount");
		Scan scan = new Scan();
		try{
			return this.countColumn(generateRowKey(poi,Long.MIN_VALUE), DAYDATA_CF, COUNT_COL, 0);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return 0;
	}*/
	public long getPoiPicCount(String poi,Date timestamp) {
		//System.out.println("fafhggh getPoicount");
		Scan scan = new Scan();
		scan.setMaxVersions();
		if(timestamp != null) {
			Date timNow = new Date();
			long maxStamp = timNow.getTime();
			long minStamp = timestamp.getTime();
			try {
				scan.setTimeRange(minStamp, maxStamp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FilterList filterList = new FilterList();
		Filter filter = new PrefixFilter(Bytes.toBytes(poi + Constants.SEPARATER));
		filterList.addFilter(filter);
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(DAYDATA_CF, PIC_COL));
		ResultScanner resultScan = this.scan(scan, null, columnList, filterList);
		Iterator<Result> iterator = resultScan.iterator();	
		long count = 0;
		while(iterator.hasNext()){
			Result result = iterator.next();			
			for(KeyValue keyValue : result.list()){		
				JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(keyValue.getValue()));
				if(jsonObject.has("actual_fpm")) count++;
				//System.out.println(Bytes.toString(keyValue.getValue()));		
			}	
		}
		return count;
	}
	/**
	 * @author ytt
	 * 从POITable中收集经纬度信息，完成聚合
	 * 
	 * */
	public void kmeansByPoiTable(String poiString){
		//如果是新建站点，则会发生增加新的point，新的聚合
		KmeansTable kms = new KmeansTable();
		List<byte[]> famlList = new ArrayList<byte[]>();
		famlList.add(INFO_CF);
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(INFO_CF, POI_INFO_COL));
		Result result = this.get(generateRowKey(poiString,Long.MIN_VALUE), famlList, columnList,1);
		
		for(KeyValue keyValue : result.list()){
			JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(keyValue.getValue()));
			double lon = jsonObject.getDouble("blon");
			double lat = jsonObject.getDouble("blat");
			Date timestamp = new Date(keyValue.getTimestamp());
			
			//坐标转换，将GPS转换成百度坐标
			/*double[] baiducor = convertXY.gpsTobaiducor(lon,lat);
			lat = baiducor[1];
			lon = baiducor[0];*/
			//System.out.println("转换之后的！！！！！！！！！！！！！！！！！" + lat + "/" + lon + "!!!!!!!");
			//百度经纬度坐标转换成平面坐标和块坐标
			double[] worldcor = convertXY.gpstoworldcor(lon, lat);
			System.out.println(worldcor[0] + "    worldcor     " + worldcor[1]);
			double[] titlecor = new double[2];
			double[] pixelcor = new double[2];
			
			for(int zoom = 1; zoom < 19; zoom++){
				pixelcor = convertXY.worldtopixel(worldcor[0], worldcor[1], zoom);
				titlecor = convertXY.pixeltotitle(pixelcor[0], pixelcor[1]);
				//System.out.println(pixelcor[0] + "     pixelcor    " + pixelcor[1]);
				Point point = new Point(pixelcor[0], pixelcor[1], lon, lat, poiString);
				//System.out.println(point.toString() + "(((((((((((((((((((((");
				//存进数据库中
				kms.setPonit(zoom, titlecor[0], titlecor[1], point.toString(), timestamp);
				
			}
			
			//取数据、聚合、存数据
			KmeansTest kmtest = new KmeansTest();
			System.out.println(poiString + "   $$$$$$$$$ poi ");
			kmtest.clusterAll();
			
			
		}
	}
	
	
	/**
	 * 删除POI以及GridPOITable中的信息
	 * 
	 * @param poi
	 * @return
	 * @throws IOException 
	 */
	public Boolean deletePOI(String poi){
		GridPoiTable gridPoiTable = new GridPoiTable();
		CollectionPicTable picTable = new CollectionPicTable();
		double lon = 0;
		double lat = 0;
		Get get = new Get(generateRowKey(poi, Long.MIN_VALUE));
		get.addColumn(INFO_CF, POI_INFO_COL);
		Result result = this.get(get);
		for(KeyValue kValue : result.list()){
			JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
			lon = jsonObject.getDouble("lon");
			lat = jsonObject.getDouble("lat");
		}
		
		
		Scan scan = new Scan();
		Filter preFilter = new PrefixFilter(Bytes.toBytes(poi));
		scan.setFilter(preFilter);
		ResultScanner rScanner = this.scan(scan, null, null, null);
		try {
			for(Result rs :rScanner){
				for(KeyValue kValue : rs.list()){
					System.out.println(Bytes.toString(kValue.getQualifier()));
					this.deleteColumn(kValue.getRow(), kValue.getFamily(), kValue.getQualifier());
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		gridPoiTable.deletePOIGrid(lon,lat);
		picTable.deletePoiPic(poi);
		return true;
	}
	
	// 根据照片的张数来删除照片，但是目前不用
	public List<String> deletePOI(long countlimit){
		List<String> poisToDelete = new ArrayList<String>();
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column(DAYDATA_CF, COUNT_COL));
		ResultScanner resultScanner = this.scan(null, null, columns, null);
		for(Result result : resultScanner){
			for(KeyValue kValue : result.list()){
				if(Bytes.toLong(kValue.getValue()) <= countlimit){
					String poiTodelet = UploadPOIRowkey(kValue.getRow());
					poisToDelete.add(poiTodelet);
					
				}
			}
		}
		return poisToDelete;
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		
		POITable pt = new POITable();
//		ArrayList<String> date = oneDayCal(new Date());
//		long timeStart = Web.testHtable.date2TimeStamp(date.get(0), "yyyy-MM-dd HH:mm:ss");
//		long timeEnd = Web.testHtable.date2TimeStamp(date.get(1), "yyyy-MM-dd HH:mm:ss");
//		System.out.println(timeStart);
//		System.out.println(timeEnd);
//		List<String> pois = new ArrayList<String>();
//		pois.add("1521166557");
//		pois.add("1649223767");
//		pois.add("1486519736");
//		pois.add("1521166557");
//		pois.add("1521166557");
//		pois.add("1521166557");
//		pois.add("1521166557");
//		pois.add("1521166557");
		List<String> poiId = new ArrayList<String>();
		poiId.add("1722471173");
		System.out.println(pt.getAllPOIOnePic(poiId));
//		System.out.println(pt.getAllPoiOnePicForMainStation(pois,1));
		//Date timestamp = new Date(1396022400000L);
		//System.out.println(timestamp.toGMTString());
		//Set<byte[]> set = pt.getPic("10010521", null);
		

		/*Date starttime = null;
		String timeString  = "2015/03/03 19:05:05";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		starttime = sdf.parse(timeString);
		List<JSONObject> jsonObjects = pt.getPoiFpm("904737764", starttime, null, true);
       if(jsonObjects.size() == 1){
    	   System.out.println("jfoajfoejfoj");
       }
		System.out.println(jsonObjects.get(0).getInt("poi_pm_value"));
		*/
		
		
		//Result rs = pt.getDaysFpm("10010117", 100);
		/*Map<String, Integer> map = null;
		map = pt.getPicFpmForModel("10010521");
		System.out.println(map);*/
		
		
		
		//int num = pt.getPicNum();
		//System.out.println(pt.getPicNum());
		
		
		/*
		 * 测试getPOIsInfo函数
		 * **/
		/*List<String> poiStrings  = new ArrayList<String>();
		poiStrings.add("904737764");
		
		List<JSONObject> jsonObjects = pt.getAllPOIOnePic(poiStrings);
		for(JSONObject jsonObject : jsonObjects){
			System.out.println(jsonObject.toString());
			
		}*/
		/*List<String> poiStrings  = new ArrayList<String>();
		poiStrings.add("1376065005");
		
		List<JSONObject> jsonObjects = pt.getAllPOIAllPic(poiStrings);
		for(JSONObject jsonObject : jsonObjects){
			System.out.println(jsonObject.toString());
			
		}*/
		
		/*pt.deletePOI(1);
		pt.deletePOI("1402356187");*/
		
/*		long count = pt.getPoiPicCount("1402356187", null);
		System.out.println(count);*/
	//	pt.setPOIInfo("826834592", 116.353395, 39.962631, "1234");
		
		//pt.kmeansByPoiTable("827012727");
		
		//测试获取所有的poiid
		/*List<String> poiStrings = pt.getAllPOIId();
		for(String poiString : poiStrings){
			System.out.println(poiString);
		}*/
		//pt.getOnePOIAllPic("1474238012");
//		pt.getPoiPicCount("904738673",null);
		//pt.kmeansByPoiTable("904738673");
		//pt.deletePOI("907609274");		
	}

}