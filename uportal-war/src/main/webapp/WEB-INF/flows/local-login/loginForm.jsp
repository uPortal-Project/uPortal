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
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<portlet:renderURL var="formUrl"/>
<c:set var="n"><portlet:namespace/></c:set>

<c:url var="loginUrl" value="/Login"/>
<portlet:renderURL var="forgotPasswordUrl" windowState="maximized">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="forgotPassword"/>
</portlet:renderURL>

<!-- Portlet -->
<div class="fl-widget portlet portlet-content" role="section" data-role="content">

  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">

    <!-- Portlet Section -->
    <div id="${n}search" class="portlet-section" role="region">

      <div class="portlet-section-body container-fluid">

        <c:if test="${ not empty attemptedUsername }">
            <!-- Portlet Message -->
            <div class="portlet-msg-error portlet-msg error" role="status">
              <div class="titlebar">
                <h3 class="title"><spring:message code="unable.to.log.in"/> . . .</h3>
              </div>
              <div class="content">
                <p>
                    <spring:message code="invalid.username.or.password"/>
                </p>
              </div>
            </div>
        </c:if>

        <form class="form-horizontal" role="form" action="${ loginUrl }" method="POST">
          <div class="form-group">
            <label class="control-label col-sm-4" for="${n}userName"><spring:message code="username"/></label>
            <div class="col-sm-8">
              <input class="xform-control" type="text" id="${n}userName" name="userName" value="${ attemptedUsername }"/>
            </div>
           </div>

          <div class="form-group">
            <label class="control-label col-sm-4" for="${n}password"><spring:message code="password"/></label>
            <div class="col-sm-8">
              <input class="xform-control" type="password" id="${n}password" name="password"/>
            </div>
          </div>

          <div class="form-group">
            <label class="control-label col-sm-4" for="${n}profile"><spring:message code="profile"/></label>
            <div class="col-sm-8">
              <select class="xform-control" id="${n}profile" name="profile">
                <option value="desktop" ${ profile == 'desktop' ? 'selected=selected' : '' }>Desktop</option>
                <option value="mobile" ${ profile == 'mobile' ? 'selected=selected' : '' }>Mobile</option>
                <option value="respondr" ${ profile == 'respondr' ? 'selected=selected' : '' }>Responsive</option>
              </select>
            </div>
          </div>

          <div class="buttons utilities form-group">
              <div class="col-sm-offset-4 col-sm-8">
                <input type="submit" value="<spring:message code="login"/>" class="primary button btn"/>
              </div>
          </div>

          <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
              <a href="${ forgotPasswordUrl }"><spring:message code="forgot.your.username.or.password"/></a>
            </div>
          </div>
        </form>

      </div>

    </div>

  </div>

</div>