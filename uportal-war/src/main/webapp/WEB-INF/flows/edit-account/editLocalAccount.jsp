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
<portlet:actionURL var="formUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet user-mgr view-reviewuser" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="edit.user"/>: <c:out value="${ person.username }"/></h2>
    </div> <!-- end: portlet-titlebar -->
    
    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">

        <form:form modelAttribute="person" action="${formUrl}" method="POST">
        
            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="attributes"/></h3>
                </div>
                <div class="content">
                
                    <table>
                        <thead>
                            <tr>
                                <th><spring:message code="attribute.name"/></th>
                                <th><spring:message code="attribute.value"/></th>
                            </tr>
                        </thead>
                        <tbody>

                            <!-- Print out each attribute -->
                            <c:forEach items="${ person.attributes }" var="attribute">
                                <tr>
                                    <td class="attribute-name">${ attribute.key }</td>
                                    <td>
                                        <c:forEach var="value" items="${ attribute.value.value }">
                                            <div>
                                                 <input name="attributes['${attribute.key}'].value" value="${ value }" />
                                            </div>
                                        </c:forEach>
                                    </td>
                                </tr>
                            </c:forEach>
                            
                        </tbody>
                    </table>

                </div>
            </div>

            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="password"/></h3>
                </div>
                <div class="content">
                    <table>
                        <thead>
                            <tr>
                                <th><spring:message code="attribute.name"/></th>
                                <th><spring:message code="attribute.value"/></th>
                            </tr>
                        </thead>
                        <tbody>

                            <!--  Password and confirm password -->
                            <tr>
                                <td class="attribute-name"><spring:message code="password"/></td>
                                <td><form:password path="password"/></td>
                            </tr>
                            <tr>
                                <td class="attribute-name"><spring:message code="confirm.password"/></td>
                                <td><form:password path="confirmPassword"/></td>
                            </tr>

                        </tbody>
                    </table>
                </div>
            </div>    
            
            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="content">
            
                    <div class="buttons">
                        <input class="button primary" type="submit" value="<spring:message code="save"/>" name="_eventId_save"/>
                        <input class="button" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
                    </div>
                </div>
            </div>

        </form:form>
            
        
    </div>
</div>