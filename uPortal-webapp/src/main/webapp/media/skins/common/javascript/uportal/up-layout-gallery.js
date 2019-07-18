/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
'use strict';
var up = up || {};

(function($, fluid) {
    up.AjaxLayoutCategoryListView = function(container, overallThat, options) {
        // construct the new component
        var that = fluid.initView(
            'up.AjaxLayoutCategoryListView',
            container,
            options
        );

        // initialize a state map for this component
        that.state = {};

        var cutpoints = [
            {
                id: '~categoryContainer:',
                selector: '.category-choice-container',
            },
            {id: 'category', selector: '.category-choice'},
            {id: 'categoryLink', selector: '.category-choice-link'},
            {id: 'categoryName', selector: '.category-choice-name'},
        ];

        that.refresh = function() {
            // Build an array of all categories containing at least
            // one deep member, sorted by name
            var categories = [];
            categories.push({
                id: '',
                name: that.options.rootCategoryName,
                description: that.options.rootCategoryDescription,
                categories: [],
                deepCategories: [],
                portlets: [],
                deepPortlets: [],
            });
            $(overallThat.registry.getAllCategories()).each(function(
                idx,
                category
            ) {
                if (
                    category.deepPortlets.length > 0 &&
                    category.id !== 'local.1'
                ) {
                    categories.push(category);
                }
            });
            categories.sort(
                up.getStringPropertySortFunction(
                    'name',
                    that.options.rootCategoryName
                )
            );

            var tree = {children: []};

            var s = overallThat.state.currentCategory || '';

            $(categories).each(function(idx, category) {
                tree.children.push({
                    ID: 'categoryContainer:',
                    children: [
                        {
                            ID: 'category',
                            decorators: [
                                {
                                    type: 'addClass',
                                    classes: category.id === s ? 'active' : '',
                                },
                            ],
                        },
                        {
                            ID: 'categoryLink',
                            decorators: [
                                {
                                    type: 'jQuery',
                                    func: 'click',
                                    args: function() {
                                        overallThat.events.onCategorySelect.fire(
                                            overallThat,
                                            category
                                        );
                                        $('.pager-button-up-inner').click();
                                    },
                                },
                            ],
                        },
                        {ID: 'categoryName', value: category.name},
                    ],
                });
            });

            if (that.state.templates) {
                fluid.reRender(
                    that.state.templates,
                    $(container).find('.categories'),
                    tree,
                    {cutpoints: cutpoints}
                );
            } else {
                that.state.templates = fluid.selfRender(
                    $(container).find('.categories'),
                    tree,
                    {cutpoints: cutpoints}
                );
            }
        };

        that.refresh();
        return that;
    };

    fluid.defaults('up.AjaxLayoutCategoryListView', {
        rootCategoryName: 'ALL',
        rootCategoryDescription: 'All Categories',
    });

    up.AjaxLayoutPortletListView = function(container, overallThat, options) {
        var that;
        var cutpoints;

        // Construct the new component.
        that = fluid.initView(
            'up.AjaxLayoutPortletListView',
            container,
            options
        );

        // Initialize draggable manager.
        that.dragManager = fluid.initSubcomponent(that, 'dragManager', [
            that.container,
            fluid.COMPONENT_OPTIONS,
        ]);

        // DragManger 'onDropTarget' callback.
        that.dragManager.events.onDropTarget.addListener(function(
            method,
            targetID
        ) {
            that.state.drag.overall.events.onPortletDrag.fire(
                that.state.drag.data,
                method,
                targetID
            );
        });

        // initialize a state map for this component
        that.state = {};

        cutpoints = [
            {id: 'portlet:', selector: '.portlet'},
            {id: 'portletWrapper', selector: '.portlet-wrapper'},
            {id: 'portletLink', selector: '.portlet-thumb-link'},
            {id: 'portletTitle', selector: '.portlet-thumb-titlebar'},
            {
                id: 'portletDescription',
                selector: '.portlet-thumb-description',
            },
            {id: 'portletIcon', selector: '.portlet-thumb-icon'},
        ];

        that.refresh = function() {
            var portlets;
            var members;

            // Build a list of all portlets that are a deep member of the
            // currently-selected category, sorted by title
            portlets = [];
            members =
                overallThat.state.currentCategory &&
                overallThat.state.currentCategory !== ''
                    ? overallThat.registry.getMemberPortlets(
                          overallThat.state.currentCategory,
                          true
                      )
                    : overallThat.registry.getAllPortlets();
            $(members).each(function(idx, portlet) {
                if (
                    !overallThat.state.portletRegex ||
                    overallThat.state.portletRegex.test(portlet.title) ||
                    overallThat.state.portletRegex.test(portlet.name) ||
                    overallThat.state.portletRegex.test(portlet.fname) ||
                    overallThat.state.portletRegex.test(portlet.description)
                ) {
                    if (!portlet.iconUrl) {
                        portlet.iconUrl = that.options.defaultIconUrl;
                    }
                    portlets.push(portlet);
                }
            });
            portlets.sort(up.getStringPropertySortFunction('title'));

            if (that.state.pager) {
                up.refreshPager(that.state.pager, portlets);
            } else {
                // define the column structure for the portlets pager
                var columnDefs = [
                    {
                        key: 'portletWrapper',
                        valuebinding: '*.id',
                        components: function(row) {
                            return {
                                decorators: [
                                    {
                                        type: 'jQuery',
                                        func: 'mousedown',
                                        args: function() {
                                            // Update drag state. This state object is read
                                            // when a thumbnail is dropped into a portal column.
                                            that.state.drag = {
                                                overall: overallThat,
                                                data: row,
                                            };
                                        },
                                    },
                                    {
                                        type: 'attrs',
                                        attributes: {
                                            title:
                                                row.title +
                                                ' (' +
                                                row.name +
                                                ')',
                                        },
                                    },
                                ],
                            };
                        },
                    },
                    {
                        key: 'portletLink',
                        valuebinding: '*.id',
                        components: function(row) {
                            return {
                                decorators: [
                                    {
                                        type: 'jQuery',
                                        func: 'click',
                                        args: function() {
                                            overallThat.events.onPortletSelect.fire(
                                                overallThat,
                                                row
                                            );
                                        },
                                    },
                                ],
                            };
                        },
                    },
                    {
                        key: 'portletTitle',
                        valuebinding: '*.title',
                    },
                    {
                        key: 'portletDescription',
                        valuebinding: '*.description',
                    },
                    {
                        key: 'portletIcon',
                        valuebinding: '*.id',
                        components: function(row) {
                            return {
                                decorators: [
                                    {
                                        type: 'attrs',
                                        attributes: {
                                            style:
                                                'background: url(' +
                                                row.iconUrl +
                                                ') top left no-repeat;',
                                        },
                                    },
                                ],
                            };
                        },
                    },
                ];

                // set the other pager options
                var pagerOptions = {
                    dataModel: portlets,
                    annotateColumnRange: 'portletTitle',
                    columnDefs: columnDefs,
                    components: {
                        bodyRenderer: {
                            type: 'fluid.table.selfRender',
                            options: {
                                selectors: {
                                    root: '.portlet-list',
                                },
                                row: 'portlet:',
                                rendererOptions: {
                                    cutpoints: cutpoints,
                                },
                            },
                        },
                        pagerBar: {
                            type: 'fluid.pager.pagerBar',
                            options: {
                                pageList: {
                                    type: 'fluid.pager.renderedPageList',
                                    options: {
                                        linkBody: 'a',
                                    },
                                },
                            },
                        },
                    },
                    listeners: {
                        onModelChange: function(newModel, oldModel, pager) {
                            // Temporary solution. The initDragAndDrop() method call has been wrapped
                            // within a setTimeout to resolve a fluid.pager bug (i.e. UP-4000). When
                            // the onModelChange event fires the DOM contained within the pager.container
                            // element becomes stale. For some reason that is not yet known, the pager's
                            // internal DOM, within the onModelChange event, is not ready for manipulation.
                            // This is important because the initDragAndDrop() method needs to apply the jQuery
                            // UI Draggable plugin to the underlying list items contained within the pager.container.
                            // With a stale or out-of-sync DOM the list items within the gallery fail to become
                            // draggable. Wrapping this method call within a setTimeout with the duration set to
                            // 0 forces the initDragAndDrop() method to be the last JavaScript function executed,
                            // giving time for the DOM to be redrawn.
                            var timer = setTimeout(function() {
                                that.dragManager.initDragAndDrop(
                                    newModel,
                                    oldModel,
                                    pager
                                );
                                clearTimeout(timer);
                            }, 0);
                        },
                    },
                };

                // initialize the pager and set it to 6 items per page.
                that.state.pager = fluid.pagedTable(
                    $(container).find('.portlet-results'),
                    pagerOptions
                );
                that.state.pager.events.initiatePageSizeChange.fire(
                    that.options.pageSize
                );
            } // end:if.
        }; // end:function.
        that.refresh();

        return that;
    };

    fluid.defaults('up.AjaxLayoutPortletListView', {
        dragManager: {
            type: 'up.LayoutDraggableManager',
        },
        selectors: {
            galleryList: '.portlet-list',
        },
        pageSize: 6,
        defaultIconUrl:
            '/ResourceServingWebapp/rs/tango/0.8.90/32x32/categories/applications-other.png',
    });

    up.BrowseContentPane = function(container, overallThat, options) {
        var that = fluid.initView('up.BrowseContentPane', container, options);

        var initialized = false;

        that.showPane = function() {
            if (!initialized) {
                overallThat.showLoading();
                initialized = true;
                that.portletBrowser = fluid.initSubcomponent(
                    that,
                    'portletBrowser',
                    [that.locate('pane'), overallThat, fluid.COMPONENT_OPTIONS]
                );
            }
            // show the pane and mark the pane link as active
            that.locate('pane').show();
            that.locate('paneLink').addClass('active');
            that.events.onShow.fire(that);
        };

        that.hidePane = function() {
            that.locate('pane').hide();
            that.locate('paneLink').removeClass('active');
        };

        that.hidePaneLink = function() {
            that.locate('pane').hide();
            that.locate('paneLink')
                .hide()
                .removeClass('active');
        };

        that.showPaneLink = function() {
            that.locate('paneLink').show();
        };

        that.showLoading = function() {
            that.locate('ui').hide();
            that.locate('loading').show();
        };

        that.hideLoading = function() {
            that.locate('loading').hide();
            that.locate('ui').show();
        };

        that.locate('paneLink').click(function() {
            overallThat.showPane(that.options.key);
        });

        return that;
    };

    fluid.defaults('up.BrowseContentPane', {
        portletBrowser: {
            type: 'up.PortletBrowser',
            options: {
                portletRegistry: {
                    options: {portletListUrl: 'channel-list.js'},
                },
                categoryListView: {
                    type: 'up.AjaxLayoutCategoryListView',
                },
                portletListView: {
                    type: 'up.AjaxLayoutPortletListView',
                },
                listeners: {
                    onLoad: function(portletBrowser, gallery) {
                        gallery.hideLoading();
                    },
                },
            },
        },
        key: 'add-content',
        selectors: {
            pane: '.add-content',
            paneLink: '.add-content-link',
            ui: '.content-results-wrapper',
            loading: '.content-loading',

            portletMenu: '.add-content .categories-column',
            portletMenuDetail: '.add-content .categories-wrapper',
            portletListLink: '.add-content .portlet-list-link',
            portletList: '.add-content .portlet-results',
        },
        events: {
            onInitialize: null,
            onShow: null,
        },
        listeners: {
            onInitialize: null,
            onShow: null,
        },
    });

    up.PortalGalleryPane = function(container, overallThat, options) {
        var that;
        var initialized;

        that = fluid.initView('up.PortalGalleryPane', container, options);

        initialized = false;

        /**
         * Show this pane.  If the initialization method has not yet been
         * called, show the loading screen, initialize the pane, and then
         * hide the loading pane when done.
         */
        that.showPane = function() {
            // if the pane has not yet been initialized, show the loading
            // screen while we initialize it
            if (!initialized) {
                overallThat.showLoading();
                that.events.onInitialize.fire(that);
                initialized = true;
                overallThat.hideLoading();
            }
            // show the pane and mark the pane link as active
            that.locate('pane').show();
            that.locate('paneLink').addClass('active');
            that.events.onShow.fire(that);
        };

        /**
         * Hide this pane
         */
        that.hidePane = function() {
            that.locate('pane').hide();
            that.locate('paneLink').removeClass('active');
        };

        // wire the pane link to display the appropriate pane
        that.locate('paneLink').click(function() {
            overallThat.showPane(that.options.key);
        });

        return that;
    };

    fluid.defaults('up.PortalGalleryPane', {
        key: null,
        selectors: {
            pane: null,
            paneLink: null,
        },
        events: {
            onInitialize: null,
            onShow: null,
        },
        listeners: {
            onInitialize: null,
            onShow: null,
        },
    });

    up.PortalGallery = function(container, options) {
        var that = fluid.initView('up.PortalGallery', container, options);

        that.panes = [];
        that.panes.push(
            fluid.initSubcomponent(that, 'browseContentPane', [
                container,
                that,
                fluid.COMPONENT_OPTIONS,
            ])
        );
        that.panes.push(
            fluid.initSubcomponent(that, 'useContentPane', [
                container,
                that,
                fluid.COMPONENT_OPTIONS,
            ])
        );
        that.panes.push(
            fluid.initSubcomponent(that, 'skinPane', [
                container,
                that,
                fluid.COMPONENT_OPTIONS,
            ])
        );
        that.panes.push(
            fluid.initSubcomponent(that, 'layoutPane', [
                container,
                that,
                fluid.COMPONENT_OPTIONS,
            ])
        );

        that.openGallery = function() {
            that.options.isOpen = true;
            that.locate('galleryHandle').addClass('handle-arrow-up');
            that.locate('galleryInner').slideDown(that.options.openSpeed);
            if (
                $('#portalPageBodyColumns .portal-page-column').filter(
                    '.canAddChildren,.up-fragment-admin'
                )
            ) {
                that.showPane('add-content');
            } else {
                that.showPane('use-content');
                that.hidePaneLink('add-content');
            }
        };

        that.closeGallery = function() {
            that.options.isOpen = false;
            that.locate('galleryHandle').removeClass('handle-arrow-up');
            that.locate('galleryInner').slideUp(that.options.closeSpeed);
        };

        that.showPane = function(key) {
            $(that.panes).each(function(idx, pane) {
                if (pane.options.key === key) {
                    pane.showPane();
                } else {
                    pane.hidePane();
                }
            });
        };

        that.hidePaneLink = function(key) {
            $(that.panes).each(function(idx, pane) {
                if (pane.options.key === key) {
                    pane.hidePaneLink();
                }
            });
        };

        that.showPaneLink = function(key) {
            $(that.panes).each(function(idx, pane) {
                if (pane.options.key === key) {
                    pane.showPaneLink();
                }
            });
        };

        that.refreshPaneLink = function(key) {
            if (
                $('#portalPageBodyColumns .portal-page-column').filter(
                    '.canAddChildren,.up-fragment-admin'
                )
            ) {
                that.showPaneLink('add-content');
            } else {
                that.hidePaneLink('add-content');
            }
        };

        that.showLoading = function() {
            that.locate('ui').hide();
            that.locate('loading').show();
        };

        that.hideLoading = function() {
            var modal;
            var ui;

            modal = that.locate('loading');
            ui = that.locate('ui');

            ui.show();
            modal.fadeOut('slow');
        };

        // wire the gallery handle to open and close the gallery panel
        // as appropriate
        that.locate('galleryHandle').click(function() {
            if (that.options.isOpen) {
                that.closeGallery();
            } else {
                that.openGallery();
            }
        });

        // wire the configured gallery close link
        that.locate('closeLink').click(that.closeGallery);

        // if the gallery is configured to initialize as open, open the gallery
        // before returning
        if (that.options.isOpen) {
            that.openGallery();
        }

        return that;
    };

    fluid.defaults('up.PortalGallery', {
        isOpen: false,
        openSpeed: 500,
        closeSpeed: 50,
        browseContentPane: {
            type: 'up.BrowseContentPane',
        },
        useContentPane: {
            type: 'up.PortalGalleryPane',
            options: {
                key: 'use-content',
                selectors: {
                    pane: '.use-content',
                    paneLink: '.use-content-link',
                },
                listeners: {
                    onInitialize: null,
                },
            },
        },
        skinPane: {
            type: 'up.PortalGalleryPane',
            options: {
                key: 'skin',
                selectors: {
                    pane: '.skins',
                    paneLink: '.skin-link',
                },
                listeners: {
                    onInitialize: null,
                },
            },
        },
        layoutPane: {
            type: 'up.PortalGalleryPane',
            options: {
                key: 'layout',
                selectors: {
                    pane: '.layouts',
                    paneLink: '.layout-link',
                },
                listeners: {
                    onInitialize: function(overallThat) {
                        up.LayoutSelector('.layouts-list', {});
                    },
                },
            },
        },
        selectors: {
            menu: '.menu',
            galleryInner: '.gallery-inner',
            galleryHandle: '.handle span',
            closeLink: '.close-button',
            loading: '.gallery-loader',
            ui: '.content-wrapper .content',
        },
    });
})(jQuery, fluid);
