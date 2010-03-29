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
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">
    
    <!-- Portlet Title -->
    <div class="fl-widget-titlebar portlet-title" role="sectionhead">
        <h2 class="title" role="heading">Edit Group Details</h2>
    </div> <!-- end: portlet-title -->
    
    <!-- Portlet Body -->
    <div class="fl-widget-content portlet-content" role="main">
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="portlet-section-body">
                <form:form action="${ formUrl }" method="POST" modelAttribute="group">
                    <table summary="<spring:message code="basicInfo.generalSettingsTableSummary"/>">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Description</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td class="fl-text-align-right">Name</td>
                                <td><form:input path="name"/></td>
                            </tr>  
                            <tr>
                                <td class="fl-text-align-right">Description</td>
                                <td><form:input path="description"/></td>
                            </tr>  
                        </tbody>
                    </table>
                    <div class="buttons">
                        <c:choose>
                            <c:when test="${ completed }">
                              <input class="button primary" type="submit" value="<spring:message code="edit-portlet.reviewButton"/>" name="_eventId_review"/>
                            </c:when>
                            <c:otherwise>
                              <input class="button primary" type="submit" value="<spring:message code="edit-portlet.nextButton"/>" name="_eventId_next"/>
                            </c:otherwise>
                        </c:choose>
                        <input class="button" type="submit" value="<spring:message code="groups-manager.cancel"/>" name="_eventId_cancel"/>
                    </div>
                </form:form>
            </div>
        </div>
        
    </div>
</div>