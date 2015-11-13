/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
"use strict";
var up = up || {};

(function($){
	$(".up-portlet-controls").ready(function() {
		$('a.up-portlet-control.print').each(function(i){
			var chanId = $(this).parent().parent().attr('id').split("_")[1];
			$(this).attr('id','printPortlet_' + chanId);
			$(this).attr('href',"javascript:void(0);");
			$(this).click(function(){
				printPortlet(this.id.split("_")[1]);
				return false;
			});

			var printPortlet = function(id) {
				var sOption="toolbar=yes,location=no,directories=yes,menubar=yes,";
				sOption+="scrollbars=yes,width=750,height=600,left=100,top=25,resizable=yes";

				var iFrame = $('#portletContent_'+id).find('iframe')[0];
				if(typeof(iFrame) == 'object') {
					var printWindows =  window.open(iFrame.src,'',sOption);
					printWindows.focus();
					printWindows.print();
					return false;
				}

				var printWindows =  window.open('','',sOption);
				var html = '<html><head><title>Impression</title>';

				$('link[rel=stylesheet]').each(function () {
					html += '<link href="'+this.href+'" rel="'+this.rel+'" type="'+this.type+'"/>\n';
				});

				html += '</head>';

				var htmlTree = '';
				$('#portletContent_'+id).parents().each(function () {
					if(this.tagName.toLowerCase() != "head" && this.tagName.toLowerCase() != 'html') {
						var oneElement = '<'+this.tagName ;
						if(this.id != 'undefined' && this.id != '') oneElement += ' id="'+this.id+'"' ;
						if(this.className != 'undefined' && this.className != '') {
							var className = this.className.toLowerCase();

							className = className.replace(new RegExp("layout-.-columns","gi"),"layout-1-columns");
							className = className.replace(new RegExp("fl-col-flex.","gi"),"fl-col-flex1");
							className = className.replace(new RegExp("left","gi"),"single");
							className = className.replace(new RegExp("fl-col-flex..","gi"),"fl-col-flex");
							oneElement += ' class="'+className+'"' ;
						}
						oneElement += ' >' ;

						htmlTree = oneElement + '\n' + htmlTree
					}
				});
				html += htmlTree;
				html +='<div id="portletContent_' + id + '" class="' + $('#portletContent_'+id).get(0).className + '">';
				// class="fl-widget-content fl-fix up-portlet-content-wrapper"
				html += $('#portletContent_'+id).clone().html();
				html +='</div>';

				$('#portletContent_'+id).parents().each(function () {
					html += '</'+this.tagName+'>\n' ;
				});

				printWindows.document.write(html);
				printWindows.document.close();
				printWindows.focus();
				printWindows.print();

				return false;
			};
		});
	});
})(jQuery);
