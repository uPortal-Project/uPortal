/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Produces an implementation of IChannelRegistryStore
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ChannelRegistryStoreFactory {
    
    private static final Log log = LogFactory.getLog(ChannelRegistryStoreFactory.class);
    
  private static IChannelRegistryStore channelRegistryStoreImpl = null;

  static {
    // Retrieve the class name of the concrete IChannelRegistryStore implementation
    String className = PropertiesManager.getProperty("org.jasig.portal.ChannelRegistryStoreFactory.implementation", null);
    
    // Fail if this is not found
    if (className == null)
      log.error( "ChannelRegistryStoreFactory: org.jasig.portal.ChannelRegistryStoreFactory.implementation must be specified in portal.properties");
    try {
      // Create an instance of the IChannelRegistryStore as specified in portal.properties
      channelRegistryStoreImpl = (IChannelRegistryStore)Class.forName(className).newInstance();
    } catch (Exception e) {
      log.error( "ChannelRegistryStoreFactory: Could not instantiate " + className, e);
    }
  }

  /**
   * Returns an instance of the IChannelRegistryStore specified in portal.properties
   * @return an IChannelRegistryStore implementation
   */
  public static IChannelRegistryStore getChannelRegistryStoreImpl() {
    return channelRegistryStoreImpl;
  }
}



