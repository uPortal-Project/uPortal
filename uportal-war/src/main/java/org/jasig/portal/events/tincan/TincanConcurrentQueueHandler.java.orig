package org.jasig.portal.events.tincan;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jasig.portal.events.tincan.om.LrsStatement;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TincanConcurrentQueueHandler implements TinCanEventSender {
	final private Queue<LrsStatement> theQueue = new ConcurrentLinkedQueue<LrsStatement>();
	
	@Override
    public void sendEvent(LrsStatement statement) {
	    this.theQueue.offer(statement);
    }

    /**
	 * Clear the queue every 1 second after last completion
	 */
	@Scheduled(fixedDelay = 997)
	private void clearQueue()
	{
	    LrsStatement	 cur = theQueue.poll();
		while (cur != null) {
		    //TODO : Add in call to process request
			cur = theQueue.poll();
		}
	}
}
