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
<portlet:actionURL var="queryUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
    
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">Set Language Preference</h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
        <form action="${queryUrl}" method="POST">
			<ul style="margin:0">
				<c:forEach items="${ locales }" var="locale">
				    <li style="list-style:none;padding:0.2em 0 0.2em 0">
					    <input type="radio" name="locale" value="${ locale.code }" ${ locale.code == currentLocale ? "checked" : '' }/>
					    <img src="/ResourceServingWebapp/rs/famfamfam/flags/${ fn:toLowerCase(locale.locale.country) }.png"/>
					    ${ locale.displayLanguage }
				    </li>
				</c:forEach>
			</ul>
            <div class="portlet-button-group">
		  	   <input class="portlet-button portlet-button-primary" type="submit" value="Update" name="_eventId_updateLocale"/>
		  	</div>
		</form>
      </div>  
    </div>
    
  </div>

</div>
