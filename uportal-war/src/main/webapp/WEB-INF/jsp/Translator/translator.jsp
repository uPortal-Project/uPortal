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
<portlet:resourceURL var="resourceUrl" />
<c:set var="ns"><portlet:namespace /></c:set>

<div class="fl-widget portlet" id="${ns}container">
  <form id="${ns}form" class="portlet-form" style="position: relative;">
    <c:set var="entityTypeSelection">
      <select id="${ns}entityType" class="form-control" aria-label="<spring:message code="translator.portlet.or.message" />">
        <option value="portlet">
          <spring:message code="portlet" />
        </option>
        <option value="message">
          <spring:message code="message" />
        </option>
      </select>
    </c:set>
    <c:set var="localeSelection">
      <select id="${ns}locale" class="form-control" aria-label="<spring:message code="translator.language" />">
          <c:forEach items="${ locales }" var="locale">
             <option value="${ fn:escapeXml(locale.code) }">${ fn:escapeXml(locale.displayLanguage) }</option>
          </c:forEach>
      </select>
    </c:set>
    <spring:message code="translate.x.into.y" arguments="${entityTypeSelection},${localeSelection}" htmlEscape="false" />
    <br />
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
            <legend>
              <label for="${ns}TranslationTitle">
                <spring:message code="title" />
              </label>
            </legend>
            <strong>
              <spring:message code="translator.original.message" />:
            </strong>
            <span class="uptrans-pfrm-orig-title"></span>
            <input id="${ns}TranslationTitle" type="text" name="title" class="uptrans-pfrm-loc-title form-control" />
          </fieldset>
          <br />

          <fieldset>
            <legend>
              <label for="${ns}TranslationName">
                <spring:message code="name" />
              </label>
            </legend>
            <strong>
              <spring:message code="translator.original.message" />:
            </strong>
            <span class="uptrans-pfrm-orig-name"></span>
            <input id="${ns}TranslationName" type="text" name="name" class="uptrans-pfrm-loc-name form-control" />
          </fieldset>
          <br />

          <fieldset>
            <legend>
              <label for="${ns}TranslationDescription">
                <spring:message code="description" />
              </label>
            </legend>
            <strong>
              <spring:message code="translator.original.message" />:
            </strong>
            <span class="uptrans-pfrm-orig-descr"></span>
            <textarea id="${ns}TranslationDescription" name="description" class="uptrans-pfrm-loc-descr form-control"></textarea>
          </fieldset>
          <br />

          <input class="btn btn-default" type="submit" value="<spring:message code="submit" />" />
          <br />
        </div>
        <input type="hidden" name="id" class="uptrans-pfrm-id" />
        <input type="hidden" name="locale" class="uptrans-pfrm-loc" />
        <input type="hidden" name="entity" value="portlet" />
        <input type="hidden" name="action" value="postTranslation" />
      </form>

      <!-- Message translation form -->
      <form action="${resourceUrl}" id="${ns}messageForm" >
        <h3>
          <spring:message code="translating.message" />: <span class="uptrans-mfrm-code"></span>
        </h3>

        <div class="portlet-form">
          <fieldset class="uptrans-mfrm-code-fieldset">
            <legend>
              <label for="${ns}TranslationCode">
                <spring:message code="code" />
              </label>
            </legend>
            <input id="${ns}TranslationCode" type="text" name="id" class="uptrans-mfrm-code form-control" />
          </fieldset>
          <fieldset>
            <legend>
              <label for="${ns}TranslationValue">
                <spring:message code="value" />
              </label>
            </legend>
            <textarea id="${ns}TranslationValue" name="value" class="uptrans-mfrm-value form-control"></textarea>
          </fieldset>
          <input class="btn btn-default" type="submit" value="<spring:message code="submit" />" />
        </div>
        <input type="hidden" name="locale" class="uptrans-mfrm-loc" />
        <input type="hidden" name="entity" value="message" />
        <input type="hidden" name="action" value="postTranslation" />
        <input type="hidden" name="type" value="modify" />
      </form>

      <div class="portlet-msg-success" style="display: none;"></div>
    </div>
  </div>

  <div class="uptrans-entities-wrapper">
    <div id="${ns}entities" class="uptrans-entities">
      <button class="btn btn-default" id="${ns}entityList" class="uptrans-entity"></button>
    </div>
    <div class="ui-helper-clearfix">
      <button id="${ns}addMessage" class="btn btn-default" style="display:none;">
        <spring:message code="add.message" />
      </button>
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
              messageTranslationSaved: '<spring:message code="message.translation.has.been.succesfully.saved" htmlEscape="false" javaScriptEscape="true" />',
              portletTranslationSaved: '<spring:message code="portlet.definition.translation.has.been.succesfully.saved" htmlEscape="false" javaScriptEscape="true" />'
          }
      });
  });
  </rs:compressJs></script>
</div>
