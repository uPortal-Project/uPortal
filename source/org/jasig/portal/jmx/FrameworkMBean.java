package org.jasig.portal.jmx;

import java.util.Date;
import org.jasig.portal.PortalSessionManager;
import java.util.HashMap;
import java.util.List;
import org.jasig.portal.PortalException;

public interface FrameworkMBean {
  public Date getStartedAt();

  public long getLastRender();

  public String[] getRecentProblems();

}
