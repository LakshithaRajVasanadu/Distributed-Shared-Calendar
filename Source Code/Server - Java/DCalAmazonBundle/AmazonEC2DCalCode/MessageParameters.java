import java.io.Serializable;

public class MessageParameters implements Serializable {
	int id;
	String msg;
	
	public MessageParameters(int id, String msg) {
		this.id = id;
		this.msg = msg;
	}
	
}
