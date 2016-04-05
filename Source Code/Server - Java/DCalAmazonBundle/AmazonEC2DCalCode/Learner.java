import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class Learner implements Runnable {

	Node n;

	public Learner(Node n) {
		
		this.n = n;
	}
	@Override
	public void run() {
		while(true) {
			if(!n.acceptor.commitsQueue.isEmpty()) {
				CommitQueueObject obj = n.acceptor.commitsQueue.get(n.acceptor.commitsQueue.size()-1);
				n.acceptor.commitsQueue.remove(n.acceptor.commitsQueue.size()-1);
				System.out.println("[LearnerThread] Committing to log file");

				n.log.put(obj.logSlot, obj.v);
				n.calendar = n.log.get(Collections.max(n.log.keySet()));
				if(n.leaderId == n.id) {
					 Thread t = new Thread(new DatabaseThread(new DatabaseObject(obj.logSlot, obj.v)));
				        t.start();
				        
				        try {
							t.join();
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							
						}
				        
				        
				}
			}
			if(n.terminate) {
				break;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {

			}
		}
		
	}

}
