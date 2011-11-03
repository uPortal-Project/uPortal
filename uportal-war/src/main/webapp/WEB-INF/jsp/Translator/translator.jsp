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
<portlet:resourceURL var="resourceUrl" />
<c:set var="ns"><portlet:namespace /></c:set>

<div class="fl-widget portlet" id="${ns}container">
  <form id="${ns}form" class="portlet-form" style="position: relative;">
    <c:set var="entityTypeSelection">
      <select id="${ns}entityType">
        <option value="portlet"><spring:message code="portlet" /></option>
        <option value="message"><spring:message code="message" /></option>
      </select>
    </c:set>
    <c:set var="localeSelection">
      <select id="${ns}locale">
          <c:forEach items="${ locales }" var="locale">
             <option value="${ fn:escapeXml(locale.code) }">${ fn:escapeXml(locale.displayLanguage) }</option>
          </c:forEach>
      </select>
    </c:set>
    <spring:message code="translate.x.into.y" arguments="${entityTypeSelection},${localeSelection}" htmlEscape="false" />
  </form>
  
  <div class="uptrans-form-container-wrapper">
    <div class="uptrans-form-container" id="${ns}formContainer">
      <!-- Portlet entity translation form -->
      <form action="${resourceUrl}" id="${ns}portletForm">
        <h3>
          <spring:message code="translating.portlet.definition" />: <span class="uptrans-pfrm-orig-title"></span>
        </h3>
      
        <div class="portlet-form">
          <fieldset>
            <div><label><spring:message code="title" />:</label> <span class="uptrans-pfrm-orig-title"></span></div>
            <div><input type="text" name="title" class="uptrans-pfrm-loc-title" /></div>
          </fieldset>
          
          <fieldset>
            <div><label><spring:message code="name" />:</label> <span class="uptrans-pfrm-orig-name"></span></div>
            <div><input type="text" name="name" class="uptrans-pfrm-loc-name" /></div>
          </fieldset>
          
          <fieldset>
            <div><label><spring:message code="description" />:</label> <span class="uptrans-pfrm-orig-descr"></span></div>
            <textarea name="description" class="uptrans-pfrm-loc-descr"></textarea>
          </fieldset>
          
          <div class="buttons">
            <input type="submit" value="<spring:message code="submit" />" />
          </div>          
        </div>
        <input type="hidden" name="id" class="uptrans-pfrm-id" /><br />
        <input type="hidden" name="locale" class="uptrans-pfrm-loc" /><br />
        <input type="hidden" name="entity" value="portlet" /><br />
        <input type="hidden" name="action" value="postTranslation" />
      </form>

      <!-- Message translation form -->
      <form action="${resourceUrl}" id="${ns}messageForm" >
        <h3>
          <spring:message code="translating.message" />: <span class="uptrans-mfrm-code"></span>
        </h3>
              
        <div class="portlet-form">
          <fieldset class="uptrans-mfrm-code-fieldset">
            <div><label><spring:message code="code" />:</label></div>
            <div><input type="text" name="id" class="uptrans-mfrm-code" /></div>
          </fieldset>
          <fieldset>
            <div><label><spring:message code="value" />:</label></div>
            <textarea name="value" class="uptrans-mfrm-value"></textarea>
          </fieldset>
          <div class="buttons">          
            <input type="submit" value="<spring:message code="submit" />" />
          </div>
        </div>
        <input type="hidden" name="locale" class="uptrans-mfrm-loc" /><br />
        <input type="hidden" name="entity" value="message" /><br />
        <input type="hidden" name="action" value="postTranslation" />
        <input type="hidden" name="type" value="modify" />
      </form>
      
      <div class="portlet-msg-success" style="display: none;"></div>
    </div>
  </div>
  
  <div class="uptrans-entities-wrapper">
    <div id="${ns}entities" class="uptrans-entities">
      <div id="${ns}entityList" class="uptrans-entity"></div>
    </div>
    <div class="ui-helper-clearfix">
      <button id="${ns}addMessage" style="display:none;"><spring:message code="add.message" /></button>
    </div>
  </div>
  <div class="ui-helper-clearfix"></div>
  
  <script type="text/javascript"><rs:compressJs>
  up.jQuery(function($) {
      up.TranslatorPortlet("#${ns}container",  {
          namespace: "<portlet:namespace />",
          resourceUrl: '<portlet:resourceURL />',
          selectors: {
              entityForm: '#${ns}form',
              locale: '#${ns}locale',
              entityType: '#${ns}entityType',
              entity: '.${ns}entity',
              entities: '#${ns}entities',
              entityList: '#${ns}entityList',
              portletForm: '#${ns}portletForm',
              messageForm: '#${ns}messageForm',
              formContainer: '#${ns}formContainer',
              addMessage: '#${ns}addMessage'
          },
          messages: {
              messageTranslationSaved: '<spring:message code="message.translation.has.been.succesfully.saved" />',
              portletTranslationSaved: '<spring:message code="portlet.definition.translation.has.been.succesfully.saved" />'
          }
      });
  });
  </rs:compressJs></script>
</div>
