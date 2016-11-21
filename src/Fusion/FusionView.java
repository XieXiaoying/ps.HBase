package Fusion;

import collection.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.*;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueExcludeFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import Common.Column;

import collection.CollectionTable;


public class FusionView {
	private CollectionTable ct = null;

	private GridTable gt = null;

	private GridComputeInterface gci = null;

	public FusionView(GridComputeInterface gci) {
		ct = new CollectionTable();
		gt = new GridTable();
		this.gci = gci;
	}

	/**
	 * 将CollectionTable表中所有数据根据lon，lat计算得到grid_x,grid_y
	 */
	public void computeGrid() {
		// lon不为空
		SingleColumnValueFilter lon_f = new SingleColumnValueFilter(
				CollectionTable.INFO_CF, Constants.LON.getBytes(),
				CompareFilter.CompareOp.NOT_EQUAL, (byte[]) null);
		lon_f.setFilterIfMissing(true);

		// lat不为空
		SingleColumnValueFilter lat_f = new SingleColumnValueFilter(
				CollectionTable.INFO_CF, Constants.LAT.getBytes(),
				CompareFilter.CompareOp.NOT_EQUAL, (byte[]) null);
		lat_f.setFilterIfMissing(true);
		
		// grid值为空
		SingleColumnValueExcludeFilter null_f = new SingleColumnValueExcludeFilter(
				CollectionTable.GRID_CF, CollectionTable.X_COL,
				CompareFilter.CompareOp.EQUAL, (byte[])null);
		
		
		// 设置扫描列
		List<Column> columnList = new ArrayList<Column>();
		columnList.add(new Column(CollectionTable.INFO_CF, Constants.LON.getBytes()));
		columnList.add(new Column(CollectionTable.INFO_CF, Constants.LAT.getBytes()));
		columnList.add(new Column(CollectionTable.GRID_CF, CollectionTable.X_COL));
		
		ResultScanner rs = ct.scan(null, null, columnList, new FilterList(lon_f,lat_f,null_f));
		if(rs != null){
			//打开写缓存
			ct.setAutoFlush(false);
			for (Result r : rs) {
				if (!r.isEmpty()) {
					byte[] rowkey = r.getRow();
					byte[] lon = r.getValue(CollectionTable.INFO_CF,
							Constants.LON.getBytes());
					byte[] lat = r.getValue(CollectionTable.INFO_CF,
							Constants.LAT.getBytes());

					if (lon != null || lat != null) {
						// 计算Grid
						Grid grid = gci.computer(Bytes.toDouble(lon), Bytes.toDouble(lat));
						// 写入Grid
						List<KeyValue> kvs = new ArrayList<KeyValue>();
						kvs.add(new KeyValue(rowkey,CollectionTable.GRID_CF, CollectionTable.X_COL,
								Bytes.toBytes(grid.getLon())));
						kvs.add(new KeyValue(rowkey,CollectionTable.GRID_CF, CollectionTable.Y_COL,
								Bytes.toBytes(grid.getLat())));
						
						ct.put(rowkey, null, kvs);
					}

				}
			}
			//关闭扫描器
			rs.close();
			//关闭写缓存
			ct.flushCommit();
			ct.setAutoFlush(true);
		}
		
	}
	
	/**
	 * 数据收集表
	 * 查询某个Grid的所有收集光线值，没有则返回null
	 * @param grid
	 * @return
	 */
	public List<Integer> getLightInGrid(Grid grid){
		return ct.getLightInGrid(grid);
	}
		
	
	/**
	 * 获得所有Grid,没有则返回null
	 * @return
	 */
	public Set<Grid> getAllGrid(){
		return ct.getAllGrid();
	}
	
	/**
	 * 保存Grid的融合light值
	 * @param grid
	 * @param light
	 * @return
	 */
	public Boolean setGridLight(Grid grid, int light){
		return gt.setLight(grid, light);
	}
	
	/**
	 * 保存Grid的融合noise值
	 * @param grid
	 * @param light
	 * @return
	 */
	public Boolean setGridNoise(Grid grid, int noise){
		return gt.setNoise(grid, noise);
	}
	
	/**
	 * 获得Grid的融合light值
	 * @param grid
	 * @return
	 */
	public int getGridLight(Grid grid){
		return gt.getLight(grid);
	}
	
	/**
	 * 获得Grid的融合noise值
	 * @param grid
	 * @return
	 */
	public int getGridNoise(Grid grid){
		return gt.getNoise(grid);
	}
	
	/**
	 * 读取startTime至endTime收集的数据
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<FusionData> getCollectedData(Date startTime, Date endTime){
		return ct.getCollectedData(startTime, endTime);	
	}
	
	public static void main(String[] args) {
		/*FusionView fv = new FusionView(null);
		Date endTime = new Date();
		System.out.println(endTime.toGMTString());
		Date startTime = endTime;
		endTime.setHours(startTime.getDate() - 1);
		System.out.println(endTime.toGMTString());
		List<FusionData> fusionDataList = fv.getCollectedData(startTime, endTime);
		
		
		for(FusionData fd : fusionDataList){
			System.out.println(fd);
		}
		
		System.out.println("hello world");*/
	}

}
