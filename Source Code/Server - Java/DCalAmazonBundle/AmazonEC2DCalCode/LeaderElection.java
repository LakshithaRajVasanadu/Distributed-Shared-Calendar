import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

public class LeaderElection implements Runnable {

	public static final int POLL_TIME = 15000;
	public static final int TIMEOUT = 3000;
	Node n;

	public LeaderElection(Node n) {
		this.n = n;
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSoc = new ServerSocket(n.port);
			int prevLeaderId = -1;
			while(true) {
				if(prevLeaderId == -1 || !(prevLeaderId > n.leaderId)) {
					new Thread(new BullyAlgorithm(this.n, TIMEOUT, serverSoc)).start();
				}
				Thread.sleep(POLL_TIME);

				System.out.println("[LeaderElectionThread] LEADER ELECTED, IS: " + n.leaderId);
				if(n.leaderId != prevLeaderId) {

					prevLeaderId = n.leaderId;
				}
				if(n.terminate)
					break;
			}
			serverSoc.close();
		} catch (IOException e) {

		} catch (InterruptedException e) {

		}
	}

	public boolean isPrevLeaderReachable(int prevLeaderId) {
		boolean isReachable = false;
		InetAddress address;
		try {
			address = InetAddress.getByName(getPrevLeader(prevLeaderId).ipAddress);
			if(address.isReachable(getPrevLeader(prevLeaderId).port)) {
				isReachable = true;
			}
		} catch (UnknownHostException e) {
			isReachable = false;
		} catch (IOException e) {
			isReachable = false;
		}

		return isReachable;
	}

	public Node getPrevLeader(int prevLeaderId) {
		Node leader = null;
		if(prevLeaderId == n.id)
			leader = n;
		else {
			for(Map.Entry<Integer, Node> entry: n.peers.entrySet()) {
				if(prevLeaderId == entry.getKey()) {
					leader = entry.getValue();
					break;
				}
			}
		}

		return leader;
	}

}