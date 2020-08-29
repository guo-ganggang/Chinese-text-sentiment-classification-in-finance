package service.ICTCLASAnalysis;

import ICTCLAS.I3S.AC.*;

public class ICTCLASAnalyzer {
		
//	public static void main(String[] args)
//    {
//        try
//        {
//            //�ַ����ִ�
//            String sInput = "�����������뿪�������سǣ�Ԥ���������������ͻص����������Ͼ��ǽ�����������¶�̬";
//            ICTCLAS_ParaProcess(sInput);//ͬtestimportuserdict��testSetPOSmap
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
            //��ʼ��
            if (testICTCLAS50.ICTCLAS_Init(argu.getBytes("GB2312")) == false)
            {
                System.out.println("Init Fail!");
                return null;
            }
 
            //���ô��Ա�ע��(0 ������������ע����1 ������һ����ע����2 ���������ע����3 ����һ����ע��)
            testICTCLAS50.ICTCLAS_SetPOSmap(2);
 
            //�����û��ʵ�ǰ�ִ�,sInputΪ��Ҫ�ִʵ��ı�
            byte nativeBytes[] = testICTCLAS50.ICTCLAS_ParagraphProcess(sInput.getBytes("GB2312"), 0, 1);//�ִʴ���
            //System.out.println(nativeBytes.length);
            String nativeStr = new String(nativeBytes, 0, nativeBytes.length, "GB2312");
            //System.out.println("δ�����û��ʵ�ķִʽ���� " + nativeStr);//��ӡ���
 
            //�����û��ֵ�
            int nCount = 0;
            String usrdir = "userdict.txt"; //�û��ֵ�·��
            byte[] usrdirb = usrdir.getBytes();//��stringת��Ϊbyte����
            //�����û��ֵ�,���ص����û����������һ������Ϊ�û��ֵ�·�����ڶ�������Ϊ�û��ֵ�ı�������
            nCount = testICTCLAS50.ICTCLAS_ImportUserDictFile(usrdirb, 0);
            //System.out.println("�����û��ʸ���" + nCount);
            nCount = 0;
 
            //�����û��ֵ���ٷִ�
            byte nativeBytes1[] = testICTCLAS50.ICTCLAS_ParagraphProcess(sInput.getBytes("GB2312"), 2, 0);
            //System.out.println(nativeBytes1.length);
            String nativeStr1 = new String(nativeBytes1, 0, nativeBytes1.length, "GB2312");
           // System.out.println("�����û��ʵ��ķִʽ���� " + nativeStr1);
            //�����û��ֵ�
            testICTCLAS50.ICTCLAS_SaveTheUsrDic();
            //�ͷŷִ������Դ
            testICTCLAS50.ICTCLAS_Exit();
            return nativeStr1;
        }
        catch (Exception ex)
        {	
        	return null;
        }
    
    }
}
