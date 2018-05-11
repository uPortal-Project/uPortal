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
<portlet:actionURL var="formUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet user-mgr view-reviewuser" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <c:choose>
                <c:when test="${ accountForm.id < 0 }">
                    <spring:message code="create.new.user"/>
                </c:when>
                <c:otherwise>
                    <portlet:renderURL var="userUrl">
                        <portlet:param name="execution" value="${flowExecutionKey}" />
                        <portlet:param name="_eventId" value="finish"/>
                    </portlet:renderURL>
                    <a href="${ userUrl }">${ fn:escapeXml(accountForm.username )}</a> &gt;
                    <spring:message code="edit.user"/>
                </c:otherwise>
            </c:choose>
        </h2>
    </div> <!-- end: portlet-titlebar -->

    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">

        <form:form modelAttribute="accountForm" action="${formUrl}" method="POST">

            <!-- Portlet Messages -->
            <spring:hasBindErrors name="accountForm">
                <div class="portlet-msg-error portlet-msg error" role="alert">
                    <form:errors path="*" element="div"/>
                </div> <!-- end: portlet-msg -->
            </spring:hasBindErrors>

            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="content">
                    <table class="portlet-table table table-hover" role="presentation">
                        <tbody>
                            <c:if test="${ accountForm.id < 0 }">
                                <tr>
                                    <td class="attribute-name">
                                        <label for="${n}userUsername">
                                            <spring:message code="username" />
                                        </label>
                                    </td>
                                    <td>
                                        <form:input path="username" id="${n}userUsername" class="form-control"/>
                                    </td>
                                </tr>
                            </c:if>
                            <!--  Password and confirm password -->
                            <tr>
                                <td class="attribute-name">
                                    <label for="${n}userPassword">
                                        <spring:message code="password"/>
                                    </label>
                                </td>
                                <td>
                                    <form:password path="password" id="${n}userPassword" class="form-control"/>
                                </td>
                            </tr>
                            <tr>
                                <td class="attribute-name">
                                    <label for="${n}userConfirmPassword">
                                        <spring:message code="confirm.password"/>
                                    </label>
                                </td>
                                <td>
                                    <form:password path="confirmPassword" id="${n}userConfirmPassword" class="form-control"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                </div>
            </div>

            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading"><spring:message code="standard.attributes"/></h3>
                </div>
                <div id="${n}standardAttributes" class="content">

                    <table class="portlet-table table table-hover" role="presentation">
                        <tbody>

                            <!-- Print out each attribute -->
                            <c:forEach items="${ editAttributes }" var="attribute">
                                <tr>
                                    <td class="attribute-name">
                                        <strong>
                                            <label for="${ n }${ attribute.name }">
                                                <spring:message code="${ attribute.label }"/>
                                            </label>
                                        </strong>
                                    </td>
                                    <td>
                                          <c:set var="paramPath" value="attributes['${ attribute.name }'].value"/>
                                          <editPortlet:preferenceInput
                                              id="${ n }${ attribute.name }"
                                              input="${ attribute.preferenceInput.value }"
                                              path="${ paramPath }"
                                              values="${ accountForm.attributes[attribute.name].value }"/>
                                    </td>
                                </tr>
                            </c:forEach>

                        </tbody>
                    </table>

                </div>
            </div>

            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="titlebar">
                    <h3 class="title" role="heading">
                        <spring:message code="custom.attributes"/>
                    </h3>
                </div>
                <div id="${n}customAttributes" class="content">

                    <table class="portlet-table table table-hover" role="presentation">
                        <tbody>

                            <!-- Print out each attribute -->
                            <c:forEach items="${ accountForm.customAttributes }" var="attribute">
                                <tr>

                                    <td class="attribute-name">
                                        <c:set var="attrName" value="${ attribute.key }"/>
                                        <strong>
                                            <label for="${ n }${ attrName }">
                                                <spring:message code="attribute.displayName.${attrName}" text="${attrName}"/>
                                            </label>
                                        </strong>
                                        ${ fn:escapeXml(attribute.key)}
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${ fn:length(attribute.value.value) > 0 }">
                                                <c:forEach var="value" items="${ attribute.value.value }">
                                                    <div>
                                                         <input type="text" id="${ n }${ attrName }" class="form-control" name="attributes['${fn:escapeXml(attribute.key)}'].value" value="${ fn:escapeXml(value )}" />
                                                         <a class="delete-attribute-value-link" href="#">
                                                            <spring:message code="remove"/>
                                                         </a>
                                                    </div>
                                                </c:forEach>
                                                <a class="add-attribute-value-link" href="#" paramName="${fn:escapeXml(name)}">
                                                <spring:message code="add.value"/></a>
                                            </c:when>
                                            <c:otherwise>
                                                <div>
                                                    <input type="text" id="${ n }${ attrName }" class="form-control" name="attributes['${fn:escapeXml(attribute.key)}'].value" value=""/>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a class="delete-attribute-link" aria-label="<spring:message code="remove"/> <spring:message code="attribute.displayName.${attrName}" text="${attrName}"/>" href="#">
                                            <spring:message code="remove"/>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>

                            <tfoot>
                                <td colspan="3">
                                    <a class="add-attribute-link" href="#">
                                    <spring:message code="add.attribute"/></a>
                                </td>
                            </tfoot>

                        </tbody>
                    </table>

                </div>
            </div>

            <!-- Portlet Section -->
            <div class="portlet-section" role="region">
                <div class="content">

                    <div class="buttons">
                        <input class="button btn primary" type="submit" value="<spring:message code="save"/>" name="_eventId_save"/>
                        <c:choose>
                            <c:when test="${ accountForm.id < 0 }">
                                <input class="button btn" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
                            </c:when>
                            <c:otherwise>
                                <input class="button btn" type="submit" value="<spring:message code="cancel"/>" name="_eventId_finish"/>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>
            </div>

        </form:form>

    </div>

    <div id="${n}parameterForm" style="display:none">
        <form>
            <spring:message code="attribute.name"/>: <input name="name"/>
            <input type="submit" value="<spring:message code="add"/>"/>
        </form>
    </div>

</div>

<script type="text/javascript">
    up.jQuery(function() {
        var $ = up.jQuery;
        $(document).ready(function(){
            up.ParameterEditor(
                    $("#${n}standardAttributes"),
                    {
                        parameterBindName: 'attributes',
                        multivalued: true,
                        dialog: $("#${n}parameterForm"),
                        displayClasses: {
                            deleteItemLink: "delete-attribute-link",
                            deleteValueLink: "delete-attribute-value-link",
                            addItemLink: "add-attribute-link",
                            addValueLink: "add-attribute-value-link"
                        },
                        messages: {
                            remove: '<spring:message code="remove" htmlEscape="false" javaScriptEscape="true"/>',
                            addValue: '<spring:message code="add.value" htmlEscape="false" javaScriptEscape="true"/>'
                        }
                    }
                );
            up.ParameterEditor(
                    $("#${n}customAttributes"),
                    {
                        parameterBindName: 'attributes',
                        multivalued: true,
                        dialog: $("#${n}parameterForm"),
                        displayClasses: {
                            deleteItemLink: "delete-attribute-link",
                            deleteValueLink: "delete-attribute-value-link",
                            addItemLink: "add-attribute-link",
                            addValueLink: "add-attribute-value-link"
                        },
                        messages: {
                            remove: '<spring:message code="remove" htmlEscape="false" javaScriptEscape="true"/>',
                            addValue: '<spring:message code="add.value" htmlEscape="false" javaScriptEscape="true"/>'
                        }
                    }
                );
        });
    });
</script>
