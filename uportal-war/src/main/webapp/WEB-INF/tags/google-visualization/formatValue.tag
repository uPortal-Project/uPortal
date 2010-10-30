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

<%@ include file="/WEB-INF/jsp/include.jsp"  %>
<%@ tag trimDirectiveWhitespaces="true" %>  
<%@ tag import="java.util.Date" %>
<%@ tag dynamic-attributes="attributes" isELIgnored="false" %>
<%@ attribute name="value" required="true" type="java.lang.Object" %>

<c:choose>
    <c:when test="<%= value instanceof Date %>">
        new Date(<fmt:formatDate value="${ value }" pattern="yyyy"/>, Number(<fmt:formatDate value="${ value }" pattern="M"/>)-1, <fmt:formatDate value="${ value }" pattern="d"/>)
    </c:when>
    <c:when test="<%= value instanceof Number || value instanceof Boolean %>">
        ${ fn:escapeXml(value )}
    </c:when>
    <c:otherwise>
        '<spring:escapeBody javaScriptEscape="true">${ value }</spring:escapeBody>'
    </c:otherwise>
</c:choose>

