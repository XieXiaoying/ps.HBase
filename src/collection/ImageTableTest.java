package collection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;

import Common.AbstractTable;
import Queue.QueuePutter;

public class ImageTableTest extends AbstractTable{
	
	// 表名
	public static final String tableName = "pic_test";
	// 收集数据列族
	public static final byte[] INFO_CF = "info".getBytes();
	// 图片内容列名
	public static final byte[] PIC_COL = "pic".getBytes();
	
	public ImageTableTest(){
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
			
			hTable = new HTable(Constants.conf, tableName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Boolean setPic( byte[] content, String filePath) {
		byte[] rowKey = filePath.getBytes();
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		kvs.add(new KeyValue(rowKey, INFO_CF, PIC_COL, content));	
		return this.put(rowKey, null, kvs);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ImageTableTest itt = new ImageTableTest();
		for(String filePath : args){
			System.out.println("导入"+filePath);
			File dir = new File(filePath);
			try {
				importJPG(itt,dir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		

	}
	
	private static void importJPG(ImageTableTest itt,File file) throws IOException{
		if(file.isFile() && file.getName().toLowerCase().endsWith(".jpg")){
			byte[] content = QueuePutter.image2bytes(file.getAbsolutePath());
			List<byte[]> subPics = CollectionPicTable.splitPic(content, 1024*1024);
			for(byte[] subPic:subPics){
				itt.setPic(subPic, file.getAbsolutePath());
			}
		}
		else if(file.isDirectory()){
			File[] subfileList = file.listFiles();
			for(File subfile : subfileList){
				importJPG(itt,subfile);
			}
		}
	}
}
