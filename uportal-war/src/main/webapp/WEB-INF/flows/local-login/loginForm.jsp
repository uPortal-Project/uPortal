<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
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

      <div class="portlet-section-body">

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
        
        <form action="${ loginUrl }" method="POST">

            <label for="${n}userName"><spring:message code="username"/></label>
            <input type="text" id="${n}userName" name="userName" value="${ attemptedUsername }"/>
            
            <label for="${n}password"><spring:message code="password"/></label>
            <input type="password" id="${n}password" name="password"/>
        
            <label for="${n}profile"><spring:message code="profile"/></label>
            <select id="${n}profile" name="profile">
                <option value="desktop" ${ profile == 'desktop' ? 'selected=selected' : '' }>Desktop</option>
                <option value="mobile" ${ profile == 'mobile' ? 'selected=selected' : '' }>Mobile</option>
            </select>
        
            <div class="buttons utilities">
                <input type="submit" value="<spring:message code="login"/>" class="primary button"/>
            </div>
        </form>
        
        <p>
            <a href="${ forgotPasswordUrl }"><spring:message code="forgot.your.username.or.password"/></a>
        </p>

      </div>  

    </div>
    
  </div>

</div>