/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.factory.Factory;

public interface ServletResponseFactory extends Factory {

    public HttpServletResponse getServletResponse(HttpServletResponse response);

    public HttpServletResponse getStoredServletResponse(HttpServletResponse response, PrintWriter writer);

}
