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

import java.util.Vector;

import org.jasig.portal.services.LogService;


/**
 * An abstract implementation of the ThreadPool interface which
 * implements the totalThreads(), idleThreads(), signalThreadIdle(),
 * signalThreadWorking(), and destroy() methods.
 *
 * @author <a href="mailto:mvi@immagic.com>Mike Ivanov</a>
 * @version $Revision$
 */

public abstract class AbstractPool implements ThreadPool {
	    protected int priority;
	    protected boolean isDestroyed = false;
	    private Vector busyThreads;
        private Vector idleThreads;
        protected int minThreads, maxThreads;
        private long DELAY = 200L;
        protected Queue workQueue;

        private static int counter = 0;

        /**
	 * AbstractPool Construcutor
	 *
	 * @param minThreads the min number of worker threads that can be in this pool
	 * @param maxThreads the max number of worker threads that can be in this pool
	 * @param threadPriority the priority these worker threads should have
	 */
	public AbstractPool(int minThreads, int maxThreads, int threadPriority) {
                idleThreads = new Vector();
                busyThreads = new Vector();
                workQueue = new UnboundedQueue();
		        this.maxThreads = maxThreads;
                this.minThreads = minThreads;
		        priority = threadPriority;
                try {
                 initThreadPool();
                } catch ( Exception e ) {

                  }
	}


        /**
         * Initialize the thread pool
         */
        private void initThreadPool() throws Exception {
          LogService.log(LogService.DEBUG,"AbstractPool.initThreadPool() starting" );
          for ( int i = 0; i < minThreads; i++ ) {
             Thread thread = createNewThread();
             idleThreads.add(thread);
             thread.start();
          }
          LogService.log(LogService.DEBUG,"AbstractPool.initThreadPool() ending" );
        }


        /**
         * If necessary creates a new thread and adds it into the pool
         */
        protected synchronized void adjustThreadPool() throws Exception {
          if ( idleThreads() == 0 && busyThreads() < maxThreads ) {
                  Thread thread = createNewThread();
                  idleThreads.add(thread);
                  thread.start();
          }
        }


	/**
	* Queues up a task to be executed.  The queue use FIFO ordering.
	*
	* @param task the task to be executed
	*
	* @return the WorkerTracker for used to track and interact with this task
	*
	* @exception IllegalStateException - thrown if the pool has been destroyed
	*/
	public abstract WorkTracker execute(WorkerTask task) throws IllegalStateException;

    // Creates a new thread
    protected abstract Thread createNewThread() throws Exception;

         /**
	 * Destroys the pooled thread
         * @param thread the thread to be destroyed
         *
	 */
	public abstract void destroyThread(Thread thread);

         /**
	 * Destroys the pool and all it's threads.  A destroyed pool thread can not be used
	 * after is has been destroyed.  Threads running when a pool is destroyed are interrupted
	 * and then destroyed.
	 */
	public void destroy() {
          for ( int i = 0; i < idleThreads.size(); i++ )
            destroyThread((Thread)busyThreads.get(i));
          for ( int i = 0; i < busyThreads.size(); i++ )
            destroyThread((Thread)busyThreads.get(i));

          isDestroyed = true;
    }


	/**
	 * Gives the total number of threads in the pool
	 *
	 * @return the total number of worker threads (busy and idle) in the pool
	 */
	public synchronized int totalThreads() throws IllegalStateException {
		if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
		}

		return busyThreads.size() + idleThreads.size();
	}

	/**
	 * Gives the number of idle threads in the pool
	 *
	 * @return the number of idle worker threads in the pool
	 */
	public synchronized int idleThreads() throws IllegalStateException {
		if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
		}

		return idleThreads.size();
	}

	/**
	 * Gives the number of busy (working) worker threads in the pool
	 *
	 * @return the number of busy workers in the pool
	 */
	public synchronized int busyThreads() throws IllegalStateException{
		if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
		}

		return busyThreads.size();
	}



        /**
         *  Releases the thread to the pool
         *  @param thread the thread to be released
         */

        public synchronized void releaseThread(Thread thread) throws Exception {
             if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
             }

             busyThreads.remove(thread);
             idleThreads.add(thread);
        }


        /**
         *  Locks the thread for running
         *  @param thread the thread to be locked
         */
        public synchronized void lockThread(Thread thread) throws Exception {
             if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
             }

             busyThreads.add(thread);
             idleThreads.remove(thread);

        }


        /**
         *  Gets the thread from the pool
         *  @return a pooled thread
         */
        public synchronized Thread getPooledThread() throws Exception {

           if (isDestroyed) {
			throw new IllegalStateException("This pool has been destroyed!");
           }

           if ( idleThreads() > 0 ) {
             Thread thread = (Thread) idleThreads.get(0);
             lockThread(thread);
             return thread;
           }

           if ( idleThreads() == 0 && busyThreads() < maxThreads ) {
                Thread thread = createNewThread();
                busyThreads.add(thread);
                return thread;
           }
             try {
               Thread.yield();
               Thread.sleep(DELAY);
             } catch (InterruptedException ie ) {}


               return getPooledThread();
        }




}