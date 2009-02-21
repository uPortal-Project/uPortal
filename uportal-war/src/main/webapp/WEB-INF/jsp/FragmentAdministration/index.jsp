<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>

<portlet:defineObjects/>

<div id="portalFragAdminList" class="block">
    <div class="block-inner">
        
        <h2 class="block-title">Fragment Administration</h2>
        <div class="block-content">
          <!--<ul>
            <c:forEach items="${FRAGMENTS}" var="item">
              <li><a href="Login?impersonateUser=${item.key}"><span>${item.value}</span></a></li>
            </c:forEach>
          </ul>-->
        
        <!-- Renders a select dropdown.-->
        <form method="POST" name="fragmentAdminForm" action="<portlet:actionURL><portlet:param name="action" value="becomeFragmentOwner"/></portlet:actionURL>">
            <select id="fragmentOwner" name="impersonateUser" title="Choose a fragment to edit">
                <option value="NONE"> -- fragments -- </option>
                <c:forEach items="${FRAGMENTS}" var="item">
                    <option value="${item.key}">${item.value}</option>
                </c:forEach>
            </select>
            <input type="Button" value="GO" onclick="if (document.fragmentAdminForm.fragmentOwner.options[document.fragmentAdminForm.fragmentOwner.selectedIndex].value != 'NONE') document.fragmentAdminForm.submit()"/>
        </form>
        
        
    	</div>                
    </div>
</div>
