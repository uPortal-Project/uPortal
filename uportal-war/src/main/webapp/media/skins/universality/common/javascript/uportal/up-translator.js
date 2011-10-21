/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

"use strict";
var up = up || {};

(function($, fluid) {
    
    up.TranslatorPortlet = function(container, options) {
        var that = fluid.initView("up.TranslatorPortlet", container, options);

        // this will hold the entity template in order to enable later reloading
        var entityTemplate = null;

        /**
         * Shows the form of new message (message code will be fillable field).
         */
        var addMessageHandler = function() {
            that.options.entity = null;
            var data = [
                    { ID: "code", value: "" }, 
                    { ID: "locale", value: that.options.locale }, 
                    { ID: "value", value: "" }
            ];
            var selectorMap = [
                    { selector: '.uptrans-mfrm-code', id: "code" },
                    { selector: '.uptrans-mfrm-loc', id: "locale" },
                    { selector: '.uptrans-mfrm-value', id: "value" }
            ];
            var $form = that.locate("messageForm");
            $form.find(".uptrans-mfrm-code-fieldset").show();
            fluid.selfRender($form.show(), data, { cutpoints: selectorMap });
            that.locate("portletForm").hide();
            that.locate("formContainer").show();
        };

        /**
         * Fills the message form with currently available message of specified
         * locale (loaded via AJAX call).
         */
        var fillMessageForm = function() {
            $.ajax({
                url: that.options.resourceUrl,
                dataType: "json",
                data: {
                    action: 'getEntity',
                    id: that.options.entity.id,
                    locale: that.options.locale,
                    entity: that.options.entityType 
                },
                success: function(json) {
                    var message = json.message;
                    // if there was no message, then initialize with empty values
                    if (message == null) {
                        message = { code: that.options.entity.id, locale: that.options.locale, value: "" };
                    }
                    var data = [
                            { ID: "code", value: message.code },
                            { ID: "locale", value: message.locale },
                            { ID: "value", value: message.value }
                    ];

                    var selectorMap = [
                            { selector: '.uptrans-mfrm-code', id: "code" },
                            { selector: '.uptrans-mfrm-loc', id: "locale" },
                            { selector: '.uptrans-mfrm-value', id: "value" }
                    ];

                    that.locate("portletForm").hide();
                    that.locate("formContainer").show();
                    var $form = that.locate("messageForm");
                    $form.find(".uptrans-mfrm-code-fieldset").hide();
                    fluid.selfRender($form.show(), data, { cutpoints: selectorMap });
                }
            });
        };

        /**
         * Posts the form contents to the resource URL using AJAX call.
         */
        var portletFormSubmitHandler = function() {
            var $form = $(this);
            $.ajax({
                url: $form.attr("action"), 
                type: "POST", 
                data: $form.serialize(), 
                success: function() {
                    var $msg = that.locate("formContainer").find(".portlet-msg-success");
                    $msg.html(that.options.messages.portletTranslationSaved);
                    $msg.show();
                    $msg.fadeOut(4000);
                }
            });
            return false;
        };

        /**
         * Fills the portlet form with currently available portlet definition of
         * specified locale (loaded via AJAX call).
         */
        var fillPortletForm = function() {
            $.ajax({
                url: that.options.resourceUrl,
                dataType: "json",
                data: {
                    action: 'getEntity',
                    id: that.options.entity.id,
                    locale: that.options.locale,
                    entity: that.options.entityType 
                },
                success: function(json) {
                    var portlet = json.portlet;
                    var data = [
                            { ID: "id", value: portlet.id },
                            { ID: "locale", value: portlet.locale },
                            { ID: "original.title", value: portlet.original.title },
                            { ID: "localized.title", value: portlet.localized.title },
                            { ID: "original.name", value: portlet.original.name },
                            { ID: "localized.name", value: portlet.localized.name },
                            { ID: "original.description", value: portlet.original.description },
                            { ID: "localized.description", value: portlet.localized.description },
                    ];

                    var selectorMap = [
                            { selector: '.uptrans-pfrm-id', id: "id" },
                            { selector: '.uptrans-pfrm-loc', id: "locale" },
                            { selector: '.uptrans-pfrm-loc-name', id: "localized.name" },
                            { selector: '.uptrans-pfrm-loc-title', id: "localized.title" },
                            { selector: '.uptrans-pfrm-loc-descr', id: "localized.description" },
                            { selector: '.uptrans-pfrm-orig-title', id: "original.title" },
                            { selector: '.uptrans-pfrm-orig-name', id: "original.name" },
                            { selector: '.uptrans-pfrm-orig-descr', id: "original.description" }
                    ];

                    that.locate("messageForm").hide();
                    that.locate("formContainer").show();
                    fluid.selfRender(that.locate("portletForm").show(), data, { cutpoints: selectorMap });
                } 
            });
        };
        
        /**
         * Updates form contents depending on selected entity type and locale.
         */
        var updateForm = function() {
            // if no entity is selected, then there's no need to show the form
            if (that.options.entity) {
                if (that.options.entityType == "portlet") {
                    fillPortletForm();
                } else if (that.options.entityType == "message") {
                    fillMessageForm();
                }
            }
        };

        /**
         * Reload translateable entity list.
         */
        var refreshEntities = function(data) {
            that.locate("formContainer").hide();
            if (that.options.entityType == 'message') {
                that.locate("addMessage").show();
            } else {
                that.locate("addMessage").hide();
            }
            var entities = [];
            $(data.entities.sort(up.getStringPropertySortFunction("title"))).each(function(idx, entity) {
                entities.push({ 
                    ID: that.options.namespace + "data:", 
                    value: entity.title, 
                    decorators: [{
                        type: "jQuery", 
                        func: "click", 
                        args: function() {
                            that.options.entity = entity;
                            updateForm();
                        } 
                    }]
                });
            });

            var selectorMap = [
                    { selector: that.options.selectors.entityList, id: that.options.namespace + "data:" },
                    { selector: that.options.selectors.entity, id: "name" }
            ];
            if (entityTemplate == null) {
                entityTemplate = fluid.selfRender(that.locate("entities"), entities, { cutpoints: selectorMap });
            } else {
                fluid.reRender(entityTemplate, that.locate("entities"), entities, { cutpoints: selectorMap });
            }
        };

        /**
         * Load entities to be translated in specified locale using AJAX.
         */
        var entityTypeSelectionChangedHandler = function() {
            var entity = that.locate("entityType").val();
            that.options.entityType = that.locate("entityType").val();
            that.options.entity = null;

            if (entity && entity != '') {
                // get entities from login
                $.ajax({
                    url: that.options.resourceUrl,
                    dataType: "json",
                    data: { entity: entity, action: 'getEntityList' },
                    success: refreshEntities 
                });
            }
        };

        /**
         * Submits a message form to a resource URL using AJAX.
         */
        var messageFormSubmitHandler = function() {
            var $form = $(this);
            $.ajax({ 
                url: $form.attr("action"), 
                type: "POST", 
                data: $form.serialize(), 
                success: function(data) {
                    // if this is a new key (selected entity was null), then we must reload entity list
                    if (that.options.entity == null) {
                        entityTypeSelectionChangedHandler();
                    }
                    var $msg = that.locate("formContainer").find(".portlet-msg-success");
                    $msg.html(that.options.messages.messageTranslationSaved);
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
        that.locate("entityForm").submit(function() {
            return false;
        });
        that.locate("addMessage").click(addMessageHandler);

        var localeSelector = that.locate("locale");
        localeSelector.change(function() {
            that.options.locale = localeSelector.val();
            updateForm();
        });

        // init portlet state - load entity list of default selection and set
        // the locale to default selection
        that.options.locale = localeSelector.val();
        entityTypeSelectionChangedHandler();

        return that;
    };

    fluid.defaults("up.TranslatorPortlet", {
        namespace: "uptrans",
        locale: null,
        entityType: null,
        entity: null,
        resourceUrl: '',
        selectors: {
            entityForm: '#uptrans-form',
            locale: '#uptrans-locale',
            entityType: '#uptrans-entityType',
            entity: '.uptrans-entity',
            entities: '#uptrans-entities',
            entityList: '#uptrans-entityList',
            portletForm: '#uptrans-portletForm',
            messageForm: '#uptrans-messageForm',
            formContainer: '#uptrans-formContainer',
            addMessage: '#uptrans-addMessage' },
        messages: {
            messageTranslationSaved: 'Message translation has been succesfully saved',
            portletTranslationSaved: 'Portlet definition translation has been successfully saved'
        }
    });
})(jQuery, fluid);