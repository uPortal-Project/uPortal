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

<%@ include file="/WEB-INF/jsp/include.jsp" %>

<!-- Portlet -->
<div id="portalFragAdminList" class="fl-widget portlet snooper view-main" role="section">

	
    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading">Fragment Administration</h2>
    </div>
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">

        <!-- Future version to use an unordered list and form submission via javascript.
        <form method="POST" id="fragmentAdminForm" name="fragmentAdminForm" action="<portlet:actionURL><portlet:param name="action" value="becomeFragmentOwner"/></portlet:actionURL>">
        	<input type="hidden" id="fragmentOwner" name="impersonateUser" />
        </form>
        <ul class="fl-listmenu">
            <c:forEach items="${FRAGMENTS}" var="item">
            	<li><a id="${fn:escapeXml(item.key)}" href="#" title="Administer fragment ${fn:escapeXml(item.value)}"><span>${fn:escapeXml(item.value)}</span></a></li>
            </c:forEach>
        </ul>
        -->
        
        <!-- Original form. Renders a select dropdown.-->
        <form method="POST" name="fragmentAdminForm" action="<portlet:actionURL><portlet:param name="action" value="becomeFragmentOwner"/></portlet:actionURL>">
            <select id="fragmentOwner" name="impersonateUser" title="Choose a fragment to edit">
            	<option value="NONE"> -- fragments -- </option>
                <c:forEach items="${FRAGMENTS}" var="item">
                	<option value="${fn:escapeXml(item.key)}">${fn:escapeXml(item.value)}</option>
                </c:forEach>
            </select>
            <input class="button" type="Button" value="Go" onclick="if (document.fragmentAdminForm.fragmentOwner.options[document.fragmentAdminForm.fragmentOwner.selectedIndex].value != 'NONE') document.fragmentAdminForm.submit()"/>
        </form>

	</div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->

<!-- Future version to use an unordered list and form submission via javascript.
<script type="text/javascript">
	$("#portalFragAdminList a")
		.click(function(){
			var linkID = $this.id;
			impersonateUser.val() = linkID;
			fragmentAdminForm.submit();
		});
</script>
-->