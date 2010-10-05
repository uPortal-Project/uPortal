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

package org.jasig.portal.security.provider;

import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.spring.locator.LocalAccountDaoLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>This is an implementation of a SecurityContext that merely checks to see
 * if the user exists in the UP_USERS database table but otherwise presumes
 * to be pre-authenticated by the context from which it is called. The typical
 * system where this might be used is a portal whose main page is protected by
 * HTTP authentication (BASIC or otherwise).</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
class TrustSecurityContext extends ChainingSecurityContext
    implements ISecurityContext {
    
    private static final Log log = LogFactory.getLog(TrustSecurityContext.class);
    
  private final int TRUSTSECURITYAUTHTYPE = 0xFF01;


  TrustSecurityContext() {
    super();
  }


  public int getAuthType() {
    return  this.TRUSTSECURITYAUTHTYPE;
  }


  public synchronized void authenticate() throws PortalSecurityException {
    this.isauth = true;
    if (this.myPrincipal.UID != null) {
      try {
        String first_name, last_name;
        ILocalAccountDao accountStore = LocalAccountDaoLocator.getLocalAccountDao();
        ILocalAccountPerson account = accountStore.getPerson(this.myPrincipal.UID);
        if (account != null) {
            first_name = (String) account.getAttributeValue("given");
            last_name = (String) account.getAttributeValue("sn");
          this.myPrincipal.FullName = first_name + " " + last_name;
          if (log.isInfoEnabled())
              log.info( "User " + this.myPrincipal.UID + " is authenticated");
          this.isauth = true;
        }
        else {
            if (log.isInfoEnabled())
                log.info( "No such user: " + this.myPrincipal.UID);
        }
      } catch (Exception e) {
        PortalSecurityException ep = new PortalSecurityException("SQL Database Error");
        log.error(e,e);
        throw  (ep);
      }
    }
    else {
        log.error( "Principal not initialized prior to authenticate");
    }
    // Ok...we are now ready to authenticate all of our subcontexts.
    super.authenticate();
    return;
  }
}



