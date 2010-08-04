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

var uportal = uportal || {};

(function($, fluid){
    
    var render = function(that) {
    
        var cutpoints = [
             { id: "layoutOptions:", selector: that.options.selectors.layoutOptions },
             { id: "layoutOptionHeader", selector: that.options.selectors.layoutOptionHeader },
             { id: "layoutOptionGroup:", selector: that.options.selectors.layoutOptionGroup },
             { id: "layoutOption:", selector: that.options.selectors.layoutOption },
             { id: "layoutOptionLink", selector: that.options.selectors.layoutOptionLink },
             { id: "layoutSelector", selector: that.options.selectors.layoutSelector },
             { id: "layoutImage", selector: that.options.selectors.layoutImage }
         ];
    
        var componentTree = { ID: "layoutOptions:", children: [ ] };
        
        var groups = {};
        var max = 0;
        
        // organize layouts into groupings based on the number of columns in
        // each layout
        $(that.options.allowedLayouts).each(function(idx, layout) {
            if (!groups[layout.length]) groups[layout.length] = [];
            groups[layout.length].push(layout);
            max = Math.max(max, groups[layout.length].length);
        });

        var currentLayoutString = that.options.currentLayout.join("-");

        var numColumns = 0;
        
        // create each group
        var index = 0;
        for (var columns in groups) {
            
            // add the layout group header 
            var tree = { ID: "layoutOptionGroup:", children: [
                { ID: "layoutOptionHeader", value: columns + (columns == 1 ? " Column" : " Columns") }
            ] };

            // add each layout to the group
            $(groups[columns]).each(function(idx, layout){
    
                var layoutString = layout.join("-");
                
                tree.children.push({
                    ID: "layoutOption:",
                    children: [
                        { ID: "layoutImage",
                            decorators: [
                                { type: "attrs", attributes: { src: that.options.imagePath + "layout_" + layoutString + ".gif" } }
                            ] 
                        },
                        { ID: "layoutOptionLink",
                            decorators: [
                                 { type: "attrs", attributes: { layoutChoice: layoutString } },
                                 { type: "jQuery", func: "click", 
                                     args: function() { 
                                         that.state.layout = $(this).attr("layoutChoice").split("-");
                                         that.locate("layoutOption").removeClass("selected");
                                         $(this).parents(that.options.selectors.layoutOption).addClass("selected");
                                         that.events.onUpdateLayout.fire(that.state.layout);
                                         return false;
                                     }
                                 }
                            ] 
                        }
                    ],
                    decorators: [
                        { type: "addClass", classes: (layoutString == currentLayoutString) ? "selected" : "" }
                    ]
                });

                index++;
            });
            
            numColumns++;
            componentTree.children.push(tree);
        }

        componentTree.decorators = { type: "addClass", classes: "fl-col-flex" + numColumns };
        
        fluid.selfRender(that.locate("pageLayoutOptions"), { children: [ componentTree ] }, { cutpoints: cutpoints });
        that.locate("pageNameInput").text(that.state.pageName);
        fluid.inlineEdits(that.locate("pageNameContainer"), {
            listeners: {
                afterFinishEdit: function(newValue, oldValue, editNode, viewNode) {
                    if (newValue != oldValue) {
                        that.state.pageName = newValue;
                        that.events.onUpdatePageName.fire(newValue);
                    }
                }
            }
        });
        that.locate("isDefaultSelector")
            .attr("checked", that.state.isDefault)
            .change(function(){
                that.state.isDefault = this.checked;
                that.events.onUpdateIsDefault.fire(this.checked);
            });
    };

    uportal.PageManager = function(container, options) {
        var that = fluid.initView("uportal.PageManager", container, options);
        
        that.state = {
            pageName: that.options.currentPageName,
            isDefault: that.options.isDefault,
            layout: that.options.currentLayout
        };

        render(that);

        // initialize the permission form submission actions
        $(container).find("form").unbind("submit")
            .submit(function(){
                that.events.onSaveOptions.fire(that.state);
                return false;
            }
        );
        
        return that;
    };

    
    // defaults
    fluid.defaults("uportal.PageManager", {
        currentLayout: [ 50, 50 ],
        allowedLayouts: null,
        currentPageName: null,
        isDefault: false,
        savePermissionsUrl: "mvc/layout",
        imagePath: "test/",
        selectors: {
            pageNameContainer: ".page-name-container",
            pageNameInput: ".page-name-input",
            isDefaultSelector: ".is-default-selector",
            
            layoutOptionsContainer: ".page-layout-options-container",
            layoutOptions: ".page-layout-options",
            layoutOptionHeader: ".layout-option-header",
            layoutOptionGroup: ".layout-option-group",
            layoutOption: ".layout-option",
            layoutOptionLink: ".layout-option-link",
            layoutSelector: ".layout-selector",
            layoutImage: ".layout-image",
        },
        listeners: {
            onSaveOptions: null,
            onUpdateLayout: null,
            onUpdatePageName: null,
            onUpdateIsDefault: null
        }
    });
    
})(jQuery, fluid);