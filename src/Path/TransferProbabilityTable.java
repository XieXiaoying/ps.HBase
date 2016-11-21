package Path;

import collection.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import Common.AbstractTable;


/**
 * 转移概率表
 * 
 * @author leeying
 * 
 */

public class TransferProbabilityTable extends AbstractTable{

	public enum TransferProbabilityType {
		// 一阶转移概率表
		FIRSTORDER_TRANSFER,
		// 二阶转移概率表
		SECONDORDER_TRANSFER
	};

	// 表名
	private static final String tableName = "transfer";
	// 列族名
	private static final byte[] INFO_CF = "info".getBytes();
	// 一阶转移概率列名
	private static final byte[] FIRST_ORDER_PRO = "pro1".getBytes();
	// 二阶转移概率列名
	private static final byte[] SECOND_ORDER_PRO = "pro2".getBytes();

	public TransferProbabilityTable(TransferProbabilityType tpt)
			throws Exception {
		try {
			hTable = new HTable(Constants.conf, tableName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public byte[] generateRowKey(int startRegion, int endRegion){
		String rowKey = "" + startRegion + Constants.SEPARATER + endRegion;
		return rowKey.getBytes();
	}
	
	
	/**
	 * 保存从startRegion到endRegion的概率
	 * @param startRegion
	 * @param endRegion
	 * @param type 指明是一阶转移概率还是二阶转移概率
	 * @param pro
	 */
	public Boolean setProbability(int startRegion,int endRegion,TransferProbabilityType type,double pro){
		byte[] rowKey = generateRowKey(startRegion, endRegion);
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		// 一阶概率
		if(type == TransferProbabilityType.FIRSTORDER_TRANSFER){
			kvs.add(new KeyValue(rowKey,INFO_CF,FIRST_ORDER_PRO,Bytes.toBytes(pro)));
		}
		else if(type == TransferProbabilityType.SECONDORDER_TRANSFER){
			kvs.add(new KeyValue(rowKey,INFO_CF,SECOND_ORDER_PRO,Bytes.toBytes(pro)));
		}
		else{
			return false;
		}
		return this.put(rowKey, null, kvs);
	}
	
	/**
	 * 读取从startRegion到endRegion的概率
	 * 没有则返回-1
	 * @param startRegion
	 * @param endRegion
	 * @param type 指明是一阶转移概率还是二阶转移概率
	 * @return
	 */
	public double getProbability(int startRegion,int endRegion,TransferProbabilityType type){
		List<byte[]> cfList = new ArrayList<byte[]>();
		cfList.add(INFO_CF);
		Result r =  this.get(generateRowKey(startRegion, endRegion)
				, null, cfList, null, false);
		if(!r.isEmpty()){
			if(type == TransferProbabilityType.FIRSTORDER_TRANSFER){
				byte[] val = r.getValue(INFO_CF, FIRST_ORDER_PRO);
				if(val != null){
					return Bytes.toDouble(val);
				}
			}
			else if(type == TransferProbabilityType.SECONDORDER_TRANSFER){
				byte[] val = r.getValue(INFO_CF, SECOND_ORDER_PRO);
				if(val != null){
					return Bytes.toDouble(val);
				}
			}
			
		}
		return -1;
	}
	

}
