<%@ include file="/WEB-INF/jsp/include.jsp"%>

<portlet:actionURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="personLookup" />
</portlet:actionURL>
<div><a href="${newSearchUrl}"><spring:message code="attributesForm.lookupPersonLink" /></a></div>

<portlet:actionURL var="attributeSwapUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<form:form modelAttribute="attributeSwapRequest" action="${attributeSwapUrl}">
    <table>
        <thead>
            <tr>
                <th><spring:message code="attributesForm.attributeHeader" /></th>
                <th><spring:message code="attributesForm.originalValueHeader" /></th>
                <th><spring:message code="attributesForm.currentValueHeader" /></th>
                <c:if test="${targetUserDetails != null}">
                    <th><spring:message code="attributesForm.copyAttributeHeader" /></th>
                    <th><spring:message code="attributesForm.valueForHeader" arguments="${uid}" /></th>
                </c:if>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="swappableAttribute" items="${swappableAttributes}">
                <tr>
                    <td><spring:message code="${swappableAttribute}" text="${swappableAttribute}" /></td>
                    <td>${fn:escapeXml(baseUserDetails.attributes[swappableAttribute])}</td>
                    <td><form:input path="currentAttributes['${swappableAttribute}'].value" /></td>
                    <c:if test="${targetUserDetails != null}">
                        <td><form:checkbox path="attributesToCopy['${swappableAttribute}'].value" value="${targetUserDetails.attributes[swappableAttribute]}" /></td>
                        <td>${fn:escapeXml(targetUserDetails.attributes[swappableAttribute])}</td>
                    </c:if>
                </tr>
            </c:forEach>
        </tbody>
    </table>
        
    <spring:message var="updateAttributesText" code="attributesForm.updateAttributesButton" />
    <input type="submit" class="button" name="_eventId_updateAttributes" value="${updateAttributesText}" />
    
    <spring:message var="resetAttributesText" code="personLookup.resetAttributesButton" />
    <input type="submit" class="button" name="_eventId_resetAttributes" value="${resetAttributesText}" />
</form:form>