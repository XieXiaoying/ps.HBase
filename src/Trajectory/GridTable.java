package Trajectory;

import collection.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.yecht.debug.TimeScanning;

import Common.AbstractTable;
import Common.Column;
import Web.GridPoiTable;
import Web.convertXY;

/**
 * @author ytt
 * 2015-3-26
 * 创建格子表
 * 
 * */
public class GridTable extends AbstractTable{
	Logger logger = Logger.getLogger(GridTable.class);
	
	private static final byte[] INFO_CF = "info".getBytes();
	private static final byte[] GRID_COL = "gridid".getBytes();
	private static final byte[] PREDICT_COL = "predict".getBytes();
	private static String[] family = {"info"};
	
	private static final String tableName = "grid";
	public GridTable(){
		try{
			createTable(Constants.conf, tableName, family);
			hTable = new HTable(Constants.conf, tableName);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	public byte[] generateRowKey(double latUpLeft, double lonUpLeft, double LatBtmRight,
			double lonBtmRight){
		String rowKey = latUpLeft + Constants.SEPARATER + lonUpLeft + Constants.SEPARATER
				+ LatBtmRight + Constants.SEPARATER + lonBtmRight;
		
		return rowKey.getBytes();
	}
	
	/**
	 * 
	 * 根据经纬度找到其对应的格子编号
	 * @param lat
	 * @param lon
	 * 因为格子的对角线上的经纬度有一定的规律
	 * 左上角：（39.958713,116.358413）
	 * 右下角：（39.959713,116.357413）
	 * @retune String 返回格子id
	 * 都是精确到小数点后六位，且最后的三个数都是一定的
	 * */
	public String findGridId(double lat, double lon) throws IOException{
		double[] gridxy = GridPoiTable.computeGridxy(lat, lon);
		
		//System.out.println(latUpLeft + "  " + lonUpLeft + "  " + latBtmRight + "  " + lonBtmRight);
		List<byte[]> famList = new ArrayList<byte[]>();
		famList.add(INFO_CF);
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column(INFO_CF, GRID_COL));
		Result rs = get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]), 
				null, famList, columns, false);
		
		
		String gridId = null;
		for(KeyValue kv : rs.list()){
			gridId = Bytes.toString(kv.getValue());
		}
		
		System.out.println(gridId);
		return gridId;
		
		
		
		
	}
	
	
	
	
	/**
	 * 从文件中导入数据到grid表中
	 * 
	 * 
	 **/
	public void importFormFile(File file){
		BufferedReader reader = null;
		
		try{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			
			while((tempString = reader.readLine()) != null){
				String[] line = tempString.split("\t");
				
				String gridId = line[0];
				double latUpLeft = Double.parseDouble(line[1]);
				double lonUpLeft = Double.parseDouble(line[2]);
				double latBtmRight = Double.parseDouble(line[3]);
				double lonBtmRight = Double.parseDouble(line[4]);
				
				List<KeyValue> keyValues = new ArrayList<KeyValue>();
				
				keyValues.add(new KeyValue(generateRowKey(latUpLeft, lonUpLeft, latBtmRight, lonBtmRight), 
						INFO_CF, GRID_COL, Bytes.toBytes(gridId)));
				put(generateRowKey(latUpLeft, lonUpLeft, latBtmRight, lonBtmRight), null, keyValues);
				
				
				
			}
			reader.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * @author ytt 
	 * 在某一个时刻，一个格子，可能要去那个格子的用户列表
	 * @param string gridId
	 * @param timestamp 时间
	 * @param list<String> users 代表预测的用户列表 
	 * @return {@link List}list<String> {usrID1,userID2....}
	 * */
	public boolean setPredictUsers(String gridId, Date timestamp,List<String> users){
		byte[] rowKey = null;
		SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter(INFO_CF, GRID_COL, CompareOp.EQUAL, 
				Bytes.toBytes(gridId));
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column(INFO_CF, GRID_COL));
		FilterList filterList = new FilterList();
		filterList.addFilter(singleColumnValueFilter);
		ResultScanner resultScanner = this.scan(null, null, columns, filterList);
		for(Result result : resultScanner){
			for(KeyValue keyValue : result.list()){
				rowKey = keyValue.getRow();
			}
		}
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		keyValues.add(new KeyValue(rowKey, INFO_CF, PREDICT_COL,
				Bytes.toBytes(users.toString())));
		return this.put(rowKey, timestamp, keyValues);
	}
	
	
	
	/**
	 * @author ytt
	 * 根据某经纬度，来查预测的用户列表
	 * 2015/5/28
	 * 
	 * 
	 * */
	public List<String> getAllUserNames4GPS(double lon, double lat){
		List<String> userStrings = new ArrayList<String>();
		double[] blonlat = convertXY.gpsTobaiducor(lon, lat);
		double blon = blonlat[0];
		double blat = blonlat[1];
		double[] gridxy = GridPoiTable.computeGridxy(blat, blon);
		List<byte[]> famList = new ArrayList<byte[]>();
		famList.add(INFO_CF);
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column(INFO_CF, PREDICT_COL));
		//System.out.println(latUpLeft + "  " + lonUpLeft + "  " + latBtmRight + "  " + lonBtmRight);
		Result rs = get(generateRowKey(gridxy[0],gridxy[1],gridxy[2],gridxy[3]), 
				null, famList, columns, false);
		if(rs.list() == null) {
			System.out.println(rs);
			return null;
		} 
	
		for(KeyValue keyValue : rs.list()){
			String users = Bytes.toString(keyValue.getValue());
			String[] strings = users.substring(1, users.length()-1).split(",");
			for(String userString : strings){
				userStrings.add(userString);
			}
			
		}
		System.out.println(userStrings + "998uhuihuihguihui");
		return userStrings;
	}
	
	
	public static void main(String[] args) throws IOException{
		GridTable gridTable = new GridTable();
		//File file = new File("resource/txt/grid_xy.txt");
		//gridTable.importFormFile(file);
		
		//gridTable.findGridId(39.958782, 116.357793);
	/*	List<String> userStrings = new ArrayList<String>();
		userStrings.add("678");
		gridTable.setPredictUsers("224", null, userStrings);*/
		List<String> userStrings = gridTable.getAllUserNames4GPS(116.3524, 39.9700);
		if(userStrings == null) {
			System.out.println(userStrings);
		}else {
			for(String userString : userStrings){
				System.out.println(userString);
			}
		}
		
	}
	
	
}
