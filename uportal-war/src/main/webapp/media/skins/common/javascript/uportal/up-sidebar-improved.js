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

/**
 * This file contains improvement to show/hide sidebar and sidebar elements depending of the user session.
 * To works with session preference from the portal session you need to define portalCookieName depending
 * on your context, by default it's the JSESSIONID.
 */
(function ($) {
	var portalCookieName = "JSESSIONID", portalCookiePath = "/", portalSession;

	$("#portalSidebar").ready(function() {
		var sidebar   = $("#portalSidebar"), cookieSidebar;
		// we apply show/hide only if the xsl placed the css class for
		if (sidebar.hasClass("sidebarToggle")) {
			// part to show/hide sidebar from cookies
			// at init we need to get the portal session value
			if (typeof(portalSession) == 'undefined') {
				portalSession = $.cookie(portalCookieName);
				if (portalSession) {
					portalSession = portalSession.split(".")[0];
				}
			}
			// if portalSession is define and not null, we retrieve the cookie sidebar state
			if (portalSession) {
				cookieSidebar = GetCookie(portalSession, "Sidebar");
			}
			// and we show/hide the siedebar from cookie value if is define or from initial css stat
			if ( (cookieSidebar && cookieSidebar == 'opened')
					|| (!cookieSidebar && !(sidebar.hasClass("closed")))) {
				showSidebar();
			} else {
				hideSidebar();
			}

			// Add click action on show/hide boutton
			$("#portalSidebarToggleButton").click( function() {
				if (!(sidebar.hasClass("closed"))) {
					hideSidebar();
				} else {
					showSidebar();
				}
			});

			// Add click action on sidebar element title
			$("#portalSidebar .fl-widget-titlebar").click( function() {
				if ($(this).parents(".fl-widget").hasClass("closed")) {
					$(this).parents(".fl-widget").removeClass("closed");
					$(this).children("a").attr("title", $(this).find("span.labelclose").text());
					SetCookie(portalSession, "SidebarNth"+$(this).parents(".fl-widget").attr('id') , null);
				} else {
					$(this).parents(".fl-widget").addClass("closed");
					$(this).children("a").attr("title", $(this).find("span.labelopen").text());
					SetCookie(portalSession, "SidebarNth"+$(this).parents(".fl-widget").attr('id'), 'closed');
				}
			});

			// Add sidebar following scroll
			var _window    = $(window),
			offset     = sidebar.offset(),
			topPadding = 15,
			maxbottom = $("#portalPageBody").offset().top + $("#portalPageBody").height();

			_window.scroll(function() {
				var current_bottom = _window.scrollTop(),
				maxTopBottom = maxbottom - sidebar.height();
				if (_window.scrollTop() > offset.top && current_bottom <= maxTopBottom) {
					sidebar.stop().animate({
						marginTop: _window.scrollTop() - offset.top + topPadding
					});
				} else if (_window.scrollTop() > offset.top && current_bottom > maxTopBottom) {
					sidebar.stop().animate({
						top: maxTopBottom - topPadding
					});
				} else {
					sidebar.stop().animate({
						marginTop: 0
					});
				}
			});
		}
	});

	function hideSidebar(){
		$("#portalSidebar").removeClass("opened").addClass("closed");
		$("#portalSidebarToggleButton").children("a").attr("title", $("#portalSidebarToggleButton").find("h2.textopen").text());
        SetCookie(portalSession, "Sidebar", 'closed');
	}
	function showSidebar() {
		$("#portalSidebar").removeClass("closed").addClass("opened");
		$("#portalSidebarToggleButton").children("a").attr("title", $("#portalSidebarToggleButton").find("h2.textclose").text());

		// from cookies set sidebar element closed
		var elmSidebar = $("#portalSidebar .fl-widget:not(#portalSidebarToggleButton)");
		var nb = elmSidebar.length-1;
		while (nb >= 0) {
			// if cookie is set to hide the nth element
			if (GetCookie(portalSession, "SidebarNth"+elmSidebar.eq(nb).attr('id') ) == 'closed') {
				elmSidebar.eq(nb).addClass("closed");
				elmSidebar.eq(nb).children("a").attr("title", elmSidebar.eq(nb).find("span.labelopen").text());
				// if no cookie is set, ie the element is opened
			} else {
				elmSidebar.eq(nb).removeClass("closed");
				elmSidebar.eq(nb).children("a").attr("title", elmSidebar.eq(nb).find("span.labelclose").text());
			}
			nb --;
		}
		SetCookie(portalSession, "Sidebar", "opened");
	}
	function SetCookie (sessionName, elementId, cookieValue) {
		if (sessionName && elementId) {
			$.cookie(sessionName + "." + elementId, cookieValue, { path: portalCookiePath, secure: true});
		}
	}
	function GetCookie (sessionName, elementId) {
		return $.cookie(sessionName + "." + elementId);
	}
})(jQuery);
