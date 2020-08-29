package entity;

/**
 * 
 * 对应MicroBlog表，表示上下文微博
 */
import java.sql.Date;

public class MicroBlog {
	public int id;
	public String userName;
	public Date createTime;
	public int isTrain;
	public String text;
	public String oldSType;
	public String newStype;
	public String words;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getIsTrain() {
		return isTrain;
	}
	public void setIsTrain(int isTrain) {
		this.isTrain = isTrain;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getOldSType() {
		return oldSType;
	}
	public void setOldSType(String oldSType) {
		this.oldSType = oldSType;
	}
	public String getNewStype() {
		return newStype;
	}
	public void setNewStype(String newStype) {
		this.newStype = newStype;
	}
	public String getWords() {
		return words;
	}
	public void setWords(String words) {
		this.words = words;
	}
	
}
