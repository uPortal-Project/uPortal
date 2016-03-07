<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="n"><portlet:namespace/></c:set>

<h2>Report:  <spring:message code="${operationNameCode}" htmlEscape="false" /></h2>

<c:set var="resultTenantAddMessage" value="tenant.created.successfully.delay.message"/>

<c:forEach items="${operationsListenerResponses}" var="response">
    <c:set var="resultCssClass">
        <c:choose>
            <c:when test="${response.result eq 'SUCCESS'}">success</c:when>
            <c:when test="${response.result eq 'FAIL'}">warning</c:when>
            <c:otherwise>danger</c:otherwise>
        </c:choose>
    </c:set>
    <div class="panel panel-${resultCssClass}">
        <div class="panel-heading"><span class="label label-${resultCssClass}"><c:out value="${response.result}" /></span> <c:out value="${response.tenantOperationsListener.name}" /></div>
        <div class="panel-body">
            <c:forEach items="${response.messages}" var="message">
                <i class="fa fa-info-circle"></i> <c:out value="${message}" escapeXml="false" />
            </c:forEach>
        </div>
    </div>
    <c:if test="${resultCssClass ne 'success'}">
        <c:set var="resultTenantAddMessage" value=""/>
    </c:if>
</c:forEach>

<div>
    <c:if test="${not empty resultTenantAddMessage}">
        <spring:message code="${resultTenantAddMessage}" htmlEscape="false"/>
    </c:if>
    <a class="btn btn-primary pull-right" href="<portlet:renderURL />" role="button"><spring:message code="done" /></a>
</div>
