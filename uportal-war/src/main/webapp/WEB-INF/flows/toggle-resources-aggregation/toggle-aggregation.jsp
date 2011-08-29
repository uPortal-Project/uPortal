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
<portlet:actionURL var="actionUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
    
<!-- Portlet -->
<div class="fl-widget portlet toggle-aggr view-main" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading">Toggle Aggregation</h2>
    </div>
    
    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">
	
        <div class="portlet-form">
            <form action="${actionUrl}" method="POST">
                <p>
                    CSS/JavaScript Aggregation is currently 
                    <strong>
                        <c:choose>
                        	<c:when test="${aggregationEnabled}">enabled</c:when>
                        	<c:otherwise>disabled</c:otherwise>
                        </c:choose>
                    </strong>
                    .
                <p> 
                <c:choose>
                    <c:when test="${aggregationEnabled}">
                        <input type="hidden" name="newAggregationValue" value="false"/> 
                        <input class="button" type="submit" value="Disable Aggregation" name="_eventId_disableAggregation"/>
                    </c:when>
                    <c:otherwise>
                    	<input type="hidden" name="newAggregationValue" value="true"/> 
                    	<input class="button" type="submit" value="Enable Aggregation" name="_eventId_enableAggregation"/>
                    </c:otherwise>
                </c:choose> 
            </form>
        </div>
    
	</div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->