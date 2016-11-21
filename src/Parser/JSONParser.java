package Parser;

import collection.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import collection.CollectionTable;


public final class JSONParser {

	public static List<Put> parseJSON2Puts(String string) {
		// 轨迹列族名
		byte[] cf = CollectionTable.INFO_CF;		
				
		JSONObject jo = JSONObject.fromObject(string);
		String username = jo.getString(Constants.USERNAME);
		
		JSONArray recordList = jo.getJSONArray(Constants.RECORDLIST);
		List<Put> resultList = new ArrayList<Put>();
		
		for (int i = 0; i < recordList.size(); i++) {
			JSONObject job = recordList.getJSONObject(i);
			String ts_str = job.getString(Constants.TIMESTAMP);
			
			if (ts_str != null) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					Date timestamp = sdf.parse(ts_str);
					long time = timestamp.getTime();
					byte[] rowKey = CollectionTable.generateRowKey(username,timestamp);
					Put put = new Put(rowKey);
					
					if (job.has(Constants.LON)) {
						double longitude = job.getDouble(Constants.LON);
						put.add(cf, Bytes.toBytes(Constants.LON),time,Bytes.toBytes(longitude));
					}
					if (job.has(Constants.LAT)) {
						double latitude = job.getDouble(Constants.LAT);
						put.add(cf, Bytes.toBytes(Constants.LAT),time,Bytes.toBytes(latitude));
					}			
					if (job.has(Constants.LIGHT)) {
						double light = job.getDouble(Constants.LIGHT);
						put.add(cf, Bytes.toBytes(Constants.LIGHT),
								time,Bytes.toBytes(light));
					}

					if (job.has(Constants.NOISE)) {
						double noise = job.getDouble(Constants.NOISE);
						put.add(cf, Bytes.toBytes(Constants.NOISE),
								time,Bytes.toBytes(noise));
					}

					if (job.has(Constants.OREN_X)) {
						double orien_x = job.getDouble(Constants.OREN_X);
						put.add(cf, Bytes.toBytes(Constants.OREN_X),
								time, Bytes.toBytes(orien_x));
					}

					if (job.has(Constants.OREN_Y)) {
						double orien_y = job.getDouble(Constants.OREN_Y);
						put.add(cf, Bytes.toBytes(Constants.OREN_Y),
								time, Bytes.toBytes(orien_y));
					}

					if (job.has(Constants.BATTERY)) {
						int battery = job.getInt(Constants.BATTERY);
						put.add(cf, Bytes.toBytes(Constants.BATTERY),
								time, Bytes.toBytes(battery));
					}

					if (job.has(Constants.CONNECT)) {
						int connect = job.getInt(Constants.CONNECT);
						put.add(cf, Bytes.toBytes(Constants.CONNECT),
								time, Bytes.toBytes(connect));
					}

					if (job.has(Constants.CHARGE)) {
						int charge = job.getInt(Constants.CHARGE);
						put.add(cf, Bytes.toBytes(Constants.CHARGE),
								time, Bytes.toBytes(charge));
					}
					
					resultList.add(put);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
			}// end if

		}// end for
		
		return resultList;
	}
	
	
	public static List<String> parseJSON2SQLs(String string) {
		JSONObject jo = JSONObject.fromObject(string);
		String username = jo.getString(Constants.USERNAME);

		JSONArray recordList = jo.getJSONArray(Constants.RECORDLIST);
		List<String> resultList = new ArrayList<String>();
		for (int i = 0; i < recordList.size(); i++) {
			
			StringBuilder columnList = new StringBuilder();
			StringBuilder valueList = new StringBuilder();
			
			columnList.append(Constants.USERNAME);
			valueList.append("'" + username + "'");
			
			JSONObject job = recordList.getJSONObject(i);
			String ts_str = job.getString(Constants.TIMESTAMP);
			
			if (ts_str != null) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd hh:mm:ss");
					Date timestamp = sdf.parse(ts_str);
					
					columnList.append("," + Constants.TIMESTAMP );
					valueList.append("," + "'" +ts_str + "'");

					if (job.has(Constants.LON)) {
						double longitude = job.getDouble(Constants.LON);
						columnList.append("," + Constants.LON );
						valueList.append("," + longitude);
					}
					if (job.has(Constants.LAT)) {
						double latitude = job.getDouble(Constants.LAT);
						columnList.append("," + Constants.LAT);
						valueList.append("," + latitude);
					}
					if (job.has(Constants.LIGHT)) {
						double light = job.getDouble(Constants.LIGHT);
						columnList.append("," + Constants.LIGHT);
						valueList.append("," + light);
					}

					if (job.has(Constants.NOISE)) {
						double noise = job.getDouble(Constants.NOISE);
						columnList.append("," + Constants.NOISE);
						valueList.append("," + noise);
					}

					if (job.has(Constants.OREN_X)) {
						double orien_x = job.getDouble(Constants.OREN_X);
						columnList.append("," + Constants.OREN_X);
						valueList.append("," + orien_x);
					}

					if (job.has(Constants.OREN_Y)) {
						double orien_y = job.getDouble(Constants.OREN_Y);
						columnList.append("," + Constants.OREN_Y);
						valueList.append("," + orien_y);
					}

					if (job.has(Constants.BATTERY)) {
						int battery = job.getInt(Constants.BATTERY);
						columnList.append("," + Constants.BATTERY);
						valueList.append("," + battery);
					}

					if (job.has(Constants.CONNECT)) {
						int connect = job.getInt(Constants.CONNECT);
						columnList.append("," + Constants.CONNECT);
						valueList.append("," + connect);
					}

					if (job.has(Constants.CHARGE)) {
						int charge = job.getInt(Constants.CHARGE);
						columnList.append("," + Constants.CHARGE);
						valueList.append("," + charge);
					}
					
					String SQLCommand = "insert into collection("+columnList.toString() + ") values (" + valueList.toString() + ");";
					
					resultList.add(SQLCommand);
				} catch (ParseException e) {
					e.printStackTrace();
				}

			}// end if

		}// end for

		return resultList;
	}

}
