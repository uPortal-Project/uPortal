<%@ taglib prefix="editPortlet" tagdir="/WEB-INF/tags/edit-portlet" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="queryUrl">
	<portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<c:set var="defaultLength" value="10"/>
<c:set var="defaultMaxLength" value="20"/>
<c:set var="defaultTextCols" value="40"/>
<c:set var="defaultTextRows" value="10"/>

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
  	<h2 role="heading">
      <c:choose>
        <c:when test="${ completed }">
          <spring:message code="edit-portlet.editPortletHeading"/>
        </c:when>
        <c:otherwise>
          <spring:message code="edit-portlet.newPortletHeading"/>
        </c:otherwise>
      </c:choose>
    </h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">

    <form:form modelAttribute="channel" action="${queryUrl}" method="POST">

    <!-- Portlet Messages -->
    <spring:hasBindErrors name="channel">
        <div class="portlet-msg-error" role="alert">
            <form:errors path="*" element="div"/>
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>

    <!-- Step Loop -->
    <c:forEach items="${ cpd.params.steps }" var="step">
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">${ step.name }</h3>
      <div class="portlet-section-body">
          <p class="portlet-section-note" role="note">${ step.description }</p>
          
          <!-- Portlet Paramaters -->
          <c:if test="${ fn:length(step.parameters) > 0 }">
            <table summary="<spring:message code="setParameters.portletParametersTableSummary"/>">
              <thead>
                <tr>
                  <th><spring:message code="setParameters.parametersHeading"/></th>
                  <th><spring:message code="setParameters.valueHeading"/></th>
                  <th><spring:message code="setParameters.userEditableHeading"/></th>
                </tr>
              </thead>
              <tbody>
                <c:forEach items="${ step.parameters }" var="parameter">
                  <c:if test="${ parameter.modify != 'subscribeOnly' }">
                    <c:choose>
                      <c:when test="${ fn:startsWith(parameter.name, 'PORTLET.') }">
                         <c:set var="paramName" value="${ fn:replace(parameter.name, 'PORTLET.', '') }"/>
                         <c:set var="paramPath" value="portletPreferences['${ paramName }'].value"/>
                         <c:set var="overrideParamPath" value="portletPreferencesOverrides['${ paramName }'].value"/>
                      </c:when>
                      <c:otherwise>
	                    <c:set var="paramPath" value="parameters['${ parameter.name }'].value"/>
	                    <c:set var="overrideParamPath" value="parameterOverrides['${ parameter.name }'].value"/>
                      </c:otherwise>
                    </c:choose>
                    <c:choose>
                      <c:when test="${ parameter.type.display == 'hidden' }">
                        <form:hidden path="${paramPath}"/>
                      </c:when>
                      <c:otherwise>
                        <tr>
                          <td><span class="uportal-label">${ parameter.label }:</span></td>
                          <td>
                            <c:choose>
                              <c:when test="${ fn:startsWith(parameter.name, 'PORTLET.') }">
                                <editPortlet:parameterInput parameterType="${ parameter.type }" 
                                  parameterPath="${ paramPath }" parameterName="${ paramName }" 
                                  parameterValues="${ channel.portletPreferences[paramName].value }"/>
                              </c:when>
                              <c:otherwise>
	                            <editPortlet:parameterInput parameterType="${ parameter.type }" 
	                              parameterPath="${ paramPath }"/>
                              </c:otherwise>
                            </c:choose>
                          </td>
                          <td>
                            <c:if test="${ parameter.modify != 'publish-only' }">
                              <form:checkbox path="${overrideParamPath}" value="true"/>
                            </c:if>
                          </td>
                        </tr>
                      </c:otherwise>
                    </c:choose>
                  </c:if>
                </c:forEach>
              </tbody>
            </table>        
          </c:if> <!-- End Portlet Preferences -->

          <c:if test="${ channel.portlet }">
             <c:if test="${ fn:length(step.preferences) > 0 }">
                <h4>Portlet.xml Preferences</h4>
                <div>
                    <table summary="This table lists a portlet's preferences.">
                      <thead>
                        <tr>
                          <th>Parameters</th>
                          <th>Values</th>
                          <th>User editable</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:forEach items="${ portlet.portletPreferences.portletPreferences }" var="pref">
                          <tr>
                            <td>${ pref.name }</td>
                            <td>${ pref.values }</td>
                            <td>${ !pref.readOnly }</td>
                          </tr>
                        </c:forEach>
                      </tbody>
                    </table>
                </div>
                
               <div class="preference-options-section">
	            <table summary="<spring:message code="setParameters.portletParametersTableSummary"/>">
	              <thead>
	                <tr>
	                  <th><spring:message code="setParameters.parametersHeading"/></th>
	                  <th><spring:message code="setParameters.valueHeading"/></th>
	                  <th><spring:message code="setParameters.userEditableHeading"/></th>
	                </tr>
	              </thead>
	              <tbody>
	                <c:forEach items="${ step.preferences }" var="parameter">
	                  <c:if test="${ parameter.modify != 'subscribeOnly' }">
	                    <c:set var="paramPath" value="portletPreferences['${ parameter.name }'].value"/>
	                    <c:set var="overrideParamPath" value="portletPreferenceOverrides['${ parameter.name }'].value"/>
	                    <c:choose>
	                      <c:when test="${ parameter.type.display == 'hidden' }">
	                        <form:hidden path="${paramPath}"/>
	                      </c:when>
	                      <c:otherwise>
	                        <tr>
	                          <td><span class="uportal-label">${ parameter.label }:</span></td>
	                          <td>
                                  <editPortlet:parameterInput parameterType="${ parameter.type }" 
                                    parameterPath="${ paramPath }" parameterName="${ parameter.name }" 
                                    parameterValues="${ channel.portletPreferences[parameter.name].value }"/>
	                          </td>
	                          <td>
		                        <c:if test="${ parameter.modify != 'publish-only' }">
		                          <form:checkbox path="${overrideParamPath}" value="true"/>
		                        </c:if>
		                      </td>
	                        </tr>
	                      </c:otherwise>
	                    </c:choose>
	                  </c:if>
	                </c:forEach>
	              </tbody>
	            </table>
	            </div>   
	          </c:if>
          </c:if> <!-- End Portlet Preferences -->
                    
                    
          <!-- Other Parameters Loop -->
          <c:forEach items="${ step.arbitraryParameters }" var="arbitraryParam">
            <c:forEach items="${ arbitraryParam.paramNamePrefixes }" var="prefix">
            
             <div class="parameter-options-section" prefix="${ prefix }">
              <table summary="This table lists a portlet's parameter settings.">
                <thead>
                  <tr>
                    <th>Parameters</th>
                    <th>Value</th>
                    <th>User editable</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach items="${ channel.parameters }" var="channelParam">
                    <c:if test="${ fn:startsWith(channelParam.key, prefix) }">
                    <c:set var="paramPath" value="parameters['${ channelParam.key }'].value"/>
                    <c:set var="overrideParamPath" value="parameterOverrides['${ channelParam.key }'].value"/>
                      <tr>
                        <td>${ fn:substringAfter(channelParam.key, prefix) }</td>
                        <td><form:input path="${ paramPath }"/></td>
                      <td>
                      <form:checkbox path="${overrideParamPath}" value="true"/>
                      </td>
                        <td><a class="delete-parameter-link" href="javascript:;"><spring:message code="setParameters.deleteButton"/></a></td>
                      </tr>
                    </c:if>
                  </c:forEach>
                </tbody>
              </table> 
              <p><a class="add-parameter-link" href="javascript:;"><spring:message code="setParameters.addButton"/></a></p>
              <div style="display:none">
                <div class="parameter-adding-dialog jqueryui" title="<spring:message code="setParameters.addButton"/>">
                </div>
              </div>
              </div>
            </c:forEach>
          </c:forEach> <!-- End Other Parameters Loop -->
      
          <!-- Other Preferences Loop -->
          <c:forEach items="${ step.arbitraryPreferences }" var="arbitraryParam">
            <div class="preference-options-section">
              <table summary="This table lists a portlet's parameter settings.">
                <thead>
                  <tr>
                    <th>Parameters</th>
                    <th>Value</th>
                    <th>User editable</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach items="${ arb }" var="name">
                    <c:set var="paramPath" value="portletPreferences['${ name }'].value"/>
                    <c:set var="overrideParamPath" value="portletPreferencesOverrides['${ name }'].value"/>
                      <tr>
                        <td>${ name }</td>
                        <td>
                            <c:forEach items="${ channel.portletPreferences[name].value }" var="val">
                             <div>
                                 <input name="portletPreferences['${name}'].value" value="${ val }" />
                                 <a class="delete-parameter-value-link" href="javascript:;">Remove</a>
                                </div>
                            </c:forEach>
                            <a class="add-parameter-value-link" href="javascript:;" paramName="${name}">Add value</a>
                        </td>
                        <td>
                            <form:checkbox path="${overrideParamPath}" value="true"/>
                        </td>
                        <td><a class="delete-parameter-link" href="javascript:;"><spring:message code="setParameters.deleteButton"/></a></td>
                      </tr>
                  </c:forEach>
                </tbody>
              </table> 
              <p><a class="add-parameter-link" href="javascript:;"><spring:message code="setParameters.addButton"/></a></p>
              <div style="display:none">
                <div class="parameter-adding-dialog jqueryui" title="<spring:message code="setParameters.addButton"/>">
                </div>
              </div>
              </div>
          </c:forEach> <!-- End Other Preferences Loop -->
      
        </div> <!-- End Pane -->
        
    	</div> <!-- end: portlet-section -->
    
    </c:forEach> <!-- End Step Loop -->

    <!-- Portlet Buttons -->    
    <div class="portlet-button-group">
      <c:choose>
        <c:when test="${ completed }">
          <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="edit-portlet.reviewButton"/>" name="_eventId_review"/>
        </c:when>
        <c:otherwise>
          <input class="portlet-button" type="submit" value="<spring:message code="edit-portlet.backButton"/>" class="secondary" name="_eventId_back"/>
          <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="edit-portlet.nextButton"/>" name="_eventId_next"/>
        </c:otherwise>
      </c:choose>
      <input class="portlet-button" type="submit" value="<spring:message code="edit-portlet.cancelButton"/>" name="_eventId_cancel"/>
    </div>
    
    </form:form> <!-- End Form -->
    
  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->

<script src="media/org/jasig/portal/flows/edit-portlet/edit-parameters.min.js" language="JavaScript" type="text/javascript"></script>
    
<script type="text/javascript">
	up.jQuery(function() {
			var $ = up.jQuery;
			  $(document).ready(function(){
				  $("div.parameter-options-section").each(function(){
					  var dialog = $(this).find(".parameter-adding-dialog");
					  uportal.portletParametersEditor(this, {
					        preferenceNamePrefix: $(this).attr("prefix"),
					        preferenceBindName: 'parameters',
					        preferenceOverrideBindName: 'parameterOverrides',
					        dialog: dialog,
					        selectors: {
					        }
					      }
					  );
			      });
                  $("div.preference-options-section").each(function(){
                      var dialog = $(this).find(".parameter-adding-dialog");
                      uportal.portletParametersEditor(this, {
                            preferenceBindName: 'portletPreferences',
                            preferenceOverrideBindName: 'portletPreferencesOverrides',
                            dialog: dialog,
                            multivalued: true,
                            selectors: {
                            }
                          }
                      );
                  });
			  });
	});
</script>