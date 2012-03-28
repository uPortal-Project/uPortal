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

package org.jasig.portal.portlet.rendering.worker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("hungWorkerAnalyzer")
public final class HungWorkerAnalyzer {

    /**
     * Indicates the number of threads in the hungWorkers queue a portlet (by 
     * fname) must have before a WARN message is logged durring the analysis 
     * process.  If a WARN message is logged, an INOFO message will not be 
     * logged.
     */
    private static final int THRESHOLD_LOG_WARN = 10; 

    /**
     * Indicates the number of threads in the hungWorkers queue a portlet (by 
     * fname) must have before an INFO message is logged durring the analysis 
     * process.
     */
    private static final int THRESHOLD_LOG_INFO = 4; 

    /**
     * A copy of the most recent analysis of hung workers.  This analysis can be 
     * used to prevent misbehaving portlets from taking too many worker threads 
     * and starving the portal of them. 
     */
    private volatile Map<String,PortletHungWorkerAnalysisEntry> hungWorkerAnalysis = Collections.emptyMap();
    
    /**
     * The ratio, relative to a portlet's configured timeout value, of how long 
     * it can hold onto a rendering thread before being considered errant.  
     * Portlets with too many erant threads may be denied further worker 
     * threads, thus preventing starvation.  
     */
    private double errantThreshold = 2.0D;  // Spring-configurable, though the default should be good
    
    /**
     * The number of errant threads a portlet may have (by fname) before further 
     * worker threads will be withheld.  A portlet that does not receive worker 
     * threads will render an error, but this outcome is better than starving 
     * the portal for threads and causing ALL portlets to render errors for all 
     * users.
     */
    private int numberPermittedErrantByFname = 10;  // Spring-configurable
    
    private final Log log = LogFactory.getLog(this.getClass());

    /*
     * Public API
     */

    /**
     * @param errantThreshold The ratio, relative to a portlet's configured 
     * timeout value, of how long it can hold onto a rendering thread before 
     * being considered errant.  Default is 2.0D (which is probably reasonable 
     * for most circumstances).
     */
    public void setErrantThreshold(double errantThreshold) {
        this.errantThreshold = errantThreshold;
    }

    @Value("${org.jasig.portal.portlet.numberPermittedErrantByFname}")
    public void setNumberPermittedErrantByFname(int numberPermittedErrantByFname) {
        this.numberPermittedErrantByFname = numberPermittedErrantByFname;
    }
    
    public void analyze(final Queue<IPortletExecutionWorker<?>> hungWorkers) {
        
        final long startTime = System.currentTimeMillis();
        
        if (hungWorkers.isEmpty()) {
            hungWorkerAnalysis = Collections.emptyMap();
            return;
        }
        
        // Prepare the report
        final  Map<String,PortletHungWorkerAnalysisEntry> report = new HashMap<String,PortletHungWorkerAnalysisEntry>();
        for (final IPortletExecutionWorker<?> worker : hungWorkers) {
            // We won't analyze complete workers since they're not a problem
            if (!worker.isComplete()) {
                final String fname = worker.getPortletFname();
                PortletHungWorkerAnalysisEntry entry = report.get(fname);
                if (entry == null) {
                    entry = new PortletHungWorkerAnalysisEntry(fname, worker.getApplicableTimeout());
                    report.put(fname, entry);
                }
                entry.recordHungWorker(worker.getStartedTime());
            }
        }
        
        // Log information from the report, if applicable
        if (!report.isEmpty()) {
            for (final PortletHungWorkerAnalysisEntry entry : report.values()) {
                switch (entry.getNumberInHungWorkersQueue()) {
                    case THRESHOLD_LOG_WARN:
                        if (log.isWarnEnabled()) {
                            log.warn(entry.toString());
                        }
                        break;
                    case THRESHOLD_LOG_INFO:
                        if (log.isInfoEnabled()) {
                            log.info(entry.toString());
                        }
                        break;
                    default:
                        if (log.isDebugEnabled()) {
                            log.debug(entry.toString());
                        }
                        break;
                }
            }
        }
        
        // Replace the existing hungWorkerAnalysis with the new report 
        hungWorkerAnalysis = Collections.unmodifiableMap(report);
        
        if (log.isTraceEnabled()) {
            final long runTime = System.currentTimeMillis() - startTime;
            log.trace("Hung worker analysis performed;  analysis completed in (milliseconds):  " + runTime);
        }
        
    }
    
    public boolean allowWorkerThreadAllocationForPortlet(final String fname) {
        
        boolean rslt = true;  // default... unless there's a reason not to
        
        /*
         *  Setting numberPermittedErrantByFname=0 shuts this feature off and 
         *  allows portlets to take workers no matter how many errant threads 
         *  they have. 
         */
        if (numberPermittedErrantByFname != 0) {
            final PortletHungWorkerAnalysisEntry entry = hungWorkerAnalysis.get(fname);
            if (entry != null) {
                // So there is a report on this fname;  now see if it meets the criteria
                if (entry.getNumberErrant() >= this.numberPermittedErrantByFname) {
                    // It DOES meet the criteria;  prevent further worker allocation
                    rslt = false;
                }
            }
        }
        
        if (!rslt && log.isTraceEnabled()) {
            log.trace("Worker thread withheld for the following portlet:  " + fname);
        }

        return rslt;
        
    }
    
    /*
     * Nested Types
     */
    
    /**
     * Provides important information about "hung worker" threads by portlet 
     * fname.  Examples are how many workers are in the hung queue for an fname, 
     * and how many of them are "errant" or rogue.  A worker is considered 
     * errant if its render time exceeds its configured timeout multiplied by 
     * <code>HungWorkerAnalyzer.errantThreshold</code>.
     * 
     * @author awills
     */
    private /* not-static */ final class PortletHungWorkerAnalysisEntry {
        
        private final String fname;
        private final long configuredTimeout;
        private int numberInHungWorkersQueue = 0;
        private int numberErrant = 0;
        private long longestRunTime = 0L;
        
        public PortletHungWorkerAnalysisEntry(final String fname, final long configuredTimeout) {
            this.fname = fname;
            this.configuredTimeout = configuredTimeout;
        }
        
        public void recordHungWorker(final long currentRunTime) {
            ++numberInHungWorkersQueue;
            if (currentRunTime > configuredTimeout * errantThreshold) {
                ++numberErrant;
            }
            if (currentRunTime > longestRunTime) {
                longestRunTime = currentRunTime;
            }
        }
        
        public int getNumberInHungWorkersQueue() {
            return numberInHungWorkersQueue;
        }

        public int getNumberErrant() {
            return numberErrant;
        }

        @Override
        public String toString() {
            final StringBuilder rslt = new StringBuilder();
            rslt.append("Portlet '").append(fname).append("' has ")
                    .append(numberInHungWorkersQueue)
                    .append(" workers not completing properly, of which ")
                    .append(numberErrant)
                    .append(" are considered errant.  The longest runtime is ")
                    .append(longestRunTime).append(" milliseconds.");
            return rslt.toString();
        }
        
    }

}
