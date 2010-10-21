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
        <h2 class="title" role="heading"><spring:message code="edit.my.account"/></h2>
    </div> <!-- end: portlet-titlebar -->
    
    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">

        <form:form modelAttribute="accountPasswordForm" action="${formUrl}" method="POST">

            <!-- Portlet Messages -->
            <spring:hasBindErrors name="form">
                <div class="portlet-msg-error portlet-msg error" role="alert">
                    <form:errors path="*" element="div"/>
                </div> <!-- end: portlet-msg -->
            </spring:hasBindErrors>
        
            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="password"/></h3>
                </div>
                <div class="content">
                    <table>
                        <tbody>

                            <!--  Password and confirm password -->
                            <tr>
                                <td class="attribute-name"><spring:message code="current.password"/></td>
                                <td><form:password path="currentPassword"/></td>
                            </tr>
                            <tr>
                                <td class="attribute-name"><spring:message code="new.password"/></td>
                                <td><form:password path="newPassword"/></td>
                            </tr>
                            <tr>
                                <td class="attribute-name"><spring:message code="confirm.password"/></td>
                                <td><form:password path="confirmNewPassword"/></td>
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
                    </div>
                </div>
            </div>

        </form:form>
            
        
    </div>
</div>