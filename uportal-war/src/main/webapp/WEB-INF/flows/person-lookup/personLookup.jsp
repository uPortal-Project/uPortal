<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<portlet:actionURL var="queryUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>

<div class="fl-widget portlet" role="section">
    <div class="fl-widget-titlebar portlet-title" role="sectionhead">
        <h2 role="heading"><spring:message code="personLookup.searchTitle" /></h2>
    </div>
    
    <div class="fl-widget-content portlet-body" role="main">
    
        <div class="portlet-section" role="region">
        
            <div class="portlet-section-body">
        
				<c:if test="${emptyQueryResults == 'true'}">
				    <div class="portlet-msg-info" role="status">
				        <spring:message code="personLookup.emptyResults" />
				    </div>
				</c:if>

				<form:form modelAttribute="personQuery" action="${queryUrl}">
				    <table>
				        <tbody>
				            <c:forEach var="queryAttribute" items="${queryAttributes}">
				                <tr>
				                    <td>
				                        <form:label path="attributes['${queryAttribute}'].value"><spring:message code="${queryAttribute}" text="${queryAttribute}" arguments="${queryAttribute}"/></form:label>
				                    </td>
				                    <td>
				                        <form:input path="attributes['${queryAttribute}'].value"/>
				                    </td>
				                    <td>
				                        <form:errors path="attributes['${queryAttribute}'].value"/>
				                    </td>
				                </tr>
				            </c:forEach>
				        </tbody>            
				    </table>

                    <div class="portlet-button-group">
	                    <spring:message var="searchButtonText" code="personLookup.searchButton" />
	                    <input class="portlet-button portlet-button-primary" type="submit" class="button" name="_eventId_search" value="${searchButtonText}" />
	                    
	                    <spring:message var="newSearchButtonText" code="personLookup.newSearchButton" />
	                    <input class="portlet-button secondary" type="submit" class="button" name="_eventId_newSearch" value="${newSearchButtonText}" />
	                    
	                    <c:if test="${showCancelButton == 'true'}">
	                        <spring:message var="cancelButtonText" code="personLookup.cancelButton" />
	                        <input class="portlet-button" type="submit" class="button" name="_eventId_cancel" value="${cancelButtonText}" />
	                    </c:if>
                    </div>
				    
				</form:form>
            </div>
        </div>
    </div>
</div>