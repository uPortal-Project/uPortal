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

package org.jasig.portal.security.provider;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import org.jasig.portal.PropertiesManager;
import org.jasig.portal.security.IAdditionalDescriptor;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPrincipal;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.services.LogService;

/**
 * <p>This is the basic abstract class for all security contexts that should
 * chain to children security contexts.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 * @author Don Fracapane (df7@columbia.edu)
 * Added a new method named getSubContextNames() that returns an Enumeration of names
 * for the subcontexts.
 */
public abstract class ChainingSecurityContext implements ISecurityContext
{
  protected static boolean stopWhenAuthenticated = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.security.provider.ChainingSecurityContext.stopWhenAuthenticated");
  protected boolean isauth = false;
  protected Vector mySubContexts;
  protected ChainingPrincipal myPrincipal;
  protected ChainingOpaqueCredentials myOpaqueCredentials;
  protected IAdditionalDescriptor myAdditionalDescriptor;
  protected Comparator myOrder;

  public ChainingSecurityContext() {
    myPrincipal = new ChainingPrincipal();
    myOpaqueCredentials = new ChainingOpaqueCredentials();
    myAdditionalDescriptor = new ChainingAdditionalDescriptor();
    mySubContexts = new Vector();
  }

  public IPrincipal getPrincipalInstance() {
    if (this.isauth)
      return new ChainingPrincipal();
    else
      return this.myPrincipal;
  }

  public IOpaqueCredentials getOpaqueCredentialsInstance() {
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

  public synchronized void authenticate()  throws PortalSecurityException {
    int i;
    Enumeration e = mySubContexts.elements();
    boolean error = false;

    while (e.hasMoreElements()) {
      ISecurityContext sctx = ((Entry) e.nextElement()).getCtx();
      // The principal and credential are now set for all subcontexts in Authentication
      try {
        sctx.authenticate();
      } catch (Exception ex) {
      	error = true;
        LogService.log(LogService.ERROR, ex);
      }
      // Stop attempting to authenticate if authenticated and if the property flag is set
      if(stopWhenAuthenticated && sctx.isAuthenticated()) {
        break;
      }
    }

    // Zero out the actual credentials if it isn't already null
    if (this.myOpaqueCredentials.credentialstring != null){
       for (i = 0; i < this.myOpaqueCredentials.credentialstring.length; i++)
         this.myOpaqueCredentials.credentialstring[i] = 0;
       myOpaqueCredentials.credentialstring = null;
    }
    if (error && !this.isauth) throw new PortalSecurityException("One of the security subcontexts threw an exception");
    return;
  }

  public IPrincipal getPrincipal() {
    if (this.isauth)
      return this.myPrincipal;
    else
      return null;
  }

  public IOpaqueCredentials getOpaqueCredentials() {
    if (this.isauth)
      return this.myOpaqueCredentials;
    else
      return null;
  }

  public IAdditionalDescriptor getAdditionalDescriptor() {
    if (this.isauth)
      return this.myAdditionalDescriptor;
    else
      return null;
  }

  public boolean isAuthenticated() {
    return this.isauth;
  }

  public synchronized ISecurityContext getSubContext(String name) {
    for (int i = 0; i < mySubContexts.size(); i++)
    {
      Entry entry = (Entry) mySubContexts.get(i);
      if (entry.getKey() != null && entry.getKey().equals(name))
      {
        return(entry.getCtx());
      }
    }
    PortalSecurityException ep = new PortalSecurityException("No such subcontext: " + name);
    LogService.log(LogService.DEBUG,ep);
    return(null);
  }

  public synchronized boolean doesSubContextExist(String name) {
    for (int i = 0; i < mySubContexts.size(); i++)
    {
      Entry entry = (Entry)mySubContexts.get(i);
      if (entry.getKey() != null && entry.getKey().equals(name))
      {
        return(true);
      }
    }
    return(false);
  }

  // Return an enumeration of subcontexts by running the vector and
  // creating the enumeration.  All this so the subcontexts will
  // be returned in the order they appeared in the properties file.
  public synchronized Enumeration getSubContexts() {
    Enumeration e = mySubContexts.elements();
    class Adapter implements Enumeration {
        Enumeration base;
        public Adapter(Enumeration e) {
            this.base = e;
        }
        public boolean hasMoreElements() {
           return base.hasMoreElements();
        }
        public Object nextElement() {
           return ((Entry) base.nextElement()).getCtx();
        }
    }
    return new Adapter(e);
  }

  public synchronized void addSubContext(String name, ISecurityContext ctx)
    throws PortalSecurityException {
    // Make sure the subcontext does not already exist in the chain
    if(doesSubContextExist(name))
    {
      PortalSecurityException ep = new PortalSecurityException("Subcontext already exists: " + name);
      LogService.log(LogService.ERROR,ep);
      throw(ep);
    }
    else
    {
      mySubContexts.add(new Entry(name, ctx));
    }
  }


  // I 'spose the public class could just implement all of these interfaces
  // but I prefer member classes. -ADN

  protected class ChainingPrincipal implements IPrincipal {
    protected String globalUID;
    protected String UID;
    protected String FullName;

    public String getUID() {
      return this.UID;
    }

    public String getGlobalUID() {
      return this.globalUID;
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

  protected class ChainingOpaqueCredentials implements IOpaqueCredentials {

    public byte[] credentialstring;

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
      if (this.credentialstring == null && credentials!=null)
        setCredentials(credentials.getBytes());
    }
  }

  // Returns an Enumeration of the names of the subcontexts.
  public synchronized Enumeration getSubContextNames() {
   Vector scNames = new Vector();
   for (int i = 0; i < mySubContexts.size(); i++)
   {
      Entry entry = (Entry) mySubContexts.get(i);
      if (entry.getKey() != null){
         scNames.add(entry.getKey());
       }
   }
   return scNames.elements();
  }


  protected class ChainingAdditionalDescriptor implements IAdditionalDescriptor {
  }

// entries in our subcontext list
  private static class Entry implements Serializable {
    String key;
    ISecurityContext ctx;
    public Entry(String key, ISecurityContext ctx) {
        this.key = key;
        this.ctx = ctx;
    }
    public ISecurityContext getCtx() {
        return this.ctx;
    }
    public String getKey() {
        return this.key;
    }
  }
}
