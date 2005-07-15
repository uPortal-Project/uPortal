/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.threading.PriorityThreadFactory;
import org.springframework.beans.factory.InitializingBean;

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * Stats recorder implementation which on receipt of each stats recording event
 * fires a new thread tasked with notifying the child recorder of the event.  This
 * accomplishes processing stats recording in a new thread rather than in
 * the thread in which the event was generated.
 * 
 * This IStatsRecorder just fires the threads - it requires a target IStatsRecorder
 * which the threads will invoke.  You inject this target via the setTargetStatsRecorder()
 * setter method.
 * 
 * @version $Revision$ $Date$
 */
public final class ThreadFiringStatsRecorder 
    implements IStatsRecorder, InitializingBean {

    private Log log = LogFactory.getLog(getClass());
    
    private IStatsRecorder targetStatsRecorder;
    
    private ExecutorService threadPool;
    
    /**
     * Constructor specifying configurating of our thread pool.
     * @param initialThreads initial number of threads in the thread pool
     * @param maxThreads maximum number of threads to allow in the thread pool
     * @param threadPriority priority for the threads.
     */
    public ThreadFiringStatsRecorder(int initialThreads, int maxThreads, int threadPriority) {
        this.threadPool 
            = new ThreadPoolExecutor(initialThreads, maxThreads, 0L, 
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), 
                    new PriorityThreadFactory(threadPriority));
    }  
    
    
    public void recordLogin(IPerson person) {
        StatsRecorderWorkerTask task = new RecordLoginWorkerTask(person);
        executeStatsRecorderEvent(task);
    }

    public void recordLogout(IPerson person) {
        StatsRecorderWorkerTask task = new RecordLogoutWorkerTask(person);
        executeStatsRecorderEvent(task);
    }
    
    public void recordSessionCreated(IPerson person) {
        StatsRecorderWorkerTask task = new RecordSessionCreatedWorkerTask(person);
        executeStatsRecorderEvent(task);
    }
    
    public void recordSessionDestroyed(IPerson person) {
        StatsRecorderWorkerTask task = new RecordSessionDestroyedWorkerTask(person);
        executeStatsRecorderEvent(task);
    }
    
    public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {
        StatsRecorderWorkerTask task = new RecordChannelDefinitionPublishedWorkerTask(person, channelDef);
        executeStatsRecorderEvent(task);
    } 
    
    public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {
        StatsRecorderWorkerTask task = new RecordChannelDefinitionModifiedWorkerTask(person, channelDef);
        executeStatsRecorderEvent(task);
    }  
    
    public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {
        StatsRecorderWorkerTask task = new RecordChannelDefinitionRemovedWorkerTask(person, channelDef);
        executeStatsRecorderEvent(task);
    }  
    
    public void recordChannelAddedToLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        StatsRecorderWorkerTask task = new RecordChannelAddedToLayoutWorkerTask(person, profile, channelDesc);
        executeStatsRecorderEvent(task);
    }    
    
    public void recordChannelUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        StatsRecorderWorkerTask task = new RecordChannelUpdatedInLayoutWorkerTask(person, profile, channelDesc);
        executeStatsRecorderEvent(task);
    }  

    public void recordChannelMovedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        StatsRecorderWorkerTask task = new RecordChannelMovedInLayoutWorkerTask(person, profile, channelDesc);
        executeStatsRecorderEvent(task);
    }
    
    public void recordChannelRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        StatsRecorderWorkerTask task = new RecordChannelRemovedFromLayoutWorkerTask(person, profile, channelDesc);
        executeStatsRecorderEvent(task);
    }
    
    public void recordFolderAddedToLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
        StatsRecorderWorkerTask task = new RecordFolderAddedToLayoutWorkerTask(person, profile, folderDesc);
        executeStatsRecorderEvent(task);
    }    
    
    public void recordFolderUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
        StatsRecorderWorkerTask task = new RecordFolderUpdatedInLayoutWorkerTask(person, profile, folderDesc);
        executeStatsRecorderEvent(task);
    }  
    
    public void recordFolderMovedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
        StatsRecorderWorkerTask task = new RecordFolderMovedInLayoutWorkerTask(person, profile, folderDesc);
        executeStatsRecorderEvent(task);
    }
    
    public void recordFolderRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
        StatsRecorderWorkerTask task = new RecordFolderRemovedFromLayoutWorkerTask(person, profile, folderDesc);
        executeStatsRecorderEvent(task);
    }  
    
    public void recordChannelInstantiated(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        StatsRecorderWorkerTask task = new RecordChannelInstantiatedWorkerTask(person, profile, channelDesc);
        executeStatsRecorderEvent(task);
    }  
    
    public void recordChannelRendered(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        StatsRecorderWorkerTask task = new RecordChannelRenderedWorkerTask(person, profile, channelDesc);
        executeStatsRecorderEvent(task);
    }  
    
    public void recordChannelTargeted(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        StatsRecorderWorkerTask task = new RecordChannelTargetedWorkerTask(person, profile, channelDesc);
        executeStatsRecorderEvent(task);
    }
    
    private void executeStatsRecorderEvent(final StatsRecorderWorkerTask task) {
        // local defensive copy so that even if someone calls our setter method during
        // this method invocation, we will check for null
        IStatsRecorder delegateStatsRecorder = this.targetStatsRecorder;
        
        if (delegateStatsRecorder == null) {
            // log the illegal state and do nothing
            // we do not throw an exception because we don't want a statistics
            // recording problem to propogate out into more important portal components.
            log.error("targetStatsRecorder JavaBean property of ThreadFiringStatsRecorder illegally null.");
        } else {
            task.setStatsRecorder(this.targetStatsRecorder);
            this.threadPool.execute(task); // TODO is execute okay or should it be submit?
        }
    }
    
    
    /**
     * Get the target IStatsRecorder which the threads we fire will invoke when they
     * awake.
     * @return Returns the targetStatsRecorder.
     */
    public IStatsRecorder getTargetStatsRecorder() {
        return this.targetStatsRecorder;
    }
    
    /**
     * Set the child IStatsRecorder which the threads we fire will invoke when they
     * awake.
     * @param targetStatsRecorder The targetStatsRecorder to set.
     */
    public void setTargetStatsRecorder(IStatsRecorder targetStatsRecorder) {
        
        if (targetStatsRecorder == null) {
            throw new IllegalArgumentException("Cannot set targetStatsRecorder to null");
        }
        
        this.targetStatsRecorder = targetStatsRecorder;
    }


    public void afterPropertiesSet() throws Exception {
        // we implement this Spring beanfactory lifecycle interface method so that
        // when an instance of this class is configured using Spring, we
        // take the opportunity to check that our required JavaBean property 
        // has been set
        if (this.targetStatsRecorder == null) {
            throw new IllegalStateException("ThreadFiringStatsRecorder requires that the targetStatsRecorder be set.");
        }
    }
  }

