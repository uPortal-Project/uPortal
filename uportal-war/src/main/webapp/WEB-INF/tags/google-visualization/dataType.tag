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

<%
    if (value instanceof Date) {
        out.print("date");
    } else if (value instanceof Number) {
        out.print("number");
    } else if (value instanceof Boolean) {
        out.print("boolean");
    } else {
        out.print("string");
    }
%>