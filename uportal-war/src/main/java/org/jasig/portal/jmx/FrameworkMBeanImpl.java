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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jasig.portal.GuestUserInstance;
import org.jasig.portal.PortalException;
import org.jasig.portal.ProblemsTable;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.UserInstance;
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
    public Date getStartedAt() {
        return new Date(0); //PortalSessionManager.STARTED_AT;
    }

    /*
     * Track framework rendering performance
     */
    public long getRenderAverage() {
        return getLastRender().average;
    }

    public long getRenderHighMax() {
        return getLastRender().highMax;
    }

    public long getRenderLast() {
        return getLastRender().lastSample;
    }

    public long getRenderMin() {
        return getLastRender().min;
    }

    public long getRenderMax() {
        return getLastRender().max;
    }

    public long getRenderTotalRenders() {
        return getLastRender().totalSamples;
    }

    public MovingAverageSample getLastRender() {
        return null;//StaticRenderingPipeline.getLastRenderSample();
    }

    public String[] getRecentProblems() {
        final List<PortalException> rpe = ProblemsTable.getRecentPortalExceptions();
        final ArrayList<String> al = new ArrayList<String>(rpe.size());
        for (final PortalException pe : rpe) {
            al.add(pe.getMessage());
        }
        return al.toArray(new String[al.size()]);
    }

    /*
     * sessions
     */
    public long getUserSessionCount() {
        return UserInstance.getUserSessions();
    }

    public long getGuestSessionCount() {
        return GuestUserInstance.getGuestSessions();
    }

    /*
     * Track framework database performance
     */
    public MovingAverageSample getLastDatabase() {
        return RDBMServices.getLastDatabase();
    }

    public long getDatabaseAverage() {
        return getLastDatabase().average;
    }

    public long getDatabaseHighMax() {
        return getLastDatabase().highMax;
    }

    public long getDatabaseLast() {
        return getLastDatabase().lastSample;
    }

    public long getDatabaseMin() {
        return getLastDatabase().min;
    }

    public long getDatabaseMax() {
        return getLastDatabase().max;
    }

    public long getDatabaseTotalConnections() {
        return getLastDatabase().totalSamples;
    }

    public int getRDBMActiveConnectionCount() {
        return RDBMServices.getActiveConnectionCount();
    }
    
    public int getRDBMMaxConnectionCount() {
        return RDBMServices.getMaxConnectionCount();
    }

    /*
     * Track framework Authentication performance
     */
    public MovingAverageSample getLastAuthentication() {
        return Authentication.lastAuthentication;
    }

    public long getAuthenticationAverage() {
        return Authentication.lastAuthentication.average;
    }

    public long getAuthenticationHighMax() {
        return Authentication.lastAuthentication.highMax;
    }

    public long getAuthenticationLast() {
        return Authentication.lastAuthentication.lastSample;
    }

    public long getAuthenticationMin() {
        return Authentication.lastAuthentication.min;
    }

    public long getAuthenticationMax() {
        return Authentication.lastAuthentication.max;
    }

    public long getAuthenticationTotalLogins() {
        return Authentication.lastAuthentication.totalSamples;
    }

    // Threads
    public long getThreadCount() {
        return -1;//PortalSessionManager.getThreadGroup().activeCount();
    }
}
