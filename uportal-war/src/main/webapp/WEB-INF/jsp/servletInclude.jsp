<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%--
This is the include JSP file suitable for inclusion atop *Servlet* JSPs.
It does not include the portlet-specific includes necessary in portlets.
(Portlets should instead include `include.jsp` to enjoy those inclusions.)
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@ taglib prefix="up" uri="http://www.uportal.org/jsp/jstl/uportal/1.0" %>
<%@ taglib prefix="rs" uri="http://www.jasig.org/resource-server" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<portlet:defineObjects/>

<spring:htmlEscape defaultHtmlEscape="true" />

<spring:htmlEscape defaultHtmlEscape="true" />


