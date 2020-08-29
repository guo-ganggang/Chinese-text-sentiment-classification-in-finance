package service.dicbasedmethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import service.common.ReadFiles;
import service.common.SePMI;

import dao.MicroBlogDAO;
import dao.SentiWordsDAO;

/**
 * 
 * @author GGG
 * 基于词典的方法:传统的以及基于SE-PMI的
 * （第3章）传统的基于词典的方法在predictClass函数中调用oPredictClassOfMicro方法来判定一条微博的情感类别
 * （第3章）基于SE-PMI的情感分类方法在predictClass函数中调用nPredictClassOfMicro方法来判定一条微博的情感类别
 * （第3章）加入了表情符号和双重否定判断的基于SE-PMI的情感分类方法在predictClass函数中调用ePredictClassOfMicro方法来判定一条微博的情感类别
 */
public class ODicBasedMethod {
	MicroBlogDAO microDao =new MicroBlogDAO();
	SentiWordsDAO sentiDao = new SentiWordsDAO();
	SePMI sePmi = new SePMI();
	String [] classes = {"angry","contempt","sad","joyful"};
	String [] negatives = {"不可以","怎么不","几乎不","从来不","从不","不用","不曾","不必","不会","不是","很少","极少","没有","难以","放下","终止","停止","放弃","反对","勿要","未曾","不","甭","勿","未","反","没","否"};
	String [] dnegatives = {"绝非不","并非不","不是不","不能不","不会不","不可不","不要不","不得不","没有不","无不","不无"};
	Map<String,Set<String>> swords = new HashMap<String, Set<String>>();//存放各个情感类别的情感词
	ArrayList<Set<String>> synlist=null; //同义词集
	/**
	 *对外接口， 判定微博情绪类别
	 * @param Map<Integer,String> 待测微博键值对，键为微博在数据库中的id，值为分词序列，对应数据库中的words字段
	 * 
	 * 	 
	 * */
	public void predictClass(Map<Integer,String> micros){
		Set<Integer> keys = micros.keySet();
		Iterator<Integer> it = keys.iterator();
		while(it.hasNext()){
			int key = it.next();
			String[] arr = micros.get(key).toString().split(" ");//获取当前待测的微博分词序列
			//String mclass = this.oPredictClassOfMicro(arr);//传统的基于词典的方法：获取当前微博的情绪类别
			//String mclass = this.nPredictClassOfMicro(arr);//基于SE-PMI的情感分类方法：获取当前微博的情绪类别
			String mclass = this.ePredictClassOfMicro(arr);//加入了表情符号和双重否定判断的基于SE-PMI的情感分类方法：获取当前微博的情绪类别
			//更新数据库
			microDao.updateClassesOfMicro(key,mclass);
		}
		
	}
	//测试代码添加3
	public void predictTrainClass(Map<Integer,String> micros){
		Set<Integer> keys = micros.keySet();
		Iterator<Integer> it = keys.iterator();
		while(it.hasNext()){
			int key = it.next();
			String[] arr = micros.get(key).toString().split(" ");//获取当前待测的微博分词序列
			//String mclass = this.oPredictClassOfMicro(arr);//传统的基于词典的方法：获取当前微博的情绪类别
			//String mclass = this.nPredictClassOfMicro(arr);//基于SE-PMI的情感分类方法：获取当前微博的情绪类别
			String mclass = this.ePredictClassOfMicro(arr);//加入了表情符号和双重否定判断的基于SE-PMI的情感分类方法：获取当前微博的情绪类别
			//标记为训练集，并更新数据库
			microDao.updateTrainClassesOfMicro(key,mclass);
		}
		
	}
	
	/**
	 *内部调用(传统的基于词典的方法调用)， 在训练数据的基础上预测测试元组的类别
	 * @param testT 待测微博，这里一条微博分词后变成一组词序列，通过该方法确定一条微博所属类别
	 * @return 待测微博的类别
	 */
	public String oPredictClassOfMicro(String[] testT) {
		//存放属于各个情感类别的微博个数
		Map<String,Integer> classCounts = new HashMap<String,Integer>();
		for (int n = 0; n < classes.length; n++) {
			classCounts.put(classes[n], 0);
			if(swords.get(classes[n].toString().trim())==null){
				//从数据库中获取该情感类别的情感词(不包含表情符号和中性)
				Map<String,Object> params = new HashMap<String,Object>();
				params.put("isEmotion", 0);
				Set<String> list =sentiDao.getSentiWordsOfClass(classes[n],null);
				swords.put(classes[n], list);
				params=null;
			}
		}
		
		for (int i = 0; i < testT.length; i++) {
			if(testT[i].trim().length()<2){
				continue;
			}
			for (int j = 0; j < classes.length; j++) {
				Set<String> list =swords.get(classes[j].toString().trim());
				//判断待测词是否在该词集中
				Iterator listit = list.iterator();
				boolean flag =false;
				while(listit.hasNext()){
					String sw = listit.next().toString().trim();
					if(sw.equals(testT[i].trim()) || testT[i].trim().indexOf(sw)!=-1){
						int count=0;
						//对情感词进行否定词的判断
						if(i>=2){
							if(this.isNegtive(testT[i-1].trim()) || this.isNegtive(testT[i-2].trim())){
								if(classes[j].trim().equals("joyful")){
									//正面情绪转为负面情绪
									count = classCounts.get("sad");
									count++;
									classCounts.put("sad", count);
								}else{
									//负面情绪转为正面情绪
									count = classCounts.get("joyful");
									count++;
									classCounts.put("joyful", count);
								}
							}else{//前面没有词语则无需再继续判断
								count = classCounts.get(classes[j]);
								count++;
								classCounts.put(classes[j], count);
							}
						}else if(i>=1){
							if(this.isNegtive(testT[i-1].trim())){
								if(classes[j].trim().equals("joyful")){
									//正面情绪转为负面情绪
									count = classCounts.get("sad");
									count++;
									classCounts.put("sad", count);
								}else{
									//负面情绪转为正面情绪
									count = classCounts.get("joyful");
									count++;
									classCounts.put("joyful", count);
								}
							}else{//前面没有词语则无需再继续判断
								count = classCounts.get(classes[j]);
								count++;
								classCounts.put(classes[j], count);
							}
						}else{//前面没有词语则无需再继续判断
							count = classCounts.get(classes[j]);
							count++;
							classCounts.put(classes[j], count);
						}
						flag=true;
						break;
					} //end if
				}//end while
				if(flag) break;
			}//end for
			
		}//end for
		//比较情感词个数，判断微博的情感类别
		int index=-1;int maxCount = 0;
		for (int k = 0; k < classes.length; k++) {
			if( classCounts.get(classes[k].trim())>maxCount){
				index=k;
				maxCount =classCounts.get(classes[k]);
			}
		}
		if(index!=-1 && maxCount!=0){
			return classes[index].toString();
		}else{
			return "neutral";
		}
		
	}//end method
	/**
	 *内部调用(基于SE-PMI的情感分类方法调用)， 在训练数据的基础上预测测试元组的类别
	 * @param testT 待测微博，这里一条微博分词后变成一组词序列，通过该方法确定一条微博所属类别
	 * @return 待测微博的类别
	 */
	public String nPredictClassOfMicro(String[] testT) {
		//存放属于各个情感类别的情感个数
		Map<String,Integer> classCounts = new HashMap<String,Integer>();
		for (int n = 0; n < classes.length; n++) {
			classCounts.put(classes[n], 0);
			if(swords.get(classes[n].toString().trim())==null){
				//从数据库中获取该情感类别的情感词(不包含表情符号和中性)
				Map<String,Object> params = new HashMap<String,Object>();
				params.put("isEmotion", 0);
				Set<String> list =sentiDao.getSentiWordsOfClass(classes[n],null);
				params=null;
				swords.put(classes[n], list);
			}
		}
		for (int i = 0; i < testT.length; i++) {
			if(testT[i].trim().length()<2){
				continue;
			}
			String tclass="";//保存当前候选词的情感类别
			for (int j = 0; j < classes.length; j++) {
				Set<String> list =swords.get(classes[j].trim());
				//判断待测词是否在该词集中
				Iterator<String> listit = list.iterator();
				boolean flag =false;
				while(listit.hasNext()){
					String sw = listit.next().toString().trim();
					if(sw.equals(testT[i].trim()) || testT[i].trim().indexOf(sw)!=-1){
						tclass = classes[j].trim();
						flag =true;
						break;
					}
				}//end while
				if(flag) break;
			}//end for
			if("".equals(tclass)){
				String s = testT[i].trim();
				//不在原有的情感词典当中，用SE-PMI方法计算
				tclass = sePmi.getSentiClass(s);
				if(!"neutral".equals(tclass)){
					//识别出新的不存在在原情感词典中的情感词，则需要加入数据库,同时更新本地情感词典
					boolean isExist= sentiDao.querySentiWordByName(s);//数据库中是否已存在
					if(!isExist){//不存在则加入数据库
						if(s.startsWith("[") && s.endsWith("]")){//判断是否为表情符号
							sentiDao.insertNewSentiWord(s,tclass,0,1,1,1);
						}else{
							sentiDao.insertNewSentiWord(s,tclass,0,1,1,0);
						}
					}
					//更新本地情感词典
					Set<String> sli = swords.get(tclass);
					sli.add(s);
					swords.put(tclass, sli);
				}
				
			}
			//如果为情感词则继续进行否定词的判断
			if(!"neutral".equals(tclass) && !"".equals(tclass)){
				int count=0;
				if(i>=2){
					if(this.isNegtive(testT[i-1].trim()) || this.isNegtive(testT[i-2].trim())){
						if(tclass.equals("joyful")){
							//正面情绪转为负面情绪
							count = classCounts.get("sad");
							count++;
							classCounts.put("sad", count);
						}else{
							//负面情绪转为正面情绪
							count = classCounts.get("joyful");
							count++;
							classCounts.put("joyful", count);
						}
					}else{//前面没有否定词则直接添加
						count = classCounts.get(tclass);
						count++;
						classCounts.put(tclass, count);
					}
				}else if(i>=1){
					if(this.isNegtive(testT[i-1].trim())){
						if(tclass.equals("joyful")){
							//正面情绪转为负面情绪
							count = classCounts.get("sad");
							count++;
							classCounts.put("sad", count);
						}else{
							//负面情绪转为正面情绪
							count = classCounts.get("joyful");
							count++;
							classCounts.put("joyful", count);
						}
					}else{//前面没有否定词则直接添加
						count = classCounts.get(tclass);
						count++;
						classCounts.put(tclass, count);
					}
				}else{//前面没有词语则无需再继续判断
					count = classCounts.get(tclass);
					count++;
					classCounts.put(tclass, count);
				}
			}//end if
		}//end for
		//比较情感词个数，判断微博的情感类别
		int index=-1;int maxCount = 0;
		for (int k = 0; k < classes.length; k++) {
			if( classCounts.get(classes[k])>maxCount){
				index=k;
				maxCount =classCounts.get(classes[k]);
			}
		}
		if(index!=-1 && maxCount!=0){
			return classes[index].toString();
		}else{
			return "neutral";
		}
		
	}//end method
	/**
	 *内部调用(加入了表情符号以及双重否定的基于SE-PMI的情感分类方法调用)
	 * @param testT 待测微博，这里一条微博分词后变成一组词序列，通过该方法确定一条微博所属类别
	 * @return 待测微博的类别
	 */
	public String ePredictClassOfMicro(String[] testT) {
		//存放属于各个情感类别的情感个数
		Map<String,Integer> classCounts = new HashMap<String,Integer>();
		for (int n = 0; n < classes.length; n++) {
			classCounts.put(classes[n], 0);
			if(swords.get(classes[n].toString().trim())==null){
				//从数据库中获取该情感类别的情感词(包含表情符号)
				Set<String> list =sentiDao.getSentiWordsOfClass(classes[n],null);
				swords.put(classes[n], list);
			}
		}
		for (int i = 0; i < testT.length; i++) {
			if(testT[i].trim().length()<2){
				continue;
			}
			String tclass="";
			for (int j = 0; j < classes.length; j++) {
				//从数据库中获取该情感类别的情感词(不包含表情符号)
				Set<String> list =swords.get(classes[j].trim());
				//判断待测词是否在该词集中
				Iterator<String> listit = list.iterator();
				boolean flag =false;
				while(listit.hasNext()){
					String sw = listit.next().toString().trim();
					if(sw.equals(testT[i].trim()) || testT[i].trim().indexOf(sw)!=-1){
						tclass = classes[j].trim();
						flag =true;
						break;
					}
				}//end while
				if(flag) break;
			}//end for
			//SE-PMI方法识别新词
			if("".equals(tclass)){
				String s = testT[i].trim();
				//System.out.println("**************" + s);
				//不在原有的情感词典当中，用SE-PMI方法计算
				tclass = sePmi.getSentiClass(s);
				if(!"neutral".equals(tclass)){
					//识别出新的不存在在原情感词典中的情感词，则需要加入数据库,同时更新本地情感词典
					boolean isExist= sentiDao.querySentiWordByName(s);//数据库中是否已存在
					if(!isExist){//不存在则加入数据库
						if(s.startsWith("[") && s.endsWith("]")){//判断是否为表情符号
							sentiDao.insertNewSentiWord(s,tclass,0,1,1,1);
						}else{
							sentiDao.insertNewSentiWord(s,tclass,0,1,1,0);
						}
					}
					//更新本地情感词典
					Set<String> sli = swords.get(tclass);
					sli.add(s);
					swords.put(tclass, sli);
				}
			}
			//如果为情感词则继续进行否定词的判断
			if(!"neutral".equals(tclass) && !"".equals(tclass)){
				int count=0;
				if(i>=2){
					if(this.isNegtive(testT[i-1].trim()) || this.isNegtive(testT[i-2].trim())){
						String tempw = testT[i-1].trim()+testT[i-2].trim();
						if(!this.isDNegtive(tempw)){
							if(tclass.equals("joyful")){
								//正面情绪转为负面情绪
								count = classCounts.get("sad");
								count++;
								classCounts.put("sad", count);
							}else{
								//负面情绪转为正面情绪
								count = classCounts.get("joyful");
								count++;
								classCounts.put("joyful", count);
							}
						}else{//是双重否定则不进行情感极性转换
							count = classCounts.get(tclass);
							count++;
							classCounts.put(tclass, count);
						}
					}else{//前面没有否定词则直接添加
						count = classCounts.get(tclass);
						count++;
						classCounts.put(tclass, count);
					}
				}else if(i>=1){
					if(this.isNegtive(testT[i-1].trim()) && !this.isDNegtive(testT[i-1].trim())){
						if(tclass.equals("joyful")){
							//正面情绪转为负面情绪
							count = classCounts.get("sad");
							count++;
							classCounts.put("sad", count);
						}else{
							//负面情绪转为正面情绪
							count = classCounts.get("joyful");
							count++;
							classCounts.put("joyful", count);
						}
					}else{//前面没有否定词或为双重否定则不进行情感极性转换
						count = classCounts.get(tclass);
						count++;
						classCounts.put(tclass, count);
					}
				}else{//前面没有词语则无需再继续判断
					count = classCounts.get(tclass);
					count++;
					classCounts.put(tclass, count);
				}
			}//end if
		}//end for
		//比较情感词个数，判断微博的情感类别
		int index=-1;int maxCount = 0;
		for (int k = 0; k < classes.length; k++) {
			if( classCounts.get(classes[k])>maxCount){
				index=k;
				maxCount =classCounts.get(classes[k]);
			}
		}
		if(index!=-1 && maxCount!=0){
			return classes[index].toString();
		}else{
			return "neutral";
		}
		
	}//end method
	/**
	 * 内部调用，查找某一词语集的同义词
	 * params  words
	 * return  list 返回同义词词组+words的集合
	 * 
	 */
	public Set<String> findSynWords(Set<String> words){
		
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
		Iterator tei = totalSet.iterator();
		ArrayList<String> removeset = new ArrayList<String>();
		while(tei.hasNext()){//去掉单个字符
			String ne= tei.next().toString().trim();
			if(ne.length()<2){
				removeset.add(ne);
			}
		}
		//去除单个字符
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
	  * * 内部调用，从文本中读入同义词林，并转为ArrayList格式
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
	 * 内部调用，判断一个词是否为否定词
	 * @param word
	 * @return
	 */
	public boolean isNegtive(String word){
		boolean flag= false;
		for (int i = 0; i < negatives.length; i++) {
			if(word.equals(negatives[i].trim()) || word.indexOf(negatives[i].trim())!=-1){
				flag = true;
				break;
			}
		}
		return flag;
	}
	/**
	 * 内部调用，判断一个词是否为双重否定词
	 * @param word
	 * @return
	 */
	public boolean isDNegtive(String word){
		boolean flag= false;
		for (int i = 0; i < dnegatives.length; i++) {
			if(word.equals(dnegatives[i].trim()) || word.indexOf(dnegatives[i].trim())!=-1){
				flag = true;
				break;
			}
		}
		return flag;
	}
}
