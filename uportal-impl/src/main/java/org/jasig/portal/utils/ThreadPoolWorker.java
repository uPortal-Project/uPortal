/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An internal worker thread for the ThreadPool
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 */
public class ThreadPoolWorker extends Thread {
    
    private static final Log log = LogFactory.getLog(ThreadPoolWorker.class);
    
    private static int nextWorkerID = 0;

    private ThreadPool pool;
    private BlockingQueue taskQueue;
    protected ThreadPoolReceipt currentReceipt;
    protected volatile boolean shouldRestart;
    protected volatile boolean shouldQuit;

    public ThreadPoolWorker(ThreadPool pool) {
	super(pool,pool.getName()+":"+getNextWorkerID());
	this.pool=pool;

	taskQueue = new BlockingQueue(0,1); // only one slot

	shouldQuit=false;
        shouldRestart=false;
    }

    public void run() {
	runWork();
        if(shouldRestart)
         pool.notifyWorkerRestart(this);
        else
         pool.notifyWorkerFinished(this);
    }

    public static synchronized String getNextWorkerID() {
	return Integer.toString(nextWorkerID++);
    }

    public synchronized ThreadPoolReceipt process(Runnable target) throws InterruptedException {
        // construct a receipt
        if(currentReceipt!=null)
          log.error("ThreadPoolWorker::process() : trying to use a working worker !!! This should never happen.");
        ThreadPoolReceipt returnReceipt=currentReceipt=new ThreadPoolReceipt(this);
	taskQueue.enqueue(target);
        return returnReceipt;
    }

    private void runWork() throws RuntimeException {
	while ( !shouldQuit && !shouldRestart) {
	    try {
		//		System.out.println("workerID="+this.getName()+", ready for work");
		if(pool.idleWorkers.nonBlockingPush(this)){
		  // wait here until the server puts a request into the box
		  Runnable r = (Runnable) taskQueue.dequeue();
		  //		  System.out.println("workerID="+this.getName()+", starting execution of new Runnable: "+r);
		  runIt(r);
		} else {
                 // could not push myself to the idleWorkers stack - bailing out
                 shouldQuit=true;
		}
	    } catch ( InterruptedException x ) {
                if(currentReceipt!=null) {
                  currentReceipt.updateStatus(null,true,false,x);
                  currentReceipt=null;
                }
		Thread.currentThread().interrupt(); // re-assert
	    }
	}
    }

    private void runIt(Runnable r) throws RuntimeException {
	try {
	    r.run();
            currentReceipt.updateStatus(null,true,true,null);
            currentReceipt=null;
	} catch ( RuntimeException runex ) {
	    // catch any and all exceptions
            currentReceipt.updateStatus(null,true,false,runex);
            currentReceipt=null;
            throw runex;
	}
    }

    public synchronized boolean completeRequest() {
	if(shouldQuit) return false;
	shouldQuit=true;
	return true;
    }

    public void stopRequest() {
	//	System.out.println("workerID="+this.getName()+", stopRequest() received.");
	shouldQuit = true;
	this.interrupt();
    }

    public void killRequest() {
	System.out.println("workerID="+this.getName()+", killRequest() received.");
	shouldRestart = true;
	this.interrupt();
	pool.killWorkerThread(this);
    }

}
