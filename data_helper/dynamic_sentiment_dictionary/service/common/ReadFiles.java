package service.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import dao.SentiWordsDAO;
import dao.TestWordsDAO;

public class ReadFiles {
	/**
	 * ����ӿڣ��ṩͣ�ôʴʼ�
	 * @throws IOException 
	 */
	public Set<String> getStopWords() {
		try{
			Set<String> stopWordSet  = new HashSet<String>();
			//��ȡͣ�ôʱ�
			String stopWordFile = "ExternalFiles/StopWords.txt";
			BufferedReader StopWordFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(stopWordFile)))); 
			//�������ͣ�ôʵļ��� 
			//���绯ͣ�ôʼ� 
			String stopWord = null; 
			for(; (stopWord = StopWordFileBr.readLine()) != null;){ 
				stopWordSet.add(stopWord); 
			}		
			return stopWordSet;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * ����ӿڣ��ṩͬ��ʴʼ�
	 */
	public Set<String> getSynonymsWords(){
		try{
			Set<String> set =new HashSet<String>();
			//��ȡͬ��ʱ�
			String synonymsWordFile = "ExternalFiles/Synonyms.txt";
			BufferedReader SynonymsWordFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(synonymsWordFile)))); 
			//�������ͬ��ʵļ��� 
			//���绯ͬ��ʼ� 
			String synonymsWord = null; 
			for(; (synonymsWord = SynonymsWordFileBr.readLine()) != null;){ 
				set.add(synonymsWord); 
			}
			return set;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 *����ӿڣ����ļ��ж�ȡ��дʲ��������ݿ��SentiWords
	 */	
	public void readSentiWords(String sentiFile,String stype,int isBase,int isNet,int isExNet,int isEmotion){
		try{
			SentiWordsDAO sentidao = new SentiWordsDAO();
			//��ȡ�ļ�
			//String stopWordFile = "ExternalFiles/SentiWords.txt";
			BufferedReader SentWordFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(sentiFile)))); 
			
			String newSentiWord = null; 
			for(; (newSentiWord = SentWordFileBr.readLine()) != null;){ 
				sentidao.insertNewSentiWord(newSentiWord,stype,isBase,isNet,isExNet,isEmotion);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 *����ӿڣ����ļ��ж�ȡ��дʲ��������ݿ��TestWords
	 */	
	public void readTestWords(String testFile,String ostype){
		try{
			TestWordsDAO sentidao = new TestWordsDAO();
			//��ȡ�ļ�
			//String stopWordFile = "ExternalFiles/SentiWords.txt";
			BufferedReader SentWordFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(testFile)))); 
			
			String newSentiWord = null; 
			for(; (newSentiWord = SentWordFileBr.readLine()) != null;){ 
				sentidao.insertNewTestWord(newSentiWord,ostype);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
