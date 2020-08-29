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
	 * 对外接口，提供停用词词集
	 * @throws IOException 
	 */
	public Set<String> getStopWords() {
		try{
			Set<String> stopWordSet  = new HashSet<String>();
			//读取停用词表
			String stopWordFile = "ExternalFiles/StopWords.txt";
			BufferedReader StopWordFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(stopWordFile)))); 
			//用来存放停用词的集合 
			//初如化停用词集 
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
	 * 对外接口，提供同义词词集
	 */
	public Set<String> getSynonymsWords(){
		try{
			Set<String> set =new HashSet<String>();
			//读取同义词表
			String synonymsWordFile = "ExternalFiles/Synonyms.txt";
			BufferedReader SynonymsWordFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(synonymsWordFile)))); 
			//用来存放同义词的集合 
			//初如化同义词集 
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
	 *对外接口，从文件中读取情感词并存入数据库表SentiWords
	 */	
	public void readSentiWords(String sentiFile,String stype,int isBase,int isNet,int isExNet,int isEmotion){
		try{
			SentiWordsDAO sentidao = new SentiWordsDAO();
			//读取文件
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
	 *对外接口，从文件中读取情感词并存入数据库表TestWords
	 */	
	public void readTestWords(String testFile,String ostype){
		try{
			TestWordsDAO sentidao = new TestWordsDAO();
			//读取文件
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
