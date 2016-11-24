package Incentive;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;

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
	public Boolean set(String username,String pwd,String email,String tel){
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
}
