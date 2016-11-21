package Pic;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;

import collection.Constants;


/**
 * 完成图片分析模块"观测标"和"天气表"的共同操作"寻找距时间戳最近的有效记录"
 * 
 * @author leeying
 * 
 */
public abstract class CommonOperate{
	// 列族名
	public final static byte[] COLFAM_NAME = "info".getBytes();

	/**
	 * 寻找距时间戳最近的有效记录
	 * 
	 * @return
	 * @throws IOException
	 */
	public Result findNearestValidRecord(String key, Date timestamp)
			throws IOException {
		Result result = null;
		// System.out.println(timestamp.toLocaleString());
		long ts = timestamp.getTime();
		// timestamp本来就是整点时刻
		if (timestamp.getMinutes() == 0 && timestamp.getMinutes() == 0) {
			// 前一个整点结果
			String rowKey = key.toLowerCase() + Constants.SEPARATER + ts;
			Get get = new Get(rowKey.getBytes());
			get.addFamily(COLFAM_NAME);
			result = getHTable().get(get);
		}

		else {
			// timestamp的前一个整点时刻
			Date pre = new Date(ts
					- (timestamp.getMinutes() * 60 + timestamp.getSeconds())
					* 1000);
			// timestamp的后一个整点时刻
			Date next = new Date(
					ts
							+ ((59 - timestamp.getMinutes()) * 60 + 60 - timestamp
									.getSeconds()) * 1000);

			long preTime = pre.getTime();
			// System.out.println("preTime = " + preTime + " " +
			// pre.toLocaleString());
			long nextTime = next.getTime();
			// System.out.println("nextTime = " + nextTime + " " +
			// next.toLocaleString());

			// 前一个整点结果
			String pre_rowKey = key.toLowerCase() + Constants.SEPARATER
					+ preTime;
			Get get = new Get(pre_rowKey.getBytes());
			get.addFamily(COLFAM_NAME);
			Result pre_result = getHTable().get(get);

			// 前一个整点结果
			String next_rowKey = key.toLowerCase() + Constants.SEPARATER
					+ nextTime;
			get = new Get(next_rowKey.getBytes());
			get.addFamily(COLFAM_NAME);
			Result next_result = getHTable().get(get);

			// 比较结果
			if (pre_result == null || pre_result.isEmpty()) {
				// System.out.println("没有前一个时刻");
				if (next_result != null && !next_result.isEmpty()) {
					result = next_result;
				}
			} else if (next_result == null || next_result.isEmpty()) {
				// System.out.println("没有后一个时刻");
				if (pre_result != null && !pre_result.isEmpty()) {
					result = pre_result;
				}
			} else {
				if (ts - preTime <= nextTime - ts) {
					// System.out.println("两者倾向前一个时刻");
					result = pre_result;
				} else {
					// System.out.println("两者倾向后一个时刻");
					result = next_result;
				}

			}
		}

		return result;

	}

	public abstract HTable getHTable();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
