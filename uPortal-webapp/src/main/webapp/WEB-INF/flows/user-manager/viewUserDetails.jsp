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
<portlet:renderURL var="backUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="cancel"/>
</portlet:renderURL>
<portlet:renderURL var="deleteUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="confirmRemove"/>
</portlet:renderURL>
<portlet:renderURL var="permissionsUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="viewPermissions"/>
</portlet:renderURL>
<portlet:renderURL var="editDetailsUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="editUser"/>
</portlet:renderURL>
<portlet:actionURL var="impersonateUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="swap"/>
</portlet:actionURL>
<portlet:renderURL var="resetLayoutUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="resetLayout"/>
</portlet:renderURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div id="${n}" class="fl-widget portlet user-mgr view-reviewuser" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">${ fn:escapeXml(person.name )}</h2>
        <div class="btn-group toolbar">
                <c:if test="${ canEdit }">
                    <a class="btn btn-default" href="${ editDetailsUrl }"><spring:message code="edit" text="Edit" /></a>
                </c:if>
                <a class="btn btn-default" href="${ permissionsUrl }"><spring:message code="view.permissions" text="Permissions" /></a>
                <c:if test="${ canDelete }">
                    <a class="btn btn-default" href="${ deleteUrl }"><spring:message code="delete" text="Delete" /></a>
                </c:if>
                <a class="btn btn-default" href="${ resetLayoutUrl }"><spring:message code="reset.user.layout" text="Reset User Layout" /></a>
                <c:if test="${ canImpersonate }">
                	<a class="btn btn-default dropdown-toggle" type="button" id="dropdownMenuImpersonate" data-toggle="dropdown">
                    <spring:message code="impersonate" text="Impersonate"/>
                    <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-right up-impersonation-menu" role="menu" aria-labelledby="dropdownMenuImpersonate">
                      <li role="presentation"><a role="menuitem" tabindex="-1" data-href="${ impersonateUrl }" href="javascript:void(0)"><spring:message code="label.default.profile" text="Default Profile"/></a></li>
                      <c:forEach var="profile" items="${profiles}">
                        <portlet:actionURL var="swapDynamicURL">
                            <portlet:param name="execution" value="${flowExecutionKey}" />
                            <portlet:param name="_eventId" value="swapDynamic"/>
                            <portlet:param name="profile" value="${profile.value.profileFname}" />
                        </portlet:actionURL>
                        <li role="presentation"><a role="menuitem" tabindex="-1" data-href="${ swapDynamicURL }" href="javascript:void(0)">${profile.value.profileName}</a></li>
                      </c:forEach>
                    </ul>
                </c:if>
        </div>
    </div> <!-- end: portlet-titlebar -->

    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">

        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading"><spring:message code="attributes" text="Attributes" /></h3>
            </div>
            <div class="portlet-content">

                <table class="portlet-table table">
                    <thead>
                        <tr>
                            <th><spring:message code="attribute.name" text="Attribute" /></th>
                            <th><spring:message code="attribute.value" text="Value" /></th>
                        </tr>
                    </thead>
                    <c:forEach items="${ groupedAttributes }" var="attribute">
                        <tr>
                            <td class="attribute-name">
                                <strong>${ attribute.displayName }</strong>
                                (<c:forEach items="${attribute.attributeNames}" var="name" varStatus="status">${ name }${ status.last ? '' : ',' }</c:forEach>)
                            </td>
                            <td>
                                <c:forEach items="${ attribute.values }" var="value">
                                   <div>${fn:escapeXml(value)}</div>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
        </div>

        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading"><spring:message code="parent.groups" text="Parent Groups" /></h3>
            </div>
            <div class="content">
                <ul style="list-style: none;">
                    <c:forEach items="${parents}" var="group">
                        <li><i class="fa fa-users"></i> <c:out value="${group.name}" /></li>
                    </c:forEach>
                </ul>
            </div>
        </div>

        <div class="buttons">
            <a class="button btn btn-default" href="${ backUrl }"><spring:message code="back" text="Back" /></a>
        </div>
    </div>
</div>

<script type="text/javascript">
(function($) {
    // Impersonation requests must be an actionURL and a POST...
    $('#${n} .up-impersonation-menu a').click(function() {
        var url = $(this).attr('data-href');
        var form = $('<form />', {
            action: url,
            method: 'POST',
            style: 'display: none;'
        });
        form.appendTo('body').submit();
    });
})(up.jQuery);
</script>
