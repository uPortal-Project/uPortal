/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.threading;

/**
 * An implementation of the Queue interface which does not have
 * a limit on the number of items it can store.
 * 
 * @author <a href="mailto:clajoie@vt.edu">Chad La Joie</a>
 * @version $Revision$
 */

import java.util.ArrayList;

public class UnboundedQueue implements Queue {
	private ArrayList elements;
	private int elementCount;

	/**
	 * Creates an UnboucedQueue with no elements
	 */
	public UnboundedQueue() {
		elementCount = 0;
		elements = new ArrayList();
	}

	/**
	 * Checks if the queue is empty
	 * 
	 * @return True if the queue contains no items, false if not
	 */
	public boolean isEmpty() {
		if (elementCount == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks to see if the queue is full
	 * 
	 * @return returns false as this queue does not have an upper item limit and can therefore never be full
	 */
	public boolean isFull() {
		return false;
	}

	/**
	 * Gets the total number of items in the queue
	 * 
	 * @return the number of items currently in the queue
	 */
	public int size() {
		return elementCount;
	}

	/**
	 * Puts an item into the Queue
	 * 
	 * @param o the item being placed in the queue
	 * 
	 * @exception InterruptedException never thrown as a thread never has to wait to put an item into the queue
	 */
	public synchronized void put(Object o) throws InterruptedException {
		elements.add(o);

		if (elementCount++ == 0) {
			notifyAll();
		}
	}

	/**
	 * Takes the next item in the Queue, if there are no items in the queue the thread 
	 * is blocked until there is.
	 * 
	 * @return The next Object in the queue
	 * 
	 * @exception InterruptedException thrown when a thread is waiting get an item
	 * 	from an empty queue and another thread interrupts it
	 */
	public synchronized Object take() throws InterruptedException {
		while (isEmpty()) {
			wait();
		}

		elementCount--;
		Object o = elements.remove(0);

		return o;
	}
}