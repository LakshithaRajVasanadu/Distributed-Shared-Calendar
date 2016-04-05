import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.crypto.Data;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class Node {

	int id;
	boolean isRunning = true;
	int port;
	int udpPort;
	int uiPort;
	String ipAddress;
	int leaderId = -1;
	boolean terminate = false;
	boolean isNode = true;
	Acceptor acceptor;
	Proposer proposer;
	HashMap<Integer, Node> peers = new HashMap<Integer, Node>();
	
	HashMap<Integer, Calendar> log = new HashMap<Integer, Calendar>();
	
	Calendar calendar = null;

	public Node(int id) {
		this.id = id;
		this.calendar = new Calendar();
		this.acceptor = new Acceptor(this);
		this.proposer = new Proposer(this);		
	}

	public void loadIpTable() {
		File dir = new File(".");
		File ipFile;
		try {
			ipFile = new File(dir.getCanonicalPath() + File.separator + "ipPortTable.txt");
			BufferedReader br = new BufferedReader(new FileReader(ipFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				int nodeId = Integer.parseInt(tokens[0]);
				if(this.id == nodeId) {
					port = Integer.parseInt(tokens[2]);
					ipAddress = tokens[1];
					udpPort = Integer.parseInt(tokens[3]);
					uiPort = Integer.parseInt(tokens[4]);
				}
				else {
					Node n = new Node(nodeId);
					n.port = Integer.parseInt(tokens[2]);
					n.ipAddress = tokens[1];
					n.udpPort = Integer.parseInt(tokens[3]);
					n.uiPort = Integer.parseInt(tokens[4]);
					this.peers.put(n.id, n);
				}
			}
			br.close();
		} catch (IOException e) {
		} 
	}
	
	public void terminate() {
		this.terminate = true;
		
		// Send special termination message to itself
		DatagramSocket socket;
		try {
			InetSocketAddress addr = new InetSocketAddress(this.udpPort);
			socket = new DatagramSocket(null);
			socket.setReuseAddress(true);
			socket.setBroadcast(true);
			socket.bind(addr);
			UDPMessage msgObj = new UDPMessage("terminate", -1, null, -1, this.id);
			msgObj.print();
			byte[] buf = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(msgObj);
			oos.flush();
			byte[] data = baos.toByteArray();
			InetAddress add = InetAddress.getByName(this.ipAddress);
			DatagramPacket packet = new DatagramPacket(data, data.length, add, this.udpPort);
			socket.send(packet);
			socket.close();
			
			Thread.sleep(1000);
		} catch (SocketException e) {

		} catch (IOException e) {
			
		} catch (InterruptedException e) {
	
			
		}
		
	}
	
	public void parseCommand(String input) {
		System.out.println("[NODE] Working on client request: " + input);
		String[] tokens = input.split(" ");
		if(tokens[0].equals("schedule")) {
			
			Appointment appointment = parseAppointment(tokens);
			boolean canInsert = this.insertAppointment(appointment);
			if(canInsert == true) {
				System.out.println("[NODE] Appointment " + appointment.name + " is valid. Trying to commit");
			} else {
				System.out.println("[NODE] Appointment " + appointment.name + " is conflicting");
			}
		}
		else if(tokens[0].equals("cancel")) {
			Appointment appointment = parseAppointment(tokens);
			this.deleteAppointment(appointment);
			System.out.println("[NODE] Appointment " + appointment.name + " removed. Trying to commit");
		}
	}
	
	public void deleteAppointment(Appointment appointment) {
		Calendar newCalendar = new Calendar();
		
		for(Appointment app : this.calendar.appointments) {
			if(!(app.equals(appointment))) {
				newCalendar.appointments.add(app);
			}
		}
		
		int nextLogSlot = -1;
		//Logic for log keys
		if(!this.log.isEmpty()) {
			nextLogSlot = Collections.max(this.log.keySet()) + 1;
		} else {
			nextLogSlot = 0;
		}
		
		//Get leader
		int leaderId = this.leaderId;
		Node leader;
		if(leaderId == this.id) {
			leader = this;
		} else {
			leader = this.peers.get(leaderId);
		}
		
		if(leader == null) {
			System.out.println("[NODE] Unable to find leader, waiting until one is selected");
			while(this.leaderId == -1) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {

				}
			}
			System.out.println("[NODE] Found leader now. Trying to commit");
			this.insertAppointment(appointment);
		}
		
		// Send propose msg to leader
		DatagramSocket socket;
		try {
			System.out.println("[NODE] Sending propose message to leader: " + leader.id);
			InetSocketAddress addr = new InetSocketAddress(this.udpPort);
			socket = new DatagramSocket(null);
			socket.setReuseAddress(true);
			socket.setBroadcast(true);
			socket.bind(addr);
			UDPMessage msgObj = new UDPMessage("propose", -1, newCalendar, nextLogSlot, this.id);
			msgObj.print();
			byte[] buf = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(msgObj);
			oos.flush();
			byte[] data = baos.toByteArray();
			InetAddress add = InetAddress.getByName(leader.ipAddress);
			DatagramPacket packet = new DatagramPacket(data, data.length, add, leader.udpPort);
			socket.send(packet);
			socket.close();
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
	}
	
	public Appointment parseAppointment(String[] tokens) {
		
		Appointment appointment = null;
		
		String name = tokens[1];
	
		String participants = tokens[2];
		participants = participants.substring(1, participants.length()-1);
		String[] users = participants.split(",");
		List<Integer> userList = new ArrayList<Integer>();
		for(String u : users) {
			u = u.substring(u.length()-1, u.length());
			userList.add(Integer.parseInt(u));
		}
		
		String time = tokens[3];
		time = time.substring(1, time.length()-1);
		String[] times = time.split(",");
		String startTime = times[0];
		String endTime = times[1];
		
		String day = tokens[4];
	
		
		appointment = new Appointment(name, day, startTime, endTime, userList);
		return appointment;
	}
	
	public boolean checkAppointmentConflict(Appointment myAppointment) {
		List<Appointment> appointments = this.calendar.appointments;
		for(Appointment app : appointments) {
			if(isConflicting(app, myAppointment) == true) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isConflicting(Appointment app1, Appointment app2) {
		if(app1 == app2) {
			return false;
		}
		if(!(app1.day.equals(app2.day))) {
			return false;
		}
		
		List<Integer> participants1 = app1.participants;
		List<Integer> participants2 = app2.participants;
		
		List<Integer> commonParticipants = listIntersection(participants1, participants2);
		
		if(commonParticipants.isEmpty()){
			return false;
		}
		
		SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
		Date s1 = null;
		Date s2 = null;
		Date e1 = null;
		Date e2 = null;
		
		try {
			 s1 = parser.parse(app1.startTime);
			 s2 = parser.parse(app2.startTime);
			 e1 = parser.parse(app1.endTime);
			 e2 = parser.parse(app2.endTime);
		} catch (ParseException e) {
			
		} 
		
		
		if(e1.before(s2) || e1.equals(s2)) {
			return false;
		}
		if(e2.before(s1) || e2.equals(s1)) {
			return false;
		}	
		
		return true;
	}
	
	public List<Integer> listIntersection(List<Integer> l1, List<Integer> l2) {
		List<Integer> newList = new ArrayList<Integer>();
		for(Integer i : l1) {
			if(l2.contains(i)) {
				newList.add(i);
			}
		}
		return newList;
	}
	
	public boolean insertAppointment(Appointment appointment) {
		
		boolean canInsert = true;
		// conflicting appointments check
		canInsert = checkAppointmentConflict(appointment);
		if(canInsert == false)
			return false;
		
		Calendar newCalendar = new Calendar(this.calendar);
		newCalendar.appointments.add(appointment);
		
		int nextLogSlot = -1;
		//Logic for log keys
		if(!this.log.isEmpty()) {
			nextLogSlot = Collections.max(this.log.keySet()) + 1;
		} else {
			nextLogSlot = 0;
		}
		
		//Get leader
		int leaderId = this.leaderId;
		Node leader;
		if(leaderId == this.id) {
			leader = this;
		} else {
			leader = this.peers.get(leaderId);
		}
		
		if(leader == null) {
			System.out.println("[NODE] Unable to find leader, waiting until one is selected");
			while(this.leaderId == -1) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					
				}
			}
			System.out.println("[NODE] Found leader now. Trying to commit");
			this.insertAppointment(appointment);
		}
		
		// Send propose msg to leader
		DatagramSocket socket;
		try {
			System.out.println("[NODE] Sending propose message to leader: " + leader.id);
			InetSocketAddress addr = new InetSocketAddress(this.udpPort);
			socket = new DatagramSocket(null);
			socket.setReuseAddress(true);
			socket.setBroadcast(true);
			socket.bind(addr);
			UDPMessage msgObj = new UDPMessage("propose", -1, newCalendar, nextLogSlot, this.id);
			msgObj.print();
			byte[] buf = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(msgObj);
			oos.flush();
			byte[] data = baos.toByteArray();
			InetAddress add = InetAddress.getByName(leader.ipAddress);
			DatagramPacket packet = new DatagramPacket(data, data.length, add, leader.udpPort);
			socket.send(packet);
			socket.close();
			
		} catch (SocketException e) {
			
		} catch (IOException e) {
			
		}
		
		return canInsert;
		
	}

	public static void main(String args[]) {
		System.out.println("[NODE] Starting node with ID: " + args[0]);
		Node node = new Node(Integer.parseInt(args[0]));
		
		System.out.println("[NODE] Loading finger table");
		node.loadIpTable();
		
		System.out.println("[NODE] Restoring data from Firebase");
		node.loadFromFirebasenew();
	
		System.out.println("[NODE] Starting Leader Election Thread on TCP Port: " + node.port);
		new Thread(new LeaderElection(node)).start();
		//Start UI Server
		System.out.println("[NODE] Starting UI Server Thread on TCP Port: " + node.uiPort);
		new Thread(new UIServer(node)).start();
		
		System.out.println("[NODE] Starting Paxos Thread on UDP Port: " + node.udpPort);
		new Thread(new Paxos(node)).start();
		
		while(true) {
			Scanner scanner = new Scanner(System.in);
			String input = scanner.nextLine();
			
			if(input.equals("quit")) {
				node.terminate();
				break;
			} 
			else {
				node.parseCommand(input);
			}
		}
		
		LogManager.getLogManager().reset();
		
	}
	
	public void loadFromFirebasenew() {
		final AtomicBoolean done = new AtomicBoolean(false);
		Firebase ref = new Firebase("https://distributedcalendar.firebaseio.com/");
		final HashMap<Integer, Calendar> myHashMap = new HashMap<Integer, Calendar>();
		ref.addValueEventListener(new ValueEventListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				if(snapshot.getValue() != null) {
				if(snapshot.child("logFile").getChildrenCount() > 0) {
					 for (DataSnapshot messageSnapshot: snapshot.child("logFile").getChildren()) {
				            DatabaseObject dbObject = (DatabaseObject)messageSnapshot.getValue(DatabaseObject.class);
				            myHashMap.put( new Integer(dbObject.getLogSlot()), dbObject.getCal());
				        }
				
				}
				}
				 done.set(true);
			}
			@Override
			public void onCancelled(FirebaseError arg0) {

			}
		});
		 while (!done.get());
		 
		 //Set new values to node's log and calendar
		 
		 if(!myHashMap.isEmpty()) {
		 this.log = myHashMap;
		 this.calendar = this.log.get(Collections.max(this.log.keySet()));
		 }
	
	}


}
