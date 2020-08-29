package service.common;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.io.BufferedReader; 
import java.io.BufferedWriter;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileNotFoundException; 
import java.io.FileOutputStream; 
import java.io.IOException;
import java.io.InputStreamReader; 
import java.io.OutputStreamWriter;
import java.util.HashSet; 
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import dao.CommonDAO;
import dao.MicroBlogDAO;
import entity.MicroBlog;
import service.ICTCLASAnalysis.ICTCLASAnalyzer;
import net.sf.json.JSONObject;
/**
 * *
 * @author YHL
 *��΢��API��ȡ���ݣ���ʱû��
 */
public class PreceedingData {
		MicroBlogDAO microDao  =new MicroBlogDAO();
		ICTCLASAnalyzer analyzer = new ICTCLASAnalyzer();
		Set<String> stopWordSet = new HashSet<String>(); 
		/**
		 * ����ӿڣ���΢��API��ȡ���ݣ����ִʣ��ִʺ����ݴ������ݿ�
		 */
		public void preMicroDatas(){
			MicroBlogDAO microdao = new MicroBlogDAO();
			//��ȡͣ�ôʱ���ͣ�ôʴ���ȫ��set��stopWordSet��
			ReadFiles reFile = new ReadFiles();
			stopWordSet=reFile.getStopWords();
			Map<Integer,String> list =microdao.getAllMicroText();
			Set<Integer> keys = list.keySet();
			Iterator it = keys.iterator();
			while(it.hasNext()){
				int id = (Integer) it.next();
				String text = list.get(id);
				//2.�ִʴ���
				String stext = analyzer.ICTCLAS_ParaProcess(text);
				String[] sarr = stext.split(" ");
				
				//3.ȥ��ͣ�ô�
				StringBuilder words= new StringBuilder();
				boolean isEmotion = false;
				int count=0;//����ȥ�����͡����õĲ���
				for (int i = 0; i < sarr.length; i++) {
					Iterator<String> sit = stopWordSet.iterator();
					boolean f = false;
					while(sit.hasNext()){
						String s = sit.next().toString();
						if(s.equals(sarr[i]) || sarr[i].indexOf(s)!=-1){
							f=true;break;
						}
					}//�ж��Ƿ�Ϊ������� ,���ڷִʻὫ��������е�[]�ֿ������ڴ��轫������ƴ��
					if(sarr[i].equals("[") || sarr[i].startsWith("[")){
						isEmotion=true;
					}
					if(sarr[i].equals("]") || sarr[i].endsWith("]")){
						isEmotion=false;
					}
					if(sarr[i].equals("�� ") || sarr[i].startsWith("�� ")){
						count=1;
					}
					if(!f && count==0){
						if(isEmotion){
							words.append(sarr[i]);
						}else{
							words.append(sarr[i]+" ");
						}
						
					}
					if(sarr[i].equals("��") || sarr[i].endsWith("��")){
						count=0;
					}
					
				}
				//3.�������ݿ�
				microDao.updateWordsOfMicro(id, words.toString());
			}
			
		}
		/*
		 * �ڲ����ã�������΢��API��ȡ����
		 */
		public static final String GET_URL = "http://api.t.sina.com.cn/trends/statuses.xml";
		public static void readContentFromGet(){
			try{
				// ƴ��get�����URL�ִ�����Ӳ���
				String title="��ѧ";//΢������
				String redirect_url="http://open.weibo.com/apps/Ӧ��APPKEY/privilege/oauth";//Ϊʹ��OAuth2.0����д�Ļص���ַ
				String getURL = GET_URL + "?trend_name='��ѧ'";
				URL getUrl = new URL(getURL);
				// ����ƴ�յ�URL�������ӣ�URL.openConnection���������URL�����ͣ�
				// ���ز�ͬ��URLConnection����Ķ�������URL��һ��http�����ʵ�ʷ��ص���HttpURLConnection
				HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
				// �������ӣ�����ʵ����get requestҪ����һ���connection.getInputStream()�����вŻ���������������
				connection.connect();
				// ȡ������������ʹ��Reader��ȡ
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));//���ñ���,������������
				System.out.println("=============================");
				System.out.println("Contents of get request");
				System.out.println("=============================");
				String lines;
				int i=0;
				while ((lines = reader.readLine()) != null && i<=10){
					System.out.println("lines");
					//lines = new String(lines.getBytes(), "utf-8");
					i++;
					System.out.println(lines);
				}
				reader.close();
				// �Ͽ�����
				connection.disconnect();
				System.out.println("=============================");
				System.out.println("Contents of get request ends");
				System.out.println("=============================");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
//		public static void getDatasFromGet(){
//			   DefaultHttpClient dhc=new DefaultHttpClient();
//			 
//			   String url = "http://192.168.1.50:1000/sql_query?=<book>bookname</book>";
//			   url=URLEncoder.encode(url, "utf-8");
//			   HttpGet get=new HttpGet(url);
//			 
//			   CloseableHttpResponse res = dhc.execute(get);
//			 
//			   String content=EntityUtils.toString(res.getEntity());
//			 
//			   System.out.println(content);
//		}
		
}
