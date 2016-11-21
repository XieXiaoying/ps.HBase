package Mysql;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.leeying.visualization.pool.DBConnectionInfo;

public class DBXMLReader {
	private final static String filePath = "/resource/mysql/databaseConnection.xml";

	private static List<DBConnectionInfo> infoList = new ArrayList<DBConnectionInfo>();

	public static List<DBConnectionInfo> read() {
		if (!infoList.isEmpty()) {
			return infoList;
		}
		try {
			// 获得文件
			File projectDir = new File("");
			String oidFilePath = projectDir.getAbsolutePath() + filePath;
			File oidFile = new File(oidFilePath);

			// 进行解析
			SAXReader reader = new SAXReader();
			Document doc = reader.read(oidFile);
			Element root = doc.getRootElement();// 获取根元素

			String driver = null;
			String urlPrefix = null;
			String user = null;
			String pass = null;
			int minConn = 0;
			int maxConn = 0;
			long timeout = 0;

			Iterator iter = root.elementIterator();
			while (iter.hasNext()) {
				Element element = (Element) iter.next();
				String name = element.getName();
				if (name.equals("driver")) {
					driver = (String) element.getData();
				} else if (name.equals("url")) {
					urlPrefix = (String) element.getData();
				} else if (name.equals("user")) {
					user = (String) element.getData();
				} else if (name.equals("pass")) {
					pass = (String) element.getData();
				} else if (name.equals("minConn")) {
					minConn = Integer.parseInt((String) element.getData());
				} else if (name.equals("maxConn")) {
					maxConn = Integer.parseInt((String) element.getData());
				} else if (name.equals("timeout")) {
					timeout = Long.parseLong((String) element.getData());
				}
			}
			
			iter = root.element("dbs").elementIterator();
			while(iter.hasNext()){
				Element dbName = (Element) iter.next();
				DBConnectionInfo dbConnnectionInfo = new DBConnectionInfo(driver, urlPrefix+dbName.getText(), dbName.getText(), user, pass, minConn, maxConn, timeout);
				infoList.add(dbConnnectionInfo);
			}
			
			return infoList;

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		read();
		
		System.out.println(infoList.size());
		
		for(DBConnectionInfo info : infoList){
			System.out.println(info.getDbName());
			System.out.println(info.getMaxConn());
			System.out.println(info.getUrl());
			System.out.println(info.getUser());
			System.out.println(info.getPassword());
		}
	}

}
