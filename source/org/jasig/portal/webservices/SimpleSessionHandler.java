/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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
 *
 *
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.jasig.portal.webservices;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeader;
import org.apache.axis.session.SimpleSession;
import org.apache.axis.utils.JavaUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.namespace.QName;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/** This handler uses SOAP headers to do simple session management.
 *
 *  <p><i>This class will be used temporarily in place of
 *  org.apache.axis.handlers.SimpleSessionHandler until a bug in Axis's
 *  SimpleSessionHandler is fixed (the session timeout logic is flawed and fixed
 *  in this class).</i><br/>
 *  See http://nagoya.apache.org/bugzilla/show_bug.cgi?id=7536<br/>
 *  The default session timeout has also been changed from 1 minute to
 *  30 minutes</p>
 *
 * <p>Essentially, you install it on both the request and response chains of
 * your service, on both the client and the server side.</p>
 *
 * <p>ON THE SERVER:</p>
 * <ul>
 * <li>The REQUEST is checked for a session ID header.  If present, we
 *     look up the correct SimpleSession.  If not, we create a new session.
 *     In either case, we install the session into the MessageContext, and
 *     put its ID in the SESSION_ID property.
 * <li>The RESPONSE gets a session ID header tacked on, assuming we found a
 *     SESSION_ID property in the MessageContext.
 * </ul>
 * <p>ON THE CLIENT:</p>
 * <ul>
 * <li>The RESPONSE messages are checked for session ID headers.  If present,
 *     we pull the ID out and insert it into an option in the AxisClient.
 *     This works because a given Call object is associated with a single
 *     AxisClient.  However, we might want to find a way to put it into the
 *     Call object itself, which would make a little more sense.  This would
 *     mean being able to get to the Call from the MC, i.e. adding a getCall()
 *     API (which would only work on the client side)....
 * <li>When REQUESTS are generated, we look to see if an ID option is present
 *     in the AxisClient associated with the MessageContext.  If so, we
 *     insert a session ID header with the appropriate ID.
 * </ul>
 *
 * <p>SimpleSessions are "reaped" periodically via a very simplistic
 * mechanism.  Each time the handler is invoke()d we check to see if more
 * than <b>reapPeriodicity</b> milliseconds have elapsed since the last
 * reap.  If so, we walk the collection of active Sessions, and for each
 * one, if it hasn't been "touched" (i.e. had a getProperty() or setProperty()
 * performed) in longer than its timeout, we remove it from the collection.</p>
 *
 * @author Glen Daniels (gdaniels@macromedia.com)
 * @author Ken Weiner, kweiner@interactivebusiness.com (modified slightly)
 */
public class SimpleSessionHandler extends BasicHandler
{
    protected static Log log =
        LogFactory.getLog(SimpleSessionHandler.class.getName());

    public static final String SESSION_ID = "SimpleSession.id";
    public static final String SESSION_NS = "http://xml.apache.org/axis/session";
    public static final String SESSION_LOCALPART = "sessionID";
    public static final QName sessionHeaderName = new QName(SESSION_NS,
                                                            SESSION_LOCALPART);

    private Hashtable activeSessions = new Hashtable();
    private long curSessionID = 0;

    // Reap timed-out sessions on the first request after this many
    // milliseconds.
    private long reapPeriodicity = 30000;
    private long lastReapTime = 0;

    // By default, sessions time out after 30 minutes of inactivity
    private int defaultSessionTimeout = 1800; // originally 1 minute

    /**
     * Process a MessageContext.
     */
    public void invoke(MessageContext context) throws AxisFault
    {
        // Should we reap timed out sessions?
        long curTime = System.currentTimeMillis();
        boolean reap = false;

        // Minimize synchronicity, just check in here, do reap later.
        synchronized (this) {
            if (curTime > lastReapTime + reapPeriodicity) {
                reap = true;
                lastReapTime = curTime;
            }
        }

        if (reap) {
            Set keys = activeSessions.keySet();
            Set victims = new HashSet();
            Object key;
            Iterator i;
            for (i = keys.iterator(); i.hasNext();) {
                key = i.next();
                SimpleSession session = (SimpleSession)activeSessions.get(key);
                if ((session.getTimeout() * 1000) < // originally >
                     (curTime - session.getLastAccessTime())) {
                    log.debug(JavaUtils.getMessage("timeout00",
                                                        key.toString()));

                    // Don't modify the hashtable while we're iterating.
                    victims.add(key);
                }
            }

            // Now go remove all the victims we found during the iteration.
            for (i = victims.iterator(); i.hasNext();) {
                activeSessions.remove(i.next());
            }
        }

        if (context.isClient()) {
            doClient(context);
        } else {
            doServer(context);
        }
    }

    /**
     * Client side of processing.
     */
    public void doClient(MessageContext context) throws AxisFault
    {
        if (context.getPastPivot()) {
            // This is a response.  Check it for the session header.
            Message msg = context.getResponseMessage();
            if (msg == null)
                return;
            SOAPEnvelope env = msg.getSOAPEnvelope();
            SOAPHeader header = env.getHeaderByName(SESSION_NS,
                                                    SESSION_LOCALPART);
            if (header == null)
                return;

            // Got one!
            try {
                Long id = (Long)header.
                             getValueAsType(Constants.XSD_LONG);
                // Store it away.
                AxisEngine engine = context.getAxisEngine();
                engine.setOption(SESSION_ID, id);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        } else {
            AxisEngine engine = context.getAxisEngine();
            Long id = (Long)engine.getOption(SESSION_ID);
            if (id == null)
                return;

            // We have a session ID, so insert the header
            Message msg = context.getRequestMessage();
            if (msg == null)
                throw new AxisFault(JavaUtils.getMessage("noRequest00"));

            SOAPEnvelope env = msg.getSOAPEnvelope();
            SOAPHeader header = new SOAPHeader(SESSION_NS,
                                               SESSION_LOCALPART,
                                               id);
            env.addHeader(header);
        }
    }

    /**
     * Server side of processing.
     */
    public void doServer(MessageContext context) throws AxisFault
    {
        if (context.getPastPivot()) {
            // This is a response.  Add the session header if we have an
            // ID.
            Long id = (Long)context.getProperty(SESSION_ID);
            if (id == null)
                return;

            Message msg = context.getResponseMessage();
            if (msg == null)
                return;
            SOAPEnvelope env = msg.getSOAPEnvelope();
            SOAPHeader header = new SOAPHeader(SESSION_NS,
                                               SESSION_LOCALPART,
                                               id);
            env.addHeader(header);
        } else {
            // Request.  Set up the session if we find the header.
            Message msg = context.getRequestMessage();
            if (msg == null)
                throw new AxisFault(JavaUtils.getMessage("noRequest00"));

            SOAPEnvelope env = msg.getSOAPEnvelope();
            SOAPHeader header = env.getHeaderByName(SESSION_NS,
                                                    SESSION_LOCALPART);
            Long id;

            if (header != null) {
                // Got one!
                try {
                    id = (Long)header.
                            getValueAsType(Constants.XSD_LONG);
                } catch (Exception e) {
                    throw AxisFault.makeFault(e);
                }
            } else {
                id = getNewSession();
            }

            SimpleSession session = (SimpleSession)activeSessions.get(id);
            if (session == null) {
                // Must have timed out, get a new one.
                id = getNewSession();
                session = (SimpleSession)activeSessions.get(id);
            }

            // This session is still active...
            session.touch();

            // Store it away in the MessageContext.
            context.setSession(session);
            context.setProperty(SESSION_ID, id);
        }
    }

    /**
     * Generate a new session, register it, and return its ID.
     *
     * @return the new session's ID for later lookup.
     */
    private synchronized Long getNewSession()
    {
        Long id = new Long(curSessionID++);
        SimpleSession session = new SimpleSession();
        session.setTimeout(defaultSessionTimeout);
        activeSessions.put(id, session);
        return id;
    }

    /**
     * Set the reaper periodicity - convenience method for testing.
     *
     * !!! TODO: Should be able to set this via options on the Handler
     * or perhaps the engine.
     */
    public void setReapPeriodicity(long reapTime)
    {
        reapPeriodicity = reapTime;
    }

    /**
     * Set the default session timeout - again, for testing.
     */
    public void setDefaultSessionTimeout(int defaultSessionTimeout) {
        this.defaultSessionTimeout = defaultSessionTimeout;
    }

}
