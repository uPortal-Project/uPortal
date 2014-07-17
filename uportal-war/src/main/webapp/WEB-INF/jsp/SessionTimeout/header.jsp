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
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.10.2/jquery-1.10.2.min.js"/>"></script>
<script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.10.3/jquery-ui-1.10.3.min.js"/>"></script>

<script src="<c:url value='/scripts/session-timeout.js'/>"/></script>
<script>
    <rs:compressJs>
        var config = {
            waitTime: 1000,
            sleepTime: 5000,
            bufferTime: 1000,
            dialogId: '${n}-session-timeout-dlg'
        };

        up.SessionTimeout(config).startTimer();
    </rs:compressJs>
</script>
