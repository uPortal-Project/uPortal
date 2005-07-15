/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
 
package org.jasig.portal.services.stats;

/**
 * A factory that produces IStatsRecorder implementations.
 * 
 * This factory interface is deprecated because the preferred approach is to 
 * use Spring bean configuration to wire together your IStatsRecorder instances 
 * rather than relying upon custom factories.  Think of Spring as the ultimate general
 * factory.
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$ $Date$
 * @deprecated as of uP 2.5.1, factory not needed when using Spring to instantiate the IStatsRecorder implementation.
 */
public interface IStatsRecorderFactory {
  /**
   * Obtains the IStatsRecorderImplementation
   * @return statsRecorder, the IStatsRecorder implementation
   */
  public IStatsRecorder getStatsRecorder();
}
