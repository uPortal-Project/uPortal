/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
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

package org.jasig.portal.security;

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
