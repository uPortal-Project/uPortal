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
 * A FIFO Queue.
 * 
 * @author <a href="mailt:clajoie@vt.edu">Chad La Joie</a>
 * @version $Revision$
 */

public interface Queue{
	
	/**
	 * Puts an item into the Queue
	 * 
	 * @param o the item being placed in the queue
	 * 
	 * @exception InterruptedException thrown when a thread is waiting to place an item
	 * 	into a full queue and another thread interrupts it
	 */
	public void put(Object o) throws InterruptedException ;
	
	/**
	 * Takes the next item in the Queue, if there are no items in the queue the thread 
	 * is blocked until there is.
	 * 
	 * @return The next Object in the queue
	 * 
	 * @exception InterruptedException thrown when a thread is waiting get an item
	 * 	from an empty queue and another thread interrupts it
	 */
	public Object take() throws InterruptedException ;
	
	/**
	 * Checks if the queue is empty
	 * 
	 * @return True if the queue contains no items, false if not
	 */
	public boolean isEmpty();
	
	/**
	 * Checks to see if the queue is full
	 * 
	 * @return True if the queue can contain no more items, false if not.
	 * 	False is also returned if the queue has no limit on the number of items it can contain
	 */
	public boolean isFull();
	
	/**
	 * Gets the total number of items in the queue
	 * 
	 * @return the number of items currently in the queue
	 */
	public int size();
}
