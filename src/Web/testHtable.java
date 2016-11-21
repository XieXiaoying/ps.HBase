package Web;


import collection.Constants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import net.sf.json.JSONObject;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;

import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import Common.AbstractTable;
import Common.Column;


public class testHtable extends AbstractTable{
	private static final String tableName = "poi222";
	
	public static final byte[] DAYDATA_CF = "dayData".getBytes();
	
	public static final byte[] PIC_COL = "pic".getBytes();
	
	public static final byte[] COUNT_COL = "count".getBytes();
	public static final byte[] FPM_COL = "poi_fpm".getBytes();
	
	public static final byte[] MODEL_INFO_COL = "model_info".getBytes();
	
	
	
	public static final byte[] INFO_CF = "info".getBytes();
	
	public static final byte[] POI_INFO_COL = "poi_info".getBytes();
	
	
	
	
public testHtable() {
		
		try {
			HBaseAdmin hAdmin = new HBaseAdmin(Constants.conf);
			if (hAdmin.tableExists(tableName)) {
			      // do nothing	
			}
			else{
				
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

	
	
	public boolean setData(){
		
		for(int i = 0; i < 100; i++){
			List<KeyValue> kvList = new ArrayList<KeyValue>();
			KeyValue kV = new KeyValue(Bytes.toBytes("row-"+i), INFO_CF,
					null, Bytes.toBytes("value-"+i));
			kvList.add(kV);
			this.put(Bytes.toBytes("row-"+i), null, kvList);
		}
		return true;
		
	}
	public void getData() {
/*		for(int i = 0; i < 100; i++) {
			Get get = new Get(Bytes.toBytes("row-"+i));
			get.addColumn(INFO_CF,TEST_COL);
			try {
				Result rs = hTable.get(get);
//				for(KeyValue kv : rs.list()){
//					JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(kv.getValue()));
//					
//					String poiString =  jsonObject.getString("poiId");
//				}
				byte[] val = rs.getValue(INFO_CF, TEST_COL);
				System.out.println("test" + Bytes.toString(val));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

		try {
			Scan scan = new Scan();
			//scan.setTimeStamp(1452691836830L);
			ResultScanner rs = hTable.getScanner(scan);
			JSONObject jsonObject = new JSONObject();
				for (Result r = rs.next(); r != null; r = rs.next()) {
				// process result...
					jsonObject = JSONObject.fromObject(Bytes.toString(r.getValue(DAYDATA_CF, PIC_COL)));
					//byte[] val = r.getValue(INFO_CF, TEST_COL);
					//System.out.println("test" + jsonObject.getString("pic"));
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public  JSONObject getOnePOIAllPic(String poi,long timeStart,long timeEnd){
		int countWangwendong = 0,countTianye = 0,countMalong = 0,countZch = 0,countWl = 0,countSy = 0;
		JSONObject jsonObject = new JSONObject();
		Scan scan = new Scan();
		//scan.setTimeStamp(1448952882000L);
		try {
			scan.setTimeRange(timeStart, timeEnd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
						int index = picString.lastIndexOf("_");
						picString = picString.substring(0, index);	
						// + "/compress_" + picString.substring(index+1)
						System.out.println(picString);
					}
					else{
						int index = picString.lastIndexOf("_");
						picString = picString.substring(0, index);	
						// + "/compress_" + picString.substring(index+1)
						System.out.println(picString);
					}
					if(picString.equals("wangwendong")) countWangwendong ++;
					if(picString.equals("joe") || picString.equals("malongfei")) countMalong ++;
					if(picString.equals("joe")) countTianye ++;
					if(picString.equals("wl")) countWl ++;
					if(picString.equals("xiaobingkuai")) countSy ++;
					if(picString.equals("zch")) countZch ++;
					picList.add(picString);
					fpmList.add(jsonObject2.getInt("fpm"));
					timeList.add(kValue.getTimestamp());	
				}	
			}
		}
		System.out.println(poi +":" +"Wwd" + countWangwendong);
		System.out.println(poi +":" +"Ty" + countTianye);
		System.out.println(poi +":" +"Malong" + countMalong);
		System.out.println(poi +":" +"Zch" + countZch);
		System.out.println(poi +":" +"Sy" + countSy);
		System.out.println(poi +":" +"Wl" + countWl);
		jsonObject.accumulate("pic", picList);
		jsonObject.accumulate("fpm", fpmList);
		jsonObject.accumulate("time", timeList);
	
	
	return jsonObject;
}
	public static long date2TimeStamp(String date_str,String format){
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			//return String.valueOf(sdf.parse(date_str).getTime()/1000) + "000";
			return sdf.parse(date_str).getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	public static String timeStamp2Date(String seconds,String format) {
		if(seconds == null || seconds.isEmpty() || seconds.equals("null")){
			return "";
		}
		if(format == null || format.isEmpty()) format = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date(Long.valueOf(seconds)));
	}
	public static void main(String[] args){
		//System.out.println("fehofh");
		//Calendar c = Calendar.getInstance();
		testHtable testHtable = new testHtable();
		long timeStart = Web.testHtable.date2TimeStamp("2016-01-25 00:00:00", "yyyy-MM-dd HH:mm:ss");
		long timeEnd = Web.testHtable.date2TimeStamp("2016-01-26 00:00:00", "yyyy-MM-dd HH:mm:ss");
		System.out.println(timeStart);
		//String time = Web.testHtable.timeStamp2Date(timeStart, "yyyy-MM-dd HH:mm:ss");
		System.out.println(timeEnd);
		//testHtable.setData();
		testHtable.getData();
//		testHtable.getOnePOIAllPic("1530028288",timeStart,timeEnd);//
//		testHtable.getOnePOIAllPic("904738673",timeStart,timeEnd);
//		testHtable.getOnePOIAllPic("1515942832",timeStart,timeEnd);
//		testHtable.getOnePOIAllPic("1549567192",timeStart,timeEnd);
//		testHtable.getOnePOIAllPic("1579885734",timeStart,timeEnd);
//		testHtable.getOnePOIAllPic("1579885734",timeStart,timeEnd);
//		testHtable.getOnePOIAllPic("1549445234", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1566504749", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1581129933", timeStart, timeEnd);
		testHtable.getOnePOIAllPic("1521166557", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1551112075", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1514698720", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1486519736", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1526693698", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1552195598", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1553991288", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1550229327", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1551113356", timeStart, timeEnd);
//		testHtable.getOnePOIAllPic("1552050738", timeStart, timeEnd);
	}
}
