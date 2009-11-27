<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<!-- Portlet -->
<div class="fl-widget portlet" role="section">

  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">Cache Management</h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
        <ul>
        <c:forEach items="${cacheNames}" var="cacheName">
        <portlet:renderURL var="renderUrl" windowState="maximized">
            <portlet:param name="cacheName" value="${cacheName}"/>
            <portlet:param name="execution" value="${flowExecutionKey}" />
        </portlet:renderURL>
        <li><a href="${renderUrl}">View statistics for <c:out value="${cacheName}"/></a></li>
        </c:forEach>
        </ul>
      </div>
    </div>
  </div>
  
</div>