<%--
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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
--%>

<%@ page errorPage="error.jsp" %>
<%@ page import="java.lang.reflect.Method" %>
<%@ page import="java.util.Enumeration" %>

<%@ page import="org.jasig.portal.*" %>
<%@ page import="org.jasig.portal.layout.*" %>
<%@ page import="org.jasig.portal.security.IPerson" %>

<%@ page import="org.xml.sax.DocumentHandler" %>
<%@ page import="org.apache.xml.serialize.HTMLSerializer" %>
<%@ page import="org.apache.xml.serialize.OutputFormat" %>

<%@ include file="checkinit.jsp" %>

<jsp:useBean id="dispatchBean" class="org.jasig.portal.DispatchBean" scope="session" />

<%
  // this is how you MUST get the layout, otherwise, all guests will recieve their own layout, which WILL CRASH YOUR SERVER!
  org.jasig.portal.ILayoutBean layoutBean = org.jasig.portal.LayoutBean.findLayoutInstance(application, session);
%>

<jsp:useBean id="authorizationBean" class="org.jasig.portal.AuthorizationBean" scope="session" />

<%
  // This will check to see if the channel wants to return
  //  to layout. This allow channels to behave the same way
  //  in uPortal 2.0.
  String sUserLayoutRoot = request.getParameter("userLayoutRoot");
  if(sUserLayoutRoot != null && sUserLayoutRoot.equals("root"))
  {
    // Reconstruct URL parameters
    String jspfile = "layout.jsp?";

    for(Enumeration e = request.getParameterNames(); e.hasMoreElements();)
    {
      String pName  = (String)e.nextElement();
      String pValue = request.getParameter(pName);
      jspfile += pName + "=" + pValue + "&";
    }

    response.sendRedirect(jspfile);
    return;
  }
%>

<%
  org.jasig.portal.IChannel ch = dispatchBean.getChannel(request);
%>

<% UtilitiesBean.preventPageCaching(response); %>

<%
  String sTitle = ch.getName();
  session.setAttribute("headerTitle", sTitle);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title><%= sTitle %></title>
    <link rel="stylesheet" href="stylesheets/portal.css" TYPE="text/css" />

  <% layoutBean.writeBodyStyle(request, response, out); %>

  <body>

  <%
    /*
    String sGlobalChannelID = req.getParameter("globalChannelID");

    if(!authorizationBean.canUserRender((IPerson)session.getAttribute("Person"), dispatchBean.getGlobalChannelID(ch)))
    {
      dispatchBean.finish(request, response);
    }
    */
  %>

  <%-- Header --%>
  <%@ include file="header.jsp" %>

  <div class="portalChannelText">
  <%-- Render Channel --%>
  <%
    try
    {
    String sMethodName = request.getParameter ("method");

    if(sMethodName == null)
    {
      sMethodName = "render";
    }

    Class[] paramTypes = new Class[] {Class.forName ("javax.servlet.http.HttpServletRequest"), Class.forName ("javax.servlet.http.HttpServletResponse"), Class.forName ("javax.servlet.jsp.JspWriter")};
    Object[] methodParams = new Object[] {request, response, out};
    java.lang.reflect.Method method = ch.getClass ().getMethod (sMethodName, paramTypes);
    method.invoke(ch, methodParams);
    }
    catch(Exception ex)
    {
      Logger.log(Logger.ERROR, ex);
      out.println("Due to technical difficulties, this channel cannot be displayed");
    }
    catch(Error er)
    {
      Logger.log(Logger.ERROR, er);
      out.println("Due to technical difficulties, this channel cannot be displayed");
    }
  %>
  </div>

  <%-- Footer --%>
  <%@ include file="footer.jsp" %>

  </body>
</html>
