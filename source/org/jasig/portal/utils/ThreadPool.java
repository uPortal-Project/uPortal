/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.utils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A thread pool implementation with a few extra kinks,
 * such as ThreadPoolReceipt.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public class ThreadPool extends ThreadGroup {
    
    private static final Log log = LogFactory.getLog(ThreadPool.class);
    
    BlockingStack idleWorkers;
    List workers;
    ResourceLimits limits;

    public ThreadPool(String name,ResourceLimits rl) {
	super(name);
	if(rl==null) {
	    // use default resource limits
	    limits=new ResourceLimits();
	    rl.maxSize=10;
	    rl.optimalSize=3;
	} else {
	    limits=rl;
	}

	idleWorkers = new BlockingStack(); // doesn't make sense to put an upper limit on this stack
	//	workers = new Vector(limits.optimalSize);
	workers = Collections.synchronizedList(new ArrayList(limits.optimalSize));


	// initialize some workers
	for (int i=0; i<limits.optimalSize; i++) {
	    ThreadPoolWorker w=new ThreadPoolWorker(this);
	    workers.add(w);
            w.start();
	}
    }

    public ThreadPoolReceipt execute(Runnable target) throws InterruptedException {
        // try growing workers if the stack is empty
	if(idleWorkers.empty()) addWorker();
        // block on waiting for the next available worker
	//	ThreadPoolWorker worker = (ThreadPoolWorker) idleWorkers.pop();
	// start the process and return a receipt
	return(((ThreadPoolWorker)idleWorkers.pop()).process(target));
    }

    /**
     * Adjust the size of the worker pool.
     * Adjustment is done by growing/shrinking the idle worker pool.
     * Active workers will not be affected by this
     */
    protected synchronized void adjustSize(int newSize) {
	// see if an adjustment can be done
	if(newSize<=limits.maxSize) {
	    synchronized(workers) {
		// determine the adjustment
		int adjustment=newSize-workers.size();
		//		System.out.println("ThreadPool:adjustSize() : requested="+newSize+", current="+workers.size()+", adj="+adjustment);
		if(adjustment<0) {
		    // prune some idle workers
		    while(adjustment++<0) {
			if(!idleWorkers.empty()) {
			    try {
				releaseWorker((ThreadPoolWorker)idleWorkers.nonBlockingPop());
			    } catch (EmptyStackException ese) {
				adjustment--;
			    }
			} else {
			    // signal some active workers to go
			    for(int i=0;i<workers.size() && adjustment<0;i++) {
				ThreadPoolWorker w=(ThreadPoolWorker)workers.get(i);
				if(releaseWorker(w)) adjustment++;
			    }
			    break;
			}
		    }
		}
		if(adjustment>0) {
		    // add some idle workers
		    for(int i=0;i<adjustment;i++)
			addNewWorker();
		}
	    }
	}
    }

    /**
     * Grow the size of the worker pool.
     * This will "attempt" to add another worker, but
     * unlike addNewWorker(), the resource limits are checked.
     */
    protected synchronized void addWorker() {
	adjustSize(workers.size()+1);
    }

    /**
     * Signals the worker that it try to interrupt
     * the current job and quit.
     */
    protected void stopWorker(ThreadPoolWorker worker) {
	//	System.out.println("ThreadPool::stopWorker()");
	worker.stopRequest();
    }


    /**
     * Signals the worker that it should quite as soon
     * as a job (if any) is complete
     * @return false if the worker has already been released
     */
    protected boolean releaseWorker(ThreadPoolWorker worker) {
	//	System.out.println("ThreadPool::releaseWorker()");
	return worker.completeRequest();
    }

    /**
     * Adds a new worker. Doesn't check anything, just adds.
     */
    protected void addNewWorker() {
	//	System.out.println("ThreadPool::addNewWorker()");
        ThreadPoolWorker w= new ThreadPoolWorker(this);
	workers.add(w);
        w.start();
    }

    /**
     * Clears all of the idle workers
     */
    public void clearIdle() {
	try {
	    Object[] idle=new Object[idleWorkers.size()-idleWorkers.getMinSize()];
	    int index=0;
	    while(index<idle.length) {
			idle[index++]=idleWorkers.pop();
	    }
	    for ( int i = 0; i < idle.length; i++ ) {
		( (ThreadPoolWorker) idle[i] ).stopRequest();
	    }
	} catch ( InterruptedException x ) {
	    Thread.currentThread().interrupt(); // re-assert
	}
    }

    /**
     * Clears all of the workers.
     */
    public void clear() {
	// Stop the idle one's first since that won't interfere with anything
	// productive.
	clearIdle();

	// give the idle workers a quick chance to die
	try { Thread.sleep(250); } catch ( InterruptedException x ) { }

	// Step through the list of ALL workers that are still alive.
	for ( int i = 0; i < workers.size(); i++ ) {
	    if ( ((ThreadPoolWorker)workers.get(i)).isAlive() ) {
		((ThreadPoolWorker)workers.get(i)).stopRequest();
	    }
	}
    }

   /**
    * Handle the case when some worker crashes
    */
    public void uncaughtException(Thread t, Throwable e) {
	log.error("Registered an uncaught exception by thread "+t.getName(), e);
	if(t instanceof ThreadPoolWorker && !(e instanceof ThreadDeath)) {
	    ThreadPoolWorker w=(ThreadPoolWorker) t;
	    // clean up currentReceipt if the thread didn't do it
	    try {
		if(w.currentReceipt!=null) {
		    w.currentReceipt.updateStatus(null,true,false,e);
		}
	    } catch (Exception bad) {};

	    notifyWorkerRestart(w);
	}
    }

    /**
     * Notifies the pool that a certain worker is done and
     * wants to have a replacement started.
     */
    protected void notifyWorkerRestart(ThreadPoolWorker pw) {
      notifyWorkerFinished(pw);
      this.addWorker();
    }

    protected void killWorkerThread(ThreadPoolWorker pw) {
	// check the receipt
	try {
          if(pw.currentReceipt!=null) {
            pw.currentReceipt.updateStatus(null,true,false,null);
          }
	} catch (Exception bad) {};

	notifyWorkerFinished(pw);
	// hopefully all of the locks are released
        pw.interrupt();
        // pw.stop();
	this.addWorker();
	//	System.out.println("Removed and stopped worker "+pw.getName());
    }

    /**
     * Notifies the pool that a certain worker has finished.
     */
    protected void notifyWorkerFinished(ThreadPoolWorker pw) {
	// clean up
	//        System.out.println("ThreadPool::notifyWorkerFinished().");
	idleWorkers.remove(pw);
	synchronized(workers) {
	    int index=workers.indexOf(pw);
	    if(index!=-1)
		workers.remove(index);
	}
    }
}
