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

<div id="portalFragAdminExit" class="fl-widget">
    <div class="fl-widget-titlebar">
    	<h2 class="fl-widget-title">Fragment Administration</h2>
    </div>
    <div class="fl-widget-content">
        <form name="fragmentAdminExitForm" action="${loginUrl}">
        	<label for="exitFragment">You are currently logged in as <strong><c:out value="${USERNAME}"/></strong> for DLM fragment administration</label>
        	<input id="exitFragment" type="Submit" value="Exit Fragment"/>
        </form>
    </div>
</div>
