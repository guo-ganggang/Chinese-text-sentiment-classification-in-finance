package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SentiWordsDAO {
	static Connection conn = CommonDAO.getConnection();
	/**
	 * 对外接口，根据参数条件获取情感词
	 * @param list 包含情感词的链表
	 */
	public Set<String> getSentiWordsOfClass(String sType,Map<String,Object> params){
		try{
			if(conn==null)
				conn = CommonDAO.getConnection();
			Set<String> list = new HashSet<String>();
			//获取的是其分词后的词序列
			StringBuilder sql = new StringBuilder();
			sql.append("select name from SentiWords where sType='");
			sql.append(sType);
			sql.append("'");
			if(params!=null){
				sql.append(" and ");
				Set<String> keys = params.keySet();
				Iterator it = keys.iterator();
				while(it.hasNext()){
					String key = it.next().toString();
					sql.append(key.toString());
					sql.append(" = ");
					sql.append(params.get(key));
				}
			}
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String name = rs.getString("name").toString();
				list.add(name);
			}
			return list;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	/**
	 * 对外接口，向SentiWords数据库表中插入一条新纪录
	 * @param word 新插入的情感词
	 * @param tclass 情感词所属的情感类别
	 * return  返回插入的状态
	 */
	public boolean insertNewSentiWord(String word,String sType,int isBase,int isNet,int isExNet,int isEmotion){
		try{
			if(conn==null)
				conn = CommonDAO.getConnection();
			String sql = "insert into SentiWords(name,sType,isBase,isNet,isExNet,isEmotion) values(?,?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, word);
			ps.setString(2, sType);
			ps.setInt(3, isBase);
			ps.setInt(4, isNet);
			ps.setInt(5, isExNet);
			ps.setInt(6, isEmotion);
			ps.executeUpdate();
			
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	/**
	 * 根据name查询数据库中是否存在该情感词
	 * @param s
	 * @return
	 */
	public boolean querySentiWordByName(String name) {
		try{
			if(conn==null)
				conn = CommonDAO.getConnection();
			//获取的是其分词后的词序列
			String sql = "select id from SentiWords where name=?";
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if(rs.next() && rs.getInt("id")!=-1){
				return true;
			}else{
				return false;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
}
