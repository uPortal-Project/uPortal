<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<c:set var="ns"><portlet:namespace/></c:set>

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
        <li style="padding-bottom: 0.5em;">
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
            
            <spring:message text="${personDisplayEntry.value}"/><br/> 
            · <a href="${selectAndGoPersonUrl}"><spring:message code="${personSearchResults_selectAndGoButtonTextKey}"/></a><br/>
            <c:set var="moreInfoAttributes" value="${renderRequest.preferences.map['person-lookup.personSearchResults.moreInfoAttributes']}" />
            <c:if test="${not empty moreInfoAttributes}"> 
                · <a class="more-info-link" href="${selectPersonUrl}"><spring:message code="personSearchResults.moreInfoLink"/></a>
                <div class="more-info" style="padding-bottom: 1em;">
                    · <a href="${selectPersonUrl}"><spring:message code="personSearchResults.viewDetailsLink"/></a>
                    <br/>
                    <table border="1">
                        <thead>
                            <tr>
                                <th><spring:message code="personSearchResults.attributeHeader"/></th>
                                <th><spring:message code="personSearchResults.valuesHeader"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="moreInfoAttr" items="${moreInfoAttributes}">
                                <tr>
                                    <td><spring:message text="${moreInfoAttr}"/></td>
                                    <td>
                                        <c:forEach var="attrValue" items="${personDisplayEntry.key.attributes[moreInfoAttr]}" varStatus="attrValueStatus">
                                            <spring:message text="${attrValue}"/>
                                            <c:if test="${not attrValueStatus.last}"><br/></c:if>
                                        </c:forEach>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
        </li>
    </c:forEach>
</ul>

<portlet:renderURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="newSearch" />
</portlet:renderURL>
<a href="${newSearchUrl}">${backToResultsLinkText}</a>

</div>

<script type="text/javascript">
up.jQuery(document).ready(function() {
    up.jQuery.uportal.personLookup_searchResults(
    {
        moreInfoLinkVisibleText:'<spring:message code="personSearchResults.hideInfoLink"/>',
        moreInfoLinkHiddenText: '<spring:message code="personSearchResults.moreInfoLink"/>',
        moreInfoLinkSelector:   '#${ns}_person-lookup .more-info-link',
        moreInfoSelector:       '#${ns}_person-lookup .more-info'
    });
});
</script>
