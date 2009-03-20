<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

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