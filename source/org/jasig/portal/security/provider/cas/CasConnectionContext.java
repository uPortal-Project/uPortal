/* Copyright 2004-2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider.cas;

import java.util.Enumeration;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.LocalConnectionContext;

/**
 * A LocalConnectionContext using the Central Authentication Service.
 * This connection context places CAS proxy tickets on the URLs it
 * processes.  It searches for and uses any security context implementing
 * ICasSecurityContext.  This means that it supports both the filtered and the
 * traditional CAS security context approaches and that this connection context
 * can be used with any other security context scheme implementing CAS-like
 * proxy ticket functionality if the corresponding security context implements
 * ICasSecurityContext.
 */
public class CasConnectionContext extends LocalConnectionContext {

    private ChannelStaticData staticData = null;
    
    private IPerson person = null;
    
    private ICasSecurityContext casSecurityContext = null;
    
    public void init(ChannelStaticData sd) {
        this.staticData = sd;
        this.person = sd.getPerson();
        
        ISecurityContext ic = this.person.getSecurityContext();
        if (ic instanceof ICasSecurityContext && ic.isAuthenticated())
            this.casSecurityContext = (ICasSecurityContext) ic;
        
        // loop through subcontexts to find implementations of
        // ICasSecurityContext
        Enumeration en = ic.getSubContexts();
        while (en.hasMoreElements()) {
            ISecurityContext sctx = (ISecurityContext) en.nextElement();
            if (sctx instanceof ICasSecurityContext && sctx.isAuthenticated())
                this.casSecurityContext = (ICasSecurityContext) sctx;
        }
        
        if (this.casSecurityContext == null)
            log.error("Unable to find authenticated ICasSecurityContext");
    }
    
    public String getDescriptor(String descriptor, ChannelRuntimeData rd) {
        if (log.isTraceEnabled()) {
            log.trace("getDescriptor(" + descriptor + ", " + rd + ")");
        }
        if (rd.getHttpRequestMethod().equals("GET")) {
            // get proxy service ticket for the service if needed
            String proxyTicket = null;
            if (this.casSecurityContext != null)
                try {
                    proxyTicket = this.casSecurityContext
                    .getCasServiceToken(descriptor);
                } catch (CasProxyTicketAcquisitionException casex) {
                    log.error(
                            "getDescriptor() - Error retreiving proxy ticket.",
                            casex);
                }
                
                // if the descriptor was null then at least return
                // the proxy service ticket as a unique string
                if (descriptor == null) {
                    return proxyTicket;
                }

                if (proxyTicket != null) {
                    // append ticket parameter and value to query string
                    if (descriptor.indexOf("?") != -1) {
                        descriptor = descriptor + "&ticket=" + proxyTicket;
                    } else {
                        descriptor = descriptor + "?ticket=" + proxyTicket;
                    }
                }
        }
        
        if (log.isTraceEnabled()) {
            log.trace("returning from getDescriptor() with [" + descriptor
                    + "]");
        }
        return descriptor;
        
    }
    
    /**
     * Returns url with proxy service ticket appended. Looks for static
     * parameter upc_cas_service_uri and uses that for service. If not
     * specified, uses the passed uri
     * 
     * @param descriptor
     *                The original descriptor.
     * @return descriptor with CAS proxy ticket parameter appended.
     */
    public String getDescriptor(String descriptor) {
        if (log.isTraceEnabled()) {
            log.trace("entering getDescriptor(" + descriptor + ")");
        }
        String proxyTicket = null;
        
        if (this.casSecurityContext != null) {
            try {
                // if no specified parameter for service, use target descriptor
                String casUri = this.staticData.getParameter("upc_cas_service_uri");
                
                if (casUri != null) {
                    proxyTicket = this.casSecurityContext.getCasServiceToken(casUri);
                } else {
                    proxyTicket = this.casSecurityContext.getCasServiceToken(descriptor);
                }
                
            } catch (CasProxyTicketAcquisitionException casex) {
                log
                .error(
                        "CasConnectionContext::getDescriptor() - Error retreiving proxy ticket.",
                        casex);
            }
        }

        if (proxyTicket != null) {
            // append ticket parameter and value to query string
            if (descriptor.indexOf("?") != -1) {
                descriptor = descriptor + "&ticket=" + proxyTicket;
            } else {
                descriptor = descriptor + "?ticket=" + proxyTicket;
            }
        }
        
        if (log.isTraceEnabled()){
            log.trace("returning from getDescriptor() with [" + descriptor + "]");
        }
        return descriptor;
    }
    
    /**
     * Get the "ticket={ticket}" parameter String given a ChannelRuntimeData
     * containing a parameter declaring the service for which a proxy ticket
     * is desired.  If the ChannelRuntimeData presents the parameter
     * "cw_xml", this method returns "ticket={ticket}" where {ticket} is a 
     * proxy ticket authenticating to the service specified by the cw_xml
     * ChannelRuntimeData parameter.  If the ChannelRuntimedata does not present
     * the "cw_xml" parameter, this method returns "ticket=null".
     * @param rd ChannelRuntimeData.
     * @return "ticket={ticket}" where {ticket} is a proxy ticket or is "null".
     */
    public String getPostData(ChannelRuntimeData rd) {
        /*
         * The design of this method might be improved by moving responsibility
         * for determining what URI is being accessed to the caller (the channel)
         * and taking a String argument identifying the service for which a
         * proxy ticket is desired, rather than making CasConnectionContext
         * aware of and bound to a particular ChannelRuntimeData parameter.
         */
        
        
        // get proxy service ticket for the service if needed
        String proxyTicket = null;
        if (this.casSecurityContext != null)
            try {
                String xmlUri = rd.getParameter("cw_xml");
                if (xmlUri == null) {
                    xmlUri = this.staticData.getParameter("cw_xml");
                }
                proxyTicket = this.casSecurityContext.getCasServiceToken(xmlUri);
            } catch (CasProxyTicketAcquisitionException casex) {
                log.error("sendLocalData() - Error retreiving proxy ticket.",
                        casex);
            }
            return ("ticket=" + proxyTicket);
    }
    
    public void sendLocalData(Object conParam, ChannelRuntimeData rd) {
      // CAS does not have anything to do here. Post data is handled by the
      // getPostData method instead of being sent separately in this method.
        return;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" staticData:");
        sb.append(this.staticData);
        sb.append(" person:");
        sb.append(this.person);
        sb.append(" casSecurityContext:");
        sb.append(this.casSecurityContext);
        return sb.toString();
    }
}
