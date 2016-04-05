import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

public class BullyAlgorithm implements Runnable{
	
	Node n;
	int timeout; 
	ServerSocket serverSoc;  
	int prevLeaderId = -1;
	
	public BullyAlgorithm(Node n, int timeout, ServerSocket serverSoc) {
		

		this.n = n;

		this.timeout = timeout;
		this.serverSoc = serverSoc;
	}
	
	@Override
	public void run() {
		new Thread(new SendMessageThread(this.n, MessageType.ELECTION)).start();
		boolean receivedOkay = false;
		MessageParameters messageObj = null;
		while(true) {
			try {
				serverSoc.setSoTimeout(timeout);
				Socket receiverSoc = serverSoc.accept();
				
				InputStream is = receiverSoc.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				 messageObj = (MessageParameters) ois.readObject();
				if(messageObj != null) {
					
					if((messageObj.msg).equals("COORDINATOR")) {
						if(messageObj.id > n.id) {
                                                n.leaderId = messageObj.id;
                                                break;
						}
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

				if (!receivedOkay){
				
					if((n.id < n.leaderId)) {
					
						System.out.println("[BullyAlgorithmThread] Pinging leader to check status");
						if(isNodeReachable(n.leaderId)) {

						} else {

							System.out.println("[BullyAlgorithmThread] Leader is dead. I am the coordinator");
							new Thread(new SendMessageThread(this.n, MessageType.COORDINATOR)).start();
							n.leaderId = n.id;
							break;
						}
												
					} 
					else {
						System.out.println("[BullyAlgorithmThread] I am the higher than the current leader. I am the coordinator");
						new Thread(new SendMessageThread(this.n, MessageType.COORDINATOR)).start();
						n.leaderId = n.id;
						break;
					}
				}
			} catch (IOException e) {
				
				
			} catch (ClassNotFoundException e) {
				
			}
		}
		
	}
	
	
	public boolean isNodeReachable(int nodeId) {
		InetSocketAddress address;

			address = new InetSocketAddress(getNode(nodeId).ipAddress, getNode(nodeId).port);
			
		//	System.out.println("Checking if node id is reachable: " + nodeId);
			boolean isReachable = sendPingMessageOnSocket(getNode(nodeId).port, getNode(nodeId).ipAddress, "ping");
			
			
		
	       
	     return isReachable;
	}

	
	public Node getNode(int nodeId) {
		Node node = null;
		if(nodeId == n.id)
			node = n;
		else {
			for(Map.Entry<Integer, Node> entry: n.peers.entrySet()) {
				if(nodeId == entry.getKey()) {
					node = entry.getValue();
					break;
				}
			}
		}
		
		return node;
	}
	
	public boolean sendPingMessageOnSocket(int port, String ipAddress, String msg) {
		boolean isPingable = true;
		Socket senderSoc;
		try {
			//logger.info("Sending message to Node on port " + port);
			InetAddress add = InetAddress.getByName(ipAddress);
			
			senderSoc = new Socket(add, port);
			// logger.info("Sender socket..");
			if(senderSoc != null) {
			// logger.info("SenderSocket not null");
			OutputStream os = senderSoc.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			MessageParameters messageObject = new MessageParameters(n.id, "ping");
			oos.writeObject(messageObject);
			oos.flush();
			// logger.info("oos flushed");
			senderSoc.close();
			 //logger.info("Sender socket closed..");
			}
			else {
				//logger.info("Sendersoc is null...");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("Port " + port + " is unreachable..");
			isPingable = false;
		}
		catch(Exception e) {
		//	System.out.println("Port " + port + " is unreachable..");
			isPingable = false;
		}
		
		return isPingable;
	}
}
