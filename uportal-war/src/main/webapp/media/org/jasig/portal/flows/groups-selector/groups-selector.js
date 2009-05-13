var uportal = uportal || {};

(function($, fluid){

    /**
     * Browse to a particular entity.
     */
    var browseEntity = function(that, key) {
        var entity = that.entityBrowser.getEntity(that.options.entityTypes, key);
        that.currentEntity = entity;
        
        updateBreadcrumbs(that, entity);
        $(".current-entity-name").text(entity.name);
        
        // clear any content from the entity browser members section
        $(that.options.entityTypes).each(function(){ $("." + this + "-member-list").html(""); });
        
        // for each entity, create a list element in the correct section 
        $(entity.children).each(function(i){
            var link = $(document.createElement("a")).attr("href", "javascript:;")
                .html("<span>" + this.name + "</span>").attr("key", this.id)
                .click(function(){ browseEntity(that, $(this).attr("key")); });
            $("." + this.entityType + "-member-list").append(
                $(document.createElement("li")).addClass(this.entityType).append(link)
            );
        });
        
        // hide any sections with no members
        $(that.options.entityTypes).each(function(){
            $("." + this + "-member-list").prev().css("display", $("." + this + "-member-list li").size() > 0 ? "block" : "none" );
        });
        
        // if there are no members overall, display the no contents message
        that.locate("browsingResultNoMembers").css("display", $(".portlet-browse-body li").size() > 0 ? "none" : "block");
        
        // set the breadcrumbs state
        setBreadcrumbSelectionState(that, $.inArray(key, that.options.selected) >= 0);
    };

    /**
     * Search for a specific entity
     */
    var search = function(that, searchTerm) {
        var entities = that.entityBrowser.searchEntities(that.options.entityTypes, searchTerm);
        var list = that.locate("searchResults").html("");
        $(entities).each(function(){
            var link = $(document.createElement("a")).attr("href", "javascript:;")
                .html("<span>" + this.name + "</span>").attr("key", this.id)
                .click(function(){ selectEntity(that, $(this).attr("key")); $(this).addClass("selected"); });
            list.append($(document.createElement("li")).addClass(this.entityType).append(link));
        });
        
        // if there are no members overall, display the no contents message
        that.locate("browsingResultNoMembers").css("display", list.find("li").size() > 0 ? "block" : "none");

        if (that.searchInitialized) {
            that.locate("searchDialog").dialog('open');
        } else { 
            that.locate("searchDialog").dialog({ width:550, modal:true });
            that.searchInitialized = true;
        }
        return false;
    };

    /**
     * Add an entity to the selected list
     */
    var selectEntity = function(that, key) {
        if ($.inArray(key, that.options.selected) < 0) {
            // add the key to our selected list
            that.options.selected.push(key);
            var entity = that.entityBrowser.getEntity(that.options.entityTypes, key);
            
            // add a element to the user-visible select list
            var li = $(document.createElement("li"));
            li.append($(document.createElement("a")).html(entity.name)
                .attr("href", "javascript:;").attr("key", entity.id)
                .click(function(){ deselectEntity(that, $(this).attr("key")); }));
            li.append($(document.createElement("input")).attr("type", "hidden")
                .attr("name", "groups").val(entity.id));
            that.locate("selectionBasket").append(li);
            
            // if the selected item is the currently selected entity, update
            // the breadcrumb selection state
            if (key == that.currentEntity.id) setBreadcrumbSelectionState(that, true);
        }
    };
    
    /**
     * Remove an entity from the selection list
     */
    var deselectEntity = function(that, key) {
        
        // generate a new list of selected entities, and remove the requested
        // entity from the selection basket
        var newselections = new Array();
        that.locate("selectionBasket").find("a").each(function(){
            if ($(this).attr("key") != key) newselections.push($(this).attr("key"));
            else $(this).parent().remove();
        });
        that.options.selected = newselections;
        
        // if the selected item is the currently selected entity, update
        // the breadcrumb selection state
        if (key == that.currentEntity.id) setBreadcrumbSelectionState(that, false);
    };

    /**
     * Update the visual selection state for the currently-browsed entity
     */
    var setBreadcrumbSelectionState = function(that, selected) {
        var link = that.locate("selectEntityLink").unbind("click");
        // deselect the currently-browsed entity
        if (!selected) {
            link.click(function(){ selectEntity(that, that.currentEntity.id); })
                .find("span").text(that.options.selectButtonMessage);
            that.locate("entityBrowsingHeader").removeClass("selected");
            
        // select the currently-browsed activity
        } else {
            link.click(function(){ deselectEntity(that, that.currentEntity.id); })
                .find("span").text(that.options.deselectButtonMessage);
            that.locate("entityBrowsingHeader").addClass("selected");
        }
    };
			
    /**
     * Update the breadcrumb trail
     */
    var updateBreadcrumbs = function(that, entity) {
        // update the current title
        var currentTitle = that.locate("currentEntityName");
        
        var breadcrumbs = that.locate("breadcrumbs");
        if (breadcrumbs.find("span a[key=" + entity.id + "]").size() > 0) {
            // if this entity already exists in the breadcrumb trail
            var removeBreadcrumb = false;
            $(breadcrumbs.find("span")).each(function(){
                if (removeBreadcrumb) { 
                    $(this).remove(); 
                } else if ($(this).find("a[key=" + entity.id + "]").size() > 0) { 
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
        currentTitle.text(entity.name).attr("key", entity.id);
    };
    

    /**
     * Create a new Entity Selection component
     */
    uportal.entityselection = function(container, options) {
        var that = fluid.initView("uportal.entityselection", container, options);
        
        // assign a new entity browser for retrieving groups, categories, and person
        // information from the portal
        that.entityBrowser = $.groupbrowser({});
        
        // initialize the search form and selection basket onclick events
        that.searchInitialized = false;
        that.locate("selectionBasket").find("a").click(function(){ deselectEntity(that, $(this).attr("key")); });
        that.locate("searchForm").submit(function(){ return search(that, this.searchterm.value) })
            .find("input[name=searchterm]").focus(function(){ $(this).val(""); $(this).unbind("focus"); });
        
        // browse to the designated default start entity
        browseEntity(that, that.options.initialFocusedEntity);
    };

    
    // defaults
    fluid.defaults("uportal.entityselection", {
        entityTypes: [],
        selected: [],
        initialFocusedEntity: 'local.0',
        selectButtonMessage: '',
        deselectButtonMessage: '',
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
    
})(up.jQuery, up.fluid);
