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

package org.jasig.portal.utils.threading;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.joda.time.ReadableDuration;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Scheduling thread pool that upon invocation of the scheduled task immediately
 * delegates execution to another {@link ExecutorService}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DelegatingThreadPoolTaskScheduler extends ThreadPoolTaskScheduler 
    implements TaskScheduler, SchedulingTaskExecutor {
    private static final long serialVersionUID = 1L;
    
    private volatile long initialized = System.currentTimeMillis();
    private volatile long lastStartDelay = 0; 
    
    private ExecutorService executorService;
    private long initialDelay = 0;
    
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * delay to add to the start date for all scheduled tasks
     * 
     * @param initialDelay Delay before starting ANY scheduled task
     */
    public void setInitialDelay(ReadableDuration initialDelay) {
        this.initialDelay = initialDelay.getMillis();
        this.lastStartDelay = this.initialDelay;
    }

    @Override
    public void afterPropertiesSet() {
        this.initialized = System.currentTimeMillis();
        super.afterPropertiesSet();
    }
    
    /**
     * @return the additional start delay to add to any scheduled task
     */
    protected long getAdditionalStartDelay() {
        //Only bother recalculating the start delay if the last time it was resulted in a delay
        if (this.lastStartDelay != 0) {
            this.lastStartDelay = Math.max(0, this.initialDelay - (System.currentTimeMillis() - this.initialized));
            logger.debug("Calculated additionalStartDelay of: " + this.lastStartDelay);
        }
        
        return this.lastStartDelay;
    }
    
    protected Date getDelayedStartDate(Date startDate) {
        final long additionalStartDelay = this.getAdditionalStartDelay();
        if (additionalStartDelay > 0) {
            final Date newStartDate = new Date(startDate.getTime() + additionalStartDelay);
            logger.debug("Updated startDate with additionalStartDelay from " + startDate + " to " + newStartDate);
            return newStartDate;
        }
        
        return startDate;
    }
    
    @Override
    public void execute(Runnable task, long startTimeout) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        super.execute(delegatingRunnable, startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        return super.submit(delegatingRunnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        final DelegatingCallable<T> delegatingCallable = new DelegatingCallable<T>(this.executorService, task);
        final Future<Future<T>> future = super.submit(delegatingCallable);
        
        return new DelegatingForwardingFuture<T>(future);
    }

    @Override
    public void execute(Runnable task) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        super.execute(delegatingRunnable);        
    }

    @Override
    public ScheduledFuture<Object> schedule(Runnable task, final Trigger trigger) {
        //Wrap the trigger so that the first call to nextExecutionTime adds in the additionalStartDelay 
        final Trigger wrappedTrigger = new Trigger() {
            boolean firstExecution = false;
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                Date nextExecutionTime = trigger.nextExecutionTime(triggerContext);
                if (firstExecution) {
                    nextExecutionTime = getDelayedStartDate(nextExecutionTime);
                    firstExecution = true;
                }
                return nextExecutionTime;
            }
        };
        
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        @SuppressWarnings("unchecked")
        final ScheduledFuture<ScheduledFuture<Object>> future = super.schedule(delegatingRunnable, wrappedTrigger);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> schedule(Runnable task, Date startTime) {
        startTime = getDelayedStartDate(startTime);
        
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        @SuppressWarnings("unchecked")
        final ScheduledFuture<ScheduledFuture<Object>> future = super.schedule(delegatingRunnable, startTime);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        startTime = getDelayedStartDate(startTime);
        
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        @SuppressWarnings("unchecked")
        final ScheduledFuture<ScheduledFuture<Object>> future = super.scheduleAtFixedRate(delegatingRunnable, startTime, period);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> scheduleAtFixedRate(Runnable task, long period) {
        final long additionalStartDelay = this.getAdditionalStartDelay();
        if (additionalStartDelay > 0) {
            //If there is an additional delay use the alternate call which includes a startTime 
            return this.scheduleAtFixedRate(task, new Date(), period);
        }
        
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        @SuppressWarnings("unchecked")
        final ScheduledFuture<ScheduledFuture<Object>> future = super.scheduleAtFixedRate(delegatingRunnable, period);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        startTime = getDelayedStartDate(startTime);
        
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        @SuppressWarnings("unchecked")
        final ScheduledFuture<ScheduledFuture<Object>> future = super.scheduleWithFixedDelay(delegatingRunnable, startTime, delay);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> scheduleWithFixedDelay(Runnable task, long delay) {
        final long additionalStartDelay = this.getAdditionalStartDelay();
        if (additionalStartDelay > 0) {
            //If there is an additional delay use the alternate call which includes a startTime 
            return this.scheduleWithFixedDelay(task, new Date(), delay);
        }
        
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(this.executorService, task);
        @SuppressWarnings("unchecked")
        final ScheduledFuture<ScheduledFuture<Object>> future = super.scheduleWithFixedDelay(delegatingRunnable, delay);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }
    
    

    private static class DelegatingRunnable implements Runnable {
        private final ExecutorService executorService;
        private final Runnable runnable;
        
        public DelegatingRunnable(ExecutorService executorService, Runnable runnable) {
            this.executorService = executorService;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            executorService.submit(this.runnable);
        }
    }
    
    private static class DelegatingCallable<T> implements Callable<Future<T>> {
        private final ExecutorService executorService;
        private final Callable<T> callable;

        public DelegatingCallable(ExecutorService executorService, Callable<T> callable) {
            this.executorService = executorService;
            this.callable = callable;
        }

        @Override
        public Future<T> call() throws Exception {
            return executorService.submit(this.callable);
        }
    }
    
    private static class DelegatingForwardingFuture<V> implements Future<V> {
        private final Future<? extends Future<V>> future;
        
        public DelegatingForwardingFuture(Future<? extends Future<V>> future) {
            this.future = future;
        }


        @Override
        public V get() throws InterruptedException, ExecutionException {
            return this.future.get().get();
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.future.get(timeout, unit).get(timeout, unit);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return this.future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return this.future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return this.future.isDone();
        }
    }
    
    private static class DelegatingForwardingScheduledFuture<V> extends DelegatingForwardingFuture<V> implements ScheduledFuture<V> {
        private final ScheduledFuture<ScheduledFuture<V>> future;
        
        public DelegatingForwardingScheduledFuture(ScheduledFuture<ScheduledFuture<V>> scheduledFuture) {
            super(scheduledFuture);
            this.future = scheduledFuture;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return this.future.getDelay(unit);
        }

        @Override
        public int compareTo(Delayed o) {
            return this.future.compareTo(o);
        }
    }
}
