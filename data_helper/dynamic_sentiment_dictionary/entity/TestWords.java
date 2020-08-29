package entity;
/**
 * 对应数据库中TestWords表，表示待测词集
 * @author GGG
 *
 */
public class TestWords {
	public int id;
	public String name;
	public String oldSType;
	public String newSType;
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
	public String getOldSType() {
		return oldSType;
	}
	public void setOldSType(String oldSType) {
		this.oldSType = oldSType;
	}
	public String getNewSType() {
		return newSType;
	}
	public void setNewSType(String newSType) {
		this.newSType = newSType;
	}
	
	
}
