import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class NewBully {
	
	Node n;
	int timeout; 
	ServerSocket serverSoc;  
	//Logger logger;
	
	public NewBully(Node n, int timeout, ServerSocket serverSoc) {
		

		this.n = n;
		//logger = new MyLogger(n).LOGGER;

		this.timeout = timeout;
		this.serverSoc = serverSoc;
	}
	
	
	public void run() {
		//Send message

		//logger.info("Starting Bully Algorithm");
		new Thread(new SendMessageThread(this.n, MessageType.ELECTION)).start();
		boolean receivedOkay = false;
		
		//logger.info("Message sent...waiting to receive object");
		while(true) {
			try {
				serverSoc.setSoTimeout(timeout);
				 //logger.info("Before recSocket accept...");
				Socket receiverSoc = serverSoc.accept();
				 //logger.info("ReceiverSocket accepted...");
				if(receiverSoc != null) {
					//logger.info("ReceiverSocket not null");
				}
				else {
					 //logger.info("ReceiverSocket null");
				}
				InputStream is = receiverSoc.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				MessageParameters messageObj = (MessageParameters) ois.readObject();
				if(messageObj != null) {
					 //logger.info("Message obj not null");
					//logger.info("Message msg: = " + messageObj.msg);
					//logger.info("Message msg id: = " + messageObj.id);
					//logger.info("Message msg nodeId: = " + n.id);
					if((messageObj.msg).equals("COORDINATOR")) {
                                                n.leaderId = messageObj.id;
                                                System.out.println("Coordinator is" + n.leaderId);
                                                System.out.println("Getting out");
                                                break;
                                        }
					if((messageObj.msg).equals("ELECTION")) {
						if(messageObj.id < n.id) {
							new SendMessage(this.n, MessageType.OKAY, messageObj.id);
							new Thread(new SendMessageThread(this.n, MessageType.ELECTION)).start();							
							continue;
						}
					}
					if((messageObj.msg).equals("OKAY")) {
						receivedOkay = true;
						continue;
					}
				}
			} catch (SocketTimeoutException e) {

				//logger.info("Socket timed out...");
				//logger.info("recievedOkay?.." + receivedOkay);
				if (!receivedOkay){
					 //logger.info("I am the coordinator..");
					new Thread(new SendMessageThread(this.n, MessageType.COORDINATOR)).start();
					n.leaderId = n.id;
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	

}
