<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<portlet:actionURL var="flushAllUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="flush-all"/>
</portlet:actionURL>

<style type="text/css">
.portlet ul.cache-member {margin:0;}
.portlet .cache-member li {list-style:none;}
.portlet .cache-member a {display:block; padding:0.2em 0 0.2em 20px; background-position:0 50%; background-repeat:no-repeat;}
.portlet .cache-member a {background-image:url(/ResourceServingWebapp/rs/famfamfam/silk/1.3/drive.png)}
.portlet .cache-member a:hover {background-color:#FFFFCC; color:#336699;}
</style>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">

  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading"><spring:message code="home.title"/></h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Toolbar -->
  <div class="fl-col-flex2 portlet-toolbar" role="toolbar">
    <div class="fl-col">
        <ul>
            <li><a href="${flushAllUrl}"><span><spring:message code="home.emptyAllButton" /></span></a></li>
      </ul>
    </div>
  </div> <!-- end: portlet-toolbar -->

  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">
       <spring:message code="home.listHeading"/>
      </h3>
      
      <div class="portlet-section-body">
        <p class="portlet-section-note" role="note"><spring:message code="home.listDescription"/></p>
      
        <ul class="cache-member">
        <c:forEach items="${cacheNames}" var="cacheName">
        <portlet:actionURL var="viewStatsUrl">
            <portlet:param name="cacheName" value="${cacheName}"/>
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="view-statistics"/>
        </portlet:actionURL>
        <li><a href="${viewStatsUrl}"><c:out value="${cacheName}"/></a></li>
        </c:forEach>
        </ul>
      </div>
    </div>
  </div>
  
</div>