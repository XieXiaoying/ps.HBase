package Parser;

import collection.Constants;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.hadoop.hbase.util.Bytes;

import net.sf.json.JSONObject;
import collection.CollectionPicTable;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import Queue.QueuePutter;

/**
 * 提取exif中的摄影信息
 * 
 * @author leeying
 * 
 */
public class EXIFParser{
	/**
	 * 解析图片，提取信息，并转化成一个Put
	 * 
	 * @param content 图片内容
	 * @return
	 */
	public static String parse(byte[] content) {
		InputStream is = new ByteArrayInputStream(content);
		JSONObject result = new JSONObject();
		try {
			Metadata metadata = JpegMetadataReader.readMetadata(is);

			Date timestamp = null;
			double lon = -1;
			double lat = -1;
			int fpm = -1;
			String username = null;

			for (Directory directory : metadata.getDirectories()) {
				for (Tag tag : directory.getTags()) {
					//System.out.println(tag.getTagName() + ":" + tag.getDescription());
					if (tag.getTagName() == "Make") {
						//System.out.println(tag.getTagName() + ":" + tag.getDescription());
						JSONObject json = JSONObject.fromObject(tag.getDescription());
						lon = json.getDouble(Constants.LON);
						lat = json.getDouble(Constants.LAT);
						username = json.getString(Constants.USERNAME);
					} else if (tag.getTagName() == "Date/Time") {
						//System.out.println(tag.getTagName() + ":" + tag.getDescription());
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd hh:mm:ss");
						timestamp = sdf.parse(tag.getDescription());
					} else if (tag.getTagName() == "Model") {
						//System.out.println(tag.getTagName() + ":" + tag.getDescription());
						fpm = JSONObject.fromObject(tag.getDescription())
								.getInt("PM2.5");
						
					}
				}
			}
			
			if(timestamp != null && username != null && lon != -1 && lat != -1){
				JSONObject pic = new JSONObject();
				pic.accumulate(Constants.TIMESTAMP, timestamp.getTime());
				pic.accumulate(Constants.LON, lon);
				pic.accumulate(Constants.LAT, lat);
				pic.accumulate(Constants.FPM, fpm);
				pic.accumulate(Constants.USERNAME, username);

				result.accumulate("pic", pic.toString());
			}

			

		} catch (JpegProcessingException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.toString();
		//return null;

	}

	/**
	 * 分割图片 默认情况下，HBase数据块限制为64KB
	 * 
	 * @param content
	 * @param blockSize
	 */
	public static List<byte[]> splitPic(byte[] content, int blockSizeInByte) {
		List<byte[]> subPics = new ArrayList<byte[]>();
		int size = content.length;
		if (size < blockSizeInByte) {
			// 不用分割
			subPics.add(content);
		} else {
			int pos = 0;
			// 分割
			while (size > blockSizeInByte) {
				byte[] subPic = new byte[blockSizeInByte];
				System.arraycopy(content, pos, subPic, 0, blockSizeInByte);
				subPics.add(subPic);
				pos += blockSizeInByte;
				size -= blockSizeInByte;
			}

			// 余下部分
			byte[] subPic = new byte[size];
			System.arraycopy(content, pos, subPic, 0, size);
			subPics.add(subPic);
		}
		return subPics;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			byte[] content = QueuePutter.image2bytes("resource/pic/ST_1401937160075.jpg");
			int len1 = content.length;
			List<byte[]> subPics = CollectionPicTable.splitPic(content, 1024*1024);	
			
			int len2 = 0;
			
			File newJPG = new File("resource/pic/ST_1401937160075.bak.jpg");
			FileOutputStream fos = new FileOutputStream(newJPG);
			for(byte[] subPic : subPics){
				fos.write(subPic);
				len2 += subPic.length;
			}
			
			fos.close();
			
			Assert.assertEquals(len1, len2);
			
			
			
			//System.out.println(EXIFParser.parse(content));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
