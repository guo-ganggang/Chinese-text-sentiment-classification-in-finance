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
 *��ȡ΢�����ݣ���΢�����зִʣ������ø��������Ľӿڶ��������з��࣬������calculate���еĽӿڷֱ����׼ȷ�ʺ��ٻ���
 *
 *
 */
public class MainClass {
	static String [] classes = {"angry","contempt","sad","joyful"};//������������˺����
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	// /*	
		//1.��ȡ΢������
		//new ReadMicroDatas().readMicrosFromMySql();
		//2.Ԥ�����ִʡ�ȥ��ͣ�ô�
		//PreceedingData pre = new PreceedingData();
		//pre.preMicroDatas();
		//3.���ļ��ж�ȡ��дʲ��������ݿ���
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
		
//		//4.ʵ�����SE-PMI����
//		SePMI sepmi = new SePMI();
//		TestWordsDAO testDAO = new TestWordsDAO();
//		Map<Integer,String> results = testDAO.getAllTestWords();
//		Set keys = results.keySet();
//		Iterator it = keys.iterator();
//		System.out.println("SE-PMI���Խ�����......"+results.size());
//		while(it.hasNext()){
//			int id = (Integer)it.next();//����ʵ�id
//			String wor =results.get(id).toString().trim();
//			String newSType = sepmi.getSentiClass(wor);
//			testDAO.updateClassesOfWord(id,newSType);
//		}
//		System.out.println("SE-PMI������ϣ���ʼ����׼ȷ�ʺ��ٻ��ʣ�");
//		//����׼ȷ�ʺ��ٻ���
//		Map<String,Double> precisions=testDAO.calPrecision();
//		Map<String,Double> recalls=testDAO.calRecall();
//		for (int i = 0; i < classes.length; i++) {
//			//double f=DecimalCalculate.div(2*precisions.get(classes[i])*recalls.get(classes[i]), (precisions.get(classes[i])+recalls.get(classes[i])), 3);
//		System.out.println(classes[i]+" : ׼ȷ��  "+precisions.get(classes[i])+"%  �ٻ���  "+recalls.get(classes[i]));
//		}
		
		MicroBlogDAO microdao  = new MicroBlogDAO();
		Map<Integer,String> micros = microdao.getAllMicroWords();
		System.out.println("DicBasedMethod����---------------��ʼ");
		//5.���ڴʵ�ķ���
//		System.out.println("���ڴʵ�ķ�����");
//		ODicBasedMethod obasedme = new ODicBasedMethod();
//		obasedme.predictClass(micros);
		//6.���ر�Ҷ˹
		System.out.println("���ر�Ҷ˹������");
		ONaiveBayes onaiveBayes = new ONaiveBayes();
		onaiveBayes.predictClass(micros); 
		
		//7.����׼ȷ�ʺ��ٻ���
		System.out.println("DicBasedMethod����---------------���");
		//����׼ȷ�ʺ��ٻ���
		System.out.println("��ʼ����׼ȷ�ʺ��ٻ���---------------");
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
			System.out.println(classes[i]+" : ׼ȷ��  "+precisions.get(classes[i])+"%  �ٻ���  "+recalls.get(classes[i])+"% Fֵ  " +f);
		}
	}
	
}
