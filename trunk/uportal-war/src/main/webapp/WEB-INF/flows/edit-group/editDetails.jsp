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
<div class="fl-widget portlet grp-mgr view-editdetails" role="section">
    
    <!-- Portlet Title -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="edit.group.details"/></h2>
    </div> <!-- end: portlet-title -->
    
    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">
        <form:form action="${ formUrl }" method="POST" modelAttribute="group">
        
            <!-- Portlet Messages -->
            <spring:hasBindErrors name="group">
                <div class="portlet-msg-error portlet-msg error" role="alert">
                    <form:errors path="*" element="div"/>
                </div> <!-- end: portlet-msg -->
            </spring:hasBindErrors>
            
            <div class="portlet-form">
            
                <table class="purpose-layout" summary="<spring:message code="basicInfo.generalSettingsTableSummary"/>">
                    <thead>
                        <tr>
                            <th><spring:message code="name"/></th>
                            <th><spring:message code="description"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="label"><label for="name"><spring:message code="name"/>:</label></td>
                            <td><form:input path="name"/></td>
                        </tr>  
                        <tr>
                            <td class="label"><label for="description"><spring:message code="description"/>:</label></td>
                            <td><form:input path="description"/></td>
                        </tr>  
                    </tbody>
                </table>
                <div class="buttons">
                    <c:choose>
                        <c:when test="${ completed }">
                          <input class="button primary" type="submit" value="<spring:message code="save"/>" name="_eventId_save"/>
                        </c:when>
                        <c:otherwise>
                          <input class="button primary" type="submit" value="<spring:message code="next"/>" name="_eventId_next"/>
                        </c:otherwise>
                    </c:choose>
                    <input class="button" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
                </div>
            </div>

        </form:form>
    </div>
</div>