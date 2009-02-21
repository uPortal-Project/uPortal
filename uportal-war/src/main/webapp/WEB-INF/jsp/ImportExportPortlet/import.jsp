<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>

<portlet:defineObjects/>

<p>| <b>Import</b> | <a href="<portlet:renderURL><portlet:param name="view" value="export"/></portlet:renderURL>">Export</a> | <a href="<portlet:renderURL><portlet:param name="view" value="delete"/></portlet:renderURL>">Delete</a> |</p>

<p>Use this form to import portal entities through this Portlet.</p>

<form method="POST" enctype="multipart/form-data" action="<portlet:actionURL><portlet:param name="action" value="doImport"/><portlet:param name="view" value="status"/></portlet:actionURL>">

<hr/>

<label for="entityFile">File:</label>
<input type="file" id="entityFile" name="entityFile"/>

<hr/>

<input type="submit" value="Import"/>

</form>

<p><strong>NOTE:</strong>  You can allow/disallow entity types using Portlet 
Preferences.  See uPortal's portlet.xml file for details.</p>
