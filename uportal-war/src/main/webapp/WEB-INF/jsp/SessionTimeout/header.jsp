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

<style type="text/css">
    #${n}session-timeout-dlg .modal-dialog {
        color: #000000;
        text-align: left;
    }

    #${n}session-timeout-dlg .modal-header {
        padding: 10px 0;
    }

    #${n}session-timeout-dlg .modal-header h4 {
        font-size: 150%;
    }

    #${n}session-timeout-dlg .modal-body {
        padding: 10px 0;
    }

    #${n}session-timeout-dlg .modal-footer {
        float: right;
        padding: 10px 0;
    }

    .session-timeout-dlg {
        border-radius: 12px;
        -moz-border-radius: 12px;
        -webkit-border-radius: 12px;
        box-shadow: 4px 4px 8px #808080;
        -moz-box-shadow: 4px 4px 8px #808080;
        -webkit-box-shadow: 4px 4px 8px #808080;
    }

    .session-timeout-dlg .ui-dialog-titlebar {
        display: none;
    }
</style>

<script src="<c:url value='/scripts/session-timeout.js'/>"></script>
<script>
    <rs:compressJs>
        up.SessionTimeout({
            enabled: ${enabled},
            sessionTimeoutMS: ${sessionTimeoutMS},
            dialogDisplayMS: ${dialogDisplayMS},
            dialogId: '${n}session-timeout-dlg',
            logoutURL: '<c:url value="${logoutURLFragment}"/>',
            resetSessionURL: '<c:url value="${resetSessionURLFragment}"/>'
        }).startTimer();
    </rs:compressJs>
</script>
