<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
        <ul>
        <c:forEach items="${cacheNames}" var="cacheName">
        <portlet:actionURL var="viewStatsUrl">
            <portlet:param name="cacheName" value="${cacheName}"/>
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="view-statistics"/>
        </portlet:actionURL>
        <li><a href="${viewStatsUrl}">View statistics for <c:out value="${cacheName}"/></a></li>
        </c:forEach>
        </ul>
        <br/>
        <portlet:actionURL var="flushAllUrl">
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="flush-all"/>
        </portlet:actionURL>
        <a href="${flushAllUrl}">Flush All Caches</a>
      </div>
    </div>
  </div>
  
</div>