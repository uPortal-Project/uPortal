package org.jasig.portal.jmx;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.ProblemsTable;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: uPortal metrics to make available to JMX </p>
 *
 * <p>Copyright: Copyright © 2005</p>
 *
 * <p>Company: </p>
 *
 * @author George Lindholm mailto:George.Lindholm@ubc.ca
 * @version 1.0
 */
public class FrameworkMBeanImpl implements FrameworkMBean {

  static private long lastRender;

  /**
   *
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
}
