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
<portlet:renderURL var="createUserUrl">
    <portlet:param name="execution" value="${flowExecutionKey}"/>
    <portlet:param name="_eventId" value="createUser"/>
</portlet:renderURL>
<portlet:renderURL var="findUserUrl">
    <portlet:param name="execution" value="${flowExecutionKey}"/>
    <portlet:param name="_eventId" value="findUser"/>
</portlet:renderURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet user-mgr view-selectuseraction" role="section">
    
    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <spring:message code="manage.users" />
        </h2>
    </div> <!-- end: portlet-titlebar -->
    
    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">
    
        <div class="panel-list icon-large">
            <div class="panel">
                <div class="titlebar">
                    <h2 class="title">
                        <a href="${ createUserUrl }"><spring:message code="create.new.user"/></a>
                    </h2>
                    <h3 class="subtitle"><spring:message code="create.new.user.description"/></h3>
                </div>
            </div>
            <div class="panel">
                <div class="titlebar">
                    <h2 class="title">
                        <a href="${ findUserUrl }"><spring:message code="find.user"/></a>
                    </h2>
                    <h3 class="subtitle"><spring:message code="find.user.description"/></h3>
                </div>
            </div>
        </div>
        
    </div>
</div>