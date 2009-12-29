<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<!-- START: VALUES BEING PASSED FROM BACKEND -->

<%@page import="org.jasig.portal.spring.PortalApplicationContextLocator"%>


<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.jasig.portal.api.portlet.PortletDelegationLocator"%>
<%@page import="javax.portlet.PortletSession"%>
<%@page import="org.jasig.portal.portlet.om.IPortletWindowId"%>
<%@page import="org.jasig.portal.api.portlet.PortletDelegationDispatcher"%>
<%@page import="org.jasig.portal.api.portlet.DelegateState"%>
<%@page import="org.jasig.portal.channels.portlet.IPortletAdaptor"%>
<%@page import="org.jasig.portal.api.portlet.DelegationRequest"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.Arrays"%>



<portlet:actionURL var="navigationUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<!--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| http://www.ja-sig.org/wiki/x/cQ
-->
    
<!-- Portlet -->
<div class="fl-widget portlet" role="section">

  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">
      <c:choose>
        <c:when test="${ completed }">
          <spring:message code="edit-portlet.editPortletHeading"/>
        </c:when>
        <c:otherwise>
          <spring:message code="edit-portlet.newPortletHeading"/>
        </c:otherwise>
      </c:choose>
    </h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  <c:set var="channelFname" value="${CHANNEL_FNAME}"/>
  <c:set var="flowKey" value="${flowExecutionKey}"/>
<%
final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
final PortletDelegationLocator portletDelegationLocator = (PortletDelegationLocator)applicationContext.getBean("portletDelegationLocator", PortletDelegationLocator.class);

final PortletSession portletSession = renderRequest.getPortletSession();
IPortletWindowId portletWindowId = (IPortletWindowId)portletSession.getAttribute("DELEGATE_WINDOW_ID");

final PortletDelegationDispatcher portletDelegationDispatcher;
final DelegateState delegateState;
if (portletWindowId == null) {
    String channelFname = (String)pageContext.getAttribute("channelFname");
    portletDelegationDispatcher = portletDelegationLocator.createRequestDispatcher(renderRequest, channelFname);
    portletWindowId = portletDelegationDispatcher.getPortletWindowId();
    portletSession.setAttribute("DELEGATE_WINDOW_ID", portletWindowId);
    
    delegateState = new DelegateState(IPortletAdaptor.CONFIG, null);
}
else {
    portletDelegationDispatcher = portletDelegationLocator.getRequestDispatcher(renderRequest, portletWindowId);
    delegateState = null;
}

final Map parameters = new LinkedHashMap();
parameters.put("execution", Arrays.asList(new String[] { (String)pageContext.getAttribute("flowKey") } ));
parameters.put("_eventId", Arrays.asList(new String[] { "configModeAction" } ));

final DelegationRequest delegationRequest = new DelegationRequest();
delegationRequest.setDelegateState(delegateState);
delegationRequest.setParentParameters(parameters);

portletDelegationDispatcher.doRender(renderRequest, renderResponse, delegationRequest, out);
%>
    
  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->
