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

/**
 * A receipt that provides some information/control
 * about a job that's being processed on the ThreadPool
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 */

public class ThreadPoolReceipt {
  protected ThreadPoolWorker worker;
  protected boolean jobdone;
  protected boolean jobsuccessful;
  protected Throwable thrownException;


  public ThreadPoolReceipt(ThreadPoolWorker w) {
    this.worker=w;
    jobdone=false;
    jobsuccessful=false;
    thrownException=null;
  }

  public String toString() {
   String s=new String("done="+jobdone+", successful="+jobsuccessful);
   if(thrownException!=null) {
    s+="\n"+thrownException;
   }
   return s;
  }

  public synchronized void updateStatus(ThreadPoolWorker currentWorker, boolean isdone, boolean issuccessful,Throwable ex) {
   this.worker=currentWorker;
   this.jobdone=isdone;
   this.jobsuccessful=issuccessful;
   this.thrownException=ex;
   notifyAll();
  }

  /**
   * Signals to the thread that it should abandon all hopes
   * and kill the job as soon as possible.
   */
  public synchronized void killJob() {
      if(!jobdone && worker!=null) {
          worker.killRequest();
      }
  }

  public synchronized void releaseWorker() {
      if(!jobdone && worker!=null) {
          worker.completeRequest();
      }
  }

  public Throwable getThrownException() {
    return thrownException;
  }
  public boolean isJobsuccessful() {
    return jobsuccessful;
  }
  public synchronized boolean isJobdone() {
    return jobdone;
  }
}
