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
        var that;
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
         * Builds and returns dataModel object based upon parsed
         * skinList.xml values.
         * 
         * @param {String} key - references the value of the <skin-key> node, found within the skinList.xml file.
         * @param {String} name - references the value of the <skin-name> node, found within the skinList.xml file.
         * @param {String} description - references the value of the <skin-description> node, found within the skinList.xml file.
         * @param {String} thumbnailPath - references the media path to the skin's thumbnail image.
         */
        var buildDataModel = function (key, name, description, thumbnailPath) {
            return {
                key: key,
                name: name,
                description: description,
                thumbnailPath: thumbnailPath,
                thumbnailAlt: (name + " Thumbnail")
            };
        };//end:function.
        
        /**
         * Build the cutpoints array, which defines renderer IDs
         * for each HTML element that will be rendered.
         */
        var buildCutPoints = function () {
            return [
                {id: "listItem-row:", selector: that.options.selectors.itemList},
                {id: "skinWidget", selector: that.options.selectors.skinWidget},
                {id: "skinName", selector: that.options.selectors.skinName},
                {id: "skinKey", selector: that.options.selectors.skinKey},
                {id: "skinDescription", selector: that.options.selectors.skinDescription},
                {id: "skinThumbnail", selector: that.options.selectors.skinThumbnail}
            ];
        };//end:function.
        
        /**
         * Build skin list component subtree.
         */
        var buildSkinListTree = function () {
            var treeChildren, skinRows;
            
            treeChildren = [];
            skinRows = fluid.transform(that.model, function (obj, index) {
                return {
                    ID: "listItem-row:",
                    children: [
                        {
                            ID: "skinWidget",
                            decorators: [
                                {
                                    type: "jQuery", func: "click",
                                    args: function () {
                                        var skinList, li, active;
                                        
                                        // Remove 'skin-active' class from previous.
                                        skinList = that.locate("skinList");
                                        skinList.find("." + that.options.activeSkin).removeClass(that.options.activeSkin);
                                        
                                        // Apply 'skin-active' class to current.
                                        li = $(this);
                                        li.addClass(that.options.activeSkin);
                                        
                                        // Capture value of hidden field.
                                        active = li.find('input[type="hidden"]').val();
                                        
                                        // Fire onSelectSkin event.
                                        that.events.onSelectSkin.fire(active);
                                    }
                                }
                            ]
                        },
                        {
                            ID: "skinName", value: obj.name,
                            decorators: [
                                {
                                    type: "attrs",
                                    attributes: {
                                        title: obj.name
                                    }
                                }
                            ]
                        },
                        {
                            ID: "skinKey", value: obj.key,
                            decorators: [
                                {
                                    type: "attrs",
                                    attributes: {
                                        value: obj.key
                                    }
                                }
                            ]
                        },
                        {
                            ID: "skinDescription", value: obj.description
                        },
                        {
                            ID: "skinThumbnail", 
                            decorators: [
                                {
                                    type: "attrs",
                                    attributes: {
                                        src: obj.thumbnailPath,
                                        alt: obj.thumbnailAlt
                                    }
                                }
                            ]
                        }
                    ]
                };
            });
            return treeChildren.concat(skinRows);
        };//end:function.
        
        /**
         * Builds component tree utilized by the fluid.renderer component.
         */
        var buildComponentTree = function () {
            return {
                children: buildSkinListTree()
            };
        };//end:function.
        
        /**
         * Configues & executes the fluid.renderer component.
         */
        var doRender = function () {
            var skinList, loader, current, options;
            
            skinList = that.locate("skinList");
            loader = that.locate("loader");
            options = {
                cutpoints: buildCutPoints(),
                model: that.model,
                autoBind: true
            };
            
            // Run renderer.
            that.renderer = fluid.selfRender(skinList, buildComponentTree(), options);
            
            // Highlight active skin.
            skinList.find('input[value="' + that.settings.currentSkin + '"]').parents(".widget").addClass(that.options.activeSkin);
            
            // Remove loading screen.
            up.hideLoader(loader);
        };//end:function.
        
        /**
         * Parses the skinList.xml file, which is located in following directory: trunk/uportal-war/src/main/webapp/media/skins/universality
         * Once parsed, this function extracts meta data for each <skin> defined in the skinList.xml file and constructs mark-up through the
         * buildSkinListTempate() function. Once all mark-up has been built, it is added to the .skin-list DOM container, housed within the 
         * .skin-selector-dialog container.
         */
        var parseSkinListXML = function () {
            // Obtain skinList.xml.
            $.ajax({
                url: (that.settings.mediaPath + "/skinList.xml?noCache=" + new Date().getTime()),
                async: true,
                dataType: "xml",
                type: "GET",
                success: function (xml) {
                    var root, skinNodes, skinList;
                    
                    root = $(xml);
                    skinNodes = root.find("skin");
                    skinList = that.locate("skinList");
                    
                    // Parse skinList.xml & construct ui.
                    $.each(skinNodes, function (idx, obj) {
                        var skin, key, name, description, thumbnailPath;
                        
                        skin = $(obj);
                        key = skin.children("skin-key").text();
                        name = skin.children("skin-name").text();
                        description = skin.children("skin-description").text();
                        thumbnailPath = (that.settings.mediaPath + "/" + key + "/" + "thumb.gif");
                        
                        // Build data model.
                        that.model.push(buildDataModel(key, name, description, thumbnailPath));
                    });//end:loop.
                    
                    // Render UI.
                    doRender();
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
        var initUI = function () {
            var form;
            
            // Cache common resources.
            that.model = that.options.model;
            that.settings = that.options.settings;
            
            // Parse skinList.xml
            parseSkinListXML();
            
            // Disable form submission.
            form = that.container.find("form");
            form.submit(function () {
                return false;
            });
        };//end:function.
        
        /**
         * Private. Entry point for the SkinSelector component.
         */
        var initialize = function () {
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
            listItem: ".list-item",
            skinWidget: ".widget",
            skinName: ".skin-name",
            skinKey: ".skin-key",
            skinDescription: ".skin-description",
            skinThumbnail: ".skin-thumbnail",
            loader: ".loader"
        },
        events: {
            onSelectSkin: null
        },
        settings: null,
        model: [],
        activeSkin: "skin-active"
    });
})(jQuery, fluid);