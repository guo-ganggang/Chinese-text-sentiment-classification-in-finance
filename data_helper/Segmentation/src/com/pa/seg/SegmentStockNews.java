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


public class SegmentStockNews implements Runnable {
	
	private String infile;
	private String outfile;
	
	public SegmentStockNews(String infile, String outfile) {
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
		//unifiedDifferentTab udt = new unifiedDifferentTab();
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
				
				if(token.length<5){
					continue;
				}else if(token.length>5){
					for(int i=4;i<(token.length-1);i++){
						text += token[i];
					}

				}else {
					text = token[4];
				}														
								
				String cleantext = TEXT_TOOL.clean_news(text).trim();
				
				if(cleantext.equals("")){
					continue;
				}
				
				String segtext = TEXT_TOOL.seg_text(cleantext).replaceAll("\t", " ");
				
				if(!segtext.equals("")){
					//token[0] + "\t"  +  token[1] + "\t"  +  segtext + "\t"  +  token[token.length-1]
					bw.write(token[0] + "\t"  + segtext);
					bw.newLine();
					bw.flush();
				}
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
		String ifile = "G:\\eastmoney_guba_1000stocks_ugc\\find_delete_post\\miss_post_info_beyond_makeup_318.csv";
		String ofile = "G:\\eastmoney_guba_1000stocks_ugc\\find_delete_post\\318\\miss_post_info_beyond_makeup_318_seg.csv";
		SegmentStockNews sw = new SegmentStockNews(ifile,ofile);
		Thread t = new Thread(sw);
		t.start();
	}

}
