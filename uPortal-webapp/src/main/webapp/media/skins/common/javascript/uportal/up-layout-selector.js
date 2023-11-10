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

(function ($, fluid) {
    var getComponentTree = function (that) {
        var currentLayoutString;
        var tree;

        // construct a string representing the current layout
        currentLayoutString = that.options.currentLayout.join('-');

        tree = {children: []};
        $(that.options.layouts).each(function (index, layout) {
            var layoutString = layout.columns.join('-');
            var classes = '';

            if (layout.columns.join('-') === currentLayoutString) {
                classes += 'selected';
            }

            if (layout.disabled) {
                classes += ' disabled';
            }

            tree.children.push({
                ID: 'layoutContainer:',
                decorators: [{type: 'addClass', classes: classes}],
                children: [
                    {
                        ID: 'layout',
                    },
                    {
                        ID: 'layoutLink',
                        decorators: [
                            {
                                type: 'jQuery',
                                func: 'click',
                                args: function () {
                                    if (!layout.disabled) {
                                        that.options.currentLayout =
                                            layout.columns;
                                        that.refresh();
                                        that.events.onLayoutSelect.fire(
                                            layout,
                                            that
                                        );
                                    }
                                },
                            },
                        ],
                    },
                    {
                        ID: 'layoutTitle',
                        value:
                            layout.columns.length +
                            ' ' +
                            that.options.strings[
                                layout.columns.length == 1
                                    ? 'column'
                                    : 'columns'
                            ],
                    },
                    {
                        ID: 'layoutDescription',
                        value: that.options.strings[layout.nameKey],
                    },
                    {
                        ID: 'layoutThumb',
                        decorators: [
                            {
                                type: 'attrs',
                                attributes: {
                                    style:
                                        'background: url(' +
                                        that.options.imagePath +
                                        'layout_' +
                                        layoutString +
                                        '.svg' +
                                        ') top left no-repeat;',
                                },
                            },
                        ],
                    },
                ],
            });
        });

        return tree;
    };

    up.LayoutSelector = function (container, options) {
        var that;
        var cutpoints;

        that = fluid.initView('up.LayoutSelector', container, options);

        cutpoints = [
            {
                id: 'layoutContainer:',
                selector: that.options.selectors.layoutContainer,
            },
            {id: 'layout', selector: that.options.selectors.layout},
            {id: 'layoutLink', selector: that.options.selectors.layoutLink},
            {id: 'layoutTitle', selector: that.options.selectors.layoutTitle},
            {
                id: 'layoutDescription',
                selector: that.options.selectors.layoutDescription,
            },
            {id: 'layoutThumb', selector: that.options.selectors.layoutThumb},
        ];

        /**
         * Refresh the rendered skin selector view
         */
        that.refresh = function () {
            var tree = getComponentTree(that);
            fluid.reRender(that.templates, $(container), tree, {
                cutpoints: cutpoints,
            });
        };

        that.templates = fluid.selfRender(
            $(container),
            getComponentTree(that),
            {
                cutpoints: cutpoints,
            }
        );

        return that;
    };

    // defaults
    fluid.defaults('up.LayoutSelector', {
        currentLayout: [50, 50],
        strings: {
            fullWidth: 'Full-width',
            narrowWide: 'Narrow, wide',
            even: 'Even',
            wideNarrow: 'Wide, narrow',
            narrowWideNarrow: 'Narrow, wide, narrow',
            column: 'Column',
            columns: 'Columns',
            sixColumn: 'Narrow Columns',
        },
        layouts: [], // Default is an empty array b/c layouts are filtered by permissions in up-layout-preferences.js
        imagePath: 'test/',
        selectors: {
            layoutContainer: '.layout',
            layout: '.layout-wrapper',
            layoutLink: '.layout-link',
            layoutTitle: '.layout-titlebar',
            layoutDescription: '.layout-description',
            layoutThumb: '.layout-thumb',
        },
        events: {
            onLayoutSelect: null,
        },
        listeners: {
            onLayoutSelect: null,
        },
    });
})(jQuery, fluid);
