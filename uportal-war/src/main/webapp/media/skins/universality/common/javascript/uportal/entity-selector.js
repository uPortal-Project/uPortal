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
     * Outputs selection markup snippet.
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
     * Update the visual selection state for the currently-browsed entity.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Boolean} selected - reference to selection toggle. 
     */
    var setSelectionState = function (that, key, selected) {
        var selectionBasket, titlebar, link, entity;
        
        // Cache.
        selectionBasket = that.locate("selectionBasket");
        titlebar = that.locate("entityBrowserTitlebar");
        link = that.locate("selectEntityLink");
        link.unbind("click");
        entity = that.entityBrowser.getEntity(getTypeFromKey(key), getKey(key));
        
        // Select/deselect currently browsed entity.
        if (!selected) {
            // Selection State: false. When clicked, select.
            link.bind("click", function () {
                link.attr("title", that.options.messages.removeSelection).find("span").text(that.options.messages.removeSelection);
                selectEntity(that, that.currentEntity.entityType + ":" + that.currentEntity.id);
            });
            titlebar.removeClass("selected");
        } else {
            // Selection State: true. When clicked, deselect.
            link.bind("click", function () {
                link.attr("title", that.options.messages.addSelection).find("span").text(that.options.messages.addSelection);
                deselectEntity(that, that.currentEntity.entityType + ":" + that.currentEntity.id);
            });
            titlebar.addClass("selected");
        }//end:if
    };//end:function.
    
    /**
     * Remove an entity from the selection list.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to passed anchor tag attribute. ex: group:local.17
     */
    var deselectEntity = function (that, key) {
        var selectionBasket, entity, buttonPrimary, newselections;
        
        // Cache & reset DOM references.
        selectionBasket = that.locate("selectionBasket");
        buttonPrimary = that.locate("buttonPrimary");
        entity = that.entityBrowser.getEntity(getTypeFromKey(key), getKey(key));
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
        
        // If the selected item is the currently selected entity, update
        // the selection state.
        if (key === (that.currentEntity.entityType + ":" + that.currentEntity.id)) {
            setSelectionState(that, key, false);
        }//end:if.
        
        // Enable submit.
        if (that.options.selected.length < 1) {
            buttonPrimary.attr("disabled", "disabled");
        }//end:if.
    };//end:function.
    
    /**
     * Add an entity to the selected list.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to currrently 'selected' entity. ex: group:local.17
     */
    var selectEntity = function (that, key) {
        var selectionBasket, buttonPrimary, li, entity;
        
        // Cache DOM elements.
        selectionBasket = that.locate("selectionBasket");
        buttonPrimary = that.locate("buttonPrimary");
        entity = that.entityBrowser.getEntity(getTypeFromKey(key), getKey(key));
        
        // Check component selection mode.
        switch (that.selectMultiple) {
        case false: // Single.
            that.options.selected = [];
            that.options.selected.push(key);
            selectionBasket.html(buildSingleSelectionSnippet(that, entity, true));
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
        
        // If the selected item is the currently selected entity, update
        // the selection state.
        if (key === (that.currentEntity.entityType + ":" + that.currentEntity.id)) {
            setSelectionState(that, key, true);
        }//end:if.
        
        // Enable submit.
        buttonPrimary.removeAttr("disabled");
    };//end:function.
    
    /**
     * Remove breadcrumb from breadcrumb list.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} anchor - reference to <a> element.
     */
    var removeBreadCrumb = function (that, anchor) {
        var crumb, next;
        
        // Cache.
        crumb = anchor.parent();
        next = crumb.nextAll();
        
        // Remove all crumbs to the right of the 'clicked' crumb.
        next.remove();
        
        // Render view associated with the 'clicked' crumb.
        browseEntity(that, anchor.attr("key"));
    };//end:function.
    
    /**
     * Builds mark-up string for breadCrumb.
     * 
     * @param {Object} key - reference to entity key.
     * @param {Object} entityName - reference to entity name.
     */
    var buildBreadCrumb = function (that, key, entityName, breadcrumbs) {
        var breadcrumb;
        breadcrumb = '<span><a href="javascript:;" title="' + entityName + '" key="' + key + '">' + entityName + '</a> &gt; </span>';
        breadcrumbs.append(breadcrumb);
        
        // Breadcrumb click event.
        breadcrumbs.find("a").unbind("click").click(function () {
            removeBreadCrumb(that, $(this));
        });//end:click.
    };//end:function.
    
    /**
     * Update the breadcrumb trail.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} entity - reference to the entity object.
     */
    var updateBreadcrumbs = function (that, entity) {
        var breadcrumbs, key, isKey;
        
        // Cache.
        breadcrumbs = that.locate("breadcrumbs");
        key = entity.entityType + ':' + entity.id;
        
        // Add breadcrumb.
        if (breadcrumbs.find('span').length > 0) {
            // Breadcrumbs do exist.
            isKey = (breadcrumbs.find('span a[key="' + key + '"]').length > 0) ? true : false;
            if (!isKey) {
                buildBreadCrumb(that, key, entity.name, breadcrumbs);
            }//end:if.
            
        } else {
            // No breadcrumbs exist.
            buildBreadCrumb(that, key, entity.name, breadcrumbs);
        }//end:if.
        
        // Add the '.last' class name to the last availble breadcrumb.
        breadcrumbs.find("." + that.options.styles.last).removeClass(that.options.styles.last).show();
        breadcrumbs.find("span:last").addClass(that.options.styles.last).hide();
    };//end:function.
    
    /**
     * Browse to a particular entity.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} key - reference to currrently 'focused' entity. ex: group:local.17
     */
    var browseEntity = function (that, key) {
        var entity, currentEntityName, browsingInclude, entityBrowserContent, memberList, browsingResultNoMembers;
        
        // Cache.
        entity = that.entityBrowser.getEntity(getTypeFromKey(key), getKey(key));
        that.currentEntity = entity;
        currentEntityName = that.locate("currentEntityName");
        browsingInclude = that.locate("browsingInclude");
        entityBrowserContent = that.locate("entityBrowserContent");
        memberList = entityBrowserContent.find("." + that.options.styles.memberList);
        browsingResultNoMembers = that.locate("browsingResultNoMembers");
        
        // Set entity starting point / defaults.
        updateBreadcrumbs(that, entity);
        currentEntityName.text(entity.name);
        browsingInclude.text(entity.name);
        memberList.html("").hide();
        
        // For each entity, create a member list.
        $.each(entity.children, function (idx, obj) {
            var li, list;
            li = '<li class="' + obj.entityType + '"><a href="javascript:;"><span class="' + that.options.styles.memberLink + '" key="' + obj.entityType + ':' +  obj.id + '">' + obj.name + '</span></a></li>';
            list = entityBrowserContent.find("." + obj.entityType).find("." + that.options.styles.memberList);
            list.append(li);
            list.show();
        });//end:loop.
        
        // Register click event on member list links.
        entityBrowserContent.find("." + that.options.styles.memberLink).click(function () {
            browseEntity(that, $(this).attr("key"));
        });//end:click.
        
        // If there are no members overall, display the no contents message.
        browsingResultNoMembers.css("display", entityBrowserContent.find("." + that.options.styles.memberList).find("li").size() > 0 ? "none" : "block");
        
        // Set selection state.
        setSelectionState(that, key, $.inArray(key, that.options.selected) >= 0);
    };//end:function.
    
    /**
     * Search for a specific entity.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {String} searchTerm - reference to search term.
     */
    var search = function (that, searchTerm, form) {
        var entities, list;
        
        // Filter searchTerm.
        if (searchTerm === that.options.messages.searchValue) {
            form.find("input[type=text]").val("");
            searchTerm = "";
        }//end:if.
        
        // Cache.
        entities = that.entityBrowser.searchEntities(that.options.entityTypes, searchTerm);
        list = that.searchPanel.find(that.options.selectors.searchResults);
        list.html("");
        
        // Loop through each entity. Build list items.
        $.each(entities, function (idx, obj) {
            var item;
            
            item = '<li class="' + obj.entityType + '"><a href="javascript:;" title="' + obj.name + '"><span key="' + obj.entityType + ':' + obj.id + '">' + obj.name + '</span></a></li>';
            list.append(item);
        });//end:loop.
        
        // Assign click event.
        list.find("span").bind("click", function () {
            selectEntity(that, $(this).attr("key"));
            $(this).parent().addClass(that.options.styles.selected);
        });//end:click.
        
        // Dialog.
        that.searchPanel.dialog('open');
        that.searchInitialized = true;
        
        return false;
    };//end:function.
    
    /**
     * Creator function for the entityselection component.
     * 
     * @param {Object} container - reference to DOM container.
     * @param {Object} options - reference to configuration object.
     */
    up.entityselection = function (container, options) {
        var that, searchForm, buttonPrimary;
        
        // Initialize & cache.
        that = fluid.initView("up.entityselection", container, options);
        that.selectMultiple = that.options.selectMultiple;
        that.searchPanel = that.locate("searchDialog");
        searchForm = that.locate("searchForm");
        buttonPrimary = that.locate("buttonPrimary");
        
        // Assign a new entity browser for retrieving groups, categories, and person
        // information from the portal.
        that.entityBrowser = $.groupbrowser({
            findEntityUrl: that.options.findEntityUrl,
            searchEntitiesUrl: that.options.searchEntitiesUrl
        });
        
        // Initialize search dialog.
        that.searchInitialized = false;
        that.searchPanel.dialog({
            autoOpen: false,
            width: 550,
            modal: true,
            dialogClass: that.options.styles.dialogClass
        });
        
        // Initialize search form.
        searchForm.find("input[type=text]").val(that.options.messages.searchValue);
        searchForm.submit(function () {
            return search(that, this.searchterm.value, searchForm);
        });
        
        // Search form focus event.
        searchForm.find("input[name=searchterm]").focus(function () {
            $(this).val("");
            $(this).unbind("focus");
        });
        
        // Disable primary button.
        buttonPrimary.attr("disabled", "disabled");
        
        // Browse to the designated default start entity.
        browseEntity(that, that.options.initialFocusedEntity);
        
        return that;
    };//end:component.
    
    // Defaults.
    fluid.defaults("up.entityselection", {
        entityTypes: [],
        selected: [],
        findEntityUrl: "mvc/findEntity",
        searchEntitiesUrl: "mvc/searchEntities",
        initialFocusedEntity: 'group:local.0',
        selectMultiple: true,
        selectors: {
            selectionBasket: "#selectionBasket",
            breadcrumbs: "#entityBrowsingBreadcrumbs",
            currentEntityName: "#currentEntityName",
            selectEntityLink: "#selectEntityLink",
            entityBrowserContent: "#entityBrowserContent",
            entityBrowserTitlebar: "#entityBrowserTitlebar",
            browsingInclude: "#browsingInclude",
            browsingResultNoMembers: "#browsingResultNoMembers",
            searchForm: "#searchForm",
            searchDialog: "#searchDialog",
            searchResults: "#searchResults",
            searchResultsNoMembers: "#searchResultsNoMembers",
            buttonPanel: "#buttonPanel",
            buttonPrimary: "#buttonPrimary"
        },
        styles: {
            memberList: "member-list",
            memberLink: "member-link",
            dialogClass: "portlet",
            selection: "selection",
            selected: "selected",
            last: "last"
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