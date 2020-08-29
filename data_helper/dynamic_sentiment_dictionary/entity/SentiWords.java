package entity;
/**
 * 对应数据库中的SentiWords，表示情感词典
 * @author GGG
 *
 */
public class SentiWords {
	public int id;
	public String name;
	public String sType;
	public int isBase;
	public int isNet;
	public int isExNet;
	public int isEmotion;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getsType() {
		return sType;
	}
	public void setsType(String sType) {
		this.sType = sType;
	}
	public int getIsBase() {
		return isBase;
	}
	public void setIsBase(int isBase) {
		this.isBase = isBase;
	}
	public int getIsNet() {
		return isNet;
	}
	public void setIsNet(int isNet) {
		this.isNet = isNet;
	}
	public int getIsExNet() {
		return isExNet;
	}
	public void setIsExNet(int isExNet) {
		this.isExNet = isExNet;
	}
	public int getIsEmotion() {
		return isEmotion;
	}
	public void setIsEmotion(int isEmotion) {
		this.isEmotion = isEmotion;
	}
	
}
