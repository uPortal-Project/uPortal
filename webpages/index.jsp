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

<%-- We need a way to configure the top level portal directory
     Here it is read in from web.xml and stored in GenericPortalBean. --%>
<%
String sPortalBaseDir = (String) application.getInitParameter ("portalBaseDir");

// Check if portal base directory has been set
if (sPortalBaseDir == null)
{
  String sErrorMsg = "ERROR: No value has been set for application-context parameter \"portalBaseDir\".";
  System.err.println (sErrorMsg);
  throw new Exception (sErrorMsg);
}
else
{
  java.io.File portalBaseDir = new java.io.File (sPortalBaseDir);
  
  // Make sure the portal base directory exists
  if (portalBaseDir.exists ())
    org.jasig.portal.GenericPortalBean.setPortalBaseDir (sPortalBaseDir);
  else
  {
    String sErrorMsg = "ERROR: The portal base directory specified does not exist: " + sPortalBaseDir;
    System.err.println (sErrorMsg);
    throw new Exception (sErrorMsg);
  }
}
%>

<jsp:forward page="layout.jsp" />
