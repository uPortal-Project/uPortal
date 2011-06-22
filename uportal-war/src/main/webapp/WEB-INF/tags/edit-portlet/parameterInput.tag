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

<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%@ tag dynamic-attributes="attributes" isELIgnored="false" %>
<%@ attribute name="input"   required="true" type="org.jasig.portal.portletpublishing.xml.ParameterInputType" %>
<%@ attribute name="path"    required="true" %>
<%@ attribute name="name"    required="false" %>

<c:choose>

  <c:when test="${ up:instanceOf(input, 'org.jasig.portal.portletpublishing.xml.SingleTextParameterInput') }">
  <!-- Single-value text input types -->
    <c:choose>
      <c:when test="${ input.display == 'TEXTAREA' }">
        <!-- Textarea -->
        <form:textarea path="${ path }"/>
      </c:when>
      <c:otherwise>
        <!-- Text input -->
        <form:input path="${ path }" />
      </c:otherwise>
    </c:choose>
  </c:when>
  
  <c:when test="${ up:instanceOf(input, 'org.jasig.portal.portletpublishing.xml.SingleChoiceParameterInput') }">
  <!-- Single-value choice input types -->
    <c:choose>
      <c:when test="${ input.display == 'RADIO' }">
      <!-- Radio buttons -->
        <form:radiobuttons path="${ path }" items="${ input.options }" itemLabel="label" itemValue="value" delimiter=" "/>
      </c:when>
      <c:otherwise>
      <!-- Select menu -->
        <form:select path="${ path }" multiple="false" items="${ input.options }" itemLabel="label" itemValue="value"/>
      </c:otherwise>
    </c:choose>
  </c:when>
  
</c:choose>
