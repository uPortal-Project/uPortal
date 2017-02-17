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

<!-- Portlet -->
<section class="fl-widget portlet imp-exp view-delete">

    <!-- Portlet Titlebar -->
    <header class="fl-widget-titlebar titlebar portlet-titlebar">
        <h2 class="title">
            <spring:message code="delete.portlet.entities"/>
        </h2>
        <nav class="toolbar">
            <ul class="btn-group">
                <li class="btn">
                    <a class="button btn btn-primary" href="<portlet:renderURL/>">
                        <spring:message code="import"/>
                        <i class="fa fa-upload" aria-hidden="true"></i>
                    </a>
                </li>
                <li class="btn">
                    <a class="button btn btn-primary" href="<portlet:renderURL><portlet:param name="action" value="export"/></portlet:renderURL>">
                        <spring:message code="export"/>
                        <i class="fa fa-download" aria-hidden="true"></i>
                    </a>
                </li>
            </ul>
        </div>
    </div>

    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">

        <!-- Messages -->
        <div class="portlet-msg-error portlet-msg error alert alert-danger" role="alert">
            <div class="titlebar">
                <h3 class="title">
                    <i class="fa fa-exclamation-triangle" aria-hidden="true"></i>
                    <spring:message code="warning"/>
                    <i class="fa fa-exclamation-triangle" aria-hidden="true"></i>
                </h3>
            </div>
            <div class="content">
                <p>
                    <spring:message code="delete.portlet.warning.explanations"/>
                </p>
            </div>
        </div>

        <!-- Note -->
        <div class="note" role="note">
            <p>
                <spring:message code="delete.portlet.note.select.entity"/>
            </p>
        </div>

        <div class="portlet-form">
            <form id="${n}form" class="form-inline" method="POST">
                <label class="portlet-form-label" for="entityType">
                    <spring:message code="type"/>:
                </label>
                <select id="entityType" class="form-control" name="entityType">
                    <option>
                        [<spring:message code="select.type"/>]
                    </option>
                    <c:forEach items="${supportedTypes}" var="type">
                        <option value="${fn:escapeXml(type.typeId)}">
                            <spring:message code="${type.titleCode}"/>
                        </option>
                    </c:forEach>
                </select>
                <label class="portlet-form-label" for="${n}sysid">
                    <spring:message code="id"/>:
                </label>
                &nbsp;
                <input type="text" id="${n}sysid" class="form-control" name="sysid"/>
                <button class="button btn primary" type="submit">
                    <spring:message code="delete"/>
                    <i class="fa fa-trash-o" aria-hidden="true"></i>
                </button>
            </form>
        </div>

    </div> <!-- end: portlet-content -->
</section> <!-- end:portlet -->

<script type="text/javascript">
    up.jQuery(document).ready(function () {
        var $ = up.jQuery;

        $("#${n}form").submit(function () {
           var form, entityType, sysId, href;

           form = this;
           entityType = form.entityType.value;
           sysId = form.sysid.value;
           $.ajax({
               url: "<c:url value="/api/entity/"/>" + entityType + "/" + sysId,
               type: "DELETE"
           });

           return false;
        });
    });
</script>
