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
 (function($) {
   $.fn.jqmAccordion = function(options) {

     var defaults = {
       showTabByDefault: true,
       tabIndexShown: 0,
       tab: 'li[data-role=list-divider]',
       slideUpSpeed: 150,
       slideDownSpeed: 300,
       closeTabs: false
     };

     var opts = $.extend(defaults, options);

     if ( opts.showTabByDefault ) {
       setTimeout(function () {
         $(opts.tab)
           .eq(opts.tabIndexShown)
           .nextUntil(opts.tab)
           .show();
       }, 10); // wait for jquery mobile...
     }

     this.delegate(opts.tab, 'click', function() {
       var $this     = $(this),
           tabIsOpen = $this.next().is(':visible'),
           otherTabs = $this.parent().children().not(opts.tab),
           thisTab   = $this.nextUntil(opts.tab);

       if ( tabIsOpen && opts.closeTabs ) {
         thisTab.slideUp(opts.slideUpSpeed);
       } else if ( tabIsOpen ) {
         return; // do nothing
       } else {
         otherTabs.slideUp(opts.slideUpSpeed);
         thisTab.slideDown(opts.slideDownSpeed);
       }
     });

     return this;
   };
 })( jQuery );

