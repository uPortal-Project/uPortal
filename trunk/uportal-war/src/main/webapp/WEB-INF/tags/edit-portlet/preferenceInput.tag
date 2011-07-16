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
<%@ attribute name="input"   required="true" type="org.jasig.portal.portletpublishing.xml.PreferenceInputType" %>
<%@ attribute name="path"    required="true" %>
<%@ attribute name="name"    required="false" %>
<%@ attribute name="values"  required="false" type="java.util.Collection" %>

<c:choose>

  <c:when test="${ up:instanceOf(input, 'org.jasig.portal.portletpublishing.xml.MultiTextPreferenceInput') }">
    <c:forEach items="${ values }" var="val">
      <div>
         <input name="${ fn:escapeXml(path )}" value="${ fn:escapeXml(val )}" />
         <a class="delete-parameter-value-link" href="javascript:;">Remove</a>
      </div>
    </c:forEach>
    <a class="add-parameter-value-link" href="javascript:;" paramName="${fn:escapeXml(name)}">Add value</a>
  </c:when>

  <c:when test="${ up:instanceOf(input, 'org.jasig.portal.portletpublishing.xml.SingleTextPreferenceInput') }">
  <!-- Single-value text input types -->
    <c:choose>
      <c:when test="${ input.display == 'TEXTAREA' }">
      <!-- Textarea -->
        <c:choose>
            <c:when test="${ values != null }">
                <textarea>${ fn:escapeXml(fn:length(values) > 0 ? values[0] : '' )}</textarea>
            </c:when>
            <c:otherwise>
                <form:textarea path="${path}"/>
            </c:otherwise>
        </c:choose>
      </c:when>
      <c:otherwise>
      <!-- Text input -->
        <c:choose>
            <c:when test="${ values != null }">
                <input name="${fn:escapeXml(path)}" value="${ fn:escapeXml(fn:length(values) > 0 ? values[0] : '' )}" />
            </c:when>
            <c:otherwise>
                <form:input path="${path}"/>
            </c:otherwise>
        </c:choose>
      </c:otherwise>
    </c:choose>
  </c:when>
  
  <c:when test="${ up:instanceOf(input, 'org.jasig.portal.portletpublishing.xml.SingleChoicePreferenceInput') }">
  <!-- Single-value choice input types -->
    <c:choose>
      <c:when test="${ input.display == 'RADIO' }">
      <!-- Radio buttons -->
        <form:radiobuttons path="${ path}" items="${ input.options }" itemLabel="label" itemValue="value" delimiter=" "/>
      </c:when>
      <c:otherwise>
      <!-- Select menu -->
        <form:select path="${ path }" multiple="false">
          <c:forEach items="${ input.options }" var="option">
            <spring:message var="label" code="${ option.label }" text="${ option.label }" />
            <form:option value="${ option.value }" label="${ label }" />
          </c:forEach>
        </form:select>
      </c:otherwise>
    </c:choose>
  </c:when>
  
  <c:when test="${ up:instanceOf(input, 'org.jasig.portal.portletpublishing.xml.MultiChoicePreferenceInput') }">
  <!-- Multi-value choice input types -->
    <c:choose>
      <c:when test="${ input.display == 'CHECKBOX' }">
      <!-- Checkboxes -->
        <form:checkboxes path="${path}" items="${ input.options }" itemLabel="label" itemValue="value" delimiter=" "/>
      </c:when>
      <c:otherwise>
      <!-- Multiple select menu -->
        <form:select path="${path}" multiple="true">
          <c:forEach items="${ input.options }" var="option">
            <spring:message var="label" code="${ option.label }" text="${ option.label }" />
            <form:option value="${ option.value }" label="${ label }" />
          </c:forEach>
        </form:select>
      </c:otherwise>
    </c:choose>
  </c:when>
  
</c:choose>
