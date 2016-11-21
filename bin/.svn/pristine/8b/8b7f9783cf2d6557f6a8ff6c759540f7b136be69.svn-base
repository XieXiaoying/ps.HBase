package RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import Queue.DataElem.DataType;
import Queue.SingletonDataQueue;

/**
 * 扩展了UnicastRemoteObject类，并实现远程接口 HelloInterface
 */
public class QueueInstance extends UnicastRemoteObject implements QueueInterface {
	
	private static final long serialVersionUID = -2192941667998139407L;
	
	private SingletonDataQueue sdq = null;
	
	@Override
	public SingletonDataQueue getSingletonDataQueue() throws RemoteException {
		return sdq;
	}

	/**
	 * 必须定义构造方法，即使是默认构造方法，也必须把它明确地写出来，因为它必须抛出出RemoteException异常
	 */
	public QueueInstance() throws RemoteException {
		sdq = SingletonDataQueue.getInstance();
	}

	@Override
	public Boolean insert(DataType dt, byte[] content, byte[] uri)
			throws RemoteException {
		// TODO Auto-generated method stub
		return sdq.addELem(dt, content,uri);
	}

	
}
