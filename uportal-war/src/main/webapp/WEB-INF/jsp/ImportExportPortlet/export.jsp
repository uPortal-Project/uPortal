<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>

<portlet:defineObjects/>

<p>| <a href="<portlet:renderURL><portlet:param name="view" value="import"/></portlet:renderURL>">Import</a> | <b>Export</b> | <a href="<portlet:renderURL><portlet:param name="view" value="delete"/></portlet:renderURL>">Delete</a> |</p>

<p>Use this form to export portal entities through this Portlet.</p>

<form method="POST" action="<portlet:actionURL><portlet:param name="action" value="doExport"/><portlet:param name="view" value="status"/></portlet:actionURL>">

<hr/>

<label for="entityType">Type:</label>
<select id="entityType" name="entityType">
    <option>[Select Type]</option>
    <c:forEach items="${supportedTypes}" var="type">
        <option value="${type}"><c:out value="${type}"/></option>
    </c:forEach>
</select>

<label for="sysid">Id:</label>
<input type="text" id="sysid" name="sysid"/>

<hr/>

<input type="submit" value="Export"/>

</form>

<p><strong>NOTE:</strong>  You can allow/disallow entity types using Portlet 
Preferences.  See uPortal's portlet.xml file for details.</p>
