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

package org.jasig.portal.jmx;

import java.util.Date;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.Authentication;
import org.jasig.portal.utils.MovingAverageSample;

/**
 * uPortal metrics to make available via JMX.
 *
 * @author George Lindholm <a href="mailto:George.Lindholm@ubc.ca">George.Lindholm@ubc.ca</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class FrameworkMBeanImpl implements FrameworkMBean {
    public FrameworkMBeanImpl() {

    }

    /**
     * Time/Data uPortal was started
     * @return Date
     */
    @Override
    public Date getStartedAt() {
        return new Date(0); //PortalSessionManager.STARTED_AT;
    }

    /*
     * Track framework rendering performance
     */
    @Override
    public long getRenderAverage() {
        return this.getLastRender().average;
    }

    @Override
    public long getRenderHighMax() {
        return this.getLastRender().highMax;
    }

    @Override
    public long getRenderLast() {
        return this.getLastRender().lastSample;
    }

    @Override
    public long getRenderMin() {
        return this.getLastRender().min;
    }

    @Override
    public long getRenderMax() {
        return this.getLastRender().max;
    }

    @Override
    public long getRenderTotalRenders() {
        return this.getLastRender().totalSamples;
    }

    public MovingAverageSample getLastRender() {
        return null;//StaticRenderingPipeline.getLastRenderSample();
    }

    /*
     * Track framework database performance
     */
    public MovingAverageSample getLastDatabase() {
        return RDBMServices.getLastDatabase();
    }

    @Override
    public long getDatabaseAverage() {
        return this.getLastDatabase().average;
    }

    @Override
    public long getDatabaseHighMax() {
        return this.getLastDatabase().highMax;
    }

    @Override
    public long getDatabaseLast() {
        return this.getLastDatabase().lastSample;
    }

    @Override
    public long getDatabaseMin() {
        return this.getLastDatabase().min;
    }

    @Override
    public long getDatabaseMax() {
        return this.getLastDatabase().max;
    }

    @Override
    public long getDatabaseTotalConnections() {
        return this.getLastDatabase().totalSamples;
    }

    @Override
    public int getRDBMActiveConnectionCount() {
        return RDBMServices.getActiveConnectionCount();
    }

    @Override
    public int getRDBMMaxConnectionCount() {
        return RDBMServices.getMaxConnectionCount();
    }

    /*
     * Track framework Authentication performance
     */
    public MovingAverageSample getLastAuthentication() {
        return Authentication.lastAuthentication;
    }

    @Override
    public long getAuthenticationAverage() {
        return Authentication.lastAuthentication.average;
    }

    @Override
    public long getAuthenticationHighMax() {
        return Authentication.lastAuthentication.highMax;
    }

    @Override
    public long getAuthenticationLast() {
        return Authentication.lastAuthentication.lastSample;
    }

    @Override
    public long getAuthenticationMin() {
        return Authentication.lastAuthentication.min;
    }

    @Override
    public long getAuthenticationMax() {
        return Authentication.lastAuthentication.max;
    }

    @Override
    public long getAuthenticationTotalLogins() {
        return Authentication.lastAuthentication.totalSamples;
    }

    // Threads
    @Override
    public long getThreadCount() {
        return -1;//PortalSessionManager.getThreadGroup().activeCount();
    }
}
