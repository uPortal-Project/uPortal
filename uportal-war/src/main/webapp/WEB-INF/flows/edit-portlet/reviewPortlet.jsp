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
<portlet:actionURL var="setControlsUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="controls"/>
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
  	<h2 role="heading">${ channel.title }</h2>
  </div> <!-- end: portlet-title -->
  
	<!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
  	<!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Configuration</h3>
      <div class="portlet-section-options">
        <a href="${ basicInfoUrl }"><span>Edit Configuration</span></a>
      </div>
      <div class="portlet-section-body">
      
        <table summary="This table is a list of the portlet's configurations.">
          <thead>
            <tr>
              <th>Configuration</th>
              <th>Value</th>
            <tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <tr>
              <td class="fl-text-align-right">Channel Title:</td>
              <td><a href="${ basicInfoUrl }" title="${ channel.title }" class="pa-edit">${ channel.title }</a></td>
            </tr>
            <tr>
              <td class="fl-text-align-right">Channel Name:</td>
              <td><a href="${ basicInfoUrl }" title="${ channel.name }" class="pa-edit">${ channel.name }</a></td>
            </tr>
            <tr>
              <td class="fl-text-align-right">Channel Functional Name:</td>
              <td><a href="${ basicInfoUrl }" title="${ channel.FName }" class="pa-edit">${ channel.FName }</a></td>
            </tr>
            <tr>
              <td class="fl-text-align-right">Channel Description:</td>
              <td><a href="${ basicInfoUrl }" title="${ channel.description }" class="pa-edit">${ channel.description }</a></td>
            </tr> 
            <tr>
              <td class="fl-text-align-right">Channel Timeout:</td>
              <td><a href="${ basicInfoUrl }" title="${ channel.timeout }" class="pa-edit">${ channel.timeout }</a></td>
            </tr>
            <tr>
              <td class="fl-text-align-right">Channel Secure:</td>
              <td><a href="${ basicInfoUrl }" title="${ channel.secure }" class="pa-edit">${ channel.secure }</a></td>
            </tr> 
            <tr>
              <td class="fl-text-align-right">Channel Type:</td>
              <td>
                <c:forEach items="${ channelTypes }" var="type">
                  <c:if test="${ type.id == channel.typeId }">
                    <a href="${ chooseTypeUrl }" title="${ channel.typeId }" class="pa-edit">${ type.name }</a>
                    <c:if test="${ type.id == -1 }">
                      <a href="${ chooseTypeUrl }" title="${ channel.typeId }" class="pa-edit">(${ channel.javaClass })</a>
                    </c:if>
                  </c:if>
                </c:forEach>
              </td>
            </tr>
            <tr>
              <td class="fl-text-align-right">Edit:</td>
              <td><a href="${ setControlsUrl }" title="${ channel.editable }" class="pa-edit">${ channel.editable }</a></td>
            </tr> 
            <tr>
              <td class="fl-text-align-right">Help:</td>
              <td><a href="${ setControlsUrl }" title="${ channel.hasHelp }" class="pa-edit">${ channel.hasHelp }</a></td>
            </tr>  
            <tr>
              <td class="fl-text-align-right">About:</td>
              <td><a href="${ setControlsUrl }" title="${ channel.hasAbout }" class="pa-edit">${ channel.hasAbout }</a></td>
            </tr>
          </tbody>
        </table>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Parameters</h3>
      <div class="portlet-section-options">
        <a href="${ setParametersUrl }"><span>Edit Parameters</span></a>
      </div>
      <div class="portlet-section-body">
      
        <table summary="This table is a list of the portlet's configurations.">
          <thead>
            <tr>
              <th>Configuration</th>
              <th>Value</th>
              <th>User editable</th>
            <tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <c:forEach items="${ cpd.params.steps }" var="step">
              <c:forEach items="${ step.parameters }" var="parameter">
                <c:if test="${ parameter.modify != 'subscribeOnly' && parameter.type.display != 'hidden' && channel.parameters[parameter.name].value != null && channel.parameters[parameter.name].value != '' }">
                  <tr>
                    <td class="fl-text-align-right">${ parameter.label }:</td>
                    <td><a href="${ setParametersUrl }" class="pa-edit">${ channel.parameters[parameter.name].value }</a></td>
                    <td>${ channel.parameterOverrides[parameter.name].value ? 'X' : '' }</td>
                  </tr>
                </c:if>
              </c:forEach>
              <c:forEach items="${ step.arbitraryParameters }" var="arbitraryParam">
                <c:forEach items="${ arbitraryParam.paramNamePrefixes }" var="prefix">
                  <c:forEach items="${ channel.parameters }" var="channelParam">
                    <c:if test="${ fn:startsWith(channelParam.key, prefix) }">
                      <tr>
                        <td class="fl-text-align-right">${ channelParam.key }:</td>
                        <td><a href="${ setParametersUrl }" class="pa-edit">${ channelParam.value }</a></td>
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
    </div> <!-- end: portlet-section -->
  	
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Categories</h3>
      <div class="portlet-section-options">
        <a href="${ chooseCategoryUrl }"><span>Edit Categories</span></a>
      </div>
      <div class="portlet-section-body">

        <ul>
          <c:forEach items="${ channel.categories }" var="category">
            <li><a href="${ chooseCategoryUrl }">${ categoryNames[category] }</a></li>
          </c:forEach>
        </ul>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Groups</h3>
      <div class="portlet-section-options">
        <a href="${ chooseGroupUrl }"><span>Edit Groups</span></a>
      </div>
      <div class="portlet-section-body">
      
        <ul>
        <c:forEach items="${ channel.groups }" var="group">
          <li><a href="${ chooseGroupUrl }">${ groupNames[group] }</a></li>
        </c:forEach>
        </ul>
        
      </div>
    </div> <!-- end: portlet-section -->
    
		<!-- Portlet Buttons -->
    <div class="portlet-button-group">
    	<a class="portlet-button portlet-button-primary" href="${ saveUrl }">Save</a>
    	<a class="portlet-button" href="${ cancelUrl }">Cancel</a>
    </div>
    
  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->