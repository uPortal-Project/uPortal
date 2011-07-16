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
<c:set var="n"><portlet:namespace/></c:set>
<p align="center">${ fn:escapeXml(caption )}</p>
<p align="center">
  <c:if test="${not empty link}">
    <a href="${ link }">
  </c:if>
    <img src="${ uri }" alt="${ fn:escapeXml(alt )}" width="${ width }" height="${ height }" border="${ border }"/>
  <c:if test="${not empty link}">
    </a>
  </c:if>
</p>
<p align="center"><font size="2">${ fn:escapeXml(subcaption )}</font></p>