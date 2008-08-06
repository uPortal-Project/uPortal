<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>

<portlet:defineObjects/>

<div class="block" id="fragAdminContainer">
    <div class="block-inner">
        <div class="block-content">

        <form name="fragmentAdminExitForm" action="${loginUrl}">
            <label for="exitFragment">You are currently logged in as <strong><c:out value="${USERNAME}"/></strong> for DLM fragment administration</label>
            <input id="exitFragment" type="Submit" value="Exit Fragment"/>
        </form>
                    
        </div>
    </div>
</div>
