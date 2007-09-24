/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider.cas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.security.IAdditionalDescriptor;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPrincipal;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;

/**
 * A mock-object CAS security context.
 * This mock object can be configured to mock-out most CAS security context
 * behaviors and is useful for testing CasConnectionContext (and potentially
 * other components making use of ICasSecurityContexts).
 * 
 * This mock object is not threadsafe.
 */
public class CasSecurityContextMock 
    implements ISecurityContext,
    ICasSecurityContext {

    private static final long serialVersionUID = 1L;
    
    private boolean authenticated = false;
    
    /**
     * A List of String service tokens and/or CasProxyTicketAcquisitionExceptions
     * and/or RuntimeExceptions which programs the behavior of this object's
     * response to getCasServiceToken().  For any call of getCasServiceToken(),
     * this object will respond by returning the first object in this List if
     * it is a String or null, or throwing the first object in this list if 
     * it is a CasProxyTicketAcquisitionException or a RuntimeException.  
     * This object will then remove the first item in this list and append it to
     * vendedServiceTokens.  If this list is empty, this object will treat it
     * as if it had contained null.
     */
    private List serviceTokensToVend = new ArrayList();
    
    /**
     * A List of responses from getCasServiceToken(), in the order given.
     */
    private final List vendedServiceTokens = new ArrayList();
    
    /**
     * A List of the String targets for which service tokens were requested.
     */
    private final List serviceTokenTargets = new ArrayList();
    
    /**
     * Map from subcontext name to ISecurityContext value.
     */
    private Map subcontexts = new HashMap();
    
    
    public String getCasServiceToken(String target) throws CasProxyTicketAcquisitionException {
        
        // record the target
        this.serviceTokenTargets.add(target);
        
        // return or throw the first item in serviceTokensToVend.
        
        Object toVend = null;
        
        if (! serviceTokensToVend.isEmpty() ) {
            toVend = this.serviceTokensToVend.get(0);
            this.serviceTokensToVend.remove(0);
        }
        
        // if the list was empty, toVend is still null, as desired.
        
        // add the response to the end of the list
        this.vendedServiceTokens.add(toVend);
        
        // return or throw the response
        if (toVend == null) {
            return null;
        } else if (toVend instanceof CasProxyTicketAcquisitionException) {
            throw (CasProxyTicketAcquisitionException) toVend;
        } else if (toVend instanceof RuntimeException) {
            throw (RuntimeException) toVend;
        } else if (toVend instanceof String) {
            return (String) toVend;
        } else {
            throw new IllegalStateException("Unexpected type: " + toVend);
        }
        
    }

    public int getAuthType() {
        return ICasSecurityContext.CAS_AUTHTYPE;
    }

    public IPrincipal getPrincipalInstance() {
        // TODO Auto-generated method stub
        return null;
    }

    public IOpaqueCredentials getOpaqueCredentialsInstance() {
        // TODO Auto-generated method stub
        return null;
    }

    public void authenticate() throws PortalSecurityException {
        // TODO Auto-generated method stub
        
    }

    public IPrincipal getPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    public IOpaqueCredentials getOpaqueCredentials() {
        // TODO Auto-generated method stub
        return null;
    }

    public IAdditionalDescriptor getAdditionalDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public ISecurityContext getSubContext(String ctx) throws PortalSecurityException {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getSubContexts() {
        return Collections.enumeration(this.subcontexts.values());
    }

    public Enumeration getSubContextNames() {
        return Collections.enumeration(this.subcontexts.keySet());
    }

    public void addSubContext(String name, ISecurityContext ctx) throws PortalSecurityException {
        this.subcontexts.put(name, ctx);
    }

    public List getServiceTokensToVend() {
        return serviceTokensToVend;
    }

    public void setServiceTokensToVend(List serviceTokensToVendArg) {
        
        List defensiveCopy = new ArrayList();
        defensiveCopy.addAll(serviceTokensToVendArg);
        
        this.serviceTokensToVend = defensiveCopy;
    }

    public List getVendedServiceTokens() {
        return vendedServiceTokens;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public List getServiceTokenTargets() {
        return serviceTokenTargets;
    }

}
