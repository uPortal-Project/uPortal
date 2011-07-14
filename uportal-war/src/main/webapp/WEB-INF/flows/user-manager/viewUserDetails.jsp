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
<portlet:renderURL var="backUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="cancel"/>
</portlet:renderURL>
<portlet:renderURL var="deleteUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="confirmRemove"/>
</portlet:renderURL>
<portlet:renderURL var="permissionsUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="viewPermissions"/>
</portlet:renderURL>
<portlet:renderURL var="editDetailsUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="editUser"/>
</portlet:renderURL>
<portlet:actionURL var="impersonateUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="swap"/>
</portlet:actionURL>
<portlet:renderURL var="resetLayoutUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="resetLayout"/>
</portlet:renderURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet user-mgr view-reviewuser" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><c:out value="${ fn:escapeXml(person.name )}"/></h2>
        <div class="toolbar">
            <ul>
                <c:if test="${ canEdit }"><li><a class="button" href="${ editDetailsUrl }"><spring:message code="edit"/></a></li></c:if>
                <li><a class="button" href="${ permissionsUrl }"><spring:message code="view.permissions"/></a></li>
                <c:if test="${ canDelete }"><li><a class="button" href="${ deleteUrl }"><spring:message code="delete"/></a></li></c:if>
                <c:if test="${ canImpersonate }"><li><a class="button" href="${ impersonateUrl }"><spring:message code="impersonate"/></a></li></c:if>
                <li><a class="button" href="${ resetLayoutUrl }"><spring:message code="reset.user.layout"/></a></li>
            </ul>
        </div>
    </div> <!-- end: portlet-titlebar -->
    
    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading"><spring:message code="attributes"/></h3>
                <div class="options">
                    <a href="${ editDetailsUrl }"><span><spring:message code="edit.user"/></span></a>
                </div>
            </div>
            <div class="content">
                <table>
                    <c:forEach items="${ person.attributes }" var="attribute">
                        <tr>
                            <td>
                                <c:set var="attrName" value="${ attribute.key }"/>
                                <spring:message code="attribute.displayName.${attrName}" text="${attrName}"/> (<spring:message code="${attrName}"/>)
                            </td>
                            <td>
                                <c:forEach items="${ attribute.value }" var="value">
                                   <div>${fn:escapeXml(value)}</div>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
        </div>
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading"><spring:message code="parent.groups"/></h3>
            </div>
            <div class="content">
                <ul>
                    <c:forEach items="${ parents }" var="group">
                        <li>${ group.name }</li>
                    </c:forEach>
                </ul>
            </div>
        </div>
        
        <div class="buttons">
            <a class="button" href="${ backUrl }"><spring:message code="back"/></a>
        </div>
    </div>
</div>