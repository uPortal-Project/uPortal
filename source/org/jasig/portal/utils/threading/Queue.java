/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
