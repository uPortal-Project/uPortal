/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.jmx;

import java.util.Date;
import org.jasig.portal.PortalSessionManager;
import java.util.HashMap;
import java.util.List;
import org.jasig.portal.PortalException;

public interface FrameworkMBean {
  public Date getStartedAt();

  public long getRenderAverage();
  public long getRenderHighMax();
  public long getRenderLast();
  public long getRenderMin();
  public long getRenderMax();
  public long getRenderTotalRenders();

  public String[] getRecentProblems();

  /* Database information */
  public int getRDBMActiveConnectionCount();
  public String getRDBMDatabase();
}
