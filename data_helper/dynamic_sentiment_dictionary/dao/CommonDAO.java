package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import entity.MicroBlog;

/**
 * 与数据库的交互操作
 * @author GGG
 *
 */
public class CommonDAO {
	static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	static final String URL = "jdbc:sqlserver://localhost:51159;databaseName=SA_CHBlog";
	static final String USER = "sa";
	static final String PWD = "123456";
	static Connection conn=null;
	public static Connection getConnection() {
		try {
				if(conn==null){
					Class.forName(DRIVER);
					conn = DriverManager.getConnection(URL, USER,PWD);
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return conn;
		}
	
	
	
}
