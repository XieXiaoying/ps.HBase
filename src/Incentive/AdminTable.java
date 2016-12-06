package Incentive;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.NullComparator;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import collection.Constants;

import Common.AbstractTable;

public class AdminTable extends AbstractTable{
	private static final String tableName = "admin";
	private final static byte[] COLFAM_NAME = "info".getBytes();
	private final static byte[] PWD = "password".getBytes();
	private final static byte[] EMAIL = "email".getBytes();
	private final static byte[] TEL = "tel".getBytes();
	private HTable hTable = null;
	public AdminTable(){
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
	public boolean set(String username,String pwd,String email,String tel){
		Put put = new Put(username.getBytes());
		if(pwd!=null)
		put.add(COLFAM_NAME, PWD, pwd.getBytes());
		if(email!=null)
		put.add(COLFAM_NAME,EMAIL,email.getBytes());
		if(tel!=null)
		put.add(COLFAM_NAME,TEL,tel.getBytes());
		
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
	 * 查找用户邮箱
	 * @param username
	 * @return
	 */
	public String getEmail(String username){
		Get get = new Get(username.getBytes());
		get.addColumn(COLFAM_NAME, EMAIL);
		
		try {
			Result r = hTable.get(get);
			if(!r.isEmpty()){
				return new String(r.getValue(COLFAM_NAME, EMAIL));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	/**
	 * 查找邮箱是否存在,并返回用户名，用于邮件发送及跳转链接
	 */
	public String getUsernameByEmail(String email) {
		Filter filter = new ValueFilter(CompareFilter.CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(email)));
		Scan scan = new Scan();
		scan.setFilter(filter);
		String userName = null;
		try {
			ResultScanner scanner = hTable.getScanner(scan);
			for(Result result : scanner) {
				for(KeyValue kv : result.raw()) {
					userName = Bytes.toString(kv.getRow());
				}
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(userName);
		return userName;
	}
	public static void main(String[] args){
		AdminTable adminTable = new AdminTable();
		System.out.println(adminTable.getEmail("xxyd"));
	}
}
