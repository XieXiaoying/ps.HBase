package psensing;

import java.sql.*;
import java.util.*;

public class Mysql {
	public String url="jdbc:mysql://10.108.109.124:33062/ps_mysql";
	public Connection conn;
	public Statement stmt;
	public ResultSet rs;
	
	public void connect()
	{
		try{
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("success mysql");
		}catch(ClassNotFoundException e1){
			System.out.println("Not Found");
			e1.printStackTrace();
		}
		
		try{
			conn = DriverManager.getConnection(url,"root", "kqlmysql");
			stmt = conn.createStatement();
			System.out.println("connection!");
			//stmt.close();
			//conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void disClose()
	{
		try{
			 stmt.close();
	         conn.close();
	         System.out.println("Succeeded close to the Database!");
	     }catch(SQLException e){
	      e.printStackTrace();
	     }catch(Exception e) {
	      e.printStackTrace();
	  }
	}
	
	public ArrayList<String> get_users()
	{
		connect();
		String sql = "select user_id from trace group by user_id";
		try{
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rm = rs.getMetaData();
			int col = rm.getColumnCount();
			ArrayList<String> arr = new ArrayList<String>();
			
			while(rs.next())
			{
				for(int i = 1; i <= col;i++)
				{
					arr.add(rs.getString(i));
				}
			}
			return arr;
		}catch(Exception e){
			System.out.println("falied query");
		}
		return null;
	}
	
	/*
	public static void main(String[] args)
	{	
		Mysql test = new Mysql();
		ArrayList<String> ret = test.get_users();
		System.out.println(ret);
	}
	*/
}
