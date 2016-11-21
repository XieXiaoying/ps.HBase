package model;
import java.io.IOException;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;


public class HelloHbase {
	public static void main(String[] args) throws IOException {
//		Configuration conf = HBaseConfiguration.create();
//		conf.set("hbase.zookeeper.quorum", "182.92.116.126");
//		HBaseAdmin admin = new HBaseAdmin(conf);
//		HTableDescriptor tableDescriptor = admin.getTableDescriptor(Bytes.toBytes("poi222"));
//		byte[] name = tableDescriptor.getName();
//		System.out.println(new String(name));
//		HColumnDescriptor[] columnFamilies = tableDescriptor.getColumnFamilies();
//		for (HColumnDescriptor d : columnFamilies) {
//			System.out.println(d.getNameAsString());
//		}
		//long timeStart = testHtable.date2TimeStamp("2016-03-17 00:00:00", "yyyy-MM-dd HH:mm:ss");
		//long timeEnd = testHtable.date2TimeStamp("2016-03-18 00:00:00", "yyyy-MM-dd HH:mm:ss");
		//testHtable.getData();
		//testHtable.getOnePOIAllPic("1552050738", timeStart, timeEnd);
	}
}