/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
    String className = PropertiesManager.getProperty("org.jasig.portal.ChannelRegistryStoreFactory.implementation");
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



