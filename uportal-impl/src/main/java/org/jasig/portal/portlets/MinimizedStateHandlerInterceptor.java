/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.springframework.web.portlet.HandlerInterceptor;
import org.springframework.web.portlet.handler.HandlerInterceptorAdapter;

/**
 * Portlet {@link HandlerInterceptor}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MinimizedStateHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandleRender(RenderRequest request, RenderResponse response, Object handler) throws Exception {
        if (WindowState.MINIMIZED.equals(request.getWindowState())) {
            return false;
        }
        
        return true;
    }
}
