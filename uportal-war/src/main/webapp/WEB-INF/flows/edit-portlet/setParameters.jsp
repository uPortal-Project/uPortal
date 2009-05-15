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
                    <c:set var="paramPath" value="parameters['${ parameter.name }'].value"/>
                    <c:set var="overrideParamPath" value="parameterOverrides['${ parameter.name }'].value"/>
                    <c:choose>
                      <c:when test="${ parameter.type.display == 'hidden' }">
                        <form:hidden path="${paramPath}"/>
                      </c:when>
                      <c:otherwise>
                        <tr>
                          <td><span class="uportal-label">${ parameter.label }:</span></td>
                          <td>
                            <c:choose>
                              <c:when test="${ parameter.type.input == 'text' }">
                                <c:choose>
                                  <c:when test="${ parameter.type.display == 'textarea' }">
                                    <form:textarea path="${paramPath}"/>
                                  </c:when>
                                  <c:otherwise>
                                    <form:input path="${paramPath}" size="${ parameter.type.length != '' ? parameter.type.length : defaultLength }" maxlength="${ parameter.type.maxlength != '' ? parameter.type.maxlength : defaultMaxLength }"/>
                                  </c:otherwise>
                                </c:choose>
                              </c:when>
                              <c:when test="${ parameter.type.input == 'single-choice' }">
                                <c:choose>
                                  <c:when test="${ parameter.type.display == 'radio' }">
                                    <form:radiobuttons path="${paramPath}" items="${ parameter.type.restriction.values }"/>
                                  </c:when>
                                  <c:otherwise>
                                    <form:select path="${paramPath}">
                                      <c:forEach items="${ parameter.type.restriction.values }" var="value">
                                        <form:option value="${ value.value }" label="${ value.value }" />
                                      </c:forEach>
                                    </form:select>
                                  </c:otherwise>
                                </c:choose>
                              </c:when>
                              <c:when test="${ parameter.type.input == 'multi-choice' }">
                                <c:choose>
                                  <c:when test="${ paramter.type.display == 'checkbox' }">
                                    <form:checkboxes path="${paramPath}" items="${ parameter.type.restriction.values }"/>
                                  </c:when>
                                  <c:otherwise>
                                    <form:select path="${paramPath}" multiple="true">
                                      <c:forEach items="${ parameter.type.restriction.values }" var="value">
                                        <form:option value="${ value.value }" label="${ value.value }" />
                                      </c:forEach>
                                    </form:select>
                                  </c:otherwise>
                                </c:choose>
                              </c:when>
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
          </c:if> <!-- End Portlet Paramaters -->
                    
          <!-- Other Parameters Loop -->
          <c:forEach items="${ step.arbitraryParameters }" var="arbitraryParam">
            <c:forEach items="${ arbitraryParam.paramNamePrefixes }" var="prefix">
            
              <c:if test="${ channel.portlet and prefix == 'PORTLET.' }">
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
	                        <td></td>
	                        <td>${ !pref.readOnly }</td>
	                      </tr>
	                    </c:forEach>
	                  </tbody>
	                </table>
	            </div>
                <h4>Portlet Definition Preferences</h4>
	          </c:if>
            
              <div id="${fn:replace(prefix, '.', '')}-arbitraryParams" >
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
                      <tr>
                        <td>${ fn:substringAfter(channelParam.key, prefix) }</td>
                        <td><form:input path="${ paramPath }"/></td>
                      <td>
                      <form:checkbox path="${overrideParamPath}" value="true"/>
                      </td>
                        <td><a href="javascript:;"><spring:message code="setParameters.deleteButton"/></a></td>
                      </tr>
                    </c:if>
                  </c:forEach>
                </tbody>
              </table> 
              <p><a class="add-parameter-link" href="javascript:;"><spring:message code="setParameters.addButton"/></a></p>
              <div style="display:none">
                <div id="${fn:replace(prefix, '.', '')}newparam" class="jqueryui" title="<spring:message code="setParameters.addButton"/>">
                </div>
              </div>
              </div>
            </c:forEach>
          </c:forEach> <!-- End Other Parameters Loop -->
      
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
    
<script type="text/javascript">
	up.jQuery(function() {
			var $ = up.jQuery;
			var initializedDialogs = [];
			function showNewParameterForm(prefix) {
				var id = "#" + prefix.replace('.', '') + "newparam";
				var html = "<form><spring:message code="setParameters.newParamForm.name"/> <input type=\"text\" name=\"name\"/><br/>";
				html += "<spring:message code="setParameters.newParamForm.value"/> <input type=\"text\" name=\"value\"/><br/>";
				html += "<spring:message code="setParameters.newParamForm.userEditable"/> <input type=\"checkbox\" name=\"override\" value=\"true\"/><br/>";
				html += "<input type=\"submit\" value=\"<spring:message code="setParameters.newParamForm.submitButton"/>\"/><form>";
				$(id).html(html)
				.find("form").submit(function() {
					var name = this.name.value;
					var value = this.value.value;
					var override = this.override.checked;
					var tr = document.createElement("tr");
					var html2 = "<td>" + name + "</td><td><input name=\"parameters['" + prefix + "." + name + "'].value\" value=\"" + value + "\" type=\"text\"/></td>";
					if (override) {
						html2 += "<td><input name=\"parameterOverrides['" + prefix + "." + name + "'].value\" value=\"true\" type=\"checkbox\" checked/></td>";
					} else {
						html2 += "<td><input name=\"parameterOverrides['" + prefix + "." + name + "'].value\" value=\"true\" type=\"checkbox\"/></td>";
					}
					html2 += "<td><a href=\"javascript:;\" onclick=\"$(this).parent().parent().remove()\">Delete</td>";
					$(tr).html(html2);
					$("#" + prefix.replace('.', '') + "-arbitraryParams tbody").append(tr);
					$(id).dialog("close");
					return false;
				});
				if ($.inArray(prefix, initializedDialogs) < 0) {
                    initializedDialogs.push(prefix);
                    $(id).dialog();
				} else {
                    $(id).dialog('open');
				}
			  }
			  $(document).ready(function(){
				  $("div[id*=arbitraryParams]").each(function(){
					  var prefix = this.id.split("-")[0];
					  $(this).find("table a").click(function(){ $(this).parent().parent().remove(); return false; });
					  $(this).find(".add-parameter-link").click(function(){ return showNewParameterForm(prefix); });
			      });
			  });
	});
</script>