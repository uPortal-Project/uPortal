<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ page import="java.util.Map"%>
<%@ page import="org.apache.pluto.testsuite.TestConfig"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>

<center>
  <table>
    <tr>
      <td align="center" nowrap="true">
        
        <%-- Run Previous Test ============================================ --%>
        <portlet:actionURL secure="<%= renderRequest.isSecure() ? \"True\" : \"False\" %>"
                           var="url">
          <portlet:param name="nextTestId"
                         value="<%= renderRequest.getParameter(\"testId\") %>"/>
          <c:forEach var="param" items="${prevTest.actionParameters}">
            <%
                TestConfig.Parameter parameter = (TestConfig.Parameter) pageContext.findAttribute("param");
                String paramName = parameter.getName();
                String paramValue = parameter.getValue();
            %>
            <portlet:param name="<%= paramName %>" value="<%= paramValue %>"/>
          </c:forEach>
        </portlet:actionURL>
        <a href="<%= url %>">
          <img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/previous.png") %>"
               border="0">
        </a>
        
        <%-- Return to Introduction ======================================= --%>
        <portlet:actionURL secure="<%= renderRequest.isSecure() ? \"True\" : \"False\" %>"
                           var="url">
          <portlet:param name="testId" value="<%= null %>"/>
        </portlet:actionURL>
        <a href="<%= url %>">
          <img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/return_index.png") %>"
               border="0">
        </a>
        
        <%-- Run Next Test ================================================ --%>
        <portlet:actionURL secure="<%= renderRequest.isSecure() ? \"True\" : \"False\" %>"
                           var="url">
          <portlet:param name="previousTestId"
                         value="<%= renderRequest.getParameter(\"testId\") %>"/>
          <c:forEach var="param" items="${nextTest.actionParameters}">
            <%
                TestConfig.Parameter parameter = (TestConfig.Parameter) pageContext.findAttribute("param");
                String paramName = parameter.getName();
                String paramValue = parameter.getValue();
            %>
            <portlet:param name="<%= paramName %>" value="<%= paramValue %>"/>
          </c:forEach>
        </portlet:actionURL>
        <a href="<%= url %>">
          <img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/next.png") %>"
               border="0">
        </a>
        
      </td>
    </tr>
  </table>
</center>



