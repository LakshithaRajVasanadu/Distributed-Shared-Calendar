
public class QueueObject {
	
	int accNum = -1;
	Calendar accVal;
	
	public QueueObject(int accNum, Calendar accVal) {
		this.accNum = accNum;
		this.accVal = accVal;
	}
	
	public QueueObject(int accNum) {
		this.accNum = accNum;
	}
	
	public QueueObject(Calendar accVal) {
		this.accVal = accVal;
	}

}
