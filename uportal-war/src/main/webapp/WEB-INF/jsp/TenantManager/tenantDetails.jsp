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

<style>
#${n}tenantDetails h2 {
    margin-bottom: 18px;
}
#${n}tenantDetails dt {
    margin-top: 12px;
}
#${n}tenantDetails btn-group btn {
    margin-right: 12px;
}
</style>

<div id="${n}tenantDetails">
    <div class="panel panel-default">
        <div class="panel-body">
            <h2><spring:message code="tenant.details" /> <c:out value="${tenant.name}" /></h2>
            <dl>
                <c:forEach items="${tenantManagerAttributes}" var="attribute">
                    <dt><spring:message code="${attribute.value}" /></dt>
                    <dd><c:out value="${tenant.attributesMap[attribute.key]}" /></dd>
                </c:forEach>
            </dl>
        </div>
    </div>
    <div class="btn-group pull-right">
        <c:forEach items="${operationsListenerAvailableActions}" var="listenerAction">
            <portlet:actionURL var="listenerActionUrl">
                <portlet:param name="action" value="doListenerAction"/>
                <portlet:param name="fname" value="${listenerAction.fname}"/>
            </portlet:actionURL>
            <a class="btn btn-default" href="${listenerActionUrl}" role="button"><spring:message code="${listenerAction.messageCode}" /></a>
        </c:forEach>
        <a class="btn btn-primary" href="<portlet:renderURL />" role="button"><spring:message code="done" /></a>
    </div>
</div>
