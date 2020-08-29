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
 * ��ͳ�����ر�Ҷ˹����
 *
 */

public class ONaiveBayes {
	String classes[] = {"angry","contempt","sad","joyful"};
	Map<String, Map<String,Integer>>  wordCounts = new HashMap<String, Map<String,Integer>>();//���ڱ���ÿ������и����ʻ���ֵĴ�������һ��StringΪ��𣬵ڶ���StringΪ���IntegerΪ���ֵĴ���
	Map<String,Double> pOfClass = new HashMap<String,Double>();//���ڴ�Ÿ��������������
	MicroBlogDAO microDao =new MicroBlogDAO();
	
	/**
	 *����ӿڣ� �ж�΢���������
	 * @param Map<Integer,String> ����΢����ֵ�ԣ���Ϊ΢�������ݿ��е�id��ֵΪ�ִ����У���Ӧ���ݿ��е�words�ֶ�
	 * 
	 * 	 
	 * */
	public void predictClass(Map<Integer,String> micros){
		//�µķ�����1.��עѵ����
		this.setSTypeOfTrainSet();
		//2.�����������
		this.updateWordCounts();
		//3.��ÿ��΢����������б�
		Set<Integer> keys = micros.keySet();
		Iterator<Integer> it = keys.iterator();
		//�µķ�����4.��ʱ�����������,ÿ��ע1/5����һ��
		int size = micros.size()/5;
		int count =0; 
		while(it.hasNext()){
			int key = it.next();
			String[] arr = micros.get(key).toString().split(" ");//��ȡ��ǰ�����΢���ִ�����
			String mclass = this.predictClassOfMicro(arr);//��ȡ��ǰ΢�����������
			//System.out.println("===========" + mclass);
			//�ɵķ��������Ϊѵ���������������ݿ�
			//microDao.updateClassesOfMicro(key,mclass);
			//�µķ������������ݿ�,���Ϊѵ����
			microDao.updateStatusOfMicro(key,mclass);
			//�µķ�������ʱ�����������,ÿ��ע1/5����һ��
			count++;
			if(count%size==0){
				this.updateWordCounts();
			}
		}
		
	}
	/**
	 * �ڲ����ã��µı�Ҷ˹���������Ĳ��裬���û��ڴʵ�ķ�����עѵ�������µ�
	 * 
	 */
	public void setSTypeOfTrainSet(){
		//���û���SE-PMI����з��෽������ע�������������ź�˫�ط�
		ODicBasedMethod onbayes = new ODicBasedMethod();
		//1.��ȡ����ѵ����
		Map<Integer,String> trainSet =microDao.getAllTrainText();
		//��ÿ��΢����������б�,���������ݿ�
		onbayes.predictClass(trainSet);
		//onbayes.predictTrainClass(trainSet);//���Դ����޸�2
		
	}
	/**
	 *�ڲ����ã� ��ѵ�����ݵĻ�����Ԥ�����Ԫ������
	 * @param testT ����Ԫ�飬��һ�������У�����һ��΢���ִʺ���һ������У�ͨ���÷���ȷ��һ��΢���������
	 * @return ����Ԫ������
	 */
	public String predictClassOfMicro(String[] testT) {
		double maxP = 0.00;//���testT��������������
		int maxPIndex = -1;//���testT�������������������ţ���ָ�������ʵ����
		for (int i = 0; i < classes.length; i++) {
			String c = classes[i].toString(); 
//			System.out.println(c+"----------------");
			double pOfC = pOfClass.get(c);//��ȡѵ���������c��ռ��
			for (int j = 0; j < testT.length; j++) {
				if(testT[j].length()<2){//�����ַ������ж�
					continue;
				}
				double pv = this.npOfV(testT[j], c);//����testT.get(j)��ʾ�Ĵ��������C�ĸ���
				if(pv!=0)
					pOfC = DecimalCalculate.mul(pOfC, pv);//����ǰ���Լ�testT�������c�ĸ����۳˵�pofC������testT�������C�ĸ���
			}
			if(pOfC > maxP){
				maxP = pOfC;
				maxPIndex = i;
			}
		}
		return classes[maxPIndex].toString();//�����������
	}
	/**
	 * �ڲ����ã������value����ĳһ���ĸ���
	 * @param value �����
	 * @param c �������
	 * @return ����
	 */
	private double npOfV(String value, String c) {
		double p = 0.00;
		int count = 0;
		int tCount = 0;//c���������дʻ���ֵĴ���֮��
		HashMap<String,Integer> map = (HashMap<String, Integer>) wordCounts.get(c);
		//��ȡ���c���������дʻ���ֵĴ���֮��
		Set<String> keys = map.keySet();
		Iterator<String> mit = keys.iterator();
		while(mit.hasNext()){
			String stemp =mit.next().toString().trim();
			int temp = map.get(stemp);
//			if(temp>1){
				tCount += temp;
//			}
		}
		//��ȡ�ʻ�value�����c�г��ֵĴ���
		if(map.get(value.trim())!=null ){
			count = map.get(value);	
		}
		p = DecimalCalculate.div(count+1, tCount+1, 3);
		return p;
	}
	
	/**
	 * �ڲ����ã����¸���������������Լ��������ÿ��������ֵĶ���������ͳ�ķ������ڿ�ʼ����һ��
	 * �µı�Ҷ˹��ÿ��һ��ʱ�����һ��
	 */
	public void updateWordCounts(){
		int trainCounts = 0;//ѵ�����ܴ�С
		for (int i = 0; i < classes.length; i++) {			
			ArrayList<String> results = new ArrayList<String>();
			//1.�����ݿ��л�ȡ������µ�ѵ��������
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
//					if(s[k].length()<2){//�����ַ������ж�
//						continue;
//					}
					//�ۼƸ����ʻ�ĸ���
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
		//�����������������ʣ���������ȫ�ֱ���pOfClass��
		for (int i = 0; i < classes.length; i++) {
			double temp= pOfClass.get(classes[i].trim());
			pOfClass.put(classes[i].trim(),DecimalCalculate.div(temp, trainCounts, 3));//����С�����3λ
//			System.out.println(classes[i]+" : "+pOfClass.get(classes[i]));
		}
		
		
	}
	
}
