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

import java.util.Enumeration;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;

/**
 * <p>A simple extension of ChainingSecurityContext that acts merely as a placeholder
 * but considers itself in an "authenticated" state if any of its subcontexts
 * are "authenticated."</p>
 *
 * @author Shawn Bayern
 * @version $Revision$ $Date$
 */
class UnionSecurityContext extends ChainingSecurityContext {
  private final int UNION_SECURITY_AUTHTYPE = 0xFF0A;

  public int getAuthType() {
    return this.UNION_SECURITY_AUTHTYPE;
  }

  public synchronized void authenticate() throws PortalSecurityException {
  // lets chaining invoke authetication on all subcontexts
  // then sets resulting principal, descriptor and isauth based on
  // first authenticated context.

      super.authenticate();

      Enumeration e = getSubContexts();
      while (e.hasMoreElements()) {
        ISecurityContext subCtx = (ISecurityContext) e.nextElement();
        if (subCtx.isAuthenticated()) {
            this.myPrincipal = new ChainingPrincipal(subCtx.getPrincipal());
            this.myAdditionalDescriptor=subCtx.getAdditionalDescriptor();
            this.isauth=true;
            break;
            }
        }
  }

  public String toString(){
      StringBuffer sb = new StringBuffer();
      sb.append(this.getClass().getName());
      sb.append(" principal:").append(this.myPrincipal);
      sb.append(" additionalDescriptor:").append(this.myAdditionalDescriptor);
      sb.append(" isAuth:").append(this.isauth);
      sb.append(this.mySubContexts);
      return sb.toString();
  }

}

