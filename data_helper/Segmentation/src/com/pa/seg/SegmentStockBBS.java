package com.pa.seg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.pa.util.TEXT_TOOL;
import com.pa.util.unifiedDifferentTab;

import org.ansj.dic.LearnTool;

public class SegmentStockBBS implements Runnable {
	
	private String infile;
	private String outfile;
	
	public SegmentStockBBS(String infile, String outfile) {
		this.infile = infile;
		this.outfile = outfile;
	}
	
	@Override
	public void run() {
		
		String line = null;
		//StringBuffer sb = null;
		//String allToken = "";
		BufferedReader br = null;
		BufferedWriter bw = null;
		int miss_pid = 0;
//		unifiedDifferentTab udt = new unifiedDifferentTab();
//		LearnTool learnTool = new LearnTool() ;
		try {
			br = Files.newReader(new File(infile), Charsets.UTF_8);
			bw= Files.newWriter(new File(outfile), Charsets.UTF_8);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			while((line = br.readLine())!=null){
				String text = "";
				//String belong_index = "";
				line = line.trim();
				if(line.equals("")){
					continue;
				}
				String[] token = line.split("\t");
				
				if(token.length<2){  //12
//					continue;
					miss_pid += 1;
					text = "null";
					System.out.println(miss_pid);
				}else if(token.length>2){  //12
					for(int i=1;i<(token.length-1);i++){  //11
						text += token[i];
					}

				}else {
					text = token[1];  //11
				}														
//				text = line;
				String cleantext = TEXT_TOOL.clean_bbs(text).trim();
				
//				if(cleantext.equals("")){
//					continue;
//				}
				//,learnTool
				String segtext = TEXT_TOOL.seg_text(cleantext).replaceAll("\t", " ");
				// 分词后为空，也保留
				if(segtext.equals("")){
					segtext = "null";
				}
//				if(!segtext.equals("")){
				//token[0] + "\t"  +  token[1] + "\t"  +  segtext + "\t"  +  token[token.length-1]
					bw.write(token[0] + "\t"  +  segtext); //token[1]
//					bw.write(segtext);
					bw.newLine();
					bw.flush();
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		String ifile = "G:\\eastmoney_guba_1000stocks_ugc\\318stocks_pid_have_comments_divide.csv";
		String ofile = "G:\\eastmoney_guba_1000stocks_ugc\\318stocks_pid_have_comments_divide_seg.csv";
		SegmentStockBBS sw = new SegmentStockBBS(ifile,ofile);
		Thread t = new Thread(sw);
		t.start();
	}

}
