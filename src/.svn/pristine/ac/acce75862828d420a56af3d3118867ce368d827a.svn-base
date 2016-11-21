package Queue;

import java.util.Arrays;

/**
 * 缓存队列中的元素
 * @author leeying
 *
 */

// 数据是JSON串还是图片
//enum DataType{JSON,PIC};

/**
 * @author leeying
 *
 */
public class DataElem {
	// 数据是JSON串还是图片
	public enum DataType {
		JSON,PIC
	};
	private DataType dt; 
	// 内容
	private byte[] content;
	
	// 路径
	private byte[] uri;
	
	// 构造函数
	public DataElem(DataType dt2, byte[] content, byte[] uri) {
		this.dt = dt2;
		this.uri = uri;
		//因为新增了一个引用，所以content的内存不会被回收
		this.content = content;
		
	}
	
	public byte[] getUri(){
		return uri;
	}


	public DataType getDt() {
		return dt;
	}
	
	public byte[] getContent(){
		return content;
	}
	
	public String getContentInString(){
		return new String(content);
	}

	@Override
	public String toString() {
		return "DataElem [dt=" + dt + ", content=" + Arrays.toString(content)
				+ ", uri=" + Arrays.toString(uri) + "]";
	}


	
	
	
	

}
