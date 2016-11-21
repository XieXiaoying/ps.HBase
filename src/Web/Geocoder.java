package Web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONObject;

public class Geocoder {
	
	private static String url4baiduAPI = "http://api.map.baidu.com/geocoder/v2/?ak=OtN1RDWlrzUaj69pcAIvfujI&output=json";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println(Geocoder.baiducoor2address4road(39.960023,116.352423));
		

	}
	  public static String getHTML(String urlToRead) {
	      URL url;
	      HttpURLConnection conn;
	      BufferedReader rd;
	      String line;
	      String result = "";
	      try {
	         url = new URL(urlToRead);
	         conn = (HttpURLConnection) url.openConnection();
	         conn.setRequestMethod("GET");
	         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	         while ((line = rd.readLine()) != null) {
	            result += line;
	         }
	         rd.close();
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	      return result;
	   }
	
	/**
	 * 通过经纬度返回位置描述，出错返回null
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	
	public static String gps2address(double lat, double lon) {
		//构造请求url
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(url4baiduAPI);
			sb.append("&coordtype=wgs84ll"+"&location="+lat+","+lon);
			
			String url = sb.toString();
			//发送同步get请求
			String jsonResponseString  = getHTML(url);
			
			//解析json
			JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
			JSONObject result = (JSONObject) jsonResponseObject.get("result");
			String description = result.getString("sematic_description");
			if (description == null || description.equals("") ) {
				description = result.getString("formatted_address");
			}
			return description;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		
		return null;
	}
	//返回市、县、街道
	public static String gps2address4road(double lat, double lon) {
		//构造请求url
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(url4baiduAPI);
			sb.append("&coordtype=wgs84ll"+"&location="+lat+","+lon);
			
			String url = sb.toString();
			//发送同步get请求http://blog.csdn.net/cheney521/article/details/8511759
			String jsonResponseString  = getHTML(url);
//			System.out.println(jsonResponseString);
			
			//解析json
			JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
			JSONObject result = (JSONObject) jsonResponseObject.get("result");
			JSONObject addressComponent = result.getJSONObject("addressComponent");
			//清空sb
			sb.delete(0, sb.length());
			sb.append(addressComponent.getString("city"));
			sb.append(addressComponent.getString("district"));
			sb.append(addressComponent.getString("street"));
			
			String description = sb.toString();
			if (description == null || description.equals("") ) {
				description = result.getString("formatted_address");
			}
			return description;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		
		return null;
	}
	
	
	public static String baiducoor2address4road(double lat, double lon) {
		//构造请求url
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(url4baiduAPI);
			sb.append("&location="+lat+","+lon);
			
			String url = sb.toString();
			//发送同步get请求
			String jsonResponseString  = getHTML(url);
//			System.out.println(jsonResponseString);
			
			//解析json
			JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
			JSONObject result = (JSONObject) jsonResponseObject.get("result");
			JSONObject addressComponent = result.getJSONObject("addressComponent");
			//清空sb
			sb.delete(0, sb.length());
			sb.append(addressComponent.getString("city"));
			sb.append(addressComponent.getString("district"));
			sb.append(addressComponent.getString("street"));
			
			String description = sb.toString();
			if (description == null || description.equals("") ) {
				description = result.getString("formatted_address");
			}
			return description;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		
		return null;
	}
	/**
	 * 同上，只是用的是百度经纬度
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static String baiducoor2address(double lat, double lon) {
		//构造请求url
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(url4baiduAPI);
			sb.append("&location="+lat+","+lon);
			
			String url = sb.toString();
			//发送同步get请求
			String jsonResponseString  = getHTML(url);
			
			//解析json
			JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
			JSONObject result = (JSONObject) jsonResponseObject.get("result");
			String description = result.getString("sematic_description");
			if (description == null || description.equals("") ) {
				description = result.getString("formatted_address");
			}
			return description;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		
		return null;
	}
	
	public static String baiducoor2DetailAddress(double lat, double lon) {
		//构造请求url
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(url4baiduAPI);
			sb.append("&location="+lat+","+lon);
			
			String url = sb.toString();
			//发送同步get请求
			String jsonResponseString  = getHTML(url);
			
			//解析json
			JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
			JSONObject result = (JSONObject) jsonResponseObject.get("result");
			JSONObject addressComponent = result.getJSONObject("addressComponent");
			//清空sb
			sb.delete(0, sb.length());
//			sb.append(addressComponent.getString("city"));
			sb.append(addressComponent.getString("district"));
			sb.append(addressComponent.getString("street"));
			sb.append(result.getString("sematic_description"));
			
			String description = sb.toString();
			
//			if (description == null || description.equals("") ) {
//				description = result.getString("formatted_address");
//			}
			
			return description;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		
		return null;
	}
	
	//返回市、县、街道
		public static String gps2DetailAddress(double lat, double lon) {
			//构造请求url
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(url4baiduAPI);
				sb.append("&coordtype=wgs84ll"+"&location="+lat+","+lon);
				
				String url = sb.toString();
				//发送同步get请求
				String jsonResponseString  = getHTML(url);
				System.out.println(jsonResponseString);
				
				//解析json
				JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
				JSONObject result = (JSONObject) jsonResponseObject.get("result");
				JSONObject addressComponent = result.getJSONObject("addressComponent");
				//清空sb
				sb.delete(0, sb.length());
				sb.append(addressComponent.getString("city"));
				sb.append(addressComponent.getString("district"));
				sb.append(addressComponent.getString("street"));
				sb.append(result.getString("sematic_description"));
				
				String description = sb.toString();
//				if (description == null || description.equals("") ) {
//					description = result.getString("formatted_address");
//				}
				return description;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}	
			
			return null;
		}
		
		public static String gps2SegmentAdressForGridPoiTable_Two(double lat, double lon) {
			//构造请求url
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(url4baiduAPI);
				sb.append("&coordtype=wgs84ll"+"&location="+lat+","+lon);
				
				String url = sb.toString();
				//发送同步get请求
				String jsonResponseString  = getHTML(url);
//				System.out.println(jsonResponseString);
				
				//解析json
				JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
				JSONObject result = (JSONObject) jsonResponseObject.get("result");
				JSONObject addressComponent = result.getJSONObject("addressComponent");
				//清空sb
				sb.delete(0, sb.length());
				sb.append(addressComponent.getString("city"));
				sb.append(addressComponent.getString("district"));
				sb.append(addressComponent.getString("street"));
				
				String description = sb.toString();
				return description;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}	
			
			return null;
		}
		public static String gps2SegmentAdressForGridPoiTable_Three(double lat, double lon) {
			//构造请求url
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(url4baiduAPI);
				sb.append("&coordtype=wgs84ll"+"&location="+lat+","+lon);
				
				String url = sb.toString();
				//发送同步get请求
				String jsonResponseString  = getHTML(url);
//				System.out.println(jsonResponseString);
				
				//解析json
				JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
				JSONObject result = (JSONObject) jsonResponseObject.get("result");
				JSONObject addressComponent = result.getJSONObject("addressComponent");
				//清空sb
				sb.delete(0, sb.length());
				sb.append(addressComponent.getString("city"));
				sb.append(addressComponent.getString("district"));
//				sb.append(addressComponent.getString("street"));
				
				String description = sb.toString();
				return description;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}	
			
			return null;
		}
		public static String gps2SegmentAdressForGridPoiTable_Four(double lat, double lon) {
			//构造请求url
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(url4baiduAPI);
				sb.append("&coordtype=wgs84ll"+"&location="+lat+","+lon);
				
				String url = sb.toString();
				//发送同步get请求
				String jsonResponseString  = getHTML(url);
//				System.out.println(jsonResponseString);
				
				//解析json
				JSONObject jsonResponseObject = JSONObject.fromObject(jsonResponseString);
				JSONObject result = (JSONObject) jsonResponseObject.get("result");
				JSONObject addressComponent = result.getJSONObject("addressComponent");
				//清空sb
				sb.delete(0, sb.length());
				sb.append(addressComponent.getString("city"));
//				sb.append(addressComponent.getString("district"));
//				sb.append(addressComponent.getString("street"));
				
				String description = sb.toString();
				return description;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}	
			
			return null;
		}

}
