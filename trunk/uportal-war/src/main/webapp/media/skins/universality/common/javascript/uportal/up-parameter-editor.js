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
    
    var getParameterPath = function(name, that) {
        return that.options.parameterBindName + '[\'' + that.options.parameterNamePrefix + name + '\'].value';
    };

    var getAuxiliaryPath = function(name, that) {
        return that.options.auxiliaryBindName + '[\'' + that.options.parameterNamePrefix + name + '\'].value';
    };

    /**
     * Add a new parameter to the table.  This method will add a row to the 
     */
    var addParameter = function(form, that) {
        
        var tr, td, name, paramPath, checkbox;
        
        // get the new parameter name from the form
        name = $(form).find("input[name=name]").val();
        paramPath = getAuxiliaryPath(name, that);
        
        // create a new row in our parameters table
        tr = $(document.createElement("tr"));
        
        // add the parameter name
        tr.append($(document.createElement("td")).text(name));

        // create a new cell for the paramter value(s) and add it to our new row
        td = $(document.createElement("td"));
        tr.append(td);
        
        // add the parameter value input field
        if (that.options.multivalued) {
            
            // add a link for adding multiple parameter values
            td.append(
                $(document.createElement("a")).attr("href", "javascript:;")
                    .addClass(that.options.displayClasses.addValueLink)
                    .text(that.options.messages.addValue)
                    .attr("paramName", name)
                    .click(function(){ addValue($(this), that); })
            );

            // add an input field for an initial single value
            addValue(td.find("." + that.options.displayClasses.addValueLink), that);

        } else {
            td.append(
                $(document.createElement("input")).attr("name", paramPath)
            );
        }
        
        // add the parameter override checkbox
        if (that.options.useAuxiliaryCheckbox) {
            checkbox = $(document.createElement("input")).attr("type", "checkbox")
                .attr("name", getAuxiliaryPath(name, that)).val("true");        
            tr.append($(document.createElement("td")).append(checkbox));
        }
        
        // add the remove parameter link
        tr.append($(document.createElement("td")).append(
            $(document.createElement("a")).text(that.options.messages.remove)
                .addClass(that.options.displayClasses.removeItemLink)
                .attr("href", "javascript:;")
                .click(function(){ removeParameter($(this), that); })
        ));
        
        // append the new row to the table
        that.locate("preferencesTable").append(tr);
        that.options.dialog.dialog('close');
        return false;
    };

    var removeParameter = function(link, that) {
        $(link).parent().parent().remove();
    };

    var addValue = function(link, that) {
        var paramPath, div;
        
        link = $(link);
        paramPath = getParameterPath(link.attr("paramName"), that);
        link.before($(document.createElement("div"))
            .append(
                $(document.createElement("input")).attr("name", paramPath)
            ).append(
                $(document.createElement("a")).attr("href", "javascript:;")
                    .addClass(that.options.displayClasses.deleteValueLink)
                    .text(that.options.messages.remove)
                    .click(function(){ removeValue($(this), that); })
            )
        );
    };
    
    var removeValue = function(link, that) {
        $(link).parent().remove();
    };
    
    /**
     * Display the parameter adding dialog.  We ask a user to choose a parameter
     * name so we can appropriately set the name of the new input element.
     */
    var showAddParameterDialog = function(that) {
        var dialog = that.options.dialog;
        if (that.options.dialogInitialized) {
            // if the dialog has already been initialized, just open it
            dialog.dialog('open');
        } else {
            // set the dialog form to add the appropriate parameter
            dialog.find("form").submit(function (){ return addParameter(this, that); });
            
            // open the dialog and mark it as initialized
            dialog.dialog();
            that.options.dialogInitialized = true;
        }
    };
    
    up.ParameterEditor = function(container, options) {
        var that = fluid.initView("up.ParameterEditor", container, options);
        container = $(container);
        
        // initialize actions for parameter value adding and deletion
        container.find("." + that.options.displayClasses.deleteItemLink)
            .click(function(){ removeParameter(this, that); });
        container.find("." + that.options.displayClasses.deleteValueLink)
            .click(function(){ removeValue(this, that); });
        container.find("." + that.options.displayClasses.addValueLink)
            .click(function(){ addValue(this, that); });
        
        // initialize the action to add a new parameter
        container.find("." + that.options.displayClasses.addItemLink)
            .click(function(){ showAddParameterDialog(that); });
        
        // prepare the modal form dialog
        that.options.dialogInitialized = false;
    };

    
    // defaults
    fluid.defaults("up.ParameterEditor", {
        parameterNamePrefix: '',
        parameterBindName: '',
        auxiliaryBindName: '',
        useAuxiliaryCheckbox: false,
        dialog: null,
        multivalued: false,
        displayClasses: {
            deleteItemLink: "delete-parameter-link",
            deleteValueLink: "delete-parameter-value-link",
            addItemLink: "add-parameter-link",
            addValueLink: "add-parameter-value-link"
        },
        messages: {
            remove: 'Remove',
            addValue: 'Add value'
        },
        selectors: {
            preferencesTable: 'tbody'
        }
    });
    
})(jQuery, fluid);
