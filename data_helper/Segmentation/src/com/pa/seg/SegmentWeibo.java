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

public class SegmentWeibo implements Runnable {
	
	private String infile;
	private String outfile;
	
	public SegmentWeibo(String infile, String outfile) {
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
//				String date_time = "";
//				String read_num = "";
//				String comment_num = "";
				line = line.trim();
				if(line.equals("")){
					continue;
				}
				//String lineNew = udt.replaceAllBlank(line);
//				String[] token = line.split(","); //","
//				
//				if(token.length<2){
//					text = ""; //token[0]
//					continue;
//				}else if(token.length>2){
//					for(int i=1;i<token.length-1;i++){
//						//sb.append(token[i]);
//						text =text+ "," +  token[i] ;
//					}
//					date_time = token[token.length-1] + "-"+ token[token.length-2]; //token.length-1
//					read_num = token[token.length-4];
//					comment_num = token[token.length-3];
					//text = sb.toString();
					//sb.delete(0, sb.length());
					//belong_index = token[token.length-1];
//				}else {
//					text = token[1];
//					belong_index = token[2];
//					date_time = token[4] + "-"+ token[3];
//					read_num = token[1];
//					comment_num = token[2];
//				}				
										
				text = line; 				
				String cleantext = TEXT_TOOL.clean_weibo(text).trim();
				
				if(cleantext.equals("")){
					continue;
				}
				
				String segtext = TEXT_TOOL.seg_text(cleantext).replaceAll("\t", " ");
				if(segtext.equals(" ")){
					continue;
				}
				if(!segtext.equals("")){
//					bw.write(date_time + "\t" + segtext + "\t" + read_num + "\t" + comment_num); // + date_time + "\n"
					bw.write(segtext); //token[0] +  "\t" + 
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
//		String ifile = "C:\\Users\\lenovo\\Desktop\\intelligence_concept_stock\\individual_stock_discription\\stock_name_info.csv";
//		String ofile = "C:\\Users\\lenovo\\Desktop\\intelligence_concept_stock\\individual_stock_discription\\stock_name_info_seg.csv";
		String ifile = "D:\\SMU_WORK\\event_gainiangu_building_model\\weibo\\weibo_corpus.csv";
		String ofile = "D:\\SMU_WORK\\event_gainiangu_building_model\\weibo\\weibo_corpus_seg.csv";
		SegmentWeibo sw = new SegmentWeibo(ifile,ofile);
		Thread t = new Thread(sw);
		t.start();
	}

}
