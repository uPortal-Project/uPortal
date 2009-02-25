/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.jmx;

import java.util.Date;

public interface FrameworkMBean {
  public Date getStartedAt();

  public long getRenderAverage();
  public long getRenderHighMax();
  public long getRenderLast();
  public long getRenderMin();
  public long getRenderMax();
  public long getRenderTotalRenders();

  /* sessions */
  public long getUserSessionCount();
  public long getGuestSessionCount();

  public String[] getRecentProblems();

  /* Database information */
  public int getRDBMActiveConnectionCount();
  public int getRDBMMaxConnectionCount();
  public long getDatabaseAverage();
  public long getDatabaseHighMax();
  public long getDatabaseLast();
  public long getDatabaseMin();
  public long getDatabaseMax();
  public long getDatabaseTotalConnections();

  public long getAuthenticationAverage();
  public long getAuthenticationHighMax();
  public long getAuthenticationLast();
  public long getAuthenticationMin();
  public long getAuthenticationMax();
  public long getAuthenticationTotalLogins();

  // Threads
  public long getThreadCount();
  public long getChannelRendererActiveThreads();
  public long getChannelRendererMaxActiveThreads();
}
