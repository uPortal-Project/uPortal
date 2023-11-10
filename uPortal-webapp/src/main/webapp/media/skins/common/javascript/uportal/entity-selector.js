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
    /**
     * Private. Returns type from key.
     *
     * @param {String} key - reference to entity key.
     */
    var getTypeFromKey = function (key) {
        var separatorIndex = key.indexOf(':');
        return key.slice(0, Math.max(0, separatorIndex));
    }; // end:function.

    /**
     * Private. Returns ID.
     *
     * @param {String} key
     */
    var getIdFromKey = function (key) {
        var separatorIndex = key.indexOf(':');
        // eslint-disable-next-line unicorn/prefer-string-slice
        return key.substring(separatorIndex + 1, key.length);
    }; // end:function.

    /**
     * Private. Builds key from entity.
     *
     * @param {Entity} entity - entity from entity registry
     */
    var getKey = function (entity) {
        return entity.entityType + ':' + entity.id;
    };

    /**
     * Private. Outputs selection markup snippet.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} entity - reference to currently selected entity object.
     */
    var buildSingleSelectionSnippet = function (that, entity, selected) {
        var markup;

        switch (selected) {
            case false: {
                markup =
                    '<span class="selection" title="' +
                    that.options.messages.nothingSelected +
                    '">' +
                    that.options.messages.nothingSelected +
                    '</span>';
                break;
            }
            case true: {
                markup =
                    '<a href="#" title="' +
                    that.options.messages.removeSelection +
                    '" key="' +
                    getKey(entity) +
                    '" class="' +
                    that.options.styles.selection +
                    '">' +
                    entity.name +
                    '</a>' +
                    '<input type="hidden" name="groups" value="' +
                    getKey(entity) +
                    '"/>';
                break;
            }
        } // end:switch.

        return markup;
    }; // end:function.

    /**
     * Private. Update button with selection state.
     * (Idempotent)
     *
     * @param {Button} button - button to update click, class and text
     * @param {Object} selectionBasket - DOM object with current selections
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {string} key - key to find in selection Basket for match
     */
    var updateButtonState = function (button, selectionBasket, that, key) {
        button.unbind('click');
        if (selectionBasket.find("a[key='" + key + "']").size() > 0) {
            button
                .text('Remove from Selection ')
                .append("<i class='fa fa-minus-circle'></i>");
            button.removeClass('btn-success').addClass('btn-danger');
            button.bind('click', function () {
                deselectEntity(that, key);
            });
        } else {
            button
                .text('Add to Selection ')
                .append("<i class='fa fa-plus-circle'></i>");
            button.removeClass('btn-danger').addClass('btn-success');
            button.bind('click', function () {
                selectEntity(that, key);
            });
        }
    };

    /**
     * Private. Update the visual selection states for the various selection links.
     * (Idempotent)
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     */
    var updateSelectionStates = function (that) {
        var content;
        var selectionBasket;
        var button;
        var key;

        // Get selections.
        selectionBasket = that.locate('selectionBasket');

        // Check current current entity.
        button = that.locate('currentSelectBtn');
        key = that.locate('currentEntityName').attr('key');
        updateButtonState(button, selectionBasket, that, key);

        // Check ad hoc groups table.
        content = that.locate('entityBrowserContent');
        content.find('button.btn-select').each(function () {
            key = $(this).attr('key');
            updateButtonState($(this), selectionBasket, that, key);
        });
    }; // end:function.

    /**
     * Private. Remove an entity from the selection list.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to passed anchor tag attribute. ex: group:local.17
     */
    var deselectEntity = function (that, key) {
        var selectionBasket;
        var entity;
        var buttonPrimary;
        var newselections;

        // Cache & reset DOM references.
        selectionBasket = that.locate('selectionBasket');
        buttonPrimary = that.locate('buttonPrimary');
        entity = that.registry.getEntity(
            getTypeFromKey(key),
            getIdFromKey(key)
        );
        newselections = [];

        // Check component selection mode.
        switch (that.selectMultiple) {
            case false: {
                // Single.
                that.options.selected = [];
                selectionBasket.html(
                    buildSingleSelectionSnippet(that, entity, false)
                );
                break;
            }
            case true: {
                // Multiple.
                // Generate a new list of selected entities. Remove the requested
                // entity from the selection basket.
                selectionBasket.find('a').each(function () {
                    var a = $(this);
                    if (a.attr('key') === key) {
                        a.parent().remove();
                    } else {
                        newselections.push(a.attr('key'));
                    } // end:if.
                }); // end:loop.
                that.options.selected = newselections;
                break;
            }
        } // end:switch.

        updateSelectionStates(that);

        // Enable submit.
        if (
            that.options.selected.length === 0 &&
            that.options.requireSelection
        ) {
            buttonPrimary.attr('disabled', 'disabled');
        } // end:if.
    }; // end:function.

    /**
     * Private. Add an entity to the selected list.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to currrently 'selected' entity. ex: group:local.17
     */
    var selectEntity = function (that, key) {
        var selectionBasket;
        var buttonPrimary;
        var li;
        var entity;

        // Cache DOM elements.
        selectionBasket = that.locate('selectionBasket');
        buttonPrimary = that.locate('buttonPrimary');
        entity = that.registry.getEntity(
            getTypeFromKey(key),
            getIdFromKey(key)
        );

        // Check component selection mode.
        switch (that.selectMultiple) {
            case false: {
                // Single.
                that.options.selected = [];
                that.options.selected.push(key);
                selectionBasket.html(
                    buildSingleSelectionSnippet(that, entity, true)
                );

                // Assign click event.
                selectionBasket.find('a').click(function () {
                    that.options.selected = [];
                    selectionBasket.html(
                        buildSingleSelectionSnippet(that, entity, false)
                    );
                    deselectEntity(that, $(this).attr('key'));
                }); // end:click.
                break;
            }
            case true: {
                // Multiple.
                // If 'key' does not exist within 'selected' arrary.
                if ($.inArray(key, that.options.selected) < 0) {
                    // Add the key to our selected list.
                    that.options.selected.push(key);

                    // Add an element to the user-visible select list.
                    li = $(
                        '<li><a href="#" key="' +
                            getKey(entity) +
                            '">' +
                            entity.name +
                            '</a><input type="hidden" name="groups" value="' +
                            getKey(entity) +
                            '"/></li>'
                    );

                    // Append li to selectionBasket.
                    selectionBasket.find('ul').append(li);

                    // Assign click event.
                    li.find('a').click(function () {
                        deselectEntity(that, $(this).attr('key'));
                    }); // end:click.
                } // end:if.
                break;
            }
        } // end:switch.

        updateSelectionStates(that);

        // Enable submit.
        buttonPrimary.removeAttr('disabled');
    }; // end:function.

    /**
     * Private. Remove breadcrumb from breadcrumb lists.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} anchor - reference to <a> element.
     @ @param {Function} browseFn - function to associate with anchor
     */
    var removeBreadCrumb = function (that, anchor, browseFunction) {
        var crumb;
        var next;

        // Cache.
        crumb = anchor.parent();
        next = crumb.nextAll();

        // Remove all crumbs to the right of the 'clicked' crumb.
        next.remove();

        // Render view associated with the 'clicked' crumb.
        browseFunction(that, anchor.attr('key'));
    }; // end:function.

    /**
     * Private. Builds mark-up string for breadCrumb.
     *
     * @param {Object} key - reference to entity key.
     * @param {Object} entityName - reference to entity name.
     * @param {Object} breadcrumbs - DOM object of breadcrumbs
     @ @param {Function} browseFn - function to associate with anchor
     */
    var buildBreadCrumb = function (
        that,
        key,
        entityName,
        breadcrumbs,
        browseFunction
    ) {
        var breadcrumb;
        breadcrumb =
            '<span><a href="#" title="' +
            entityName +
            '" key="' +
            key +
            '">' +
            entityName +
            '</a> &gt; </span>';
        breadcrumbs.append(breadcrumb);

        // Breadcrumb click event.
        breadcrumbs
            .find('a')
            .unbind('click')
            .click(function () {
                removeBreadCrumb(that, $(this), browseFunction);
            }); // end:click.
    }; // end:function.

    /**
     * Private. Update the breadcrumb trail.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} entity - reference to the entity object.
     * @param {String} breadcrumbsSel - selector name of breadcrumb
     @ @param {Function} browseFn - function to associate with anchor
     */
    var updateBreadcrumbs = function (
        that,
        entity,
        breadcrumbsSel,
        browseFunction
    ) {
        var breadcrumbs;
        var key;
        var isKey;

        // Cache.
        breadcrumbs = that.locate(breadcrumbsSel);
        key = getKey(entity);

        // Add breadcrumb.
        if (breadcrumbs.find('span').length > 0) {
            // Breadcrumbs do exist.
            isKey =
                breadcrumbs.find('span a[key="' + key + '"]').length > 0
                    ? true
                    : false;
            if (!isKey) {
                buildBreadCrumb(
                    that,
                    key,
                    entity.name,
                    breadcrumbs,
                    browseFunction
                );
            } // end:if.
        } else {
            // No breadcrumbs exist.
            buildBreadCrumb(
                that,
                key,
                entity.name,
                breadcrumbs,
                browseFunction
            );
        } // end:if.

        // Add the '.last' class name to the last availble breadcrumb.
        breadcrumbs
            .find('.' + that.options.styles.last)
            .removeClass(that.options.styles.last)
            .css('visibility', 'visible');
        breadcrumbs
            .find('span:last')
            .addClass(that.options.styles.last)
            .css('visibility', 'hidden');
    }; // end:function.

    /**
     * Private. Browse to a particular entity.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to currently 'focused' group key. ex: group:pags.Ad%20Hoc%20Groups
     */
    var browseEntity = function (that, key) {
        console.log('browse ad hoc');
        var entity;
        var currentEntityName;
        var content;

        // Cache.
        entity = that.registry.getEntity(
            getTypeFromKey(key),
            getIdFromKey(key)
        );
        that.currentAdHocGroup = entity;

        // Update title and breadcrumbs.
        currentEntityName = that.locate('currentEntityName');
        currentEntityName.text(entity.name);
        currentEntityName.attr('key', getKey(entity));
        updateBreadcrumbs(that, entity, 'breadcrumbs', browseEntity);

        // Clear member tables.
        content = that.locate('entityBrowserContent');
        content.find('table').each(function () {
            $(this).html('').hide();
        });

        // For each entity, create a member list item.
        $.each(entity.children, function (index, object) {
            var tdChild;
            var tdButtons;
            var divButtons;
            var selButton;
            var selIcon;
            var tr;
            var table;
            var a;
            var objectType;

            objectType = object.entityType.toLowerCase();

            // Create entity name/link.
            if (objectType == 'person' || objectType == 'portlet') {
                a = document.createElement('span');
                a.textContent = object.name;
            } else {
                a = document.createElement('a');
                a.href = 'javascript:;';
                a.text = object.name;
                a.setAttribute('key', getKey(object));
            }
            a.className = that.options.styles.memberLink;

            // Create entity td.
            tdChild = document.createElement('td');
            tdChild.append(a);

            // Create buttons, div and td.
            selButton = document.createElement('button');
            selButton.className = 'btn btn-select btn-success btn-xs';
            selButton.setAttribute('key', getKey(object));
            selButton.append(document.createTextNode('Add to Selection '));
            selIcon = document.createElement('i');
            selIcon.className = 'fa fa-plus-circle';
            selButton.append(selIcon);
            divButtons = document.createElement('div');
            divButtons.className = 'btn-group pull-right';
            divButtons.role = 'group';
            divButtons.append(selButton);
            tdButtons = document.createElement('td');
            tdButtons.append(divButtons);
            /*
                 <button type="button" class="btn btn-info btn-xs">Edit Group <i class="fa fa-pencil"></i></button>
                 <button type="button" class="btn btn-danger btn-xs">Delete Group <i class="fa fa-trash-o"></i></button>
             */

            // Create row and add to table.
            tr = document.createElement('tr');
            tr.append(tdChild);
            tr.append(tdButtons);
            table = content
                .find('.' + objectType)
                .find('.' + that.options.styles.memberList);
            table.append(tr);
            table.show();
        }); // end:loop.

        // Reset no-members.
        that.container.find('.no-members').each(function (index, object) {
            object = $(object);
            if (object.parent().find('tr').size() > 0) {
                object.hide();
            } else {
                object.show();
            }
        });

        // Register click event on member list links.
        content
            .find('table')
            .find('a')
            .click(function () {
                browseEntity(that, $(this).attr('key'));
            }); // end:click.

        // Set selection states.
        updateSelectionStates(that);
    }; // end:function.

    /**
     * Private. Renders 'selected' search items to the end user.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     */
    var updateSearchView = function (that) {
        var list;

        // Cache.
        list = that.searchDropDown.find(that.options.selectors.searchResults);
        list.find('.' + that.options.styles.selected).removeClass(
            that.options.styles.selected
        );

        // Loop through selected array.
        $.each(that.options.selected, function (index, object) {
            var span = list.find('span[key="' + object + '"]');
            span.parent().parent().addClass(that.options.styles.selected);
        }); // end:loop.
    }; // end:function.

    /**
     * Private. Determines which action, selection or deselection, executes.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to key attribute passed over when search link is clicked.
     */
    var itemSelectionHandler = function (that, key) {
        // Cache.
        var entity = that.registry.getEntity(
            getTypeFromKey(key),
            getIdFromKey(key)
        );
        console.log(entity);

        // Selection.
        if ($.inArray(key, that.options.selected) === -1) {
            // Key does not exist.
            selectEntity(that, getKey(entity));
        } else {
            // Key exists.
            deselectEntity(that, getKey(entity));
        } // end:if.

        // Update UI.
        updateSearchView(that);
    }; // end:function.

    /**
     * Private. Search for a specific entity.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} searchTerm - reference to search term.
     */
    var search = function (that, searchTerm) {
        var entities;
        var list;
        var listItem;
        var searchResultsNoMembers;
        var members;

        // Filter searchTerm.
        if (searchTerm === that.options.messages.searchValue) {
            searchTerm = '';
        } // end:if.

        // Cache.
        entities = that.registry.searchEntities(
            that.options.entityTypes,
            searchTerm
        );
        list = that.searchDropDown.find(that.options.selectors.searchResults);
        searchResultsNoMembers = that.locate('searchResultsNoMembers');
        list.html('');
        listItem = '';

        // Loop through each entity. Build list items.
        $.each(entities, function (index, object) {
            listItem +=
                '<li class="' +
                object.entityType +
                '"><a href="#" title="' +
                object.name +
                '"><span key="' +
                getKey(object) +
                '">' +
                object.name +
                '</span></a></li>';
        }); // end:loop.
        list.html(listItem);

        // Assign default 'click' event.
        list.find('a').bind('click', function () {
            var span = $(this).find('span');
            console.log(span.attr('key'));
            itemSelectionHandler(that, span.attr('key'));
        }); // end:listener.

        // Render 'No Members' when list is empty.
        members = list.find('li');
        if (members.length === 0) {
            searchResultsNoMembers.show();
            list.hide();
        } else {
            list.show();
            searchResultsNoMembers.hide();
        }
        // end:if.

        // Update UI.
        updateSearchView(that);

        return false;
    }; // end:function.

    /**
     * Private. Initializes search feature.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     */
    var searchEntity = function (that) {
        var closeSearch;
        var searchForm;
        var searchField;
        var searchDropDown;
        var loader;

        // Cache.
        closeSearch = that.locate('closeSearch');
        searchForm = that.locate('searchForm');
        searchField = searchForm.find('input[type=search]');
        loader = that.locate('searchLoader');
        searchDropDown = that.locate('searchDropDown');

        // Apply.
        searchDropDown.css({top: searchField.outerHeight()});

        // Binds 'submit' event.
        searchForm.submit(function () {
            up.showLoader(loader);
            search(that, searchField.val());
            searchDropDown.show();
            up.hideLoader(loader);
            return false;
        }); // end:listener.

        // Binds 'click' event to close button.
        closeSearch.find('a').bind('click', function () {
            searchDropDown.hide();
        }); // end:listener.

        // Binds 'click' event listener to the document. Detects a 'click'
        // that occurs outside of the component.
        $(document).bind('click', function (event) {
            if (
                that.isEmptyArray(
                    $(event.target).parents('.' + that.options.styles.search)
                )
            ) {
                searchDropDown.hide();
            } // end:if.
        }); // end:function.
    }; // end:function.

    var createTestMaps = function (groupNames, testAttribute) {
        var tests = [];
        $.each(groupNames, function (index, name) {
            tests.push({
                testValue: name,
                attributeName: testAttribute,
                testerClassName:
                    'org.apereo.portal.groups.pags.testers.AdHocGroupTester',
            });
        });
        return tests;
    };

    /**
     * Private. Runs initialization functions.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     */
    var initialize = function (that) {
        // Initialize search drop-down.
        searchEntity(that);

        that.locate('selectionBasket')
            .find('a')
            .click(function () {
                deselectEntity(that, $(this).attr('key'));
            }); // end:click.

        // Browse to the designated start of ad hoc groups.
        browseEntity(that, that.options.initialFocusedEntity);

        if (that.options.enableAdHocGroups) {
            var jsTreeIncludes = false;
            var jsTreeExcludes = false;

            var entityToJSTreeNode = function (entity) {
                var childNodes = [];
                $.each(entity.children, function (index, child) {
                    if (child.entityType == 'GROUP') {
                        childNodes.push({
                            id: getKey(child),
                            text: child.name,
                            state: {loaded: false},
                        });
                    }
                });
                return {
                    id: getKey(entity),
                    text: entity.name,
                    children: childNodes,
                };
            };

            var callback = function (object, callback_) {
                var key;
                var entity;
                var childNodes = [];
                if (object.id === '#') {
                    // root tree node, so send back initial node
                    key = that.options.initialFocusedEntity;
                    entity = that.registry.getEntity(
                        getTypeFromKey(key),
                        getIdFromKey(key)
                    );
                    childNodes.push(entityToJSTreeNode(entity));
                } else {
                    key = object.id;
                    entity = that.registry.getEntity(
                        getTypeFromKey(key),
                        getIdFromKey(key)
                    );
                    $.each(entity.children, function (index, child) {
                        if (child.entityType == 'GROUP') {
                            childNodes.push(entityToJSTreeNode(child));
                        }
                    });
                }
                callback_.call(this, childNodes);
            };

            var displayResponseMessage = function (xmlhttp) {
                console.log('display response message');
                console.log(xmlhttp.responseText);
                switch (xmlhttp.status) {
                    case 200: // SC_OK
                    case 201: // SC_CREATED
                    case 202: {
                        // SC_ACCEPTED
                        $(that.options.selectors.alertSuccess).show();
                        break;
                    }
                    case 400: {
                        // SC_BAD_REQUEST -> bad parent
                        $(that.options.selectors.alertInvalidParent).show();
                        break;
                    }
                    case 409: {
                        // SC_CONFLICT -> group exists
                        $(that.options.selectors.alertGroupExists).show();
                        break;
                    }
                    case 401: // SC_UNAUTHORIZED
                    case 403: {
                        // SC_FORBIDDEN
                        $(that.options.selectors.alertUnauthorized).show();
                        break;
                    }
                    default: {
                        $(that.options.selectors.alertUnknown).show();
                        break;
                    }
                }
            };

            that.locate('saveAdHocButton').bind('click', function () {
                var parentKey;
                var parentName;
                var includes;
                var excludes;
                var tests;
                var pagsGroup;
                var json;
                var xmlhttp;
                parentKey = that.locate('currentEntityName').attr('key');
                parentName = that.locate('currentEntityName').text();
                includes = [];
                that.locate('dataIncludesList')
                    .find('li')
                    .each(function () {
                        includes.push($(this).text());
                    });
                excludes = [];
                that.locate('dataExcludesList')
                    .find('li')
                    .each(function () {
                        excludes.push($(this).text());
                    });

                tests = createTestMaps(includes, 'group-member');
                tests = tests.concat(
                    createTestMaps(excludes, 'not-group-member')
                );
                pagsGroup = {};
                pagsGroup['name'] = $('#groupName').val();
                pagsGroup['description'] = $('#groupDesc').val();
                pagsGroup['testGroups'] = [{tests: tests}];
                json = JSON.stringify(pagsGroup);
                console.log(json);

                xmlhttp = new XMLHttpRequest();
                xmlhttp.addEventListener('readystatechange', function () {
                    if (xmlhttp.readyState == 4) {
                        if (xmlhttp.status >= 200 && xmlhttp.status <= 202) {
                            that.registry.removeEntity(parentKey);
                            browseEntity(that, parentKey);
                        }
                        displayResponseMessage(xmlhttp);
                    }
                });
                xmlhttp.open(
                    'POST',
                    that.options.pagsApiUrl + parentName + '.json',
                    true
                );
                xmlhttp.send(json);
            });

            $(that.options.selectors.adHocGroupsModal).on(
                'show.bs.modal',
                function () {
                    $(that.options.selectors.alerts).hide();

                    // Initialize jsTree widgets within this dialog only when shown and only once
                    if (!jsTreeIncludes) {
                        $(that.options.selectors.dialogIncludesTree).jstree({
                            core: {
                                data: callback,
                            },
                            checkbox: {
                                keep_selected_style: false,
                                three_state: false,
                            },
                            plugins: ['checkbox'],
                        });
                    }
                    if (!jsTreeExcludes) {
                        $(that.options.selectors.dialogExcludesTree).jstree({
                            core: {
                                data: callback,
                            },
                            checkbox: {
                                keep_selected_style: false,
                                three_state: false,
                            },
                            plugins: ['checkbox'],
                        });
                    }
                }
            );
        } else {
            that.locate('adHocCreate').hide();
        }

        // Disable primary button.
        if (
            that.options.selected.length === 0 &&
            that.options.requireSelection
        ) {
            that.locate('buttonPrimary').attr('disabled', 'disabled');
        } // end:if.
    }; // end:function.

    /**
     * Creator function for the entityselection component.
     *
     * @param {Object} container - reference to DOM container.
     * @param {Object} options - reference to configuration object.
     */
    up.entityselection = function (container, options) {
        var that;

        // Initialize component & cache globals.
        that = fluid.initView('up.entityselection', container, options);
        that.selectMultiple = that.options.selectMultiple;
        that.searchDropDown = that.locate('searchDropDown');

        that.registry = fluid.initSubcomponent(that, 'entityRegistry', [
            container,
            fluid.COMPONENT_OPTIONS,
        ]);

        /**
         * Public. Checks passed array's length property.
         * If the length of the array is 0 the array is
         * empty. Returns true if the array is empty.
         *
         * @param {Object} arr - reference to passed array object.
         */
        that.isEmptyArray = function (array) {
            return array.length > 0 ? false : true;
        }; // end:function.

        initialize(that);
        return that;
    }; // end:component.

    // Defaults.
    fluid.defaults('up.entityselection', {
        entityRegistry: {
            type: 'up.EntityRegistry',
        },
        entityTypes: [],
        selected: [],
        initialFocusedEntity: 'group:local.0',
        enableAdHocGroups: false,
        selectMultiple: true,
        requireSelection: true,
        pagsApiUrl: '/api/v4-3/pags/',
        selectors: {
            alerts: '.alert',
            alertSuccess: '#alertSuccess',
            alertInvalidParent: '#alertInvalidParent',
            alertGroupExists: '#alertGroupExists',
            alertUnauthorized: '#alertUnauthorized',
            alertUnknown: '#alertUnknown',
            selectionBasket: '#selectionBasket',
            breadcrumbs: '#entityBrowsingBreadcrumbs',
            currentEntityName: '#currentEntityName',
            entityBrowserContent: '#entityBrowserContent',
            closeSearch: 'closeDropDown',
            searchForm: '#searchForm',
            searchDropDown: '#searchDropDown',
            searchResults: '#searchResults',
            searchResultsNoMembers: '#searchResultsNoMembers',
            searchLoader: 'searchLoader',
            currentSelectBtn: '#currentSelectBtn',
            adHocCreate: '#adHocCreate',
            adHocGroupsModal: '#adhocGroupModal',
            dialogIncludesTree: '#dataIncludes',
            dataIncludesList: '#dataIncludesList',
            dialogExcludesTree: '#dataExcludes',
            dataExcludesList: '#dataExcludesList',
            saveAdHocButton: '#saveAdHocButton',
            buttonPrimary: '#buttonPrimary',
        },
        styles: {
            memberList: 'member-list',
            memberLink: 'member-link',
            selection: 'selection',
            selected: 'selected',
            last: 'last',
            title: 'title',
            search: 'portlet-search',
        },
        messages: {
            selectButtonMessage: '',
            deselectButtonMessage: '',
            removeCrumb: '',
            removeSelection: '',
            addSelection: '',
            selected: '',
            nothingSelected: '',
            searchValue: '',
        },
    });
})(jQuery, fluid);
