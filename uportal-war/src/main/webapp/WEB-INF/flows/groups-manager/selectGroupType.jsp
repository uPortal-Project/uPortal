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
<portlet:renderURL var="formUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:renderURL>
<portlet:renderURL var="peopleUrl">
    <portlet:param name="execution" value="${flowExecutionKey}"/>
    <portlet:param name="groupType" value="group"/>
    <portlet:param name="_eventId" value="next"/>
</portlet:renderURL>
<portlet:renderURL var="portletUrl">
    <portlet:param name="execution" value="${flowExecutionKey}"/>
    <portlet:param name="groupType" value="category"/>
    <portlet:param name="_eventId" value="next"/>
</portlet:renderURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet grp-mgr view-selectgroup" role="section">
    
    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <spring:message code="groups.by.type" />
        </h2>
    </div> <!-- end: portlet-titlebar -->
    
    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">
    
    	<div class="panel-list icon-large group-types">
        	<div class="panel type-categories">
            	<div class="titlebar">
                	<h2 class="title">
                    	<a href="${ portletUrl }"><spring:message code="portlet.categories"/></a>
                    </h2>
                    <h3 class="subtitle"><spring:message code="portlet.categories.description"/></h3>
                </div>
                <div class="content">
                	<span class="link-list">
                    	<c:forEach items="${ groups.categories }">
                        	<a href="${ groupUrl }">${ fn:escapeXml(group.name )}</a>${ fn:escapeXml(status.last ? "" : ", " )}
                        </c:forEach>
                    </span>
                </div>
            </div>
            <div class="panel type-people">
            	<div class="titlebar">
                	<h2 class="title">
                    	<a href="${ peopleUrl }"><spring:message code="person.groups"/></a>
                    </h2>
                    <h3 class="subtitle"><spring:message code="person.groups.description"/></h3>
                </div>
                <div class="content">
                	<span class="link-list">
                    	<c:forEach items="${ groups.people }">
                        	<a href="${ groupUrl }">${ fn:escapeXml(group.name )}</a>${ fn:escapeXml(status.last ? "" : ", " )}
                        </c:forEach>
                    </span>
                </div>
            </div>
        </div>
        
    </div>
</div>