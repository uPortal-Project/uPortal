/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.jmx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jasig.portal.ChannelManager;
import org.jasig.portal.GuestUserInstance;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.ProblemsTable;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.UserInstance;
import org.jasig.portal.rendering.StaticRenderingPipeline;
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
        return PortalSessionManager.STARTED_AT;
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
        return StaticRenderingPipeline.getLastRenderSample();
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
        return RDBMServices.lastDatabase;
    }

    public long getDatabaseAverage() {
        return RDBMServices.lastDatabase.average;
    }

    public long getDatabaseHighMax() {
        return RDBMServices.lastDatabase.highMax;
    }

    public long getDatabaseLast() {
        return RDBMServices.lastDatabase.lastSample;
    }

    public long getDatabaseMin() {
        return RDBMServices.lastDatabase.min;
    }

    public long getDatabaseMax() {
        return RDBMServices.lastDatabase.max;
    }

    public long getDatabaseTotalConnections() {
        return RDBMServices.lastDatabase.totalSamples;
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
        return PortalSessionManager.getThreadGroup().activeCount();
    }

    public long getChannelRendererActiveThreads() {
        return ChannelManager.getActiveRenderers();
    }

    public long getChannelRendererMaxActiveThreads() {
        return ChannelManager.getMaxRenderThreads();
    }
}
