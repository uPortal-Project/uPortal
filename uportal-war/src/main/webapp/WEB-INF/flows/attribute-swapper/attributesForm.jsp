<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<portlet:actionURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="personLookup" />
</portlet:actionURL>
<portlet:actionURL var="attributeSwapUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>

<div class="fl-widget portlet" role="section">
	<div class="fl-col-flex2 portlet-toolbar" role="toolbar">
	    <div class="fl-col">
	        <ul>
	            <li>
	                <a href="${newSearchUrl}"><spring:message code="attributesForm.lookupPersonLink" /></a>
	            </li>
	        </ul>
        </div>
	</div>
	
	<div class="fl-widget-content portlet-body" role="main">
	
		<div class="portlet-section" role="region">
		
		    <div class="portlet-section-body">
		
				<form:form modelAttribute="attributeSwapRequest" action="${attributeSwapUrl}">
				    <table>
				        <thead>
				            <tr>
				                <th><spring:message code="attributesForm.attributeHeader" /></th>
				                <th><spring:message code="attributesForm.originalValueHeader" /></th>
				                <th><spring:message code="attributesForm.currentValueHeader" /></th>
				                <c:if test="${targetUserDetails != null}">
				                    <th><spring:message code="attributesForm.copyAttributeHeader" /></th>
				                    <th><spring:message code="attributesForm.valueForHeader" arguments="${targetUserDetails.name}" /></th>
				                </c:if>
				            </tr>
				        </thead>
				        <tbody>
				            <c:forEach var="swappableAttribute" items="${swappableAttributes}">
				                <tr>
				                    <td><spring:message code="${swappableAttribute}" text="${swappableAttribute}" arguments="${swappableAttribute}"/></td>
				                    <td>${fn:escapeXml(baseUserDetails.attributes[swappableAttribute][0])}</td>
				                    <td><form:input path="currentAttributes['${swappableAttribute}'].value" /></td>
				                    <c:if test="${targetUserDetails != null}">
				                        <c:choose>
				                            <c:when test="empty targetUserDetails.attributes[swappableAttribute]">
				                                <c:set var="attributeToCopy" scope="page" value="" />
				                            </c:when>
				                            <c:otherwise>
				                                <c:set var="attributeToCopy" scope="page" value="${targetUserDetails.attributes[swappableAttribute][0]}" />
				                            </c:otherwise>
				                        </c:choose>
				                        <td>
				                            <c:if test="${not empty attributeToCopy}">
				                                <form:checkbox path="attributesToCopy['${swappableAttribute}'].value" value="${attributeToCopy}" />
				                            </c:if>
				                        </td>
				                        <td>
				                            <label for="attributesToCopy['${fn:escapeXml(swappableAttribute)}'].value1">${fn:escapeXml(targetUserDetails.attributes[swappableAttribute][0])}</label>
				                        </td>
				                    </c:if>
				                </tr>
				            </c:forEach>
				        </tbody>
				    </table>
				        
				    <div class="portlet-button-group">
					    <spring:message var="updateAttributesText" code="attributesForm.updateAttributesButton" />
					    <input type="submit" class="portlet-button portlet-button-primary" name="_eventId_updateAttributes" value="${updateAttributesText}" />
					    
					    <spring:message var="resetAttributesText" code="personLookup.resetAttributesButton" />
					    <input type="submit" class="portlet-button" name="_eventId_resetAttributes" value="${resetAttributesText}" />
				    </div>
				</form:form>
			</div>
		</div>
	</div>
</div>