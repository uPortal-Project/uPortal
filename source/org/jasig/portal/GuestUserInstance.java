/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.services.StatsRecorder;

/**
 * A multithreaded version of a UserInstance.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public class GuestUserInstance extends UserInstance implements HttpSessionBindingListener {
    
    private static final Log log = LogFactory.getLog(GuestUserInstance.class);
    
    // state class
    private class IState {
        private ChannelManager channelManager;
        private LocaleManager localeManager;
        private StandaloneChannelRenderer p_browserMapper;
        private Object p_rendering_lock;
        public IState() {
            channelManager=null;
            p_rendering_lock=null;
            p_browserMapper=null;
        }
    }
    Map stateTable;

    // manages layout and preferences
    GuestUserPreferencesManager uLayoutManager;

    public GuestUserInstance(IPerson person) {
        super(person);
        // instantiate state table
	      stateTable=Collections.synchronizedMap(new HashMap());
        uLayoutManager=new GuestUserPreferencesManager(person);
    }

    /**
     * Register arrival of a new session.
     * Create and populate new state entry.
     * @param req a <code>HttpServletRequest</code> value
     */
    public void registerSession(HttpServletRequest req) throws PortalException {
	      IState newState=new IState();
        newState.channelManager=new ChannelManager(new GuestUserPreferencesManagerWrapper(uLayoutManager,req.getSession(false).getId()));
        newState.localeManager = new LocaleManager(person, req.getHeader("Accept-Language"));        
        newState.p_rendering_lock=new Object();
        uLayoutManager.setLocaleManager(newState.localeManager);
        uLayoutManager.registerSession(req);
        stateTable.put(req.getSession(false).getId(),newState);
    }

    /**
     * Unbinds a registered session.
     * @param sessionId a <code>String</code> value
     */
    public void unbindSession(String sessionId) {
        IState state=(IState)stateTable.get(sessionId);
        if(state==null) {
            log.error("GuestUserInstance::unbindSession() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return;
        }
        state.channelManager.finishedSession();
        uLayoutManager.unbindSession(sessionId);
        stateTable.remove(sessionId);
    }

    /**
     * This notifies UserInstance that it has been unbound from the session.
     * Method triggers cleanup in ChannelManager.
     *
     * @param bindingEvent an <code>HttpSessionBindingEvent</code> value
     */
    public void valueUnbound (HttpSessionBindingEvent bindingEvent) {
        this.unbindSession(bindingEvent.getSession().getId());
        log.debug("GuestUserInstance::valueUnbound() : unbinding session \""+bindingEvent.getSession().getId()+"\"");

        // Record the destruction of the session
        StatsRecorder.recordSessionDestroyed(person);
    }

    /**
     * Notifies UserInstance that it has been bound to a session.
     *
     * @param bindingEvent a <code>HttpSessionBindingEvent</code> value
     */
    public void valueBound (HttpSessionBindingEvent bindingEvent) {
        log.debug("GuestUserInstance::valueBound() : instance bound to a new session \""+bindingEvent.getSession().getId()+"\"");

        // Record the creation of the session
        StatsRecorder.recordSessionCreated(person);
    }

    /**
     * Prepares for and initates the rendering cycle.
     * @param req the servlet request object
     * @param res the servlet response object
     */
    public void writeContent (HttpServletRequest req, HttpServletResponse res) throws PortalException {
        String sessionId=req.getSession(false).getId();
        IState state=(IState)stateTable.get(sessionId);
        if(state==null) {
            log.error("GuestUserInstance::writeContent() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
            return;
        }
        // instantiate user layout manager and check to see if the profile mapping has been established
        if (state.p_browserMapper != null) {
            try {
                state.p_browserMapper.prepare(req);
            } catch (Exception e) {
                throw new PortalException(e);
            }
        }
        if (uLayoutManager.isUserAgentUnmapped(sessionId)) {
            uLayoutManager.unbindSession(sessionId);
            uLayoutManager.registerSession(req);
        } else {
            // p_browserMapper is no longer needed
            state.p_browserMapper = null;
        }

        if (uLayoutManager.isUserAgentUnmapped(sessionId)) {
            // unmapped browser
            if (state.p_browserMapper== null) {
                state.p_browserMapper = new org.jasig.portal.channels.CSelectSystemProfile();
                state.p_browserMapper.initialize(new Hashtable(), "CSelectSystemProfile", true, true, false, 10000, getPerson());
            }
            try {
                state.p_browserMapper.render(req, res);
            } catch (PortalException pe) {
                throw pe;
            } catch (Throwable t) {
                // something went wrong trying to show CSelectSystemProfileChannel
                log.error("GuestUserInstance::writeContent() : CSelectSystemProfileChannel.render() threw: "+t);
                throw new PortalException("CSelectSystemProfileChannel.render() threw: "+t);
            }
            // don't go any further!
            return;
        }

        renderState (req, res, state.channelManager, state.localeManager, new GuestUserPreferencesManagerWrapper(uLayoutManager,sessionId),state.p_rendering_lock);
    }
}



