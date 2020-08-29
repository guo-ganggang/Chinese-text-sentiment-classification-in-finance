package service.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dao.MicroBlogDAO;

import entity.MicroBlog;

import service.ICTCLASAnalysis.ICTCLASAnalyzer;
/**
 * 抓取的微博数据保存在mySql数据库中，共148523条
 * 本类用于从MySQl数据库中获取微博数据并进行预处理后存入SQL Server数据库
 * @author YHL
 *
 */
public class ReadMicroDatas {
	MicroBlogDAO microDao  =new MicroBlogDAO();
	ICTCLASAnalyzer analyzer = new ICTCLASAnalyzer();
	Set<String> stopWordSet = new HashSet<String>(); 
	
	public void readMicrosFromMySql(){
		//获取停用词表，将停用词存入全局set：stopWordSet中
		ReadFiles reFile = new ReadFiles();
		stopWordSet=reFile.getStopWords();
       // URL指向要访问的数据库名sina_weibo
       String url = "jdbc:mysql://localhost:3306/sina_weibo";
       // MySQL配置时的用户名
       String user = "root"; 
       // MySQL配置时的密码
       String password = "123456";

       try { 
        // 加载驱动程序
        Class.forName("com.mysql.jdbc.Driver");
        // 连续数据库
        Connection conn = DriverManager.getConnection(url, user, password);
        // statement用来执行SQL语句
        Statement statement = conn.createStatement();
        // 要执行的SQL语句
        int count =0;
        int offset =97001;
        while(count<10){
        	//limit 选择1000行，offset 忽略47000行 即从47000行开始选择
	        String sql = "select user,created_at,text from sina_weibo limit 1000 offset "+ offset;
	        // 结果集
	        System.out.println("read data from mysql......:"+count);
	        ResultSet rs = statement.executeQuery(sql);
	        
	        ArrayList<MicroBlog> microList =new ArrayList<MicroBlog>();
	        while(rs.next()) {
	        	MicroBlog micro = new MicroBlog();
		        //1.获取数据列
	        	micro.setUserName(rs.getString("user"));
	        	micro.setCreateTime(rs.getDate("created_at"));
	        	String text = rs.getString("text");
	      
	        	micro.setText(text);
	        	/*
		         // 首先使用ISO-8859-1字符集将name解码为字节序列并将结果存储新的字节数组中。
		         // 然后使用GB2312字符集解码指定的字节数组
		       	// text = new String(text.getBytes("ISO-8859-1"),"GB2312");
	       	 	//2.分词处理
				String stext = analyzer.ICTCLAS_ParaProcess(text);
				String[] sarr = stext.split(" ");
				
				//3.去除停用词
				StringBuilder sb= new StringBuilder();
				for (int i = 0; i < sarr.length; i++) {
					Iterator<String> it = stopWordSet.iterator();
					boolean f = false;
					while(it.hasNext()){
						String s = it.next().toString();
						if(s.equals(sarr[i])){
							f=true;
							break;
						}
					}
					if(!f){
						sb.append(sarr[i]+" ");
					}
					
				}
				micro.setWords(sb.toString());
				
			*/	
				
				microList.add(micro);
		
	        }
	        
	        	
	        System.out.println("read work done!---------:"+count);
	        //3.存入数据库
	        System.out.println("write data to sqlserver........:"+count);
			microDao.insertNewMicros(microList,count);//插入语句
			microList = null;
			 System.out.println("write work done!---------:"+count);
			 rs.close();
			 
			 count++;
			 offset += 1000;
        }
		
        conn.close();
       } catch(ClassNotFoundException e) {
	        System.out.println("Sorry,can`t find the Driver!"); 
	        e.printStackTrace();
       } catch(SQLException e) {
        e.printStackTrace();
       } catch(Exception e) {
        e.printStackTrace();
       }
	}

}
