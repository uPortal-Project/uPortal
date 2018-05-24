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
<c:set var="n"><portlet:namespace/></c:set>

<portlet:actionURL var="doAddTenantUrl">
    <portlet:param name="action" value="doAddTenant"/>
</portlet:actionURL>

<style>
#${n}tenantManager .form-fields li {
    list-style: none;
}

/* Down Caret*/
#${n}tenantManager #addInfoToggle.collapsed i:after {
    content: "\f0d7";
}
/* Up Caret*/
#${n}tenantManager #addInfoToggle i:after {
    content: "\f0d8";
}

#${n}tenantManager .field-error {
    display: none;
    padding: 3px;
    margin: 0 5px;
}
#${n}tenantManager .has-error .field-error {
    display: block;
}
</style>

<div id="${n}tenantManager">
    <h2><spring:message code="tenant.manager.add" /></h2>
    <form id="addTenantForm" role="form" class="form-horizontal" action="${doAddTenantUrl}" method="POST">
        <c:set var="errorCssClass">
            <c:choose>
                <c:when test="${invalidFields['name'] ne null}">has-error</c:when>
                <c:otherwise></c:otherwise>
            </c:choose>
        </c:set>
        <div class="form-group ${errorCssClass}">
            <label for="tenantName" class="col-sm-2 control-label"><spring:message code="tenant.manager.name" /></label>
            <div class="col-sm-10">
                <c:set var="previousResponse">
                    <c:choose>
                        <c:when test="${previousResponses['name'] ne null}">${previousResponses['name']}</c:when>
                        <c:otherwise></c:otherwise>
                    </c:choose>
                </c:set>
                <input type="text" class="form-control" name="name" id="tenantName" value="${previousResponse}" placeholder="<spring:message code="enter.tenant.name" />" />
                <div class="field-error bg-danger"><spring:message code="enter.tenant.name" /></div>
            </div>
        </div>

        <c:forEach items="${tenantManagerAttributes}" var="attribute">
            <c:set var="errorCssClass">
                <c:choose>
                    <c:when test="${invalidFields[attribute.key] ne null}">has-error</c:when>
                    <c:otherwise></c:otherwise>
                </c:choose>
            </c:set>
            <div class="form-group ${errorCssClass}">
                <label for="${attribute.key}" class="col-sm-2 control-label"><spring:message code="${attribute.value}" /></label>
                <div class="col-sm-10">
                    <c:set var="previousResponse">
                        <c:choose>
                            <c:when test="${previousResponses[attribute.key] ne null}">${previousResponses[attribute.key]}</c:when>
                            <c:otherwise></c:otherwise>
                        </c:choose>
                    </c:set>
                    <input type="text" class="form-control" name="${attribute.key}" id="${attribute.key}" value="${previousResponse}" placeholder="<spring:message code="tenant.manager.addTenant.placeholder.${attribute.key}" />" />
                    <div class="field-error bg-danger"><spring:message code="tenant.manager.addTenant.placeholder.${attribute.key}" /></div>
                </div>
            </div>
        </c:forEach>

        <c:if test="${not empty optionalOperationsListeners}">
            <div class="form-group">
                <label class="col-sm-2 control-label"><spring:message code="tenant.manager.optional.steps" /></label>
                <div class="col-sm-10">
                    <div class="panel panel-default">
                        <div class="panel-body">
                            <div class="checkbox">
                                <c:forEach items="${optionalOperationsListeners}" var="listener">
                                    <label>
                                        <input name="optionalListener" type="checkbox" value="${listener.fname}" checked="checked" /> <c:out value="${listener.name}" />
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <div class="text-right">
            <input type="hidden" name="fname" id="fname" value="">
            <button id="tenantFormSubmit" type="submit" class="btn btn-primary">Submit</button>
            <a href="<portlet:renderURL />" class="btn btn-link">Cancel</a>
        </div>
    </form>

</div>
