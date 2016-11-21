package psensing;
import java.sql.*;
public class Myclass {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("success mysql");
		}catch(ClassNotFoundException e1){
			System.out.println("Not Found");
			e1.printStackTrace();
		}
		
		String url="jdbc:mysql://10.108.109.124:33062/test";
		Connection conn;
		
		try{
			conn = DriverManager.getConnection(url,"root", "kqlmysql");
			//Statement stmt = conn.createStatement();
			System.out.print("connection!");
			//stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
}
