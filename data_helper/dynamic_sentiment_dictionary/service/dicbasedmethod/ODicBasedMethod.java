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
 * ���ڴʵ�ķ���:��ͳ���Լ�����SE-PMI��
 * ����3�£���ͳ�Ļ��ڴʵ�ķ�����predictClass�����е���oPredictClassOfMicro�������ж�һ��΢����������
 * ����3�£�����SE-PMI����з��෽����predictClass�����е���nPredictClassOfMicro�������ж�һ��΢����������
 * ����3�£������˱�����ź�˫�ط��жϵĻ���SE-PMI����з��෽����predictClass�����е���ePredictClassOfMicro�������ж�һ��΢����������
 */
public class ODicBasedMethod {
	MicroBlogDAO microDao =new MicroBlogDAO();
	SentiWordsDAO sentiDao = new SentiWordsDAO();
	SePMI sePmi = new SePMI();
	String [] classes = {"angry","contempt","sad","joyful"};
	String [] negatives = {"������","��ô��","������","������","�Ӳ�","����","����","����","����","����","����","����","û��","����","����","��ֹ","ֹͣ","����","����","��Ҫ","δ��","��","��","��","δ","��","û","��"};
	String [] dnegatives = {"���ǲ�","���ǲ�","���ǲ�","���ܲ�","���᲻","���ɲ�","��Ҫ��","���ò�","û�в�","�޲�","����"};
	Map<String,Set<String>> swords = new HashMap<String, Set<String>>();//��Ÿ������������д�
	ArrayList<Set<String>> synlist=null; //ͬ��ʼ�
	/**
	 *����ӿڣ� �ж�΢���������
	 * @param Map<Integer,String> ����΢����ֵ�ԣ���Ϊ΢�������ݿ��е�id��ֵΪ�ִ����У���Ӧ���ݿ��е�words�ֶ�
	 * 
	 * 	 
	 * */
	public void predictClass(Map<Integer,String> micros){
		Set<Integer> keys = micros.keySet();
		Iterator<Integer> it = keys.iterator();
		while(it.hasNext()){
			int key = it.next();
			String[] arr = micros.get(key).toString().split(" ");//��ȡ��ǰ�����΢���ִ�����
			//String mclass = this.oPredictClassOfMicro(arr);//��ͳ�Ļ��ڴʵ�ķ�������ȡ��ǰ΢�����������
			//String mclass = this.nPredictClassOfMicro(arr);//����SE-PMI����з��෽������ȡ��ǰ΢�����������
			String mclass = this.ePredictClassOfMicro(arr);//�����˱�����ź�˫�ط��жϵĻ���SE-PMI����з��෽������ȡ��ǰ΢�����������
			//�������ݿ�
			microDao.updateClassesOfMicro(key,mclass);
		}
		
	}
	//���Դ������3
	public void predictTrainClass(Map<Integer,String> micros){
		Set<Integer> keys = micros.keySet();
		Iterator<Integer> it = keys.iterator();
		while(it.hasNext()){
			int key = it.next();
			String[] arr = micros.get(key).toString().split(" ");//��ȡ��ǰ�����΢���ִ�����
			//String mclass = this.oPredictClassOfMicro(arr);//��ͳ�Ļ��ڴʵ�ķ�������ȡ��ǰ΢�����������
			//String mclass = this.nPredictClassOfMicro(arr);//����SE-PMI����з��෽������ȡ��ǰ΢�����������
			String mclass = this.ePredictClassOfMicro(arr);//�����˱�����ź�˫�ط��жϵĻ���SE-PMI����з��෽������ȡ��ǰ΢�����������
			//���Ϊѵ���������������ݿ�
			microDao.updateTrainClassesOfMicro(key,mclass);
		}
		
	}
	
	/**
	 *�ڲ�����(��ͳ�Ļ��ڴʵ�ķ�������)�� ��ѵ�����ݵĻ�����Ԥ�����Ԫ������
	 * @param testT ����΢��������һ��΢���ִʺ���һ������У�ͨ���÷���ȷ��һ��΢���������
	 * @return ����΢�������
	 */
	public String oPredictClassOfMicro(String[] testT) {
		//������ڸ����������΢������
		Map<String,Integer> classCounts = new HashMap<String,Integer>();
		for (int n = 0; n < classes.length; n++) {
			classCounts.put(classes[n], 0);
			if(swords.get(classes[n].toString().trim())==null){
				//�����ݿ��л�ȡ�����������д�(������������ź�����)
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
				//�жϴ�����Ƿ��ڸôʼ���
				Iterator listit = list.iterator();
				boolean flag =false;
				while(listit.hasNext()){
					String sw = listit.next().toString().trim();
					if(sw.equals(testT[i].trim()) || testT[i].trim().indexOf(sw)!=-1){
						int count=0;
						//����дʽ��з񶨴ʵ��ж�
						if(i>=2){
							if(this.isNegtive(testT[i-1].trim()) || this.isNegtive(testT[i-2].trim())){
								if(classes[j].trim().equals("joyful")){
									//��������תΪ��������
									count = classCounts.get("sad");
									count++;
									classCounts.put("sad", count);
								}else{
									//��������תΪ��������
									count = classCounts.get("joyful");
									count++;
									classCounts.put("joyful", count);
								}
							}else{//ǰ��û�д����������ټ����ж�
								count = classCounts.get(classes[j]);
								count++;
								classCounts.put(classes[j], count);
							}
						}else if(i>=1){
							if(this.isNegtive(testT[i-1].trim())){
								if(classes[j].trim().equals("joyful")){
									//��������תΪ��������
									count = classCounts.get("sad");
									count++;
									classCounts.put("sad", count);
								}else{
									//��������תΪ��������
									count = classCounts.get("joyful");
									count++;
									classCounts.put("joyful", count);
								}
							}else{//ǰ��û�д����������ټ����ж�
								count = classCounts.get(classes[j]);
								count++;
								classCounts.put(classes[j], count);
							}
						}else{//ǰ��û�д����������ټ����ж�
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
		//�Ƚ���дʸ������ж�΢����������
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
	 *�ڲ�����(����SE-PMI����з��෽������)�� ��ѵ�����ݵĻ�����Ԥ�����Ԫ������
	 * @param testT ����΢��������һ��΢���ִʺ���һ������У�ͨ���÷���ȷ��һ��΢���������
	 * @return ����΢�������
	 */
	public String nPredictClassOfMicro(String[] testT) {
		//������ڸ������������и���
		Map<String,Integer> classCounts = new HashMap<String,Integer>();
		for (int n = 0; n < classes.length; n++) {
			classCounts.put(classes[n], 0);
			if(swords.get(classes[n].toString().trim())==null){
				//�����ݿ��л�ȡ�����������д�(������������ź�����)
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
			String tclass="";//���浱ǰ��ѡ�ʵ�������
			for (int j = 0; j < classes.length; j++) {
				Set<String> list =swords.get(classes[j].trim());
				//�жϴ�����Ƿ��ڸôʼ���
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
				//����ԭ�е���дʵ䵱�У���SE-PMI��������
				tclass = sePmi.getSentiClass(s);
				if(!"neutral".equals(tclass)){
					//ʶ����µĲ�������ԭ��дʵ��е���дʣ�����Ҫ�������ݿ�,ͬʱ���±�����дʵ�
					boolean isExist= sentiDao.querySentiWordByName(s);//���ݿ����Ƿ��Ѵ���
					if(!isExist){//��������������ݿ�
						if(s.startsWith("[") && s.endsWith("]")){//�ж��Ƿ�Ϊ�������
							sentiDao.insertNewSentiWord(s,tclass,0,1,1,1);
						}else{
							sentiDao.insertNewSentiWord(s,tclass,0,1,1,0);
						}
					}
					//���±�����дʵ�
					Set<String> sli = swords.get(tclass);
					sli.add(s);
					swords.put(tclass, sli);
				}
				
			}
			//���Ϊ��д���������з񶨴ʵ��ж�
			if(!"neutral".equals(tclass) && !"".equals(tclass)){
				int count=0;
				if(i>=2){
					if(this.isNegtive(testT[i-1].trim()) || this.isNegtive(testT[i-2].trim())){
						if(tclass.equals("joyful")){
							//��������תΪ��������
							count = classCounts.get("sad");
							count++;
							classCounts.put("sad", count);
						}else{
							//��������תΪ��������
							count = classCounts.get("joyful");
							count++;
							classCounts.put("joyful", count);
						}
					}else{//ǰ��û�з񶨴���ֱ�����
						count = classCounts.get(tclass);
						count++;
						classCounts.put(tclass, count);
					}
				}else if(i>=1){
					if(this.isNegtive(testT[i-1].trim())){
						if(tclass.equals("joyful")){
							//��������תΪ��������
							count = classCounts.get("sad");
							count++;
							classCounts.put("sad", count);
						}else{
							//��������תΪ��������
							count = classCounts.get("joyful");
							count++;
							classCounts.put("joyful", count);
						}
					}else{//ǰ��û�з񶨴���ֱ�����
						count = classCounts.get(tclass);
						count++;
						classCounts.put(tclass, count);
					}
				}else{//ǰ��û�д����������ټ����ж�
					count = classCounts.get(tclass);
					count++;
					classCounts.put(tclass, count);
				}
			}//end if
		}//end for
		//�Ƚ���дʸ������ж�΢����������
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
	 *�ڲ�����(�����˱�������Լ�˫�ط񶨵Ļ���SE-PMI����з��෽������)
	 * @param testT ����΢��������һ��΢���ִʺ���һ������У�ͨ���÷���ȷ��һ��΢���������
	 * @return ����΢�������
	 */
	public String ePredictClassOfMicro(String[] testT) {
		//������ڸ������������и���
		Map<String,Integer> classCounts = new HashMap<String,Integer>();
		for (int n = 0; n < classes.length; n++) {
			classCounts.put(classes[n], 0);
			if(swords.get(classes[n].toString().trim())==null){
				//�����ݿ��л�ȡ�����������д�(�����������)
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
				//�����ݿ��л�ȡ�����������д�(�������������)
				Set<String> list =swords.get(classes[j].trim());
				//�жϴ�����Ƿ��ڸôʼ���
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
			//SE-PMI����ʶ���´�
			if("".equals(tclass)){
				String s = testT[i].trim();
				//System.out.println("**************" + s);
				//����ԭ�е���дʵ䵱�У���SE-PMI��������
				tclass = sePmi.getSentiClass(s);
				if(!"neutral".equals(tclass)){
					//ʶ����µĲ�������ԭ��дʵ��е���дʣ�����Ҫ�������ݿ�,ͬʱ���±�����дʵ�
					boolean isExist= sentiDao.querySentiWordByName(s);//���ݿ����Ƿ��Ѵ���
					if(!isExist){//��������������ݿ�
						if(s.startsWith("[") && s.endsWith("]")){//�ж��Ƿ�Ϊ�������
							sentiDao.insertNewSentiWord(s,tclass,0,1,1,1);
						}else{
							sentiDao.insertNewSentiWord(s,tclass,0,1,1,0);
						}
					}
					//���±�����дʵ�
					Set<String> sli = swords.get(tclass);
					sli.add(s);
					swords.put(tclass, sli);
				}
			}
			//���Ϊ��д���������з񶨴ʵ��ж�
			if(!"neutral".equals(tclass) && !"".equals(tclass)){
				int count=0;
				if(i>=2){
					if(this.isNegtive(testT[i-1].trim()) || this.isNegtive(testT[i-2].trim())){
						String tempw = testT[i-1].trim()+testT[i-2].trim();
						if(!this.isDNegtive(tempw)){
							if(tclass.equals("joyful")){
								//��������תΪ��������
								count = classCounts.get("sad");
								count++;
								classCounts.put("sad", count);
							}else{
								//��������תΪ��������
								count = classCounts.get("joyful");
								count++;
								classCounts.put("joyful", count);
							}
						}else{//��˫�ط��򲻽�����м���ת��
							count = classCounts.get(tclass);
							count++;
							classCounts.put(tclass, count);
						}
					}else{//ǰ��û�з񶨴���ֱ�����
						count = classCounts.get(tclass);
						count++;
						classCounts.put(tclass, count);
					}
				}else if(i>=1){
					if(this.isNegtive(testT[i-1].trim()) && !this.isDNegtive(testT[i-1].trim())){
						if(tclass.equals("joyful")){
							//��������תΪ��������
							count = classCounts.get("sad");
							count++;
							classCounts.put("sad", count);
						}else{
							//��������תΪ��������
							count = classCounts.get("joyful");
							count++;
							classCounts.put("joyful", count);
						}
					}else{//ǰ��û�з񶨴ʻ�Ϊ˫�ط��򲻽�����м���ת��
						count = classCounts.get(tclass);
						count++;
						classCounts.put(tclass, count);
					}
				}else{//ǰ��û�д����������ټ����ж�
					count = classCounts.get(tclass);
					count++;
					classCounts.put(tclass, count);
				}
			}//end if
		}//end for
		//�Ƚ���дʸ������ж�΢����������
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
	 * �ڲ����ã�����ĳһ���Ｏ��ͬ���
	 * params  words
	 * return  list ����ͬ��ʴ���+words�ļ���
	 * 
	 */
	public Set<String> findSynWords(Set<String> words){
		
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
		Iterator tei = totalSet.iterator();
		ArrayList<String> removeset = new ArrayList<String>();
		while(tei.hasNext()){//ȥ�������ַ�
			String ne= tei.next().toString().trim();
			if(ne.length()<2){
				removeset.add(ne);
			}
		}
		//ȥ�������ַ�
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
	  * * �ڲ����ã����ı��ж���ͬ����֣���תΪArrayList��ʽ
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
	 * �ڲ����ã��ж�һ�����Ƿ�Ϊ�񶨴�
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
	 * �ڲ����ã��ж�һ�����Ƿ�Ϊ˫�ط񶨴�
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
