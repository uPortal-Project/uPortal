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
<%@ attribute name="parameterType"   required="true" type="org.jasig.portal.portlets.portletadmin.xmlsupport.ICPDOptionType" %>
<%@ attribute name="parameterPath"   required="true" %>
<%@ attribute name="parameterName" required="false" %>
<%@ attribute name="parameterValues" required="false" type="java.util.Collection" %>

<c:choose>
  <c:when test="${ parameterType.input == 'multi-text' }">
    <c:forEach items="${ parameterValues }" var="val">
      <div>
         <input name="${ fn:escapeXml(parameterPath )}" value="${ fn:escapeXml(val )}" 
            size="${ parameterType.length != '' ? parameterType.length : defaultLength }"
            maxlength="${ parameterType.maxlength != '' ? parameterType.maxlength : defaultMaxLength }"/>
         <a class="delete-parameter-value-link" href="javascript:;">Remove</a>
      </div>
    </c:forEach>
    <a class="add-parameter-value-link" href="javascript:;" paramName="${fn:escapeXml(parameterName)}">Add value</a>
  </c:when>

  <c:when test="${ parameterType.input == 'text' }">
  <!-- Single-value text input types -->
    <c:choose>
      <c:when test="${ parameterType.display == 'textarea' }">
      <!-- Textarea -->
        <c:choose>
            <c:when test="${ parameterValues != null }">
                <textarea>${ fn:escapeXml(fn:length(parameterValues) > 0 ? parameterValues[0] : '' )}</textarea>
            </c:when>
            <c:otherwise>
                <form:textarea path="${parameterPath}"/>
            </c:otherwise>
        </c:choose>
      </c:when>
      <c:otherwise>
      <!-- Text input -->
        <c:choose>
            <c:when test="${ parameterValues != null }">
                <input name="${fn:escapeXml(parameterPath)}" value="${ fn:escapeXml(fn:length(parameterValues) > 0 ? parameterValues[0] : '' )}" 
                    size="${ parameterType.length != '' ? parameterType.length : defaultLength }" 
                    maxlength="${ parameterType.maxlength != '' ? parameterType.maxlength : defaultMaxLength }"/>
            </c:when>
            <c:otherwise>
		        <form:input path="${parameterPath}" size="${ parameterType.length != '' ? parameterType.length : defaultLength }" 
		            maxlength="${ parameterType.maxlength != '' ? parameterType.maxlength : defaultMaxLength }"/>
            </c:otherwise>
        </c:choose>
      </c:otherwise>
    </c:choose>
  </c:when>
  
  <c:when test="${ parameterType.input == 'single-choice' }">
  <!-- Single-value choice input types -->
    <c:choose>
      <c:when test="${ parameterType.display == 'radio' }">
      <!-- Radio buttons -->
        <form:radiobuttons path="${parameterPath}" items="${ parameterType.restriction.values }" itemLabel="value" itemValue="value" delimiter=" "/>
      </c:when>
      <c:otherwise>
      <!-- Select menu -->
        <form:select path="${parameterPath}" multiple="false" items="${ parameterType.restriction.values }" itemValue="value" itemLabel="value"/>
      </c:otherwise>
    </c:choose>
  </c:when>
  
  <c:when test="${ parameterType.input == 'multi-choice' }">
  <!-- Multi-value choice input types -->
    <c:choose>
      <c:when test="${ parameterType.display == 'checkbox' }">
      <!-- Checkboxes -->
        <form:checkboxes path="${parameterPath}" items="${ parameterType.restriction.values }" itemLabel="value" itemValue="value" delimiter=" "/>
      </c:when>
      <c:otherwise>
      <!-- Multiple select menu -->
        <form:select path="${parameterPath}" multiple="true">
          <c:forEach items="${ parameterType.restriction.values }" var="value">
            <form:option value="${ value.value }" label="${ value.value }" />
          </c:forEach>
        </form:select>
      </c:otherwise>
    </c:choose>
  </c:when>
  
</c:choose>
