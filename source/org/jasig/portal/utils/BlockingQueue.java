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


package  org.jasig.portal.utils;

import java.util.LinkedList;

/**
 * A simple FIFO queue that has MIN/MAX capacity and
 * that blocks if either enqueue/dequeue would result
 * in violation of these limits.
 * Default values for min/max are 0/infinite
 * @author Peter Kharchenko
 */

public class BlockingQueue
{
    int maxSize;
    int minSize;
    
    volatile LinkedList queue = null;

    BlockingQueue()
    {
	maxSize=-1;
	minSize=0;
        queue = new LinkedList();
    }

    /**
     * Construct a new blocking queue with predefined max/min limits
     */
    BlockingQueue(int min, int max)
    {
	this();
	maxSize=max;
	minSize=min;
    }

    BlockingQueue(int max) {
	this(0,max);
    }

    /**
     * Add new object to the end of the queue
     * @param o object to be placed on the queue
     */
    public synchronized void enqueue( Object o ) throws InterruptedException
    {
	//    	while((queue.size()>=maxSize) || maxSize!=-1) {
    	while(queue.size()>=maxSize && maxSize!=-1) {
	    wait();
	}
	queue.addLast(o);
	notifyAll();
    }

    /**
     * Remove object from the beginning of the queue
     * @throws InterruptedException if the wait was interrupted
     */
    public synchronized Object dequeue() throws InterruptedException
    {
    	while(queue.size()<=minSize) {
	    wait();
	}
	notifyAll();
        return queue.removeFirst();
    }

    /**
     * Set the queue limits. 
     * To specify a queue without an upper bound (that is max=inifinity) use max value of -1
     */
    public synchronized void setLimits(int max, int min) {
	maxSize=max; minSize=min;
	notifyAll();
    }
    
    public int getMaxSize() { return maxSize; }
    public int getMinSize() { return minSize; }
    public synchronized void setMaxSize(int max) { maxSize=max; notifyAll();}
    public synchronized void setMinSize(int min) { minSize=min; notifyAll(); }
    public synchronized int size() { return queue.size(); }
}
