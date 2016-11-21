package Queue;

import java.io.*;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * 缓存队列"生产者"
 *
 * @author leeying
 *
 */
public class QueuePutter {
	static Logger logger = Logger.getLogger(QueuePutter.class); 
	
	private SingletonDataQueue dataQueue;

	// 构造函数
	public QueuePutter(SingletonDataQueue dataQueue) {
		this.dataQueue = dataQueue;
	}

	/**
	 * 图片转byte
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static byte[] image2bytes(String path) throws IOException{
		InputStream inputStream = new FileInputStream(path);
		BufferedInputStream in = new BufferedInputStream(inputStream);
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		byte[] temp = new byte[1024];
		int size = 0;
		while ((size = in.read(temp)) != -1) {
			out.write(temp, 0, size);
		}
		byte[] content = out.toByteArray();
		in.close();
		out.close();
		return content;
	}

	/**
	 * 插入一个元素
	 * 
	 * @param dt
	 *            数据是JSON串还是图片
	 * @param tt
	 *            如果数据是JSON串，是数据表的内容，还是图片表的内容
	 * @param content
	 *            内容
	 * @return true：插入成功 false：队列满
	 */
	public boolean addElem(DataElem.DataType dt, byte[] content,byte[] uri) {
		Date before = new Date();
		boolean rtnval = this.dataQueue.addELem(dt, content,uri);
		Date after = new Date();
		if(rtnval){
			logger.info("AddElem successed in " + (after.getTime() - before.getTime()) + "ms");
		}
		else{
			logger.info("AddElem failed in " + (after.getTime() - before.getTime()) + "ms");
		}
		
		return rtnval;
	}
	
	
}
