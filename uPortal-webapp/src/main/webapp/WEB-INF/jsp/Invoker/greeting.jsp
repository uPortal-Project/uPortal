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

<c:set var="greeting">
    <c:choose>
        <c:when test="${userInfo['impersonating'] eq 'true'}"><spring:message code="you.are.idswapped.as"/></c:when>
        <c:otherwise><spring:message code="you.are.signed.in.as"/></c:otherwise>
    </c:choose>
</c:set>

<div class="user-name"><span class="hidden-xs">${greeting}</span>&nbsp;${userInfo['displayName']}</div>
