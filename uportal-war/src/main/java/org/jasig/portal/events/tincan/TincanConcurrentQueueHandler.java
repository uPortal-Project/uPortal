package org.jasig.portal.events.tincan;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jasig.portal.events.tincan.om.LrsObject;
import org.springframework.scheduling.annotation.Scheduled;

public class TincanConcurrentQueueHandler {
	
	final private Queue<LrsObject> theQueue = new ConcurrentLinkedQueue<LrsObject>();
	
	/**
	 * Clear the queue every 1 second after last completion
	 */
	@Scheduled(fixedDelay = 1000)
	private void clearQueue()
	{
		LrsObject	 cur = theQueue.poll();
		while (cur != null) {
			//TODO : Add in call to process request
			cur = theQueue.poll();
		}
	}
	
	public void sendLrs(LrsObject newEdition) {
		theQueue.add(newEdition);
	}
}
