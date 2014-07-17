<%--
  ~ Licensed to Jasig under one or more contributor license
  ~ agreements. See the NOTICE file distributed with this work
  ~ for additional information regarding copyright ownership.
  ~ Jasig licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file
  ~ except in compliance with the License. You may obtain a
  ~ copy of the License at:
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on
  ~ an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<!-- Session timeout portlet -->
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<div id="${n}-session-timeout-dlg" style="display: none" title="<spring:message code="session-timeout.title"/>">
    <div class="session-timeout-body">
        <span>
            <spring:message code="session-timeout.body1" arguments="<span class='session-timeout-remaining'></span>" htmlEscape="false"/>
        </span>
        <span>
            <spring:message code="session-timeout.body2"/>
        </span>
    </div>

    <div class="session-timeout-buttons btn-group">
        <button type="button" class="btn btn-default">
            <spring:message code="session-timeout.button.keep-alive"/>
        </button>
        <button type="button" class="btn btn-default">
            <spring:message code="session-timeout.button.logout"/>
        </button>
    </div>
</div>
