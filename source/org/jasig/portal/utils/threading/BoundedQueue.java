/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.threading;

/**
 * An implmentation of the Queue interface which has an upper limit 
 * on the number of items it can contain
 * 
 * @author <a href="mailto:clajoie@vt.edu">Chad La Joie</a>)
 * @version $Revision$
 */

import java.util.ArrayList;

public class BoundedQueue implements Queue {
	private ArrayList elements;
	private int capacity;
	private int elementCount;

	/**
	 * Creates a BoucedQueue with no elements
	 * 
	 * @param capacity the max number of items this queue may contain
	 */
	public BoundedQueue(int capacity) {
		this.capacity = capacity;

		elementCount = 0;
		elements = new ArrayList(capacity / 4);
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
	 * @return True if the queue can contain no more items, false if not.
	 * 	False is also returned if the queue has no limit on the number of items it can contain
	 */
	public boolean isFull() {
		if (elementCount == capacity) {
			return true;
		} else {
			return false;
		}
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
	 * @exception InterruptedException thrown when a thread is waiting to place an item
	 * 	into a full queue and another thread interrupts it
	 */
	public void put(Object o) throws InterruptedException {
		while (isFull()) {
			synchronized (this) {
				wait();
			}
		}

		elements.add(o);

		if (elementCount++ == 0) {
			synchronized (this) {
				notifyAll();
			}
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
	public Object take() throws InterruptedException {
		while (isEmpty()) {
			synchronized (this) {
				wait();
			}
		}

		Object o = elements.remove(0);

		if (elementCount-- == capacity) {
			synchronized (this) {
				notifyAll();
			}
		}

		return o;
	}

}