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
'use strict';
var up = up || {};

/**
 * Modern utility methods for uPortal - no Fluid dependencies
 */
(function ($) {
    /**
     * Extracts a uPortal DLM element ID from an HTML node.
     */
    up.defaultNodeIdExtractor = function (element) {
        return $(element).attr('id').split('_')[1];
    };

    /**
     * Returns a sort function for case-insensitive string property comparison
     */
    up.getStringPropertySortFunction = function (propertyName, firstValue) {
        return function (a, b) {
            const aprop = a[propertyName].toLowerCase();
            const bprop = b[propertyName].toLowerCase();

            if (aprop === bprop) return 0;

            if (firstValue) {
                const first = firstValue.toLowerCase();
                if (aprop === first) return -1;
                if (bprop === first) return 1;
            }

            return aprop > bprop ? 1 : -1;
        };
    };

    /**
     * Escape special characters for RegExp
     */
    up.escapeSpecialChars = function (string_) {
        const specials = /[.*+?|()[\]{}\\]/g;
        return string_.replace(specials, '\\$&');
    };

    /**
     * Shows a loading screen
     */
    up.showLoader = function (selector) {
        selector.show();
    };

    /**
     * Hides a loading screen after delay
     */
    up.hideLoader = function (selector, time) {
        const duration = time === undefined ? 1000 : time;
        setTimeout(() => {
            selector.fadeOut('slow');
        }, duration);
    };

    /**
     * Format message with placeholder replacement
     */
    up.formatMessage = function (message, arguments_) {
        let s = message;
        for (let index = 0; index < arguments_.length; index++) {
            const reg = new RegExp('\\{' + index + '\\}', 'gm');
            s = s.replace(reg, arguments_[index]);
        }
        return s;
    };
})(jQuery);