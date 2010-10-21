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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>

<portlet:defineObjects/>

<portlet:actionURL var="actionUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<div id="portalFragAdminExit" class="block">
  <div class="block-inner">
    <h2 class="block-title">Fragment Administration</h2>
    <div class="block-content">
      <form name="fragmentAdminExitForm" action="${actionUrl}" method="POST">
      	<label for="exitFragment">You are currently logged in as <strong><c:out value="${remoteUser}"/></strong> for DLM fragment administration</label>
      	<input id="exitFragment" type="Submit" value="Exit" name="_eventId_logout"/>
      </form>
    </div>
  </div>
</div>
