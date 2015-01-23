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
    #${n} .portlet-section {
        margin-bottom: 2em;
    }

    #${n} .portlet-section .titlebar {
        margin: 2em 0;
    }

    #${n} .portlet-section .titlebar .title {
        background-color: #f5f6f7;
        border-bottom: 2px solid #eee;
        border-radius: 4px;
        font-weight: 400;
        margin: 0;
        padding: 0.25em 1em;
    }

    #${n} .form-group label {
        color: #000;
    }

    #${n} .buttons {
        border-top: 1px dotted #ccc;
        border-bottom: 1px dotted #ccc;
        margin: 1em 0;
        padding: 1em 0;
    }

    #${n} .glyphicon-info-sign {
        color: #3a7eef;
    }

    #${n} .name-title-mismatch-warn {
        display: none;
    }
</style>

<form:form modelAttribute="portlet" action="${queryUrl}" method="POST" role="form" class="form-horizontal">
    <!-- Portlet -->
    <div class="fl-widget portlet ptl-mgr view-basicinfo" id="${n}" role="section">

        <!-- Portlet Titlebar -->
        <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
            <h2 class="title" role="heading">
                <spring:message code="${ completed ? 'edit.portlet' : 'register.new.portlet' }"/>
            </h2>
        </div>
        <!-- end: portlet-titlebar -->

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
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="summary.information"/></h3>
                </div>
                <div class="content">
                    <div class="form-group">
                        <span class="col-sm-3 control-label">
                            <label for="portletTitle"><spring:message code="portlet.title"/></label>
                            <span class="glyphicon glyphicon-info-sign"
                                  title="<spring:message code='portlet.title.tooltip'/>" data-toggle="tooltip"
                                  data-placement="top"></span>
                        </span>

                        <div class="col-sm-9">
                            <form:input path="title" type="text" class="form-control" id="portletTitle"/>
                        </div>
                    </div>
                    <div class="form-group name-title-mismatch-warn">
                        <div class="col-sm-offset-3 col-sm-9">
                            <div class="alert alert-info">
                                <spring:message code="portlet.name.title.mismatch"/>
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <span class="col-sm-3 control-label">
                            <label for="portletName"><spring:message code="portlet.name"/></label>
                            <span class="glyphicon glyphicon-info-sign"
                                  title="<spring:message code='portlet.name.tooltip'/>" data-toggle="tooltip"
                                  data-placement="top"></span>
                        </span>

                        <div class="col-sm-9">
                            <form:input path="name" type="text" class="form-control" id="portletName"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <span class="col-sm-3 control-label">
                            <label for="portletFname"><spring:message code="portlet.functional.name"/></label>
                            <span class="glyphicon glyphicon-info-sign"
                                  title="<spring:message code='portlet.functional.name.tooltip'/>" data-toggle="tooltip"
                                  data-placement="top"></span>
                        </span>

                        <div class="col-sm-9">
                            <form:input path="fname" type="text" class="form-control" id="portletFname"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <span class="col-sm-3 control-label">
                            <label for="portletDescription"><spring:message code="portlet.description"/></label>
                            <span class="glyphicon glyphicon-info-sign"
                                  title="<spring:message code='portlet.description.tooltip'/>" data-toggle="tooltip"
                                  data-placement="top"></span>
                        </span>

                        <div class="col-sm-9">
                            <form:input path="description" type="text" class="form-control" id="portletDescription"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <span class="col-sm-3 control-label">
                            <label for="portletTimeout"><spring:message code="portlet.timeout"/></label>
                            <span class="glyphicon glyphicon-info-sign" title="<spring:message code='portlet.timeout.tooltip'/>"
                                  data-toggle="tooltip" data-placement="top"></span>
                        </span>

                        <div class="col-sm-9">
                            <form:input path="timeout" type="text" class="form-control" id="portletTimeout"/>
                        </div>
                    </div>

                </div>
            </div>
            <!-- end: portlet-section -->

            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="controls"/></h3>
                </div>
                <div class="content">

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
            <!-- end: portlet-section -->
        </div>
        <!-- end: portlet-content -->

    </div>
    <!-- end: portlet -->


    <!-- ---------------------------------------------------------------------------------------------------------------------------------------------- -->

    <!-- Portlet -->
    <div class="fl-widget portlet ptl-mgr view-setparameters" role="section">

        <!-- Portlet Titlebar -->
        <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
            <h2 class="title" role="heading">
                <spring:message code="${ completed ? 'edit.portlet' : 'register.new.portlet' }"/>
            </h2>
        </div>
        <!-- end: portlet-titlebar -->

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

            <!-- Portlet.xml Preferences Section -->
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
                            <th><spring:message code="preferences"/></th>
                            <th><spring:message code="values"/></th>
                            <th><spring:message code="read.only.prevents.user.customization"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${ portletDescriptor.portletPreferences.portletPreferences }" var="pref">
                            <tr class="${ up:containsKey(portlet.portletPreferences, pref.name) ? 'override-preference' : '' }">
                                <td class="preference-name">${ fn:escapeXml(pref.name )}</td>
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
            </div>
            <!-- END: Portlet.xml Preferences Section -->

            <!-- Step Loop -->
            <c:forEach items="${ cpd.steps }" var="step" varStatus="status">

                <!-- Portlet Section -->
                <div class="portlet-section" role="region">
                    <div class="titlebar">
                        <h3 class="title" role="heading">
                            <spring:message code="${ step.name }" text="${ step.name }"/>
                        </h3>
                    </div>
                    <div class="content">
                        <p class="note" role="note">${ fn:escapeXml(step.description )}</p>

                        <!-- Portlet Parameters -->
                        <c:if test="${ fn:length(step.parameters) > 0 }">
                            <table class="portlet-table table table-hover"
                                   summary="<spring:message code="this.table.lists.portlet.parameters"/>">
                                <thead>
                                <tr>
                                    <th><spring:message code="parameter"/></th>
                                    <th><spring:message code="value"/></th>
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
                                                <td><span class="uportal-label"><spring:message
                                                        code="${ parameter.label }"
                                                        text="${ parameter.label }"/>:</span>
                                                </td>
                                                <td>
                                                    <editPortlet:parameterInput
                                                            input="${ parameter.parameterInput.value }"
                                                            path="${ paramPath }"/>
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:if> <!-- End Portlet Parameters -->

                        <c:if test="${ portlet.portlet }">
                            <c:if test="${ fn:length(step.preferences) > 0 }">
                                <div class="preference-options-section">
                                    <table class="portlet-table table table-hover"
                                           summary="<spring:message code="this.table.lists.portlet.parameters"/>">
                                        <thead>
                                        <tr>
                                            <th><spring:message code="parameter"/></th>
                                            <th><spring:message code="value"/></th>
                                            <th><spring:message code="read.only.prevents.user.customization"/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${ step.preferences }" var="preference">
                                            <c:set var="paramPath"
                                                   value="portletPreferences['${ preference.name }'].value"/>
                                            <c:set var="overrideParamPath"
                                                   value="portletPreferenceReadOnly['${ preference.name }'].value"/>
                                            <c:choose>
                                                <c:when test="${ preference.preferenceInput.value.display == 'HIDDEN' }">
                                                    <c:set var="values"
                                                           value="${ portlet.portletPreferences[preference.name].value }"/>
                                                    <input type="hidden" name="${ fn:escapeXml(paramPath )}"
                                                           value="${ fn:escapeXml(fn:length(values) > 0 ? values[0] : '' )}"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <tr>
                                                        <td class="preference-name"><span
                                                                class="uportal-label"><spring:message
                                                                code="${ preference.label }"
                                                                text="${ preference.label }"/>:</span>
                                                        </td>
                                                        <td>
                                                            <editPortlet:preferenceInput
                                                                    input="${ preference.preferenceInput.value }"
                                                                    path="${ paramPath }" name="${ preference.name }"
                                                                    values="${ portlet.portletPreferences[preference.name].value }"/>
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
                                            <th><spring:message code="preference"/></th>
                                            <th><spring:message code="value"/></th>
                                            <th><spring:message code="read.only.prevents.user.customization"/></th>
                                            <th></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${ arbitraryPreferenceNames }" var="name">
                                            <c:set var="paramPath" value="portletPreferences['${ name }'].value"/>
                                            <c:set var="overrideParamPath"
                                                   value="portletPreferenceReadOnly['${ name }'].value"/>
                                            <tr>
                                                <td class="preference-name">${ fn:escapeXml(name) }</td>
                                                <td>
                                                    <c:forEach items="${ portlet.portletPreferences[name].value }"
                                                               var="val">
                                                        <div>
                                                            <input name="portletPreferences['${fn:escapeXml(name)}'].value"
                                                                   value="${ fn:escapeXml(val) }"/>
                                                            <a class="delete-parameter-value-link" href="javascript:;">
                                                                Remove
                                                            </a>
                                                        </div>
                                                    </c:forEach>
                                                    <input type="hidden"
                                                           name="portletPreferences['${fn:escapeXml(name)}'].value"
                                                           value=""/>
                                                    <a class="add-parameter-value-link" href="javascript:;"
                                                       paramName="${fn:escapeXml(name)}">Add value
                                                    </a>
                                                </td>
                                                <td>
                                                    <form:checkbox path="${overrideParamPath}" value="true"/>
                                                </td>
                                                <td>
                                                    <a class="delete-parameter-link" href="javascript:;"><spring:message
                                                            code="setParameters.deleteButton"/></a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                    <p>
                                        <a class="add-parameter-link" href="javascript:;"><spring:message
                                                code="add.preference"/></a>
                                    </p>
                                </div>
                            </c:if>


                        </c:if> <!-- End Portlet Preferences -->

                    </div>
                    <!-- end: content -->

                </div>
                <!-- end: portlet-section -->

            </c:forEach> <!-- End Step Loop -->
        </div>
        <!-- end: portlet-content -->
    </div>
    <!-- end: portlet -->

    <!-- ----------------------------------------------------------------------------------------------------------- -->
    <div class="form-group">
        <label class="col-sm-3 control-label"><spring:message code="groups"/></label>
        <div class="col-sm-9">
            <p class="form-control-static">
                <c:forEach items="${ portlet.groups }" var="group">
                    <a class="label label-info" href="${ chooseGroupUrl }">${ fn:escapeXml(group.name )}</a>
                </c:forEach>
            </p>
        </div>
        <div class="col-sm-offset-3">
            <input class="button btn" type="submit" value="<spring:message code="edit.groups"/>" name="_eventId_chooseGroup"/>
        </div>
    </div>

    <!-- ----------------------------------------------------------------------------------------------------------- -->
    <div class="form-group">
        <label class="col-sm-3 control-label"><spring:message code="categories"/></label>
        <div class="col-sm-9">
            <p class="form-control-static">
                <c:forEach items="${ portlet.categories }" var="category">
                    <a class="label label-info" href="${ chooseCategoryUrl }">${ fn:escapeXml(category.name )}</a>
                </c:forEach>
            </p>
        </div>
        <div class="col-sm-offset-3">
            <input class="button btn" type="submit" value="<spring:message code="edit.categories"/>" name="_eventId_chooseCategory"/>
        </div>
    </div>

    <!-- ---------------------------------------------------------------------------------------------------------------------------------------------- -->

    <!-- Portlet -->
    <div id="${n}PortletLifecycle" class="fl-widget portlet ptl-mgr view-lifecycle" role="section">

        <!-- Portlet Titlebar -->
        <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
            <h2 class="title" role="heading">
                <spring:message code="${ completed ? 'edit.portlet' : 'register.new.portlet' }"/>
            </h2>
        </div>
        <!-- end: portlet-titlebar -->

        <!-- Portlet Content -->
        <div class="fl-widget-content content portlet-content" role="main">

            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="lifecycle.management"/></h3>
                </div>
                <div class="content">

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
                                         <form:option value="0" label="AM"/>
                                         <form:option value="1" label="PM"/>
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
                     </form:select>:<form:select path="expirationMinute">
                         <c:forEach begin="0" end="59" var="min">
                             <fmt:formatNumber var="m" value="${ min }" minIntegerDigits="2"/>
                             <form:option value="${ m }"/>
                         </c:forEach>
                     </form:select>
                     <form:select path="expirationAmPm">
                         <form:option value="0" label="AM"/>
                         <form:option value="1" label="PM"/>
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

            <!-- Buttons -->
            <div class="buttons">
                <c:if test="${supportsConfig}">
                    <input class="button btn btn-primary" type="submit"
                           value="<spring:message code="save.and.configure"/>" name="_eventId_saveAndConfig"/>
                </c:if>
                <input class="button btn btn-primary" type="submit" value="<spring:message code="continue"/>"
                       name="_eventId_save">
                <c:choose>
                    <c:when test="${completed}">
                        <input class="button btn btn-link" type="submit" value="<spring:message code="cancel"/>"
                               name="_eventId_cancel"/>
                    </c:when>
                    <c:otherwise>
                        <input class="button btn" type="submit" value="<spring:message code="back"/>"
                               name="_eventId_back"/>
                    </c:otherwise>
                </c:choose>

            </div>
            <!-- end: Portlet Buttons -->


        </div>
        <!-- end: portlet-content -->

    </div>
    <!-- end: portlet -->
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

<script src="media/skins/universality/common/javascript/uportal/up-parameter-editor.js" language="JavaScript" type="text/javascript"></script>
<script type="text/javascript">
    (function ($, _, Backbone) {
        var selector = '#${n} .glyphicon-info-sign',
                titleSelector = '#portletTitle',
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
                timeout: '5000'
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

        /* Enable the help tooltips */
        $(selector).bootstrapTooltip({
            container: 'body',
            trigger: 'click'
        });

        // clicking anywhere on the page should dismiss the currently visible
        // tooltip.
        $('body').click(function (evt) {
            var $target = $(evt.target);

            $(selector).each(function (idx, el) {
                if (!$(el).is($target)) {
                    $(el).bootstrapTooltip('hide');
                }
            });
        });
    })(up.jQuery, up._, up.Backbone);


    up.jQuery(function () {
        var $ = up.jQuery;
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
                        }
                );
            });
            $("div.preference-options-section").each(function () {
                up.ParameterEditor(this, {
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
                        }
                );
            });
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
    });
</script>
