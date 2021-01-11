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
    (function(window, $) {
        'use strict';

        var init;
        var defaultOpts;
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
        convertExclusiveUrlToPageUrl = function(url) {
            var newUrl;
            var matches;
            var currentPagePortletId;
            var portletId;
            var state;

            matches = /\/p\/([^\/]+)\//.exec(window.location.pathname);
            if (matches && matches[1]) {
                currentPagePortletId = matches[1];
            }

            matches = /\/p\/([^\/]+)\//.exec(url);
            if (matches && matches[1]) {
                portletId = matches[1];
            }

            state = 'normal';
            // if editing a normal portlet, Try to retain the state.  If editing a
            // portlet in a region, have to switch back to normal mode.  This is
            // not ideal, but it's very similar to what the happens now anyhow.
            if (currentPagePortletId && portletId === currentPagePortletId) {
                matches = /\/(normal|maximized|exclusive|detached)\/render.uP/.exec(
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
        processAjaxResponse = function(conf, content) {
            var tempHtml = $('<div/>').html(content);

            // rewrite action URL on all forms...
            $(tempHtml)
                .find('form')
                .each(function(idx, form) {
                    var action;
                    var $form;

                    $form = $(form);
                    action = convertExclusiveUrlToPageUrl($form.attr('action'));
                    $form.attr('action', action);
                });

            // rewrite href attr on all links.
            $(tempHtml)
                .find('a')
                .each(function(idx, a) {
                    var href;
                    var $a;

                    $a = $(a);

                    href = convertExclusiveUrlToPageUrl($a.attr('href'));
                    $a.attr('href', href);

                    return href;
                });

            // once URLS are fixed, attach the content.
            $(conf.selectors.content)
                .empty()
                .append(tempHtml);
        };

        /**
         * Overridable options that can be passed to init.
         */
        defaultOpts = {
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
        init = function(config) {
            var conf;
            var pageLoadedFn;
            var pageLoadErrorFn;

            conf = $.extend(true, defaultOpts, config);

            $(conf.selectors.editLinks).click(function(evt) {
                var url;
                var title;
                var promise;

                evt.preventDefault();
                evt.stopPropagation();

                url = $(evt.currentTarget).data('lightboxUrl');
                title = $(evt.currentTarget).data('lightboxTitle');

                pageLoadedFn = function(content) {
                    processAjaxResponse(conf, content);

                    $(conf.selectors.loading).fadeOut();
                    $(conf.selectors.content).fadeIn();
                };

                pageLoadErrorFn = function() {
                    // if the ajax call fails, revert to the classic, inline method
                    // for handling configuration
                    window.location.href = $(evt.currentTarget).attr('href');
                };

                if (title) {
                    $(conf.selectors.title).text(title);
                }

                $(conf.selectors.content).hide();
                $(conf.selectors.loading).show();
                $(conf.selectors.lightbox).modal(conf.lightboxOptions);

                promise = $.ajax({
                    url: url,
                    contentType: 'text/html',
                });

                promise.then(pageLoadedFn, pageLoadErrorFn);

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
