<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<c:set var="ns"><portlet:namespace/></c:set>

<style type="text/css">
    .person-lookup .attributeItem {
        font-size: 75%;
    }
</style>


<div id="${ns}_person-lookup" class="person-lookup">
<portlet:renderURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="newSearch" />
</portlet:renderURL>
<spring:message var="backToResultsLinkText" code="personSearchResults.newSearchLink" />
<a href="${newSearchUrl}">${backToResultsLinkText}</a>

<div><spring:message code="personSearchResults.resultsTitle" arguments="${fn:length(queryDisplayResults)}"/></div>

<ul>
    <c:forEach var="personDisplayEntry" items="${queryDisplayResults}">
        <li>
            <portlet:actionURL var="selectPersonUrl">
                <portlet:param name="execution" value="${flowExecutionKey}" />
                <portlet:param name="_eventId" value="select" />
                <portlet:param name="uid" value="${personDisplayEntry.key.name}" />
            </portlet:actionURL>
            <portlet:actionURL var="selectAndGoPersonUrl">
                <portlet:param name="execution" value="${flowExecutionKey}" />
                <portlet:param name="_eventId" value="selectAndGo" />
                <portlet:param name="uid" value="${personDisplayEntry.key.name}" />
            </portlet:actionURL>
            
            <spring:message text="${personDisplayEntry.value}"/>
            <ul style="margin-top: 0;">
                <li>
                    <a href="${selectAndGoPersonUrl}"><spring:message code="${personSearchResults_selectAndGoButtonTextKey}"/></a>
                    ·
                    <a href="${selectPersonUrl}"><spring:message code="personSearchResults.viewDetailsLink"/></a>
                </li>

                <c:set var="moreInfoAttributes" value="${renderRequest.preferences.map['person-lookup.personSearchResults.moreInfoAttributes']}" />
                <c:if test="${not empty moreInfoAttributes}">
                    <c:forEach var="moreInfoAttr" items="${moreInfoAttributes}">
                        <c:if test="${not empty personDisplayEntry.key.attributes[moreInfoAttr]}">
                            <li class="attributeItem">
                                <span><spring:message text="${moreInfoAttr}"/>: </span>
                                <span>
                                    <c:forEach var="attrValue" items="${personDisplayEntry.key.attributes[moreInfoAttr]}" varStatus="attrValueStatus">
                                        <spring:message text="${attrValue}"/>
                                        <c:if test="${not attrValueStatus.last}">, </c:if>
                                    </c:forEach>
                                </span>
                            </li>
                        </c:if>
                    </c:forEach>
                </c:if>
            </ul>
        </li>
    </c:forEach>
</ul>

<portlet:renderURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="newSearch" />
</portlet:renderURL>
<a href="${newSearchUrl}">${backToResultsLinkText}</a>

</div>
