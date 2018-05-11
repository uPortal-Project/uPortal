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

<portlet:actionURL var="doUpdateTenantUrl">
    <portlet:param name="action" value="doUpdateTenant"/>
</portlet:actionURL>

<style>
#${n}tenantDetails h2 {
    margin-bottom: 18px;
}
#${n}tenantDetails dt {
    margin-top: 12px;
}
#${n}tenantDetails btn-group btn {
    margin-right: 12px;
}
#${n}tenantDetails .field-error {
    display: none;
    padding: 3px;
    margin: 0 5px;
}
#${n}tenantDetails .has-error div.field-error {
    display: block;
}
</style>

<div id="${n}tenantDetails">
    <h2><spring:message code="tenant.details" /> <c:out value="${tenant.name}" /></h2>
    <form id="updateTenantForm" role="form" class="form-horizontal" action="${doUpdateTenantUrl}" method="POST">
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
                    <c:set var="previousValue">
                        <c:choose>
                            <c:when test="${previousResponses[attribute.key] ne null}">${previousResponses[attribute.key]}</c:when>
                            <c:otherwise>${tenant.attributesMap[attribute.key]}</c:otherwise>
                        </c:choose>
                    </c:set>
                    <input type="text" class="form-control" name="${attribute.key}" id="${attribute.key}" value="${previousValue}" placeholder="<spring:message code="tenant.manager.addTenant.placeholder.${attribute.key}" />" />
                    <div class="field-error bg-danger"><spring:message code="tenant.manager.addTenant.placeholder.${attribute.key}" /></div>
                </div>
            </div>
        </c:forEach>

        <div class="btn-group pull-right">
            <c:forEach items="${operationsListenerAvailableActions}" var="listenerAction">
                <portlet:actionURL var="listenerActionUrl">
                    <portlet:param name="action" value="doListenerAction"/>
                    <portlet:param name="fname" value="${listenerAction.fname}"/>
                </portlet:actionURL>
                <a class="btn btn-default up-tenant-listener-action" data-href="${listenerActionUrl}" href="javascript:void(0)" role="button"><spring:message code="${listenerAction.messageCode}" htmlEscape="false" /></a>
            </c:forEach>
            <button class="btn btn-primary" type="submit" onclick="return confirm('<spring:message code="tenant.manager.update.attributes.confirm" arguments="${tenant.name}" />')"><spring:message code="tenant.manager.update.attributes" htmlEscape="false" /></button>
            <a class="btn btn-link" href="<portlet:renderURL />" role="button"><spring:message code="cancel" /></a>
        </div>
    </form>
</div>

<script type="text/javascript">
(function($) {
    // Tenant operations listener actions must be invoked with an actionURL and a POST...
    $('#${n}tenantDetails a.up-tenant-listener-action').click(function() {
        if (confirm('<spring:message code="tenant.manager.invoke.action.confirm" />')) {
            var url = $(this).attr('data-href');
            var form = $('<form />', {
                action: url,
                method: 'POST',
                style: 'display: none;'
            });
            form.appendTo('body').submit();
        }
    });
})(up.jQuery);
</script>
