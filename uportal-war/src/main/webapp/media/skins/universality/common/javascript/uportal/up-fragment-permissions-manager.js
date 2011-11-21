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
    
    /**
     * Populate a permissions form according to the presence or absence of
     * CSS classes on the specified element.
     * 
     * @param that      layout manager instance
     * @param element   HTML element with permission CSS classes
     */
    var populateForm = function(that, element) {

        // set the hidden portlet ID attribute
        var form = that.locate("permissionsDialog").find("form");
        form.find("[name=nodeId]").val(that.options.nodeIdExtractor(element));
        
        // set the movable permission
        if (element.hasClass(that.options.cssClassNames.movable)) {
            form.find("[name=movable]").attr("checked", "checked");
        } else {
            form.find("[name=movable]").removeAttr("checked");
        }

        // set the deletable permission
        if (element.hasClass(that.options.cssClassNames.deletable)) {
            form.find("[name=deletable]").attr("checked", "checked");
        } else {
            form.find("[name=deletable]").removeAttr("checked");
        }

        // set the movable permission
        if (element.hasClass(that.options.cssClassNames.editable)) {
            form.find("[name=editable]").attr("checked", "checked");
        } else {
            form.find("[name=editable]").removeAttr("checked");
        }

        // set the movable permission
        if (element.hasClass(that.options.cssClassNames.addChildAllowed)) {
            form.find("[name=addChildAllowed]").attr("checked", "checked");
        } else {
            form.find("[name=addChildAllowed]").removeAttr("checked");
        }

        var title = that.options.titleExtractor(element);
        that.locate("formTitle").html(title);
        
    };

    /**
     * Update the persisted permissions for a portal page according to the
     * selections in the submitted form.
     * 
     * @param that      layout manager instance
     * @param form      permissions form
     * @return
     */
    var updatePermissions = function(that, element) {
        var newPermissions = persistPermissions(that, that.locate("permissionsDialog").find("form")[0]);
        updatePermissionClasses(that, element, newPermissions);

        that.locate("permissionsDialog").dialog("close");
        that.events.onUpdatePermissions.fire(element, newPermissions);
        return false;
    };
    
    /**
     * Save the permissions from the supplied form to the layout
     * 
     * @param that      layout manager instance
     * @param form      permissions form
     * @return array of newly-persisted permissions
     */
    var persistPermissions = function(that, form) {
        var data = {};
        
        // construct an appropriate data object from the form
        data.elementID = $(form).find("[name=nodeId]").val();
        data.action = "updatePermissions";
        data.deletable = $(form.deletable).attr("checked") === "checked";
        data.movable = $(form.movable).attr("checked") === "checked";
        
        if (form.editable) {
            data.editable = $(form.editable).attr("checked") === "checked";
        }
        if (form.addChildAllowed) {
            data.addChildAllowed = $(form.addChildAllowed).attr("checked") === "checked";
        }
        
        $.post(that.options.savePermissionsUrl, data, null, "json");
        
        var permissions = {
            deletable: data.deletable,
            movable: data.movable,
            editable: data.editable,
            addChildAllowed: data.addChildAllowed
        };
        return permissions;
        
    };
    
    /**
     * Update the permission-related classes on an element, according to the
     * supplied array of permissions.
     * 
     * @param element
     * @param permissions
     * @return
     */
    var updatePermissionClasses = function(that, element, permissions) {
        setClass(element, permissions.deletable, that.options.cssClassNames.deletable);
        setClass(element, permissions.movable, that.options.cssClassNames.movable);
        setClass(element, permissions.editable, that.options.cssClassNames.editable);
        setClass(element, permissions.addChildAllowed, that.options.cssClassNames.addChildAllowed);
    };
    
    /**
     * Add or remove a classname on an element depending on the value of the
     * supplied permission.  A permission of <code>true</code> will cause this
     * method to add the CSS class, which a value of <code>false</code> will
     * result in the CSS class being removed.  If the permission is undefined,
     * the method will exit without making any modifications.
     * 
     * @param element
     * @param permission
     * @param className
     * @return
     */
    var setClass = function(element, permission, className) {
        // if no permission is set, just return without modifying any classes
        if (permission == undefined) return;
        
        // otherwise, add or remove the class as appropriate
        if (permission) {
            $(element).addClass(className);
        } else {
            $(element).removeClass(className);
        }
    };
    
    up.FragmentPermissionsMenu = function(container, options) {
        var that = fluid.initView("up.FragmentPermissionsMenu", container, options);
        
        that.state = {};

        that.refresh = function(link) {
            var element = that.options.elementExtractor(that, link);
            populateForm(that, element);
            
            // initialize the permission form submission actions
            that.locate("permissionsForm").unbind("submit")
                .submit(function(){
                    return updatePermissions(that, element);
                }
            );
            
            return false;
        };
        
        return that;
    };

    
    // defaults
    fluid.defaults("up.FragmentPermissionsMenu", {
        savePermissionsUrl: "mvc/layout",
        elementExtractor: null,
        titleExtractor: null,
        nodeIdExtractor: up.defaultNodeIdExtractor,
        cssClassNames: {
            movable: "movable",
            editable: "editable",
            deletable: "deletable",
            addChildAllowed: "canAddChildren"
        },
        selectors: {
            formTitle: null
        },
        events: {
            onUpdatePermissions: null
        },
        listeners: {
            onUpdatePermissions: null
        }
    });
    
    up.FragmentPermissionsManager = function(container, options) {
        var that = fluid.initView("up.FragmentPermissionsManager", container, options);
        
        that.menus = {};

        // tabs permissions manager
        that.locate("pageDialog").dialog({ width: 550, modal: true, autoOpen: false });
        that.menus.pagePermissionsManager = up.FragmentPermissionsMenu(that.locate("pageDialog"), {
            savePermissionsUrl: that.options.savePermissionsUrl,
            elementExtractor: function(that, link){
                return $("#portalNavigationList li.active"); 
            },
            titleExtractor: function(element){ return $("#portalNavigationList li.active a.portal-navigation-link").attr("title"); },
            selectors: {
                formTitle: "h2"
            }
        });
        that.locate("pageDialogLink").click(function(){
            that.menus.pagePermissionsManager.refresh($(this));
            that.locate("pageDialog").dialog("open");
        });
        // TODO: on update, refresh tab name, editing, delete
        
        // columns permissions manager
        that.locate("columnDialog").dialog({ width: 550, modal: true, autoOpen: false });
        that.menus.columnPermissionsManager = up.FragmentPermissionsMenu(that.locate("columnDialog"), {
            savePermissionsUrl: that.options.savePermissionsUrl,
            elementExtractor: function(that, link){
                return $(link).parents(".portal-page-column"); 
            },
            titleExtractor: function(element){ 
                return up.formatMessage(that.options.messages.columnX, [$(".portal-page-column").index(element) + 1]); 
            },
            selectors: {
                formTitle: "h2"
            }
        });
        that.locate("columnDialogLink").click(function(){
            that.menus.columnPermissionsManager.refresh($(this));
            that.locate("columnDialog").dialog("open");
        });
        // TODO: on update, refresh layout options
        
        // portlet permissions manager
        that.locate("portletDialog").dialog({ width: 550, modal: true, autoOpen: false });
        that.menus.portletPermissionsManager = up.FragmentPermissionsMenu(that.locate("portletDialog"), {
            savePermissionsUrl: that.options.savePermissionsUrl,
            elementExtractor: function(that, link){
                return $(link).parents(".up-portlet-wrapper"); 
            },
            titleExtractor: function(element){ return element.find(".up-portlet-wrapper-inner h2 a").text(); },
            selectors: {
                formTitle: "h2"
            },
            events: {
                onUpdatePermissions: null
            },
            listeners: {
                onUpdatePermissions: function(element, newPermissions) {
                    if (!newPermissions.movable) {
                        element.addClass("locked").removeClass("fl-reorderer-movable-default");
                        element.find("[id*=toolbar_]").removeClass("ui-draggable");
                    } else {
                        element.removeClass("locked").addClass("fl-reorderer-movable-default");
                        element.find("[id*=toolbar_]").addClass("ui-draggable");
                    }
                    // TODO: Apply portlet permissions: 
                    // includes refreshing reorderer and displaying/hiding delete
                }
            }
        });
        that.locate("portletDialogLink").click(function(){
            that.menus.portletPermissionsManager.refresh($(this));
            that.locate("portletDialog").dialog("open");
        });
        
        return that;
    };

    // defaults
    fluid.defaults("up.FragmentPermissionsManager", {
        savePermissionsUrl: "mvc/layout",
        selectors: {
            formTitle: null,
            pageDialog: ".edit-page-permissions-dialog",
            pageDialogLink: "#editPagePermissionsLink",
            columnDialog: ".edit-column-permissions-dialog",
            columnDialogLink: ".portal-column-permissions-link",
            portletDialog: ".edit-portlet-permissions-dialog",
            portletDialogLink: ".portlet-permissions-link"
        }, 
        messages: {
            columnX: "Column {0}"
        }
    });
    
})(jQuery, fluid);
