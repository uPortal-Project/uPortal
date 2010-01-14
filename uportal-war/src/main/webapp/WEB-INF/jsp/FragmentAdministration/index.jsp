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

<div id="portalFragAdminList" class="fl-widget">
    <div class="fl-widget-inner">
        <div class="fl-widget-titlebar">
        	<h2 class="block-title">Fragment Administration</h2>
        </div>
        <div class="fl-widget-content">
          <!-- Future version to use an unordered list and form submission via javascript.
          <form method="POST" id="fragmentAdminForm" name="fragmentAdminForm" action="<portlet:actionURL><portlet:param name="action" value="becomeFragmentOwner"/></portlet:actionURL>">
              <input type="hidden" id="fragmentOwner" name="impersonateUser" />
          </form>
          <ul class="fl-listmenu">
            <c:forEach items="${FRAGMENTS}" var="item">
              <li><a id="${item.key}" href="#" title="Administer fragment ${item.value}"><span>${item.value}</span></a></li>
            </c:forEach>
          </ul>
          -->
        
        <!-- Original form. Renders a select dropdown.-->
        <form method="POST" name="fragmentAdminForm" action="<portlet:actionURL><portlet:param name="action" value="becomeFragmentOwner"/></portlet:actionURL>">
            <select id="fragmentOwner" name="impersonateUser" title="Choose a fragment to edit">
                <option value="NONE"> -- fragments -- </option>
                <c:forEach items="${FRAGMENTS}" var="item">
                    <option value="${item.key}">${item.value}</option>
                </c:forEach>
            </select>
            <input type="Button" value="Go" onclick="if (document.fragmentAdminForm.fragmentOwner.options[document.fragmentAdminForm.fragmentOwner.selectedIndex].value != 'NONE') document.fragmentAdminForm.submit()"/>
        </form>
        
        
    	</div>                
    </div>
</div>
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