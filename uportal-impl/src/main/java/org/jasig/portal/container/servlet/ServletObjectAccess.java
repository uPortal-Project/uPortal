/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.services.factory.FactoryManager;

public class ServletObjectAccess {

//    public static HttpServletRequest getServletRequest(HttpServletRequest request, PortletWindow portletWindow) {
//        return getRequestFactory().getServletRequest(request, portletWindow);
//    }

    public static HttpServletResponse getServletResponse(HttpServletResponse response) {
        return getResponseFactory().getServletResponse(response);
    }

    public static HttpServletResponse getStoredServletResponse(HttpServletResponse response, PrintWriter writer) {
        return getResponseFactory().getStoredServletResponse(response, writer);
    }

//    private static ServletRequestFactory getRequestFactory() {
//        return (ServletRequestFactory)FactoryManager.getFactory(javax.servlet.http.HttpServletRequest.class);
//    }
    
    private static ServletResponseFactory getResponseFactory() {
        return (ServletResponseFactory)FactoryManager.getFactory(javax.servlet.http.HttpServletResponse.class);
    }
}
