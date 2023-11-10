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
var up = up || {};

up.lightboxConfig =
    up.lightboxConfig ||
    (function (window, $) {
        'use strict';

        var init;
        var defaultOptions;
        var processAjaxResponse;
        var convertExclusiveUrlToPageUrl;

        /**
         * Since the portlet config mode is loaded with the exclusive state, need
         * to rewrite all the URLS with a more natural state or when the action
         * occurs, you lose all the portal-y stuff (headers, other portlets, etc...)
         *
         * This tries to be kinda smart about it, but is somewhat limited in what
         * we can do.  If the portlet being editing is maximized, it will try to
         * retain that.  Otherwise, it will switch back the normal state.
         *
         * @param url the url to update
         * @return the updated URL as a string.
         */
        convertExclusiveUrlToPageUrl = function (url) {
            var newUrl;
            var matches;
            var currentPagePortletId;
            var portletId;
            var state;

            matches = /\/p\/([^/]+)\//.exec(window.location.pathname);
            if (matches && matches[1]) {
                currentPagePortletId = matches[1];
            }

            matches = /\/p\/([^/]+)\//.exec(url);
            if (matches && matches[1]) {
                portletId = matches[1];
            }

            state = 'normal';
            // if editing a normal portlet, Try to retain the state.  If editing a
            // portlet in a region, have to switch back to normal mode.  This is
            // not ideal, but it's very similar to what the happens now anyhow.
            if (currentPagePortletId && portletId === currentPagePortletId) {
                matches =
                    /\/(normal|maximized|exclusive|detached)\/render.uP/.exec(
                        window.location.pathname
                    );
                if (matches && matches[1]) {
                    state = matches[1];
                }
            }

            newUrl = url.replace('/exclusive/', '/' + state + '/');

            return newUrl;
        };

        /**
         * Grab the page source and rewrite all URLS in the response
         * to fix the links to exclusive state.
         *
         * @param conf the lightbox configuration
         * @param content the portlet content as a string.
         */
        processAjaxResponse = function (config, content) {
            var temporaryHtml = $('<div/>').html(content);

            // rewrite action URL on all forms...
            $(temporaryHtml)
                .find('form')
                .each(function (index, form) {
                    var action;
                    var $form;

                    $form = $(form);
                    action = convertExclusiveUrlToPageUrl($form.attr('action'));
                    $form.attr('action', action);
                });

            // rewrite href attr on all links.
            $(temporaryHtml)
                .find('a')
                .each(function (index, a) {
                    var href;
                    var $a;

                    $a = $(a);

                    href = convertExclusiveUrlToPageUrl($a.attr('href'));
                    $a.attr('href', href);

                    return href;
                });

            // once URLS are fixed, attach the content.
            $(config.selectors.content).empty().append(temporaryHtml);
        };

        /**
         * Overridable options that can be passed to init.
         */
        defaultOptions = {
            selectors: {
                editLinks: '[data-lightbox-url]',
                lightbox: '#config-lightbox',
                title: '#config-lightbox .modal-title',
                loading: '#config-lightbox .loading',
                content: '#config-lightbox .modal-body-content',
            },
            lightboxOptions: {
                backdrop: 'static',
                show: true,
            },
        };

        /**
         * Initialize the config lightbox support.
         *
         * @param config custom options (optional)
         */
        init = function (config) {
            var config_;
            var pageLoadedFunction;
            var pageLoadErrorFunction;

            config_ = $.extend(true, defaultOptions, config);

            $(config_.selectors.editLinks).click(function (event_) {
                var url;
                var title;
                var promise;

                event_.preventDefault();
                event_.stopPropagation();

                url = $(event_.currentTarget).data('lightboxUrl');
                title = $(event_.currentTarget).data('lightboxTitle');

                pageLoadedFunction = function (content) {
                    processAjaxResponse(config_, content);

                    $(config_.selectors.loading).fadeOut();
                    $(config_.selectors.content).fadeIn();
                };

                pageLoadErrorFunction = function () {
                    // if the ajax call fails, revert to the classic, inline method
                    // for handling configuration
                    window.location.href = $(event_.currentTarget).attr('href');
                };

                if (title) {
                    $(config_.selectors.title).text(title);
                }

                $(config_.selectors.content).hide();
                $(config_.selectors.loading).show();
                $(config_.selectors.lightbox).modal(config_.lightboxOptions);

                promise = $.ajax({
                    url: url,
                    contentType: 'text/html',
                });

                promise.then(pageLoadedFunction, pageLoadErrorFunction);

                return false;
            });
        };

        /**
         * Return the public API.
         */
        return {
            init: init,
        };
    })(window, jQuery);
