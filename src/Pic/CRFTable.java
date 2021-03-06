package Pic;

import collection.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;


public class CRFTable {
	// 实例
	//private static CRFTable instance = null;
	// 表名
	private static final String tableName = "crf";

	private final static byte[] INFO_CF = "info".getBytes();

	private final static byte[] CRF = "crf".getBytes();

	private HTable hTable = null;

	/*public static CRFTable getInstance() {
		if (instance == null) {
			instance = new CRFTable();
		}
		return instance;
	}*/

	public CRFTable() {
		Configuration conf = HBaseConfiguration.create();
		try {
			
			HBaseAdmin hAdmin = new HBaseAdmin(Constants.conf);
			if (hAdmin.tableExists(tableName)) {
			      // do nothing	
			}
			else{
				// 设置版本保存策略
				HColumnDescriptor des = new HColumnDescriptor(INFO_CF);
				des.setMaxVersions(Integer.MAX_VALUE);
				HTableDescriptor t = new HTableDescriptor(tableName);
				t.addFamily(des);
				hAdmin.createTable(t);
			}
			hAdmin.close();
			
			hTable = new HTable(conf, tableName);

			/*
			 * HBase有两种方式回收旧的版本
			 * 
			 * 1. 可以设置最多可以保留的版本数量。如果超过，则最老的版本会被抛弃，默认设置是3个版本，这个可以在创建Column
			 * Family时通过HColumnDescriptor.setMaxVersions(int
			 * versions)设置，因此这个是Column
			 * Family级别的。当然这个限制中读取的时候是逻辑限制，即时生效，但老版本的物理删除还是需要等到major
			 * compact操作中执行
			 * 。将这个值设置为1并不是说就禁用了多版本，每次Put的时候还是同样的会生成新的版本，只是最后只保留一个版本而已。
			 * 
			 * 2. 可以设置TTL(Time To Live)，如果版本存在的时间超过TTL，则会被删除。默认的TTL是forever。通过
			 * HColumnDescriptor.setTimeToLive(int
			 * seconds)可以设置TTL，物理清除版本还是要等到major
			 * compact，但Get/Scan等读取操作逻辑是即时生效的。需要注意的是
			 * ，如果row中所有的cell都被TTL失效以后，这一行记录就被删除了
			 * （HBase中不需要显示的建立或者删除行，如果行中的cell有值，行就存在）。
			 * 
			 * 对于上面的两种回收方式，会有一种有趣的场景，假设你对一个cell插入了三个版本t1,t2,t3,而最大版本数设置是2,这时候请求所有版本
			 * ，
			 * 则只有t2和t3会返回，这是正常的。但这时如果删除t2或者t3，再请求所有版本，则t1可能又会出现在结果中了。当然，在major
			 * compact操作之后，就不会有这个问题了。因此major compact并不是对用户透明的操作，可能会影响用户的查询结果。
			 */

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存phoneID在timestamp生成的crf文件
	 * @author leeying
	 * @param phoneID
	 * @param crf
	 *            文件
	 * @param timestamp
	 */
	public void set(int phoneID, File crf, Date timestamp) {
		try {
			InputStream is = new FileInputStream(crf);
			long fileLength = crf.length();
			// 超过Int范围
			if (fileLength > Integer.MAX_VALUE) {
				System.out.println("file size = " + fileLength);
			} else {
				byte[] b = new byte[(int) fileLength];
				int num = is.read(b);
				if (num != fileLength) {
					System.out.println("read failed");
				}
				// System.out.println(new String(b));
				// 调用
				set(phoneID, b, timestamp);
			}
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存phoneID在timestamp生成的crf内容
	 * @author leeying
	 * @param phoneID
	 * @param crf
	 *            文件
	 * @param timestamp
	 */
	public Boolean set(int phoneID, byte[] crf, Date timestamp) {
		// 加上时间戳
		Put put = new Put(Bytes.toBytes(phoneID), timestamp.getTime());
		put.add(INFO_CF, CRF, crf);
		try {
			hTable.put(put);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 返回手机型号phoneModel在timestamp之前的最新CRF模型
	 * @author leeying
	 * @param phoneModel
	 * @param timestamp
	 * @return byte[]
	 */
	public byte[] getLastestCRF(int phoneID, Date timestamp) {
		Get get = new Get(Bytes.toBytes(phoneID));
		try {
			// 设置时间范围
			get.setTimeRange(0, timestamp.getTime() + 1);
			get.setMaxVersions(1);
			get.addColumn(INFO_CF, CRF);
			Result r = hTable.get(get);
			if (!r.isEmpty()) {
				return r.getValue(INFO_CF, CRF);
			} else {
				System.out.println(phoneID + " before " + timestamp.toString() +  " result is null");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return null;
	}

	/**
	 * 删除phoneID某timestamp下的记录
	 * 
	 * @param phoneID
	 * @param timestamp
	 * @return
	 */
	public Boolean delete(int phoneID, Date timestamp) {
		Delete del = new Delete(Bytes.toBytes(phoneID), timestamp.getTime());
		try {
			hTable.delete(del);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void main(String[] argv) {
		CRFTable ct = new CRFTable();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		int phoneID = new Random(new Date().getTime()).nextInt(10000);
		try {
			Date timestamp1 = sdf.parse("2014-05-06 16:00:00");
			ct.set(phoneID, "model1".getBytes(), timestamp1);
			Date timestamp2 = sdf.parse("2014-05-06 17:00:00");
			ct.set(phoneID, "model2".getBytes(), timestamp2);
			Date timestamp3 = sdf.parse("2014-05-06 18:00:00");
			ct.set(phoneID, "model3".getBytes(), timestamp3);
			Date timestamp4 = sdf.parse("2014-05-06 19:00:00");
			ct.set(phoneID, "model4".getBytes(), timestamp4);

			Date queryTime = new Date();
			byte[] result = ct.getLastestCRF(phoneID, queryTime);
			Assert.assertArrayEquals("model4".getBytes(), result);
		
			// TODO model1还在不在呢？

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
