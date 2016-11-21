package collection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.util.Bytes;

import Common.AbstractTable;
import Common.Column;
import Web.GridPoiTable;
import Web.POITable;
import Web.convertXY;

/**
 * 收集的图片表
 *
 * @author leeying
 *
 */
public class CollectionPicTable extends AbstractTable {
	// 表名
	public static final String tableName = "pic";
	// 收集数据列族
	public static final byte[] INFO_CF = "info".getBytes();
	// POI列族
	public static final byte[] POI_CF = "poi".getBytes();

	public static final String[] family = {"info",  "poi"};

	// 图片内容列名
	public static final byte[] PIC_COL = "pic".getBytes();
	// 压缩图片内容
	public static final byte[] COMPRESS_COL = "compress".getBytes();
	// 图片PM2.5列名
	public static final byte[] FPM_COL = "fpm".getBytes();

	public static final byte[] POI_COL = "poi".getBytes();

	public CollectionPicTable() {
		try {
			createTable(Constants.conf, tableName, family);
			hTable = new HTable(Constants.conf, tableName);

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成行键
	 *
	 * @param lon
	 * @param lat
	 * @param timestamp
	 * @param username
	 * @return
	 */
	public static byte[] generateRowkey(double lon, double lat, Date timestamp,
			String username) {
		String rowkey = timestamp.getTime() + Constants.SEPARATER;

		if (lon != -1 && lat != -1) {
			rowkey += lon + Constants.SEPARATER + lat + Constants.SEPARATER;
		}

		if (username != null) {
			rowkey += username;
		}
		return rowkey.getBytes();
	}

	/**
	 * @param rowkey
	 * @return UploadPicInfo
	 *
	 * */
	public UploadPicInfo retrieveInfoFromRowKey(byte[] rowKey) {
		String str = Bytes.toString(rowKey);
		String[] tokens = str.split(Constants.SEPARATER);

		if (tokens.length < 3) {
			System.out.println("Invalid CollectionPicTable rowkey = " + str);
			return null;
		}

		long timestamp = Long.parseLong(tokens[0]);
		double lon = Double.parseDouble(tokens[1]);
		double lat = Double.parseDouble(tokens[2]);
		String username = "unkown";
		if (tokens.length == 4) {
			username = tokens[3];
		}

		return new UploadPicInfo(timestamp, lon, lat, username);
	}

	/**
	 * 保存（lon，lat）地�?timestamp时间 图片内容content和pm2.5值fpm
	 *
	 * @param lon
	 * @param lat
	 * @param timestamp
	 * @param username
	 * @param content
	 *            原图 or 图片路径
	 * @param compressContent
	 *            压缩图片
	 * @param fpm
	 *            图片PM2.5�?
	 * @return
	 */
	public Boolean setPic(double lon, double lat, Date timestamp,String username, byte[] content, byte[] compressContent, int fpm,String poi) {
		byte[] rowKey = generateRowkey(lon, lat, timestamp, username);
		//System.out.println("pic = " + Bytes.toString(content) + " lon = " + lon
			//	+ " lat = " + lat + " username = " + username + "fpm = " + fpm);
		//System.out.println("RowKey = " + Bytes.toString(rowKey));
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		kvs.add(new KeyValue(rowKey, INFO_CF, PIC_COL, content));
		kvs.add(new KeyValue(rowKey, INFO_CF, FPM_COL, Bytes.toBytes(fpm)));
		if(poi != null){
			kvs.add(new KeyValue(rowKey, POI_CF, POI_COL, Bytes.toBytes(poi)));
		}
		if (compressContent != null) {
			kvs.add(new KeyValue(rowKey, INFO_CF, COMPRESS_COL, compressContent));
		}
		return this.put(rowKey, null, kvs);
	}

	/**
	 * 获得图片,没有则返回null
	 *
	 * @param lon
	 * @param lat
	 * @param timestamp
	 * @return
	 */
	public List<byte[]> getPic(double lon, double lat, Date timestamp) {
		// 设置查询�?
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(INFO_CF, PIC_COL));

		Scan scan = new Scan();
		scan.setStartRow(generateRowkey(lon, lat, timestamp, null));
		double endLat = Double.parseDouble(lat + "0");
		scan.setStopRow(generateRowkey(lon, endLat, timestamp, null));

		ResultScanner rs = this.scan(scan, null, columnList, null);
		if (rs != null) {
			List<byte[]> picList = new ArrayList<byte[]>();
			for (Result r : rs) {
				picList.add(r.getValue(INFO_CF, PIC_COL));
			}

			rs.close();

			return picList;
		}

		return null;

	}

	/**
	 * 获得从beginTime至今的图�?没有则返回null
	 *
	 * @return
	 */
	public List<Pic> getPic(Date beginTime) {
		// 设置查询�?
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(INFO_CF, PIC_COL));

		// 起始行键
		byte[] startRowKey = generateRowkey(-1, -1, beginTime, null);
		Scan scan = new Scan();
		scan.setStartRow(startRowKey);

		ResultScanner rs = this.scan(scan, null, columnList, null);
		if (rs != null) {
			List<Pic> picList = new ArrayList<Pic>();
			for (Result r : rs) {
				try {
					byte[] val = null;
					if ((val = r.getValue(INFO_CF, PIC_COL)) != null) {
						UploadPicInfo uploadPicInfo = retrieveInfoFromRowKey(r
								.getRow());
						if (uploadPicInfo == null) {
							continue;
						}
						Pic pic = new Pic(new Date(
								uploadPicInfo.getUploadtime()),
								uploadPicInfo.getLon(), uploadPicInfo.getLat(),
								Bytes.toString(val));
						picList.add(pic);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			rs.close();
			return picList;

		}

		return null;

	}

	/**
	 * 删除图片
	 *
	 * @param rowKey
	 * @return
	 */

/*	public Boolean delPic(byte[] rowKey) {
		Delete del = new Delete(rowKey);
		try {
			hTable.delete(del);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
*/

	public static List<byte[]> splitPic(byte[] content, int sectionSize) {
		List<byte[]> subPics = new ArrayList<byte[]>();
		int index = 0;
		while(index < content.length){
			int len = Math.min(content.length - index, sectionSize);
			byte[] subPic = new byte[len];
			System.arraycopy(content, index, subPic, 0, len);

			subPics.add(subPic);

			index += len;

		}

		return subPics;

	}

	/**
	 * 扫描获得所有POI值，没有则返回null
	 *
	 * @return
	 */
	/*public List<String> getAllPOI() {
		// 构造扫描所有具有POI的数据的扫描�?
		SingleColumnValueFilter f = new SingleColumnValueFilter(POI_CF,
				POI_COL, CompareFilter.CompareOp.NOT_EQUAL, (byte[]) null);
		f.setFilterIfMissing(true);

		List<byte[]> cfList = new ArrayList<byte[]>();
		cfList.add(POI_CF);

		ResultScanner rs = this.scan(null, cfList, null, new FilterList(f));

		if (rs != null) {
			List<String> results = new ArrayList<String>();
			for (Result r : rs) {
				byte[] poi = null;
				if (!r.isEmpty()
						&& (poi = r.getValue(CollectionPicTable.POI_CF,
								CollectionPicTable.POI_COL)) != null) {
					results.add(Bytes.toString(poi));
					//System.out.println(Bytes.toString(poi));
				}
			}
			rs.close();

			if (results.isEmpty()) {
				return null;
			}
			return results;
		}

		return null;
	}
*/

	/**
	 *
	 * 导入伪数?
	 */
	public void importFakeData(String basePath) {
		File f = new File("resource/txt/newimg_infotest.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				String filename = tokens[0] + ".jpg";
				double lat = Double.parseDouble(tokens[1]);
				double lon = Double.parseDouble(tokens[2]);
				String relativePath = tokens[3];
				int fpm = Integer.parseInt(tokens[4]);
				String time = tokens[5] + " 00:00:00";

				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy/MM/dd hh:mm:ss");
				Date timestamp = sdf.parse(time);

				String absolutePath = basePath.endsWith(Constants.SEPARATER) ? basePath
						+ relativePath + filename
						: basePath + Constants.SEPARATER + relativePath
								+ filename;

				String username = "ImportedDate";

				byte[] rowKey = generateRowkey(lon, lat, timestamp, username);

				double[] baiducor = convertXY.gpsTobaiducor(lon, lat);

				double blat = baiducor[1];
				double blon = baiducor[0];
				GridPoiTable gridPoi = new GridPoiTable();
				//因为每次的computerID一样的经纬度是不一样的结果
				String poi = gridPoi.regionIsExist(baiducor[1], baiducor[0]);

				// 存储图片到pic表中



				POITable pt = new POITable();



				/*流程分析：：：：：
				 *
				 *
				 * 当来了一张图片
				 * 此处不考虑PM的计算，假设都是计算好的pm值存到图片之中
				 * 首先判断是否为新的站点(判断所需的参数有转换之后的经纬度)
				 *#### 当是一个新的站点的时候——————————
				 *用到的表有    gridPoiTable、POITable、kmeansTable
				 * 		1.我们要建站
				 * 		2.存入kmeansTable中
				 * 			讲一个经纬度转换到百度坐标、平面坐标、18个像素坐标、18个图块坐标
				 * 			存入到kmeans
				 * 			去数据、做聚合、存数据
				 * 		3.存入到poi表中
				 * 			包括poi的信息和图片的信息
				 * #### 当不是一个新的站点的时候————————
				 * 用到的表有poi表
				 * 		存入到poi表中
				*/


				/*
				 * 非导入的时候要用到的
				 * double[] baiducor = convertXY.gpsTobaiducor(lon, lat);
				lat = baiducor[1];
				lon = baiducor[0];
				GridPoiTable gridPoi = new GridPoiTable();*/

				/*判断是否为新的站点
				 * ***************************************************************
				 * 如果是则建立新的站点，则会涉及坐标的转换、还有POI的聚合
				 * */
				if(poi != null){
					//一、 建立新的站点
					gridPoi.setGrid(lat, lon, poi, timestamp, username);

					poi = gridPoi.regionIsExistPOI(lat, lon);
					System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^ " + poi);
					/*
					 * 二、 转换坐标为像素坐标等，进行聚合
					 *
					 *
					 * */
					//将这个点的坐标转换成百度坐标的形式
					/*KmeansTable kms = new KmeansTable();
					double[] worldcor = convertXY.gpstoworldcor(lon, lat);
					double[] titlecor = new double[2];
					double[] pixelcor = new double[2];

					for(int zoom = 1; zoom < 19; zoom++){
						pixelcor = convertXY.worldtopixel(worldcor[0], worldcor[1], zoom);
						titlecor = convertXY.pixeltotitle(pixelcor[0], pixelcor[1]);
						Point point = new Point(pixelcor[0], pixelcor[1], lon, lat, poi);
						//存进数据库中
						kms.setPonit(zoom, titlecor[0], titlecor[1], point.toString(), timestamp);



					}

					//取数据、聚合、存数据
					KmeansTest kmtest = new KmeansTest();
					kmtest.clusterAll();*/
				}


				/*
				 * ****************************************************************
				 * 如果不是新建站点的话，就只需要存到poi表中即可
				 * 因为建站和聚合都是早poiid的层次
				 *
				 *
				 * ***/
				else {

					poi = gridPoi.regionIsExistPOI(lat, lon);
					//donothing

				}

				pt.setPOI(poi, lon, lat, blon, blat, username, timestamp, fpm, absolutePath);
				List<KeyValue> kvs = new ArrayList<KeyValue>();
				kvs.add(new KeyValue(rowKey, INFO_CF, PIC_COL, absolutePath
						.getBytes()));
				kvs.add(new KeyValue(rowKey, INFO_CF, FPM_COL, Bytes
						.toBytes(fpm)));
				kvs.add(new KeyValue(rowKey, POI_CF, POI_COL, poi.getBytes()));
				this.put(rowKey, timestamp, kvs);

			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void fun() {
		try {

			File infoFile = new File("resource/txt/laowang.txt");
			if (infoFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(
						infoFile));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.split("\t");
					if (tokens.length == 7) {
						String picName = tokens[3].endsWith("/")?tokens[3]
								+ tokens[0]:tokens[3]+"/"+tokens[0];
						if(!picName.startsWith("/")){
							picName = "/" + picName;
						}
						double lat = Double.parseDouble(tokens[1]);
						double lon = Double.parseDouble(tokens[2]);
						int fpm = Integer.parseInt(tokens[4]);
						String poi = tokens[5];
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy/MM/dd");
						Date timestamp = sdf.parse(tokens[6]);
						setPic(lon, lat, timestamp, "importer", picName.getBytes(), null, fpm,poi);

					}
				}
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public  void fun2() {
		try {

			File infoFile = new File("resource/txt/BUPT.txt");
			if (infoFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(
						infoFile));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.split("\t");
					if (tokens.length == 3) {
						//String picName = "/usr/local/apache-tomcat-7.0.54/webapps/ROOT/"
						//		+ tokens[0];
						String picName = tokens[0];
						String POI = tokens[1];

						JSONObject pic = JSONObject.fromObject(tokens[2])
								.getJSONObject("pic");
						if (!pic.isNullObject()) {
							Date timestamp = new Date(
									pic.getLong(Constants.TIMESTAMP));

							setPic(pic.getDouble(Constants.LON),
									pic.getDouble(Constants.LAT), timestamp,
									pic.getString(Constants.USERNAME),
									picName.getBytes(), null,
									pic.getInt(Constants.FPM), POI);
						}

					}
				}
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public void fun3(String importfile) throws IOException{
		File importFile3 = new File(importfile + ".txt");
		if(importFile3.exists()){
			try {
				String poi = null;
				BufferedReader reader = new BufferedReader(new FileReader(importFile3));
				String line = null;
				while( (line = reader.readLine()) != null){
					JSONObject jsonObject = JSONObject.fromObject(line);

					//ComputePOI();


					double lon = jsonObject.getDouble("lon");
					double lat = jsonObject.getDouble("lat");
					int fpm = jsonObject.getInt("fpm");
					Date timestamp = new Date(jsonObject.getLong("time"));
					String userName = importfile.substring(importfile.lastIndexOf("/") + 1);
					String path = "/home/ps/img/photos/" + userName + "/" + jsonObject.getString("pic");
					//String path = "/home/ps/img/photos/wwd/iphone2/" + jsonObject.getString("pic");
					double[] blonlat = convertXY.gpsTobaiducor(lon, lat);
					//double blon = blonlat[0] - 0.006555;
					//double blat = blonlat[1] - 0.006017;
					double blon = lon - 0.006555;
					double blat = lat - 0.006017;
					POITable poiTable = new POITable();


					//GridPoiTable gridPoiTable = new GridPoiTable();

                    //poi = gridPoiTable.regionIsExist(blat, blon);
                    //poi = "1491903140";
                    //System.out.println(poi);

 					if(poi == null){
						GridPoiTable gridPoiTable = new GridPoiTable();

						poi = gridPoiTable.regionIsExist(blat, blon);
						if(poi != null){
							//如果不为空，那么久是新建站点，那么就要在gridPoiTable中新建格子，在POITable中新建POI
							gridPoiTable.setGrid(blat, blon, poi, timestamp, userName);
							poiTable.setPOI(poi, lon, lat, blon, blat, userName, timestamp, fpm, path);

						}




					}
 					poiTable.setPic(poi, path, fpm, timestamp);
					setPic(lon, lat, timestamp, userName, Bytes.toBytes(path), null, fpm, poi);
					//poiTable.setPOI(poi, lon, lat, blon, blat, userName, timestamp, fpm, path);

				}
				reader.close();


			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}




	/**
	 * @author ytt
	 * 删除某个poi站点下的所有的信息
	 *
	 * */

	public boolean deletePoiPic(String poi){
		byte[] deleteRow = null;
		SingleColumnValueFilter filter = new SingleColumnValueFilter(POI_CF, POI_COL, CompareOp.EQUAL, Bytes.toBytes(poi));
		FilterList filterList = new FilterList();
		filterList.addFilter(filter);
		ResultScanner resultScanner = this.scan(null, null, null, filterList);
		for(Result result : resultScanner){
			for(KeyValue kValue : result.list()){
				deleteRow = kValue.getRow();
				this.deleteColumn(deleteRow, INFO_CF, PIC_COL);
				this.deleteColumn(deleteRow, INFO_CF, FPM_COL);
				this.deleteColumn(deleteRow, INFO_CF, COMPRESS_COL);
				this.deleteColumn(deleteRow, POI_CF, POI_COL);




			}
		}
		return true;


	}



	/**
	 * @author ytt
	 * 2015/7/2
	 * 依据图片的名称 删除某一张照片
	 * 删除pic表以及poi222中的图片
	 * */

	public boolean delPicByPicPath(String picPath){
		POITable poiTable = new POITable();
		byte[] deleteRow = null;
		SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter(INFO_CF, PIC_COL, CompareOp.EQUAL, Bytes.toBytes(picPath));
		Scan scan = new Scan();
		scan.setFilter(singleColumnValueFilter);
		List<byte[]> famList = new ArrayList<byte[]>();
		famList.add(INFO_CF);
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column(INFO_CF, PIC_COL));
		ResultScanner rScanner = this.scan(scan, famList, columns, null);
		for(Result result : rScanner){
			for(KeyValue keyValue : result.list()){
				deleteRow = keyValue.getRow();
				if(keyValue.getQualifier().equals(POI_CF)){
					String deletePicPOI = Bytes.toString(keyValue.getValue());
					Date timestamp = new Date(keyValue.getTimestamp());
					byte[] poiRowkey = poiTable.generateRowKey(deletePicPOI, timestamp);
					poiTable.deleteColumn(poiRowkey, poiTable.DAYDATA_CF, poiTable.PIC_COL);

				}


				this.deleteColumn(deleteRow, INFO_CF, PIC_COL);
				this.deleteColumn(deleteRow, INFO_CF, FPM_COL);
				this.deleteColumn(deleteRow, INFO_CF, COMPRESS_COL);
				this.deleteColumn(deleteRow, POI_CF, POI_COL);

			}
		}
		return true;
	}

	public static void main(String[] args) throws Exception{
		CollectionPicTable picTable = new CollectionPicTable();
		/*
		 * Date today = new Date(1411344653981L); today.setHours(0);
		 *
		 * System.out.println("today is " + today.toGMTString() + "("
		 * +today.getTime()+")");
		 *
		 * List<Pic> picList = new CollectionPicTable().getPic(today); for(Pic
		 * pic : picList){ System.out.println(pic); }
		 */

		//new CollectionPicTable().fun();
		//new CollectionPicTable().fun2();;


		//new CollectionPicTable().fun3("resource/txt/wanglaoshi");
		//new CollectionPicTable().fun3("resource/txt/ytt");
		new CollectionPicTable().fun3("resource/txt/zhenchenghao");
/*
		//new CollectionPicTable().importFakeData("/home/ps/");
		//new CollectionPicTable().getAllPOI();

		//测试删除图片
		//new CollectionPicTable().deletePoiPic("827012727");
*/
		//picTable.deletePoiPic("1474238012");




		//测试删除单张图片
		//System.out.println("test for delPicByPicPath");
		//picTable.delPicByPicPath("vivian_1436687979000.jpg");

	}

}
