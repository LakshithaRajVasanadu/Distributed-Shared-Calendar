import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Proposer implements Runnable {

	Node node;
	int uid;
	int currentProposalNumber;
	List<UDPMessage> commandQueue;
	List<Integer> committedSlots;
	HashMap<Integer, ProposerProposal> myProposals;
	HashMap<Integer, LinkedHashMap<Integer, QueueObject>> promiseQueues;
	HashMap<Integer, LinkedHashMap<Integer, QueueObject>> ackQueues;
	boolean terminate = false;
	boolean isProposer = true;

	public Proposer(Node node) {
		this.node = node;
		this.uid = node.id;
		this.currentProposalNumber = this.uid;
		commandQueue = new ArrayList<UDPMessage>();
		committedSlots = new ArrayList<Integer>();
		myProposals = new HashMap<Integer, ProposerProposal>();
		promiseQueues = new HashMap<Integer, LinkedHashMap<Integer, QueueObject>>();
		ackQueues = new HashMap<Integer, LinkedHashMap<Integer, QueueObject>>();
		
	}



	@Override
	public void run() {
		new Thread(new PromiseListener(this)).start();
		new Thread(new AckListener(this)).start();

		while(true) {
			if(!commandQueue.isEmpty()) {
				UDPMessage message = commandQueue.get(commandQueue.size()-1);
				commandQueue.remove(commandQueue.size()-1);
				message.print();
				if(message.msgType.equals("propose")){
					sendPrepare(message);
				}
				if(message.msgType.equals("promise")) {
					receivePromise(message);
				}
				if(message.msgType.equals("ack")) {
					receiveAck(message);
				}
			}
			if(this.terminate) {
				break;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				
			}
		}
	}


	public void sendPrepare(UDPMessage message) {
		this.currentProposalNumber += 10;
		int m = currentProposalNumber;
		Calendar calendar = message.calendar;
		int logSlot = message.logSlot;
		this.myProposals.put(logSlot, new ProposerProposal(m, calendar));

		System.out.println("[ProposerThread] Working on sendPrepare request for logSlot: " + logSlot);
		UDPMessage msg = new UDPMessage("prepare", m, new Calendar(), logSlot, this.node.id);

		for(Map.Entry<Integer, Node> entries : this.node.peers.entrySet()) {
			int key = entries.getKey();
			Node value = entries.getValue(); 
			System.out.println("Sending msg to " + value.id);
			this.sendUDPMessage(msg, value.udpPort, value.ipAddress);
		}
		
		this.sendUDPMessage(msg, this.node.udpPort, this.node.ipAddress);
	}

	public void receivePromise(UDPMessage message) {
		//System.out.println("In receive Promise");
		System.out.println("[ProposerThread] Working on receivePromise request for logSlot: " + message.logSlot);

		message.print();
		LinkedHashMap<Integer, QueueObject> promiseQueueValue = this.promiseQueues.get(message.logSlot) ; 
		if(promiseQueueValue== null) {
		//	System.out.println("No values in promiseQueue for that slot");
			promiseQueueValue = new LinkedHashMap<Integer, QueueObject>();
			promiseQueueValue.put(message.senderID, new QueueObject(message.acceptedNum, message.acceptedVal));
			this.promiseQueues.put(message.logSlot, promiseQueueValue);
		}
		else {
			promiseQueueValue.put(message.senderID, new QueueObject(message.acceptedNum, message.acceptedVal));
			this.promiseQueues.put(message.logSlot, promiseQueueValue);
		}
	}

	public void receiveAck(UDPMessage message) {
		System.out.println("[ProposerThread] Working on receiveAck request for logSlot: " + message.logSlot);

		LinkedHashMap<Integer, QueueObject> ackQueueValue = this.ackQueues.get(message.logSlot) ; 
		if(ackQueueValue== null) {
			ackQueueValue = new LinkedHashMap<Integer, QueueObject>();
			ackQueueValue.put(message.senderID, new QueueObject(message.acceptedVal));
			this.ackQueues.put(message.logSlot, ackQueueValue);
		}
		else {
			ackQueueValue.put(message.senderID, new QueueObject(message.acceptedVal));
			this.ackQueues.put(message.logSlot, ackQueueValue);
		}
	}

	public void sendAccept(int m, Calendar v, int logSlot){
		System.out.println("[ProposerThread] Working on sendAccept request for logSlot: " + logSlot);

		UDPMessage msg = new UDPMessage("accept", m, v, logSlot, this.node.id);
		msg.print();
		
		for(Map.Entry<Integer, Node> entries : node.peers.entrySet()) {
			int key = entries.getKey();
			Node value = entries.getValue();
			sendUDPMessage(msg, value.udpPort, value.ipAddress);
		}
		
		this.sendUDPMessage(msg, this.node.udpPort, this.node.ipAddress);


	}

	public void sendCommit(Calendar v, int logSlot){
		System.out.println("[ProposerThread] Working on sendCommit request for logSlot: " + logSlot);

		UDPMessage msg = new UDPMessage("commit", -1, v, logSlot, this.node.id);
		for(Map.Entry<Integer, Node> entries : node.peers.entrySet()) {
			int key = entries.getKey();
			Node value = entries.getValue();
			sendUDPMessage(msg, value.udpPort, value.ipAddress);
		}
		
		this.sendUDPMessage(msg, this.node.udpPort, this.node.ipAddress);

	}

	public void sendUDPMessage(UDPMessage msg, int udpPort, String ipAddress) {
		try {
			System.out.println("[ProposerThread] Sending UDP message to IPAddress: " + ipAddress + " , Port: " + udpPort );

			msg.print();
			DatagramSocket socket = new DatagramSocket(null);
			InetSocketAddress addr = new InetSocketAddress(node.udpPort);
			socket.setReuseAddress(true);
			socket.setBroadcast(true);
			socket.bind(addr);
			byte[] buf = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(msg);
			oos.flush();
			byte[] data = baos.toByteArray();
			InetAddress add = InetAddress.getByName(ipAddress);
			DatagramPacket packet = new DatagramPacket(data, data.length, add, udpPort);
			socket.send(packet);
			socket.close();
			
		} catch (SocketException e) {
			
			
		} catch (UnknownHostException e) {
			
			
		} catch (IOException e) {
			
			
		}
	}

}
