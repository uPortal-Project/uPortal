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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortletHungCompleteEvent;
import org.jasig.portal.events.PortletHungEvent;
import org.jasig.portal.utils.ConcurrentMapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Watches for {@link PortletHungEvent} and {@link PortletHungCompleteEvent} events and uses that information to track
 * the number of portlets for each fname that are hung.
 * 
 * @author Eric Dalquist
 */
@ManagedResource("uPortal:section=Framework,name=HungWorkerAnalyzer")
@Service("hungWorkerAnalyzer")
public class HungWorkerAnalyzer implements ApplicationListener<PortalEvent>, InitializingBean, IPortletExecutionInterceptor, HungWorkerAnalyzerMXBean {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    // Tracks the number of hung portlets for each fname 
    private final ConcurrentMap<String, AtomicInteger> hungPortletCounts = new ConcurrentHashMap<String, AtomicInteger>();

    //Read only view that returns Integer instead of AtomicInteger, used for JMX stats
    private final Map<String, Integer> hungPortletCountsView = Maps.transformValues(this.hungPortletCounts, new Function<AtomicInteger, Integer>() {
        public Integer apply(AtomicInteger value) {
            return value.get();
        }
    });
    
    private final AtomicInteger hungPortletCountTotal = new AtomicInteger();
    
    
    @Deprecated
    private Integer numberPermittedErrantByFname;
    
    private ThreadPoolExecutor portletThreadPool;
    
    private double percentPermittedErrantByFname = .1;
    
    /**
     * @deprecated use {@link #setPercentPermittedErrantByFname(double)}
     */
    @Value("${org.jasig.portal.portlet.numberPermittedErrantByFname:}")
    @Deprecated
    public void setNumberPermittedErrantByFname(Integer numberPermittedErrantByFname) {
        this.numberPermittedErrantByFname = numberPermittedErrantByFname;
    }
    
    @Value("${org.jasig.portal.portlet.percentPermittedErrantByFname:.1}")
    @Override
    public void setPercentPermittedErrantByFname(double percentPermittedErrantByFname) {
        this.percentPermittedErrantByFname = percentPermittedErrantByFname;
    }
    
    @Override
    public double getPercentPermittedErrantByFname() {
        return this.percentPermittedErrantByFname;
    }

    @Autowired
    public void setPortletThreadPool(@Qualifier("portletThreadPool") ExecutorService portletThreadPool) {
        //Note this is injected as a ExecutorService then cast due to the original object being created by a FactoryBean that declares itself as an ExecutorService
        this.portletThreadPool = (ThreadPoolExecutor)portletThreadPool;
    }
    
    @Override
    public int getHungPortletCountTotal() {
        return hungPortletCountTotal.get();
    }
    
    @Override
    public Map<String, Integer> getHungPortletCounts() {
        return this.hungPortletCountsView;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (numberPermittedErrantByFname != null) {
            if (numberPermittedErrantByFname == 0) {
                this.percentPermittedErrantByFname = 0;
            }
            else if (numberPermittedErrantByFname > 0) {
                this.percentPermittedErrantByFname = ((double)numberPermittedErrantByFname) / this.portletThreadPool.getMaximumPoolSize();
            }
        }
    }

    @Override
    public void onApplicationEvent(PortalEvent event) {
        if (event instanceof PortletHungEvent) {
            final IPortletExecutionWorker<?> worker = ((PortletHungEvent) event).getWorker();
            countHungWorker(worker);
        }
        else if (event instanceof PortletHungCompleteEvent) {
            final IPortletExecutionWorker<?> worker = ((PortletHungCompleteEvent) event).getWorker();
            countHungCompleteWorker(worker);
        }
    }
    
    protected void countHungWorker(IPortletExecutionWorker<?> worker) {
        final String portletFname = worker.getPortletFname();
        AtomicInteger count = this.hungPortletCounts.get(portletFname);
        if (count == null) {
            count = ConcurrentMapUtils.putIfAbsent(this.hungPortletCounts, portletFname, new AtomicInteger());
        }
        final int hungWorkerCount = count.incrementAndGet();
        this.hungPortletCountTotal.incrementAndGet();

        logState(portletFname, hungWorkerCount);
    }
    
    protected void countHungCompleteWorker(IPortletExecutionWorker<?> worker) {
        final String portletFname = worker.getPortletFname();
        final AtomicInteger count = this.hungPortletCounts.get(portletFname);
        if (count != null) {
            final int hungWorkerCount = count.decrementAndGet();

            logState(portletFname, hungWorkerCount);
        }
        this.hungPortletCountTotal.decrementAndGet();
    }

    private void logState(final String portletFname, final int hungWorkerCount) {
        final int maximumPoolSize = this.portletThreadPool.getMaximumPoolSize();
        final int availableWorkers = maximumPoolSize - this.portletThreadPool.getActiveCount();
        final double hungWorkerLimit = this.percentPermittedErrantByFname * availableWorkers;
        
        final String msg = "Portlet '{}' has {} hung workers out of {} total and {} available workers with a limit of {} hung workers.";
        final Object[] args = new Object[] { portletFname, hungWorkerCount, maximumPoolSize, availableWorkers, hungWorkerLimit };
        if (hungWorkerCount >= Math.ceil(hungWorkerLimit)) {
            logger.warn(msg, args);
        }
        else if (hungWorkerCount >= Math.ceil(hungWorkerLimit / 2)) {
            logger.info(msg, args);
        }
        else {
            logger.debug(msg, args);
        }
    }
    
    @Override
    public void preSubmit(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
        if (this.percentPermittedErrantByFname <= 0) {
            //Hung worker starving is disabled, let everything execute
            return;
        }
        
        final String portletFname = context.getPortletFname();
        final AtomicInteger count = this.hungPortletCounts.get(portletFname);
        if (count == null) {
            //Never had a hung worker, good job go execute
            return;
        }
        
        final int hungWorkers = count.get();
        if (hungWorkers == 0) {
            //Currently no hung workers, good job go execute
            return;
        }
        
        final int maximumPoolSize = this.portletThreadPool.getMaximumPoolSize();
        final int availableWorkers = maximumPoolSize - this.portletThreadPool.getActiveCount();
        final double hungWorkerLimit = this.percentPermittedErrantByFname * availableWorkers;
        if (hungWorkers < Math.ceil(hungWorkerLimit)) {
            //Number of hung workers is less than the calculated hung worker limit
            return;
        }
        
        final String msg = "Denying worker execution for " + portletFname + " that has " + hungWorkers + " hung threads over limit of " + hungWorkerLimit + " with " + availableWorkers + " threads of " + maximumPoolSize + " available";
        logger.info(msg);
        throw new IllegalStateException(msg);
    }

    @Override
    public void preExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
    }

    @Override
    public void postExecution(HttpServletRequest request, HttpServletResponse response,
            IPortletExecutionContext context, Exception e) {
    }
}
