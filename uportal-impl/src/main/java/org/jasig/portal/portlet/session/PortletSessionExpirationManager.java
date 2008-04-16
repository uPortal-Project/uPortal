/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletWindowID;
import org.apache.pluto.spi.optional.PortletInvocationEvent;
import org.apache.pluto.spi.optional.PortletInvocationListener;
import org.jasig.portal.spring.web.context.support.HttpSessionDestroyedEvent;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * After each request processed by a portlet the portlets session (if one exists) is stored in a Map in the Portal's
 * session. When a portal session is invalidated the {@link PortletSession#invalidate()} method is called on all portlet
 * sessions in the Map.
 * 
 * TODO this may not play well with distributed sessions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletSessionExpirationManager implements PortletInvocationListener, ApplicationListener {
    public static final String PORTLET_SESSIONS_MAP = PortletSessionExpirationManager.class.getName() + ".PORTLET_SESSIONS";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortalRequestUtils portalRequestUtils;
    
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Required
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }


    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.PortletInvocationListener#onEnd(org.apache.pluto.spi.optional.PortletInvocationEvent)
     */
    public void onEnd(PortletInvocationEvent event) {
        final PortletRequest portletRequest = event.getPortletRequest();
        final PortletSession portletSession = portletRequest.getPortletSession(false);

        if (portletSession != null) {
            final HttpServletRequest portalRequest = this.portalRequestUtils.getOriginalPortalRequest(portletRequest);
            final HttpSession portalSession = portalRequest.getSession();
            
            Map<PortletWindowID, PortletSession> portletSessions;
            synchronized (portalSession) {
                portletSessions = (Map<PortletWindowID, PortletSession>)portalSession.getAttribute(PORTLET_SESSIONS_MAP);
                if (portletSessions == null) {
                    portletSessions = new ConcurrentHashMap<PortletWindowID, PortletSession>();
                    portalSession.setAttribute(PORTLET_SESSIONS_MAP, portletSessions);
                }
            }
            
            final PortletWindow portletWindow = event.getPortletWindow();
            final PortletWindowID portletWindowId = portletWindow.getId();
            portletSessions.put(portletWindowId, portletSession);
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof HttpSessionDestroyedEvent) {
            final HttpSession session = ((HttpSessionDestroyedEvent)event).getSession();
            final Map<PortletWindowID, PortletSession> portletSessions = (Map<PortletWindowID, PortletSession>)session.getAttribute(PORTLET_SESSIONS_MAP);
            if (portletSessions == null) {
                return;
            }
            
            for (final Map.Entry<PortletWindowID, PortletSession> portletSessionEntry: portletSessions.entrySet()) {
                final PortletWindowID portletWindowId = portletSessionEntry.getKey();
                final PortletSession portletSession = portletSessionEntry.getValue();
                try {
                    portletSession.invalidate();
                }
                catch (IllegalStateException e) {
                    this.logger.info("PortletSession with id '" + portletSession.getId() + "' for portletWindowId '" + portletWindowId + "' has already been invalidated.");
                }
                catch (Exception e) {
                    this.logger.warn("Failed to invalidate PortletSession with id '" + portletSession.getId() + "' for portletWindowId '" + portletWindowId + "'", e);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.PortletInvocationListener#onBegin(org.apache.pluto.spi.optional.PortletInvocationEvent)
     */
    public void onBegin(PortletInvocationEvent event) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.PortletInvocationListener#onError(org.apache.pluto.spi.optional.PortletInvocationEvent, java.lang.Throwable)
     */
    public void onError(PortletInvocationEvent event, Throwable t) {
        // Ignore
    }
}
