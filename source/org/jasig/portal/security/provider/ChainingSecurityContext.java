/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.security.provider;

import org.jasig.portal.security.*;
import org.jasig.portal.Logger;
import org.jasig.portal.RdbmServices;
import java.util.*;
import java.sql.*;

/**
 * <p>This is the basic abstract class for all security contexts that should
 * chain to children security contexts.</p>
 *
 * @author Andrew Newman
 * @version $Revision$
 */

public abstract class ChainingSecurityContext implements SecurityContext {

  protected boolean isauth = false;
  protected Hashtable mySubContexts;
  protected ChainingPrincipal myPrincipal;
  protected ChainingOpaqueCredentials myOpaqueCredentials;
  protected ChainingAdditionalDescriptor myAdditionalDescriptor;

  public ChainingSecurityContext() {
    myPrincipal = new ChainingPrincipal();
    myOpaqueCredentials = new ChainingOpaqueCredentials();
    myAdditionalDescriptor = new ChainingAdditionalDescriptor();
    mySubContexts = new Hashtable();
  }

  public Principal getPrincipalInstance() {
    if (this.isauth)
      return new ChainingPrincipal();
    else
      return this.myPrincipal;
  }

  public OpaqueCredentials getOpaqueCredentialsInstance() {
    if (this.isauth)
      return new ChainingOpaqueCredentials();
    else
      return this.myOpaqueCredentials;
  }

  /**
   * We walk the chain of subcontext assigning principals and opaquecredentials
   * from the parent. Note that the contexts themselves should resist
   * actually performing the assignment if an assignment has already been made
   * to either the credentials or the UID.
   */

  public synchronized void authenticate() {
    int i;
    Enumeration e = mySubContexts.elements();

    while (e.hasMoreElements()) {
      SecurityContext sctx = (SecurityContext)e.nextElement();
      Principal sp = sctx.getPrincipalInstance();
      OpaqueCredentials op = sctx.getOpaqueCredentialsInstance();
      sp.setUID(this.myPrincipal.UID);
      op.setCredentials(this.myOpaqueCredentials.credentialstring);
      sctx.authenticate();
    }

    // Zero out the actual credentials

    for (i = 0; i < this.myOpaqueCredentials.credentialstring.length; i++)
      this.myOpaqueCredentials.credentialstring[i] = 0;
    myOpaqueCredentials.credentialstring = null;
    return;
  }

  public Principal getPrincipal() {
    if (this.isauth)
      return this.myPrincipal;
    else
      return null;
  }

  public OpaqueCredentials getOpaqueCredentials() {
    if (this.isauth)
      return this.myOpaqueCredentials;
    else
      return null;
  }

  public AdditionalDescriptor getAdditionalDescriptor() {
    if (this.isauth)
      return this.myAdditionalDescriptor;
    else
      return null;
  }

  public boolean isAuthenticated() {
    return this.isauth;
  }

  public synchronized SecurityContext getSubContext(String name) {
    SecurityContext c = (SecurityContext)mySubContexts.get(name);
    if (c == null)
      Logger.log(Logger.ERROR,
          new PortalSecurityException("No such subcontext: " + name));
    return c;
  }

  public synchronized Enumeration getSubContexts() {
    Enumeration e = mySubContexts.elements();
    return e;
  }

  public synchronized void addSubContext(String name, SecurityContext ctx) {
    if (mySubContexts.get(name) != null)
      Logger.log(Logger.ERROR,
          new PortalSecurityException("Subcontext already exists: " + name));
    else
      mySubContexts.put(name, ctx);
    return;
  }


  // I 'spose the public class could just implement all of these interfaces
  // but I prefer member classes. -ADN

  protected class ChainingPrincipal implements Principal {
    protected String UID;
    protected String FullName;

    public String getUID() {
      return this.UID;
    }

    public String getGlobalUID() {
      return this.UID;
    }

    // This is supposed to be the person's "human readable" name. We should
    // probably do an account lookup at the very least to return this.

    public String getFullName() {
      return this.FullName;
    }

    public void setUID(String UID) {
      if (this.UID == null)
        this.UID = UID;
    }
  }

  protected class ChainingOpaqueCredentials implements OpaqueCredentials {

    protected byte[] credentialstring;

    // Since we want to explicitly zero our credentials after authenticate,
    // copy the credentials here in case a sub-authenticator doesn't want
    // to perform the operation immediately.

    public void setCredentials(byte[] credentials) {
      int i;

      if (this.credentialstring == null) {
        this.credentialstring = new byte[credentials.length];
        for (i = 0; i < credentials.length; i++)
          this.credentialstring[i] = credentials[i];
      }
    }

    public void setCredentials(String credentials) {
      if (this.credentialstring == null)
        setCredentials(credentials.getBytes());
    }
  }

  protected class ChainingAdditionalDescriptor implements AdditionalDescriptor {
  }

}