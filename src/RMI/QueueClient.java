package RMI;

import collection.Constants;

import java.rmi.Naming;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import Parser.EXIFParser;
import Queue.DataElem.DataType;
import Queue.QueuePutter;

class QueueClientThread implements Runnable {
	static Logger logger = Logger.getLogger(QueueClientThread.class);

	@Override
	public void run() {
		try {

			QueueInterface queue = (QueueInterface) Naming
					.lookup("rmi://localhost:1099/Queue");
			Date before = new Date();
			String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
			Random r = new Random();
			for (int i = 0; i < 10000; i++) {
				JSONObject jo = new JSONObject();
				// 用户名随机生成
				StringBuilder username = new StringBuilder();
				for (int j = 0; j < 6; j++) {
					int index = r.nextInt(51);
					username.append(chars.charAt(index));
				}
				jo.put(Constants.USERNAME, username.toString());
				// 数据随机生成
				JSONArray ja = new JSONArray();
				for (int j = 1; j < 5; j++) {
					JSONObject data = new JSONObject();
					data.put(Constants.LON, r.nextDouble());
					data.put(Constants.LAT, r.nextDouble());

					// 随机生成时间戳
					StringBuilder timestamp = new StringBuilder();
					timestamp.append("2014-");
					int month = r.nextInt(11) + 1;
					int day = r.nextInt(27) + 1;
					if (month < 10) {
						timestamp.append("0");
					}
					timestamp.append(month);
					timestamp.append("-");
					if (day < 10) {
						timestamp.append("0");
					}
					timestamp.append(day);
					timestamp.append(" ");
					int hour = r.nextInt(23);
					if (hour < 10) {
						timestamp.append("0");
					}
					timestamp.append(hour);
					timestamp.append(":");
					int min = r.nextInt(59);
					if (min < 10) {
						timestamp.append("0");
					}
					timestamp.append(min);
					timestamp.append(":");
					int sec = r.nextInt(59);
					if (sec < 10) {
						timestamp.append("0");
					}
					timestamp.append(sec);

					data.put(Constants.TIMESTAMP, timestamp.toString());
					data.put(Constants.NOISE, r.nextInt(100));
					ja.add(data);
				}
				jo.put(Constants.RECORDLIST, ja);
				byte[] content = jo.toString().getBytes();
			
				Date beforeRMI = new Date();
				// 调用远程方法
				while (!queue.insert(DataType.JSON, content,null)) {
					Thread.sleep(10);
				}
				Date afterRMI = new Date();
				 logger.info(i + "\tQueueClient successfully insert Queue in "
				 + (afterRMI.getTime()-beforeRMI.getTime()) + " ms");

			}// end for
			Date after = new Date();
			System.out.println("client send all request in"
					+ (after.getTime() - before.getTime()) + "ms");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

public class QueueClient {
	/**
	 * 查找远程对象并调用远程方法
	 */
	public static void main(String[] argv) {
		for (int i = 0; i < 1; i++) {
			QueueClientThread qct = new QueueClientThread();
			Thread t = new Thread(qct);
			t.start();
		}
		
		/*try {
			QueueInterface queue = (QueueInterface) Naming
					.lookup("rmi://10.108.107.92:1099/Queue");
			
			byte[] content = QueuePutter.image2bytes("/usr/local/apache-tomcat-7.0.54/webapps/ROOT/zzy_1411107495684.jpg");
			String json = EXIFParser.parse(content);
			
			for(int i = 0;i<20;i++){
				System.out.println(queue.insert(DataType.PIC,json.getBytes(),"zzy_1411107495684.jpg".getBytes()));
				Thread.sleep(100);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		

	}

}