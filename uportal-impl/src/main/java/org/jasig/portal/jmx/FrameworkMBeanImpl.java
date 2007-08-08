/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.jmx;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jasig.portal.ChannelManager;
import org.jasig.portal.GuestUserInstance;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
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
  public Date getStartedAt() {return PortalSessionManager.STARTED_AT;}


  /*
   * Track framework rendering performance
   */
  public long getRenderAverage() {return UserInstance.lastRender.average;}
  public long getRenderHighMax() {return UserInstance.lastRender.highMax;}
  public long getRenderLast() {return UserInstance.lastRender.lastSample;}
  public long getRenderMin() {return UserInstance.lastRender.min;}
  public long getRenderMax() {return UserInstance.lastRender.max;}
  public long getRenderTotalRenders() {return UserInstance.lastRender.totalSamples;}
  public MovingAverageSample getLastRender() {return UserInstance.lastRender;}


   public String[] getRecentProblems() {
    final List rpe = ProblemsTable.getRecentPortalExceptions();
    final ArrayList al = new ArrayList(rpe.size());
    for (Iterator it = rpe.iterator(); it.hasNext(); ) {
      final PortalException pe = (PortalException) it.next();
      al.add(pe.getMessage());
    }
    return (String[]) al.toArray(new String[0]);
  }

  /*
   * sessions
   */
  public long getUserSessionCount() {return UserInstance.userSessions.longValue();}
  public long getGuestSessionCount() {return GuestUserInstance.guestSessions.longValue();}

  /*
   * Track framework database performance
   */
  public MovingAverageSample getLastDatabase() { return RDBMServices.lastDatabase;}
  public long getDatabaseAverage() {return RDBMServices.lastDatabase.average;}
  public long getDatabaseHighMax() {return RDBMServices.lastDatabase.highMax;}
  public long getDatabaseLast() {return RDBMServices.lastDatabase.lastSample;}
  public long getDatabaseMin() {return RDBMServices.lastDatabase.min;}
  public long getDatabaseMax() {return RDBMServices.lastDatabase.max;}
  public long getDatabaseTotalConnections() {return RDBMServices.lastDatabase.totalSamples;}
  public int getRDBMActiveConnectionCount() {return RDBMServices.getActiveConnectionCount();}
  public int getRDBMMaxConnectionCount() {return RDBMServices.getMaxConnectionCount();}


  /*
   * Track framework Authentication performance
   */
  public MovingAverageSample getLastAuthentication() {return Authentication.lastAuthentication;}
  public long getAuthenticationAverage() {return Authentication.lastAuthentication.average;}
  public long getAuthenticationHighMax() {return Authentication.lastAuthentication.highMax;}
  public long getAuthenticationLast() {return Authentication.lastAuthentication.lastSample;}
  public long getAuthenticationMin() {return Authentication.lastAuthentication.min;}
  public long getAuthenticationMax() {return Authentication.lastAuthentication.max;}
  public long getAuthenticationTotalLogins() {return Authentication.lastAuthentication.totalSamples;}


  // Threads
  public long getThreadCount() {return PortalSessionManager.getThreadGroup().activeCount();}
  public long getChannelRendererActiveThreads() {return ChannelManager.activeRenderers.get();}
  public long getChannelRendererMaxActiveThreads() {return ChannelManager.maxRenderThreads.get();}
}
