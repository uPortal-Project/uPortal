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

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="basicInfoUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="basicInfo"/>
</portlet:actionURL>
<portlet:actionURL var="chooseTypeUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="chooseType"/>
</portlet:actionURL>
<portlet:actionURL var="configModeUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="configMode"/>
</portlet:actionURL>
<portlet:actionURL var="setParametersUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="setParameters"/>
</portlet:actionURL>
<portlet:actionURL var="chooseGroupUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="chooseGroup"/>
</portlet:actionURL>
<portlet:actionURL var="chooseCategoryUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="chooseCategory"/>
</portlet:actionURL>
<portlet:actionURL var="lifecycleUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="lifecycle"/>
</portlet:actionURL>
<portlet:actionURL var="cancelUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="cancel"/>
</portlet:actionURL>
<portlet:actionURL var="saveUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="save"/>
</portlet:actionURL>
<portlet:actionURL var="saveAndConfigUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="saveAndConfig"/>
</portlet:actionURL>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<!--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| http://www.ja-sig.org/wiki/x/cQ
-->

<!-- Portlet -->
<div class="fl-widget portlet" role="section">

	<!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
  	<h2 role="heading"><c:out value="${ channel.title }"/></h2>
  </div> <!-- end: portlet-title -->
  
	<!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
  	<!-- General Configuration Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="reviewPortlet.heading"/></h3>
      <div class="portlet-section-options">
        <a href="${ basicInfoUrl }"><span><spring:message code="reviewPortlet.editButton"/></span></a>
      </div>
      <div class="portlet-section-body">
      
        <table summary="<spring:message code="reviewPortlet.configurationTableSummary"/>">
          <thead>
            <tr>
              <th><spring:message code="reviewPortlet.configurationHeading"/></th>
              <th><spring:message code="reviewPortlet.valueHeading"/></th>
            <tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.channelTitle"/></td>
              <td><a href="${ basicInfoUrl }" title="<c:out value="${ channel.title }"/>" class="pa-edit"><c:out value="${ channel.title }"/></a></td>
            </tr>
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.channelName"/></td>
              <td><a href="${ basicInfoUrl }" title="<c:out value="${ channel.name }"/>" class="pa-edit"><c:out value="${ channel.name }"/></a></td>
            </tr>
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.channelFName"/></td>
              <td><a href="${ basicInfoUrl }" title="<c:out value="${ channel.fname }"/>" class="pa-edit"><c:out value="${ channel.fname }"/></a></td>
            </tr>
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.channelDescription"/></td>
              <td><a href="${ basicInfoUrl }" title="<c:out value="${ channel.description }"/>" class="pa-edit"><c:out value="${ channel.description }"/></a></td>
            </tr> 
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.channelTimeout"/></td>
              <td><a href="${ basicInfoUrl }" title="<c:out value="${ channel.timeout }"/>" class="pa-edit"><c:out value="${ channel.timeout }"/></a></td>
            </tr>
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.channelSecure"/></td>
              <td><a href="${ basicInfoUrl }" title="<c:out value="${ channel.secure }"/>" class="pa-edit"><c:out value="${ channel.secure }"/></a></td>
            </tr> 
            <tr>
              <td class="fl-text-align-right"><spring:message code="reviewPortlet.channelType"/></td>
              <td>
                <c:forEach items="${ channelTypes }" var="type">
                  <c:if test="${ type.key.id == channel.typeId }">
                    <a href="${ chooseTypeUrl }" title="${ channel.typeId }" class="pa-edit"><c:out value="${ type.key.name }"/></a>
                    <c:if test="${ type.key.id == -1 }">
                      <a href="${ chooseTypeUrl }" title="${ channel.typeId }" class="pa-edit">(<c:out value="${ channel.javaClass }"/>)</a>
                    </c:if>
                  </c:if>
                </c:forEach>
              </td>
            </tr>
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.editableHeading"/></td>
              <td><a href="${ basicInfoUrl }" title="${ channel.editable }" class="pa-edit">${ channel.editable }</a></td>
            </tr> 
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.hasHelpHeading"/></td>
              <td><a href="${ basicInfoUrl }" title="${ channel.hasHelp }" class="pa-edit">${ channel.hasHelp }</a></td>
            </tr>  
            <tr>
              <td class="fl-text-align-right"><spring:message code="basicInfo.hasAboutHeading"/></td>
              <td><a href="${ basicInfoUrl }" title="${ channel.hasAbout }" class="pa-edit">${ channel.hasAbout }</a></td>
            </tr>
          </tbody>
        </table>
        
      </div>
    </div>
    <!-- END: General Configuration Section -->
    
    
    <!-- Channel Parameters Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="setParameters.parametersHeading"/></h3>
      <div class="portlet-section-options">
        <a href="${ setParametersUrl }"><span><spring:message code="reviewPortlet.editParametersButton"/></span></a>
      </div>
      <div class="portlet-section-body">
      
        <table summary="<spring:message code="reviewPortlet.configurationTableSummary"/>">
          <thead>
            <tr>
              <th><spring:message code="reviewPortlet.configurationHeading"/></th>
              <th><spring:message code="setParameters.valueHeading"/></th>
              <th><spring:message code="setParameters.userEditableHeading"/></th>
            </tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <c:forEach items="${ cpd.params.steps }" var="step">
              <c:forEach items="${ step.parameters }" var="parameter">
                <c:if test="${ (parameter.modify != 'subscribeOnly' && parameter.type.display != 'hidden') && ((channel.parameters[parameter.name].value != null && channel.parameters[parameter.name].value != '') || (fn:startsWith(parameter.name, 'PORTLET.') && channel.portletPreferences[fn:replace(parameter.name, 'PORTLET.', '')].value != null && channel.portletPreferences[fn:replace(parameter.name, 'PORTLET.', '')].value != '')) }">
                  <tr>
                    <td class="fl-text-align-right"><c:out value="${ parameter.label }"/>:</td>
                    <td>
                        <a href="${ setParametersUrl }" class="pa-edit">
                            <c:choose>
	                            <c:when test="${ fn:startsWith(parameter.name, 'PORTLET.') }">
	                               <c:set var="values" value="${channel.portletPreferences[fn:replace(parameter.name, 'PORTLET.', '')].value}"/>
	                               <c:out value="${ fn:length(values) > 0 ? values[0] : '' }"/>
	                            </c:when>
	                            <c:otherwise>
    	                            <c:out value="${ channel.parameters[parameter.name].value }"/>
	                            </c:otherwise>
                            </c:choose>
                        </a>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${ fn:startsWith(parameter.name, 'PORTLET.') }">
                                ${ channel.portletPreferencesOverrides[fn:replace(parameter.name, 'PORTLET.', '')].value ? 'X' : '' }
                            </c:when>
                            <c:otherwise>
                                ${ channel.parameterOverrides[parameter.name].value ? 'X' : '' }
                            </c:otherwise>
                        </c:choose>
                    </td>
                  </tr>
                </c:if>
              </c:forEach>
              <c:forEach items="${ step.arbitraryParameters }" var="arbitraryParam">
                <c:forEach items="${ arbitraryParam.paramNamePrefixes }" var="prefix">
                  <c:forEach items="${ channel.parameters }" var="channelParam">
                    <c:if test="${ fn:startsWith(channelParam.key, prefix) }">
                      <tr>
                        <td class="fl-text-align-right"><c:out value="${ channelParam.key }"/>:</td>
                        <td><a href="${ setParametersUrl }" class="pa-edit"><c:out value="${ channelParam.value }"/></a></td>
                        <td>${ channel.parameterOverrides[parameter.name].value ? 'X' : '' }</td>
                      </tr>
                    </c:if>
                  </c:forEach>
                </c:forEach>
              </c:forEach>
            </c:forEach>
          </tbody>
        </table>
        
      </div>
    </div>
    <!-- END: Channel Parameters Section -->
    
    <c:if test="${ channel.portlet }">
      <!-- Portlet.xml Preferences Section -->
      <div class="portlet-section" role="region">
        <h3 class="portlet-section-header" role="heading"><spring:message code="setParameters.xmlPreferencesHeader"/></h3>
        <div class="portlet-section-body">
          <table>
            <thead>
              <tr>
                <th><spring:message code="setParameters.preferencesHeading"/></th>
                <th><spring:message code="setParameters.valuesHeading"/></th>
                <th><spring:message code="setParameters.userEditableHeading"/></th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${ portlet.portletPreferences.portletPreferences }" var="pref">
                <tr class="${ up:containsKey(channel.portletPreferences, pref.name) ? 'override-preference' : '' }">
                  <td class="preference-name">${ pref.name }</td>
                  <td>
                    <c:forEach var="value" items="${ pref.values }">
                        <div>${ value }</div>
                    </c:forEach>
                  </td>
                  <td>${ !pref.readOnly }</td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
      <!-- END: Portlet.xml Preferences Section -->
    
      <!-- Portlet Preferences Section -->
      <div class="portlet-section" role="region">
        <h3 class="portlet-section-header" role="heading"><spring:message code="setParameters.preferencesHeader"/></h3>
        <div class="portlet-section-options">
          <c:choose>
            <c:when test="${supportsConfig and channel.id >= 0}">
              <a href="${ configModeUrl }"><span><spring:message code="reviewPortlet.enterConfigModeButton"/></span></a>
            </c:when>
            <c:otherwise>
              <a href="${ setParametersUrl }"><span>Edit Preferences</span></a>
            </c:otherwise>
          </c:choose>
        </div>
        <div class="portlet-section-body">
        
          <table summary="<spring:message code="reviewPortlet.configurationTableSummary"/>">
            <thead>
              <tr>
                <th><spring:message code="setParameters.preferencesHeading"/></th>
                <th><spring:message code="setParameters.valuesHeading"/></th>
                <th><spring:message code="setParameters.userEditableHeading"/></th>
              </tr>
            </thead>
            <tfoot></tfoot>
            <tbody>
              <c:forEach items="${ arbitraryPreferenceNames }" var="name">
                <c:set var="paramPath" value="portletPreferences['${ name }'].value"/>
                <c:set var="overrideParamPath" value="portletPreferencesOverrides['${ name }'].value"/>
                  <tr>
                    <td class="preference-name">${ name }</td>
                    <td>
                        <c:forEach items="${ channel.portletPreferences[name].value }" var="val">
                         <div>${ val }</div>
                        </c:forEach>
                    </td>
                    <td>${ channel.portletPreferencesOverrides[name].value }</td>
                  </tr>
              </c:forEach>
            </tbody>
          </table>
          
        </div>
      </div>
      <!-- END: Portlet Preferences Section -->
    </c:if>
  	
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="reviewPortlet.categoriesHeading"/></h3>
      <div class="portlet-section-options">
        <a href="${ chooseCategoryUrl }"><span><spring:message code="reviewPortlet.editCategoriesButton"/></span></a>
      </div>
      <div class="portlet-section-body">

        <ul class="category-member">
          <c:forEach items="${ channel.categories }" var="category">
            <li><a href="${ chooseCategoryUrl }"><c:out value="${ category.name }"/></a></li>
          </c:forEach>
        </ul>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="reviewPortlet.groupsHeading"/></h3>
      <div class="portlet-section-options">
        <a href="${ chooseGroupUrl }"><span><spring:message code="reviewPortlet.editGroupsButton"/></span></a>
      </div>
      <div class="portlet-section-body">
      
        <ul class="group-member">
        <c:forEach items="${ channel.groups }" var="group">
          <li><a href="${ chooseGroupUrl }"><c:out value="${ group.name }"/></a></li>
        </c:forEach>
        </ul>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="lifecycle.heading"/></h3>
      <div class="portlet-section-options">
        <a href="${ lifecycleUrl }"><span><spring:message code="reviewPortlet.editLifecycleButton"/></span></a>
      </div>
      <div class="portlet-section-body">
      
        <table summary="<spring:message code="reviewPortlet.lifecycleSummary"/>">
          <thead>
            <tr>
              <th><spring:message code="reviewPortlet.optionHeading"/></th>
              <th><spring:message code="reviewPortlet.valueHeading"/></th>
            <tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <tr>
              <td class="fl-text-align-right"><spring:message code="lifecycle.stateHeading"/></td>
              
              <%-- The following is temporary and will be replaced when backend
                   work for portlet lifecycle management is done. --%>
              <td><a href="${ lifecycleUrl }" title="<spring:message code="lifecycle.name.${ channel.lifecycleState }"/>" class="pa-edit"><spring:message code="lifecycle.name.${ channel.lifecycleState }"/></a></td>
            </tr>
            <c:if test="${ (channel.lifecycleState != 'PUBLISHED' && channel.lifecycleState != 'EXPIRED') && channel.publishDate != null }">
	            <tr>
	              <td class="fl-text-align-right"><spring:message code="lifecycle.publishDate"/></td>
	              <fmt:formatDate type="both" value="${channel.publishDate}" var="publishDate"/>
	              <td><a href="${ lifecycleUrl }" title="${ publishDate }" class="pa-edit">${ publishDate }</a></td>
	            </tr>
            </c:if>
            <c:if test="${ channel.lifecycleState != 'EXPIRED' && channel.expirationDate != null }">
	            <tr>
	              <td class="fl-text-align-right"><spring:message code="lifecycle.expirationDate"/></td>
	              <fmt:formatDate type="both" value="${channel.expirationDate}" var="expirationDate"/>
	              <td><a href="${ lifecycleUrl }" title="${ expirationDate }" class="pa-edit">${ expirationDate }</a></td>
	            </tr>
            </c:if>
          </tbody>
        </table>
        
      </div>
    </div> <!-- end: portlet-section -->
    
		<!-- Portlet Buttons -->
    <div class="portlet-button-group">
    	<a class="portlet-button portlet-button-primary" href="${ saveUrl }"><spring:message code="edit-portlet.saveButton"/></a>
    	<c:if test="${supportsConfig and channel.id < 0}">
    	   <a class="portlet-button portlet-button-primary" href="${ saveAndConfigUrl }"><spring:message code="edit-portlet.saveAndConfigButton"/></a>
    	</c:if>
    	<a class="portlet-button" href="${ cancelUrl }"><spring:message code="edit-portlet.cancelButton"/></a>
    </div>
    
  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->
