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

import org.jasig.portal.services.LogService;


/**
 * An internal worker thread for the ThreadPool
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 */
public class ThreadPoolWorker extends Thread {
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
          LogService.log(LogService.ERROR,"ThreadPoolWorker::process() : trying to use a working worker !!! This should never happen.");
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
