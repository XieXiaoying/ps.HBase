package Trajectory;

import collection.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.AbstractDocument.Content;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;


import org.apache.log4j.Logger;

import psensing.Mysql;



import Common.AbstractTable;
import Common.Column;
/**
 * @author ytt
 * 2015-3-26
 * 轨迹表TrajTable
 * 
 * */
public class TrajTable extends AbstractTable{
	static Logger logger = Logger.getLogger(TrajTable.class);
	//表名
	public static final String tableName = "traj";
	//列族info
	public static final byte[] INFO_CF = "info".getBytes();
	public static final String[] family = {"info"};
	
	//path列
	public static final byte[] PATH_COL = "path".getBytes();
	//图片的数目
	public static int pathNum = 0;
	
	
	public TrajTable(){
		try{
			createTable(Constants.conf, tableName, family);
			hTable = new HTable(Constants.conf, tableName);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	synchronized public int getPathNum(){
		return pathNum;
	}
	
	
	public static byte[] generateRowKey(String userId, String trajId){
		String rowKey = userId + Constants.SEPARATER;
		if(trajId != null){
			rowKey += trajId;
		}
		
		return(rowKey.getBytes());
	}
	
	/**
	 * 
	 * @author ytt
	 * 因为path的列的个数是不确定的增加的，所以是生成的nextPATH_COL
	 * 
	 * */
	synchronized private byte[] nextPATH_COL(String userId,
			String trajId, Date timestamp){
		String colName = new String(PATH_COL) + ++pathNum;
		return colName.getBytes();
	}
	
	
	/**
	 * 
	 * @param userId
	 * @param trajId
	 * @param lat，lon，gridId
	 * @param timestamp
	 * 向轨迹表中添加path轨迹信息
	 * 
	 * */
	public boolean setPath(String userId, String trajId, double lat, double lon, String gridId,
				Date timestamp){
		byte[] col = nextPATH_COL(userId, trajId, timestamp);
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		JSONObject path = new JSONObject();
		path.accumulate("lat", lat);
		path.accumulate("lon", lon);
		path.accumulate("gridId", gridId);
		
		
		kvs.add(new KeyValue(generateRowKey(userId, trajId), INFO_CF, col, timestamp.getTime(), 
				Bytes.toBytes(path.toString())));
		
		System.out.println("userId" + userId + " trajId" + trajId + "path :" + path.toString() + 
				"timestamp: " + timestamp);
		return (this.put(generateRowKey(userId, trajId), timestamp, kvs));
		
		
		
	}
	
	/**
	 * @param file
	 * 
	 * 从file文件中导出trancest.txt轨迹到trajTable中
	 * */
	public void importPathInfo(File file){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			
			while((tempString = reader.readLine()) != null){
				String line[] = tempString.split("\t");
				String userId = line[0];
				String trajId = line[1];
				String gridId = line[2];
				double lat = Double.parseDouble(line[3]);
				double lon = Double.parseDouble(line[4]);
				
				
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = null;
				String dateString = line[5];
				try{
					date = df.parse(dateString);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
				setPath(userId, trajId, lat, lon, gridId, date);
				
				}
				reader.close();
			}
		catch(Exception e){
			e.printStackTrace();
		}
		
			
		}

	/**
	 * @author ytt
	 * 
	 * @param usrid
	 * @timestamp 目前可以为空
	 * @return List<byte[]> 对应于用户ID的轨迹序列 {grid1，。。。。。}
	 * 
	 * 
	 * */
	
	public List<String> getTrajIds(String userId, Date timestamp){
		Date before = new Date();
		Scan scan = new Scan();
		//此处有两种方法来检索row，一种就是RowFilter,第二种就是利用起始和终止行健，行健的字符长度要一致，然后直接rowid+1即可作为中止行
		//Filter filter = new RowFilter(CompareOp.EQUAL, new BinaryPrefixComparator((userId+"/").getBytes()));
		scan.setStartRow(generateRowKey(userId, null));
 		scan.setStopRow(generateRowKey(userId+1, null));
		
		
		/*FilterList filterList = new FilterList();
		
		filterList.addFilter(filter);*/
		
		if(timestamp != null){
			scan.setTimeStamp(timestamp.getTime());
		}
		else{
			scan.setMaxVersions();
		}
		
		List<byte[]> cfList = new ArrayList<byte[]>();
		cfList.add(INFO_CF);
		//ResultScanner rs = this.scan(scan, cfList, null, filterList);
		
		
		ResultScanner rs = this.scan(scan, cfList, null, null);
		if(rs != null){
			//System.out.println("hefiowehfgiwhg");
			
			List<String> trajs = new ArrayList<String>();
			for(Result r : rs){
				String rowKey = Bytes.toString(r.getRow());
				String[] row = rowKey.split(Constants.SEPARATER);
				trajs.add(row[1]);
				//System.out.println(trajs);
				
			}
			//System.out.println(trajs);
			return trajs;
			
		}
		
		return null;
		
	}
	/**
	 * @author ytt
	 * 
	 * @param userId
	 * @param gridId
	 * @timestamp 目前是null
	 * @return List<String> Arraylist={grid1/timestamp1,......}
	 * 根据用户ID以及轨迹Id来取得该轨迹的格子序列
	 * 
	 * 
	 * 
	 * */
	public List<String> getPath(String userId, String trajId, Date timestamp){
		Date before = new Date();
		
		List<byte[]> cfList = new ArrayList<byte[]>();
		cfList.add(trajId.getBytes());
		
		List<String> result = new ArrayList<String>();

		Result rs = get(generateRowKey(userId, trajId), timestamp, null, null, false);
		
		for(KeyValue kv : rs.list()){
			JSONObject jo = JSONObject.fromObject(Bytes.toString(kv.getValue()));
			String pathString = jo.get("gridId") + Constants.SEPARATER + kv.getTimestamp();
			result.add(pathString);
			/*System.out.println("userId: " + userId + "trajId: " + trajId +  "path " +
					Bytes.toString(kv.getValue()));*/
			//System.out.println(result);
			
		}
		return result;
	}


	/**
	 * @author ytt
	 * 2015/5/17
	 * 返回某用户的最近时刻的轨迹id
	 * */
	public String getNeartraj(String userId){
		String neartraj = null;
		Scan scan = new Scan();
		scan.setStartRow(generateRowKey(userId, null));
		scan.setStopRow(generateRowKey(userId+1, null));
		
		List<byte[]> familyList = new ArrayList<byte[]>();
		familyList.add(INFO_CF);
		ResultScanner resultScanner = this.scan(scan, familyList, null, null);
		
		Result nearResult = null;
		long maxtime = Long.MIN_VALUE;
		for(Result result : resultScanner){
			for(KeyValue keyValue : result.list()){
				if(maxtime <= keyValue.getTimestamp()){
					nearResult = result;
					maxtime = keyValue.getTimestamp();
				}
			}
		}
		
		String rowKey = Bytes.toString(nearResult.getRow());
		System.out.println("ejoewrj");
		neartraj = rowKey.substring(rowKey.indexOf(Constants.SEPARATER)+1);
		return neartraj;
	}
	
	
	
	public static void main(String[] args){
		TrajTable trajTable = new TrajTable();
		File file = new File("resource/txt/traces.txt");
		//trajTable.importPathInfo(file);
		
		
		/*List<String> trajs =null;
		trajs = trajTable.getTrajIds("002", null);
		System.out.println(trajs);*/
		/*String trajId = trajs.get(0);
		System.out.println("trajId" + trajId);*/
		
		//System.out.println(trajTable.getPath("039", "5384", null));
		
		
		String neartraj = trajTable.getNeartraj("001");
		System.out.println(neartraj);
		
		
		
		
	}
	
	
	
	
	
	
}
