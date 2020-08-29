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
 * 对应第三章的SE-PMI方法
 *
 */
public class SePMI {
	String[] classes = {"angry","contempt","sad","joyful"};
	SentiWordsDAO sentiDao  =new SentiWordsDAO();
	MicroBlogDAO microDao  =new MicroBlogDAO();
	static double MIN_PMI = 2.0;//pmi阈值
	static double THE_MINUS =0.01;//正负差阈值
	static int N;//上下文微博数
	static int MIN_JOINCOUNT=2;//共同微博数下限，根据规模来定
	static double MIN_PERCENT = 0.3;//joimCount在cwCount和swCount中的占比
	Map<String,String> allTWords = new HashMap<String,String>();//将已测过的候选词加入该集合，格式为《name,class》
	ArrayList<Set<String>> synlist=null; //同义词集
	ArrayList<ArrayList<String>> microlist =null;//微博词集
	/**
	 * 对外接口，计算某一条微博与各情感类别的pmi值，返回情感类别
	 * params  words 待测微博的次序列
	 * return  情感类别
	 */
	public String getSentiClass(String word){
		if(allTWords.get(word)!=null){//已经SE-PMI测过情感类别了，可直接获取
			String returnClass = allTWords.get(word).toString().trim();
			return returnClass;
		}else if(word.length()<2){
			return "neutral";
		}else{
			Map<String,Double> map = new HashMap<String, Double>();//存放待测词与各类别的pmi值
			Map<String,Map<String,Integer>> countmap = new HashMap<String, Map<String,Integer>>();
			Set<String> words = new HashSet<String>();
			words.add(word);
			
			for (int i = 0; i < classes.length; i++) {
				//1.从数据库中获取该情绪类别的基准情感词组
				Map<String,Object> params = new HashMap<String,Object>();
				params.put("isBase", 1);
				Set<String> list =sentiDao.getSentiWordsOfClass(classes[i],params);
				//获取同义词，计算待测词与基准词各自出现的微博数，计算两者的pmi值
				//System.out.println("============"+classes[i]+"============");
				Map<String,Integer> cmap =this.getWordsCounts(findSynWords(words),findSynWords(list));
				double pmi = this.calPMI(cmap);
				map.put(classes[i],pmi);
				countmap.put(classes[i], cmap);
			}
			
			//计算minus
			double sminus =map.get("joyful")-(map.get("angry")+map.get("contempt")+map.get("sad"));
			if(sminus>THE_MINUS || sminus<(-THE_MINUS)){//正负差阈值范围外则进行情感判断
				String classTy="angry";
				double maxpmi = 0.0;
				if(sminus>THE_MINUS){//为正
					maxpmi = map.get("joyful");
					if(map.get("joyful")>MIN_PMI){
						classTy="joyful";
					}else{
						classTy="neutral";
					}
				}else{//为负
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
				if(maxpmi<=MIN_PMI){//阈值，没超过该阈值则判定为neutral
					classTy="neutral";
				}else{
					//判断joinCount是否在范围内
					//阈值：joincount必须为2以上，joinCount在cwCount和swCount中的占比必须大于0.2
					Map<String,Integer> cmap =countmap.get(classTy);//获取候选词与当前类别的各项count值
					int temp_joinCount = cmap.get("joinCount");//共同出现的频率
					int temp_cwCount = cmap.get("cwCount");//候选词出现的频率
					int temp_swCount = cmap.get("swCount");//情感词出现的频率
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
	 * 内部调用，计算单个词语与某一个情感类别的PMI的值
	 * params  cwords 待测词组，swords 目标情感词组
	 * return  待测词组出现的微博数、情感词组出现的微博数、他们共同出现的微博数
	 */
	public double calPMI(Map<String,Integer> map){
		double pmi=0.0000;
		pmi=DecimalCalculate.div(N*map.get("joinCount")+1, map.get("cwCount")*map.get("swCount")+1, 5);
		pmi = DecimalCalculate.div(Math.log(pmi),Math.log(2),5);
		return pmi;
	}
	/**
	 * 内部调用，查找某一词语集的同义词
	 * params  words
	 * return  list 返回同义词词组+words的集合
	 * 
	 */
	public Set<String> findSynWords(Set<String> words){
//		System.out.println("findSynWords=======start");
		
		Set<String> totalSet = new HashSet<String>();//存放同义词
		//获取所有同义词词组
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
		while(tei.hasNext()){//去掉单个字符
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
	 * 内部调用，从文本中读入同义词林，并转为ArrayList格式
	 * return ArrayList格式的同义词词组，其每条记录为一个同义词词组
	 * 
	 */
	public ArrayList<Set<String>> tosynArray(){
		ReadFiles reFile = new ReadFiles();
		Set synonymsSet=reFile.getSynonymsWords();//从文本中读入同义词林
		Iterator sit = synonymsSet.iterator();
		ArrayList<Set<String>> tlist = new ArrayList<Set<String>>();
		while(sit.hasNext()){
			String s = (String) sit.next();
			String[] ss = s.split(" ");
			if(ss[0].endsWith("=")){//以=结尾的为同义词，以#结尾的为同类词，以@结尾的为不存在同义词和同类词的词语
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
	 * 内部调用，统计待测词和目标情感库词语各自出现的频率以及共同出现的频率
	 * params  cwords 待测词组，swords 目标情感词组(均已经添加了同义词组了)
	 * return  待测词组出现的微博数、情感词组出现的微博数、他们共同出现的微博数
	 */
	public Map<String,Integer>  getWordsCounts(Set<String> cwords,Set<String> swords){
//		System.out.println("getWordsCounts============start");
//		System.out.println("cwords:"+cwords.size());
//		System.out.println("swords:"+swords.size());
		Map<String,Integer> map = new HashMap<String,Integer>();
		int cwCount = 0;//包含待测词的微博数
		int swCount = 0;//包含情感词的微博数
		int joinCount = 0;//共同出现的微博数
		//1.从数据库中获取已经分词的微博
		if(microlist==null || microlist.size()==0)
			microlist = this.microtoArray();
		//2.遍历每条微博,计算待测词和情感词出现的次数
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
	 * 内部调用，从数据库中读入微博词集，并转为ArrayList格式,供getWordsCounts调用
	 * return ArrayList格式的微博集，其每条记录为一条分词后的微博
	 * 
	 */
	public ArrayList<ArrayList<String>> microtoArray(){
		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		//从数据库中读入微博词集
		Map<Integer,String> omicrowords = microDao.getAllMicroWords();
		N= omicrowords.size();//赋值微博数据集规模
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
