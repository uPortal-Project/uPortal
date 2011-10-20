<%@ include file="/WEB-INF/jsp/include.jsp"%>
<portlet:resourceURL var="resourceUrl" />
<c:set var="ns"><portlet:namespace /></c:set>

<style>
.uptrans-entities-wrapper {float: left; margin-right: -200px; width: 200px;}
.uptrans-entities-wrapper button {width: 100%;}
.uptrans-entities {max-height: 400px; overflow: auto;}
.uptrans-form-container {margin-left: 220px;}
.uptrans-entity {padding: 5px; font-size: 107%; cursor: pointer;}
.uptrans-entity:HOVER {background-color: orange!important;}
.uptrans-form-container-wrapper {float: right; width: 100%;}
.uptrans-form-container {display: none; margin-top: 15px;}
.uptrans-form-container form fieldset {padding: 10px;}
.uptrans-form-container form input[type="text"] {width: 100%;}
.uptrans-form-container form .buttons {text-align: right;}
.uptrans-form-container form textarea {width: 100%; height: 50px;}
#${ns}entityList {display: none;}
</style>

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
          <fieldset id="${ns}code">
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
  
  <script type="text/javascript"><rs:compressJs>
  up.jQuery(function($) {
      var fluid = up.fluid;

      up.TranslatorPortlet = function(container, options) {
          var that = fluid.initView("up.TranslatorPortlet", container, options);
          
          // this will hold the entity template in order to enable later reloading 
          var entityTemplate = null;
          
          /** Shows the form of new message (message code will be fillable field). */
          var addMessageHandler = function() {
              that.options.entity = null;
              var data = [
                  {ID: "code", value: ""},
                  {ID: "locale", value: that.options.locale},
                  {ID: "value", value: ""}
              ];
              var selectorMap = [
                  {selector: '.uptrans-mfrm-code', id: "code"},
                  {selector: '.uptrans-mfrm-loc', id: "locale"},
                  {selector: '.uptrans-mfrm-value', id: "value"}
              ];
              $form = that.locate("messageForm");
              $form.find("#${ns}code").show();
              fluid.selfRender($form.show(), data, {cutpoints: selectorMap});
              that.locate("portletForm").hide();
              that.locate("formContainer").show();
          };
          
          /** Fills the message form with currently available message of specified locale (loaded via AJAX call). */
          var fillMessageForm = function(){
              entity = that.options.entity;
              $.ajax({
                  url: that.options.resourceUrl,
                  dataType: "json",
                  data: {action: 'getEntity', id: entity.id, locale: that.options.locale, entity: that.options.entityType},
                  success: function(json) {
                      var message = json.message;
                      // if there was no message, then initialzie  with empty values
                      if (message == null) message = {code: entity.id, locale: that.options.locale, value: ""};
                      var data = [
                          {ID: "code", value: message.code},
                          {ID: "locale", value: message.locale},
                          {ID: "value", value: message.value}
                      ];
                      
                      var selectorMap = [
                          {selector: '.uptrans-mfrm-code', id: "code"},
                          {selector: '.uptrans-mfrm-loc', id: "locale"},
                          {selector: '.uptrans-mfrm-value', id: "value"}
                      ];
                      
                      that.locate("portletForm").hide();
                      that.locate("formContainer").show();
                      $form = that.locate("messageForm");
                      $form.find("#${ns}code").hide();
                      fluid.selfRender($form.show(), data, {cutpoints: selectorMap});
                  }
              });
          };

          /** Posts the form contents to the resource URL using AJAX call. */
          var portletFormSubmitHandler = function() {
              $form = $(this);
              $.ajax({
                  url: $form.attr("action"),
                  type: "POST",
                  data: $form.serialize(),
                  success: function() {
                      $msg = that.locate("successMessage");
                      $msg.html('<spring:message code="portlet.definition.translation.has.been.succesfully.saved" />');
                      $msg.show();
                      $msg.fadeOut(4000);
                  }
              });
              return false;
          };
          
          /** Fills the portlet form with currently available portlet definition of specified locale (loaded via AJAX call). */
          var fillPortletForm = function(){
              entity = that.options.entity;
              $.ajax({
                  url: that.options.resourceUrl,
                  dataType: "json",
                  data: {action: 'getEntity', id: entity.id, locale: that.options.locale, entity: that.options.entityType},
                  success: function(json) {
                      var portlet = json.portlet;
                      var data = [
                          {ID: "id", value: portlet.id},
                          {ID: "locale", value: portlet.locale},
                          {ID: "original.title", value: portlet.original.title},
                          {ID: "localized.title", value: portlet.localized.title},
                          {ID: "original.name", value: portlet.original.name},
                          {ID: "localized.name", value: portlet.localized.name},
                          {ID: "original.description", value: portlet.original.description},
                          {ID: "localized.description", value: portlet.localized.description},
                      ];
                      
                      var selectorMap = [
                          {selector: '.uptrans-pfrm-id', id: "id"},
                          {selector: '.uptrans-pfrm-loc', id: "locale"},
                          {selector: '.uptrans-pfrm-loc-name', id: "localized.name"},
                          {selector: '.uptrans-pfrm-loc-title', id: "localized.title"},
                          {selector: '.uptrans-pfrm-loc-descr', id: "localized.description"},
                          {selector: '.uptrans-pfrm-orig-title', id: "original.title"},
                          {selector: '.uptrans-pfrm-orig-name', id: "original.name"},
                          {selector: '.uptrans-pfrm-orig-descr', id: "original.description"}
                      ];
                      
                      that.locate("messageForm").hide();
                      that.locate("formContainer").show();
                      fluid.selfRender(that.locate("portletForm").show(), data, {cutpoints: selectorMap});
                  }
              });
          };
          
          /**  Updates form contents depending on selected entity type and locale. */
          var updateForm = function(){
              if (that.options.entityType == "portlet") {
                  fillPortletForm();
              } else if (that.options.entityType == "message"){
                  fillMessageForm();
              }
          };
          
          /** Reload translateable entity list. */
          var refreshEntities = function(data) {
              that.locate("formContainer").hide();
              if (that.options.entityType == 'message') {
                  that.locate("addMessage").show();
              } else {
                  that.locate("addMessage").hide();
              }
              var entities = [];
              $(data.entities.sort(up.getStringPropertySortFunction("title"))).each(function(idx, entity){
                  entities.push({
                      ID: "${ns}data:",
                      value: entity.title,
                      decorators: [{
                          type: "jQuery", 
                          func: "click",
                          args: function(){
                              that.options.entity = entity;
                              updateForm();
                          }
                      }]
                  });
              });
              
              var selectorMap = [
                  {selector: "#${ns}entityList", id: "${ns}data:"},
                  {selector: ".${ns}entity", id: "name"}
              ];
              if (entityTemplate == null) {
                  entityTemplate = fluid.selfRender($("#${ns}entities"), entities, {cutpoints: selectorMap});
              } else {
                  fluid.reRender(entityTemplate, $("#${ns}entities"), entities, {cutpoints: selectorMap});
              }
          };
          
          /** Load entities to be translated in specified locale using AJAX. */
          var entityTypeSelectionChangedHandler = function() {
              var entity = that.locate("entityType").val();
              that.options.entityType = that.locate("entityType").val();
              that.options.entity = null;
    
              if (entity && entity != ''){
                // get entities from login
                $.ajax({
                    url: that.options.resourceUrl,
                    dataType: "json",
                    data: {entity: entity, action: 'getEntityList'},
                    success: refreshEntities
                });
              }
          };
          
          /** Submits a message form to a resource URL using AJAX. */
          var messageFormSubmitHandler = function() {
              $form = $(this);
              $.ajax({
                  url: $form.attr("action"),
                  type: "POST",
                  data: $form.serialize(),
                  success: function(data) {
                      // if this is a new key (selected entity was null), then we must reload entity list
                      if (that.options.entity == null) {
                          entityTypeSelectionChangedHandler();
                      }
                      $msg = that.locate("successMessage");
                      $msg.html('<spring:message code="message.translation.has.been.succesfully.saved" />');
                      $msg.show();
                      $msg.fadeOut(4000);
                  }
              });
              return false;
          };
          
          // init event handlers 
          that.locate("entityType").change(entityTypeSelectionChangedHandler);
          that.locate("portletForm").submit(portletFormSubmitHandler);
          that.locate("messageForm").submit(messageFormSubmitHandler);
          that.locate("entityForm").submit(function(){return false;});
          that.locate("addMessage").click(addMessageHandler);
          
          var localeSelector = that.locate("locale");
          localeSelector.change(function(){
              that.options.locale = localeSelector.val();
              updateForm();
          });

          // init portlet state - load entity list of default selection and set the locale to default selection 
          that.options.locale = localeSelector.val();
          entityTypeSelectionChangedHandler();
          
          return that;
      };

      fluid.defaults("up.TranslatorPortlet", {
          locale: null,
          entityType: null,
          entity: null,
          resourceUrl: '<portlet:resourceURL />',
          selectors: {
              keyList: '',
              entityForm: '#${ns}form',
              locale: '#${ns}locale',
              entityType: '#${ns}entityType',
              portletForm: '#${ns}portletForm',
              messageForm: '#${ns}messageForm',
              formContainer: '#${ns}formContainer',
              successMessage: '#${ns}formContainer .portlet-msg-success',
              addMessage: '#${ns}addMessage'
          },
          messages: {}
      });

      // init view 
      up.TranslatorPortlet("#${ns}container",  {});
  });
  </rs:compressJs></script>
</div>
