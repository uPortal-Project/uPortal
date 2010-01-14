<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

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