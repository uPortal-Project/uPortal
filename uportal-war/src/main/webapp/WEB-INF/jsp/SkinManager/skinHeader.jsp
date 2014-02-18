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
<c:set var="n"><portlet:namespace/></c:set>
<c:if test="${isAdmin}">
    <%-- Put something non-visible that the will give the admin the config icon in the chrome as another option
         to editing the preferences. --%>
    &nbsp;&nbsp;&nbsp;
    <%-- This doesn't seem to work. Get error trying to set portletMode to config  UP-3895
    <portlet:renderURL var="editUrl" portletMode="config"/>
    <a href="${editUrl}"><spring:message code="skin.manager.edit.config"/></a>   --%>
</c:if>
<link rel="stylesheet" type="text/css" href='<c:url value="${skinCssUrl}"/>'/>

