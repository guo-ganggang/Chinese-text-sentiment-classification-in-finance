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
 * ץȡ��΢�����ݱ�����mySql���ݿ��У���148523��
 * �������ڴ�MySQl���ݿ��л�ȡ΢�����ݲ�����Ԥ��������SQL Server���ݿ�
 * @author YHL
 *
 */
public class ReadMicroDatas {
	MicroBlogDAO microDao  =new MicroBlogDAO();
	ICTCLASAnalyzer analyzer = new ICTCLASAnalyzer();
	Set<String> stopWordSet = new HashSet<String>(); 
	
	public void readMicrosFromMySql(){
		//��ȡͣ�ôʱ���ͣ�ôʴ���ȫ��set��stopWordSet��
		ReadFiles reFile = new ReadFiles();
		stopWordSet=reFile.getStopWords();
       // URLָ��Ҫ���ʵ����ݿ���sina_weibo
       String url = "jdbc:mysql://localhost:3306/sina_weibo";
       // MySQL����ʱ���û���
       String user = "root"; 
       // MySQL����ʱ������
       String password = "123456";

       try { 
        // ������������
        Class.forName("com.mysql.jdbc.Driver");
        // �������ݿ�
        Connection conn = DriverManager.getConnection(url, user, password);
        // statement����ִ��SQL���
        Statement statement = conn.createStatement();
        // Ҫִ�е�SQL���
        int count =0;
        int offset =97001;
        while(count<10){
        	//limit ѡ��1000�У�offset ����47000�� ����47000�п�ʼѡ��
	        String sql = "select user,created_at,text from sina_weibo limit 1000 offset "+ offset;
	        // �����
	        System.out.println("read data from mysql......:"+count);
	        ResultSet rs = statement.executeQuery(sql);
	        
	        ArrayList<MicroBlog> microList =new ArrayList<MicroBlog>();
	        while(rs.next()) {
	        	MicroBlog micro = new MicroBlog();
		        //1.��ȡ������
	        	micro.setUserName(rs.getString("user"));
	        	micro.setCreateTime(rs.getDate("created_at"));
	        	String text = rs.getString("text");
	      
	        	micro.setText(text);
	        	/*
		         // ����ʹ��ISO-8859-1�ַ�����name����Ϊ�ֽ����в�������洢�µ��ֽ������С�
		         // Ȼ��ʹ��GB2312�ַ�������ָ�����ֽ�����
		       	// text = new String(text.getBytes("ISO-8859-1"),"GB2312");
	       	 	//2.�ִʴ���
				String stext = analyzer.ICTCLAS_ParaProcess(text);
				String[] sarr = stext.split(" ");
				
				//3.ȥ��ͣ�ô�
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
	        //3.�������ݿ�
	        System.out.println("write data to sqlserver........:"+count);
			microDao.insertNewMicros(microList,count);//�������
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
