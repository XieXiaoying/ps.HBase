package Incentives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
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
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.jruby.ext.posix.FreeBSDHeapFileStat.time_t;

import Common.AbstractTable;
import Common.Column;
import collection.Constants;

public class TaskTable extends AbstractTable {
	static Logger logger = Logger.getLogger(TaskTable.class);
	
	private static final String tableName = "task";
	
	private static final byte[] INFO_CF = "info".getBytes();
	private static final byte[] PROPERTIES_COL = "proper".getBytes();
	
	private static final byte[] STATE_CF = "state".getBytes();
	private static final byte[] TASKPHASE_COL = "taskphase".getBytes();
	private static final byte[] PARTICIPANTS_COL = "participants".getBytes();
	private static final byte[] PARTICIPANTPER_COL= "participantPerRound".getBytes();
	private static final byte[] ROUNDTASKINFO_COL = "roundTaskInfo".getBytes();
	private static final byte[] ROUNDTASKSTATUS_COL = "roundTaskStatus".getBytes();
	
	private static final byte[] BID_CF = "bid".getBytes();	
	public TaskTable(){
		try {
			HBaseAdmin hBaseAdmin = new HBaseAdmin(Constants.conf);
			if(hBaseAdmin.tableExists(tableName)){
				//do nothing;
			}
			else {
				HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(INFO_CF);
				HColumnDescriptor hColumnDescriptor2 = new HColumnDescriptor(STATE_CF);
				HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
				HColumnDescriptor hColumnDescriptor3 = new HColumnDescriptor(BID_CF);
				hTableDescriptor.addFamily(hColumnDescriptor);
				hTableDescriptor.addFamily(hColumnDescriptor2);
				hTableDescriptor.addFamily(hColumnDescriptor3);
				
				hBaseAdmin.createTable(hTableDescriptor);
				
				
			}
			hBaseAdmin.close();
			hTable = new HTable(Constants.conf, tableName);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	public byte[] generateRowkey(double lat, double lon, double radius, Date timestamp){
		String rowKey = lat + Constants.SEPARATER + lon + Constants.SEPARATER + radius + 
				Constants.SEPARATER + timestamp.getTime();
		return Bytes.toBytes(rowKey);
	}
	
	/*public byte[] generateRowKey(Task task){
		return generateRowkey(task.getLat(), task.getLon(), task.getRadius());
	}
	*/
	
	
	/**
	 * @author ytt
	 * 插入操作，新建一个任务
	 * {@link time_t} 2015/5/22
	 * */
	public boolean createTask(Task task, Date timestamp){
		
		if(timestamp == null){
			timestamp = new Date();	
		}
		
		byte[] rowKey = generateRowkey(task.getLat(), task.getLon(), task.getRadius(), timestamp);
		
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		JSONObject properJsonObject = new JSONObject();
		properJsonObject.accumulate("taskType", task.getTask_type());
		properJsonObject.accumulate("incentiveType", task.getIncentive_type());
		properJsonObject.accumulate("budget", task.getBudget());
		properJsonObject.accumulate("dataNumber", task.getData_number());
		properJsonObject.accumulate("beginTime", task.getBegin_time());
		properJsonObject.accumulate("biddingDeadline", task.getBidding_deadling());
		properJsonObject.accumulate("endTime", task.getEnd_time());
		properJsonObject.accumulate("taskDescription", task.getTask_description());
		properJsonObject.accumulate("taskId", Bytes.toString(rowKey));
		keyValues.add(new KeyValue(rowKey, INFO_CF, PROPERTIES_COL, Bytes.toBytes(properJsonObject.toString())));
		
		keyValues.add(new KeyValue(rowKey, STATE_CF, TASKPHASE_COL, Bytes.toBytes(task.getTask_phase())));
		JSONArray jsonArray = new JSONArray();
		for(String string : task.getParticipants()){
			jsonArray.add(string);
		}
		keyValues.add(new KeyValue(rowKey, STATE_CF, PARTICIPANTS_COL, Bytes.toBytes(jsonArray.toString())));

		
		return this.put(rowKey, timestamp, keyValues);
	}
	
	/**
	 * @author
	 * 插入操作，新建一个周期任务
	 * {@link time_t} 2015/5/22
	 * */
	public boolean createRoundTask(Task task, Date timestamp){
		
		if(timestamp == null){
			timestamp = new Date();	
		}
		
		byte[] rowKey = generateRowkey(task.getLat(), task.getLon(), task.getRadius(), timestamp);
		
		//生成proper
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		JSONObject properJsonObject = new JSONObject();
		properJsonObject.accumulate("taskType", task.getTask_type());
		properJsonObject.accumulate("incentiveType", task.getIncentive_type());
		properJsonObject.accumulate("budget", task.getBudget());
		properJsonObject.accumulate("dataNumber", task.getData_number());
		properJsonObject.accumulate("beginTime", task.getBegin_time());
		properJsonObject.accumulate("biddingDeadline", task.getBidding_deadling());
		properJsonObject.accumulate("endTime", task.getEnd_time());
		properJsonObject.accumulate("taskDescription", task.getTask_description());
		properJsonObject.accumulate("totalRound", task.getRound());
		properJsonObject.accumulate("taskId", Bytes.toString(rowKey));
		keyValues.add(new KeyValue(rowKey, INFO_CF, PROPERTIES_COL, Bytes.toBytes(properJsonObject.toString())));
		
		//生成taskPhase
		keyValues.add(new KeyValue(rowKey, STATE_CF, TASKPHASE_COL, Bytes.toBytes(task.getTask_phase())));
		
		//生成participants
		JSONArray jsonArray = new JSONArray();
		for(String string : task.getParticipants()){
			jsonArray.add(string);
		}
		keyValues.add(new KeyValue(rowKey, STATE_CF, PARTICIPANTS_COL, Bytes.toBytes(jsonArray.toString())));
		
		//生成particitpantsPerRound
		JSONArray jsonArray2 = new JSONArray();
		for(String string : task.getParticipantsPerRound()){
			jsonArray2.add(string);
		}
		keyValues.add(new KeyValue(rowKey, STATE_CF, PARTICIPANTPER_COL, Bytes.toBytes(jsonArray2.toString())));

		//生成roundInfo
		List<InfoPerRound> roundInfo = task.getRoundInfo();
//		roundInfo.add(new InfoPerRound(0,0,0,0));
//		roundInfo.add(new InfoPerRound(2,2,2,2));
		JSONArray roundInfoArray = new JSONArray();
		for(int i=0;i<roundInfo.size();i++){
			
			JSONObject item = new JSONObject();
			item.accumulate("xiPerRound", roundInfo.get(i).xiPerRound);
			item.accumulate("diPerRound", roundInfo.get(i).diPerRound);
			item.accumulate("pPerRound", roundInfo.get(i).pPerRound);
			item.accumulate("budgetPerRound", roundInfo.get(i).budgetPerRound);
			
			roundInfoArray.add(item);
		}
//		System.out.println(roundInfoArray);
		keyValues.add(new KeyValue(rowKey, STATE_CF, ROUNDTASKINFO_COL, Bytes.toBytes(roundInfoArray.toString())));
		
		//生成roundTaskStatus
		JSONObject roundJsonObject = new JSONObject();
		roundJsonObject.accumulate("thisRound", task.getThisRound());
		roundJsonObject.accumulate("budgetThisRound", task.getBudgetThisRound());
		roundJsonObject.accumulate("budgetRemains", task.getBudgetRemains());
		roundJsonObject.accumulate("data_numberThisRound", task.getDataThis());
		roundJsonObject.accumulate("data_numberRemains", task.getDataRemains());
		keyValues.add(new KeyValue(rowKey, STATE_CF, ROUNDTASKSTATUS_COL, Bytes.toBytes(roundJsonObject.toString())));
		
		//全部写入
		return this.put(rowKey, timestamp, keyValues);
	}
	
	
	public boolean modify(String taskID, int taskphase, Task task) {	
		Logger logger = Logger.getLogger(Task.class);
		List<KeyValue> keyValues = new ArrayList<KeyValue>();

		//修改proper
		JSONObject properJsonObject = new JSONObject();
		properJsonObject.accumulate("taskType", task.getTask_type());
		properJsonObject.accumulate("incentiveType", task.getIncentive_type());
		properJsonObject.accumulate("budget", task.getBudget());
		properJsonObject.accumulate("dataNumber", task.getData_number());
		properJsonObject.accumulate("beginTime", task.getBegin_time());
		properJsonObject.accumulate("biddingDeadline", task.getBidding_deadling());
		properJsonObject.accumulate("endTime", task.getEnd_time());
		properJsonObject.accumulate("taskDescription", task.getTask_description());
		properJsonObject.accumulate("totalRound", task.getRound());
		properJsonObject.accumulate("taskId", taskID);
		keyValues.add(new KeyValue(Bytes.toBytes(taskID), INFO_CF, PROPERTIES_COL, Bytes.toBytes(properJsonObject.toString())));
		
		//修改taskPhase
		if(taskphase != 0){
			keyValues.add(new KeyValue(Bytes.toBytes(taskID), STATE_CF, 
					TASKPHASE_COL, Bytes.toBytes(taskphase)));
		}
		
		//修改所有参与者
		JSONArray jsonArray = new JSONArray();
		for(String string : task.getParticipants()){
			jsonArray.add(string);
		}
		keyValues.add(new KeyValue(Bytes.toBytes(taskID), STATE_CF, PARTICIPANTS_COL, Bytes.toBytes(jsonArray.toString())));
		
		//修改每一轮参与者
		JSONArray jsonArrayPer = new JSONArray();
		for(String string : task.getParticipantsPerRound()){
			jsonArrayPer.add(string);
		}
		keyValues.add(new KeyValue(Bytes.toBytes(taskID), STATE_CF, PARTICIPANTPER_COL, Bytes.toBytes(jsonArrayPer.toString())));
		
		
		//生成roundInfo
		List<InfoPerRound> roundInfo = task.getRoundInfo();
//		roundInfo.add(new InfoPerRound(0,0,0,0));
//		roundInfo.add(new InfoPerRound(2,2,2,2));
		JSONArray roundInfoArray = new JSONArray();
		for(int i=0;i<roundInfo.size();i++){
					
			JSONObject item = new JSONObject();
			item.accumulate("xiPerRound", roundInfo.get(i).xiPerRound);
			item.accumulate("diPerRound", roundInfo.get(i).diPerRound);
			item.accumulate("pPerRound", roundInfo.get(i).pPerRound);
			item.accumulate("budgetPerRound", roundInfo.get(i).budgetPerRound);
					
			roundInfoArray.add(item);
		}
//		System.out.println(roundInfoArray);
		keyValues.add(new KeyValue(Bytes.toBytes(taskID), STATE_CF, ROUNDTASKINFO_COL, Bytes.toBytes(roundInfoArray.toString())));
				
				
				
//		// 生成roundInfo
//		JSONArray roundInfoArray = new JSONArray();
//		roundInfoArray = JSONArray.fromObject(task.getRoundInfo());
//		keyValues.add(new KeyValue(Bytes.toBytes(taskID), STATE_CF, ROUNDTASKINFO_COL, Bytes.toBytes(roundInfoArray.toString())));

		// 生成roundTaskStatus
		JSONObject roundJsonObject = new JSONObject();
		roundJsonObject.accumulate("thisRound", task.getThisRound());
		roundJsonObject.accumulate("budgetThisRound", task.getBudgetThisRound());
		roundJsonObject.accumulate("budgetRemains", task.getBudgetRemains());
		roundJsonObject.accumulate("data_numberThisRound", task.getDataThis());
		roundJsonObject.accumulate("data_numberRemains", task.getDataRemains());
		keyValues.add(new KeyValue(Bytes.toBytes(taskID), STATE_CF, ROUNDTASKSTATUS_COL, Bytes.toBytes(roundJsonObject.toString())));

		return this.put(Bytes.toBytes(taskID), null, keyValues);
	}
	
	/**
	 * @author ytt
	 * 改变TaskPhase或者改变participants
	 * */
	public boolean modify(String taskId, int taskphase, List<String> participants,Date timestamp){
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		if(taskphase != 0){
			keyValues.add(new KeyValue(Bytes.toBytes(taskId), STATE_CF, 
					TASKPHASE_COL, Bytes.toBytes(taskphase)));
			
		}
		if(participants != null){
			JSONArray jsonArray = new JSONArray();
			for(String string : participants){
				jsonArray.add(string);
			}
			keyValues.add(new KeyValue(Bytes.toBytes(taskId), STATE_CF, 
					PARTICIPANTS_COL, Bytes.toBytes(jsonArray.toString())));
			
		}
		return this.put(Bytes.toBytes(taskId), timestamp, keyValues);
	}
	

	
	public boolean modify(String taskId, int taskphase, List<String> participants, List<String> participantsPerRound, Date timestamp){
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		if(taskphase != 0){
			keyValues.add(new KeyValue(Bytes.toBytes(taskId), STATE_CF, 
					TASKPHASE_COL, Bytes.toBytes(taskphase)));
			
		}
		if(participants != null){
			JSONArray jsonArray = new JSONArray();
			for(String string : participants){
				jsonArray.add(string);
			}
			keyValues.add(new KeyValue(Bytes.toBytes(taskId), STATE_CF, 
					PARTICIPANTS_COL, Bytes.toBytes(jsonArray.toString())));
			
		}
		if(participantsPerRound != null){
			JSONArray jsonArray = new JSONArray();
			for(String string : participantsPerRound){
				jsonArray.add(string);
			}
			keyValues.add(new KeyValue(Bytes.toBytes(taskId), STATE_CF, 
					PARTICIPANTPER_COL, Bytes.toBytes(jsonArray.toString())));
			
		}
		return this.put(Bytes.toBytes(taskId), timestamp, keyValues);
	}
	
	
	public boolean modifyTaskPhase(String taskId, int taskphase){
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		if(taskphase != 0){
			keyValues.add(new KeyValue(Bytes.toBytes(taskId), STATE_CF, 
					TASKPHASE_COL, Bytes.toBytes(taskphase)));
			
		}
		
		return this.put(Bytes.toBytes(taskId), null, keyValues);
	}
	
	/**
	 * @author ytt
	 * 根据taskPhase来返回task行健列表
	 * 
	 * */
	public List<byte[]> getTasksByTaskphase(int taskPhase){
		List<byte[]> tasks = new ArrayList<byte[]>();
		
		SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter(STATE_CF, 
				TASKPHASE_COL, CompareOp.EQUAL, Bytes.toBytes(taskPhase));
		FilterList f = new FilterList();
		f.addFilter(singleColumnValueFilter);
		List<byte[]> famList = new ArrayList<byte[]>();
		List<Column> columns = new ArrayList<Column>();
		famList.add(STATE_CF);
		columns.add(new Column(STATE_CF, TASKPHASE_COL));
		ResultScanner resultScanner = this.scan(null, famList, columns, f);
		
		for(Result result : resultScanner){
			for(KeyValue keyValue : result.list()){
				tasks.add(keyValue.getRow());
			}
		}
		return tasks;
	}
	
	/**
	 * @
	 * 返回具体的Task
	 * @param byte[] rowKey
	 * @return {@link JSONObject}
	 * 2015/5/22
	 * 
	 * */
	public Task getTaskInfo(byte[] rowKey){
		Task task = new Task();
		List<byte[]> famList = new ArrayList<byte[]>();
		List<Column> columns = new ArrayList<Column>();
		famList.add(INFO_CF);
		famList.add(STATE_CF);
		columns.add(new Column(INFO_CF, PROPERTIES_COL));
		columns.add(new Column(STATE_CF, TASKPHASE_COL));
		columns.add(new Column(STATE_CF, PARTICIPANTS_COL));
		columns.add(new Column(STATE_CF, PARTICIPANTPER_COL));
		columns.add(new Column(STATE_CF, ROUNDTASKINFO_COL));
		columns.add(new Column(STATE_CF, ROUNDTASKSTATUS_COL));
		ResultScanner resultScanner = this.scan(null, famList, columns, null);
		
		
		Get get = new Get(rowKey);
		get.addColumn(INFO_CF, PROPERTIES_COL);
		try {
			if(!this.rowExists(get)){
				return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Result result = this.get(rowKey, null, famList, columns, false);
		
		String[] rows = Bytes.toString(rowKey).split(Constants.SEPARATER);
		task.setLat(Double.valueOf(rows[0]));
		task.setLon(Double.valueOf(rows[1]));
		task.setRadius(Double.valueOf(rows[2]));
		
		
		
		for(KeyValue keyValue : result.list()){
			if(Bytes.toString(keyValue.getQualifier()).equals("proper")){
//				System.out.println("找到proper");
				JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(keyValue.getValue()));
				task.setTask_type(jsonObject.getInt("taskType"));
				task.setIncentive_type(jsonObject.getInt("incentiveType"));
				task.setBudget(jsonObject.getDouble("budget"));
				task.setData_number(jsonObject.getInt("dataNumber"));
				task.setBegin_time(jsonObject.getLong("beginTime"));
				task.setBidding_deadling(jsonObject.getLong("biddingDeadline"));
				task.setEnd_time(jsonObject.getLong("endTime"));
				task.setTask_description(jsonObject.getString("taskDescription"));
				task.setTask_ID(jsonObject.getString("taskId"));
				if(jsonObject.has("totalRound"))task.setRound(jsonObject.getInt("totalRound"));
			}
			else if(Bytes.toString(keyValue.getQualifier()).equals("taskphase")){
				//System.out.println(keyValue.getTimestamp());
//				System.out.println("找到taskphase");
				 task.setTask_phase( Bytes.toInt(keyValue.getValue()));
			}
			else if(Bytes.toString(keyValue.getQualifier()).equals("participants")){
//				System.out.println("找到participants");
				JSONArray paticipans = JSONArray.fromObject(Bytes.toString(keyValue.getValue()));
				//System.out.println(paticipans.toString());
				List<String> paticipantsList = new ArrayList<String>();
				for(Object paticipant : paticipans){
					String pString = (String)paticipant;
					paticipantsList.add(pString);
				}
				task.setParticipants(paticipantsList);
				
			}else if(Bytes.toString(keyValue.getQualifier()).equals("participantPerRound")){
//				System.out.println("找到participantPerRound");
				JSONArray paticipanPer = JSONArray.fromObject(Bytes.toString(keyValue.getValue()));
				//System.out.println(paticipans.toString());
				List<String> paticipantsList = new ArrayList<String>();
				for(Object paticipant : paticipanPer){
					String pString = (String)paticipant;
					paticipantsList.add(pString);
				}
				task.setParticipantsPerRound(paticipantsList);
				
			}else if(Bytes.toString(keyValue.getQualifier()).equals("roundTaskInfo")){
//				System.out.println("找到roundTaskInfo");
				JSONArray taskInfoArray = JSONArray.fromObject(Bytes.toString(keyValue.getValue()));
				
				
				List<InfoPerRound> infoPerRoundList = new ArrayList<InfoPerRound>();
				for(int i=0; i<taskInfoArray.size();i++){
					JSONObject infoObject = new JSONObject();
					infoObject = (JSONObject)taskInfoArray.get(i);
					double x = infoObject.getDouble("xiPerRound");
					double d = infoObject.getDouble("diPerRound");
					int p = infoObject.getInt("pPerRound");
					double b = infoObject.getDouble("budgetPerRound");
//					System.out.println("xiPerRound:"+x+"\ndiPerRound:"+d+"\npPerRound:"+p+"\nbudgetPerRound:"+b);
					InfoPerRound info = new InfoPerRound(x, d, p, b);
//					InfoPerRound info = new InfoPerRound(1, 1, 0, 8.25);
					infoPerRoundList.add(info);
				}
				
				task.setRoundInfo(infoPerRoundList);
				
			}else if(Bytes.toString(keyValue.getQualifier()).equals("roundTaskStatus")){
//				System.out.println("找到roundTaskStatus");
				JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(keyValue.getValue()));
				task.setThisRound(jsonObject.getInt("thisRound"));
				task.setBudgetThisRound(jsonObject.getDouble("budgetThisRound"));
				task.setBudgetRemains(jsonObject.getDouble("budgetRemains"));
				task.setDataThis(jsonObject.getInt("data_numberThisRound"));
				task.setDataRemains(jsonObject.getInt("data_numberRemains"));
				
			}
		}
		
		
		
		return task;		
	}
	
	/**
	 * @author ytt
	 * 输入一个用户竞价
	 * 
	 * */
	public boolean insertBid(String taskId, String userId, double bidPrice, Date timestamp){
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		byte[] qualifier = userId.getBytes();
		JSONObject bidJsonObject = new JSONObject();
		bidJsonObject.accumulate("userId", userId);
		bidJsonObject.accumulate("bidPrice", bidPrice);
		keyValues.add(new KeyValue(Bytes.toBytes(taskId), BID_CF, qualifier, Bytes.toBytes(bidJsonObject.toString())));
		
		return this.put(Bytes.toBytes(taskId), timestamp, keyValues);
	}
	
	/**
	 * @author ytt
	 * 按照bidPrice排序
	 * */
	public static void sort(List<JSONObject> data){
		Collections.sort(data, new Comparator<JSONObject>(){
			public int compare(JSONObject jo1, JSONObject jo2){
				Double a = jo1.getDouble("bidPrice");
				Double b = jo2.getDouble("bidPrice");
				
				return a.compareTo(b);
			}
		});
	}
	/**
	 * @author ytt
	 * 找到前n个最低投标者
	 * {userId: {@link String},bidPrice: {@link Double}}
	 * 
	 * */
	public List<JSONObject> getBids(String taskId, int n){
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
	    Scan scan = new Scan();
	    scan.setStartRow(Bytes.toBytes(taskId));
	    scan.setStopRow(Bytes.toBytes(taskId + 1));
	    
	    List<byte[]> famList = new ArrayList<byte[]>();
	    famList.add(BID_CF);
	    
	    
	    
	    ResultScanner rScanner = this.scan(scan, famList, null, null);
	    
	    for(Result result : rScanner){
	    	for(KeyValue keyValue : result.list()){
				jsonObjects.add(JSONObject.fromObject(Bytes.toString(keyValue.getValue())));
				
			}
	    }
		
//		System.out.println(jsonObjects.toString());
		sort(jsonObjects);
//		System.out.println(jsonObjects.toString());
		if(n >= jsonObjects.size()){
			n = jsonObjects.size();
		}
		 return jsonObjects.subList(0, n);
	}
	
	/**
	 * @author ytt
	 * 找到前所有投标者
	 * {userId: {@link String},bidPrice: {@link Double}}
	 * 
	 * */
	public List<JSONObject> getAllBids(String taskId){
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
	    Scan scan = new Scan();
	    scan.setStartRow(Bytes.toBytes(taskId));
	    scan.setStopRow(Bytes.toBytes(taskId + 1));
	    
	    List<byte[]> famList = new ArrayList<byte[]>();
	    famList.add(BID_CF);
	    
	    
	    
	    ResultScanner rScanner = this.scan(scan, famList, null, null);
	    
	    for(Result result : rScanner){
	    	for(KeyValue keyValue : result.list()){
				jsonObjects.add(JSONObject.fromObject(Bytes.toString(keyValue.getValue())));
				
			}
	    }
		
//		System.out.println(jsonObjects.toString());
		sort(jsonObjects);
//		System.out.println(jsonObjects.toString());
//		if(n >= jsonObjects.size()){
//			n = jsonObjects.size();
//		}
		 return jsonObjects;
	}
	
	public static void changeTable() {
		try{
			// Instantiating configuration object
			Configuration conf = HBaseConfiguration.create();
			// Instantiating HBaseAdmin class
			HBaseAdmin admin = new HBaseAdmin(conf); 
			admin.addColumn("employee", new HColumnDescriptor("columnDescriptor"));
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * @author ytt
	 * 返回未选择的用户的所有报价
	 * {userId: {@link String},bidPrice: {@link Double}}
	 * 
	 * */
	public List<JSONObject> getFaildBids(String taskId, int n){
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
	    Scan scan = new Scan();
	    scan.setStartRow(Bytes.toBytes(taskId));
	    scan.setStopRow(Bytes.toBytes(taskId + 1));
	    
	    List<byte[]> famList = new ArrayList<byte[]>();
	    famList.add(BID_CF);
	    
	    
	    
	    ResultScanner rScanner = this.scan(scan, famList, null, null);
	    
	    for(Result result : rScanner){
	    	for(KeyValue keyValue : result.list()){
				jsonObjects.add(JSONObject.fromObject(Bytes.toString(keyValue.getValue())));
				
			}
	    }
		
//	    System.out.println(jsonObjects.toString());
		sort(jsonObjects);
//		System.out.println(jsonObjects.toString());
		if(n >= jsonObjects.size()){
			return null;
			
		}
		return jsonObjects.subList(n+1,jsonObjects.size());
	}
	 
	public static void main(String[] args){
		TaskTable taskTable = new TaskTable();
		taskTable.modifyTaskPhase("39.962612/116.35228/300.0/1468568373644",4);
		
//		TaskTable taskTable = new TaskTable();
//		
//		Task aTask = new Task();
//		aTask.setTask_type(3);
//		aTask.setIncentive_type(3);
//		aTask.setBudget(121);
//		aTask.setData_number(12);
//		aTask.setTask_phase(1);
//		
//		aTask.setLat(39.953311);
//		aTask.setLon(120);
//		aTask.setRadius(1000);
//		
//		aTask.setTask_description("");
//		int bidMinute = 60;
//		aTask.setBegin_time(1463139432);
//		aTask.setEnd_time(1463219990);
//		aTask.setBidding_deadling(bidMinute*60*1000+1463139432);//计算竞价任务的竞价结束时间点
//		aTask.setRound(5);
//		
//		
//				
//		aTask.setThisRound(1);
//		aTask.setBudgetThisRound(111/5*0.5);
//		aTask.setBudgetRemains(111);
//		aTask.setDataThis(2);
//				
//		aTask.setDataRemains(11);
//				
//		List<InfoPerRound> roundInfo = new ArrayList<InfoPerRound>();
//		roundInfo.add(new InfoPerRound(1, 2, 3, 4));
//		aTask.setRoundInfo(roundInfo);
//		
//		
//		taskTable.createRoundTask(aTask,null);
//			
		
		//List<Task> tasks = Task.tasks4test(10);
		//测试新建一个任务
		/*for(Task task : tasks){
			taskTable.createTask(task, null);
		}
		*/
		/*Date time = new Date();
		Random random = new Random(time.getTime());
		List<byte[]> rows = taskTable.getTasksByTaskphase(4);
		for(byte[] rowKey : rows){
			Task task = taskTable.getTaskInfo(rowKey);
			for(int i = 0; i < 10 ; i++){
		        taskTable.insertBid(task, String.valueOf(random.nextLong()), random.nextDouble(), null);
			}
			List<JSONObject> jsonObjects= taskTable.getBids(task, 2);
			for(JSONObject jsonObject : jsonObjects){
				System.out.println(jsonObject.toString());
			}
			
			
		}*/
		
		//taskTable.getTaskInfo(Bytes.toBytesBinary("39.97/116.3524/100.01"));
		/*List<String> test = new ArrayList<String>();
		test.add("tes1");
		test.add("tes2");
		System.out.println(test.toString());
		*/
	
//		Task aTask = taskTable.getTaskInfo(Bytes.toBytes("40.0039/116.3674/300.0/1433488469275"));
//		List<String> participants = aTask.getParticipants();
//		for (String string : participants) {
//			System.out.println(string);
//		}
//		
//		participants.add("jack");
//		taskTable.modify(aTask.getTask_ID(), aTask.getTask_phase(), participants, null);
//		Task aTask2 = taskTable.getTaskInfo(Bytes.toBytes("40.0039/116.3674/300.0/1433488469275"));
//		List<String> participants2 = aTask.getParticipants();
//		for (String string : participants2) {
//			System.out.println(string);
//		}
	/*	JSONArray jaArray = new JSONArray();
		jaArray.add("tty");
		jaArray.add("lz");
		jaArray.add("jack");
		System.out.println(jaArray.toString());
		JSONArray jaArray2 = JSONArray.fromObject(jaArray.toString());
		List<String> usernames = new ArrayList<>();
	    
		for (Object string : jaArray2) {
			String usernameString = (String)string;
			usernames.add(usernameString);
		}
		System.out.println(usernames.toString());
	
		*/
		
	
	}
}
