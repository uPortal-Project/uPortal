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

<%@ include file="/WEB-INF/jsp/include.jsp" %>

<portlet:actionURL var="flushAllUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="flush-all"/>
</portlet:actionURL>

<!-- Portlet -->
<div class="fl-widget portlet cache-mgr view-list" role="section">

  <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="uportal.cache.manager"/></h2>
        <div class="toolbar" role="toolbar">
            <ul>
                <li><a class="button" href="${flushAllUrl}"><span><spring:message code="empty.all.caches" /></span></a></li>
            </ul>
        </div>
    </div> <!-- end: portlet-titlebar -->
    
  <!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
    	<div class="titlebar">
            <h3 class="portlet-section-header" role="heading">
            	<spring:message code="available.caches"/>
            </h3>
        </div>
      
      <div class="content">
        <p class="note" role="note"><spring:message code="select.cache.to.view.stats.and.clear.content"/></p>
      
        <table class="portlet-table cache-table">
            <thead>
                <tr>
                    <th><spring:message code="name"/></th>
                    <th><spring:message code="percent.used"/></th>
                    <th><spring:message code="effectiveness"/></th>
                    <th><spring:message code="flush"/></th>
                </tr>
            </thead>
            <c:forEach items="${statisticsMap}" var="statisticsEntry">
                <tr class="cache-member">
                    <td class="cache-name">
                        <portlet:actionURL var="viewStatsUrl">
                            <portlet:param name="cacheName" value="${statisticsEntry.key}"/>
                            <portlet:param name="execution" value="${flowExecutionKey}" />
                            <portlet:param name="_eventId" value="view-statistics"/>
                        </portlet:actionURL>
                        <a href="${viewStatsUrl}">${fn:escapeXml(statisticsEntry.key)}</a>
                    </td>
                    <td class="cache-used">
                        <span><fmt:formatNumber value="${statisticsEntry.value.usage}" pattern="00%" /> </span> 
                        <small>(${fn:escapeXml(statisticsEntry.value.size)} / ${fn:escapeXml(statisticsEntry.value.maxSize)})</small>
                    </td>
                    <td class="cache-effectiveness">
                        <span><fmt:formatNumber value="${statisticsEntry.value.effectiveness}" pattern="00%" /> </span>
                        <small>(${fn:escapeXml(statisticsEntry.value.hits)} <spring:message code="hits"/>, ${fn:escapeXml(statisticsEntry.value.misses)} <spring:message code="misses"/>)</small>
                    </td>
                    <td class="cache-flush">
                        <portlet:actionURL var="flushUrl">
                          <portlet:param name="cacheName" value="${statisticsEntry.key}"/>
                          <portlet:param name="_eventId" value="flush"/>
                          <portlet:param name="execution" value="${flowExecutionKey}" />
                        </portlet:actionURL>
                        <a href="${flushUrl}"><spring:message code="flush"/></a>
                    </td>
                </tr>
            </c:forEach>
        </table>
      </div>
    </div>
  </div>
  
</div>