package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import entity.DecimalCalculate;

public class TestWordsDAO {
	static Connection conn = CommonDAO.getConnection();
	/**
	 * ����ӿڣ���TestWords���ݿ���в���һ���¼�¼
	 * @param word �²������д�
	 * @param osType ��дʵ��˹���ע���
	 * return  ���ز����״̬
	 */
	public boolean insertNewTestWord(String word,String osType){
		try{
			if(conn==null)
				conn = CommonDAO.getConnection();
			String sql = "insert into TestWords(name,oldSType) values(?,?)";
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, word);
			ps.setString(2, osType);
			ps.executeUpdate();
			
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	/**
	 * ����ӿڣ���ȡ���д����
	 * @param list ������дʵ�����
	 */
	public Map<Integer,String> getAllTestWords(){
		try{
			Map<Integer,String> map = new HashMap<Integer,String>();//��Ŵ����
		
			//��ȡ�����
			String s = "select  id,name from TestWords order by id ";
			PreparedStatement ps = conn.prepareStatement(s);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				int id = rs.getInt("id");
				String word = rs.getString("name").toString();
				map.put(id,word);
			}
			return map;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		
	}
	/**
	 * ���´���ʵ��������
	 * @param key �����id
	 * @param mclass ������������
	 * @return ִ��״̬
	 */
	public boolean updateClassesOfWord(int id,String mclass){
		try{
			String sql = "update TestWords set newSType = ? where id = ?";
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
	 * ����׼ȷ��
	 * @return ������������׼ȷ�ʰٷֱ�
	 */
	public Map<String,Double> calPrecision(){
		Map<String,Double> result  = new HashMap<String, Double>();
		String [] classes = {"angry","contempt","sad","joyful","neutral"};
		try{
			String sql1 = "select count(*) from TestWords where newSType =oldSType and newSType= ?";
			String sql2 = "select count(*) from TestWords where newSType = ?";
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
	 * @return �������������ٻ��ʰٷֱ�
	 */
	public Map<String,Double> calRecall(){
		Map<String,Double> result  = new HashMap<String, Double>();
		String [] classes = {"angry","contempt","sad","joyful","neutral"};
		try{
			String sql1 = "select count(*) from TestWords where newSType =oldSType and newSType= ?";
			String sql2 = "select count(*) from TestWords where oldSType = ?";
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
