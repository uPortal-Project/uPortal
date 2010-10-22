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

var up = up || {};

(function($, fluid){
    
    var getTypeFromKey = function(key) {
        var separatorIndex = key.indexOf(":");
        return key.substring(0, separatorIndex);
    };
    
    var getKey = function(key) {
        var separatorIndex = key.indexOf(":");
        return key.substring(separatorIndex + 1, key.length);
    };
    
    /**
     * Browse to a particular entity.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} key - reference to currrently 'focused' entity. ex: group:local.17
     */
    var browseEntity = function(that, key) {
        var entity = that.entityBrowser.getEntity(getTypeFromKey(key), getKey(key));
        that.currentEntity = entity;
        
        updateBreadcrumbs(that, entity);
        $(".current").text(entity.name);
        
        // Clear any content from the entity browser members section.
        $(that.options.entityTypes).each(function(){ $("." + this + " .member-list").html(""); });
        
        // For each entity, create a list element in the correct section.
        $(entity.children).each(function(i){
            var link = $(document.createElement("a")).attr("href", "javascript:;")
                .html("<span>" + this.name + "</span>").attr("key", this.entityType + ":" + this.id)
                .click(function(){ browseEntity(that, $(this).attr("key")); });
            $("." + this.entityType + " .member-list").append(
                $(document.createElement("li")).addClass(this.entityType).append(link)
            );
        });
        
        // Hide any sections with no members.
        $(that.options.entityTypes).each(function(){
            $("." + this + " .member-list").prev().css("display", $("." + this + " .member-list li").size() > 0 ? "block" : "none" );
        });
        
        // If there are no members overall, display the no contents message.
        that.locate("browsingResultNoMembers").css("display", $(".browse-hierarchy .member-list li").size() > 0 ? "none" : "block");
        
        // Set the breadcrumbs state.
        setBreadcrumbSelectionState(that, $.inArray(key, that.options.selected) >= 0);
    };//end:function.
    
    /**
     * Search for a specific entity
     */
    var search = function(that, searchTerm) {
        var entities = that.entityBrowser.searchEntities(that.options.entityTypes, searchTerm);
        var list = $(that.options.selectors.searchResults).html("");
        $(entities).each(function(){
            var link = $(document.createElement("a")).attr("href", "javascript:;")
                .html("<span>" + this.name + "</span>").attr("key", this.entityType + ":" + this.id)
                .click(function(){ selectEntity(that, $(this).attr("key")); $(this).addClass("selected"); });
            list.append($(document.createElement("li")).addClass(this.entityType).append(link));
        });
        
        // if there are no members overall, display the no contents message
        $(that.options.selectors.searchResultsNoMembers).css("display", list.find("li").size() > 0 ? "none" : "block");

        if (that.searchInitialized) {
            $(that.options.selectors.searchDialog).dialog('open');
        } else { 
            that.locate("searchDialog").dialog({ width:550, modal:true });
            that.searchInitialized = true;
        }
        return false;
    };
    
    /**
     * Add an entity to the selected list.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} key - reference to currrently 'selected' entity. ex: group:local.17
     */
    var selectEntity = function(that, key) {
        var selectionBasket, entity, li;
        
        // Cache DOM elements.
        selectionBasket = that.locate("selectionBasket");
        entity = that.entityBrowser.getEntity(getTypeFromKey(key), getKey(key));
        
        // Check component selection mode.
        if (that.selectionMode === "single") {
            // Remove all <li> elements that exist within selection basket.
            if (selectionBasket.children().length > 0) {
                selectionBasket.children().remove();
            }//end:if.
            
            if (that.options.selected.length > 0) {
                that.options.selected = [];
            }//end:if.
        }//end:if.
        
        // If 'key' does not exist within 'selected' arrary.
        if ($.inArray(key, that.selected) < 0) {
            // Add the key to our selected list.
            that.options.selected.push(key);
            
            // Add an element to the user-visible select list.
            li = $('<li><a href="javascript:;" key="' + entity.entityType + ":" + entity.id + '">' + entity.name + '</a></li>' + 
                 '<input type="hidden" name="groups" value="' + entity.entityType + ":" + entity.id + '"/>');
            
            // Append li to selectionBasket.
            that.locate("selectionBasket").append(li);
            
            // Assign click event.
            li.find("a").click(function () {
                deselectEntity(that, $(this).attr("key"));
            });//end:click.
            
            // If the selected item is the currently selected entity, update
            // the breadcrumb selection state.
            if (key === (that.currentEntity.entityType + ":" + that.currentEntity.id)) {
                setBreadcrumbSelectionState(that, true);
            }//end:if.
        }//end:if.
    };//end:function.
    
    /**
     * Remove an entity from the selection list.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Object} key - reference to passed anchor tag attribute. ex: group:local.17
     */
    var deselectEntity = function(that, key) {
        var selectionBasket, newselections;
        
        // Cache & reset DOM references.
        selectionBasket = that.locate("selectionBasket");
        newselections = [];
        
        // Generate a new list of selected entities, and remove the requested
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
        
        
        // If the selected item is the currently selected entity, update
        // the breadcrumb selection state.
        if (key === (that.currentEntity.entityType + ":" + that.currentEntity.id)) {
            setBreadcrumbSelectionState(that, false);
        }//end:if.
    };//end:function.
    
    /**
     * Update the visual selection state for the currently-browsed entity.
     * 
     * @param {Object} that - reference to an instance of the up.entityselection component.
     * @param {Boolean} selected - reference to breadcrumb toggle. 
     */
    var setBreadcrumbSelectionState = function(that, selected) {
        var link, header;
        
        // Cache & reset.
        link = that.locate("selectEntityLink");
        link.unbind("click");
        header = that.locate("entityBrowsingHeader");
        
        // Select/deselect currently browsed entity.
        if (!selected) {
            link.click(function () {
                selectEntity(that, that.currentEntity.entityType + ":" + that.currentEntity.id);
            }).find("span").text(that.options.selectButtonMessage);
            header.removeClass("selected");
        } else {
            link.click(function () {
                deselectEntity(that, that.currentEntity.entityType + ":" + that.currentEntity.id);
            }).find("span").text(that.options.deselectButtonMessage);
            header.addClass("selected");
        }//end:if.
    };//end:function.
    
    /**
     * Update the breadcrumb trail
     */
    var updateBreadcrumbs = function(that, entity) {
        // update the current title
        var currentTitle = that.locate("currentEntityName");
        
        var breadcrumbs = that.locate("breadcrumbs");
        if (breadcrumbs.find("span a[key=" + entity.entityType + ":" + entity.id + "]").size() > 0) {
            // if this entity already exists in the breadcrumb trail
            var removeBreadcrumb = false;
            $(breadcrumbs.find("span")).each(function(){
                if (removeBreadcrumb) { 
                    $(this).remove(); 
                } else if ($(this).find("a[key=" + entity.entityType + ":" + entity.id + "]").size() > 0) { 
                    removeBreadcrumb = true;
                    $(this).remove();
                }
            });
        } else {
            // otherwise, append this entity to the end of the breadcrumbs
            if (currentTitle.text() != '') {
                var breadcrumb = $(document.createElement("span"));
                breadcrumb.append(
                    $(document.createElement("a")).html(currentTitle.text())
                        .attr("href", "javascript:;").attr("key", currentTitle.attr("key"))
                        .click(function(){browseEntity(that, $(this).attr("key"));})
                ).append(document.createTextNode(" > "));
                that.locate("breadcrumbs").append(breadcrumb);
            }
        }
        currentTitle.text(entity.name).attr("key", entity.entityType + ":" + entity.id);
    };
    
    /**
     * Creator function for the entityselection component.
     * 
     * @param {Object} container - reference to DOM container.
     * @param {Object} options - reference to configuration object.
     */
    up.entityselection = function(container, options) {
        var that, selectionBasket, searchForm;
        
        // Initialize & cache.
        that = fluid.initView("up.entityselection", container, options);
        that.selectionMode = that.options.selectionMode;
        selectionBasket = that.locate("selectionBasket");
        searchForm = that.locate("searchForm");
        
        // Assign a new entity browser for retrieving groups, categories, and person
        // information from the portal.
        that.entityBrowser = $.groupbrowser({
            findEntityUrl: that.options.findEntityUrl,
            searchEntitiesUrl: that.options.searchEntitiesUrl
        });
        
        // Initialize selection basket onclick.
        that.searchInitialized = false;
        selectionBasket.find("a").click(function () {
            deselectEntity(that, $(this).attr("key"));
        });
        
        // Initialize search form submit.
        searchForm.submit(function () {
            return search(that, this.searchterm.value);
        }).find("input[name=searchterm]").focus(function () {
            $(this).val("");
            $(this).unbind("focus");
        });
        
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
        selectButtonMessage: '',
        deselectButtonMessage: '',
        selectionMode: 'multiple',
        selectors: {
            selectionBasket: "#selectionBasket",
            breadcrumbs: "#entityBrowsingBreadcrumbs",
            currentEntityName: "#currentEntityName",
            selectEntityLink: "#selectEntityLink",
            entityBrowsingHeader: "#entityBrowsingHeader",
            browsingResultNoMembers: "#browsingResultNoMembers",
            searchForm: "#searchForm",
            searchDialog: "#searchDialog",
            searchResults: "#searchResults",
            searchResultsNoMembers: "#searchResultsNoMembers"
        }
    });
})(jQuery, fluid);