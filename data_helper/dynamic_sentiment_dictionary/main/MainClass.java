package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dao.MicroBlogDAO;
import dao.TestWordsDAO;
import entity.DecimalCalculate;
import service.common.PreceedingData;
import service.common.ReadFiles;
import service.common.ReadMicroDatas;
import service.common.SePMI;
import service.dicbasedmethod.ODicBasedMethod;
import service.naivebayes.ONaiveBayes;

/**
 * 
 * @author GGG
 *获取微博数据，对微博进行分词，并调用各个方法的接口对其进行情感分类，最后调用calculate类中的接口分别计算准确率和召回率
 *
 *
 */
public class MainClass {
	static String [] classes = {"angry","contempt","sad","joyful"};//生气、轻蔑、悲伤和愉快
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	// /*	
		//1.获取微博数据
		//new ReadMicroDatas().readMicrosFromMySql();
		//2.预处理：分词、去除停用词
		//PreceedingData pre = new PreceedingData();
		//pre.preMicroDatas();
		//3.从文件中读取情感词并存入数据库中
//		ReadFiles readf = new ReadFiles();
//		String  sentiWordsFile = "ExternalFiles/joyful.txt";//stopWordFile
//		String stype = "joyful";
//		readf.readSentiWords(sentiWordsFile,stype,0,0,0,0);
//		sentiWordsFile = "ExternalFiles/sad.txt";
//		stype = "sad";
//		readf.readSentiWords(sentiWordsFile,stype,0,0,0,0);
//		sentiWordsFile = "ExternalFiles/contempt.txt";
//		stype = "contempt";
//		readf.readSentiWords(sentiWordsFile,stype,0,0,0,0);
//		sentiWordsFile = "ExternalFiles/angry.txt";
//		stype = "angry";
//		readf.readSentiWords(sentiWordsFile,stype,0,0,0,0);	
	// */
		
//		//4.实验测试SE-PMI方法
//		SePMI sepmi = new SePMI();
//		TestWordsDAO testDAO = new TestWordsDAO();
//		Map<Integer,String> results = testDAO.getAllTestWords();
//		Set keys = results.keySet();
//		Iterator it = keys.iterator();
//		System.out.println("SE-PMI测试进行中......"+results.size());
//		while(it.hasNext()){
//			int id = (Integer)it.next();//待测词的id
//			String wor =results.get(id).toString().trim();
//			String newSType = sepmi.getSentiClass(wor);
//			testDAO.updateClassesOfWord(id,newSType);
//		}
//		System.out.println("SE-PMI测试完毕，开始计算准确率和召回率：");
//		//计算准确率和召回率
//		Map<String,Double> precisions=testDAO.calPrecision();
//		Map<String,Double> recalls=testDAO.calRecall();
//		for (int i = 0; i < classes.length; i++) {
//			//double f=DecimalCalculate.div(2*precisions.get(classes[i])*recalls.get(classes[i]), (precisions.get(classes[i])+recalls.get(classes[i])), 3);
//		System.out.println(classes[i]+" : 准确率  "+precisions.get(classes[i])+"%  召回率  "+recalls.get(classes[i]));
//		}
		
		MicroBlogDAO microdao  = new MicroBlogDAO();
		Map<Integer,String> micros = microdao.getAllMicroWords();
		System.out.println("DicBasedMethod测试---------------开始");
		//5.基于词典的方法
//		System.out.println("基于词典的方法：");
//		ODicBasedMethod obasedme = new ODicBasedMethod();
//		obasedme.predictClass(micros);
		//6.朴素贝叶斯
		System.out.println("朴素贝叶斯方法：");
		ONaiveBayes onaiveBayes = new ONaiveBayes();
		onaiveBayes.predictClass(micros); 
		
		//7.计算准确率和召回率
		System.out.println("DicBasedMethod测试---------------完毕");
		//计算准确率和召回率
		System.out.println("开始计算准确率和召回率---------------");
		Map<String,Double> precisions=microdao.calPrecision();
		Map<String,Double> recalls=microdao.calRecall();
		double f=0.0;
		for (int i = 0; i < classes.length; i++) {
			double mul = precisions.get(classes[i])+recalls.get(classes[i]);
			if(mul==0){
				f=0;
			}else{
				f=DecimalCalculate.div(2*precisions.get(classes[i])*recalls.get(classes[i]), mul, 3);
			}
			System.out.println(classes[i]+" : 准确率  "+precisions.get(classes[i])+"%  召回率  "+recalls.get(classes[i])+"% F值  " +f);
		}
	}
	
}
