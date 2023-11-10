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
/**
 * The TabManger component houses the tab renaming functionality. This
 * component is a custom wrapper for the fluid simple text inline editor component.
 * The TabManger component is most likely a temporary solution as wrapping the
 * the fluid simple text inline editor component is somewhat overkill. As work continues
 * on enhancing uPortal's Tab management, the TabManger component will most likely
 * be renamed to house forthcoming tab management features.
 *
 * For more information on planned tab management options, please see the below url:
 * https://wiki.jasig.org/display/UPC/Manage+Tabs
 *
 * -------------------
 * Available selectors
 * -------------------
 *
 * ----------------
 * Available events
 * ----------------
 *
 * ---------------------------
 * Other Configuration Options
 * ---------------------------
 *
 */

'use strict';
var up = up || {};

(function ($, fluid) {
    /**
     * Set up the edit container and append the edit field to the container.  If an edit container
     * is not provided, default markup is created.
     *
     * @param {Object} displayContainer The display mode container
     * @param {Object} editField The edit field that is to be appended to the edit container
     * @param {Object} editContainer The edit container markup provided by the integrator
     *
     * @return eContainer The edit container containing the edit field
     */
    fluid.inlineEdit.setupEditContainer = function (
        displayContainer,
        editField,
        editContainer
    ) {
        // eslint-disable-next-line unicorn/prevent-abbreviations
        var eContainer = $(editContainer);
        eContainer =
            eContainer.length > 0
                ? eContainer
                : $("<div class='fl-inlineEditContainer'></div>");
        displayContainer.parent().after(eContainer);
        eContainer.append(editField);

        return eContainer;
    };

    /**
     * Set up the textEditButton. Append a background image with appropriate
     * descriptive text to the button.
     *
     * @return {jQuery} The accessible button located after the display text
     */
    fluid.inlineEdit.setupTextEditButton = function (that) {
        var options = that.options;
        var textEditButton = that.locate('textEditButton');

        if (textEditButton.length === 0) {
            var markup = $(
                "<span class='flc-inlineEdit-textEditButton'></span>"
            );
            markup.addClass(options.styles.textEditButton);

            /**
             * Set text for the button and listen
             * for modelChanged to keep it updated
             */

            fluid.inlineEdit.updateTextEditButton(
                markup,
                that.model.value || options.defaultViewText,
                options.strings.textEditButton
            );
            that.events.modelChanged.addListener(function () {
                fluid.inlineEdit.updateTextEditButton(
                    markup,
                    that.model.value || options.defaultViewText,
                    options.strings.textEditButton
                );
            });

            that.locate('text').after(markup);

            // Refresh the textEditButton with the newly appended options
            textEditButton = that.locate('textEditButton');
        }
        return textEditButton;
    };

    /**
     * Private. Initializes the fluid.inlineEditor component to be used with
     * the active portal tab, as long as the active tab is editable.
     *
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var editTabHandler = function (that) {
        var edit;
        var remove;
        var text;
        var gripper;
        var numberOfPortlets;

        // Cache DOM elements.
        edit = that.locate('edit');
        remove = that.locate('remove');
        gripper = that.locate('grabHandle');

        if (edit.length > 0) {
            // Initialize & configure fluid.inlineEdit component.
            that.inlineEditor = fluid.inlineEdits(that.container, {
                selectors: {
                    text: that.options.selectors.text,
                    edit: that.options.selectors.edit,
                },
                listeners: {
                    afterBeginEdit: function () {
                        // Hide gripper & remove icon.
                        remove.hide();
                        gripper.hide();
                        edit.hide();
                    },
                    afterFinishEdit: function (
                        newValue,
                        oldValue,
                        editNode,
                        viewNode
                    ) {
                        // Show gripper & remove icon.
                        remove.show();
                        gripper.filter('.active').show();
                        edit.show();

                        // Fire afterFinishEdit event.
                        that.events.onTabEdit.fire(
                            newValue,
                            oldValue,
                            editNode,
                            viewNode
                        );
                    },
                },
                submitOnEnter: that.options.submitOnEnter,
                useTooltip: false,
            });

            // Mouseenter event listener. Give focus to the
            // tab name on mouseenter. The mouseenter event
            // only triggers its handler on the element to which
            // it is bound.
            text = that.locate('text');
            text.bind('mouseenter', function () {
                text.css('cursor', 'text');
                text.focus();
            });

            // Mouseleave event listener. Remove focus from
            // the tab name on mouseleave. The mouseleave event
            // only triggers its handler on the element to which
            // it is bound.
            text.bind('mouseleave', function () {
                text.css('cursor', 'pointer');
                text.blur();
            });

            // Trigger edit mode.
            numberOfPortlets = Number.parseInt(that.options.numberOfPortlets);
            if (
                numberOfPortlets === 0 &&
                text.text() === that.options.addTabLabel
            ) {
                text.trigger('click');
            } // end:if.
        } // end:if.
    }; // end:function.

    /**
     * Private. Fires the onRemove event when the 'remove' icon is clicked. Passes a
     * reference to the onRemove listener.
     *
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var removeTabHandler = function (that) {
        var remove;
        remove = that.locate('remove');
        remove.bind('click', function () {
            that.events.onTabRemove.fire(this);
        });
    }; // end: function.

    /**
     * Private. Fires the onAddTab event when the 'Add Tab' link is clicked. Passes new
     * tab configurations to the onAddTab listener.
     *
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var addTabHandler = function (that) {
        var add;
        var tabGroup;
        add = that.locate('add');
        tabGroup = that.container.find(that.options.selectors.tabGroup).text();
        add.bind('click', function () {
            that.events.onTabAdd.fire(
                that.options.addTabLabel,
                that.options.addTabWidths,
                tabGroup
            );
        });
    }; // end: function.

    /**
     * Private. Initializes & configures the fluid.reorderLayout component
     * based upon the TabManager options.
     *
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var moveTabHandler = function (that) {
        // Initialize & configure fluid.reorderLayout component.
        that.reorderLayout = fluid.reorderLayout(that.container, {
            selectors: {
                columns: that.options.selectors.columns,
                modules: that.options.selectors.modules,
                lockedModules: that.options.selectors.lockedModules,
                grabHandle:
                    that.options.tabContext === 'header'
                        ? that.options.selectors.grabHandle
                        : '',
            },
            styles: {
                defaultStyle:
                    'fl-reorderer-' +
                    that.options.tabContext +
                    '-movable-default',
                selected:
                    'fl-reorderer-' +
                    that.options.tabContext +
                    '-movable-selected',
                dragging:
                    'fl-reorderer-' +
                    that.options.tabContext +
                    '-movable-dragging',
                mouseDrag:
                    'fl-reorderer-' +
                    that.options.tabContext +
                    '-movable-dragging',
                hover:
                    'fl-reorderer-' +
                    that.options.tabContext +
                    '-movable-hover',
                dropMarker:
                    'fl-reorderer-' + that.options.tabContext + '-dropMarker',
                avatar: 'fl-reorderer-' + that.options.tabContext + '-avatar',
            },
            listeners: {
                afterMove: function (item) {
                    var tab;
                    var tabShortId;
                    var method;
                    var targetTabShortId;
                    var tabPosition;
                    var listItems;

                    // Capture moved tab & set defaults.
                    tab = $(item);
                    tabShortId = up.defaultNodeIdExtractor(tab);
                    method = that.options.insertBefore;
                    targetTabShortId = null;
                    tabPosition = 1;
                    listItems = that.locate('tabListItems');

                    // Determine when tab is the last tab and
                    // calculate the targetTabShortId.
                    if (tab.is(':last-child')) {
                        method = that.options.appendAfter;
                        targetTabShortId = tab.prev().attr('id').split('_')[1];
                    } else {
                        targetTabShortId = tab.next().attr('id').split('_')[1];
                    } // end:if.

                    // Calculate tab position and apply styles based upon
                    // tab position.
                    $.each(listItems, function (index, object) {
                        var li = $(object);

                        // Find position of moved item.
                        if (li.attr('id') === tab.attr('id')) {
                            tabPosition = index + 1;
                        } // end:if.

                        if (listItems.length === 1) {
                            // Apply & remove styles for a single tab.
                            li.removeClass(that.options.styles.firstTab)
                                .removeClass(that.options.styles.lastTab)
                                .addClass(that.options.styles.singleTab);
                        } else if (index === 0) {
                            // Apply & remove styles for the first tab.
                            li.removeClass(that.options.styles.singleTab)
                                .removeClass(that.options.styles.lastTab)
                                .addClass(that.options.styles.firstTab);
                        } else if (index === listItems.length - 1) {
                            // Apply & remove styles for the last tab.
                            li.removeClass(that.options.styles.singleTab)
                                .removeClass(that.options.styles.firstTab)
                                .addClass(that.options.styles.lastTab);
                        } else {
                            // Apply & remove styles for all other tabs.
                            li.removeClass(that.options.styles.singleTab)
                                .removeClass(that.options.styles.lastTab)
                                .removeClass(that.options.styles.firstTab);
                        } // end:if.
                    }); // end:loop.

                    // Fire afterTabMove event.
                    that.events.onTabMove.fire(
                        tabShortId,
                        method,
                        targetTabShortId,
                        tabPosition
                    );
                },
            },
        });
    }; // end: function.

    /**
     * Private. Evaluates the list of tabs for the last instance of a 'locked' tab.
     * Once found, this function adds a 'locked' class to all previously rendered tabs
     * and it also hides the drag & drop grab handle from 'locked' tabs.
     *
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var manageLockedTabs = function (that) {
        var tabList;
        var lastLockedTab;

        // Find the last instance of a locked tab and add a 'locked' class name
        // to all previous tabs. Hide all drag & drop grab handles from 'locked' tabs.
        tabList = that.locate('tabList');
        lastLockedTab = tabList
            .find(that.options.selectors.lockedModules)
            .last();

        if (lastLockedTab.length > 0) {
            lastLockedTab.find(that.options.selectors.grabHandle).hide();
            lastLockedTab
                .prevAll()
                .addClass(that.options.styles.lockedTab)
                .find(that.options.selectors.grabHandle)
                .hide();
        } // end:if.

        // Initialize drag & drop.
        moveTabHandler(that);
    }; // end:function.

    /**
     * Private. Executes initialization functions for the TabManager component.
     *
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var initialize = function (that) {
        editTabHandler(that);
        removeTabHandler(that);
        addTabHandler(that);
        manageLockedTabs(that);
    }; // end:function.

    /**
     * Creator function for the TabManger component.
     *
     * @param {Object} container - reference to HTML DOM element by ID.
     * @param {Object} options - reference to object containing all configurations.
     -------------------------------------------------------*/
    up.TabManager = function (container, options) {
        var that;
        that = fluid.initView('up.TabManager', container, options);

        initialize(that);
        return that;
    }; // end:component.

    /**
     * Defaults function for the TabManger component.
     -------------------------------------------------------*/
    fluid.defaults('up.TabManager', {
        selectors: {
            text: '.flc-inlineEdit-text',
            edit: '.flc-inlineEditable',
            remove: '.portal-navigation-delete',
            add: '.portal-navigation-add',
            columns: '.flc-reorderer-column',
            modules: '.movable,.up-fragment-admin',
            lockedModules: '.locked:not(.up-fragment-admin)',
            grabHandle: '.portal-navigation-gripper',
            tabList: '#portalNavigationList',
            tabListItems: '.portal-navigation',
            tabGroup: '#activeTabGroup',
        },
        events: {
            onTabEdit: null,
            onTabRemove: null,
            onTabAdd: null,
            onTabMove: null,
        },
        styles: {
            lockedTab: 'locked',
            singleTab: 'single',
            firstTab: 'first',
            lastTab: 'last',
        },
        addTabLabel: 'My Tab',
        addTabWidths: [50, 50],
        insertBefore: 'insertBefore',
        appendAfter: 'appendAfter',
        tabContext: 'header',
        numberOfPortlets: 0,
        submitOnEnter: true,
    });
})(jQuery, fluid);
