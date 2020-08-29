package service.naivebayes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import service.dicbasedmethod.ODicBasedMethod;

import dao.MicroBlogDAO;
import entity.DecimalCalculate;
/**
 * 
 * @author GGG
 * 传统的朴素贝叶斯方法
 *
 */

public class ONaiveBayes {
	String classes[] = {"angry","contempt","sad","joyful"};
	Map<String, Map<String,Integer>>  wordCounts = new HashMap<String, Map<String,Integer>>();//用于保存每个类别中各个词汇出现的次数，第一个String为类别，第二个String为词语，Integer为出现的次数
	Map<String,Double> pOfClass = new HashMap<String,Double>();//用于存放各个类别的先验概率
	MicroBlogDAO microDao =new MicroBlogDAO();
	
	/**
	 *对外接口， 判定微博情绪类别
	 * @param Map<Integer,String> 待测微博键值对，键为微博在数据库中的id，值为分词序列，对应数据库中的words字段
	 * 
	 * 	 
	 * */
	public void predictClass(Map<Integer,String> micros){
		//新的方法：1.标注训练集
		this.setSTypeOfTrainSet();
		//2.计算先验概率
		this.updateWordCounts();
		//3.对每条微博进行情感判别
		Set<Integer> keys = micros.keySet();
		Iterator<Integer> it = keys.iterator();
		//新的方法：4.定时更新先验概率,每标注1/5更新一次
		int size = micros.size()/5;
		int count =0; 
		while(it.hasNext()){
			int key = it.next();
			String[] arr = micros.get(key).toString().split(" ");//获取当前待测的微博分词序列
			String mclass = this.predictClassOfMicro(arr);//获取当前微博的情绪类别
			//System.out.println("===========" + mclass);
			//旧的方法：标记为训练集，并更新数据库
			//microDao.updateClassesOfMicro(key,mclass);
			//新的方法：更新数据库,标记为训练集
			microDao.updateStatusOfMicro(key,mclass);
			//新的方法：定时更新先验概率,每标注1/5更新一次
			count++;
			if(count%size==0){
				this.updateWordCounts();
			}
		}
		
	}
	/**
	 * 内部调用，新的贝叶斯方法新增的步骤，采用基于词典的方法标注训练集，新的
	 * 
	 */
	public void setSTypeOfTrainSet(){
		//采用基于SE-PMI的情感分类方法来标注，并加入表情符号和双重否定
		ODicBasedMethod onbayes = new ODicBasedMethod();
		//1.获取所有训练集
		Map<Integer,String> trainSet =microDao.getAllTrainText();
		//对每条微博进行情感判别,并存入数据库
		onbayes.predictClass(trainSet);
		//onbayes.predictTrainClass(trainSet);//测试代码修改2
		
	}
	/**
	 *内部调用， 在训练数据的基础上预测测试元组的类别
	 * @param testT 测试元组，是一个词序列，这里一条微博分词后变成一组词序列，通过该方法确定一条微博所属类别
	 * @return 测试元组的类别
	 */
	public String predictClassOfMicro(String[] testT) {
		double maxP = 0.00;//存放testT所属类别的最大概率
		int maxPIndex = -1;//存放testT所属最大概率类别的索引号，即指向最大概率的类别
		for (int i = 0; i < classes.length; i++) {
			String c = classes[i].toString(); 
//			System.out.println(c+"----------------");
			double pOfC = pOfClass.get(c);//获取训练集中类别c的占比
			for (int j = 0; j < testT.length; j++) {
				if(testT[j].length()<2){//单个字符不作判断
					continue;
				}
				double pv = this.npOfV(testT[j], c);//计算testT.get(j)表示的词属于类别C的概率
				if(pv!=0)
					pOfC = DecimalCalculate.mul(pOfC, pv);//将当前测试集testT属于类别c的概率累乘到pofC，计算testT属于类别C的概率
			}
			if(pOfC > maxP){
				maxP = pOfC;
				maxPIndex = i;
			}
		}
		return classes[maxPIndex].toString();//返回所属类别
	}
	/**
	 * 内部调用，计算词value属于某一类别的概率
	 * @param value 待测词
	 * @param c 情绪类别
	 * @return 概率
	 */
	private double npOfV(String value, String c) {
		double p = 0.00;
		int count = 0;
		int tCount = 0;//c包含的所有词汇出现的次数之和
		HashMap<String,Integer> map = (HashMap<String, Integer>) wordCounts.get(c);
		//获取类别c包含的所有词汇出现的次数之和
		Set<String> keys = map.keySet();
		Iterator<String> mit = keys.iterator();
		while(mit.hasNext()){
			String stemp =mit.next().toString().trim();
			int temp = map.get(stemp);
//			if(temp>1){
				tCount += temp;
//			}
		}
		//获取词汇value在类别c中出现的次数
		if(map.get(value.trim())!=null ){
			count = map.get(value);	
		}
		p = DecimalCalculate.div(count+1, tCount+1, 3);
		return p;
	}
	
	/**
	 * 内部调用，更新各个类别的先验概率以及各类别下每个词语出现的短文数，传统的方法仅在开始计算一次
	 * 新的贝叶斯则每隔一段时间更新一次
	 */
	public void updateWordCounts(){
		int trainCounts = 0;//训练集总大小
		for (int i = 0; i < classes.length; i++) {			
			ArrayList<String> results = new ArrayList<String>();
			//1.从数据库中获取该类别下的训练集数据
			results = microDao.getTrainTextsOfMicro(classes[i].trim());
			
			trainCounts+=results.size();
			pOfClass.put(classes[i].trim(),(double)results.size());
			
			Map<String,Integer> map=new HashMap<String, Integer>();
			for (int j = 0; j < results.size(); j++) {
				if(results.get(j).trim().length()<2){
					continue;
				}
				String[] s=results.get(j).split(" ");
				for (int k = 0; k < s.length; k++) {
//					if(s[k].length()<2){//单个字符不作判断
//						continue;
//					}
					//累计各个词汇的个数
					if(s[k]!=null && !"".equals(s[k].trim()) && map.containsKey(s[k].trim())){
						int count = map.get(s[k].trim())+1;
						map.put(s[k].trim(),count);
					}else{
						map.put(s[k].trim(),1);
					}	
				}
			}
			wordCounts.put(classes[i].trim(), map);
		}//end for
		//计算各个类别的先验概率，并更新至全局变量pOfClass中
		for (int i = 0; i < classes.length; i++) {
			double temp= pOfClass.get(classes[i].trim());
			pOfClass.put(classes[i].trim(),DecimalCalculate.div(temp, trainCounts, 3));//保留小数点后3位
//			System.out.println(classes[i]+" : "+pOfClass.get(classes[i]));
		}
		
		
	}
	
}
