package com.pa.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class updataData {
	
	public ArrayList file2list(String infile1){
		String line1 = null;
		BufferedReader br1 = null;
		ArrayList al = new ArrayList();
		al.clear();
		int flag = 0;
		try {
			br1 = Files.newReader(new File(infile1), Charsets.UTF_8);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			while(((line1 = br1.readLine())!=null)){  // && flag < 6000			
					line1 = line1.trim();
					if(line1.equals("")){
						continue;
					}				
					String[] token = line1.split("\t");
				    String uid = token[0];
				    al.add(uid);
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
        return al;
	}
	
	public void Fromfile2File(ArrayList al,String infile2,String outfile){				
			String line2 = null;			
			BufferedReader br2 = null;
			BufferedWriter bw = null;
			StringBuffer sb = null;
			try {			
				br2 = Files.newReader(new File(infile2), Charsets.UTF_8);
				bw= Files.newWriter(new File(outfile), Charsets.UTF_8);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			try {		
			    while((line2 = br2.readLine())!=null){
			    	String community = "";
			    	String uid = "";
			    	line2 = line2.trim();
					if(line2.equals("")){
						continue;
					}				
					String[] token2 = line2.split("\t");										
					if(token2.length<2){
						continue;
					}				
				    String uid2 = token2[0];					   					    
				    if(al.contains(uid2)){
				    	 try{	
				    		        community = "0." + token2[1];
				    		 //if(!token2[1].equals("")){
						        	bw.write(uid2 + "\t"  +  community);
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
		Filetool2 dbt = new Filetool2();
		//ArrayList al = new ArrayList(); 
		String ifile1 = "D:\\dataIO\\agFiles\\input\\selectedUserWeibo\\fileMerge\\weibo_description_seg.txt";
		String ifile2 = "D:\\dataIO\\agFiles\\input\\selectedUserWeibo\\fileMerge\\community.txt";
		String ofile = "D:\\dataIO\\agFiles\\input\\selectedUserWeibo\\fileMerge\\community_normalize";		
		dbt.Fromfile2File(dbt.file2list(ifile1),ifile2,ofile);
		
		
	}
	
	
	
}
