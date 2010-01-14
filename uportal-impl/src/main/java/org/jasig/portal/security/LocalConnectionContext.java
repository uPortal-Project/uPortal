/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;

/**
 * <p>LocalConnectionContext allows channels to tailor backend connections
 * to reflect local policy and implementation.  Connections are provided
 * as Objects: they may be URL, LDAP, database connections, etc.</p>
 *
 * <p>Channels using this need a way to determine which implementation to
 * use.  A standard way to do this is with a static data parameter   
 * 'upc_localConnContext', the value being the name of the implementing
 * class.  The default implementation handles the case where there are
 * no local changes to standard behaviour.</p>
 *
 * @author Sarah Arnott, sarnott@mun.ca
 * @author Andrew Draskoy, andrew@mun.ca
 * @version $Revision$
 */
public abstract class LocalConnectionContext
{
  protected ChannelStaticData staticData;
  
  protected final Log log = LogFactory.getLog(getClass());

  /**
   * Initialize	LocalConnectionContext by setting static data.
   * Parameters	may be passed to the implementing class	via static
   * data (which includes access to IPerson).  Must always be called
   * before sendLocalData.
   *
   * @param sd The calling channel's ChannelStaticData.
   */
  public void init (ChannelStaticData sd)
  {
    staticData = sd;
  }

  /**
   * Returns a descriptor such as a URL for opening a connection
   * to the backend application.  The descriptor should	be modified
   * as	necessary, for example modifying the a URL to include new
   * query string parameters.
   *
   * @param descriptor The original descriptor.
   * @param rd The calling channel's ChannelRuntimeData.
   */
   public String getDescriptor(String descriptor, ChannelRuntimeData rd)
   {
     return descriptor;
   }

  /**
   * Send any per-connection local data to the backend application.
   * E.g. headers in an http POST request. The default implementation
   * does nothing.
   *
   * @param connection The connection Object to the backend application
   *                (ie. HttpURLConnection, DirContext).
   * @param rd The calling channel's ChannelRuntimeData.
   */
   public void sendLocalData(Object connection, ChannelRuntimeData rd)
   {
   }

}
