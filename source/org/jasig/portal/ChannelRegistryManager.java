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

package  org.jasig.portal;

import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SmartCache;
import org.w3c.dom.Document;
import java.sql.SQLException;

/**
 * Manages the channel registry which is a listing of published channels
 * that one can subscribe to (add to their layout).
 * Also currently manages the channel types data. (maybe this should be managed
 * by another class  -Ken)
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class ChannelRegistryManager {
  protected static final IChannelRegistryStore chanRegStore = RdbmServices.getChannelRegistryStoreImpl();
  protected static final int registryCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.channel_registry_cache_timeout");
  protected static final int chanTypesCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.channel_types_cache_timeout");
  protected static final SmartCache channelRegistryCache = new SmartCache(registryCacheTimeout);
  protected static final SmartCache channelTypesCache = new SmartCache(chanTypesCacheTimeout);
  
  /**
   * Returns the channel registry as a Document.  This list is not filtered by roles.
   * @return the channel registry as a Document
   */
  public static Document getChannelRegistry() throws PortalException {
    Document channelRegistry = (Document)channelRegistryCache.get("channelRegistry");
    if (channelRegistry == null)
    {
      // Channel registry has expired, so get it and cache it
      try {
        channelRegistry = chanRegStore.getChannelRegistryXML();
      } catch (Exception e) {
        throw new GeneralRenderingException(e.getMessage());
      }

      if (channelRegistry != null)
      {
        channelRegistryCache.put("channelRegistry", channelRegistry);
        LogService.instance().log(LogService.INFO, "Caching channel registry.");
      }
    }
    return channelRegistry;    
  }     
  
  /**
   * Returns the publishable channel types as a Document.
   * @return a list of channel types as a Document
   */
  public static Document getChannelTypes() throws PortalException {
    String key = "channelTypes";
    Document channelTypes = (Document)channelTypesCache.get(key);
    if (channelTypes == null)
    {
      // Channel types doc has expired, so get it and cache it
      try {
        channelTypes = chanRegStore.getChannelTypesXML();
      } catch (Exception e) {
        throw new GeneralRenderingException(e.getMessage());
      }

      if (channelTypes != null)
      {
        channelTypesCache.put(key, channelTypes);
        LogService.instance().log(LogService.INFO, "Caching channel types.");
      }
    }
    return channelTypes;    
  }           
}






