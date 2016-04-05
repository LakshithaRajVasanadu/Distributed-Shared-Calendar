import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class AckListener implements Runnable {

	Proposer proposer;
	int numNodes;
	int majority;

	public AckListener(Proposer proposer) {
		this.proposer = proposer;
	}

	@Override
	public void run() {
		while(true) {
			numNodes = proposer.node.peers.size()+ 1;
			majority = (int) (Math.ceil(numNodes/2.0));

			//********* CHECK QUEUE ***********
			for(Map.Entry<Integer, LinkedHashMap<Integer, QueueObject>> slotEntry: this.proposer.ackQueues.entrySet()){
				Integer slot = slotEntry.getKey();
				LinkedHashMap<Integer, QueueObject> slotQueue = slotEntry.getValue();
				int numberacks = slotQueue.size();
				if(numberacks >= majority) {
					if(!this.proposer.committedSlots.contains(slot)) {
						//to get first key
						Map.Entry<Integer, QueueObject> entry = slotQueue.entrySet().iterator().next();
						Calendar v = slotQueue.get(entry.getKey()).accVal;
						this.proposer.sendCommit(v, slot);
						this.proposer.committedSlots.add(slot);
					}
				}
			}
			if(proposer.terminate) {
				break;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {

			}
		}
	}

}
