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
<!-- Session timeout portlet -->
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="n"><portlet:namespace/></c:set>

<div id="${n}session-timeout-dlg" style="display: none;">
    <div class="modal-header">
        <h4 class="modal-title"><spring:message code="session-timeout.title"/></h4>
    </div>
    <div class="modal-body">
        <p><spring:message code="session-timeout.body1" arguments="<span class='session-timeout-remaining'></span>" htmlEscape="false"/></p>
        <p><spring:message code="session-timeout.body2"/></p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-default refresh-session" data-dismiss="modal">
            <spring:message code="session-timeout.button.keep-alive"/>
        </button>
        <button type="button" class="btn btn-primary logout">
            <spring:message code="session-timeout.button.logout"/>
        </button>
    </div>
</div>