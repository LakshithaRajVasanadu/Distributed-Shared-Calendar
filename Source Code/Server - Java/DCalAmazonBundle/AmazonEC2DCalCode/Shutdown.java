
public class Shutdown implements Runnable {

	Node n;
	
	public Shutdown(Node n) {
		this.n = n;
	}
	@Override
	public void run() {
		while(true) {
			if(n.terminate) {
				System.out.println("[ShutDownThread] Terminating the node");
				n.proposer.terminate = true;
				n.acceptor.terminate = true;
				break;
			}
		}
		
	}

}
