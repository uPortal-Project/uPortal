/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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