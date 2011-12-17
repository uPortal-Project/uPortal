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

import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.google.common.util.concurrent.ForwardingFuture;

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
    
    private ExecutorService executorService;
    
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        super.execute(delegatingRunnable, startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        return super.submit(delegatingRunnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        final DelegatingCallable<T> delegatingCallable = new DelegatingCallable<T>(task);
        final Future<Future<T>> future = super.submit(delegatingCallable);
        
        return new DelegatingForwardingFuture<T>(future);
    }

    @Override
    public void execute(Runnable task) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        super.execute(delegatingRunnable);        
    }

    @Override
    public ScheduledFuture<Object> schedule(Runnable task, Trigger trigger) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        final ScheduledFuture<ScheduledFuture<Object>> future = super.schedule(delegatingRunnable, trigger);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> schedule(Runnable task, Date startTime) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        final ScheduledFuture<ScheduledFuture<Object>> future = super.schedule(delegatingRunnable, startTime);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        final ScheduledFuture<ScheduledFuture<Object>> future = super.scheduleAtFixedRate(delegatingRunnable, startTime, period);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> scheduleAtFixedRate(Runnable task, long period) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        final ScheduledFuture<ScheduledFuture<Object>> future = super.scheduleAtFixedRate(delegatingRunnable, period);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        final ScheduledFuture<ScheduledFuture<Object>> future = super.scheduleWithFixedDelay(delegatingRunnable, startTime, delay);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    @Override
    public ScheduledFuture<Object> scheduleWithFixedDelay(Runnable task, long delay) {
        final DelegatingRunnable delegatingRunnable = new DelegatingRunnable(task);
        final ScheduledFuture<ScheduledFuture<Object>> future = super.scheduleWithFixedDelay(delegatingRunnable, delay);
        return new DelegatingForwardingScheduledFuture<Object>(future);
    }

    private class DelegatingRunnable implements Runnable {
        private final Runnable runnable;
        
        public DelegatingRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            executorService.submit(this.runnable);
        }
    }
    
    private class DelegatingCallable<T> implements Callable<Future<T>> {
        private final Callable<T> callable;

        public DelegatingCallable(Callable<T> callable) {
            this.callable = callable;
        }

        @Override
        public Future<T> call() throws Exception {
            return executorService.submit(this.callable);
        }
    }
    
    private static class DelegatingForwardingFuture<V> extends ForwardingFuture<V> {
        private final Future<? extends Future<V>> future;
        
        public DelegatingForwardingFuture(Future<? extends Future<V>> future) {
            this.future = future;
        }

        @Override
        protected Future<V> delegate() {
            try {
                return this.future.get();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException)cause;
                }
                if (cause != null) {
                    throw new RuntimeException(cause);
                }
                throw new RuntimeException(e);
            }
        }

        @Override
        public int hashCode() {
            return this.delegate().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.delegate().equals(obj);
        }
    }
    
    private static class DelegatingForwardingScheduledFuture<V> extends DelegatingForwardingFuture<V> implements ScheduledFuture<V> {
        private final ScheduledFuture<ScheduledFuture<V>> scheduledFuture;

        public DelegatingForwardingScheduledFuture(ScheduledFuture<ScheduledFuture<V>> scheduledFuture) {
            super(scheduledFuture);
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        protected ScheduledFuture<V> delegate() {
            final Future<V> delegate = super.delegate();
            return (ScheduledFuture<V>)delegate;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return this.delegate().getDelay(unit);
        }

        @Override
        public int compareTo(Delayed o) {
            return this.delegate().compareTo(o);
        }
    }
}
