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

<style>
#${n}tenantManager .panel {
    margin-top: 15px;
}
#${n}tenantManager .panel-heading {
    font-weight: bold;
    position: relative;
}
</style>

<portlet:renderURL var="showAddTenantUrl">
    <portlet:param name="action" value="showAddTenant"/>
</portlet:renderURL>

<div id="${n}tenantManager">

    <div class="panel panel-default tenant-manager">
        <!-- Default panel contents -->
        <div class="panel-heading clearfix">
            <spring:message code="tenant.manager" />
            <div class="btn-group pull-right">
                <a href="${showAddTenantUrl}" class="btn btn-primary btn-xs"><i class="fa fa-plus"></i> <spring:message code="tenant.manager.add" /></a>
            </div>
        </div>
        <div class="panel-body">
            <p><spring:message code="tenant.manager.welcome" /></p>

            <c:choose>
                <c:when test="${fn:length(tenantsList) gt 0}">
                    <table class="table table-striped table-hover">
                        <thead>
                            <tr>
                                <th><spring:message code="tenant.manager.name" /></th>
                                <th><spring:message code="tenant.manager.fname" /></th>
                                <th><spring:message code="tenant.manager.adminContactUsername" /></th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${tenantsList}" var="tenant">
                                <portlet:actionURL var="removeUrl">
                                    <portlet:param name="action" value="doRemoveTenant"/>
                                    <portlet:param name="fname" value="${tenant.fname}"/>
                                </portlet:actionURL>
                                <portlet:renderURL var="detailsUrl">
                                    <portlet:param name="action" value="showTenantDetails"/>
                                    <portlet:param name="fname" value="${tenant.fname}"/>
                                </portlet:renderURL>
                                <tr>
                                    <td><a href="${detailsUrl}" title="<spring:message code="tenant.manager.edit" /> ${tenant.name}"><c:out value="${tenant.name}" /></a></td>
                                    <td><c:out value="${tenant.fname}" /></td>
                                    <td><a href="mailto:${tenant.attributesMap['adminContactEmail']}" title="<spring:message code="tenant.manager.email.address.link" />">${tenant.attributesMap['adminContactUsername']}</a></td>
                                    <td class="text-right"><a class="btn btn-xs btn-danger up-tenant-remove" data-href="${removeUrl}" data-confirm="<spring:message code="tenant.manager.remove.tenant.confirm" arguments="${tenant.name}" />" title="<spring:message code="tenant.manager.remove.tenant" />" href="javascript:void(0)"><span class="glyphicon glyphicon-trash"></span> <spring:message code="tenant.manager.remove" /></a></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="col-sm-8 col-sm-offset-2 alert alert-info"><spring:message code="tenant.manager.no.tenants" /></div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script type="text/javascript">
(function($) {
    // Deleting a tenant requires an actionURL and a POST...
    $('#${n}tenantManager a.up-tenant-remove').click(function() {
        var confirmText = $(this).attr('data-confirm');
        if (confirm(confirmText)) {
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
