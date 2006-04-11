/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.utils.threading.BaseTask;

/**
 * Base class for all stats recorder worker tasks to extend.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated IStatsRecorder implementation is replaced with a much more flexible system 
 * based on the Spring ApplicationEventPublisher and Event Listeners. 
 * For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public abstract class StatsRecorderWorkerTask extends BaseTask {
  
  protected IStatsRecorder statsRecorder;

  /**
   * Does the real work of the worker task
   */  
  public abstract void execute() throws Exception;
  
  /**
   * Set the stats reocorder that this task should use
   * @param statsRecorder the stats recorder that this task should use
   */  
  public void setStatsRecorder(IStatsRecorder statsRecorder) {
    this.statsRecorder = statsRecorder;
  }
}



