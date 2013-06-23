package org.jasig.portal.events.tincan;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jasig.portal.events.tincan.om.LrsStatement;
import org.springframework.scheduling.annotation.Scheduled;

public class TincanConcurrentQueueHandler {
	
	final private Queue<LrsStatement> theQueue = new ConcurrentLinkedQueue<LrsStatement>();
	
	/**
	 * Clear the queue every 1 second after last completion
	 */
	@Scheduled(fixedDelay = 1000)
	private void clearQueue()
	{
	    LrsStatement	 cur = theQueue.poll();
		while (cur != null) {
			//TODO : Add in call to process request
			cur = theQueue.poll();
		}
	}
	
	public void sendLrs(LrsStatement newEdition) {
		theQueue.add(newEdition);
	}
}
