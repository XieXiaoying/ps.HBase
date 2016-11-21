package Queue;

import collection.Constants;

import java.util.Date;

import kmeans.KmeansTable;
import kmeans.KmeansTest;
import kmeans.Point;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import collection.CollectionPicTable;
import collection.CollectionTable;
import Web.GridPoiTable;
import Web.POITable;
import Web.convertXY;
import Queue.DataElem.DataType;

/**
 * 缓存队列"消费者"
 * 
 * @author leeying
 * 
 */
public class QueueGetter implements Runnable {
	// 图片压缩线程池
	/*ExecutorService compressThreadPool = Executors.newSingleThreadExecutor(); 
	
	// 图片压缩线程
	Callable<byte[]> compressThread = new Callable<byte[]>() {
		public byte[] call() throws Exception {
			byte[] content = null;
			while( (content = getPic()) == null){
				// do nothing
			}
			// 压缩过程差不多需要300多ms
			return compressImageByarray.compress(content);
		}
	};
	
	// 原图
	private byte[] originalPic = null;
	
	synchronized public void setPic(byte[] content){
		originalPic = content;
	}
	
	synchronized public byte[] getPic(){
		return originalPic;
	}*/
	
	
	// 缓存队列
	private SingletonDataQueue dataQueue;
	// 感应数据收集表
	private CollectionTable ct = null;
	// 图片收集表
	private CollectionPicTable cpt = null;
	
	int threadId;

	static Logger logger = Logger.getLogger(QueueGetter.class);

	public QueueGetter(SingletonDataQueue dataQueue) {
		this.dataQueue = dataQueue;
		 
		ct = new CollectionTable();
		// 开启写缓存
		ct.setAutoFlush(false);

		cpt = new CollectionPicTable();
		
		
	}

	@Override
	public void run() {
		while (true) {
			
			Date before = new Date();
			logger.warn("!!Thread run");
			if(dataQueue==null)logger.warn("!!Thread dataQueue: null");
			logger.warn("!!Thread dataQueue: "+ dataQueue);
			logger.warn("!!Thread dataQueue size: "+ dataQueue.getSize());
			DataElem elem = dataQueue.getAndRemoveELem();
			logger.warn("!!Thread dataElem: "+ elem);
			if (elem != null) {
				// 内容是JSON格式
				if (elem.getDt() == DataType.JSON) {
					ct.setJSON(elem.getContentInString());
					Date after = new Date();
					logger.info("Deal with JSON string in "
							+ (after.getTime() - before.getTime())
							+ " ms ");

				}
				// 内容是图片格式
				else if (elem.getDt() == DataType.PIC) {
					// 图片划分
					/*
					 * List<byte[]> subPics = CollectionTable.splitPic(
					 * elem.getContent(), 1024 * 1024);
					 */
					
					//压缩图片
					//Future<byte[]> future = compressThreadPool.submit(compressThread);
					
					
					// 解析图片
					//JSONObject json = JSONObject.fromObject(EXIFParser
					//		.parse(elem.getContent()));
					
					// 存储图片
					JSONObject pic = JSONObject.fromObject(elem.getContentInString()).getJSONObject("pic");
					if (!pic.isNullObject()) {
							Date timestamp = new Date(pic.getLong(Constants.TIMESTAMP));
							
							//提取压缩结果
							/*byte[] compressContent = null;
							try {
								compressContent = future.get(100,TimeUnit.MILLISECONDS);
							} catch (InterruptedException | ExecutionException e) {								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (TimeoutException e) {
								e.printStackTrace();
							}*/
							
							cpt.setPic(pic.getDouble(Constants.LON),
										pic.getDouble(Constants.LAT), timestamp,
										pic.getString(Constants.USERNAME),
										elem.getUri(), null,
										pic.getInt(Constants.FPM), pic.getString(Constants.STATION));
							//把坐标转换成百度坐标
							double[] baiducor = convertXY.gpsTobaiducor(pic.getDouble(Constants.LON),
										pic.getDouble(Constants.LAT));
							
							double blat = baiducor[1];
							double blon = baiducor[0];
							System.out.println("lat yu lon zhuanhuan ");
							
							//用到的table
							GridPoiTable gridPoiTable = new GridPoiTable();
							POITable poiTable = new POITable();
							
							//不管是不是新建站点都要存入到poitable
							
							poiTable.setPic(pic.getString(Constants.STATION), Bytes.toString(elem.getUri()), pic.getInt(Constants.FPM), timestamp);
							String poiIDString = gridPoiTable.regionIsExist(blat, blon);
							logger.info("setGrid");
								
							if (poiIDString!=null) {//新建站点，需要更新Web.GridPoiTable,需要增加poiTable中的poiInfo
								logger.info("before新建站点"+poiIDString);
								
								gridPoiTable.setGrid(blat, blon, pic.getString(Constants.STATION), timestamp, pic.getString(Constants.USERNAME));
								
								poiTable.setPOIInfo(pic.getString(Constants.STATION), pic.getDouble(Constants.LON),
										pic.getDouble(Constants.LAT), blon, blat, pic.getString(Constants.USERNAME));
								logger.info("after新建站点"+ pic.getString(Constants.STATION));
								
							}
							
							
								
						
					}
					Date after = new Date();
					logger.info("Deal with PIC" + " in"
							+ (after.getTime() - before.getTime()) + "ms");

				} else {
					logger.info(elem.getDt() + "is not a valid dataType");
				}

			}// end if

			// 队列空,等待
			else {
				try {
					// 最后要将writebuffer里的结果flush回去呀。。。
					ct.flushCommit();
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}

		}// end while

	}

}
