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

/**
 * A receipt that provides some information/control
 * about a job that's being processed on the ThreadPool
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
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
