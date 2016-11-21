package Incentive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import net.sf.json.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueExcludeFilter;
import org.apache.hadoop.hbase.util.Bytes;

import collection.Constants;

/**
 * 激励模块的视图
 * 
 * @author leeying
 * 
 */

public class IncentiveJobTable {
	// 尝试完成“已完成”任务异常
	public String TRY_TO_FINISH_FINISHEDJOB_EXCEPTION = "Job has been finished";
	// 非任务响应用户尝试完成任务异常
	public String INVALID_USER = "User did not response the job";
	
	// 表名
	private static final String tableName = "incentiveJob";
	// 列族名
	private final static byte[] JOB_CF = "job".getBytes();
	private final static byte[] USER_CF = "user".getBytes();

	// 列名之数据类型
	private final static byte[] DATATYPE = "dataType".getBytes();
	// 列名之报酬
	private final static byte[] PAYMENT = "payment".getBytes();
	// 列名之是否被推送
	private final static byte[] PUSHED = "pushed".getBytes();
	// 列名之响应的用户列表
	private final static byte[] WILLING_USERS = "WILLING_USERS".getBytes();

	// 列名之完成用户
	private final static byte[] FINISHED_USER = "finished_user".getBytes();
	// 列名之完成时间
	private final static byte[] FINISHED_TIME = "finished_time".getBytes();
	// 列名之完成激励
	private final static byte[] FINISHED_PAY = "finished_pay".getBytes();

	//private static IncentiveJobTable instance = null;
	private HTable hTable = null;

	/*public static IncentiveJobTable getInstance() {
		if (instance == null) {
			instance = new IncentiveJobTable();
		}
		return instance;
	}*/

	public IncentiveJobTable() {
		Configuration conf = HBaseConfiguration.create();
		try {
			hTable = new HTable(conf, tableName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 构造行键
	 * 
	 * @param job
	 * @return
	 */
	public byte[] generateRowkey(IncentiveJobDataUnit job) {
		return generateRowkey(job.getLon(), job.getLat(), job.getPublishTime());
	}

	/**
	 * 构造行键
	 * 
	 * @param lon
	 * @param lat
	 * @param timestamp
	 * @return
	 */
	public byte[] generateRowkey(double lon, double lat, Date timestamp) {
		String rowkey = "" + lon + Constants.SEPARATER + lat
				+ Constants.SEPARATER + timestamp.getTime();
		return rowkey.getBytes();

	}

	/**
	 * 新增一个激励任务
	 * 
	 * @param job
	 * @param flag
	 * @return
	 */
	public Boolean setIncentiveJob(IncentiveJobDataUnit job, Boolean flag) {
		return setIncentiveJob(job.getDataType(), job.getLon(), job.getLat(),
				job.getPublishTime(), job.getPayment(), flag);
	}

	/**
	 * 新增一个激励任务
	 * 
	 * @param dataType
	 *            采集数据类型
	 * @param lon
	 * @param lat
	 * @param timestamp
	 *            发布激励的时间
	 * @param payment
	 *            报酬金额
	 * @param flag
	 *            是否被推送
	 * @return 操作成功返回true否者返回false
	 */
	public Boolean setIncentiveJob(int dataType, double lon, double lat,
			Date timestamp, double payment, boolean flag) {
		Put put = new Put(generateRowkey(lon, lat, timestamp));
		put.add(JOB_CF, DATATYPE, Bytes.toBytes(dataType));
		put.add(JOB_CF, PAYMENT, Bytes.toBytes(payment));
		put.add(JOB_CF, PUSHED, Bytes.toBytes(flag));

		try {
			hTable.put(put);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 删除激励任务
	 * 
	 * @param job
	 * @return
	 */
	public Boolean delIncentiveJob(IncentiveJobDataUnit job) {
		return delIncentiveJob(job.getLon(), job.getLat(), job.getPublishTime());
	}

	/**
	 * 删除激励任务
	 * 
	 * @param lon
	 * @param lat
	 * @param timestamp
	 * @return
	 */
	public Boolean delIncentiveJob(double lon, double lat, Date timestamp) {
		Delete del = new Delete(generateRowkey(lon, lat, timestamp));

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
	 * 更新激励任务推送情况
	 * 
	 * @param elem
	 * @param flag
	 * @return 操作成功返回true否者返回false
	 */
	public Boolean setPushed(IncentiveJobDataUnit elem, Boolean flag) {
		return setPushed(elem.getLon(), elem.getLat(), elem.getPublishTime(),
				flag);
	}

	/**
	 * 更新激励任务的推送情况
	 * 
	 * @param lon
	 * @param lat
	 * @param timestamp
	 * @param flag
	 *            是否被推送
	 * @return
	 */
	public Boolean setPushed(double lon, double lat, Date timestamp,
			Boolean flag) {
		Put put = new Put(generateRowkey(lon, lat, timestamp));
		put.add(JOB_CF, PUSHED, Bytes.toBytes(flag));
		try {
			hTable.put(put);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 用户响应激励任务
	 * 
	 * @param elem
	 * @param users
	 * @return
	 */
	public Boolean setUsersResponseIncentiveJob(IncentiveJobDataUnit elem,
			List<UserResponseDataUnit> users) {
		return setUsersResponseIncentiveJob(elem.getLon(), elem.getLat(),
				elem.getPublishTime(), users);
	}

	/**
	 * 用户响应激励任务
	 * 
	 * @param users
	 * @param lon
	 * @param lat
	 * @param timestamp
	 * @return
	 */
	public Boolean setUsersResponseIncentiveJob(double lon, double lat,
			Date timestamp, List<UserResponseDataUnit> users) {
		byte[] rowkey = generateRowkey(lon, lat, timestamp);
		// 读取原来的用户列表
		Get get = new Get(rowkey);
		get.addColumn(USER_CF, WILLING_USERS);
		Result r;
		try {
			r = hTable.get(get);
			// 已经有响应用户
			if (!r.isEmpty()) {
				byte[] value = r.getValue(USER_CF, WILLING_USERS);
				// 转换成JSONObject
				JSONObject jo = JSONObject.fromObject(value);
				for (UserResponseDataUnit user : users) {
					// 添加用户
					JSONObject val = new JSONObject();
					val.accumulate("time", user.getResponseTime().getTime());
					val.accumulate("pay", user.getPayment());
					jo.accumulate(user.getUsername(), val);
				}
				System.out.println(jo.toString());
				Put put = new Put(rowkey);
				put.add(USER_CF, WILLING_USERS, Bytes.toBytes(jo.toString()));
				hTable.put(put);

			} else {
				// 新建JSONObject
				JSONObject jo = new JSONObject();
				for (UserResponseDataUnit user : users) {
					// 添加用户
					JSONObject val = new JSONObject();
					val.accumulate("time", user.getResponseTime().getTime());
					val.accumulate("pay", user.getPayment());
					jo.accumulate(user.getUsername(), val);
				}
				System.out.println(jo.toString());
				Put put = new Put(rowkey);
				put.add(USER_CF, WILLING_USERS, Bytes.toBytes(jo.toString()));
				hTable.put(put);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;

	}

	/**
	 * 查询任务的所有响应用户信息，没有则返回null
	 * 
	 * @param job
	 * @return
	 */
	public List<UserResponseDataUnit> queryAllResponseUsers(
			IncentiveJobDataUnit job) {
		byte[] rowkey = generateRowkey(job);
		Get get = new Get(rowkey);
		get.addFamily(USER_CF);

		List<UserResponseDataUnit> users = null;
		try {
			Result r = hTable.get(get);
			// 最后的结果-是JSONObject字符串表示
			byte[] value = null;
			if (!r.isEmpty()
					&& (value = r.getValue(USER_CF, WILLING_USERS)) != null) {
				users = new ArrayList<UserResponseDataUnit>();
				System.out.println("value = " + new String(value));
				JSONObject jo = JSONObject.fromObject(new String(value));

				// JSONObject遍历
				Iterator it = jo.keys();
				while (it.hasNext()) {
					String username = (String) it.next();
					JSONObject val = jo.getJSONObject(username);
					long responseTime = val.getLong("time");
					double payment = val.getDouble("pay");
					UserResponseDataUnit user = new UserResponseDataUnit(
							username, new Date(responseTime), payment);

					users.add(user);
				}

				return users;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 用户完成激励任务,返回用户的激励，当完成用户不在响应用户列表里时抛出INVALID_USER exception，
	 * 当激励任务已经被完成时抛出 TRY_TO_FINISH_FINISHEDJOB_EXCEPTION exception
	 * 
	 * @param job
	 * @param username
	 *            用户名
	 * @param finishedTime
	 *            用户完成时间
	 * @return 返回用户对应的激励
	 * @throws Exception
	 */
	public double setUserFinishedIncentiveJob(IncentiveJobDataUnit job,
			String username, Date finishedTime) throws Exception {
		return setUserFinishedIncentiveJob(job.getLon(), job.getLat(),
				job.getPublishTime(), username, finishedTime);
	}

	/**
	 * 用户完成激励任务,返回用户的激励
	 * 
	 * 
	 * @param lon
	 * @param lat
	 * @param timestamp
	 * @param username
	 * @param finishedTime
	 * @return
	 * @throws Exception
	 */
	public double setUserFinishedIncentiveJob(double lon, double lat,
			Date timestamp, String username, Date finishedTime)
			throws Exception {
		byte[] rowkey = generateRowkey(lon, lat, timestamp);
		// 读取响应用户列表
		Get get = new Get(rowkey);
		get.addColumn(USER_CF, WILLING_USERS);
		get.addColumn(USER_CF, FINISHED_USER);
		Result r = hTable.get(get);
		
		if (!r.isEmpty()) {
			// 任务已经被完成了
			if (r.getValue(USER_CF, FINISHED_USER) != null) {
				throw new Exception(TRY_TO_FINISH_FINISHEDJOB_EXCEPTION);
			}
			
			// 任务没有完成
			byte[] responseUsers = r.getValue(USER_CF, WILLING_USERS);
			if (responseUsers != null) {
				System.out.println("responseUsers = "
						+ new String(responseUsers));
				// 转换成JSONObject
				JSONObject jo = JSONObject
						.fromObject(new String(responseUsers));
				System.out.println("jo = " + jo.toString());
				// 获得username对应的响应时间和约定激励
				JSONObject val = jo.getJSONObject(username);
				if(val.isNullObject()){
					throw new Exception(INVALID_USER);
				}
				else{
					Put put = new Put(rowkey);
					put.add(USER_CF, FINISHED_USER, username.getBytes());
					put.add(USER_CF, FINISHED_TIME,
							Bytes.toBytes(finishedTime.getTime()));
					double pay = val.getDouble("pay");
					put.add(USER_CF, FINISHED_PAY,
							Bytes.toBytes(pay));

					
					try{
						hTable.put(put);
					}
					catch (IOException e){
						e.printStackTrace();
					}
					
					return pay;
				}

			} 
			// 没有响应用户
			else {
				throw new Exception(INVALID_USER);
			}
		}
		else{
			throw new Exception(INVALID_USER);
		}
	}

	/**
	 * 
	 * 查找标志位为flag的任务，没有则返回null
	 * 
	 * @param flag
	 *            是否已经被推送
	 * @return List<IncentiveJobDataUnit> 符合标志位为flag激励任务列表
	 */
	public List<IncentiveJobDataUnit> queryIncentiveJob(Boolean flag) {
		Scan scan = new Scan();
		scan.addColumn(JOB_CF, DATATYPE);
		scan.addColumn(JOB_CF, PAYMENT);
		scan.addColumn(JOB_CF, PUSHED);
		Filter f = new SingleColumnValueExcludeFilter(JOB_CF, PUSHED,
				CompareFilter.CompareOp.EQUAL, new BinaryComparator(
						Bytes.toBytes(flag)));
		scan.setFilter(f);
		List<IncentiveJobDataUnit> result = new ArrayList<IncentiveJobDataUnit>();
		ResultScanner rs = null;
		try {
			rs = hTable.getScanner(scan);
			for (Result r : rs) {
				int dataType = Bytes.toInt(r.getValue(JOB_CF, DATATYPE));
				double payment = Bytes.toDouble(r.getValue(JOB_CF, PAYMENT));
				// 获得rowkey
				String rowKey = Bytes.toString(r.getRow());
				String[] tokens = rowKey.split(Constants.SEPARATER);
				Assert.assertEquals(3, tokens.length);

				double lon = Double.parseDouble(tokens[0]);
				double lat = Double.parseDouble(tokens[1]);
				long publishTime = Long.parseLong(tokens[2]);
				result.add(new IncentiveJobDataUnit(dataType, lon, lat,
						payment, new Date(publishTime)));

			}
			rs.close();
			if (result.isEmpty()) {
				return null;
			}

			return result;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(rs != null){
				rs.close();
			}
		}

		return null;
	}

}
