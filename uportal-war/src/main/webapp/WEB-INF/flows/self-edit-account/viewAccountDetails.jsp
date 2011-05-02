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

<%@ include file="/WEB-INF/jsp/include.jsp" %>
<portlet:renderURL var="editDetailsUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="editAccount"/>
</portlet:renderURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet user-mgr view-reviewuser" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="my.account"/></h2>
        <c:if test="${ canEditAccount }">
            <div class="toolbar">
                <ul>
                    <li><a class="button" href="${ editDetailsUrl }"><spring:message code="edit"/></a></li>
                </ul>
            </div>
        </c:if>
    </div> <!-- end: portlet-titlebar -->
    
    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">

        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="content">
            
                <table class="portlet-table">
                    <tbody>

                        <!-- Print out each attribute -->
                        <c:forEach items="${ attributeNames }" var="attribute">
                            <tr>
                                <td class="attribute-name">
                                    <spring:message code="attribute.displayName.${ attribute.key }"/>
                                </td>
                                <td>
                                    <c:if test="${ fn:length(person.attributeMap[attribute.key]) > 0 }">
                                        ${ fn:escapeXml(person.attributeMap[attribute.key][0]) }
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        
                    </tbody>
                </table>

            </div>
        </div>
    </div>

</div>
