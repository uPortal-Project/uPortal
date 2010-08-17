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

/**
 * The SkinSelector component houses the uPortal skin selection functionality and works
 * with a jQuery UI Dialog configured in the initializeSkinMenu() function, which lives
 * in the ajax-preferences-jquery.js file. A list of available skins is obtained through
 * an ajax GET request, which retrieves the skinList.xml file. The skinList.xml file is
 * parsed and a list of skins is dynamically generated and added to the associated
 * jQuery UI Dialog.
 * 
 * -------------------
 * Available selectors
 * -------------------
 * 
 * skinList
 *         :Reference to empty DOM container found within the associated jQuery UI Dialog.
 *          In particular, the skinList selector references the .skin-list DOM container and
 *          eventually houses the list of available skins dynamically created from parsing the
 *          skinList.xml file. The SkinSelector dialog implementation can be found within the
 *          preferences.xsl file.
 *         
 * loader
 *         :Reference to DOM container acting as a loading screen. In particular, the loader
 *          selector references the .loader DOM container found witin SkinSelector dialog
 *          implementation within the preferences.xsl file.
 *         
 * ----------------
 * Available events
 * ----------------
 * 
 * onSelectSkin 
 *         :Fired when a user "clicks" the choose submission button on the SkinSelector jQuery UI Dialog.
 *         
 * ---------------------------
 * Other Configuration Options
 * ---------------------------
 * 
 * settings
 *         :Reference to the settings object passed into the $.uportal.UportalLayoutManager object.
 *         
 */

"use strict";
var up = up || {};

(function ($, fluid) {
    
    /**
     * SkinSelector creator function for the SkinSelector component.
     * 
     * @param {Object} container - reference to HTML DOM element by ID.
     * @param {Object} options - reference to object containing all configurations.
     */
    up.SkinSelector = function (container, options) {
        var that, selectSkin, buildSkinListTempate, parseSkinListXML, initUI, initialize;
        that = fluid.initView("up.SkinSelector", container, options);
        
        /**
         * The open() function is passed a jQuery dialog
         * instance. The open() method is then invoked on the 
         * passed instance.
         * 
         * @param {Object} instance - jQuery dialog instance.
         */
        that.open = function (instance) {
            instance.dialog("open");
        };//end:function.
        
        /**
         * Captures the skin selected when skin selector form is submitted.
         * Also, fires the onSelectSkin() function and passes the skin selected
         * to any function listening for the onSelectSkin event.
         */
        selectSkin = function () {
            var selected, skinList;
            
            // Capture selected skin.
            skinList = that.locate("skinList");
            selected = skinList.find("input:checked").val();
            if (selected === undefined || selected === "") {
                return false;
            }
            
            // Fire onSelectSkin event.
            that.events.onSelectSkin.fire(selected);
        };//end:function.
        
        /**
         * Builds mark-up template for the SkinSelector component.
         * Returns a mark-up snippet.
         * 
         * @param {String} key - references the value of the <skin-key> node, found within the skinList.xml file.
         * @param {String} name - references the value of the <skin-name> node, found within the skinList.xml file.
         * @param {String} description - references the value of the <skin-description> node, found within the skinList.xml file.
         * @param {String} imagePath - references the media path to the skin's thumbnail image.
         */
        buildSkinListTempate = function (key, name, description, imagePath) {
            var template;
            
            template = 
                '<div class="fl-widget">' + 
                    '<div class="fl-widget-titlebar fl-fix">' + 
                        '<input type="radio" name="skinChoice" value="' + key + '" class="fl-force-left">' + 
                        '<h2 class="fl-force-left">' + name + '</h2>' + 
                    '</div>' + 
                    '<div class="fl-widget-content">' + 
                        '<p>' + description + '</p>' +
                        '<img src="' + imagePath + '">' + 
                    '</div>' + 
                '</div>';
                
            return template;
        };//end:function.
        
        /**
         * Parses the skinList.xml file, which is located in following directory: trunk/uportal-war/src/main/webapp/media/skins/universality
         * Once parsed, this function extracts meta data for each <skin> defined in the skinList.xml file and constructs mark-up through the
         * buildSkinListTempate() function. Once all mark-up has been built, it is added to the .skin-list DOM container, housed within the 
         * .skin-selector-dialog container.
         */
        parseSkinListXML = function () {
            var settings, skinURL;
            
            settings = that.options.settings;
            skinURL = (settings.mediaPath + "/skinList.xml?noCache=" + new Date().getTime());
            
            // Obtain skinList.xml.
            $.ajax({
                url: skinURL,
                async: true,
                dataType: "xml",
                type: "GET",
                success: function (xml) {
                    var root, skinNodes, template, skinList, loader;
                    
                    root = $(xml);
                    skinNodes = root.find("skin");
                    skinList = that.locate("skinList");
                    loader = that.locate("loader");
                    template = "";
                    
                    // Parse skinList.xml & construct ui.
                    $.each(skinNodes, function (idx, obj) {
                        var skin, key, name, description, imagePath;
                        
                        skin = $(obj);
                        key = skin.children("skin-key").text();
                        name = skin.children("skin-name").text();
                        description = skin.children("skin-description").text();
                        imagePath = (settings.mediaPath + "/" + key + "/" + "thumb.gif");
                        
                        // Build template.
                        template += buildSkinListTempate(key, name, description, imagePath);
                    });//end:loop.
                    
                    // Add to DOM.
                    skinList.html(template);
                    
                    // Set current skin.
                    skinList.find('input[value=' + settings.currentSkin + ']').attr("checked", "checked");
                    
                    // Remove loading screen.
                    up.hideLoader(loader);
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    if (console) {
                        console.log("AJAX Failure: ", XMLHttpRequest, textStatus, errorThrown);
                    }
                }
            });
        };//end:function.
        
        /**
         * Prepares UI for SkinSelector component. This function clears
         * the .skin-list DOM container, calls the parseSkinListXML() function
         * and registers the submit event for the SkinSelector form.
         */
        initUI = function () {
            var skinList, form;
            
            // Clear skinList.
            skinList = that.locate("skinList");
            skinList.html("");
            
            // Parse skinList.xml
            parseSkinListXML();
            
            // Mange form submit.
            form = that.container.find("form");
            form.submit(function () {
                selectSkin();
                return false;
            });
        };//end:function.
        
        /**
         * Private. Entry point for the SkinSelector component.
         */
        initialize = function () {
            initUI();
        };//end:function.
        
        initialize();
        return that;
    };//end:component.
    
    /**
     * SkinSelector
     * Defaults function for the SkinSelector component.
     ---------------------------------*/
    fluid.defaults("up.SkinSelector", {
        selectors: {
            skinList: ".skin-list",
            loader: ".loader"
        },
        events: {
            onSelectSkin: null
        },
        settings: null
    });
})(jQuery, fluid);