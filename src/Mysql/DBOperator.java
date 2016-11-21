package Mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.leeying.visualization.pool.DBConnectionInfo;
import com.leeying.visualization.pool.DBConnectionManager;

/**
 * 数据库操作者
 * 
 * @author leeying
 * 
 */
public class DBOperator {
	// 连接池管理者
	private DBConnectionManager manager;
	// 执行timeout
	private int timeout = 10;
	private Logger logger = Logger.getLogger(DBOperator.class);

	public DBOperator() {
		// 获得数据库连接信息
		List<DBConnectionInfo> infoList = DBXMLReader.read();
		// 初始化线程池
		manager = DBConnectionManager.getInstance(infoList);
	}
	
	public ResultSet doQuery(String dbName, String sql){
		Connection conn = manager.getConnection(dbName);
		try {
			if (conn != null) {

				Statement stmt = conn.createStatement();
				if (stmt != null) {
					// 设置超时时长
					stmt.setQueryTimeout(this.timeout);
					return stmt.executeQuery(sql);
				}
			}
			System.out.println("没法获得连接");
		} catch (SQLTimeoutException e) {
			// 超时重新执行
			// doOperate(dbName, sql);
			System.out.println("[ERROR] " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[ERROR] " + e.getMessage());
		}finally{
			// 释放连接
			manager.freeConnection(dbName, conn);
		}
		return null;
	}
	
	/**
	 * 更新数据库，返回id值
	 * @param dbName
	 * @param sql
	 * @return
	 */
	public int doOperate(String dbName, String sql) {
		// 获得连接
		Connection conn = manager.getConnection(dbName);
		try {
			if (conn != null) {

				Statement stmt = conn.createStatement();
				if (stmt != null) {
					// 设置超时时长
					stmt.setQueryTimeout(this.timeout);
					int ret = stmt.executeUpdate(sql);
					
					ResultSet rs = stmt.executeQuery("select last_insert_id()");
					if(rs.first()){
						return rs.getInt(1);
					}
				}
				
			}
		} catch (SQLTimeoutException e) {
			// 超时重新执行
			doOperate(dbName, sql);
		} catch (SQLException e) {
			System.out.println("[ERROR] " + e.getMessage());
		}finally{
			// 释放连接
			manager.freeConnection(dbName, conn);
		}
		return -1;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DBOperator op = new DBOperator();
		op.doOperate("phy_net", "INSERT INTO data_path(isActive,datapath_name)values(1,'test');");
	}

}
