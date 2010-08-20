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

(function ($, fluid) {
    
    up.AjaxLayoutCategoryListView = function (container, overallThat, options) {

        // construct the new component
        var that = fluid.initView("up.AjaxLayoutCategoryListView", container, options);

        // initialize a state map for this component
        that.state = {};

        var cutpoints = [ 
            { id: "~categoryContainer:", selector: ".category-choice-container" },
            { id: "category", selector: ".category-choice" },
            { id: "categoryLink", selector: ".category-choice-link" },
            { id: "categoryName", selector: ".category-choice-name" }
        ];

        that.refresh = function () {
            // Build an array of all categories containing at least
            // one deep member, sorted by name
            var categories = [];
            categories.push({
                id: "",
                name: "ALL",
                description: "All Categories",
                categories: [],
                deepCategories: [],
                portlets: [],
                deepPortlets: []
            });
            $(overallThat.registry.getAllCategories()).each(function (idx, category) {
                if (category.deepPortlets.length > 0 && category.id !== "local.1") {
                    categories.push(category);
                }
            });
            categories.sort(up.getStringPropertySortFunction("name", "ALL"));

            var tree = { children: [] };

            var s = overallThat.state.currentCategory || "";

            $(categories).each(function (idx, category) {
                tree.children.push({
                    ID: "categoryContainer:",
                    children: [
                        {
                            ID: "category", 
                            decorators: [
                                { type: "addClass", classes: category.id === s ? "active" : "" }
                            ] 
                        },
                        {
                            ID: "categoryLink", 
                            decorators: [
                                { type: "jQuery", func: "click",
                                    args: function () {
                                        overallThat.events.onCategorySelect.fire(overallThat, category);
                                    }
                                }
                            ] 
                        },
                        { ID: "categoryName", value: category.name }
                    ]
                });
            });

            if (that.state.templates) {
                fluid.reRender(that.state.templates, $(container).find(".categories"), tree, { cutpoints: cutpoints });
            } else {
                that.state.templates = fluid.selfRender($(container).find(".categories"), tree, { cutpoints: cutpoints });
            }
            
        };

        that.refresh();
        return that;        
    };
    

    up.AjaxLayoutPortletListView = function (container, overallThat, options) {
    
        var that, cutpoints;

        // construct the new component
        that = fluid.initView("up.AjaxLayoutPortletListView", container, options);

        // initialize a state map for this component
        that.state = {};

        cutpoints = [
            { id: "portlet:", selector: ".portlet" },
            { id: "portletLink", selector: ".portlet-link" },
            { id: "portletTitle", selector: ".portlet-titlebar" },
            { id: "portletDescription", selector: ".portlet-description" },
            { id: "portletIcon", selector: ".portlet-icon" }
        ];
        
        that.refresh = function () {
            
            var portlets, members;

            // Build a list of all portlets that are a deep member of the
            // currently-selected category, sorted by title
            portlets = [];
            members = (overallThat.state.currentCategory && overallThat.state.currentCategory !== "") ? overallThat.registry.getMemberPortlets(overallThat.state.currentCategory, true) : overallThat.registry.getAllPortlets();
            $(members).each(function (idx, portlet) {
                if (!overallThat.state.portletRegex || overallThat.state.portletRegex.test(portlet.title) || overallThat.state.portletRegex.test(portlet.description)) {
                    for (var i = 0; i < portlet.parameters.length; i++) {
                        var parameter = portlet.parameters[i];
                        if (parameter.name === "iconUrl") {
                            portlet.iconUrl = parameter.value;
                            break;
                        }
                    }
                    portlets.push(portlet);
                }
            });
            portlets.sort(up.getStringPropertySortFunction("title"));

            if (that.state.pager) {
                up.refreshPager(that.state.pager, portlets);
            } else {

                // define the column structure for the portlets pager
                var columnDefs = [
                    { key: "portletLink", valuebinding: "*.id", 
                        components: function (row) {
                            return { decorators: [
                                { type: "jQuery", func: "click", args: function () {
                                        overallThat.events.onPortletSelect.fire(overallThat, row);
                                    } 
                                }
                            ] };
                        }
                    },
                    { key: "portletTitle", valuebinding: "*.title" },
                    { key: "portletDescription", valuebinding: "*.description" },
                    { key: "portletIcon", valuebinding: "*.id",
                        components: function (row) {
                            return { decorators: [ { type: "attrs", attributes: { src: row.iconUrl } } ] };
                        }
                    }
                ];

                // set the other pager options
                var pagerOptions = {
                    dataModel: portlets,
                    columnDefs: columnDefs,
                    bodyRenderer: {
                        type: "fluid.pager.selfRender",
                        options: {
                            selectors: {
                                root: ".portlet-list"
                            },
                            row: "portlet:",
                            renderOptions: {
                                cutpoints: cutpoints
                            }
                        }

                    },
                    pagerBar: {
                        type: "fluid.pager.pagerBar", 
                        options: {
                            pageList: {
                                type: "fluid.pager.renderedPageList",
                                options: { 
                                    linkBody: "a"
                                }
                            }
                        }
                    }
                };
    
                // initialize the pager and set it to 8 items per page
                that.state.pager = fluid.pager($(container).find(".portlet-results"), pagerOptions);
                that.state.pager.events.initiatePageSizeChange.fire(8);
                
            }
            
        };

        that.refresh();
        return that;
    };

    up.BrowseContentPane = function (container, overallThat, options) {
        
        var that = fluid.initView("up.BrowseContentPane", container, options);

        var initialized = false;
        
        that.showPane = function () {
            if (!initialized) {
                overallThat.showLoading();
                that.events.onInitialize.fire(that);
                initialized = true;
                overallThat.hideLoading();
            }
            that.locate("pane").show();
            that.locate("paneLink").addClass("active");
        };
        
        that.hidePane = function () {
            that.locate("pane").hide();
            that.locate("paneLink").removeClass("active");
        };
        
        that.showPortletList = function () {
            if (!that.portletBrowser) {
                overallThat.showLoading();
                that.portletBrowser = fluid.initSubcomponent(that, "portletBrowser", [that.locate("pane"), fluid.COMPONENT_OPTIONS]);
                overallThat.hideLoading();
            }
            that.locate("portletList").show();
            that.locate("fragmentList").hide();
        };
        
        that.showFragmentList = function () {
            if (!that.fragmentBrowser) {
                overallThat.showLoading();
                that.fragmentBrowser = fluid.initSubcomponent(that, "fragmentBrowser", [that.locate("pane"), fluid.COMPONENT_OPTIONS]);
                overallThat.hideLoading();
            }
            that.locate("portletList").hide();
            that.locate("fragmentList").show();
        };

        that.locate("paneLink").click(function () { 
            overallThat.showPane(that.options.key); 
        });
        
        that.locate("portletListLink").click(that.showPortletList);
        that.locate("fragmentListLink").click(that.showFragmentList);
        
        return that;
        
    };

    fluid.defaults("up.BrowseContentPane", {
        portletBrowser: {
            type: "up.PortletBrowser",
            options: {
                portletRegistry: {
                    options: { portletListUrl: "channel-list.js" }
                },
                categoryListView: {
                    type: "up.AjaxLayoutCategoryListView"
                },
                portletListView: {
                    type: "up.AjaxLayoutPortletListView"
                }
            }
        },
        fragmentBrowser: {
            type: "up.FragmentBrowser",
            options: {
                fragmentServiceUrl: "tab-list.js"
            }
        },
        key: 'add-content',
        selectors: {
            pane: ".add-content",
            paneLink: ".add-content-link",
    
            portletMenu: ".categories-column",
            portletListLink: ".portlet-list-link",
            portletList: ".portlet-results",
                        
            fragmentMenu: ".packages-column",
            fragmentListLink: ".package-list-link",
            fragmentList: ".package-results"
        },
        listeners: {
            onInitialize: function (that) {
                that.showPortletList();
            },
            onShow: null
        }
    });

    up.PortalGalleryPane = function (container, overallThat, options) {
        
        var that = fluid.initView("up.PortalGalleryPane", container, options);
        
        var initialized = false;
        
        that.showPane = function () {
            if (!initialized) {
                overallThat.showLoading();
                that.events.onInitialize.fire(that);
                initialized = true;
                overallThat.hideLoading();
            }
            that.locate("pane").show();
            that.locate("paneLink").addClass("active");
        };
        
        that.hidePane = function () {
            that.locate("pane").hide();
            that.locate("paneLink").removeClass("active");
        };
        
        that.locate("paneLink").click(function () { 
            overallThat.showPane(that.options.key); 
        });
        
        return that;
        
    };
    
    fluid.defaults("up.PortalGalleryPane", {
        key: null,
        selectors: {
            pane: null,
            paneLink: null
        },
        listeners: {
            onInitialize: null,
            onShow: null
        }
    });

    up.PortalGallery = function (container, options) {
        
        var that = fluid.initView("up.PortalGallery", container, options);

        that.panes = [];
        that.panes.push(fluid.initSubcomponent(that, "browseContentPane", [container, that, fluid.COMPONENT_OPTIONS]));
        that.panes.push(fluid.initSubcomponent(that, "useContentPane", [container, that, fluid.COMPONENT_OPTIONS]));
        that.panes.push(fluid.initSubcomponent(that, "skinPane", [container, that, fluid.COMPONENT_OPTIONS]));
        that.panes.push(fluid.initSubcomponent(that, "layoutPane", [container, that, fluid.COMPONENT_OPTIONS]));

        that.openGallery = function () {
            that.options.isOpen = true;
            that.locate("galleryHandle").addClass('handle-arrow-up');
            that.locate("galleryInner").slideDown(that.options.openSpeed);
            that.showPane("add-content");
        };

        that.closeGallery = function () {
            that.options.isOpen = false;
            that.locate("galleryHandle").removeClass('handle-arrow-up');
            that.locate("galleryInner").slideUp(that.options.closeSpeed);
        };
        
        that.showPane = function (key) {
            $(that.panes).each(function (idx, pane) {
                if (pane.options.key === key) {
                    pane.showPane();
                } else {
                    pane.hidePane();
                }
            });
        };
        
        that.showLoading = function () {
            that.locate("ui").hide();
            that.locate("loading").show();
        };
        
        that.hideLoading = function () {
            that.locate("loading").hide();
            that.locate("ui").show();
        };
        
        // wire the gallery handle to open and close the gallery panel
        // as appropriate
        that.locate("galleryHandle").click(function () { 
            if (that.options.isOpen) {
                that.closeGallery();
            } else {
                that.openGallery();
            }
        });
        
        // wire the configured gallery close link
        that.locate("closeLink").click(that.closeGallery);
        
        // if the gallery is configured to initialize as open, open the gallery
        // before returning
        if (that.options.isOpen) { 
            that.openGallery();
        }
        
        return that;
    };

    fluid.defaults("up.PortalGallery", {
        isOpen: false,
        openSpeed: 500,
        closeSpeed: 50,
        browseContentPane: {
            type: "up.BrowseContentPane"
        },
        useContentPane: {
            type: "up.PortalGalleryPane",
            options: {
                key: "use-content",
                selectors: {
                    pane: ".use-content",
                    paneLink: ".use-content-link"
                },
                listeners: {
                    onInitialize: null
                }
            }
        },
        skinPane: {
            type: "up.PortalGalleryPane",
            options: {
                key: "skin",
                selectors: {
                    pane: ".skins",
                    paneLink: ".skin-link"
                },
                listeners: {
                    onInitialize: null
                }
            }
        },
        layoutPane: {
            type: "up.PortalGalleryPane",
            options: {
                key: "layout",
                selectors: {
                    pane: ".layout",
                    paneLink: ".layout-link"
                },
                listeners: {
                    onInitialize: function (overallThat) {
                        up.LayoutSelector(".layouts-list", {});
                    }
                }
            }
        },
        selectors: {
            menu: ".menu",
            galleryInner: ".gallery-inner",
            galleryHandle: ".handle span",
            closeLink: ".close-button",
            loading: ".content-modal",
            ui: ".content-wrapper .content"
        }
    });
    
})(jQuery, fluid);
