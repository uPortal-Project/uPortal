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
<%@page import="org.jasig.portal.channels.portlet.IPortletAdaptor"%><portlet:actionURL var="navigationUrl">
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

portletDelegationDispatcher.doRender(renderRequest, renderResponse, delegateState);
%>

    <form action="${navigationUrl}" method="POST">

    <!-- Portlet Buttons -->    
    <div class="portlet-button-group">
      <!-- watch for change in portlet mode for completion signal -->
      <c:choose>
        <c:when test="${ completed }">
          <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="edit-portlet.reviewButton"/>" name="_eventId_review"/>
        </c:when>
        <c:otherwise>
          <input class="portlet-button" type="submit" value="<spring:message code="edit-portlet.backButton"/>" class="secondary" name="_eventId_back"/>
          <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="edit-portlet.nextButton"/>" name="_eventId_next"/>
        </c:otherwise>
      </c:choose>
      <input class="portlet-button" type="submit" value="<spring:message code="edit-portlet.cancelButton"/>" name="_eventId_cancel"/>
    </div>
    
    </form> <!-- End Form -->
    
  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->
