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

<%@ include file="/WEB-INF/jsp/include.jsp"%>

<p>| <a href="<portlet:renderURL><portlet:param name="view" value="import"/></portlet:renderURL>">Import</a> | <a href="<portlet:renderURL><portlet:param name="view" value="export"/></portlet:renderURL>">Export</a> | <a href="<portlet:renderURL><portlet:param name="view" value="delete"/></portlet:renderURL>">Delete</a> |</p>

<p>Status of your ${fn:escapeXml(operation)} operation:  ${fn:escapeXml(result)}</p>

<c:if test="${message != null}">
    <p>${fn:escapeXml(message)}</p>
</c:if>

<c:if test="${downloadUrl != null}">
    <p><a href="${downloadUrl}">Get It!</a></p>
</c:if>
