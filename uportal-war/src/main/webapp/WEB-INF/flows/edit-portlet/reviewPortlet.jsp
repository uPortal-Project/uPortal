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

<%-- Portlet Namespace  --%>
<c:set var="n"><portlet:namespace/></c:set>

<%-- Paramaters --%>
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

<%--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| http://www.ja-sig.org/wiki/x/cQ
--%>

<%-- Styles specific to this portlet --%>
<style type="text/css">

  #${n}.view-review .tab-pane .options {
    margin: 1em 0;
  }

  #${n}.view-review .tab-pane {
    padding: 2em;
  }

  #${n}.view-review .tab-pane .read-only {
    color: #000;
  }

  #${n}.view-review .form-group label {
    color: #000;
  }

  #${n}.view-review .buttons {
    border-top: 1px dotted #ccc;
    border-bottom: 1px dotted #ccc;
    margin: 1em 0;
    padding: 1em 0;
  }

  /* Styles specific to universality
     replicates bootstrap styles used in Respondr */

  /* Hide nav-tabs - they are non-functional without bootstrap */
  .universality #${n}.view-review .nav-tabs {
    display: none;
  }

  /* Column styles */
  .universality #${n}.view-review .col-sm-2,
  .universality #${n}.view-review .col-sm-5,
  .universality #${n}.view-review .col-sm-10 {
    float: left;
    position: relative;
    min-height: 1px;
    padding-right: 15px;
    padding-left: 15px;
  }

  .universality #${n}.view-review .col-sm-10 {
    width: 80%;
  }

  .universality #${n}.view-review .col-sm-5 {
    width: 38%;
  }

  .universality #${n}.view-review .col-sm-2 {
    width: 16%;
  }

  .universality #${n}.view-review .col-sm-offset-2 {
    margin-left: 16%;
  }

  .universality #${n}.view-review .form-horizontal .form-group:before,
  .universality #${n}.view-review .form-horizontal .form-group:after {
    content: " ";
    display: table;
  }

  .universality #${n}.view-review .form-horizontal .form-group:after {
    clear: both;
  }

  /* Form styles */
  .universality #${n}.view-review .form-horizontal label {
    display: inline-block;
    margin-bottom: 5px;
    font-weight: bold;
  }
  .universality #${n}.view-review .form-horizontal .control-label {
    padding-top: 7px;
    margin-top: 0;
    margin-bottom: 0;
    text-align: right;
  }

  .universality #${n}.view-review .form-horizontal .form-group {
    margin-bottom: 15px;
    margin-right: -15px;
    margin-left: -15px;
  }

  .universality #${n}.view-review .form-horizontal .form-control-static {
    padding-top: 7px;
  }

  .universality #${n}.view-review .tab-pane {
    border-bottom: 1px solid #ccc;
  }

  .universality #${n}.view-review .edit-action {
    border: 1px solid #ccc;
    border-radius: 4px;
    color: #000;
    cursor: pointer;
    display: block;
    float: left;
    font-size: 14px;
    font-weight: normal;
    line-height: 1.42857143;
    margin-bottom: 10px;
    padding: 6px 12px;
    text-decoration: none;
  }

  .universality #${n}.view-review .edit-action:hover,
  .universality #${n}.view-review .edit-action:focus {
    color: #333;
    background-color: #ebebeb;
    border-color: #adadad;
  }
</style>


<!-- Portlet -->
<div class="fl-widget portlet ptl-mgr view-review" id="${n}" role="section">

	<!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
  	<h2 class="title" role="heading">${ fn:escapeXml(portlet.title )}</h2>
  </div> <!-- end: portlet-titlebar -->
  
	<!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">
  
    <!-- Nav tabs -->
    <ul class="nav nav-tabs">
      <li class="active"><a href="#configuration" data-toggle="tab">Configuration</a></li>
      <%-- Display parameters/preferences sections *only if* the CPD declares steps for editing them. --%>
      <c:if test="${fn:length(cpd.steps) != 0}">
        <li><a href="#parameters" data-toggle="tab">Parameters</a></li>
      </c:if>
      <li><a href="#xmlPreferences" data-toggle="tab">Portlet XML Preferences</a></li>
      <li><a href="#preferences" data-toggle="tab">Preferences</a></li>
      <li><a href="#categories" data-toggle="tab">Categories</a></li>
      <li><a href="#groups" data-toggle="tab">Groups</a></li>
      <li><a href="#lifecycle" data-toggle="tab">Lifecycle</a></li>
    </ul>

    <form class="form-horizontal" role="form">

      <!-- Tab panes -->
      <div class="tab-content">

        <%-- Configuration --%>
        <div class="tab-pane active" id="configuration">
          <div class="form-group">
            <div class="col-sm-2 col-sm-offset-2">
              <a class="btn btn-default edit-action" href="${ basicInfoUrl }"><span><spring:message code="edit.configuration"/></span></a>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="portlet.title"/></label>
            <div class="col-sm-10">
              <p class="form-control-static"><a href="${ basicInfoUrl }" title="${ fn:escapeXml(portlet.title )}" class="pa-edit">${ fn:escapeXml(portlet.title )}</a></p>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="portlet.name"/></label>
            <div class="col-sm-10">
              <p class="form-control-static"><a href="${ basicInfoUrl }" title="${ fn:escapeXml(portlet.name )}" class="pa-edit">${ fn:escapeXml(portlet.name )}</a></p>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="portlet.functional.name"/></label>
            <div class="col-sm-10">
              <p class="form-control-static"><a href="${ basicInfoUrl }" title="${ fn:escapeXml(portlet.fname )}" class="pa-edit">${ fn:escapeXml(portlet.fname )}</a</p>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="portlet.description"/></label>
            <div class="col-sm-10">
              <p class="form-control-static"><a href="${ basicInfoUrl }" title="${ fn:escapeXml(portlet.description )}" class="pa-edit">${ fn:escapeXml(portlet.description )}</a></p>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="portlet.timeout"/></label>
            <div class="col-sm-10">
              <p class="form-control-static"><a href="${ basicInfoUrl }" title="${ fn:escapeXml(portlet.timeout )}" class="pa-edit">${ fn:escapeXml(portlet.timeout )}</a></p>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="portlet.type"/></label>
            <div class="col-sm-10">
              <p class="form-control-static">
                <c:forEach items="${ portletTypes }" var="type">
                  <c:if test="${ type.key.id == portlet.typeId }">
                    <a href="${ chooseTypeUrl }" title="${ fn:escapeXml(portlet.typeId )}" class="pa-edit">${ fn:escapeXml(type.key.name )}</a>
                  </c:if>
                </c:forEach>
              </p>
            </div>
          </div>
        </div>

        <%-- Parameters --%>
        <%-- Display parameters/preferences sections *only if* the CPD declares steps for editing them. --%>
        <c:if test="${fn:length(cpd.steps) != 0}">
          <div class="tab-pane" id="parameters">
            <div class="form-group">
              <div class="col-sm-2 col-sm-offset-2">
                <a class="btn btn-default edit-action" href="${ setParametersUrl }"><span><spring:message code="edit.parameters"/></span></a>
              </div>
            </div>
            <c:forEach items="${ cpd.steps }" var="step">
              <c:forEach items="${ step.parameters }" var="parameter">
                <c:if test="${ parameter.parameterInput.value.display != 'HIDDEN' && (portlet.parameters[parameter.name].value != null && portlet.parameters[parameter.name].value != '') }">
                  <div class="form-group">
                    <label class="col-sm-2 control-label"><spring:message code="${ parameter.label }" text="${ parameter.label }"/></label>
                    <div class="col-sm-10">
                      <p class="form-control-static">
                        <a href="${ setParametersUrl }" class="pa-edit">
                          ${ fn:escapeXml(portlet.parameters[parameter.name].value )}
                        </a>
                      </p>
                    </div>
                  </div>
                </c:if>
              </c:forEach>
            </c:forEach>
          </div>
        </c:if>

        <%-- XML Preferences --%>
        <div class="tab-pane" id="xmlPreferences">
          <c:forEach items="${ portletDescriptor.portletPreferences.portletPreferences }" var="pref">
            <div class="form-group ${ up:containsKey(portlet.portletPreferences, pref.name) ? 'override-preference' : '' }">
              <label class="col-sm-2 control-label preference-name">${ fn:escapeXml(pref.name )}</label>
              <c:forEach var="value" items="${ pref.values }">
                <div class="col-sm-5">
                  <p class="form-control-static">${ fn:escapeXml(value )}</p>
                </div>
              </c:forEach>
              <div class="col-sm-5">
                <p class="form-control-static">
                  <c:if test="${ pref.readOnly == 'true'}">
                    <span class="read-only">Read only</span>
                  </c:if>
                </p>
              </div>
            </div>
          </c:forEach>
        </div>

        <%-- Preferences --%>
        <div class="tab-pane" id="preferences">
          <div class="form-group">
            <div class="col-sm-2 col-sm-offset-2">
              <c:choose>
                <c:when test="${supportsConfig and portlet.id >= 0}">
                  <a class="btn btn-default edit-action" href="${ configModeUrl }"><span><spring:message code="edit.rich.configuration"/></span></a>
                </c:when>
                <c:otherwise>
                  <a class="btn btn-default edit-action" href="${ setParametersUrl }"><span><spring:message code="edit.preferences"/></span></a>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
          <c:forEach items="${ portlet.portletPreferences }" var="pref">
            <div class="form-group">
              <label class="col-sm-2 control-label preference-name">${ fn:escapeXml(pref.key )}</label>
              <div class="col-sm-5">
                <p class="form-control-static">
                  <c:forEach items="${ pref.value.value }" var="val">
                    <span class="pref-value">${ fn:escapeXml(val )}</span>
                  </c:forEach>
                </p>
              </div>
              <div class="col-sm-5">
                <p class="form-control-static">
                  <c:if test="${ portlet.portletPreferenceReadOnly[pref.key].value == 'true'}">
                    <span class="read-only">Read only</span>
                  </c:if>
                </p>
              </div>
            </div>
          </c:forEach>
        </div>

        <%-- Categories --%>
        <div class="tab-pane" id="categories">
          <div class="form-group">
            <div class="col-sm-offset-2">
              <a class="btn btn-default edit-action" href="${ chooseCategoryUrl }"><span><spring:message code="edit.categories"/></span></a>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="categories"/></label>
            <div class="col-sm-10">
              <p class="form-control-static">
                <c:forEach items="${ portlet.categories }" var="category">
                  <a class="label label-info" href="${ chooseCategoryUrl }">${ fn:escapeXml(category.name )}</a>
                </c:forEach>
              </p>
            </div>
          </div>
        </div>

        <%-- Groups --%>
        <div class="tab-pane" id="groups">
          <div class="form-group">
            <div class="col-sm-offset-2">
              <a class="btn btn-default edit-action" href="${ chooseGroupUrl }"><span><spring:message code="edit.groups"/></span></a>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="groups"/></label>
            <div class="col-sm-10">
              <p class="form-control-static">
                <c:forEach items="${ portlet.groups }" var="group">
                  <a class="label label-info" href="${ chooseGroupUrl }">${ fn:escapeXml(group.name )}</a>
                </c:forEach>
              </p>
            </div>
          </div>
        </div>

        <%-- Lifecycle --%>
        <div class="tab-pane" id="lifecycle">
          <div class="form-group">
            <div class="col-sm-offset-2">
              <a class="btn btn-default edit-action" href="${ lifecycleUrl }"><span><spring:message code="edit.lifecycle"/></span></a>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label"><spring:message code="state"/></label>
            <div class="col-sm-10">
              <%-- The following is temporary and will be replaced when backend
                     work for portlet lifecycle management is done. --%>
              <p class="form-control-static"><a href="${ lifecycleUrl }" title="<spring:message code="lifecycle.name.${ portlet.lifecycleState }"/>" class="pa-edit"><spring:message code="lifecycle.name.${ portlet.lifecycleState }"/></a></p>
            </div>
          </div>
          <c:if test="${ (portlet.lifecycleState != 'PUBLISHED' && portlet.lifecycleState != 'EXPIRED') && portlet.publishDate != null }">
            <div class="form-group">
              <label class="col-sm-2 control-label"><spring:message code="auto.publish.date.time"/></label>
              <div class="col-sm-10">
                <fmt:formatDate type="both" value="${portlet.publishDate}" var="publishDate"/>
                <p class="form-control-static"><a href="${ lifecycleUrl }" title="${ fn:escapeXml(publishDate )}" class="pa-edit">${ fn:escapeXml(publishDate )}</a></p>
              </div>
            </div>
          </c:if>
          <c:if test="${ portlet.lifecycleState != 'EXPIRED' && portlet.expirationDate != null }">
            <div class="form-group">
              <label class="col-sm-2 control-label"><spring:message code="auto.expire.date.time"/></label>
              <div class="col-sm-10">
                <fmt:formatDate type="both" value="${portlet.expirationDate}" var="expirationDate"/>
                <p class="form-control-static"><a href="${ lifecycleUrl }" title="${ fn:escapeXml(expirationDate )}" class="pa-edit">${ fn:escapeXml(expirationDate )}</a></p>
              </div>
            </div>
          </c:if>
        </div>

      </div> <!-- end: nav-tabs -->
    
    <!-- Buttons -->
    <c:set var="promptConfigMode" value="${supportsConfig and portlet.id == null}" />
    <div class="buttons form-group">
      <div class="col-sm-10 col-sm-offset-2">
        <a class="button btn<c:if test="${!promptConfigMode}"> btn-primary</c:if>" href="${saveUrl}"><spring:message code="save"/></a>
        <c:if test="${promptConfigMode}">
           <a class="button btn btn-primary" href="${saveAndConfigUrl}"><spring:message code="save.and.configure"/></a>
        </c:if>
        <a class="button btn btn-link" href="${cancelUrl}"><spring:message code="cancel"/></a>
      </div>
    </div>

  </form>
    
  </div> <!-- end: portlet-content -->
  
</div> <!-- end: portlet -->
