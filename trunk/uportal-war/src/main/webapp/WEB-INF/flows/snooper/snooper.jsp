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

<!-- Portlet -->
<div class="fl-widget portlet snooper view-main" role="section">

	
    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading">Snooper Information</h2>
    </div>
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">
  
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading">HTTP Request</h3>
            </div>
            <div class="content">
                <table class="portlet-table">
                    <thead>
                    	<tr>
                        	<th>Name</th>
                        	<th>Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Request Protocol</td>
                            <td>${ fn:escapeXml(request.protocol )}</td>
                        </tr>
                        <tr>
                        	<td>Request Method</td>
                        	<td>${ fn:escapeXml(request.method )}</td>
                        </tr>
                        <tr>
                        	<td>Server Name</td>
                        	<td>${ fn:escapeXml(request.serverName )}</td>
                        </tr>
                        <tr>
                        	<td>Server Port</td>
                        	<td>${ fn:escapeXml(request.serverPort )}</td>
                        </tr>
                        <tr>
                        	<td>Request URI</td>
                        	<td>${ fn:escapeXml(request.requestURI )}</td>
                        </tr>
                        <tr>
                        	<td>Context Path</td>
                        	<td>${ fn:escapeXml(request.contextPath )}</td>
                        </tr>
                        <tr>
                        	<td>Servlet Path</td>
                        	<td>${ fn:escapeXml(request.servletPath )}</td>
                        </tr>
                        <tr>
                        	<td>Query String</td>
                        	<td>${ fn:escapeXml(request.queryString )}</td>
                        </tr>
                        <tr>
                        	<td>Path Info</td>
                        	<td>${ fn:escapeXml(request.pathInfo )}</td>
                        </tr>
                        <tr>
                        	<td>Path Translated</td>
                        	<td>${ fn:escapeXml(request.pathTranslated )}</td>
                        </tr>
                        <tr>
                        	<td>Content Length</td>
                        	<td>${ fn:escapeXml(request.contentLength )}</td>
                        </tr>
                        <tr>
                        	<td>Content Type</td>
                        	<td>${ fn:escapeXml(request.contentType )}</td>
                        </tr>
                        <tr>
                        	<td>Remote User</td>
                        	<td>${ fn:escapeXml(request.remoteUser )}</td>
                        </tr>
                        <tr>
                        	<td>Remote Address</td>
                        	<td>${ fn:escapeXml(request.remoteAddr )}</td>
                        </tr>
                        <tr>
                        	<td>Remote Host</td>
                        	<td>${ fn:escapeXml(request.remoteHost )}</td>
                        </tr>
                        <tr>
                        	<td>Authorization Scheme</td>
                        	<td>${ fn:escapeXml(request.authType )}</td>
                        </tr>
                        <tr>
                        	<td>Locale</td>
                        	<td>${ fn:escapeXml(request.locale )}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading">HTTP Header Info</h3>
            </div>
            <div class="content">
                <table class="portlet-table">
                    <thead>
                    	<tr>
                        	<th>Name</th>
                        	<th>Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${ header }" var="h">
                            <tr>
                                <td>${ fn:escapeXml(h.key )}</td>
                                <td>${ fn:escapeXml(h.value )}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
        	</div>
        </div>
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading">Locales</h3>
            </div>
            <div class="content">
                <table class="portlet-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Code</th>
                            <th>Language</th>
                            <th>Country</th>
                            <th>Variant</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${ locales }" var="locale">
                            <tr>
                            	<td>${ fn:escapeXml(locale.displayName )}</td>
                            	<td>${ fn:escapeXml(locale.locale )}</td>
                            	<td>${ fn:escapeXml(locale.displayLanguage )} (${ fn:escapeXml(locale.locale.language )}, ${ fn:escapeXml(locale.locale.ISO3Language )})</td>
                            	<td>${ fn:escapeXml(locale.displayCountry )} (${ fn:escapeXml(locale.locale.country )}, ${ fn:escapeXml(locale.locale.ISO3Country )})</td>
                            	<td>${ fn:escapeXml(not empty local.locale.variant ? locale.displayVariant + ' (' + locale.locale.variant + ')' : '' )}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
        	</div>
        </div>

	</div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->
      