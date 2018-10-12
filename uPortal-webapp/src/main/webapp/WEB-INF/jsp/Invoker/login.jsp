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
<c:set var="request" value="${pageContext.request}" />

<div id="webLoginContainer" class="fl-widget portal-login">
  <div class="fl-widget-inner">
    <div class="fl-widget-content">
        <div id="portalCASLogin" class="fl-widget-content">
            <a id="portalCASLoginLink" class="btn" title="<spring:message code="sign.in.via.cas"/>" href="${casRefUrlEncoder.getCasLoginUrl(request)}">
                <i class="fa fa-sign-in" aria-hidden="true"></i>
                <spring:message code="sign.in"/>
            </a>
            <a id="portalCASLoginNewLink" title="<spring:message code="create.new.portal.account"/>" href="http://www.jasig.org/cas" class="btn">
                <i class="fa fa-user-plus" aria-hidden="true"></i>
                <spring:message code="new.user.question"/>
            </a>
        </div>
    </div>
  </div>
</div>
