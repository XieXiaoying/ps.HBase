package model;


import Web.GridPoiTable;
import collection.Constants;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSON;
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
import org.apache.jasper.tagplugins.jstl.core.Set;

import Common.AbstractTable;
import Common.Column;
import java.util.*;

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
			//String row = "1014675856/";
			//scan.setFilter(new PrefixFilter(row.getBytes()));
			//scan.setTimeStamp(1452691836830L);
			ResultScanner rs = hTable.getScanner(scan);
			JSONObject jsonObject = new JSONObject();
				for (Result r = rs.next(); r != null; r = rs.next()) {
				// process result...
					jsonObject = JSONObject.fromObject(Bytes.toString(r.getValue(DAYDATA_CF, PIC_COL)));
					String val = Bytes.toString(r.getValue(DAYDATA_CF, PIC_COL));
					System.out.println("test" + val);
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public  void getOnePOIAllPic(String poi,long timeStart,long timeEnd){
		int countWangwendong = 0,countTianye = 0,countMalong = 0,countZch = 0,countWl = 0,countSy = 0,countLs = 0,countDcY = 0;
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
		
		try {
			hTable = new HTable(Constants.conf, tableName);
			//ResultScanner rs = hTable.getScanner(scan);
			List<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column(INFO_CF, POI_INFO_COL));
			columnList.add(new Column(DAYDATA_CF, PIC_COL));
			columnList.add(new Column(DAYDATA_CF, FPM_COL));
			ResultScanner rs = this.scan(scan, null, columnList, filterList);
			Iterator<Result> iterator = rs.iterator();
			
			while(iterator.hasNext()){
			Result result = iterator.next();		
			for(KeyValue kValue : result.list()){
			
				if(Bytes.toString(kValue.getFamily()).equals("info")){
					jsonObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
					
				}		
				/*
				 * HBase中主要是Table和Family和Qualifier，这三个概念。Table可以直接理解为表，
				 * 而Family和Qualifier其实都可以理解为列，一个Family下面可以有多个Qualifier，
				 * 所以可以简单的理解为，HBase中的列是二级列，也就是说Family是第一级列，Qualifier是第二级列。
				 * 两个是父子关系。
				 */
				if(Bytes.toString(kValue.getQualifier()).equals("pic")){
					JSONObject jsonObject2 = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
					String picString = jsonObject2.getString("pic");
					
					if(picString.lastIndexOf("/") != -1){
						int index = picString.lastIndexOf("_");
						picString = picString.substring(0, index);	
					}
					else{
						int index = picString.lastIndexOf("_");
						picString = picString.substring(0, index);	
					}
					if(picString.equals("wangwendong")) countWangwendong ++;
					if(picString.equals("joe") || picString.equals("malongfei")) countMalong ++;
					if(picString.equals("joe")) countTianye ++;
					if(picString.equals("wl")) countWl ++;
					if(picString.equals("xiaobingkuai")) countSy ++;
					if(picString.equals("zch")) countZch ++;
					if(picString.equals("lishuai")) countLs ++;
					if(picString.equals("dengchaoyue"))countDcY ++;
				}	
			}
		}
			System.out.println(poi +":" +"Wwd" + countWangwendong);
			System.out.println(poi +":" +"Ty" + countTianye);
			System.out.println(poi +":" +"Malong" + countMalong);
			System.out.println(poi +":" +"Zch" + countZch);
			System.out.println(poi +":" +"Sy" + countSy);
			System.out.println(poi +":" +"Wl" + countWl);
			System.out.println(poi +":" +"liShuai" + countLs);
			System.out.println(poi +":" +"dengchaoyue" + countDcY);
//			for (Result r = rs.next(); r != null; r = rs.next()) {
//			// process result...
//				if(Bytes.toString(kValue.getFamily()).equals("info")){
//					jsonObject = JSONObject.fromObject(Bytes.toString(r.getValue()));				
//				}
//				//jsonObject = JSONObject.fromObject(Bytes.toString(r.getValue(DAYDATA_CF, PIC_COL)));
//				String val = Bytes.toString(r.getValue(DAYDATA_CF, PIC_COL));
//				System.out.println(poi + val);
//			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> countPoiPic(long timeStart,long timeEnd){
		ArrayList<String> res = new ArrayList<String>();
		String[] userName = {"wangwendong","joe","malongfei","wl","xiaobingkuai","zch","lishuai","dengchaoyue"};
		LinkedHashMap<String, Integer> mapUserCount = new LinkedHashMap<String, Integer> ();
        for(int i = 0; i < userName.length; i++) {
        	mapUserCount.put(userName[i], 0);
        }
		JSONObject jsonObject = new JSONObject();
		Scan scan = new Scan();
		//scan.setTimeStamp(1448952882000L);		
		try {
			scan.setMaxVersions();
			scan.setTimeRange(timeStart, timeEnd);
			hTable = new HTable(Constants.conf, tableName);
			List<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column(INFO_CF, POI_INFO_COL));
			columnList.add(new Column(DAYDATA_CF, PIC_COL));
			columnList.add(new Column(DAYDATA_CF, FPM_COL));
			ResultScanner rs = this.scan(scan, null, columnList, null);
			Iterator<Result> iterator = rs.iterator();
			while(iterator.hasNext()){
			Result result = iterator.next();		
			for(KeyValue kValue : result.list()){
			
				if(Bytes.toString(kValue.getFamily()).equals("info")){
					jsonObject = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
					
				}
				if(Bytes.toString(kValue.getQualifier()).equals("pic")){
					JSONObject jsonObject2 = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
					String picString = jsonObject2.getString("pic");
					
					if(picString.lastIndexOf("/") != -1){
						int index = picString.lastIndexOf("_");
						picString = picString.substring(0, index);	
					}
					else{
						int index = picString.lastIndexOf("_");
						picString = picString.substring(0, index);	
					}
					Integer a = 0;
					if(mapUserCount.get(picString) != null)  a = mapUserCount.get(picString);
					a++;
					mapUserCount.put(picString, a);//如果用新的用户名去上传也可以检测的到
				}	
			}
		}
			java.util.Iterator it = mapUserCount.entrySet().iterator();
			while(it.hasNext()){
				java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
				String key = (String) entry.getKey(); 
				Integer value = (Integer) entry.getValue(); 
				String countUser = key + ":" + value;
				//System.out.println(countUser);
				res.add(countUser);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Iterator<String> it = res.iterator(); 遍历res的方法
//		while(it.hasNext()){
//			System.out.println(it.next());
//		}
		return res;		
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
	
	public void getAllPoiCount(long timeStart,long timeEnd){
		testHtable testHtable = new testHtable();
		String pois[] = {"1530028288","904738673","1515942832","1549567192","1579885734","1549445234","1566504749","1581129933"
		,"1521166557","1551112075","1514698720","1486519736","1526693698","1552195598","1553991288","1550229327","1551113356","1552050738"};
		for(int i = 0; i < pois.length; i++) {
			testHtable.getOnePOIAllPic(pois[i], timeStart, timeEnd);
		}
	}
	public ArrayList<String> oneDayCal() {
		ArrayList<String> res = new ArrayList<String>();
		Format format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(new Date()); 
		cal.add(cal.DATE, 1); 		
		String dateStart = format.format(new Date());
		String dateEnd = format.format(cal.getTime());
		res.add(dateStart);
		res.add(dateEnd);
		return res;
	}
	public List<JSONObject> getMainStation(int zoom) {
		List<String> res = new ArrayList<String>();
		GridPoiTable gpt = new GridPoiTable();
		res = gpt.getPois();
		String pois[] = new String[res.size()];
		if(zoom < 19 && zoom > 15){		
			for(int i = 0; i < res.size(); i++) {
				pois[i] = res.get(i);
			}
		}else {
			pois = new String[]{"904738673","1552195598","1551113356","1565427823","1549567192"};
		}
		List<JSONObject> mainStation = new ArrayList<JSONObject>();
		Scan scan = new Scan();
		for(int i = 0; i < pois.length; i++) {
			FilterList filterList = new FilterList();
			Filter filter = new PrefixFilter(Bytes.toBytes(pois[i] + Constants.SEPARATER));
			filterList.addFilter(filter);
			
				List<Column> columnList = new ArrayList<Column>();
				columnList.add(new Column(INFO_CF, POI_INFO_COL));
				ResultScanner rs = this.scan(scan, null, columnList, filterList);
				Iterator<Result> iterator = rs.iterator();				
				while(iterator.hasNext()){
				Result result = iterator.next();		
				for(KeyValue kValue : result.list()){	
					JSONObject mainSta = new JSONObject();
					if(Bytes.toString(kValue.getQualifier()).equals("poi_info")){
						JSONObject jsonObject2 = JSONObject.fromObject(Bytes.toString(kValue.getValue()));
						String blon = jsonObject2.getString("blon");
						String blat = jsonObject2.getString("blat");
						double pixelX = Double.valueOf(blon);
						double pixelY = Double.valueOf(blat);	
						//System.out.print(pixelX);
						//System.out.println(pixelY);
						mainSta.accumulate("centerx", pixelX);
						mainSta.accumulate("centery", pixelY);
						mainStation.add(mainSta);
					
					}	
				}
			}
		}
		return mainStation;
	}
	public static void main(String[] args){
		testHtable testHtable = new testHtable();
		ArrayList<String> date = testHtable.oneDayCal();
		long timeStart = Web.testHtable.date2TimeStamp(date.get(0), "yyyy-MM-dd HH:mm:ss");
		long timeEnd = Web.testHtable.date2TimeStamp(date.get(1), "yyyy-MM-dd HH:mm:ss");
		//testHtable.setData();
		//testHtable.getData();
		//testHtable.getAllPoiCount(timeStart, timeEnd);统计个分站点的上传数量
		//testHtable.countPoiPic(timeStart, timeEnd);//统计一天内的上传总量
		testHtable.getMainStation(5);
	}
}
