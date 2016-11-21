package Queue;

import java.util.concurrent.*;

import org.apache.log4j.Logger;

/**
 * 缓存队列
 * 单例模式     从而方便对实例个数的控制并节约系统资源。如果希望在系统中某个类的对象只能存在一个，单例模式是最好的解决方案。

 * 
 * @author leeying
 * 
 */
public class SingletonDataQueue {
	static Logger logger = Logger.getLogger(SingletonDataQueue.class);  
	// 单例锁
	private static final Object singletonLock = new Object();
	// 单实例
	private static SingletonDataQueue uniqueInstance = null;
	// 队列容量
	public static final int qListCapacity = 1000;

	private BlockingQueue<DataElem> qList;

	// 构造函数
	private SingletonDataQueue(int qListCapacity) {
		// 不同于LinkedBlockingQueue,读写共用一个锁,在插入或删除元素时不会产生或销毁任何额外的对象实例，而后者则会生成一个额外的Node对象。
		qList = new ArrayBlockingQueue<DataElem>(qListCapacity, true);
	}
	
	// 获取单例
	public static SingletonDataQueue getInstance() {
		if (uniqueInstance == null) {
			// 线程同步
			synchronized (singletonLock) {
				if (uniqueInstance == null) {
					uniqueInstance = new SingletonDataQueue(qListCapacity);
				}
			}
		}
		return uniqueInstance;
	}
	
	public int getSize(){
		int size = 0;
		size = qList.size();
		return size;
	}

	synchronized public boolean addELem(DataElem.DataType dt, byte[] content, byte[] uri) {
		//logger.info("QueueSize = " + qList.size());
		DataElem elem = new DataElem(dt, content,uri);
		// 成功返回true，否则返回false
		// 用了非阻塞方法，阻塞方法是put，内部会notify
		return qList.offer(elem);
		
	}

	synchronized public DataElem getAndRemoveELem() {
		//logger.info("QueueSize = " + qList.size());
		DataElem elem = qList.peek();
		if (elem != null) {
			qList.poll();
		}
		
		return elem;
	}

	public void clearQueue() {
		qList.clear();
	}


}
