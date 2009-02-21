<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>

<portlet:defineObjects/>

<p>| <a href="<portlet:renderURL><portlet:param name="view" value="import"/></portlet:renderURL>">Import</a> | <a href="<portlet:renderURL><portlet:param name="view" value="export"/></portlet:renderURL>">Export</a> | <a href="<portlet:renderURL><portlet:param name="view" value="delete"/></portlet:renderURL>">Delete</a> |</p>

<p>Status of your <c:out value="${operation}"/> operation:  <c:out value="${result}"/></p>

<c:if test="${message != null}">
    <p><c:out value="${message}"/></p>
</c:if>

<c:if test="${downloadUrl != null}">
    <p><a href="<c:out value="${downloadUrl}"/>">Get It!</a></p>
</c:if>
