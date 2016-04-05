import java.util.HashMap;


public class DatabaseObject {
	
	int logSlot;
	Calendar cal;
	
	public DatabaseObject() {
		
	}
	public DatabaseObject(int logSlot, Calendar cal) {
		super();
		this.logSlot = logSlot;
		this.cal = cal;
	}
	public int getLogSlot() {
		return logSlot;
	}
	public void setLogSlot(int logSlot) {
		this.logSlot = logSlot;
	}
	public Calendar getCal() {
		return cal;
	}
	public void setCal(Calendar cal) {
		this.cal = cal;
	}
	
}
