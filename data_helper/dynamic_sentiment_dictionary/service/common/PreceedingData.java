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
 *从微博API获取数据，暂时没用
 */
public class PreceedingData {
		MicroBlogDAO microDao  =new MicroBlogDAO();
		ICTCLASAnalyzer analyzer = new ICTCLASAnalyzer();
		Set<String> stopWordSet = new HashSet<String>(); 
		/**
		 * 对外接口，从微博API获取数据，并分词，分词后将数据存入数据库
		 */
		public void preMicroDatas(){
			MicroBlogDAO microdao = new MicroBlogDAO();
			//获取停用词表，将停用词存入全局set：stopWordSet中
			ReadFiles reFile = new ReadFiles();
			stopWordSet=reFile.getStopWords();
			Map<Integer,String> list =microdao.getAllMicroText();
			Set<Integer> keys = list.keySet();
			Iterator it = keys.iterator();
			while(it.hasNext()){
				int id = (Integer) it.next();
				String text = list.get(id);
				//2.分词处理
				String stext = analyzer.ICTCLAS_ParaProcess(text);
				String[] sarr = stext.split(" ");
				
				//3.去除停用词
				StringBuilder words= new StringBuilder();
				boolean isEmotion = false;
				int count=0;//用于去除「和」引用的部分
				for (int i = 0; i < sarr.length; i++) {
					Iterator<String> sit = stopWordSet.iterator();
					boolean f = false;
					while(sit.hasNext()){
						String s = sit.next().toString();
						if(s.equals(sarr[i]) || sarr[i].indexOf(s)!=-1){
							f=true;break;
						}
					}//判断是否为表情符号 ,由于分词会将表情符号中的[]分开，故在此需将其重新拼接
					if(sarr[i].equals("[") || sarr[i].startsWith("[")){
						isEmotion=true;
					}
					if(sarr[i].equals("]") || sarr[i].endsWith("]")){
						isEmotion=false;
					}
					if(sarr[i].equals("「 ") || sarr[i].startsWith("「 ")){
						count=1;
					}
					if(!f && count==0){
						if(isEmotion){
							words.append(sarr[i]);
						}else{
							words.append(sarr[i]+" ");
						}
						
					}
					if(sarr[i].equals("」") || sarr[i].endsWith("」")){
						count=0;
					}
					
				}
				//3.存入数据库
				microDao.updateWordsOfMicro(id, words.toString());
			}
			
		}
		/*
		 * 内部调用，从新浪微博API获取数据
		 */
		public static final String GET_URL = "http://api.t.sina.com.cn/trends/statuses.xml";
		public static void readContentFromGet(){
			try{
				// 拼凑get请求的URL字串，添加参数
				String title="科学";//微博主题
				String redirect_url="http://open.weibo.com/apps/应用APPKEY/privilege/oauth";//为使用OAuth2.0，填写的回调地址
				String getURL = GET_URL + "?trend_name='科学'";
				URL getUrl = new URL(getURL);
				// 根据拼凑的URL，打开连接，URL.openConnection函数会根据URL的类型，
				// 返回不同的URLConnection子类的对象，这里URL是一个http，因此实际返回的是HttpURLConnection
				HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
				// 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到服务器
				connection.connect();
				// 取得输入流，并使用Reader读取
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));//设置编码,否则中文乱码
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
				// 断开连接
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
