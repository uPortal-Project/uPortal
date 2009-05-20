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
<portlet:actionURL var="cancelUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="cancel"/>
</portlet:actionURL>
<portlet:actionURL var="saveUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="save"/>
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
  
  	<!-- Portlet Section -->
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
                  <c:if test="${ type.id == channel.typeId }">
                    <a href="${ chooseTypeUrl }" title="${ channel.typeId }" class="pa-edit"><c:out value="${ type.name }"/></a>
                    <c:if test="${ type.id == -1 }">
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
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
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
            <tr>
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
              <c:forEach items="${ step.preferences }" var="parameter">
                <c:if test="${ parameter.modify != 'subscribeOnly' && parameter.type.display != 'hidden' && channel.portletPreferences[parameter.name].value != null && fn:length(channel.portletPreferences[parameter.name].value) > 0 }">
                  <tr>
                    <td class="fl-text-align-right"><c:out value="${ parameter.label }"/>:</td>
                    <td>
                        <a href="${ setParametersUrl }" class="pa-edit">
                            <c:forEach items="${ channel.portletPreferences[parameter.name].value }" var="val" varStatus="status">
                                <c:out value="${ val }"/>${ !status.last ? '<br/>' : '' }
                            </c:forEach>
                        </a>
                    </td>
                    <td>${ channel.portletPreferencesOverrides[parameter.name].value ? 'X' : '' }</td>
                  </tr>
                </c:if>
              </c:forEach>
            </c:forEach>
          </tbody>
        </table>
        
      </div>
    </div> <!-- end: portlet-section -->
  	
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="reviewPortlet.categoriesHeading"/></h3>
      <div class="portlet-section-options">
        <a href="${ chooseCategoryUrl }"><span><spring:message code="reviewPortlet.editCategoriesButton"/></span></a>
      </div>
      <div class="portlet-section-body">

        <ul class="category-member">
          <c:forEach items="${ channel.categories }" var="category">
            <li><a href="${ chooseCategoryUrl }"><c:out value="${ categoryNames[category] }"/></a></li>
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
          <li><a href="${ chooseGroupUrl }"><c:out value="${ groupNames[group] }"/></a></li>
        </c:forEach>
        </ul>
        
      </div>
    </div> <!-- end: portlet-section -->
    
		<!-- Portlet Buttons -->
    <div class="portlet-button-group">
    	<a class="portlet-button portlet-button-primary" href="${ saveUrl }"><spring:message code="edit-portlet.saveButton"/></a>
    	<a class="portlet-button" href="${ cancelUrl }"><spring:message code="edit-portlet.cancelButton"/></a>
    </div>
    
  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->
