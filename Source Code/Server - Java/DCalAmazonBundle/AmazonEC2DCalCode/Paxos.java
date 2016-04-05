import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class Paxos implements Runnable {

	Node node;

	public Paxos(Node node) {
		
		this.node = node;
	}

	@Override
	public void run() {
		System.out.println("[PaxosThread] Starting Proposer Thread");
		new Thread(node.proposer).start();
		
		System.out.println("[PaxosThread] Starting Acceptor Thread");
		new Thread(node.acceptor).start();
		
		System.out.println("[PaxosThread] Starting Learner Thread");
		new Thread(new Learner(node)).start();
		
		System.out.println("[PaxosThread] Starting Shutdown Thread");
		new Thread(new Shutdown(node)).start();
		//start learner and shutdown
		try {
			DatagramSocket serverSocket = new DatagramSocket(null);
			serverSocket.setReuseAddress(true);
			serverSocket.setBroadcast(true);
			InetSocketAddress address = new InetSocketAddress(node.udpPort);
			serverSocket.bind(address);
			byte[] receiveData;

			while(true) {
				receiveData = new byte[4096];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				System.out.println("[PaxosThread] Waiting for Datagram Packet to arrive on port.." + node.udpPort);

				serverSocket.receive(receivePacket);
				byte[] data = receivePacket.getData();
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(bais);
				UDPMessage msgObj = (UDPMessage)ois.readObject();
				msgObj.print();
				if(msgObj != null) {
					if(msgObj.msgType.equals("terminate")) {
						serverSocket.close();
						break;
					}
					
					// Coded by me
					UDPMessage newMessage = new UDPMessage(new String(msgObj.msgType), 
							msgObj.m, new Calendar(msgObj.calendar), msgObj.logSlot, msgObj.senderID);
					
					if(msgObj.msgType.equals("promise") || msgObj.msgType.equals("ack")) {
						newMessage = new UDPMessage(new String(msgObj.msgType), new Calendar(msgObj.acceptedVal),
								msgObj.acceptedNum, msgObj.logSlot, msgObj.senderID); 
	
					}
					parseUDPMessage(newMessage);
				}

			}
		} catch (IOException e) {
			
			
		} catch (ClassNotFoundException e) {
			
			
		}

	}

	public void parseUDPMessage(UDPMessage msg) {
		List<String> validMsgTypes = new ArrayList<String>();
		validMsgTypes.add("propose");
		validMsgTypes.add("prepare");
		validMsgTypes.add("promise");
		validMsgTypes.add("accept");
		validMsgTypes.add("ack");
		validMsgTypes.add("commit");
		if(!validMsgTypes.contains(msg.msgType)) {
			return;
		}

		if(msg.msgType.equals("propose")) {
			int proposedSlot = msg.logSlot;
			for(int i = 0; i < proposedSlot; i++) {
				if(!node.log.keySet().contains(i)) {
					Calendar slotCalendar = node.acceptor.acceptedValues.get(i);
					node.log.put(i, slotCalendar);
				}
			}
			node.proposer.commandQueue.add(msg);
			return;
		}
		else if(msg.msgType.equals("prepare")) {
			node.acceptor.commandQueue.add(msg);
			return;
		}
		else if(msg.msgType.equals("promise")) {
			node.proposer.commandQueue.add(msg);
			return;
		}
		else if(msg.msgType.equals("accept")) {
			node.acceptor.commandQueue.add(msg);
			return;
		}
		else if(msg.msgType.equals("ack")) {
			node.proposer.commandQueue.add(msg);
			return;
		}
		else if(msg.msgType.equals("commit")) {
			node.acceptor.commandQueue.add(msg);
			return;
		}

		return;
	}
	
}
