/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.container;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.core.InternalActionResponse;
import org.apache.pluto.factory.PortletObjectAccess;
import org.apache.pluto.invoker.PortletInvoker;
import org.apache.pluto.invoker.PortletInvokerAccess;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.log.Log;

/**
 * Same as Pluto's PortletContainerImpl, but this one ignores redirects.
 * In uPortal, we can't let channels and portlets request redirects because the
 * state of the response object is not guaranteed.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletContainerImpl extends org.apache.pluto.PortletContainerImpl {

    // We wouldn't need this member variable if it was protected or public
    // in org.apaceh.pluto.PortletContainerImpl
    private String uniqueContainerName = null;

    public PortletContainerImpl(String uniqueContainerName) {
        super();
        this.uniqueContainerName = uniqueContainerName;
    }

    public void processPortletAction(PortletWindow portletWindow, HttpServletRequest servletRequest, HttpServletResponse servletResponse)
        throws PortletException, IOException, PortletContainerException {
        PortletContainerServices.prepare(uniqueContainerName);
        PortletInvoker invoker = null;

        if (Log.isDebugEnabled("org.apache.pluto.invoker")) {
            Log.debug("org.apache.pluto.invoker", "PortletContainerImpl.performPortletAction(" + portletWindow.getId() + ") called.");
        }

        String location = null;

        InternalActionResponse _actionResponse = null;
        ActionRequest actionRequest = null;

        try {
            /*ActionRequest*/
            actionRequest = PortletObjectAccess.getActionRequest(portletWindow, servletRequest, servletResponse);

            ActionResponse actionResponse = PortletObjectAccess.getActionResponse(portletWindow, servletRequest, servletResponse);
            invoker = PortletInvokerAccess.getPortletInvoker(portletWindow.getPortletEntity().getPortletDefinition());

            _actionResponse = (InternalActionResponse)actionResponse;

            // call action() at the portlet
            invoker.action(actionRequest, actionResponse);

            location = _actionResponse.getRedirectLocation();
        } catch (PortletException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } finally {
            PortletInvokerAccess.releasePortletInvoker(invoker);
            PortletContainerServices.release();
        }
    }
}
