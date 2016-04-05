import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class PromiseListener implements Runnable {
	
	Proposer proposer;
	int numNodes;
	int majority;
	
	public PromiseListener(Proposer proposer) {
		this.proposer = proposer;
	}

	@Override
	public void run() {
		
		while(true) {
			numNodes = proposer.node.peers.size()+ 1;
			majority = (int) (Math.ceil(numNodes/2.0));
			
			if(!this.proposer.promiseQueues.isEmpty()) {

			}
			
			for(Map.Entry<Integer, LinkedHashMap<Integer, QueueObject>> slotEntry: this.proposer.promiseQueues.entrySet()){
				Integer slot = slotEntry.getKey();
				HashMap<Integer, QueueObject> slotQueue = slotEntry.getValue();
				int numberPromises = slotQueue.size();
				if(numberPromises >= majority) {
					int m = -1;
					Calendar v = new Calendar();
					for(Map.Entry<Integer, QueueObject> queueEntry : slotQueue.entrySet()) {
						int key = queueEntry.getKey();
						QueueObject value = queueEntry.getValue();
					
						if(value.accNum != -1) {
							if(value.accNum > m) {
								m = this.proposer.myProposals.get(slot).proposalNumber;
								v = this.proposer.myProposals.get(slot).calendar;
							}
						}
					}
					if(!this.proposer.committedSlots.contains(slot)) {
						this.proposer.sendAccept(m, v, slot);
					}
				}
			}
			if(proposer.terminate) {
				break;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
