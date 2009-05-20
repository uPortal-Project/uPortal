var uportal = uportal || {};

(function($, fluid){

    var addParameter = function(that, form) {
        var name = $(form).find("input[name=name]").val();
        var tr = $(document.createElement("tr"));
        var paramString = '[\'' + that.options.preferenceNamePrefix + name + '\'].value';
        
        // add the parameter name
        tr.append($(document.createElement("td")).text(name));
        
        // add the parameter value input field
        if (that.options.multivalued) {
            var div = $(document.createElement("div")).append(
                    $(document.createElement("input")).attr("name", that.options.preferenceBindName + paramString)
                ).append(
                    $(document.createElement("a")).attr("href", "javascript:;")
                        .addClass("delete-parameter-value-link")
                        .text("Remove").click(function(){ removeValue(that, this); })
                );
            tr.append($(document.createElement("td")).append(div).append(
                    $(document.createElement("a")).attr("href", "javascript:;")
                        .addClass("add-parameter-value-link").text("Add value")
                        .attr("paramName", name).click(function(){ addValue(that, this); })
                ));
        } else {
            tr.append($(document.createElement("td")).append(
                $(document.createElement("input")).attr("name", that.options.preferenceBindName + paramString)
            ));
        }
        
        // add the parameter override checkbox
        var checkbox = $(document.createElement("input")).attr("type", "checkbox")
            .attr("name", that.options.preferenceOverrideBindName + paramString).val("true");        
        tr.append($(document.createElement("td")).append(checkbox));
        
        // add the remove parameter link
        tr.append($(document.createElement("td")).append(
            $(document.createElement("a")).text("Delete").addClass("delete-parameter-link")
                .attr("href", "javascript:;").click(function(){ removeParameter(that, this); })
        ));
        
        // append the new row to the table
        that.locate("preferencesTable").append(tr);
        that.options.dialog.dialog('close');
        return false;
    };
    
    var removeParameter = function(that, link) {
        $(link).parent().parent().remove();
    };
    
    var addValue = function(that, link) {
        var name = $(link).attr("paramName");
        var div = $(document.createElement("div")).append(
                $(document.createElement("input"))
                    .attr("name", that.options.preferenceBindName + '[\'' + that.options.preferenceNamePrefix + name + '\'].value')
            ).append(
                $(document.createElement("a")).attr("href", "javascript:;")
                    .addClass("delete-parameter-value-link")
                    .text("Remove").click(function(){ removeValue(that, this); })
            );
        $(link).before(div);
    };
    
    var removeValue = function(that, link) {
        $(link).parent().remove();
    };
    
    var showAddParameterDialog = function(that) {
        var dialog = that.options.dialog;
        if (that.options.dialogInitialized) {
            dialog.dialog('open');
        } else {
            var html = "<form><p>Parameter name: <input name=\"name\"/></p>";
            html += "<input type=\"submit\" value=\"Add\"/></form>";
            dialog.html(html);
            dialog.find("form").submit(function(){ return addParameter(that, this); });
            dialog.dialog();
            that.options.dialogInitialized = true;
        }
    };
    
    uportal.portletParametersEditor = function(container, options) {
        var that = fluid.initView("uportal.portletParametersEditor", container, options);
        
        // 
        that.locate("preferencesTable").find(".delete-parameter-link")
            .click(function(){ removeParameter(that, this); });
        that.locate("preferencesTable").find(".delete-parameter-value-link")
            .click(function(){ removeValue(that, this); });
        that.locate("preferencesTable").find(".add-parameter-value-link")
        .click(function(){ addValue(that, this); });
        that.locate("parameterAddingLink").click(function(){ showAddParameterDialog(that); });
        
        // prepare the modal form dialog
        that.options.dialogInitialized = false;
    };

    
    // defaults
    fluid.defaults("uportal.portletParametersEditor", {
        preferenceNamePrefix: '',
        preferenceBindName: '',
        preferenceOverrideBindName: '',
        dialog: null,
        multivalued: false,
        selectors: {
            preferencesTable: 'tbody',
            parameterAddingLink: '.add-parameter-link'
        }
    });
    
})(up.jQuery, up.fluid);
