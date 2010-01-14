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
<div class="fl-widget portlet" role="section">

    <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">HTTP Request</h3>
      <div class="portlet-section-body">
        <table>
            <thead>
                <tr><th>Name</th><th>Value</th></tr>
            </thead>
            <tbody>
	            <tr><td>Request Protocol</td><td>${ request.protocol }</td></tr>
	            <tr><td>Request Method</td><td>${ request.method }</td></tr>
	            <tr><td>Server Name</td><td>${ request.serverName }</td></tr>
	            <tr><td>Server Port</td><td>${ request.serverPort }</td></tr>
	            <tr><td>Request URI</td><td>${ request.requestURI }</td></tr>
	            <tr><td>Context Path</td><td>${ request.contextPath }</td></tr>
	            <tr><td>Servlet Path</td><td>${ request.servletPath }</td></tr>
	            <tr><td>Query String</td><td>${ request.queryString }</td></tr>
	            <tr><td>Path Info</td><td>${ request.pathInfo }</td></tr>
	            <tr><td>Path Translated</td><td>${ request.pathTranslated }</td></tr>
	            <tr><td>Content Length</td><td>${ request.contentLength }</td></tr>
	            <tr><td>Content Type</td><td>${ request.contentType }</td></tr>
	            <tr><td>Remote User</td><td>${ request.remoteUser }</td></tr>
	            <tr><td>Remote Address</td><td>${ request.remoteAddr }</td></tr>
	            <tr><td>Remote Host</td><td>${ request.remoteHost }</td></tr>
	            <tr><td>Authorization Scheme</td><td>${ request.authType }</td></tr>
	            <tr><td>Locale</td><td>${ request.locale }</td></tr>
            </tbody>
        </table>
      </div>
    </div> <!-- end: portlet-section -->

    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">HTTP Header Info</h3>
      <div class="portlet-section-body">
        <table>
            <thead>
                <tr><th>Name</th><th>Value</th></tr>
            </thead>
            <tbody>
	            <c:forEach items="${ header }" var="h">
	                <tr><td>${ h.key }</td><td>${ h.value }</td></tr>
	            </c:forEach>
	        </tbody>
        </table>
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Locales</h3>
      <div class="portlet-section-body">
        <table>
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
		                <td>${ locale.displayName }</td>
		                <td>${ locale.locale }</td>
		                <td>${ locale.displayLanguage } (${ locale.locale.language }, ${ locale.locale.ISO3Language })</td>
	                    <td>${ locale.displayCountry } (${ locale.locale.country }, ${ locale.locale.ISO3Country })</td>
	                    <td>${ not empty local.locale.variant ? locale.displayVariant + ' (' + locale.locale.variant + ')' : '' }</td>
	                </tr>
	            </c:forEach>
            </tbody>
        </table>
      </div>
    </div> <!-- end: portlet-section -->

  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->
      