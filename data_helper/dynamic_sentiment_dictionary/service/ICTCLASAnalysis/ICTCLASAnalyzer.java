package service.ICTCLASAnalysis;

import ICTCLAS.I3S.AC.*;

public class ICTCLASAnalyzer {
		
//	public static void main(String[] args)
//    {
//        try
//        {
//            //字符串分词
//            String sInput = "随后温总理就离开了舟曲县城，预计温总理今天下午就回到北京。以上就是今天上午的最新动态";
//            ICTCLAS_ParaProcess(sInput);//同testimportuserdict和testSetPOSmap
//        }
//        catch (Exception ex)
//        {
//        }
//    }
 
    public static String ICTCLAS_ParaProcess(String sInput)
    {
        try
        {
            ICTCLAS50 testICTCLAS50 = new ICTCLAS50();
            String argu = ".";
            //初始化
            if (testICTCLAS50.ICTCLAS_Init(argu.getBytes("GB2312")) == false)
            {
                System.out.println("Init Fail!");
                return null;
            }
 
            //设置词性标注集(0 计算所二级标注集，1 计算所一级标注集，2 北大二级标注集，3 北大一级标注集)
            testICTCLAS50.ICTCLAS_SetPOSmap(2);
 
            //导入用户词典前分词,sInput为需要分词的文本
            byte nativeBytes[] = testICTCLAS50.ICTCLAS_ParagraphProcess(sInput.getBytes("GB2312"), 0, 1);//分词处理
            //System.out.println(nativeBytes.length);
            String nativeStr = new String(nativeBytes, 0, nativeBytes.length, "GB2312");
            //System.out.println("未导入用户词典的分词结果： " + nativeStr);//打印结果
 
            //导入用户字典
            int nCount = 0;
            String usrdir = "userdict.txt"; //用户字典路径
            byte[] usrdirb = usrdir.getBytes();//将string转化为byte类型
            //导入用户字典,返回导入用户词语个数第一个参数为用户字典路径，第二个参数为用户字典的编码类型
            nCount = testICTCLAS50.ICTCLAS_ImportUserDictFile(usrdirb, 0);
            //System.out.println("导入用户词个数" + nCount);
            nCount = 0;
 
            //导入用户字典后再分词
            byte nativeBytes1[] = testICTCLAS50.ICTCLAS_ParagraphProcess(sInput.getBytes("GB2312"), 2, 0);
            //System.out.println(nativeBytes1.length);
            String nativeStr1 = new String(nativeBytes1, 0, nativeBytes1.length, "GB2312");
           // System.out.println("导入用户词典后的分词结果： " + nativeStr1);
            //保存用户字典
            testICTCLAS50.ICTCLAS_SaveTheUsrDic();
            //释放分词组件资源
            testICTCLAS50.ICTCLAS_Exit();
            return nativeStr1;
        }
        catch (Exception ex)
        {	
        	return null;
        }
    
    }
}
