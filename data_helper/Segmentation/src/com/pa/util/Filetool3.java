package com.pa.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class Filetool3 {
	
	public Map<String, String> file2list(String infile1){
		String line1 = null;
		BufferedReader br1 = null;
		//ArrayList al = new ArrayList();
		Map<String, String> map = new HashMap<String, String>();
		//al.clear();
		int flag = 0;
		try {
			br1 = Files.newReader(new File(infile1), Charsets.UTF_8);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			while(((line1 = br1.readLine())!=null)){  // && flag < 6000			
					line1 = line1.trim();
					String description = "";
					if(line1.equals("")){
						continue;
					}				
					String[] token = line1.split("\t");
				    String uid = token[0];
				    for(int i=1;i<token.length;i++){
				    	description += token[i];
				    }				    
				    map.put(uid, description);
				    //al.add(uid);
				    flag += 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		         	     
        try {	     		
			br1.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        return map;
	}
	
	public void Fromfile2File(Map<String, String> al,String infile2,String outfile){				
			String line2 = null;			
			BufferedReader br2 = null;
			BufferedWriter bw = null;
			//StringBuffer sb = null;
			try {			
				br2 = Files.newReader(new File(infile2), Charsets.UTF_8);
				bw= Files.newWriter(new File(outfile), Charsets.UTF_8);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			try {		
			    while((line2 = br2.readLine())!=null){
			    	String text = "";
			    	line2 = line2.trim();
					if(line2.equals("")){
						continue;
					}				
					String[] token2 = line2.split("\t");										
					if(token2.length<2){
						continue;
					}else if(token2.length>2){
						for(int i=1;i<token2.length-1;i++){
							//sb.append(token2[i]);
							text += token2[i];
						}
						//text = sb.toString();
						//sb.delete(0, sb.length());
					}else {					
					}
					text = token2[1];
				    String uid2 = token2[0];					   					    
				    if(al.keySet().contains(uid2)){
				    	 try{	
					        	//if(!token2[1].equals("")){
						        	bw.write(uid2 + "\t"  + text.replaceAll("\\[", "").replaceAll("\\]", "")+ "\t"  + al.get(uid2)+ "\t"  + token2[token2.length-1] );//
									bw.newLine();
									bw.flush();
					        	//}
									token2 = null;
				        	} catch (IOException e) {
				    			e.printStackTrace();
				    		} catch (Exception e) {
				    			e.printStackTrace();
				    		}			         						    						   						    	
				    }
				}
			    al.clear();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}		         	     
	        try {	     
				bw.close();
				br2.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}           
		
   }
	
	public static void main(String[] args){
		Filetool3 dbt = new Filetool3();
		//ArrayList al = new ArrayList(); 
		String ifile1 = "D:\\dataIO\\agFiles\\input\\selectedUserWeibo\\fileMerge\\weibo_description_seg.txt";
		String ifile2 = "D:\\dataIO\\agFiles\\input\\selectedUserWeibo\\fileMerge\\select_weibo_seg.txt";
		String ofile = "D:\\dataIO\\agFiles\\input\\selectedUserWeibo\\fileMerge\\weibo_occupation_cd.txt";		
		dbt.Fromfile2File(dbt.file2list(ifile1),ifile2,ofile);
		
		
	}
	
	
	
}
