package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import entity.DecimalCalculate;
import entity.MicroBlog;

public class MicroBlogDAO {
	int N=100;//΢�����ݼ���ģ
	static Connection conn = CommonDAO.getConnection();
	/**
	 * �����ݿ��в�ѯ�������c��ѵ����Ԫ��
	 * @param c �������
	 * @return �������c��ѵ����Ԫ��
	 */
	public ArrayList<String> getTrainTextsOfMicro(String c){
		try{
			ArrayList<String> list = new ArrayList<String>();//����������c��ѵ�����Ĵ��������
		
			//��ȡ������ִʺ�Ĵ�����
			String s = "select words from MicroBlog where isTrain = 1 and newSType = ?";
			PreparedStatement ps = conn.prepareStatement(s);
			ps.setString(1, c);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String str = rs.getString("words").toString();
				list.add(str);
			}
			return list;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		
	}
	/**
	 * �����ݿ��в�ѯ����ѵ����Ԫ��
	 * @return ѵ����Ԫ��
	 */
	public Map<Integer,String> getAllTrainText(){
		try{
			Map<Integer,String> map = new HashMap<Integer,String>();//���ѵ�����Ĵ�����
		
			//��ȡ������ִʺ�Ĵ�����
			String s = "select id,words from MicroBlog where isTrain = 1";
			
			//String s = "select id,words from MicroBlog"; //���Դ����޸�1
			
			PreparedStatement ps = conn.prepareStatement(s);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				int id = rs.getInt("id");
				String words = rs.getString("words").toString();
				map.put(id,words);
			}
			return map;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		
	}
	/**
	 * ����ĳ��΢����������𣬲����Ϊѵ����
	 * @param key ΢��id
	 * @param mclass ΢���������
	 * @return ִ��״̬
	 */
	public boolean updateClassesOfMicro(int id,String mclass){
		try{
			String sql = "update MicroBlog set newSType = ? where id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, mclass);//1Ϊռλ���������һ���ʺ�
			ps.setInt(2, id);//2Ϊռλ��������ڶ����ʺ�
			ps.executeUpdate();
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	//���Դ������4
	public boolean updateTrainClassesOfMicro(int id,String mclass){
		try{
			String sql = "update MicroBlog set oldSType = ? where id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, mclass);//1Ϊռλ���������һ���ʺ�
			ps.setInt(2, id);//2Ϊռλ��������ڶ����ʺ�
			ps.executeUpdate();
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * ����ĳ��΢����������𣬲����Ϊѵ����
	 * @param key ΢��id
	 * @param mclass ΢���������
	 * @return ִ��״̬
	 */
	public boolean updateStatusOfMicro(int id,String mclass){
		try{
			String sql = "update MicroBlog set isTrain =1,newSType = ? where id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, mclass);
			ps.setInt(2, id);
			ps.executeUpdate();
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	/**
	 * ����ĳ��΢���ķִʽ��
	 * @param key ΢��id
	 * @param mclass ΢���������
	 * @return ִ��״̬
	 */
	public boolean updateWordsOfMicro(int id,String words){
		try{
			String sql = "update MicroBlog set words = ? where id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, words);
			ps.setInt(2, id);
			ps.executeUpdate();
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	/**
	 * ����ӿڣ������ݿ��MicroBlog�в�������
	 * @param list ����MicroBlog���������
	 */
	public void insertNewMicros(ArrayList<MicroBlog> list,int thoundtimes){
		try{
			if(conn==null)
				conn = CommonDAO.getConnection();
			if(list!=null){
				//��Ҷ˹����������������
				//String sql = "insert into MicroBlog(userName,createTime,text,words,isTrain) values(?,?,?,?,?)";// ,oldSType,newStype  ,?,? 
				//�ʵ䷽��������������
				String sql = "insert into MicroBlog(userName,createTime,text,words) values(?,?,?,?)";
				//System.out.println("�����ݿ��MicroBlog�в�����������:"+list.size());
				for (int i = 0; i < list.size(); i++) {
					MicroBlog micro = (MicroBlog)list.get(i);
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.setString(1, micro.getUserName());
					ps.setDate(2, micro.getCreateTime());
					ps.setString(3, micro.getText());
					ps.setString(4, micro.getWords());
					//��Ҷ˹��������ѵ����,ѵ����Ϊ1�����Լ�Ϊ0
//					if(thoundtimes<7){
//						ps.setInt(5,1);//micro.getIsTrain()
//					}else{
//						ps.setInt(5,0);
//					}
					//ps.setString(6,null);//micro.getOldSType()
					//ps.setString(7,null);//micro.getNewStype()
						
					ps.executeUpdate();
					
				}
			}
			System.out.println("Done!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * ����ӿڣ������ݿ��MicroBlog�л�ȡ���зִʺ��΢������
	 * @param list ����΢���ִʽ��������
	 */
	public Map<Integer,String> getAllMicroWords(){
		try{
			if(conn==null)
				conn = CommonDAO.getConnection();
			Map<Integer,String> map = new HashMap<Integer,String>();
			//��ȡ������ִʺ�Ĵ�����
			//String sql = "select top "+N+" id,words from MicroBlog where isTrain=0 order by id ";
			//��Ҷ˹����
			String sql = "select  id,words from MicroBlog where id in(select  id from MicroBlog where isTrain=0 and newSType is null) ";
			//�ʵ䷽��
			//String sql = "select  id,words from MicroBlog";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				int id = rs.getInt("id");
				String words = rs.getString("words").toString();
				map.put(id,words);
			}
			return map;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	/**
	 * 
	 * ��ȡ����΢�����ı�����
	 * @return
	 */
	public Map<Integer,String> getAllMicroText(){
		try{
			if(conn==null)
				conn = CommonDAO.getConnection();
			Map<Integer,String> map = new HashMap<Integer,String>();
			//��ȡ������ִʺ�Ĵ�����
			//String sql = "select top 3000 id,text from MicroBlog where id >0 order by id";//50710
			//�����޸Ĵ���5
			String sql = "select id,text from MicroBlog where id >0 order by id";//50710
			System.out.println(sql);
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				int id = rs.getInt("id");
				String text = rs.getString("text").toString();
				map.put(id,text);
			}
			return map;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	/**
	 * ����׼ȷ��
	 * @return
	 */
	public Map<String, Double> calPrecision() {
		Map<String,Double> result  = new HashMap<String, Double>();
		String [] classes = {"angry","contempt","sad","joyful","neutral"};
		try{
//			String sql1 = "select count(A.id) from (select top  "+N+"  * from MicroBlog where isTrain=0 order by id) A where A.newSType =A.oldSType and A.newSType= ?";
//			String sql2 = "select count(B.id) from (select top  "+N+"  * from MicroBlog where isTrain=0 order by id) B where B.newSType = ?";
			String sql1 = "select count(A.id) from (select * from MicroBlog where oldSType!='neutral' and oldSType is not null) A where A.newSType =A.oldSType and A.newSType= ?";//isTrain=0 and 
			String sql2 = "select count(B.id) from (select * from MicroBlog where oldSType!='neutral' and oldSType is not null) B where B.newSType = ?";//isTrain=0 and 
			
			PreparedStatement ps =null;
			ResultSet rs =null;
			for (int i = 0; i < classes.length; i++) {
				double precision = 0.00;
				String s = classes[i];
				ps = conn.prepareStatement(sql1);
				ps.setString(1, classes[i]);
				rs = ps.executeQuery();
				while(rs.next()){
					precision = rs.getInt(1);
				}
				if(precision!=0.00){
					ps = conn.prepareStatement(sql2);
					ps.setString(1, classes[i]);
					rs = ps.executeQuery();
					while(rs.next()){
						int num = rs.getInt(1);
						precision = DecimalCalculate.div(precision, num, 4)*100;
					}
				}
				result.put(classes[i], precision);
			}
			
			return result;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	/**
	 * �����ٻ���
	 * @return
	 */
	public Map<String, Double> calRecall() {
		Map<String,Double> result  = new HashMap<String, Double>();
		String [] classes = {"angry","contempt","sad","joyful","neutral"};
		try{//12551
//			String sql1 = "select count(A.id) from (select top  "+N+"  * from MicroBlog where isTrain=0 order by id) A where A.newSType =A.oldSType and A.newSType= ?";
//			String sql2 = "select count(B.id) from (select top  "+N+"  * from MicroBlog where isTrain=0 order by id) B where B.oldSType = ?";
			String sql1 = "select count(A.id) from (select * from MicroBlog where oldSType!='neutral' and oldSType is not null) A where A.newSType =A.oldSType and A.newSType= ?";//isTrain=0 and 
			String sql2 = "select count(B.id) from (select * from MicroBlog where oldSType!='neutral' and oldSType is not null) B where B.oldSType = ?";//isTrain=0 and 
			
			PreparedStatement ps =null;
			ResultSet rs =null;
			for (int i = 0; i < classes.length; i++) {
				double precision = 0.00;
				String s = classes[i];
				ps = conn.prepareStatement(sql1);
				ps.setString(1, classes[i]);
				rs = ps.executeQuery();
				while(rs.next()){
					precision = rs.getInt(1);
				}
				if(precision!=0.00){
					ps = conn.prepareStatement(sql2);
					ps.setString(1, classes[i]);
					rs = ps.executeQuery();
					while(rs.next()){
						int num = rs.getInt(1);
						precision = DecimalCalculate.div(precision, num, 4)*100;
					}
				}
				result.put(classes[i], precision);
			}
			
			return result;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
}
