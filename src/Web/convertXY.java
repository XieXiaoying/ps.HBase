package Web;

import collection.Constants;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.Hbase.increment_args;

import net.sf.json.JSONObject;


/**
 * @author ytt
 * 坐标的转换
 * 利用百度接口坐标转换，同一个GPS坐标多次转换，每次都会有不同的结果，误差在两米之内，不收影响
 * @return 0代表lon，1代表lat
 * */
public class convertXY {
	@SuppressWarnings("unchecked")
	public static double[] convertBase(double lon,double lat, int from, int to){
		double[] convered = new double[2];
		String reslut = "";
		String path =  "http://api.map.baidu.com/geoconv/v1/?coords=" + lon + "," +
					lat + "&from=" + from + "&to=" + to + "&ak=OtN1RDWlrzUaj69pcAIvfujI";
		
		try{
			URL url = new URL(path);
			URLConnection connection = url.openConnection();
	         // 设置通用的请求属性
	        connection.setRequestProperty("accept", "*/*");
	        connection.setRequestProperty("connection", "Keep-Alive");
	        connection.setRequestProperty("user-agent",
	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	         // 建立实际的连接
	        connection.connect();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while((line = in.readLine()) != null){
				reslut += line;
			}
	        
			JSONObject resultjson = JSONObject.fromObject(reslut);
			List<JSONObject> corred = new ArrayList<JSONObject>();
			if(resultjson.getInt("status") == 0){
				corred = (List<JSONObject>) resultjson.get("result");
				convered[0] = corred.get(0).getDouble("x");
				convered[1] = corred.get(0).getDouble("y");				
			}
			else{
				System.out.println("eeror" + resultjson.getInt("status"));
				return null;
			}
		}catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		//System.out.println(convered[0] + "   " + convered[1]);
		return convered;
	}
	
	
	//gps转换成百度坐标
	public static double[] gpsTobaiducor(double lon, double lat){
		return convertBase(lon, lat, 1, 5);
	}
	//百度坐标转换成平面坐标
	public static double[] gpstoworldcor(double lon, double lat){
		return convertBase(lon, lat, 1, 6);
	}
	
	//平面坐标转化成像素坐标
	public static double[] worldtopixel(double worldx, double worldy, int zoom){
		double[] pixelcor = new double[2];
		pixelcor[0] = Math.floor(worldx * Math.pow(2, zoom - 18));
		pixelcor[1] = Math.floor(worldy * Math.pow(2, zoom - 18));
		
		//System.out.println(pixelcor[0] + "   " + pixelcor[1]);
		return pixelcor;
	}
	
	//像素坐标转换成图块坐标
	public static double[] pixeltotitle(double pixelcorx, double pixelcory){
		double[] titlecor = new double[2];
		titlecor[0] = Math.floor(pixelcorx/256);
		titlecor[1] = Math.floor(pixelcory/256);
		//System.out.println(titlecor[0] + "  " + titlecor[1]);
		return titlecor;
	}
	
	
	
	public static void  main(String[] args){
		//convertBase(114.21892734521, 29.575429778924,1, 5);
		//worldtopixel(12961630.95, 4817603.74, 11);
		//pixeltotitle(101262, 37637);
		//39.959447222222224,"lon":116.35073055555554
		double[] baidu = gpsTobaiducor(116.35073055555554,39.959447222222224);
		double[] world = gpstoworldcor(116.30780190719,40.013424612535);
		double[] pixel = worldtopixel(world[0], world[1], 8);
		double[] title = pixeltotitle(pixel[0], pixel[1]);
		
		//百度 教三 116.363486,39.966476
		
		//高德  教三 116.356984,39.960548
		double Gaodelon = baidu[0] - 0.006501999999997565;
		double Gaodelat = baidu[1] - 0.006027999999997269;
		System.out.println("高德坐标" + Gaodelon + "," + Gaodelat);
		
		
		System.out.println(baidu[0] + "," + baidu[1]);
		System.out.println(world[0] + "/" + world[1]);
		System.out.println(pixel[0] + "/" + pixel[1]);
		System.out.println(title[0] + title[1]);
		ComputePOI.computePOI(baidu[0], baidu[1]);
		ComputePOI.computePOI(baidu[0], baidu[1]);
		ComputePOI.computePOI(baidu[0], baidu[1]);
		ComputePOI.computePOI(baidu[0], baidu[1]);
	}
	
}
