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

<li>
    <c:out value="${assignment.principal.name}"/>
    <select name="<c:out value="${assignment.principal.id}"/>_type">
        <option value="INHERIT"<c:if test="${assignment.type == 'INHERIT'}"> selected="selected"</c:if>>INHERIT</option>
        <option value="GRANT"<c:if test="${assignment.type == 'GRANT'}"> selected="selected"</c:if>>GRANT</option>
        <option value="DENY"<c:if test="${assignment.type == 'DENY'}"> selected="selected"</c:if>>DENY</option>
    </select>
    <c:if test="${not empty assignment.children}">
    <ul>
        <c:forEach var="child" items="${assignment.children}">
            <c:set var="assignment" value="${child}" scope="request"/>
            <c:import url="/WEB-INF/flows/edit-permission/principal.jsp"/>
        </c:forEach>
    </ul>
    </c:if>
</li>
