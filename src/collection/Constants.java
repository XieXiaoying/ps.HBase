package collection;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * 常量�? * @author leeying
 *
 */
public final class Constants {
	public static Configuration conf = HBaseConfiguration.create();
	
	/**
	 * JSON KEY �?	 */
	public final static String USERNAME="username";
	
	public final static String RECORDLIST="data";
	
	public final static String TIMESTAMP="Time";
	
	public final static String LIGHT="Light";
	
	public final static String NOISE ="Noise";
	
	public final static String LON="Longitude";
	
	public final static String LAT ="Latitude";
	
	public final static String OREN_X = "orientation_x";
	
	public final static String OREN_Y = "orientation_y";
	
	public final static String BATTERY = "BatteryState";
	
	public final static String CONNECT = "NetState";
	
	public final static String CHARGE = "ChargeState";
	
	public final static String TID = "tid";
	
	public final static String FPM = "fpm";
	public final static String STATION = "Station";
	
	 
	
	
	/**
	 * HBASE表列族名、列�?	 */
	
	//public final static String COLLECTDATA = "CollectData";
	
	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");
	
	
	
	public final static String SEPARATER = "/";
	
	
		public final static DecimalFormat  DF = new DecimalFormat("0.000000");
}
