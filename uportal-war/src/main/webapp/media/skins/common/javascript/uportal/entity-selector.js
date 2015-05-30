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
"use strict";
var up = up || {};

(function ($, fluid) {
    
    /**
     * Private. Returns type from key.
     * 
     * @param {String} key - reference to entity key.
     */
    var getTypeFromKey = function (key) {
        var separatorIndex = key.indexOf(":");
        return key.substring(0, separatorIndex);
    };//end:function.
    
    /**
     * Private. Returns key.
     * 
     * @param {String} key
     */
    var getKey = function (key) {
        var separatorIndex = key.indexOf(":");
        return key.substring(separatorIndex + 1, key.length); 
    };//end:function.
    
    /**
     * Private. Outputs selection markup snippet.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} entity - reference to currently selected entity object.
     */
    var buildSingleSelectionSnippet = function (that, entity, selected) {
        var markup;
        
        switch (selected) {
        case false:
            markup = '<span class="selection" title="' + that.options.messages.nothingSelected + '">' + that.options.messages.nothingSelected + '</span>';
            break;
        case true:
            markup = '<a href="javascript:;" title="' + that.options.messages.removeSelection + '" key="' + entity.entityType + ':' + entity.id + '" class="' + that.options.styles.selection + '">' + entity.name + '</a>' + 
                     '<input type="hidden" name="groups" value="' + entity.entityType + ':' + entity.id + '"/>';
            break;
        }//end:switch.
        
        return markup;
    };//end:function.
    
    /**
     * Private. Update the visual selection states for the various selection links.
     * (Idempotent)
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     */
    var updateSelectionStates = function (that) {
        var currentEntityName, currentEntityKey, titleBar, link, adHocMemberList, selectionBasket;

        // Get selections.
        selectionBasket = that.locate("selectionBasket");

        // Check current entity.
        currentEntityName = that.locate("currentEntityName");
        currentEntityKey = currentEntityName.attr("key");
        titleBar = that.locate("entityBrowserTitlebar");
        link = that.locate("selectEntityLink");
        link.unbind("click");
        if (selectionBasket.find("a[key='" + currentEntityKey + "']").size() > 0) {
            titleBar.addClass(that.options.styles.selected);
            link.bind('click', function (e) {
                deselectEntity(that, currentEntityKey);
            });
            link.attr("title", currentEntityName);
        } else {
            titleBar.removeClass(that.options.styles.selected);
            link.bind('click', function (e) {
                selectEntity(that, currentEntityKey);
            });
            link.attr("title", currentEntityName);
        }

        // Check ad hoc groups.
        adHocMemberList = that.locate("adHocMemberList");
        adHocMemberList.find("a").each(function() {
            var a = $(this);
            var button = a.parent().next().find("button.btn-select");
            button.unbind("click");
            var i = button.find("i");
            if (selectionBasket.find("a[key='" + a.attr("id") + "']").size() > 0) {
                button.text("Remove from Selection ").append("<i class='fa fa-minus-circle'></i>");
                button.removeClass("btn-success").addClass("btn-danger");
                button.bind('click', function(e) {
                    deselectEntity(that, a.attr("id"));
                });
            } else {
                button.text("Add to Selection ").append("<i class='fa fa-plus-circle'></i>");
                button.removeClass("btn-danger").addClass("btn-success");
                button.bind('click', function(e) {
                    selectEntity(that, a.attr("id"));
                });
            }
        });

    };//end:function.

    /**
     * Private. Remove an entity from the selection list.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to passed anchor tag attribute. ex: group:local.17
     */
    var deselectEntity = function (that, key) {
        var selectionBasket, entity, buttonPrimary, newselections;
        
        // Cache & reset DOM references.
        selectionBasket = that.locate("selectionBasket");
        buttonPrimary = that.locate("buttonPrimary");
        entity = that.registry.getEntity(getTypeFromKey(key), getKey(key));
        newselections = [];
        
        // Check component selection mode.
        switch (that.selectMultiple) {
        case false: // Single.
            that.options.selected = [];
            selectionBasket.html(buildSingleSelectionSnippet(that, entity, false));
            break;
        case true: // Multiple.
            // Generate a new list of selected entities. Remove the requested
            // entity from the selection basket.
            selectionBasket.find("a").each(function () {
                var a = $(this);
                if (a.attr("key") !== key) {
                    newselections.push(a.attr("key"));
                } else {
                    a.parent().remove();
                }//end:if.
            });//end:loop.
            that.options.selected = newselections;
            break;
        }//end:switch.

        updateSelectionStates(that);

        // Enable submit.
        if (that.options.selected.length < 1) {
            buttonPrimary.attr("disabled", "disabled");
        }//end:if.
    };//end:function.
    
    /**
     * Private. Add an entity to the selected list.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to currrently 'selected' entity. ex: group:local.17
     */
    var selectEntity = function (that, key) {
        var selectionBasket, buttonPrimary, li, entity;
        
        // Cache DOM elements.
        selectionBasket = that.locate("selectionBasket");
        buttonPrimary = that.locate("buttonPrimary");
        entity = that.registry.getEntity(getTypeFromKey(key), getKey(key));
        
        // Check component selection mode.
        switch (that.selectMultiple) {
        case false: // Single.
            that.options.selected = [];
            that.options.selected.push(key);
            selectionBasket.html(buildSingleSelectionSnippet(that, entity, true));

            // Assign click event.
            selectionBasket.find("a").click(function () {
                that.options.selected = [];
                selectionBasket.html(buildSingleSelectionSnippet(that, entity, false));
                deselectEntity(that, $(this).attr("key"));
            });//end:click.
            break;
        case true: // Multiple.
            // If 'key' does not exist within 'selected' arrary.
            if ($.inArray(key, that.options.selected) < 0) {

                // Add the key to our selected list.
                that.options.selected.push(key);

                // Add an element to the user-visible select list.
                li = $('<li><a href="javascript:;" key="' + entity.entityType + ":" + entity.id + '">' + entity.name + '</a><input type="hidden" name="groups" value="' + entity.entityType + ":" + entity.id + '"/></li>');

                // Append li to selectionBasket.
                selectionBasket.find("ul").append(li);

                // Assign click event.
                li.find("a").click(function () {
                    deselectEntity(that, $(this).attr("key"));
                });//end:click.
            }//end:if.
            break;
        }//end:switch.
        
        updateSelectionStates(that);

        // Enable submit.
        buttonPrimary.removeAttr("disabled");
    };//end:function.
    
    /**
     * Private. Remove breadcrumb from breadcrumb lists.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} anchor - reference to <a> element.
     @ @param {Function} browseFn - function to associate with anchor
     */
    var removeBreadCrumb = function (that, anchor, browseFn) {
        var crumb, next;
        
        // Cache.
        crumb = anchor.parent();
        next = crumb.nextAll();
        
        // Remove all crumbs to the right of the 'clicked' crumb.
        next.remove();
        
        // Render view associated with the 'clicked' crumb.
        browseFn(that, anchor.attr("key"));
    };//end:function.
    
    /**
     * Private. Builds mark-up string for breadCrumb.
     * 
     * @param {Object} key - reference to entity key.
     * @param {Object} entityName - reference to entity name.
     * @param {Object} breadcrumbs - DOM object of breadcrumbs
     @ @param {Function} browseFn - function to associate with anchor
     */
    var buildBreadCrumb = function (that, key, entityName, breadcrumbs, browseFn) {
        var breadcrumb;
        breadcrumb = '<span><a href="javascript:;" title="' + entityName + '" key="' + key + '">' + entityName + '</a> &gt; </span>';
        breadcrumbs.append(breadcrumb);
        
        // Breadcrumb click event.
        breadcrumbs.find("a").unbind("click").click(function () {
            removeBreadCrumb(that, $(this), browseFn);
        });//end:click.
    };//end:function.
    
    /**
     * Private. Update the breadcrumb trail.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} entity - reference to the entity object.
     * @param {String} breadcrumbsSel - selector name of breadcrumb
     @ @param {Function} browseFn - function to associate with anchor
     */
    var updateBreadcrumbs = function (that, entity, breadcrumbsSel, browseFn) {
        var breadcrumbs, key, isKey;
        
        // Cache.
        breadcrumbs = that.locate(breadcrumbsSel);
        key = entity.entityType + ':' + entity.id;
        
        // Add breadcrumb.
        if (breadcrumbs.find('span').length > 0) {
            // Breadcrumbs do exist.
            isKey = (breadcrumbs.find('span a[key="' + key + '"]').length > 0) ? true : false;
            if (!isKey) {
                buildBreadCrumb(that, key, entity.name, breadcrumbs, browseFn);
            }//end:if.
            
        } else {
            // No breadcrumbs exist.
            buildBreadCrumb(that, key, entity.name, breadcrumbs, browseFn);
        }//end:if.
        
        // Add the '.last' class name to the last availble breadcrumb.
        breadcrumbs.find("." + that.options.styles.last).removeClass(that.options.styles.last).css("visibility", "visible");
        breadcrumbs.find("span:last").addClass(that.options.styles.last).css("visibility", "hidden");
    };//end:function.

    /**
     * Private. Browse to a particular entity.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to currently 'focused' entity. ex: group:local.17
     */
    var browseEntity = function (that, key) {
    	console.log("browse");
        var entity, currentEntityName, browsingInclude, entityBrowserContent, memberList;
        
        // Cache.
        entity = that.registry.getEntity(getTypeFromKey(key), getKey(key));
        that.currentEntity = entity;
        currentEntityName = that.locate("currentEntityName");
        browsingInclude = that.locate("browsingInclude");
        entityBrowserContent = that.locate("entityBrowserContent");
        memberList = entityBrowserContent.find("." + that.options.styles.memberList);
        
        // Set entity starting point / defaults.
        updateBreadcrumbs(that, entity, "breadcrumbs", browseEntity);
        currentEntityName.text(entity.name);
        currentEntityName.attr("key", entity.entityType + ":" + entity.id);
        browsingInclude.text(entity.name);
        memberList.html("").hide();
        
        // For each entity, create a member list.
        $.each(entity.children, function (idx, obj) {
            var li, list;
            li = '<li class="' + obj.entityType + '"><a href="javascript:;"><span class="' + that.options.styles.memberLink + '" key="' + obj.entityType + ':' +  obj.id + '">' + obj.name + '</span></a></li>';
            list = entityBrowserContent.find("." + obj.entityType.toLowerCase()).find("." + that.options.styles.memberList);
            list.append(li);
            list.show();
        });//end:loop.
        that.container.find(".no-members").each(function (idx, obj) {
        	obj = $(obj);
        	if (obj.parent().find("li").size() > 0) {
        		obj.hide();
        	} else {
        		obj.show();
        	}
        });
        
        // Register click event on member list links.
        //entityBrowserContent.find("." + that.options.styles.memberLink).click(function () {
        entityBrowserContent.find("a").click(function () {
            browseEntity(that, $(this).find("span").attr("key"));
        });//end:click.
        
        // Set selection states.
        updateSelectionStates(that);
    };//end:function.

    /**
     * Private. Browse to a particular ad hoc group.
     *
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to currently 'focused' group key. ex: group:pags.Ad%20Hoc%20Groups
     */
    var browseAdHocGroup = function (that, key) {
    	console.log("browse ad hoc");
        var entity, currentAdHocGroupName, memberTable;

        // Cache.
        entity = that.registry.getEntity(getTypeFromKey(key), getKey(key));
        that.currentAdHocGroup = entity;

        // Update title and breadcrumbs.
        currentAdHocGroupName = that.locate("currentAdHocGroupName");
        currentAdHocGroupName.text(entity.name);
        currentAdHocGroupName.attr("key", entity.entityType + ":" + entity.id);
        updateBreadcrumbs(that, entity, "adHocBreadcrumbs", browseAdHocGroup);

        // Clear member table.
        memberTable = that.locate("adHocMemberList");
        memberTable.html("").hide();

        // For each entity, create a member list item.
        $.each(entity.children, function (idx, obj) {
            var tdChild, tdButtons, divButtons, selButton, selIcon, tr, table, img, a;

            // Create folder image.
            img = document.createElement("img");
            img.alt = "Folder icon";
            img.src = "/ResourceServingWebapp/rs/famfamfam/silk/1.3/folder.png";

            // Create entity name/link.
            a = document.createElement('a');
            a.href = "javascript:;";
            a.id = obj.entityType + ":" + obj.id;
            a.text = obj.name;

            // Create entity td.
            tdChild = document.createElement("td");
            tdChild.appendChild(img);
            tdChild.appendChild(a);

            // Create buttons, div and td.
            selButton = document.createElement('button');
            selButton.className = "btn btn-select btn-success btn-xs";
            selButton.appendChild(document.createTextNode("Add to Selection "));
            selIcon = document.createElement('i');
            selIcon.className = "fa fa-plus-circle";
            selButton.appendChild(selIcon);
            divButtons = document.createElement('div');
            divButtons.className = "btn-group pull-right";
            divButtons.role = "group";
            divButtons.appendChild(selButton);
            tdButtons = document.createElement('td');
            tdButtons.appendChild(divButtons);
            /*
                 <button type="button" class="btn btn-info btn-xs">Edit Group <i class="fa fa-pencil"></i></button>
                 <button type="button" class="btn btn-danger btn-xs">Delete Group <i class="fa fa-trash-o"></i></button>
             */

            // Create row and add to table.
            tr = document.createElement("tr");
            tr.appendChild(tdChild);
            tr.appendChild(tdButtons);
            table = that.locate("adHocMemberList");
            table.append(tr);
            table.show();
        });//end:loop.

        // Reset no-members.
        that.container.find(".no-members").each(function (idx, obj) {
        	obj = $(obj);
        	if (obj.parent().find("tr").size() > 0) {
        		obj.hide();
        	} else {
        		obj.show();
        	}
        });

        // Register click event on member list links.
        memberTable.find("a").click(function () {
            browseAdHocGroup(that, $(this).attr("id"));
        });//end:click.

        // Set selection states.
        updateSelectionStates(that);
    };//end:function.

    /**
     * Private. Renders 'selected' search items to the end user.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     */
    var updateSearchView = function (that) {
        var list;
        
        // Cache.
        list = that.searchDropDown.find(that.options.selectors.searchResults);
        list.find("." + that.options.styles.selected).removeClass(that.options.styles.selected);
        
        // Loop through selected array.
        $.each(that.options.selected, function (idx, obj) {
            var span = list.find('span[key="' + obj + '"]');
            span.parent().parent().addClass(that.options.styles.selected);
        });//end:loop.
    };//end:function.
    
    /**
     * Private. Determines which action, selection or deselection, executes.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to key attribute passed over when search link is clicked.
     */
    var itemSelectionHandler = function (that, key) {
        // Cache.
        var entity = that.registry.getEntity(getTypeFromKey(key), getKey(key));
        
        // Selection.
        if ($.inArray(key, that.options.selected) !== -1) {
            // Key exists.
            deselectEntity(that, entity.entityType + ":" + entity.id);
        } else {
            // Key does not exist.
            selectEntity(that, entity.entityType + ":" + entity.id);
        }//end:if.
        
        // Update UI.
        updateSearchView(that);
    };//end:function.
    
    /**
     * Private. Search for a specific entity.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} searchTerm - reference to search term.
     */
    var search = function (that, searchTerm, form) {
        var entities, list, listItem, searchResultsNoMembers, members;
        
        // Filter searchTerm.
        if (searchTerm === that.options.messages.searchValue) {
            searchTerm = "";
        }//end:if.
        
        // Cache.
        entities = that.registry.searchEntities(that.options.entityTypes, searchTerm);
        list = that.searchDropDown.find(that.options.selectors.searchResults);
        searchResultsNoMembers = that.locate("searchResultsNoMembers");
        list.html("");
        listItem = "";
        
        // Loop through each entity. Build list items.
        $.each(entities, function (idx, obj) {
            listItem += '<li class="' + obj.entityType + '"><a href="javascript:;" title="' + obj.name + '"><span key="' + obj.entityType + ':' + obj.id + '">' + obj.name + '</span></a></li>';
        });//end:loop.
        list.html(listItem);
        
        // Assign default 'click' event.
        list.find("a").bind("click", function () {
            var span = $(this).find("span");
            itemSelectionHandler(that, span.attr("key"));
        });//end:listener.
        
        // Render 'No Members' when list is empty.
        members = list.find("li");
        if (members.length < 1) {
            searchResultsNoMembers.show();
            list.hide();
        } else {
        	list.show();
        	searchResultsNoMembers.hide();
        }
        //end:if.
        
        // Update UI.
        updateSearchView(that);
        
        return false;
    };//end:function.
    
    /**
     * Private. Initializes search feature.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     */
    var searchEntity = function (that) {
        var portletSearch, closeSearch, searchForm, searchField, searchDropDown, loader;
        
        // Cache.
        closeSearch = that.locate("closeSearch");
        searchForm = that.locate("searchForm");
        portletSearch = searchForm.parent();
        searchField = searchForm.find("input[type=text]");
        loader = that.locate("searchLoader");
        searchDropDown = that.locate("searchDropDown");
        
        // Apply.
        searchField.val(that.options.messages.searchValue);
        searchDropDown.css({'top': searchField.outerHeight()});
        
        // Binds 'click' & 'focus' events to input field.
        searchField.bind("click focus", function () {
            $(this).val("");
        });//end:listener.
        
        // Binds 'submit' event.
        searchForm.submit(function () {
            up.showLoader(loader);
            search(that, searchField.val(), searchForm);
            searchDropDown.show();
            up.hideLoader(loader);
            return false;
        });//end:listener.
        
        // Binds 'click' event to close button.
        closeSearch.bind("click", function () {
            searchDropDown.hide();
        });//end:listener.
        
        // Binds 'click' event listener to the document. Detects a 'click'
        // that occurs outside of the component.
        $(document).bind("click", function (e) {
            if (that.isEmptyArray($(e.target).parents("." + that.options.styles.search))) {
                searchDropDown.hide();
            }//end:if.
        });//end:function.
    };//end:function.
    
    /**
     * Private. Runs initialization functions.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     */
    var initialize = function (that) {
        // Initialize search drop-down.
        searchEntity(that);

        that.locate("selectionBasket").find("a").click(function () {
            deselectEntity(that, $(this).attr("key"));
        });//end:click.

        // Browse to the designated default start entity.
        browseEntity(that, that.options.initialFocusedEntity);

        // Browse to the designated start of ad hoc groups.
        browseAdHocGroup(that, that.options.initialFocusedEntity);
        
        // Disable primary button.
        if (that.options.selected.length < 1 && that.options.requireSelection) {
            that.locate("buttonPrimary").attr("disabled", "disabled");
        }//end:if.

    };//end:function.
    
    /**
     * Creator function for the entityselection component.
     * 
     * @param {Object} container - reference to DOM container.
     * @param {Object} options - reference to configuration object.
     */
    up.entityselection = function (container, options) {
        var that;
        
        // Initialize component & cache globals.
        that = fluid.initView("up.entityselection", container, options);
        that.selectMultiple = that.options.selectMultiple;
        that.searchDropDown = that.locate("searchDropDown");

        that.registry = fluid.initSubcomponent(that, "entityRegistry", [container, fluid.COMPONENT_OPTIONS]);
        
        /**
         * Public. Checks passed array's length property.
         * If the length of the array is 0 the array is
         * empty. Returns true if the array is empty.
         * 
         * @param {Object} arr - reference to passed array object.
         */
        that.isEmptyArray = function (arr) {
            return ((arr.length > 0) ? false : true);
        };//end:function.
        
        initialize(that);
        return that;
    };//end:component.
    
    // Defaults.
    fluid.defaults("up.entityselection", {
        entityRegistry: {
            type: "up.EntityRegistry"
        },
        entityTypes: [],
        selected: [],
        initialFocusedEntity: 'group:local.0',
        initialAdHocEntity: 'group:pags.Ad%20Hoc%20Groups',
        selectMultiple: true,
        requireSelection: true,
        selectors: {
            selectionBasket: "#selectionBasket",
            breadcrumbs: "#entityBrowsingBreadcrumbs",
            currentEntityName: "#currentEntityName",
            selectEntityLink: "#selectEntityLink",
            entityBrowserContent: "#entityBrowserContent",
            entityBrowserTitlebar: "#entityBrowserTitlebar",
            browsingInclude: "#browsingInclude",
            closeSearch: "closeDropDown",
            searchForm: "#searchForm",
            searchDropDown: "#searchDropDown",
            searchResults: "#searchResults",
            searchResultsNoMembers: "#searchResultsNoMembers",
            searchLoader: "searchLoader",
            currentAdHocGroupName: "#currentAdHocGroupName",
            adHocCreate: "#adHocCreate",
            adHocBreadcrumbs: "#adHocBreadcrumbs",
            adHocMemberList: "#adHocMemberList",
            buttonPanel: "#buttonPanel",
            buttonPrimary: "#buttonPrimary"
        },
        styles: {
            memberList: "member-list",
            memberLink: "member-link",
            adHocMemberList: "ad-hoc-member-list",
            selection: "selection",
            selected: "selected",
            last: "last",
            title: "title",
            search: "portlet-search"
        },
        messages: {
            selectButtonMessage: '',
            deselectButtonMessage: '',
            removeCrumb: '',
            removeSelection: '',
            addSelection: '',
            selected: '',
            nothingSelected: '',
            searchValue: ''
        }
    });
})(jQuery, fluid);