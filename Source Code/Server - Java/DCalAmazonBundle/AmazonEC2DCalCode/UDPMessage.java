import java.io.Serializable;
import java.util.HashMap;


public class UDPMessage implements Serializable {
	String msgType;
	int m = -1;
	Calendar calendar;
	int logSlot;
	int senderID;
	int acceptedNum = -1;
	Calendar acceptedVal;
	
	public UDPMessage(String msgType, int m, Calendar calendar, int logSlot, int senderID) {
		this.msgType = msgType;
		this.m = m;
		this.calendar = calendar;
		this.logSlot = logSlot;
		this.senderID = senderID;
		this.acceptedVal = new Calendar();
	}
	
	public UDPMessage(String msgType, Calendar acceptedVal, int acceptedNum, int logSlot, int senderID) {
		this.msgType = msgType;
		this.acceptedNum = acceptedNum;
		this.acceptedVal = acceptedVal;
		this.logSlot = logSlot;
		this.senderID = senderID;
		this.calendar = new Calendar();
	}
	
	public void print() {
//		System.out.println("Message Type: "+msgType+" M: "+m+" logSlot: "+logSlot+
//				" SenderID: "+senderID+" AcceptedNum: "+acceptedNum+" Calendar Object: "+calendar+" AcceptedVal: "+ acceptedVal);
//	
	}
	
//	public void printNew() {
//		System.out.println("Message Type: "+msgType+" M: "+m+" logSlot: "+logSlot+
//				" SenderID: "+senderID+" AcceptedNum: "+acceptedNum+" Calendar Object: "+calendar+" AcceptedVal: "+ acceptedVal);
//	
//	}
	
	

}
