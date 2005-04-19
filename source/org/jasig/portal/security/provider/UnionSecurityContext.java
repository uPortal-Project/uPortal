/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
            this.myPrincipal=(ChainingPrincipal)subCtx.getPrincipal();
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

