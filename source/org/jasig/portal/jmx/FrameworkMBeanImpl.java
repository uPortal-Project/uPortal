/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.jmx;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.ProblemsTable;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.rdbm.IDatabaseServer;

/**
 *
 * <p>Title: FrameworkMBeanImpl</p>
 *
 * <p>Description: uPortal metrics to make available to JMX </p>
 *
 * <p>Copyright: Copyright © 2005</p>
 *
 * <p>Company: </p>
 *
 * @author George Lindholm <a href="mailto:George.Lindholm@ubc.ca">George.Lindholm@ubc.ca</a>
 * @version 1.0
 */
public class FrameworkMBeanImpl implements FrameworkMBean {

  private static long lastRender;
  private static String database;

  public FrameworkMBeanImpl() {
    database = RDBMServices.getDatabaseServer().toString();
  }
  /**
   * Time/Data uPortal was started
   * @return Date
   */
  public Date getStartedAt() {
    return PortalSessionManager.startedAt;
  }

  public long getLastRender() {
    return lastRender;
  }

  public static void setLastRender(final long time) {
    lastRender = time;
  }

  public String[] getRecentProblems() {
    final List rpe = ProblemsTable.getRecentPortalExceptions();
    final ArrayList al = new ArrayList(rpe.size());
    for (Iterator it = rpe.iterator(); it.hasNext(); ) {
      final PortalException pe = (PortalException) it.next();
      al.add(pe.getMessage());
    }
    return (String[]) al.toArray(new String[0]);
  }

  /* Database */
  public int getRDBMActiveConnectionCount() {
    return RDBMServices.getActiveConnectionCount();

  }
  public String getRDBMDatabase() {
    return database;
  }

}
