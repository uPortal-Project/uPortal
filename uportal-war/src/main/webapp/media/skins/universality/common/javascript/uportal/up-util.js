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

/**
 * This file contains a number of general utility methods for use within 
 * uPortal.
 */
(function ($, fluid) {
    
    /**
     * Extracts a uPortal DLM element ID from an HTML node.  This default 
     * implementation assumes that any submitted HTML element has an ID of
     * the form <type>_<elementID>.
     * 
     * @param that    Fluid component
     * @param element HTML element for which to determine the ID
     * @return {String} DLM node ID associated with the supplied HTML node
     */
    up.defaultNodeIdExtractor = function (element) {
        return $(element).attr("id").split("_")[1];
    };
    
    /**
     * Returns a sort function designed to sort two objects via a
     * case-insensitive comparison of a single string-type object property.
     * This method also allows the caller to specify a "special" value that
     * will always be the first item in the array, regardless of its 
     * alphabetic relationship to other elements in the array.
     * 
     * @param propertyName {String} name of the property on which to perform
     *          comparisons
     * @param firstValue {String} property value which should always be sorted
     *          first.  May be left undefined.
     */
    up.getStringPropertySortFunction = function (propertyName, firstValue) {
        return function (a, b) {
            
            // get the values for the configured property from 
            // each object and transform them to lower case
            var aprop = a[propertyName].toLowerCase();
            var bprop = b[propertyName].toLowerCase();
            
            // if the values are identical, indicate an equals
            if (aprop === bprop) {
                return 0;
            }
            
            // if the firstValue property has been specified, 
            // test each object to determine whether it matches
            if (firstValue) {
                firstValue = firstValue.toLowerCase();
                if (aprop === firstValue) {
                    return -1;
                } else if (bprop === firstValue) {
                    return 1;
                }
            }
            
            // otherwise perform a normal alphabetic sort
            if (aprop > bprop) {
                return 1;
            } else {
                return -1;
            }
        };
    };
    
    /**
     * Escape special character in preparation for using the supplied string
     * in a JavaScript RegExp.
     * 
     * @param str {String} String to be cleaned
     */
    up.escapeSpecialChars = function (str) {
        var specials = new RegExp("[.*+?|()\\[\\]{}\\\\]", "g"); // .*+?|()[]{}\
        return str.replace(specials, "\\$&");
    };
    
    /**
     * Refresh a Fluid pager with a new data model.  This method will also 
     * return the pager to the first page
     * 
     * @param pager  {Object}  pager to be refreshed
     * @param data   {Object}  new data model
     */
    up.refreshPager = function (pager, data) {
        var newModel = fluid.copy(pager.model);
        newModel.totalRange = data.length;
        newModel.pageIndex = 0;
        newModel.pageCount = Math.max(1, Math.floor((newModel.totalRange - 1) / newModel.pageSize) + 1);
        fluid.clear(pager.options.dataModel);
        fluid.model.copyModel(pager.options.dataModel, data);
        pager.permutation = undefined;
        pager.events.onModelChange.fire(newModel, pager.model, pager);
        fluid.model.copyModel(pager.model, newModel);
    };
    
    /**
     * Shows a "loading" screen.
     * 
     * @param {Object} selector - reference to jQuery selector.
     */
    up.showLoader = function (selector) {
        selector.show();
    };//end:function.
    
    /**
     * Hides a "loading" screen after a 1.5 second delay.
     * 
     * @param {Object} selector - reference to jQuery selector.
     */
    up.hideLoader = function (selector, time) {
        var t, duration;
        
        duration = (time === undefined) ? 1000 : time;
        t = setTimeout(function () {
            selector.fadeOut("slow");
            clearTimeout(t);
        }, duration);
    };//end:function.
    
    /**
     * Replaces all occurances of {0}, {1}, ..., {n} with values from argument array.
     *  
     * @param {String} message - message string.
     * @param {Array} args - array of string objects.
     * @returns Formatted message string.
     */
    up.formatMessage = function(message, args) {
        var s = message;
        for (var i = 0; i < args.length; i++) {
            var reg = new RegExp("\\{" + i + "\\}", "gm");
            s = s.replace(reg, args[i]);
        }
        return s;
    };//end:function
})(jQuery, fluid);