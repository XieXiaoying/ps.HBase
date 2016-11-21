package Incentive;

import collection.Constants;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import Common.AbstractTable;
import Common.Column;
import Incentives.*;

//svn test
//i have seen it, from jack(toulaoshi)

public class UserTable extends AbstractTable{
	// 表名
	private static final String tableName = "user";
	// 列族名
	private final static byte[] COLFAM_NAME = "info".getBytes();
	// 密码
	private final static byte[] PWD = "pwd".getBytes();
	// 权限
	private final static byte[] PRIO = "prio".getBytes();
	// 总共获得的激励
	private final static byte[] EARN = "earn".getBytes();
	// 参与次数
	private final static byte[] TIMES = "times".getBytes();
	// 推送token
	private final static byte[] TOKEN = "token".getBytes();
	
	
	private final static byte[] TRANSACTION_CF = "transaction".getBytes();
	
	//private static UserTable instance = null;

	private HTable hTable = null;
	
	/*public static UserTable getInstance(){
		if(instance == null){
			instance = new UserTable();
		}
		return instance;
	}*/
	
/*	public UserTable(){
		try {
			HBaseAdmin hBaseAdmin = new HBaseAdmin(Constants.conf);
		
		HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(COLFAM_NAME);
		HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
		hTableDescriptor.addFamily(hColumnDescriptor);
		
		if(hBaseAdmin.tableExists(tableName)){
			//do nothing
		}else{
			hBaseAdmin.createTable(hTableDescriptor);
			hTable = new HTable(Constants.conf, tableName); 
		}
		
		//Configuration conf = HBaseConfiguration.create();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	public UserTable(){
		Configuration conf = Constants.conf;
		try {
			hTable = new HTable(conf, tableName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 存储用户基本信息
	 * @param username
	 * @param pwd
	 * @param priv
	 * @return
	 */
	public Boolean set(String username,String pwd,int priv,String token){
		Put put = new Put(username.getBytes());
		if(pwd!=null)
		put.add(COLFAM_NAME, PWD, pwd.getBytes());
		if(priv!=-1)
		put.add(COLFAM_NAME,PRIO,Bytes.toBytes(priv));
		if(token!=null)
		put.add(COLFAM_NAME,TOKEN,token.getBytes());
		
		try {
		
			hTable.put(put);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 查找用户密码
	 * @param username
	 * @return
	 */
	public String getPwd(String username){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, PWD);
		
		try {
			Result r = hTable.get(get);
			if(!r.isEmpty()){
				return new String(r.getValue(COLFAM_NAME, PWD));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	
	/**
	 * 为用户username增加earn激励收入
	 * @param username
	 * @param earn
	 * @return
	 */
	// 其实可以写入不同的version呢！！！！！！！！！！！！！！！！！！！！！！！
	public Boolean setEarn(String username,double earn){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, EARN);
		try {
			Result r = hTable.get(get);
			// 有值，value += earn;
			if(r != null && !r.isEmpty()){
				double value = Bytes.toDouble(r.getValue(COLFAM_NAME, EARN));
				DecimalFormat df = new DecimalFormat("0.00");
				value = Double.valueOf(df.format(value));
				earn = Double.valueOf(df.format(earn));
				value += earn;
				
				//重新存回新值
				Put put = new Put(username.getBytes());
				put.add(COLFAM_NAME, EARN, Bytes.toBytes(value));
				hTable.put(put);
			}
			// 还没有值，value设为earn
			else{
				//重新存回新值
				Put put = new Put(username.getBytes());
				DecimalFormat df = new DecimalFormat("0.00");
				earn = Double.valueOf(df.format(earn));
				put.add(COLFAM_NAME, EARN, Bytes.toBytes(earn));
				hTable.put(put);
			}
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 用户的参与次数+1
	 * @param username
	 * @param times
	 * @return
	 */
	// 其实可以写入不同的version呢！！！！！！！！！！！！！！！！！！！！！！！
	public Boolean setTimes(String username){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, TIMES);
		try {
			Result r = hTable.get(get);
			// 有值，value++;
			if(!r.isEmpty()){
				int value = Bytes.toInt(r.getValue(COLFAM_NAME, TIMES));
				value += 1;
				
				//重新存回新值
				Put put = new Put(username.getBytes());
				put.add(COLFAM_NAME, TIMES, Bytes.toBytes(value));
				hTable.put(put);
			}
			// 还没有值，value设为1
			else{
				//重新存回新值
				Put put = new Put(username.getBytes());
				put.add(COLFAM_NAME, TIMES, Bytes.toBytes(1));
				hTable.put(put);
			}
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 为用户username增加earn激励收入，并将参与次数+1
	 * 推荐使用这个函数代替执行上者两个函数，因为只需一次查询和插入
	 * @param username
	 * @param earn
	 * @return
	 */
	public Boolean setEarnAndTimes(String username,double earn){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, EARN);
		get.addColumn(COLFAM_NAME, TIMES);
		try {
			Result r = hTable.get(get);
			// 有值，value += earn;
			if(r != null && !r.isEmpty()){
				double newEarn = Bytes.toDouble(r.getValue(COLFAM_NAME, EARN));
				DecimalFormat df = new DecimalFormat("0.00");
				newEarn = Double.valueOf(df.format(newEarn));
				earn = Double.valueOf(df.format(earn));
				newEarn += earn;
				
				int newTimes = Bytes.toInt(r.getValue(COLFAM_NAME, TIMES));
				newTimes += 1;
				
				//重新存回新值
				Put put = new Put(username.getBytes());
				put.add(COLFAM_NAME, EARN, Bytes.toBytes(newEarn));
				put.add(COLFAM_NAME, TIMES, Bytes.toBytes(newTimes));
				hTable.put(put);
			}
			// 还没有值，value设为earn
			else{
				//重新存回新值
				Put put = new Put(username.getBytes());
				DecimalFormat df = new DecimalFormat("0.00");
				earn = Double.valueOf(df.format(earn));
				put.add(COLFAM_NAME, EARN, Bytes.toBytes(earn));
				put.add(COLFAM_NAME, TIMES, Bytes.toBytes(1));
				hTable.put(put);
			}
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 为用户username增加earn激励收入，并将参与次数+1
	 * 推荐使用这个函数代替执行上者两个函数，因为只需一次查询和插入
	 * @param username
	 * @param earn
	 * @return
	 */
	public Boolean setOriginalEarnAndTimes(String username,double earn, int times){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, EARN);
		get.addColumn(COLFAM_NAME, TIMES);
		try {
			DecimalFormat df = new DecimalFormat("0.00");
			earn = Double.valueOf(df.format(earn));
			Put put = new Put(username.getBytes());
			put.add(COLFAM_NAME, EARN, Bytes.toBytes(earn));
			put.add(COLFAM_NAME, TIMES, Bytes.toBytes(1));
			hTable.put(put);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 获得用户的总收益
	 * @param username
	 * @return
	 */
	public double getEarn(String username){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, EARN);
		
		try {
			Result r = hTable.get(get);
			if(!r.isEmpty()){
				byte[] earn;
				if((earn = r.getValue(COLFAM_NAME, EARN)) != null){
					return Bytes.toDouble(earn);
				}
				else{
					return (double)0;
				}
				
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (double)0;
	}
	
	/**
	 * 获得用户的参与次数
	 * @param username
	 * @return
	 */
	public int getTimes(String username){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, TIMES);
		
		try {
			Result r = hTable.get(get);
			if(!r.isEmpty()){
				byte[] times;
				if((times = r.getValue(COLFAM_NAME, TIMES)) != null){
					return Bytes.toInt(times);
				}
				else{
					return 0;
				}
				
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public Boolean delete(String username){
		Delete del = new Delete(username.getBytes());
		try {
			hTable.delete(del);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 获得用户的推送token
	 * @param username
	 * @return
	 */
	public String getToken(String username){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, TOKEN);
		
		try {
			Result r = hTable.get(get);
			if(!r.isEmpty()){
				return new String(r.getValue(COLFAM_NAME, TOKEN));			
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public Boolean setToken(String userName, String token) {
		Put put = new Put(userName.getBytes());
		put.add(COLFAM_NAME, TOKEN, token.getBytes());
		try {
			hTable.put(put);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public int getPrio(String username){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, PRIO);
		
		try {
			Result r = hTable.get(get);
			if(!r.isEmpty()){
				return Bytes.toInt(r.getValue(COLFAM_NAME, PRIO));			
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	
	public boolean setPrio(String userName, int prio) {
		Put put = new Put(userName.getBytes());
		put.add(COLFAM_NAME, PRIO, Bytes.toBytes(prio));
		try {
			hTable.put(put);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @author ytt
	 * 输入一条交易记录
	 * 
	 * */
	public boolean setTransation(String userId, String taskId, double earn, int phase, Date timestamp){
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		Transaction transaction = new Transaction();
		transaction.setEarn(earn);
		transaction.setTaskID(taskId);
		transaction.setTransactionPhase(phase);
		transaction.setUserID(userId);
		
		String transationId = userId + Constants.SEPARATER + taskId;
		transaction.setTransactionID(transationId);
		
		Put put = new Put(Bytes.toBytes(userId));
		put.add(TRANSACTION_CF, Bytes.toBytes(taskId), Bytes.toBytes(transaction.toString()));
		try {
			hTable.put(put);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean setTransation(String userId, String taskId, double earn, double bonus, int phase, Date timestamp){
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		Transaction transaction = new Transaction();
		transaction.setEarn(earn);
		transaction.setBonus(bonus);
		transaction.setTaskID(taskId);
		transaction.setTransactionPhase(phase);
		transaction.setUserID(userId);
		
		String transationId = userId + Constants.SEPARATER + taskId;
		transaction.setTransactionID(transationId);
		
		Put put = new Put(Bytes.toBytes(userId));
		put.add(TRANSACTION_CF, Bytes.toBytes(taskId), Bytes.toBytes(transaction.toString()));
		try {
			hTable.put(put);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @author ytt
	 * 返回用户的所有的交易记录
	 * @return list<JSONObject>
	 * */
	public List<Transaction> getAllTransactions(String userId){
		List<Transaction> lists = new ArrayList<Transaction>();
		Scan scan = new Scan();
		scan.setStartRow(Bytes.toBytes(userId));
		scan.setStopRow(Bytes.toBytes(userId + 1));
		scan.addFamily(TRANSACTION_CF);
		
		
		ResultScanner resultScanner;
		try {
			resultScanner = hTable.getScanner(scan);
			for(Result result : resultScanner){
				
				for(KeyValue keyValue : result.list()){
					Transaction transaction = new Transaction();
					JSONObject jsonObject = JSONObject.fromObject(Bytes.toString(keyValue.getValue()));
					transaction.setEarn(jsonObject.getDouble("earn"));
					transaction.setBonus(jsonObject.getDouble("bonus"));
					transaction.setTaskID(jsonObject.getString("taskID"));
					transaction.setTransactionID(jsonObject.getString("transactionID"));
					transaction.setTransactionPhase(jsonObject.getInt("transactionPhase"));
					transaction.setUserID(jsonObject.getString("userID"));
					lists.add(transaction);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lists;
	}
	
	
	
	/***
	 * @author ytt 
	 * 查询某个用户的某一个阶段的所有交易记录
	 * 
	 * */
	public List<Transaction> getAllTransationByPhase(String userId, int phase){
		List<Transaction> result = new ArrayList<Transaction>();
		List<Transaction> transactions = getAllTransactions(userId);
		for(Transaction transaction : transactions){
			if(transaction.getTransactionPhase() == phase){
				result.add(transaction);
			}
		}
		return result;
	}
	
	/**
	 * @author ytt
	 * 修改一个交易的阶段值
	 * 
	 * */
	public boolean modifyTransationPhase(String transation, int phase, Date timestamp){
		int Sepindex = transation.indexOf(Constants.SEPARATER);
		String userId = transation.substring(0, Sepindex);
		String taskId = transation.substring(Sepindex+1);
		Transaction result = null;
		List<Transaction> transactions = this.getAllTransactions(userId);
		for(Transaction transaction : transactions){
			if(transaction.getTaskID().equals(taskId)){
				result = transaction;
			}
		}
		return this.setTransation(userId, taskId, result.getEarn(), phase, timestamp);
	}
	
	
	/**
	 * @author zch
	 * 修改一个交易的奖金
	 * 
	 * */
	public boolean setTransationBonus(String userId,String taskId, double bonus, int phase,Date timestamp){
		Transaction result = null;
		List<Transaction> transactions = this.getAllTransactions(userId);
		for(Transaction transaction : transactions){
			if(transaction.getTaskID().equals(taskId)){
				result = transaction;
			}
		}
		return this.setTransation(userId, taskId, result.getEarn(), bonus,phase, timestamp);
	}
	
	
	/**
	 * @author ytt
	 * 获取所有的用户名称
	 * 
	 * 
	 * */
	public List<String> getAllUserNames(){
		List<String> lists = new ArrayList<String>();
		List<byte[]> famList = new ArrayList<byte[]>();
		famList.add(COLFAM_NAME);
		List<Column> colList = new ArrayList<Column>();
		colList.add(new Column(COLFAM_NAME, PWD));
		try{
			ResultScanner rScanner = hTable.getScanner(COLFAM_NAME, PWD);
			for(Result result : rScanner){
				for(KeyValue keyValue : result.list()){
					lists.add(Bytes.toString(keyValue.getRow()));
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return lists;
	}
	
	
	
	
	public static void main(String[] args){
		UserTable userTable = new UserTable();
//		userTable.setOriginalEarnAndTimes("zch",0,0);
		
//		
//		List<String> userStrings = userTable.getAllUserNames();
//		for(String user : userStrings){
//			userTable.setOriginalEarnAndTimes("zch",0,0);
//			//System.out.println(user);
//		}
		
		
		userTable.setTransation("xiaobingkuaier", "", 30.0, 9, null);
//		List<Transaction> transactions = userTable.getAllTransactions("jack");
//		//List<Transaction> list= userTable.getAllTransationByPhase("jack", 3);
//		for(Transaction transaction : transactions){
//			
//			System.out.println(transaction.toString());
//		}
	}
	

}
