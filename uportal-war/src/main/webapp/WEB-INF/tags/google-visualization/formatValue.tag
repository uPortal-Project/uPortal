<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

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
        ${ value }
    </c:when>
    <c:otherwise>
        '<spring:escapeBody javaScriptEscape="true">${ value }</spring:escapeBody>'
    </c:otherwise>
</c:choose>

