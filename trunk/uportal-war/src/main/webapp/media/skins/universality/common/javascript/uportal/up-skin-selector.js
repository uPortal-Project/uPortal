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
 *         :Reference to the .skin-list DOM container that houses the list of available skins
 *          dynamically created from parsing the skinList.xml file.
 *         
 * listItem
 *         :Reference to <li> DOM container with .list-item class name, which is housed under
 *          the <ul class="skin-list"> container.
 *         
 * skinWidget
 *         :Reference to <div> DOM container with the .fl-widget class name, which is housed under
 *          the <ul class="skin-list"> container.
 *         
 * skinName
 *         :Reference to <h2> DOM container with the .skin-name class name, which is housed in the
 *          <ul class="skin-list"> container.
 *         
 * skinThumbnail
 *         :Reference to <img> DOM container with the .skin-thumbnail class name, which is housed
 *          under the <ul class="skin-list"> container.
 *         
 * ----------------
 * Available events
 * ----------------
 * 
 * onSelectSkin 
 *         :Fired when a user "clicks" on a skin rendered to end users.
 *         
 * ---------------------------
 * Other Configuration Options
 * ---------------------------
 * 
 * currentSkin
 *         :Reference to the currently selected skin.
 *         
 * skinListURL
 *         :URL utilized in ajax GET request to retrieve the skinList.xml file.
 *         
 * mediaPath
 *         :Reference to src attribute value for skin thumbnails.
 *         
 * activeSkin
 *         :Reference to .skin-active class name. The .skin-active class name is applied to the
 *          currently selected skin in the list of skins rendered to end users.
 */

"use strict";
var up = up || {};

(function ($, fluid) {
    /**
     * Private. Builds and returns dataModel object based upon parsed
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
     * Private. Build the cutpoints array, which defines renderer IDs
     * for each HTML element that will be rendered.
     * 
     * @param {Object} that - reference to up.SkinSelector component.
     */
    var buildCutPoints = function (that) {
        return [
            {id: "listItem-row:", selector: that.options.selectors.listItem},
            {id: "skinWrapper", selector: that.options.selectors.skinWrapper},
            {id: "skinLink", selector: that.options.selectors.skinLink},
            {id: "skinName", selector: that.options.selectors.skinName},
            {id: "skinThumbnail", selector: that.options.selectors.skinThumbnail}
        ];
    };//end:function.
    
    /**
     * Private. Build skin list component subtree.
     * 
     * @param {Object} that - reference to up.SkinSelector component.
     */
    var buildSkinListTree = function (that) {
        var treeChildren, skinRows;
        
        treeChildren = [];
        skinRows = fluid.transform(that.state.model, function (obj, index) {
            return {
                ID: "listItem-row:",
                children: [
                    {
                        ID: "skinWrapper",
                        decorators: [
                            { type: "addClass", classes: obj.key === that.options.currentSkin ? that.options.activeSkin : "" }
                        ]
                    },
                    {
                        ID: "skinLink",
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
                                    
                                    // Fire onSelectSkin event.
                                    that.events.onSelectSkin.fire(obj);
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
                        ID: "skinThumbnail", 
                        decorators: [
                            {
                                type: "attrs",
                                attributes: {
                                    style: 'background: url(' + obj.thumbnailPath + ') top left no-repeat;'
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
     * Private. Builds component tree utilized by the fluid.renderer component.
     * 
     * @param {Object} that - reference to up.SkinSelector component.
     */
    var buildComponentTree = function (that) {
        return {
            children: buildSkinListTree(that)
        };
    };//end:function.
    
    /**
     * Private. Configues & executes the fluid.renderer component.
     * 
     * @param {Object} that - reference to up.SkinSelector component.
     */
    var doRender = function (that) {
        var skinList, options;
        
        skinList = that.locate("skinList");
        options = {
            cutpoints: buildCutPoints(that),
            model: that.state.model,
            autoBind: true
        };
        
        // Run renderer.
        that.state.templates = fluid.selfRender(skinList, buildComponentTree(that), options);
        
        // Highlight active skin.
        skinList.find('input[value="' + that.options.currentSkin + '"]').parents(".widget").addClass(that.options.activeSkin);
        
    };//end:function.
    
    /**
     * Private. Parses the skinList.xml file, which is located in following directory: trunk/uportal-war/src/main/webapp/media/skins/universality
     * Once parsed, this function extracts meta data for each <skin> defined in the skinList.xml file and constructs a data model through
     * the buildDataModel() function. Once built, the doRender() function is called, which houses the fluid.renderer implemenentation.
     * 
     * @param {Object} that - reference to up.SkinSelector component.
     */
    var parseSkinListXML = function (that) {
        // Obtain skinList.xml.
        $.ajax({
            url: that.options.skinListURL,
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
                    thumbnailPath = (that.options.mediaPath + "/" + key + "/" + "thumb.gif");
                    
                    // Build data model.
                    that.state.model.push(buildDataModel(key, name, description, thumbnailPath));
                });//end:loop.
                
                // Render UI.
                doRender(that);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                if (console) {
                    console.log("AJAX Failure: ", XMLHttpRequest, textStatus, errorThrown);
                }
            }
        });
    };//end:function.
    
    /**
     * Private. Entry point for the SkinSelector component. Prepares UI
     * for SkinSelector component. This function calls the parseSkinListXML()
     * function and surpresses the submit event for the SkinSelector form.
     * 
     * @param {Object} that - reference to up.SkinSelector component.
     */
    var initialize = function (that) {
        var form;
        
        // Initialize state map & model array.
        that.state = {};
        that.state.model = [];
        
        // Parse skinList.xml
        parseSkinListXML(that);
        
    };//end:function.
    
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
         * Refresh.
         */
        that.refresh = function () {
            var options;
            
            options = {
                cutpoints: buildCutPoints(that),
                model: that.state.model,
                autoBind: true
            };
            fluid.reRender(that.state.templates, that.locate("skinList"), buildComponentTree(that), options);
        };
        
        // Run initialization.
        initialize(that);
        return that;
    };//end:component.
    
    /**
     * SkinSelector
     * Defaults function for the SkinSelector component.
     ---------------------------------*/
    fluid.defaults("up.SkinSelector", {
        selectors: {
            skinList: ".skins-list",
            listItem: ".skin",
            skinWrapper: ".skins-wrapper",
            skinLink: ".skin-link",
            skinName: ".skin-titlebar",
            skinThumbnail: ".skin-thumb"
        },
        events: {
            onSelectSkin: null
        },
        currentSkin: null,
        skinListURL: null,
        mediaPath: null,
        activeSkin: "selected"
    });
})(jQuery, fluid);