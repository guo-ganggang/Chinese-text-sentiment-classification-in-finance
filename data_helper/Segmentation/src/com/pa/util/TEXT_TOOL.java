package com.pa.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.commons.lang.StringEscapeUtils;
import org.nlpcn.commons.lang.jianfan.JianFan;
import org.ansj.dic.LearnTool;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.UnicodeBlock.*;
import com.google.common.base.Strings;

public class TEXT_TOOL {
	
	static String zfpattern = "转发微博。|转发微博|转发微博  ";
	static String replyPattern = "回复\\s*@.+?[:：\\s]";
	public static String atPattern = "(@.+?[\\s:])";
	public static String at_endPattern = "(@.+?$)";
	static String sharePattern = "\\(分享自.+?\\)";
	static String topicPattern = "#.+?#";
	static String titlePattern = "【.+?】";
	static String puncPattern = ",.;?";
	public static String emoPattern = "\\[.+?\\]";
	static Pattern postagPattern = Pattern.compile("/[a-zA-Z]+[0-9]?$");
	
	
	static String punctChars = "['\"“”‘’.?!…,:;]"; 
    static String punctSeq   = "['\"“”‘’]+|[.?!,…]+|[:;]+";        //'anthem'. => ' anthem ' .
    static String entity     = "&(?:amp|lt|gt|quot);";

    static String urlStart1  = "(?:https?://|\\bwww\\.)";
    static String commonTLDs = "(?:com|org|edu|gov|net|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|pro|tel|travel|xxx)";
    static String ccTLDs         = "(?:ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|" +
    		"bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|" +
    		"er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|" +
    		"hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|" +
    		"lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|" +
    		"nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|" +
    		"sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|" +
    		"va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)";        //TODO: remove obscure country domains?
    static String urlStart2  = "\\b(?:[A-Za-z\\d-])+(?:\\.[A-Za-z0-9]+){0,3}\\." + "(?:"+commonTLDs+"|"+ccTLDs+")"+"(?:\\."+ccTLDs+")?(?=\\W|$)";
    static String urlBody    = "(?:[^\\.\\s<>][^\\s<>]*?)?";
    static String urlExtraCrapBeforeEnd = "(?:"+punctChars+"|"+entity+")+?";
    static String urlEnd     = "(?:\\.\\.+|[<>]|\\s|$)";
    public static String url  = "(?:"+urlStart1+"|"+urlStart2+")"+urlBody+"(?=(?:"+urlExtraCrapBeforeEnd+")?"+urlEnd+")";
    
    static Pattern chpattern = Pattern.compile("[\u4e00-\u9fa5]");
    
    static String singlechar = "[abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ∩\"☆]";
    
    static String allchinese = "^[\u4e00-\u9fa5・-]+$";
    
	static {
		String line = null;
		BufferedReader SogouLab_dict = null;
//		BufferedReader weibo_emotion_dict = null;
		BufferedReader financial_dict = null;
		BufferedReader bbs_emotion_dict = null;
		BufferedReader gainiangu_discription_dict = null;
		try {
			SogouLab_dict = Files.newReader(new File("./library/SogouLabDic.dic"), Charsets.UTF_8);
			
//			weibo_emotion_dict = Files.newReader(new File("./library/emotion_weibo.dic"), Charsets.UTF_8);
			
			financial_dict = Files.newReader(new File("./library/finance_domain_eastmoney_bbs.dic"), Charsets.UTF_8);
			
			bbs_emotion_dict = Files.newReader(new File("./library/emotion_eastmoney_bbs.dic"), Charsets.UTF_8);
			
			gainiangu_discription_dict = Files.newReader(new File("./library/gainiangu_discription.dic"), Charsets.UTF_8);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			while((line = SogouLab_dict.readLine()) != null){
				String[] token = line.split("\t");
				UserDefineLibrary.insertWord(token[0], "userDefine", Integer.parseInt(token[1]));
			}
//			while((line = weibo_emotion_dict.readLine()) != null){
//				String[] token = line.split("\t");
//				UserDefineLibrary.insertWord(token[1], "userDefine", Integer.parseInt(token[2]));
//			}
			while((line = financial_dict.readLine()) != null){
				String[] token = line.split("\t");
				UserDefineLibrary.insertWord(token[1], "userDefine", Integer.parseInt(token[2]));
			}
			while((line = bbs_emotion_dict.readLine()) != null){
				String[] token = line.split("\t");
				UserDefineLibrary.insertWord(token[1], "userDefine", Integer.parseInt(token[2]));
			}
			while((line = gainiangu_discription_dict.readLine()) != null){
				String[] token = line.split("\t");
				UserDefineLibrary.insertWord(token[1], "userDefine", Integer.parseInt(token[2]));
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
//			weibo_emotion_dict.close();
			financial_dict.close();
			bbs_emotion_dict.close();
			gainiangu_discription_dict.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 去除转发微博中的 // 和 url
	
	public static String removeRetweet(String weibo) throws IOException {
		
		weibo = weibo.replaceAll(url, "").replaceAll("#","").trim();
		
		String []weiboList = weibo.toLowerCase().split("//");
	
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < weiboList.length; ++i) {
			weiboList[i] = weiboList[i].trim();
			if (!weiboList[i].isEmpty()) {
				sb.append(weiboList[i]);
				
				if(weiboList[i].endsWith("http:")){
					sb.append("//");
				}else{
					sb.append(" ");
				}
			}
		}
		return sb.toString().trim();
	}
	
	public static String removeAT(String weibo) {
		
		return weibo.replaceAll(atPattern, "").replaceAll(at_endPattern, "")
				.replaceAll(url, "").replaceAll("#","").trim();
	}
	

	public static String clean_weibo(String weibo) throws Exception {
		
		String jweibo = JianFan.f2J(weibo);
		//jweibo = PRE_TOOL.removeAT(jweibo);
		jweibo = removeRetweet(jweibo); 
		jweibo = jweibo.replaceAll(zfpattern, "");
		return jweibo.trim();
	}
	
	public static String clean_news (String news) throws Exception {
		//简繁体相互转换
		String jweibo = JianFan.f2J(news);
		return jweibo.trim();
	}
	
	public static String clean_bbs (String bbs) throws Exception {
		//半角字符->全角字符转换  
//		String banjiao_bbs = BCConvert.qj2bj(bbs);
		//简繁体相互转换
		String eastmoney_bbs = JianFan.f2J(bbs);
		return eastmoney_bbs.trim();
	}
	
	public static String clean(String keyword) {

		String re_kw = StringEscapeUtils.unescapeHtml(keyword);
		
		re_kw = re_kw.replaceAll("\\<[^>]*>","").trim();
		re_kw = re_kw.replaceAll("\\p{C}", "\\$");
		re_kw = re_kw.replaceAll("[`★~!$^&*()=|{}':;'\\[\\].<>/ˉ﹃ ≥≤﹏?~！￥……&*（）——|{}【】‘；：”“'。，、？]⊙ o ⊙／¯ ¯°", "");
		re_kw = re_kw.replaceAll("\\p{C}", "?").replaceAll("\\?", "").trim();
		re_kw = re_kw.replaceAll("[ ]{1,}", "").replaceAll("[,]{1,}", "").trim();
		
		return re_kw;
	}
	
	// 判断分词后的字符串是否全为数字，如果是则返回空字符串
	public static String clean_digit(String keyword) {
		
		String re_kw = StringEscapeUtils.unescapeHtml(keyword);
		
		Pattern pattern_haveDigt = Pattern.compile(".*\\d+.*"); 
		
		String[] special_chinese_symbol_del = {".", "%","℃", "——","个","成","根",  "两","双","开","下","道","几","岁","瓶", "本","斤", "线", "条", "笔", "盘","秒", "局", "米","千", "票", "第","派", "度", "块","位", "名", "只","支","万","手","张","发","点","亿","角","股","家","种","倍","次","左右","号","元","年","月","日","时","分","周","天","一","多"};
		String[] special_chinese_retain = { "等", "出","封"};
		
	    if (pattern_haveDigt.matcher(re_kw).matches()){
//	    	System.out.println("有数字！");
	    	
	    	boolean  is_not_realNumber = re_kw.matches("[0-9]{1,}");
	    	boolean  is_not_chinese_symbol_del = false;
	    	boolean  is_not_chinese_retain = false;
	    	
	    	if(!is_not_realNumber){
		    	for(String word : special_chinese_symbol_del) {
	                if(re_kw.contains(word)) {
	                	is_not_chinese_symbol_del = true;
	               	 	continue;
	                }
		    	}
	    	}
	           
		    if(!is_not_chinese_symbol_del){
		    	for(String word : special_chinese_retain) {
	                if(re_kw.contains(word)){ 
	                	is_not_chinese_retain = true;
	                	continue;
	                }
	            }
		    }
		    
		    if(is_not_realNumber || is_not_chinese_symbol_del ){
		    	re_kw = "";
//		    	System.out.println("需要删除的");
		    }else if(is_not_chinese_retain){
		    	re_kw = re_kw.replaceAll("\\d+","").trim();
//		    	System.out.println(re_kw);
		    }
	    	    	
	    }else{
	    	return re_kw;
	    }
		return re_kw;   
	}
	
	//判断一个字符串是否有中文
	 private static boolean checkStringContainChinese(String checkStr){
	        if(!Strings.isNullOrEmpty(checkStr)){
	            char[] checkChars = checkStr.toCharArray();
	            for(int i = 0; i < checkChars.length; i++){
	                char checkChar = checkChars[i];
	                if(checkCharContainChinese(checkChar)){
	                    return true;
	                }
	            }
	        }
	        return false;
	    }

    private static boolean checkCharContainChinese(char checkChar){
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(checkChar);
        if(CJK_UNIFIED_IDEOGRAPHS == ub || CJK_COMPATIBILITY_IDEOGRAPHS == ub || CJK_COMPATIBILITY_FORMS == ub ||
                CJK_RADICALS_SUPPLEMENT == ub || CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A == ub || CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B == ub){
            return true;
        }
        return false;
    }
	
	public static String clean_punc(String weibo) {
		
		weibo = BCConvert.qj2bj(weibo);
		
		String re_kw = weibo.replaceAll("[!]{1,}", "!");
		re_kw = re_kw.replaceAll("[。]{1,}", "。");
		re_kw = re_kw.replaceAll("[！]{1,}", "！");
		re_kw = re_kw.replaceAll("[.]{1,}", ".");
		re_kw = re_kw.replaceAll("[…]{1,}", "…");
		re_kw = re_kw.replaceAll("[~]{1,}", "~");
		re_kw = re_kw.replaceAll("[: ]{1,}", ": ");
		re_kw = re_kw.replaceAll("[·]{1,}", "·");
		return re_kw.trim();
	}
	
	
public static String clean_punc_all(String weibo) {
		
		weibo = BCConvert.qj2bj(weibo);
		
		String re_kw = weibo.replaceAll("[!]{1,}", "");
		re_kw = re_kw.replaceAll("[。]{1,}", "");
		re_kw = re_kw.replaceAll("[！]{1,}", "");
		re_kw = re_kw.replaceAll("[.]{1,}", "");
		re_kw = re_kw.replaceAll("[…]{1,}", "");
		re_kw = re_kw.replaceAll("[~]{1,}", "");
		re_kw = re_kw.replaceAll("[: ]{1,}", " ");
		re_kw = re_kw.replaceAll("[·]{1,}", "");
//		re_kw = re_kw.replaceAll("\\d+","");//清楚数字字符串
		return re_kw.trim();
	}
	
	// clean 单个词中一些标点符号，异常符号，不可见字符
	public static String clean_word(String word) {
		
		String jword = clean(word);
		jword = clean_punc_all(jword).replaceAll( "\\p{Punct}", "" );
		jword = CharMatcher.INVISIBLE.removeFrom(word);//匹配所有看不见的字符
		jword = clean_digit(jword);
		return jword.trim();
	}
	
	// 对text进行分词，分词结果用空格连接 ,LearnTool learnTool
	public static String seg_text(String text) throws Exception {
		//,learnTool
		List<Term> parse = NlpAnalysis.parse(text);
		
		StringBuffer sb = new StringBuffer();
		
		for(Term t : parse){
			String clean_t = clean_word(t.getName());
			if(!Stopwords.isStopword(clean_t) && !clean_t.equals("")){
				sb.append(clean_t).append(" ");
			}
		}
		return sb.toString().trim();
	}
	
}
