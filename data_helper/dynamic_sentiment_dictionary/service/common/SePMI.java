package service.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.CommonDAO;
import dao.MicroBlogDAO;
import dao.SentiWordsDAO;

import entity.DecimalCalculate;

/**
 * 
 * @author YHL
 * ��Ӧ�����µ�SE-PMI����
 *
 */
public class SePMI {
	String[] classes = {"angry","contempt","sad","joyful"};
	SentiWordsDAO sentiDao  =new SentiWordsDAO();
	MicroBlogDAO microDao  =new MicroBlogDAO();
	static double MIN_PMI = 2.0;//pmi��ֵ
	static double THE_MINUS =0.01;//��������ֵ
	static int N;//������΢����
	static int MIN_JOINCOUNT=2;//��ͬ΢�������ޣ����ݹ�ģ����
	static double MIN_PERCENT = 0.3;//joimCount��cwCount��swCount�е�ռ��
	Map<String,String> allTWords = new HashMap<String,String>();//���Ѳ���ĺ�ѡ�ʼ���ü��ϣ���ʽΪ��name,class��
	ArrayList<Set<String>> synlist=null; //ͬ��ʼ�
	ArrayList<ArrayList<String>> microlist =null;//΢���ʼ�
	/**
	 * ����ӿڣ�����ĳһ��΢������������pmiֵ������������
	 * params  words ����΢���Ĵ�����
	 * return  ������
	 */
	public String getSentiClass(String word){
		if(allTWords.get(word)!=null){//�Ѿ�SE-PMI����������ˣ���ֱ�ӻ�ȡ
			String returnClass = allTWords.get(word).toString().trim();
			return returnClass;
		}else if(word.length()<2){
			return "neutral";
		}else{
			Map<String,Double> map = new HashMap<String, Double>();//��Ŵ�����������pmiֵ
			Map<String,Map<String,Integer>> countmap = new HashMap<String, Map<String,Integer>>();
			Set<String> words = new HashSet<String>();
			words.add(word);
			
			for (int i = 0; i < classes.length; i++) {
				//1.�����ݿ��л�ȡ���������Ļ�׼��д���
				Map<String,Object> params = new HashMap<String,Object>();
				params.put("isBase", 1);
				Set<String> list =sentiDao.getSentiWordsOfClass(classes[i],params);
				//��ȡͬ��ʣ������������׼�ʸ��Գ��ֵ�΢�������������ߵ�pmiֵ
				//System.out.println("============"+classes[i]+"============");
				Map<String,Integer> cmap =this.getWordsCounts(findSynWords(words),findSynWords(list));
				double pmi = this.calPMI(cmap);
				map.put(classes[i],pmi);
				countmap.put(classes[i], cmap);
			}
			
			//����minus
			double sminus =map.get("joyful")-(map.get("angry")+map.get("contempt")+map.get("sad"));
			if(sminus>THE_MINUS || sminus<(-THE_MINUS)){//��������ֵ��Χ�����������ж�
				String classTy="angry";
				double maxpmi = 0.0;
				if(sminus>THE_MINUS){//Ϊ��
					maxpmi = map.get("joyful");
					if(map.get("joyful")>MIN_PMI){
						classTy="joyful";
					}else{
						classTy="neutral";
					}
				}else{//Ϊ��
					classTy="sad";
					maxpmi = map.get("sad");
					if(map.get("contempt")>maxpmi){
						classTy="contempt";
						maxpmi = map.get("contempt");
					}
					if(map.get("angry")>maxpmi){
						classTy="angry";
						maxpmi = map.get("angry");
					}
				}
				if(maxpmi<=MIN_PMI){//��ֵ��û��������ֵ���ж�Ϊneutral
					classTy="neutral";
				}else{
					//�ж�joinCount�Ƿ��ڷ�Χ��
					//��ֵ��joincount����Ϊ2���ϣ�joinCount��cwCount��swCount�е�ռ�ȱ������0.2
					Map<String,Integer> cmap =countmap.get(classTy);//��ȡ��ѡ���뵱ǰ���ĸ���countֵ
					int temp_joinCount = cmap.get("joinCount");//��ͬ���ֵ�Ƶ��
					int temp_cwCount = cmap.get("cwCount");//��ѡ�ʳ��ֵ�Ƶ��
					int temp_swCount = cmap.get("swCount");//��дʳ��ֵ�Ƶ��
					double jc_cw = DecimalCalculate.div(temp_joinCount, temp_cwCount, 3);
					double jc_sw = DecimalCalculate.div(temp_joinCount, temp_swCount, 3);
					if(temp_joinCount<MIN_JOINCOUNT || jc_cw <=MIN_PERCENT || jc_sw<=MIN_PERCENT){
						classTy="neutral";
					}else{
						System.out.println(word+" : "+classTy+" : "+ maxpmi);
						System.out.println("count: cw"+temp_cwCount+" sw "+temp_swCount+" jw "+temp_joinCount);
						System.out.println("j/cw: "+jc_cw);
						System.out.println("j/sw: "+jc_sw);
					}
				}
				allTWords.put(word, classTy);
				return classTy;
			}else{
				allTWords.put(word, "neutral");
				return "neutral";
			}
		}
	}
	/**
	 * �ڲ����ã����㵥��������ĳһ���������PMI��ֵ
	 * params  cwords ������飬swords Ŀ����д���
	 * return  ���������ֵ�΢��������д�����ֵ�΢���������ǹ�ͬ���ֵ�΢����
	 */
	public double calPMI(Map<String,Integer> map){
		double pmi=0.0000;
		pmi=DecimalCalculate.div(N*map.get("joinCount")+1, map.get("cwCount")*map.get("swCount")+1, 5);
		pmi = DecimalCalculate.div(Math.log(pmi),Math.log(2),5);
		return pmi;
	}
	/**
	 * �ڲ����ã�����ĳһ���Ｏ��ͬ���
	 * params  words
	 * return  list ����ͬ��ʴ���+words�ļ���
	 * 
	 */
	public Set<String> findSynWords(Set<String> words){
//		System.out.println("findSynWords=======start");
		
		Set<String> totalSet = new HashSet<String>();//���ͬ���
		//��ȡ����ͬ��ʴ���
		if(synlist==null || synlist.size()==0){
			synlist = this.tosynArray();
		}
		Iterator sit = synlist.iterator();	
		while(sit.hasNext()){
			Set<String> sset = (Set<String>) sit.next();
			Iterator wit = words.iterator();
			while(wit.hasNext()){
				String cword =wit.next().toString().trim();
				Iterator ip = sset.iterator();
				while(ip.hasNext()){
					String sword = ip.next().toString().trim();
					if(sword.equals(cword) || sword.indexOf(cword)!=-1){
						totalSet.addAll(sset);
						break;
					}
				}
			}	
			
		}
		totalSet.addAll(words);
//		System.out.println("syn:"+totalSet.size());
		Iterator tei = totalSet.iterator();
		ArrayList<String> removeset = new ArrayList<String>();
		while(tei.hasNext()){//ȥ�������ַ�
			String ne= tei.next().toString().trim();
			if(ne.length()<2){
				removeset.add(ne);
			}
		}
		for (int i = 0; i < removeset.size(); i++) {
			if(totalSet.contains(removeset.get(i))){
				totalSet.remove(removeset.get(i));
			}
		}
//		System.out.println("syn: "+totalSet.toString());
//		System.out.println("findSynWords=======end");
		return totalSet;
	}
	/**
	 * �ڲ����ã����ı��ж���ͬ����֣���תΪArrayList��ʽ
	 * return ArrayList��ʽ��ͬ��ʴ��飬��ÿ����¼Ϊһ��ͬ��ʴ���
	 * 
	 */
	public ArrayList<Set<String>> tosynArray(){
		ReadFiles reFile = new ReadFiles();
		Set synonymsSet=reFile.getSynonymsWords();//���ı��ж���ͬ�����
		Iterator sit = synonymsSet.iterator();
		ArrayList<Set<String>> tlist = new ArrayList<Set<String>>();
		while(sit.hasNext()){
			String s = (String) sit.next();
			String[] ss = s.split(" ");
			if(ss[0].endsWith("=")){//��=��β��Ϊͬ��ʣ���#��β��Ϊͬ��ʣ���@��β��Ϊ������ͬ��ʺ�ͬ��ʵĴ���
				Set<String> set = new HashSet<String>();
				for (int i = 1; i < ss.length; i++) {
					set.add(ss[i]);
				}
				tlist.add(set);
			}
			
		}
		return tlist;
	}
	/**
	 * �ڲ����ã�ͳ�ƴ���ʺ�Ŀ����п������Գ��ֵ�Ƶ���Լ���ͬ���ֵ�Ƶ��
	 * params  cwords ������飬swords Ŀ����д���(���Ѿ������ͬ�������)
	 * return  ���������ֵ�΢��������д�����ֵ�΢���������ǹ�ͬ���ֵ�΢����
	 */
	public Map<String,Integer>  getWordsCounts(Set<String> cwords,Set<String> swords){
//		System.out.println("getWordsCounts============start");
//		System.out.println("cwords:"+cwords.size());
//		System.out.println("swords:"+swords.size());
		Map<String,Integer> map = new HashMap<String,Integer>();
		int cwCount = 0;//��������ʵ�΢����
		int swCount = 0;//������дʵ�΢����
		int joinCount = 0;//��ͬ���ֵ�΢����
		//1.�����ݿ��л�ȡ�Ѿ��ִʵ�΢��
		if(microlist==null || microlist.size()==0)
			microlist = this.microtoArray();
		//2.����ÿ��΢��,�������ʺ���дʳ��ֵĴ���
		for (int i = 0; i < microlist.size(); i++) {
			Iterator cit = cwords.iterator();
			Iterator sit = swords.iterator();
			boolean flag = false;
			while(cit.hasNext()){
				boolean exists=false;
				String s = cit.next().toString().trim();
				ArrayList<String> list = microlist.get(i);
				for (int j = 0; j <list.size(); j++) {
					if(s.equals(list.get(j).trim()) || list.get(j).trim().indexOf(s)!=-1){
						cwCount++;
						flag = true;
						exists = true;
						break;
					}
				}
				if(exists){
					break;
				}
			}
			while(sit.hasNext()){
				boolean exists2=false;
				String s2 = sit.next().toString().trim();
				ArrayList<String> list = microlist.get(i);
				for (int j = 0; j <list.size(); j++) {
					if(s2.equals(list.get(j).trim())|| list.get(j).trim().indexOf(s2)!=-1){
						swCount++;
						if(flag){
//							System.out.println(list.toString());
							joinCount++;
						}
						exists2 = true;
						break;
					}
				}
				if(exists2){
					break;
				}
			}
					
		}
		map.put("cwCount",cwCount);
		map.put("swCount",swCount);
		map.put("joinCount",joinCount);
//		System.out.println("cwCount"+cwCount);
//		System.out.println("swCount"+swCount);
//		System.out.println("joinCount"+joinCount);
		return map;
	}
	/**
	 * �ڲ����ã������ݿ��ж���΢���ʼ�����תΪArrayList��ʽ,��getWordsCounts����
	 * return ArrayList��ʽ��΢��������ÿ����¼Ϊһ���ִʺ��΢��
	 * 
	 */
	public ArrayList<ArrayList<String>> microtoArray(){
		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		//�����ݿ��ж���΢���ʼ�
		Map<Integer,String> omicrowords = microDao.getAllMicroWords();
		N= omicrowords.size();//��ֵ΢�����ݼ���ģ
		Collection<String> values=omicrowords.values();
		Iterator it = values.iterator();
		while(it.hasNext()){
			ArrayList<String> list = new ArrayList<String>();
			String s = (String)it.next();
			String[] ss = s.split(" ");
			for (int j = 1; j < ss.length; j++) {
				list.add(ss[j]);
			}
			results.add(list);
		}
		return results;
	}
	
	
}
