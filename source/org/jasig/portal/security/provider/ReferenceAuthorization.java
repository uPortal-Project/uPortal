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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.security.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

import org.jasig.portal.security.IAuthorization;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SmartCache;


/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 * @deprecated As of uPortal 2.0, replaced by {@link AuthorizationImpl}
 */
public class ReferenceAuthorization implements IAuthorization {
  // Clear the caches every 10 seconds
  protected static SmartCache userRolesCache = new SmartCache(300);
  protected static SmartCache chanRolesCache = new SmartCache(300);
  protected static String s_channelPublisherRole = null;
  static {
  	InputStream stream = null;
    try {
      // Find our properties file and open it
      stream = ReferenceAuthorization.class.getResourceAsStream("/properties/security.properties");
      Properties securityProps = new Properties();
      securityProps.load(stream);
      s_channelPublisherRole = securityProps.getProperty("channelPublisherRole");
      } catch (IOException e) {
        LogService.log(LogService.ERROR, new PortalSecurityException(e.getMessage()));
      } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
      } finally {
		try {
			if (stream != null)
				stream.close();
		} catch (IOException exception) {
			LogService.log(LogService.ERROR,"ReferenceAuthorization:static::unable to close InputStream "+ exception);
		}
	}
  }


  // For the subscribe mechanism to use
  public Vector getAuthorizedChannels (IPerson person) {
    if (person == null || person.getID() == -1) {
      // Possibly throw security exception
      return  (null);
    }
    return  (new Vector());
  }
}



