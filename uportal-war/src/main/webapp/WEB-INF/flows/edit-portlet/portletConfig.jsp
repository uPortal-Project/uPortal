<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="editPortlet" tagdir="/WEB-INF/tags/edit-portlet" %>
<%@ page import="org.jasig.portal.portlet.om.PortletLifecycleState,java.util.Set" %>

<%-- Portlet Namespace  --%>
<c:set var="n"><portlet:namespace/></c:set>

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="queryUrl">
    <portlet:param name="execution" value="${flowExecutionKey}"/>
</portlet:actionURL>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<c:set var="defaultLength" value="10"/>
<c:set var="defaultMaxLength" value="20"/>
<c:set var="defaultTextCols" value="40"/>
<c:set var="defaultTextRows" value="10"/>

<spring:message var="amLabel" code="time.am" text="AM"/>
<spring:message var="pmLabel" code="time.pm" text="PM"/>

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

<div class="fl-widget portlet ptl-mgr" id="${n}">

<form:form modelAttribute="portlet" action="${queryUrl}" method="POST" role="form" class="form-horizontal portlet-config">
    <!-- Portlet -->
    <div class="view-basicinfo" role="section">

        <!-- Portlet Titlebar -->
        <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
            <h2 class="title" role="heading">
                <spring:message code="${ completed ? 'edit.portlet' : 'register.new.portlet' }"/>
            </h2>
        </div> <!-- end: portlet-titlebar -->

        <!-- Portlet Content -->
        <div class="fl-widget-content content portlet-content" role="main">

            <!-- Portlet Messages -->
            <spring:hasBindErrors name="portlet">
                <!--div class="portlet-msg-error portlet-msg error text-danger" role="alert">
                <form:errors path="*" element="div"/>
                </div--> <!-- end: portlet-msg -->

                <div class="alert alert-danger" role="alert">
                    <form:errors path="*" element="div"/>
                </div>
            </spring:hasBindErrors>

            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="content row">
                    <div class="col-md-6">
                        <div class="titlebar">
                            <h3 class="title" role="heading"><spring:message code="summary.information"/></h3>
                        </div>
                        <div class="form-group">
                            <span class="col-sm-4 control-label">
                                <label for="portletTitle"><spring:message code="portlet.title"/></label>
                                <span class="glyphicon glyphicon-info-sign"
                        title="<spring:message code='portlet.title.tooltip'/>" data-toggle="tooltip"
                        data-placement="top"></span>
                            </span>
                            <div class="col-sm-8">
                                <form:input path="title" type="text" class="form-control" id="portletTitle" autocomplete="off" autocorrect="off"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <span class="col-sm-4 control-label">
                                <label for="portletName"><spring:message code="portlet.name"/></label>
                                <span class="glyphicon glyphicon-info-sign"
                            title="<spring:message code='portlet.name.tooltip'/>" data-toggle="tooltip"
                            data-placement="top"></span>
                            </span>
                            <div class="col-sm-8">
                                <form:input path="name" type="text" class="form-control" id="portletName" autcomplete="off" autocorrect="off"/>
                            </div>
                        </div>
                        <div class="form-group name-title-mismatch-warn" style="display: none">
                            <div class="col-sm-12">
                                <div class="alert alert-info">
                                    <spring:message code="portlet.name.title.mismatch"/>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <span class="col-sm-4 control-label">
                                <label for="portletFname"><spring:message code="portlet.functional.name"/></label>
                                <span class="glyphicon glyphicon-info-sign"
                                title="<spring:message code='portlet.functional.name.tooltip'/>" data-toggle="tooltip"
                                data-placement="top"></span>
                            </span>
                            <div class="col-sm-8">
                                <form:input path="fname" type="text" class="form-control" id="portletFname" autcomplete="off" autocorrect="off"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <span class="col-sm-4 control-label">
                                <label for="portletDescription"><spring:message code="portlet.description"/></label>
                                <span class="glyphicon glyphicon-info-sign"
                                title="<spring:message code='portlet.description.tooltip'/>" data-toggle="tooltip"
                                data-placement="top"></span>
                            </span>
                            <div class="col-sm-8">
                                <form:input path="description" type="text" class="form-control" id="portletDescription" autcomplete="off" autocorrect="off"/>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="titlebar">
                            <h3 class="title" role="heading"><spring:message code="controls"/></h3>
                        </div>
                        <div class="form-group">
                            <label for="portletControls" class="col-sm-3 control-label"><spring:message
                        code="portlet.controls"/></label>

                            <div class="col-sm-9">
                                <div class="checkbox">
                                    <label for="hasHelp">
                                        <form:checkbox path="hasHelp"/>
                                        <spring:message code="hasHelp"/>
                                    </label>
                                </div>
                                <div class="checkbox">
                                    <label for="editable">
                                        <form:checkbox path="editable"/>
                                        <spring:message code="editable"/>
                                    </label>
                                </div>
                                <div class="checkbox">
                                    <label for="configurable">
                                        <form:checkbox path="configurable"/>
                                        <spring:message code="configurable"/>
                                    </label>
                                </div>
                                <div class="checkbox">
                                    <label for="hasAbout">
                                        <form:checkbox path="hasAbout"/>
                                        <spring:message code="hasAbout"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div> <!-- end: portlet-section -->
        </div> <!-- end: portlet-content -->
    </div> <!-- end: portlet -->

    <!-- Portlet -->
    <div class="fl-widget portlet ptl-mgr view-setparameters" role="section">
        <!-- Portlet Content -->
        <div class="fl-widget-content content portlet-content" role="main">

            <!-- Add a note to the page if the portle supports config mode  -->
            <c:if test="${supportsConfig}">
                <div class="portlet-msg-info portlet-msg info" role="alert">
                    <p class="text-info">
                        <span class="label label-info"><spring:message code="note"/></span>
                        <spring:message code="this.portlet.supports.rich.config.message"/>
                    </p>
                </div>
            </c:if>
            <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                <div class="panel panel-info">
                    <div class="panel-heading" role="tab" id="portletConfigAccordionHeading">
                        <h4 class="panel-title">
                            <i class="fa fa-chevron-up"></i> <a data-toggle="collapse" data-parent="#accordion" href="#portletConfigAccordion" aria-expanded="true" aria-controls="portletConfigAccordion">Advanced Options</a>
                        </h4>
                    </div>
                    <div id="portletConfigAccordion" class="panel-collapse collapse" role="tabpanel" aria-labelledby="portletConfigAccordionHeading">
                        <div class="panel-body">
                            <div class="portlet-section" role="region">
                                <div class="titlebar">
                                    <h3 class="title" role="heading"><spring:message code="portlet.options" text="Portlet Options"/></h3>
                                </div>
                                <div class="portlet-section" role="region">
                                    <div class="form-group">
                                        <span class="col-sm-2 control-label">
                                            <label for="portletTimeout"><spring:message code="portlet.timeout"/></label>
                                            <span class="glyphicon glyphicon-info-sign" title="<spring:message code='portlet.timeout.tooltip'/>"
                                                  data-toggle="tooltip" data-placement="top"></span>
                                        </span>
                                        <div class="col-sm-4">
                                            <form:input path="timeout" type="text" class="form-control" id="portletTimeout"/>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Portlet.xml Preferences Section -->
                            <c:if test="${not empty portletDescriptor.portletPreferences.portletPreferences}">
                                <div class="portlet-section" role="region">
                                    <div class="titlebar">
                                        <h3 class="title" role="heading"><spring:message code="portlet.xml.preferences"/></h3>
                                    </div>
                                    <div class="content">
                                        <p class="note" role="note"><spring:message
                                        code="default.preferences.provided.by.portlet.descriptor"/></p>
                                        <table class="portlet-table table table-hover">
                                            <thead>
                                                <tr>
                                                    <th width="40%"><spring:message code="preferences"/></th>
                                                    <th width="30%"><spring:message code="values"/></th>
                                                    <th width="30%"><spring:message code="read.only.prevents.user.customization"/></th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${ portletDescriptor.portletPreferences.portletPreferences }" var="pref">
                                                    <tr class="${ up:containsKey(portlet.portletPreferences, pref.name) ? 'override-preference' : '' }">
                                                        <td class="preference-name">
                                                            <div class="control-label">
                                                                ${ fn:escapeXml(pref.name )}
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <c:forEach var="value" items="${ pref.values }">
                                                                <div>${ fn:escapeXml(value )}</div>
                                                            </c:forEach>
                                                        </td>
                                                        <td>${ fn:escapeXml(pref.readOnly )}</td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div> <!-- END: Portlet.xml Preferences Section -->
                            </c:if>

                            <!-- Step Loop -->
                            <c:forEach items="${ cpd.steps }" var="step" varStatus="status">

                                <!-- Portlet Section -->
                                <div class="portlet-section" role="region">
                                    <div class="titlebar">
                                        <h3 class="title" role="heading"><spring:message code="${ step.name }" text="${ step.name }"/></h3>
                                    </div>
                                    <div class="content">
                                        <p class="note" role="note">${ fn:escapeXml(step.description )}</p>

                                        <!-- Portlet Display Settings -->
                                        <c:if test="${ fn:length(step.parameters) > 0 }">
                                            <div class="portlet-table">
                                                <table class=" table table-hover" summary="<spring:message code="this.table.lists.portlet.parameters"/>">
                                                    <thead>
                                                        <tr>
                                                            <th width="30%"><spring:message code="parameter"/></th>
                                                            <th width="70%"><spring:message code="value"/></th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach items="${ step.parameters }" var="parameter">
                                                            <c:set var="paramPath" value="parameters['${ parameter.name }'].value"/>
                                                            <c:choose>
                                                                <c:when test="${ parameter.parameterInput.value.display == 'HIDDEN' }">
                                                                    <form:hidden path="${paramPath}"/>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <tr>
                                                                        <td class="text-right">
                                                                            <div class="control-label">
                                                                                <spring:message code="${parameter.label}"/>
                                                                                <c:if test="${not empty parameter.description}">
                                                                                    <span class="glyphicon glyphicon-info-sign"
                                                                                          title="${fn:escapeXml(parameter.description)}"
                                                                                          data-toggle="tooltip"
                                                                                          data-placement="top">
                                                                                    </span>
                                                                                </c:if>
                                                                            </div>
                                                                        </td>
                                                                        <td><editPortlet:parameterInput input="${ parameter.parameterInput.value }" path="${ paramPath }" cssClass="form-control"/></td>
                                                                    </tr>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </c:if> <!-- End Portlet Display Settings -->

                                        <c:if test="${ portlet.portlet }">
                                            <c:if test="${ fn:length(step.preferences) > 0 }">
                                                <div class="preference-options-section">
                                                    <table class="portlet-table table table-hover" summary="<spring:message code="this.table.lists.portlet.parameters"/>">
                                                        <thead>
                                                            <tr>
                                                                <th width="40%"><spring:message code="parameter"/></th>
                                                                <th width="30%"><spring:message code="value"/></th>
                                                                <th width="30%"><spring:message code="read.only.prevents.user.customization"/></th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <c:forEach items="${ step.preferences }" var="preference">
                                                                <c:set var="paramPath" value="portletPreferences['${ preference.name }'].value"/>
                                                                <c:set var="overrideParamPath" value="portletPreferenceReadOnly['${ preference.name }'].value"/>
                                                                <c:choose>
                                                                    <c:when test="${ preference.preferenceInput.value.display == 'HIDDEN' }">
                                                                        <c:set var="values" value="${ portlet.portletPreferences[preference.name].value }"/>
                                                                        <input type="hidden" name="${ fn:escapeXml(paramPath )}"  value="${ fn:escapeXml(fn:length(values) > 0 ? values[0] : '' )}"/>
                                                                    </c:when>
                                                                <c:otherwise>
                                                                    <tr>
                                                                        <td class="preference-name">
                                                                            <div class="control-label">
                                                                                <spring:message code="${ preference.label }" text="${ preference.label }"/>
                                                                                <c:if test="${not empty preference.description}">
                                                                                    <span class="glyphicon glyphicon-info-sign"
                                                                                          title="${fn:escapeXml(preference.description)}"
                                                                                          data-toggle="tooltip"
                                                                                          data-placement="top">
                                                                                    </span>
                                                                                </c:if>
                                                                            </div>
                                                                        </td>
                                                                        <td>
                                                                            <editPortlet:preferenceInput input="${ preference.preferenceInput.value }" path="${ paramPath }" name="${ preference.name }" values="${ portlet.portletPreferences[preference.name].value }"/>
                                                                        </td>
                                                                        <td>
                                                                            <form:checkbox path="${overrideParamPath}" value="true"/>
                                                                        </td>
                                                                    </tr>
                                                                </c:otherwise>
                                                                </c:choose>
                                                            </c:forEach>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </c:if>

                                            <c:if test="${ not empty step.arbitraryPreferences }">
                                                <div class="preference-options-section">
                                                    <table class="portlet-table table table-hover">
                                                        <thead>
                                                            <tr>
                                                                <th width="30%"><spring:message code="preference"/></th>
                                                                <th width="30%"><spring:message code="value"/></th>
                                                                <th width="30%"><spring:message code="read.only.prevents.user.customization"/></th>
                                                                <th width="10%"></th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <c:forEach items="${ arbitraryPreferenceNames }" var="name">
                                                                <c:set var="paramPath" value="portletPreferences['${ name }'].value"/>
                                                                <c:set var="overrideParamPath" value="portletPreferenceReadOnly['${ name }'].value"/>
                                                                <tr>
                                                                    <td class="preference-name">
                                                                        <div class="control-label">
                                                                            ${ fn:escapeXml(name) }
                                                                        </div>
                                                                    </td>
                                                                    <td>
                                                                        <c:forEach items="${ portlet.portletPreferences[name].value }" var="val">
                                                                            <div>
                                                                                <input class="form-control parameter-editor-value" name="portletPreferences['${fn:escapeXml(name)}'].value" value="${ fn:escapeXml(val) }"/>
                                                                                <a class="delete-parameter-value-link btn btn-xs btn-default" href="javascript:;">
                                                                                    <spring:message code="remove" text="Remove"/>
                                                                                    <i class="fa fa-minus-circle"></i>
                                                                                </a>
                                                                            </div>
                                                                        </c:forEach>
                                                                        <input type="hidden" class="form-control" name="portletPreferences['${fn:escapeXml(name)}'].value" value=""/>
                                                                        <a class="add-parameter-value-link btn btn-xs btn-info" href="javascript:;" paramName="${fn:escapeXml(name)}">
                                                                            <spring:message code="add.value" text="Add value"/>
                                                                            <i class="fa fa-plus-circle"></i>
                                                                        </a>
                                                                    </td>
                                                                    <td><form:checkbox path="${overrideParamPath}" value="true"/></td>
                                                                    <td><a class="delete-parameter-link btn btn-warning" href="javascript:;"><spring:message code="setParameters.deleteButton"/> <i class="fa fa-trash"></i></a></td>
                                                                </tr>
                                                            </c:forEach>
                                                        </tbody>
                                                    </table>
                                                    <p><a class="add-parameter-link btn btn-primary" href="javascript:;"><spring:message code="add.preference"/>&nbsp;&nbsp;<i class="fa fa-plus-circle"></i></a></p>
                                                </div>
                                            </c:if>
                                        </c:if> <!-- end: (portlet step test) -->
                                    </div> <!-- end: content -->
                                </div> <!-- end: portlet-section -->
                            </c:forEach> <!-- end: (step loop) -->
                        </div>  <!-- end: panel-body -->
                    </div> <!-- end: panel-collapse -->
                </div> <!-- end: panel -->
            </div> <!-- end: accordion -->
        </div> <!-- end: portlet-content -->
    </div> <!-- end: portlet -->

    <!-- Portlet config groups and categories -->
    <div id="${n}PortletGroupsCategories" class="fl-widget portlet ptl-mgr view-groups-cats" role="section">
        <!-- Portlet Content -->
        <div class="fl-widget-content content portlet-content" role="main">
            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading">
                        <spring:message code="groups.and.categories" text="Groups and Categories"/>
                    </h3>
                </div>
                <div class="content row">

                    <!-- Portlet groups -->
                    <div class="col-md-6">
                        <div class="form-group">
                            <label class="col-sm-4 control-label h3"><spring:message code="groups"/></label>
                            <div class="col-sm-offset-4">
                                <button type="submit" class="button btn btn-primary" name="_eventId_chooseGroup"><spring:message code="edit.groups"/>&nbsp;&nbsp;<i class="fa fa-users"></i></button>
                            </div>
                            <div class="col-sm-offset-4">
                                <c:if test="${empty portlet.groups}">
                                    <p class="text-warning">You should specify a group or no one will be able to view the portlet</p>
                                </c:if>
                                <ul class="config-list">
                                    <c:forEach items="${ portlet.groups }" var="group">
                                        <li><i class="fa fa-users"></i> ${fn:escapeXml(group.name )}</li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </div>
                    </div> <!-- end: portlet groups -->

                    <!-- Portlet categories -->
                    <div class="col-md-6">
                        <div class="form-group">
                            <label class="col-sm-4 control-label h3"><spring:message code="categories"/></label>
                            <div class="col-sm-offset-4">
                                <button type="submit" class="button btn btn-primary" name="_eventId_chooseCategory"><spring:message code="edit.categories"/>&nbsp;&nbsp;<i class="fa fa-folder-open"></i></button>
                            </div>
                            <div class="col-sm-offset-4">
                                <%-- If there are no categories selected and there are no lifecycle states, the
                                     user does not have the Manage ALL_CATEGORIES permission so they must specify a
                                     category to get a set of lifecycle states.  Give them a friendly message to
                                     help them understand this. For a tenant admin, this also helps insure they can
                                     access this portlet later because it must be in one of the tenant categories
                                     since they don't have the ALL_PORTLETS permission. --%>
                                <c:if test="${empty portlet.categories && empty lifecycleStates}">
                                    <p class="text-warning">You must specify a category to be able to save</p>
                                </c:if>
                                <ul class="config-list">
                                    <c:forEach items="${ portlet.categories }" var="category">
                                        <li><i class="fa fa-folder-open"></i> ${fn:escapeXml(category.name )}</li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </div>
                    </div> <!-- end: portlet categories -->
                </div>
            </div>
        </div> <!-- end: portlet config groups and categories -->
    </div>

    <!-- Portlet Lifecycle -->
    <div id="${n}PortletLifecycle" class="fl-widget portlet ptl-mgr view-lifecycle" role="section">
        <!-- Portlet Content -->
        <div class="fl-widget-content content portlet-content" role="main">
            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="lifecycle.management"/></h3>
                </div>
                <div class="content">
                    <div class="preference-options-section">
                        <table class="portlet-table table table-hover" summary="">
                            <thead>
                                <tr>
                                    <th><spring:message code="option"/></th>
                                    <th><spring:message code="state"/></th>
                                    <th><spring:message code="description"/></th>
                                </tr>
                            </thead>
                            <tfoot></tfoot>
                            <tbody>
                            <c:forEach items="${ lifecycleStates }" var="lifecycleState">
                                <tr>
                                    <td align="center">
                                        <form:radiobutton path="lifecycleState" value="${ lifecycleState }"
                                                          cssClass="portlet-form-input-field lifecycle-state"/>
                                    </td>
                                    <td><spring:message code="lifecycle.name.${ lifecycleState }"/></td>
                                    <td><spring:message code="lifecycle.description.${ lifecycleState }"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <!-- end: portlet-section -->

            <!-- Portlet Section -->
            <div class="portlet-section" id="publishingDateSection" style="display: none;">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="auto.publish.optional"/></h3>
                </div>
                <div class="content">

                    <table class="portlet-table table table-hover"
                           summary="<spring:message code="publish.and.expiration.dates"/>">
                        <thead>
                        <tr>
                            <th><spring:message code="option"/></th>
                            <th><spring:message code="setting"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td class="fl-text-align-right"><spring:message code="auto.publish.date.time"/></td>
                            <td>
                                <form:input path="publishDateString" size="10" cssClass="cal-datepicker"/>
                                    <span style="${ portlet.publishDate == null ? 'display:none' : '' }">
                                         <form:select path="publishHour">
                                             <c:forEach begin="1" end="12" var="hour">
                                                 <form:option value="${ hour }"/>
                                             </c:forEach>
                                         </form:select>:<form:select path="publishMinute">
                                       <c:forEach begin="0" end="59" var="min">
                                           <fmt:formatNumber var="m" value="${ min }" minIntegerDigits="2"/>
                                           <form:option value="${ m }"/>
                                       </c:forEach>
                                    </form:select>
                                     <form:select path="publishAmPm">
                                         <form:option value="0" label="${ amLabel }"/>
                                         <form:option value="1" label="${ pmLabel }"/>
                                     </form:select>
                             (<a class="clear-date" href="javascript:;"><spring:message code="reset"/></a>)
                         </span>
                            </td>
                        </tr>
                        </tbody>
                    </table>

                </div>
            </div>
            <!-- end: portlet-section -->

            <!-- Portlet Section -->
            <div class="portlet-section" id="expirationDateSection" style="display: none;">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="auto.expire.optional"/></h3>
                </div>
                <div class="content">

                    <table class="portlet-table table table-hover"
                           summary="<spring:message code="publish.and.expiration.dates"/>">
                        <thead>
                            <tr>
                                <th><spring:message code="option"/></th>
                                <th><spring:message code="setting"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td class="fl-text-align-right"><spring:message code="auto.expire.date.time"/></td>
                                <td>
                                    <form:input path="expirationDateString" size="10" cssClass="cal-datepicker"/>
                                    <span style="${ portlet.expirationDate == null ? 'display:none' : '' }">
                                        <form:select path="expirationHour">
                                            <c:forEach begin="1" end="12" var="hour">
                                                <form:option value="${ hour }"/>
                                            </c:forEach>
                                        </form:select>:
                                        <form:select path="expirationMinute">
                                            <c:forEach begin="0" end="59" var="min">
                                                <fmt:formatNumber var="m" value="${ min }" minIntegerDigits="2"/>
                                                <form:option value="${ m }"/>
                                            </c:forEach>
                                        </form:select>
                                        <form:select path="expirationAmPm">
                                            <form:option value="0" label="${ amLabel }"/>
                                            <form:option value="1" label="${ pmLabel }"/>
                                        </form:select>
                                        (<a class="clear-date" href="javascript:;"><spring:message code="reset"/></a>)
                                    </span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div> <!-- end: portlet-section -->

            <!-- Buttons -->
            <div class="buttons">
                <c:if test="${supportsConfig}">
                    <input class="button btn btn-primary" type="submit" value="<spring:message code="save.and.configure"/>" name="_eventId_saveAndConfig"/>
                </c:if>
                <input class="button btn btn-primary" type="submit" value="<spring:message code="save"/>"name="_eventId_save">
                <c:choose>
                    <c:when test="${completed}">
                        <input class="button btn btn-link" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
                    </c:when>
                    <c:otherwise>
                        <input class="button btn" type="submit" value="<spring:message code="back"/>" name="_eventId_back"/>
                    </c:otherwise>
                </c:choose>
            </div><!-- end: Portlet Buttons -->
        </div> <!-- end: portlet-content -->
    </div><!-- end: portlet -->
</form:form>


<div style="display:none">
    <div id="${n}addParameterDialog" class="parameter-adding-dialog jqueryui"
         title="<spring:message code="add.preference"/>">
        <div>
            <form id="${n}addParameterForm" action="javascript:;">
                <p><spring:message code="preference.name"/>:
                    <input name="name"/>
                </p>
                <input type="submit" value="<spring:message code="add"/>"/>
            </form>
        </div>
    </div>
</div>
</div>

<script src="media/skins/universality/common/javascript/uportal/up-parameter-editor.js" language="JavaScript" type="text/javascript"></script>
<script type="text/javascript">
    (function ($, _, Backbone) {
        var titleSelector = '#portletTitle',
                nameSelector = '#portletName',
                fnameSelector = '#portletFname',
                nameTitleMismatchSelector = '.name-title-mismatch-warn',
                NameTitleModel,
                nameTitleModel,
                nameTitleMismatchTimer,
                nameChangedFn,
                titleChangedFn,
                fnameChangedFn;

        /**
         * Define a backbone model to capture the name/title/fname values and
         * orchestrate their updates.  Changes on the page should update the model
         * and allow listeners attached to the model to handle propagating the changes
         * to the appropriate places in the UI
         */
        NameTitleModel = Backbone.Model.extend({
            defaults: {
                name: '',
                title: '',
                titleWarn: false,
                titleEdited: false,
                fname: '',
                fnameEdited: false,
                timeout: '10000'
            },


            /**
             * Initialize the model.
             */
            initialize: function () {
                this.on('change:title', this._updateTitle, this);
                this.on('change:name', this._updateName, this);
                this.on('change:fname', this._updateFName, this);
            },


            /**
             * import existing settings into the model.  Will attempt to determine if the
             * title and name match and if the title and fname are in sync.  If so, it will
             * enable auto-updates to those fields.  If not, changing title will not affect
             * the fields that appear to have been modified.
             */
            import: function (name, title, fname) {
                var calcFName, titleEdited, fnameEdited;

                if (title && name) {
                    if (title !== name) {
                        titleEdited = true;
                    }
                }

                if (title && fname) {
                    calcFName = this._toFName(title);
                    if (fname !== calcFName) {
                        fnameEdited = true;
                    }
                }

                this.set({
                    name: name,
                    title: title,
                    fname: fname,
                    titleEdited: titleEdited,
                    fnameEdited: fnameEdited
                }, {validate: true});
            },


            /**
             * Do validation to check for name/title mismatches and set the 'titleWarn' attribute.
             */
            validate: function (attrs, options) {
                var finalAttrs = _.extend({}, this.attributes, attrs);
                this.set('titleWarn', finalAttrs.title !== finalAttrs.name);
            },


            /**
             * Given a title, calculate the fname to use.
             */
            _toFName: function (name) {
                var tmp = name.toLowerCase();
                return tmp ? tmp.replace(/[^a-z0-9]+/g, '-') : '';
            },


            /**
             * Internal event for when 'title' changes.  Will try to update name and fname if
             * they have not been edited.
             */
            _updateTitle: function (model, value) {
                if (!model.get('nameEdited')) {
                    model.set('name', value, {validate: true});
                }

                if (!model.get('fnameEdited')) {
                    model.set('fname', this._toFName(value));
                }
            },


            /**
             * Internal event handler for when 'name' changes.
             */
            _updateName: function (model, value) {
                model.attributes.nameEdited = !(model.get('title') === value);
            },


            /**
             * Internal event handler for when fname changes.
             */
            _updateFName: function (model, value) {
                var calcFName = this._toFName(model.get('title'));
                model.attributes.fnameEdited = !(calcFName === value);
            }
        });

        // instantiate the model...
        nameTitleModel = new NameTitleModel();
        nameTitleModel.import($(nameSelector).val(), $(titleSelector).val(), $(fnameSelector).val());

        /* When the model changes, update the UI */
        nameTitleModel.on('change:title', function (model, value) {
            $(titleSelector).val(value);
        });

        nameTitleModel.on('change:name', function (model, value) {
            $(nameSelector).val(value);
        });

        nameTitleModel.on('change:fname', function (model, value) {
            $(fnameSelector).val(value);
        });

        nameTitleModel.on('change:titleWarn', function (model, value) {
            clearTimeout(nameTitleMismatchTimer);
            // don't re-render on every keystroke to avoid flashing.
            nameTitleMismatchTimer = setTimeout(function () {
                if (value) {
                    $(nameTitleMismatchSelector).slideDown('slow');
                } else {
                    $(nameTitleMismatchSelector).slideUp('slow');
                }
            }, 500);
        });

        nameChangedFn = function() {
            nameTitleModel.set('title', $(titleSelector).val(), {validate: true});
        };

        titleChangedFn = function() {
            nameTitleModel.set('name', $(nameSelector).val(), {validate: true});
        };

        fnameChangedFn = function() {
            nameTitleModel.set('fname', $(fnameSelector).val());
        };

        // When the user enters values, update the model...
        $(titleSelector).keyup(nameChangedFn).focus(nameChangedFn);
        $(nameSelector).keyup(titleChangedFn).focus(titleChangedFn);
        $(fnameSelector).keyup(fnameChangedFn).focus(fnameChangedFn);
    })(up.jQuery, up._, up.Backbone);


    up.jQuery(function () {
        var $ = up.jQuery,
                helpIconSelector = '#${n} .glyphicon-info-sign';

        /* Enable the help tooltips */
        $(helpIconSelector).bootstrapTooltip({
            container: 'body',
            trigger: 'click'
        });

        // clicking anywhere on the page should dismiss the currently visible
        // tooltip.
        $('body').click(function (evt) {
            var $target = $(evt.target);

            $(helpIconSelector).each(function (idx, el) {
                if (!$(el).is($target)) {
                    $(el).bootstrapTooltip('hide');
                }
            });
        });

        $(document).ready(function () {
            $("div.parameter-options-section").each(function () {
                up.ParameterEditor(this, {
                    parameterNamePrefix: $(this).attr("prefix"),
                    parameterBindName: 'parameters',
                    auxiliaryBindName: 'parameterOverrides',
                    useAuxiliaryCheckbox: true,
                    dialog: $("#${n}addParameterDialog-" + $(this).attr("dialog")),
                    multivalued: false,
                    messages: {
                        remove: '<spring:message code="remove" htmlEscape="false" javaScriptEscape="true"/>',
                        removeParameter: '<spring:message code="setParameters.deleteButton" htmlEscape="false" javaScriptEscape="true"/>',
                        addValue: '<spring:message code="add.value" htmlEscape="false" javaScriptEscape="true"/>'
                    }
                });
            });
            $("div.preference-options-section").each(function () {
                up.ParameterEditor(this, {
                    displayClasses: {
                        addValueLinkExtraClass: 'btn btn-xs btn-info',
                        deleteValueLinkExtraClass: 'btn btn-xs btn-default',
                        deleteItemLinkExtraClass: 'btn btn-warning',
                        inputElementExtraClass: "form-control parameter-editor-value"
                    },
                    parameterBindName: 'portletPreferences',
                    auxiliaryBindName: 'portletPreferenceReadOnly',
                    useAuxiliaryCheckbox: true,
                    dialog: $("#${n}addParameterDialog"),
                    form: $("#${n}addParameterForm"),
                    multivalued: true,
                    messages: {
                        remove: '<spring:message code="remove" htmlEscape="false" javaScriptEscape="true"/>',
                        removeParameter: '<spring:message code="setParameters.deleteButton" htmlEscape="false" javaScriptEscape="true"/>',
                        addValue: '<spring:message code="add.value" htmlEscape="false" javaScriptEscape="true"/>'
                    }
                });
            });

            <c:if test="${empty lifecycleStates}">
                // If there are no lifecycle states showing, which can happen for a tenant administrator
                // before they select a category, disable the Save and Configure and Save buttons.
                $('#${n} input[name="_eventId_saveAndConfig"]').prop('disabled',true);
                $('#${n} input[name="_eventId_save"]').prop('disabled',true);
            </c:if>
        });
    });


    up.jQuery(function () {
        var $ = up.jQuery;
        $(document).ready(function () {
            var updateOptionalInputs = function () {
                var lifecycle = $('#${n}PortletLifecycle .lifecycle-state:checked').val();
                $('#${n}PortletLifecycle #publishingDateSection').css('display', lifecycle == "APPROVED" ? "block" : "none");
                $('#${n}PortletLifecycle #expirationDateSection').css('display', lifecycle == "PUBLISHED" ? "block" : "none");
            };
            $("#${n}PortletLifecycle .cal-datepicker").datepicker().change(function () {
                if ($(this).val()) $(this).next().css("display", "inline");
                else $(this).next().css("display", "none");
            });
            $("#${n}PortletLifecycle .clear-date").click(function () {
                $(this).parent().css("display", "none").prev().val("");
            });
            $("#${n}PortletLifecycle .lifecycle-state").click(function () {
                updateOptionalInputs();
            });
            updateOptionalInputs();
        });

        function toggleChevron(e) {
            $(e.target)
            .prev('.panel-heading')
            .find("i.fa")
            .toggleClass('fa-chevron-down fa-chevron-up');
            }
        $('#accordion').on('hidden.bs.collapse', toggleChevron);
        $('#accordion').on('shown.bs.collapse', toggleChevron);
    });
</script>
