/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
