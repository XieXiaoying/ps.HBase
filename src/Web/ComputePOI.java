package Web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.sf.json.JSONObject;


/***
 * 
 * POI_id的计算
 * @author ytt
 * @time 2015/4/5
 * 
 * 
 * 
 * 
 * */


public class ComputePOI {
	
	public static String sendGet(String url, String param){
		String result = "";
		BufferedReader in = null;
		try{
			String urlString = url + "?" + param;
			URL realUrl = new URL(urlString);
			
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while((line = in.readLine()) != null){
				result += line;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	public static String sendPost(String url, String param) {  
	        PrintWriter out = null;  
	        BufferedReader in = null;  
	        String result = "";  
	        try {  
	            URL realUrl = new URL(url);  
	            // 打开和URL之间的连接  
	            URLConnection conn = realUrl.openConnection();  
	            // 设置通用的请求属性  
	            conn.setRequestProperty("accept", "*/*");  
	            conn.setRequestProperty("connection", "Keep-Alive");  
	            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");  
	              
	            conn.setDoOutput(true);// 发送POST请求必须设置如下两行  
	            conn.setDoInput(true);  
	              
	            out = new PrintWriter(conn.getOutputStream());// 获取URLConnection对象对应的输出流s  
	            out.print(param);// 发送请求参数  
	            out.flush();// flush输出流的缓冲  
	            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));// 定义BufferedReader输入流来读取URL的响应  
	            String line;  
	            while ((line = in.readLine()) != null) {  
	                result += "\n" + line;  
	            }  
	        } 
	        catch (Exception e) {  
	            System.out.println("发送POST请求出现异常！" + e);  
	            e.printStackTrace();  
	        }  
	        // 使用finally块来关闭输出流、输入流  
	        finally {  
	            try {  
	                if (out != null) {  
	                    out.close();  
	                }  
	                if (in != null) {  
	                    in.close();  
	                }  
	            } catch (IOException ex) {  
	                ex.printStackTrace();  
	            }  
	        }  
	        return result;  
	    }  
		
	//httpTest test = new httpTest();

	public static String computePOI(double lon, double lat) {
		QueryString qs = new QueryString("ak", "OtN1RDWlrzUaj69pcAIvfujI");
		qs.add("latitude", String.valueOf(lat));
		qs.add("longitude", String.valueOf(lon));
		qs.add("geotable_id", "98977");
		qs.add("coord_type", "1");
		
		
		//System.out.println(qs.toString());
		
		String string = sendPost("http://api.map.baidu.com/geodata/v3/poi/create",qs.toString() );
		//System.out.println(string);
		
		JSONObject object = JSONObject.fromObject(string);
		
		int state = object.getInt("status");
		String POI_id = object.getString("id");
		//System.out.println(POI_id);
		return POI_id;
	}
	
	public static double[] computeLonlat(String poi){
		double[] lonlat = new double[2];
		long poiInt = Long.parseLong(poi);
		int geotable_id = 98977;
		QueryString qs = new QueryString("ak", "OtN1RDWlrzUaj69pcAIvfujI");
		qs.add("id", String.valueOf(poiInt));
		qs.add("geotable_id", String.valueOf(geotable_id));
		
		//System.out.println(qs.toString());
		
		String string = sendGet("http://api.map.baidu.com/geodata/v3/poi/detail",qs.toString());
		System.out.println(string);
		
		JSONObject result = JSONObject.fromObject(string);
		
		int state = result.getInt("status");
		JSONObject poiJsonObject = (JSONObject) result.get("poi");	
		
		String lonlat2 = poiJsonObject.getString("location");
		
		int index = lonlat2.indexOf(",");
		lonlat[0] = Double.valueOf(lonlat2.substring(1, index));
		lonlat[1] = Double.valueOf(lonlat2.substring(index+1, lonlat2.length()-1));
		System.out.println(lonlat[0] + "   " + lonlat[1]);
		return lonlat;
		
	}
	
	
	
	public static void main(String[] args) throws UnsupportedEncodingException{	
		computePOI(129.234, 46.45);
		//827053822
		//computeLonlat("827053822");
		
		
	}


	
}



class QueryString {
	private StringBuffer query = new StringBuffer();


	private synchronized void encode(String name, String value) {
		try {
			query.append(URLEncoder.encode(name, "UTF-8"));
			query.append('=');
			query.append(URLEncoder.encode(value, "UTF-8"));
			
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Broken VM does not support UTF-8");
		}
	}

	
	public QueryString(String name, String value) {
		encode(name, value);
	}

	public synchronized void add(String name, String value) {
		query.append('&');
		encode(name, value);
	}
	public String getQuery() {
		return query.toString();
	}

	public String toString() {
		return getQuery();
	}
}