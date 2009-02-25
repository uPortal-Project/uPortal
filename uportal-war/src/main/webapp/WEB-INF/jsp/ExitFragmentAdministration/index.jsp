<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>

<portlet:defineObjects/>

<div id="portalFragAdminExit" class="block">
  <div class="block-inner">
    <h2 class="block-title">Fragment Administration</h2>
    <div class="block-content">
      <form name="fragmentAdminExitForm" action="${loginUrl}">
      	<label for="exitFragment">You are currently logged in as <strong><c:out value="${USERNAME}"/></strong> for DLM fragment administration</label>
      	<input id="exitFragment" type="Submit" value="Exit Fragment"/>
      </form>
    </div>
  </div>
</div>
