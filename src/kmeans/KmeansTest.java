package kmeans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;





import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Priority;

import cData.Sitepixelxy;
import cData.XMLBuilder;

/**
 * 
 * @author ytt
 * 2015-04-23
 * 基于网格的kmeans算法
 * 
 * 
 * */

public class KmeansTest {
	
	int k;   //指定划分的簇数
	double mu;  //迭代的终止条件,当各个新质心相对于老质心偏移量小于mu时终止迭代
	double[][] center;  //上一次各个簇质心的位置
	List<Point> nearList;
	int repeat; //重复运行的次数
	private KmeansTable kms;
	
	public KmeansTest( double mu, int repeat){
		this.k = 0;
		this.mu = mu;
		this.repeat = repeat;
		this.nearList = new ArrayList<Point>();
		this.kms = new KmeansTable();
	
		
	}
	public KmeansTest(){
		this.k = 0;
		this.mu = 1E-10;
		this.repeat = 7;
		this.nearList = new ArrayList<Point>();
		this.kms = new KmeansTable();
	}
	
	public void resetKnum(){
		this.k = 0;
	}
	
    //计算两个点之间的距离
	public static double getdoubleDistans(double[] center1 , double[] center2){
		double double_x = Math.abs(center1[0] - center2[0]);
		double double_y = Math.abs(center1[1] - center2[1]);
		return Math.sqrt(double_x * double_x + double_y * double_y);
	}
	
	public static double getdoubleDistans(double x1, double y1, double x2, double y2){
		double double_x = Math.abs(x1 - x2);
		double double_y = Math.abs(y1 - y2);
		return Math.sqrt(double_x * double_x + double_y * double_y);
	}
	
	
	
	/***
	 * 
	 * 以网格为初始的质心
	 * 
	 * */
	public void initCenter(int zoom){
		ResultScanner resultScanner = kms.getResource(zoom);
		Iterator<Result> iterator = resultScanner.iterator();
		
		List<double[]> sumlist = new ArrayList<double[]>();
   		
		//进入到簇的范畴
		while(iterator.hasNext()){
			this.k ++;
			
			//count代表每簇的点的个数
			int count = 0;
			//sumindex中存在的是[所有点的pixelX的和，所有点的pixelY的和，所有点的个数]
			double[] sumindex = new double[3];
	
			Result result = iterator.next();
			
			
			//进入到每个簇下的每个点的范畴
			for(KeyValue kvs : result.list()){
				
				String keyvalue = Bytes.toString(kvs.getValue());
				String[] keyString = keyvalue.substring(1, keyvalue.length()-1).split(",");
				for(int j = 0; j < keyString.length; j++){
					count++;
					String[] poitStrings = keyString[j].split("/");
					sumindex[0] += Double.valueOf(poitStrings[0]);
					sumindex[1] += Double.valueOf(poitStrings[1]);
					
				}
				
			}
			//将每个簇内的点的个数保存下来
			sumindex[2] = count;
			sumlist.add(sumindex);
			
			
		}
		center = new double[k][];
		for(int i = 0; i < k; i++){
			center[i] = new double[2];
		}
		for(int i = 0; i < k; i++){
			center[i][0] = sumlist.get(i)[0] / sumlist.get(i)[2];
			center[i][1] = sumlist.get(i)[1] / sumlist.get(i)[2];
			
			//System.out.println(String.valueOf(center[i][0] + "    " + center[i][1]));
		}
		
		
		
	}
	

		//把数据集中的每一个点归到离他最近的那个质心
		public void classify(ArrayList<Point> objects){
			Iterator<Point> iterator2 = objects.iterator();
			while(iterator2.hasNext()){
				Point object = iterator2.next();
				double[] vector = object.getVector();
				int len = vector.length;
				int index = 0;
				double neardist = Double.MAX_VALUE;
				for(int i = 0; i < k; i++){
					double dist = object.getDistance(center[i][0], center[i][1]);
					if(dist < neardist){
						neardist = dist;
						 index = i;
					}
				}
				object.setCid(index);
			}
		}
	
		
		

	
	//重新计算每个簇的质心，并判断终止条件是否满足，如果不满足更新个簇的的质心，如果满足就返回true
	public boolean calNewCenter(ArrayList<Point> objects){
		boolean end = true;
		 int[] count = new int[k];
		 double[][] sum = new double[k][];
		 for(int i = 0; i < k; i++){
			 sum[i] = new double[2];
		 }
		 Iterator<Point> iterator = objects.iterator();
		 while(iterator.hasNext()){
			 Point object = iterator.next();
			 int id = object.getCid();
			 count[id]++;
			 for(int i = 0; i < 2; i++){
				 sum[id][i] += object.getVector()[i];
			 }
		 }
		 for(int i = 0; i < k; i++){
			 if(count[i] != 0){
				 for(int j = 0; j < 2; j++){
					 sum[i][j] /= count[i];
				 }
			 }
			else{
				int a = (i+1)%k;
				int b = (i+3)%k;
				int c = (i+5)%k;
				for(int j = 0; j < 2; j++){
					center[i][j] = (center[a][j] + center[b][j] + center[c][j])/3;
						 
				}
			}
		}
		for(int i = 0; i < k; i++){
			if(getdoubleDistans(center[i], sum[i]) >= mu){
				end = false;
				break;
			}
		}
			 
		if(!end){
			System.out.println("没有终止！ --1");
			for(int i = 0; i < k; i++){
				for(int j = 0; j < 2; j++){
					center[i][j] = sum[i][j];
				 }
			}
		}
		
		return end;
			 
		 
	}
	
	
	/**
	 * @author ytt
	 * 展示聚合完之后的离质心最近的点
	 * 
	 * **/
	//寻找离质心最近的点
	public void findTheNearest(ArrayList<Point> objects){
		for(int i = 0; i < k; i++){
			Point nearPoint = new Point();
			int nearPointId = 0;
			double neardic = Double.MAX_VALUE;
			Iterator<Point> iterator = objects.iterator();
			while(iterator.hasNext()){
				Point point = iterator.next();
				if(point.getCid() == i){
					double dic = point.getDistance(center[i][0], center[i][1]);
					
					if(dic < neardic){
						nearPointId =  objects.indexOf(point);
					}
				}
			}
			nearPoint = objects.get(nearPointId);
			nearList.add(nearPoint);
			
		}	
	}
	
	
	/**
	 * @author ytt
	 * 聚合并且导入到数据库中
	 * 作为其他的工具
	 * 
	 * */
	public boolean clusterAndImport(DataSource datasource, int zoom){
		initCenter(zoom);
		classify(datasource.getObjects());
		
		while(!calNewCenter(datasource.getObjects())){
			classify(datasource.getObjects());
		}
		System.out.println("聚合结果导入！ ----1  ");
		findTheNearest(datasource.getObjects());
		System.out.println("聚合结果导入！ ----2  ");
		
		//得到聚合的结果
		ArrayList<ArrayList<Point>> clusterResult = datasource.getResult(
				datasource.getObjects(), k);
		
		//聚合后的质心们
		List<Point> centerList = new ArrayList<Point>();
		//聚合之后计算，所有质心与监测站点的距离倒数，即权值
		
		System.out.println("聚合结果导入！ ----3  ");
		for(int i = 0; i < k; i++ ){
			centerList.add(new Point(center[i][0], center[i][1]));
			List<Double> prios = new ArrayList<Double>();
			
			//首先需要判断nearest点属于哪个省市区域，选择该城市的监测点的xml文件，此处暂时预定为beijing.xml
			/*List<Sitepixelxy> sitepixelxies = XMLBuilder.selectPixel("src/cData/beijing.xml", zoom);
			for(Sitepixelxy sitepixelxy : sitepixelxies){
				double[] siteps = new double[2];
				siteps[0] = sitepixelxy.getPixelx();
				siteps[1] = sitepixelxy.getPixely();
				double[] centerps = new double[2];
				centerps[0] = center[i][0];
				centerps[1] = center[i][1];
				double prio = 1/(getdoubleDistans(centerps, siteps));
				prios.add(prio);
			}*/
			
			
			
		}
		
		System.out.println("聚合结果导入！ ----4 ");
		
		//将聚合结果导入到Hbase,titleXY代表对应的图块坐标的值
		for(int i = 0; i < k; i++){
			double[] titleXY = new double[2];
			titleXY = centerList.get(i).getTitleXY();
			
			System.out.println("聚合结果导入！ ----5  ");
			if(!kms.setCluster(zoom, titleXY[0], titleXY[1], 
					clusterResult.get(i).toString(), centerList.get(i).toString(),
					nearList.get(i).toString(),null)){
				return false;
			}
			
			
			
		}
		
		
		return true;
	}
	
	/**
	 * 从数据库中取出所有的级别下数据并聚合,导入到数据库
	 * 
	 * 
	 * */
	public boolean clusterAll(){
		for(int i = 1; i < 19;i++){
			System.out.println(i + "  缩放级别0");
			this.resetKnum();
			this.nearList.clear();
			if(!this.cluster(i)){
				return false;
			}
			
			
		}
		return true;
	}
	
	/**
	 * @author ytt
	 * 
	 * 对某一个集合下的所有的点 取出、聚合、导入到数据库
	 * 
	 * */
	public boolean cluster(int zoom){
			
			ArrayList<Point> pList = new ArrayList<Point>();
			ResultScanner result = kms.getResource(zoom);
			Iterator<Result> iterator = result.iterator();
			while(iterator.hasNext()){
				Result rs = iterator.next();
				for(KeyValue kvs : rs.list()){
					String keyStrings = Bytes.toString(kvs.getValue());
					String valueString = keyStrings.substring(1, keyStrings.length()-1);
					String[] keyString = valueString.split(",");
					for(int j = 0; j < keyString.length; j++){
						String[] pointStrings = keyString[j].split("/");
						pList.add(new Point(Double.valueOf(pointStrings[0]), Double.valueOf(pointStrings[1]),
								Double.valueOf(pointStrings[2]), Double.valueOf(pointStrings[3]), pointStrings[4]));
					}
				}
			}
			
			DataSource dataSource = new DataSource(pList);
			System.out.println("cluster开始！");
			
			if(!clusterAndImport(dataSource,zoom)){
				System.out.println("在zoom为： " + zoom + "时，聚合结果失败！");
			}
		
			System.out.println("cluster 成功！");
		
		return true;
		
		
	}
	
	
	public static void main(String[] args){
		//随机存储一些点
		
		KmeansTable kmeTable = new KmeansTable();
		//kms.setCluster(1, titleX, titleY, cluster, center, timestamp)
		Date before = new Date();
		System.out.println(before.toString());
		KmeansTest kmtest = new KmeansTest(1E-10, 7);
		//kmtest.initCenter(14);
		//kmtest.cluster(14);
		kmtest.clusterAll();
		//km.clusterAndImport(datasource, 3);
		Date laterDate = new Date();
		System.out.println(laterDate.toString());
	}
	
}
