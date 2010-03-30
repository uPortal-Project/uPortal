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

<!-- Portlet -->
<div class="fl-widget portlet prs-lkp view-lookup" role="section">

	<!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="personLookup.searchTitle" /></h2>
    </div>
    
    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">
        
        <c:if test="${emptyQueryResults == 'true'}">
            <div class="portlet-msg-info" role="status">
                <spring:message code="personLookup.emptyResults" />
            </div>
        </c:if>
		
        <div class="portlet-form">
            <form:form modelAttribute="personQuery" action="${queryUrl}">
                <table class="purpose-layout">
                    <tbody>
                        <c:forEach var="queryAttribute" items="${queryAttributes}">
                            <tr>
                                <td class="label">
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
				
                <!-- Buttons -->
                <div class="buttons">
                    <spring:message var="searchButtonText" code="personLookup.searchButton" />
                    <input class="button primary" type="submit" class="button" name="_eventId_search" value="${searchButtonText}" />
                    
                    <spring:message var="newSearchButtonText" code="personLookup.newSearchButton" />
                    <input class="button" type="submit" class="button" name="_eventId_newSearch" value="${newSearchButtonText}" />
                    
                    <c:if test="${showCancelButton == 'true'}">
                        <spring:message var="cancelButtonText" code="personLookup.cancelButton" />
                        <input class="button" type="submit" class="button" name="_eventId_cancel" value="${cancelButtonText}" />
                    </c:if>
                </div>
                
            </form:form>
        </div>

    </div>
</div>