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


package  org.jasig.portal;

import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.utils.XSLT;
import  javax.servlet.*;
import  javax.servlet.jsp.*;
import  javax.servlet.http.*;
import  java.io.*;
import  java.util.*;
import  java.text.*;
import  java.net.*;
import  org.w3c.dom.*;
import  org.apache.xalan.xpath.*;
import  org.apache.xalan.xslt.*;
import  org.apache.xml.serialize.*;


/**
 * A multithreaded version of a UserInstance.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public class GuestUserInstance extends UserInstance implements HttpSessionBindingListener {
    // state class
    private class IState {
        private ChannelManager channelManager;
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
    GuestUserLayoutManager uLayoutManager;


    public GuestUserInstance(IPerson person) {
        super(person);
        // instantiate state table
	stateTable=Collections.synchronizedMap(new HashMap());
        uLayoutManager=new GuestUserLayoutManager(person);
    }

   
    /**
     * Register arrival of a new session.
     * Create and populate new state entry.
     * @param req a <code>HttpServletRequest</code> value
     */
    public void registerSession(HttpServletRequest req) {
	IState newState=new IState();
        newState.channelManager=new ChannelManager(new GuestUserLayoutManagerWrapper(uLayoutManager,req.getSession(false).getId()));
        newState.p_rendering_lock=new Object();
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
	    Logger.log(Logger.ERROR,"GuestUserInstance::unbindSession() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
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
        Logger.log(Logger.DEBUG,"GuestUserInstance::valueUnbound() : unbinding session \""+bindingEvent.getSession().getId()+"\"");
    }
    
    /**
     * Notifies UserInstance that it has been bound to a session.
     *
     * @param bindingEvent a <code>HttpSessionBindingEvent</code> value
     */
    public void valueBound (HttpSessionBindingEvent bindingEvent) {
        Logger.log(Logger.DEBUG,"GuestUserInstance::valueBound() : instance bound to a new session \""+bindingEvent.getSession().getId()+"\"");
    }
   
    /**
     * Prepares for and initates the rendering cycle. 
     * @param the servlet request object
     * @param the servlet response object
     * @param the JspWriter object
     */
    public void writeContent (HttpServletRequest req, HttpServletResponse res, java.io.PrintWriter out) {
        String sessionId=req.getSession(false).getId();
	IState state=(IState)stateTable.get(sessionId);
	if(state==null) {
	    Logger.log(Logger.ERROR,"GuestUserInstance::writeContent() : trying to envoke a method on a non-registered sessionId=\""+sessionId+"\".");
	    return;
	}

        try {
            // instantiate user layout manager and check to see if the profile mapping has been established
            if (state.p_browserMapper != null) {
                state.p_browserMapper.prepare(req);
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
                } catch (Exception e) {
                    // something went wrong trying to show CSelectSystemProfileChannel 
                    Logger.log(Logger.ERROR,"GuestUserInstance::writeContent() : unable caught an exception while trying to display CSelectSystemProfileChannel! Exception:"+e);
                }
                // don't go any further!
                return;
            }
	    
            // if we got to this point, we can proceed with the rendering
            if (state.channelManager == null) {
                state.channelManager = new ChannelManager(uLayoutManager); 
            }
	    
            // call layout manager to process all user-preferences-related request parameters
            // this will update UserPreference object contained by UserLayoutManager, so that
            // appropriate attribute incorporation filters and parameter tables can be constructed.
            uLayoutManager.processUserPreferencesParameters(req);
            renderState (req, res, out, state.channelManager, uLayoutManager.getUserLayout(sessionId), uLayoutManager.getUserPreferences(sessionId), uLayoutManager.getStructureStylesheetDescription(sessionId),uLayoutManager.getThemeStylesheetDescription(sessionId),state.p_rendering_lock);
        } catch (Exception e) {
            StringWriter sw=new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sw.flush();
            Logger.log(Logger.ERROR,"UserInstance::writeContent() : an unknown exception occurred : "+sw.toString());
        }
    }
}



