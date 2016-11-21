package RMI;

import java.rmi.*;

import Queue.SingletonDataQueue;
import Queue.DataElem.DataType;

/**   
 * 远程接口必须扩展接口java.rmi.Remote   
 */   
public interface QueueInterface extends Remote    
{    
   /**   
    * 远程接口方法必须抛出 java.rmi.RemoteException   
    * dt 指定是JSON还是Pic
    * content 存放json串，或者PIC解析出来的JSON串
    * uri 对于JSON类型，uri为null， PIC类型，uri为原始图片文件的路径
    */   
   public Boolean insert(DataType dt, byte[] content, byte[] uri) throws RemoteException; 
   
   public SingletonDataQueue getSingletonDataQueue() throws RemoteException;
}
