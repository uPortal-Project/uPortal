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
	
	var initialHide = function (that) {
		$(that.locate("stacktracediv")).hide();
	};
	
	var addToggle = function (that) {
		var toggleHandle = $(that.locate("stacktracetoggle"));
		var stacktracediv = $(that.locate("stacktracediv"));
		var hideMesg = that.options.hidemessage;
		var showMesg = that.options.showmessage;
		toggleHandle.toggle(
				function() {
    				stacktracediv.show('fast', function() {
    					toggleHandle.text(hideMesg);
    				});
    			},
    			function() {
    				stacktracediv.hide('fast', function() { 
    					toggleHandle.text(showMesg);
    				});
    			}
		);
	}
	
	up.showHideToggle = function(container, options) {
		var that = fluid.initView("up.showHideToggle", container, options);

        initialHide(that);
        addToggle(that);

        return that; 
	};
	
	fluid.defaults("up.showHideToggle", {
		showmessage: 'Show Stack Trace',
		hidemessage: 'Hide Stack Trace',
		selectors: {
			stacktracediv: '.stacktrace',
			stacktracetoggle: '.stacktracetoggle'
		}
	});

})(jQuery, fluid);

