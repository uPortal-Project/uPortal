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
		var settings = $.extend({
			'categoryShown': 0
		}, options);	

		function init() {
			$('li[data-role=list-divider]')
				.eq(settings.categoryShown)
				.nextUntil('li[data-role=list-divider]')
				.css('display', 'block');
		}
		init();
		return this.delegate('.ui-li-divider', 'click', function() {
			var $this = $(this);
			if($this.next().is(':visible')) {
				return;
			}
			$('.ui-listview > .ui-li:not(.ui-li-divider)').slideUp(150);
			$this.nextUntil('.ui-li-divider').slideDown(300);
		});
	};
})( jQuery );

