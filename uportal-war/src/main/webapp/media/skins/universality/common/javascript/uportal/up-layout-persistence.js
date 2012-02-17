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
     * Instantiate a LayoutPersistence component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.LayoutPreferencesPersistence = function(container, options) {
        
        // construct the new component
        var that = fluid.initView("up.LayoutPreferencesPersistence", container, options);

        that.update = function(data, success) {
            $.ajax({
                url: that.options.saveLayoutUrl,
                type: "POST",
                data: data,
                dataType: "json",
                async: false,
                success: success,
                error: function(request, text, error) {
                    that.events.onError.fire(that, request, text, error);
                }
            });
        };
        
        return that;
    };

    
    // defaults
    fluid.defaults("up.LayoutPreferencesPersistence", {
        saveLayoutUrl: null,
        selectors: {
            errorMessage: ".layout-persistence-error-message"
        },
        messages: {
            error: "Error persisting layout change"
        },
        events: {
            onSuccess: null,
            onError: null
        },
        listeners: {
            onSuccess: null,
            onError: function(that, request, text, error) {
                if (console) console.log(request, text, error);
                that.locate("errorMessage").text(that.options.messages.error);
            }
        }
    });
    
})(jQuery, fluid);
