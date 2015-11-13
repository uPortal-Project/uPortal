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

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="queryUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

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
<style>
    .form-group {
        margin-top: 10px;
    }
    .buttons {
        padding-left: 15px;
        padding-right: 15px;
    }
</style>
    
<!-- Portlet -->
<div class="fl-widget portlet ptl-mgr view-chooseportlet bootstrap-styles container-fluid" role="section">
    <div class="row">
        <!-- Portlet Titlebar -->
        <div class="fl-widget-titlebar titlebar portlet-titlebar col-md-12" role="sectionhead">
            <h2 class="title" role="heading">
                <spring:message code="register.new.portlet"/>
            </h2>
        </div> <!-- end: portlet-titlebar -->
    </div>

    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content row" role="main">

        <!-- Portlet Messages -->
        <spring:hasBindErrors name="portlet">
            <div class="col-md-6 col-md-offset-2">
                <div class="portlet-msg-error portlet-msg error alert alert-danger" role="alert">
                    <form:errors path="*" element="div" />
                </div> <!-- end: portlet-msg -->
            </div>
        </spring:hasBindErrors>

        <form action="${queryUrl}" method="POST" class="form-horizontal" role="form">

            <!-- Title -->
            <div class="titlebar">
                <h3 class="title" role="heading"><spring:message code="summary.information"/></h3>
            </div>
            <!-- Application Dropdown -->
            <div class="form-group">
                <div class=" col-md-3">
                    <select name="application" class="form-control">
                        <c:forEach items="${contexts}" var="context">
                            <option value="${fn:escapeXml(context.name)}">${fn:escapeXml(context.name)}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <!-- Portlet Dropdown -->
            <div class="form-group">
                <div class="col-md-3">
                    <select name="portlet" class="form-control col-md-4">
                        <c:forEach items="${contexts[0].portlets}" var="portlet">
                            <option value="${fn:escapeXml(portlet.portletName)}
                            ">${fn:escapeXml(fn:length(portlet.displayNames) > 0 ? portlet.displayNames[0].displayName : portlet.portletName)}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <!-- Buttons -->
            <div class="form-group">
                <div class="buttons">
                    <c:choose>
                        <c:when test="${ completed }">
                            <input class="button btn btn-primary" type="submit" value="<spring:message code="review"/>" name="_eventId_review"/>
                        </c:when>
                        <c:otherwise>
                            <input class="button btn btn-primary" type="submit" value="<spring:message code="continue"/>" name="_eventId_next"/>
                            <input class="button btn" type="submit" value="<spring:message code="back"/>" name="_eventId_back"/>
                        </c:otherwise>
                    </c:choose>
                    <input class="button btn btn-link" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
                </div>
            </div>

        </form> <!-- End Form -->

    </div> <!-- end: portlet-content -->
    
    <script type="text/javascript">
	    up.jQuery(function() {
	        var $ = up.jQuery;
	        var portlets = {};
	        <c:forEach items="${contexts}" var="context">
	            portlets['${context.name}'] = [<c:forEach items="${context.portlets}" var="portlet" varStatus="status">{name: '${portlet.portletName}', title: '${fn:length(portlet.displayNames) > 0 ? fn:replace(portlet.displayNames[0].displayName, "'", "\\'") : fn:replace(portlet.portletName, "'", "\\'")}'}${status.last ? '' : ','}</c:forEach>];
		</c:forEach>
	        $(document).ready(function(){
	            $("select[name=application]").change(function(){
	                var select = $("select[name=portlet]").html("");
	                var p = portlets[$(this).val()];
	                $(p).each(function(i){
                        select.get(0).options[i] = new Option(this.title, this.name);
	                });
	            });
	        });
        });
    </script>
        
</div> <!-- end: portlet -->
