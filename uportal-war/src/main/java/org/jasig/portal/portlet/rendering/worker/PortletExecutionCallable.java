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

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Used by {@link PortletExecutionWorker} to submit to the thread pool
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <V>
 */
final class PortletExecutionCallable<V> extends FutureTask<V> implements Callable<V>, IPortletExecutionContext {
    private final Callable<V> callable;
    private final IPortletExecutionContext portletExecutionContext;

    public PortletExecutionCallable(Callable<V> callable, IPortletExecutionContext portletExecutionContext) {
        super(callable);
        this.callable = callable;
        this.portletExecutionContext = portletExecutionContext;
    }

    @Override
    public ExecutionType getExecutionType() {
        return this.portletExecutionContext.getExecutionType();
    }

    @Override
    public Object setExecutionAttribute(String name, Object value) {
        return this.portletExecutionContext.setExecutionAttribute(name, value);
    }

    @Override
    public Object getExecutionAttribute(String name) {
        return this.portletExecutionContext.getExecutionAttribute(name);
    }

    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletExecutionContext.getPortletWindowId();
    }

    @Override
    public String getPortletFname() {
        return this.portletExecutionContext.getPortletFname();
    }

    @Override
    public boolean isSubmitted() {
        return this.portletExecutionContext.isSubmitted();
    }

    @Override
    public boolean isStarted() {
        return this.portletExecutionContext.isStarted();
    }

    @Override
    public boolean isComplete() {
        return this.portletExecutionContext.isComplete();
    }

    @Override
    public long getSubmittedTime() {
        return this.portletExecutionContext.getSubmittedTime();
    }

    @Override
    public long getStartedTime() {
        return this.portletExecutionContext.getStartedTime();
    }

    @Override
    public long getCompleteTime() {
        return this.portletExecutionContext.getCompleteTime();
    }

    @Override
    public long getWait() {
        return this.portletExecutionContext.getWait();
    }

    @Override
    public long getDuration() {
        return this.portletExecutionContext.getDuration();
    }

    @Override
    public long getApplicableTimeout() {
        return this.portletExecutionContext.getApplicableTimeout();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public V call() throws Exception {
        return this.callable.call();
    }

}