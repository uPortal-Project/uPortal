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

up.SessionTimeout =
    up.SessionTimeout ||
    (function($) {
        'use strict';

        var SECONDS = 1000;
        var MINUTES = 60 * SECONDS;
        var timeoutDialog;
        var timerId;
        var LOCAL_STORAGE_LOGOUT_KEY = 'UportalSessionTimeoutLogoutTime';
        var saveLogoutTime;
        var getSavedLogoutTime;
        var that = {};

        /**
         * Save the logout time to the browser's localStorage.
         * @param logoutTime to save.
         */
        saveLogoutTime = function(logoutTime) {
            localStorage.setItem(LOCAL_STORAGE_LOGOUT_KEY, logoutTime);
        };

        /**
         * Load the logout time from the browser's localStorage.
         * @return the logoutTime previously saved.
         */
        getSavedLogoutTime = function() {
            return localStorage.getItem(LOCAL_STORAGE_LOGOUT_KEY);
        };

        /**
         * Factory for timeoutDialog instances.
         * @param config
         * @return the timeoutDialog instance
         */
        timeoutDialog = function(config) {
            var display;
            var updateCountdown;
            var doLogout;
            var doRefresh;
            var hideDialog;
            var countdownTimerId;
            var newerTimerExists;

            /**
             * Log out of the app.
             */
            doLogout = function() {
                window.location = config.logoutURL;
            };

            /**
             * Refresh the session
             */
            doRefresh = function() {
                var promise;
                var success;
                var fail;

                promise = $.ajax({
                    url: config.resetSessionURL,
                });

                success = function() {
                    hideDialog();
                    config.restartTimer();
                };

                fail = function() {
                    alert('Error resetting your session!');
                };

                promise.then(success, fail);
            };

            /**
             * Hide the timeout dialog.
             */
            hideDialog = function() {
                config.dialogEl.dialog('close');
                if (countdownTimerId) {
                    clearTimeout(countdownTimerId);
                }
            };

            /**
             *    Checks if a newer timer instance exists (from another browser tab).
             *    If so, restart this instance of the timer using the newer logoutTime.
             */
            newerTimerExists = function() {
                var savedLogoutTime = getSavedLogoutTime();

                if (savedLogoutTime > config.logoutTime) {
                    config.restartTimer(savedLogoutTime);
                    return true;
                } else {
                    return false;
                }
            };

            /**
             * Update the countdown time on the dialog.  If countdown
             * reaches 0, will auto-logout.
             */
            updateCountdown = function() {
                var now = new Date().getTime();
                var remaining;

                // Bail out if a newer timer exists.
                if (newerTimerExists()) {
                    hideDialog();
                    return;
                }

                // # of seconds before auto-logout...
                remaining = Math.round((config.logoutTime - now) / 1000);

                // time's up!  Log out...
                if (remaining <= 0) {
                    hideDialog();
                    doLogout();
                    return;
                }

                config.dialogEl
                    .find('.session-timeout-remaining')
                    .text(remaining);
                countdownTimerId = setTimeout(updateCountdown, 500);
            };

            /**
             * Display the timeout dialog.
             */
            display = function() {
                // Bail out if a newer timer exists.
                if (newerTimerExists()) {
                    return;
                }

                config.dialogEl.find('.refresh-session').click(doRefresh);
                config.dialogEl.find('.logout').click(doLogout);
                // grr.  would prefer to use bootstrap, but doesn't work well
                // in universality.
                config.dialogEl.dialog({
                    show: 'fade',
                    hide: 'fade',
                    autoOpen: true,
                    closeOnEscape: false,
                    dialogClass: 'session-timeout-dlg',
                    draggable: false,
                    width: '600px',
                    modal: true,
                    resizable: false,
                });

                // start the timer that counts down # of seconds...
                countdownTimerId = setTimeout(updateCountdown, 500);
            };

            return {
                display: display,
            };
        };

        /**
         * Factory for creating the sessionTimeout instance.
         *
         * @param config object.
         * @return {{startTimer: startTimer}}
         * @constructor
         */
        that = function(config) {
            var startTimer;

            // start the timer that tracks when a session has expired.
            startTimer = function(newLogoutTime) {
                var showTimeoutDialog;
                var sessionTimeoutMS;
                var dialogDisplayMS;
                var sleepMS;
                var bufferMS;
                var now = new Date().getTime();
                var logoutTime;

                // Load timeout values from config.
                sessionTimeoutMS = config.sessionTimeoutMS || 30 * MINUTES;
                bufferMS = config.bufferTimeMS || 30 * SECONDS;
                dialogDisplayMS = config.dialogDisplayMS || 1 * MINUTES;

                // If a specific logoutTime was passed in, use that.
                // This happens when a newer browser tab has started a SessionTimeout instance.
                if (newLogoutTime) {
                    logoutTime = newLogoutTime;

                    sleepMS = Math.max(logoutTime - dialogDisplayMS - now, 0);
                } else {
                    // If no specific logoutTime was passed in, calculate it based on the normal config options.
                    // This will always run on the initial page load to set up the timeout values.
                    sleepMS = Math.max(
                        sessionTimeoutMS - bufferMS - dialogDisplayMS,
                        0
                    );

                    // calculate when auto-logout should occur...
                    logoutTime = now + sleepMS + dialogDisplayMS;

                    // Save the newly calculated logout time to localStorage so other instances of the SessionTimeout code can use it.
                    saveLogoutTime(logoutTime);
                }

                showTimeoutDialog = function() {
                    timeoutDialog({
                        logoutTime: logoutTime,
                        dialogEl: $('#' + config.dialogId),
                        logoutURL: config.logoutURL,
                        resetSessionURL: config.resetSessionURL,
                        restartTimer: startTimer,
                    }).display();
                };

                if (timerId) {
                    clearTimeout(timerId);
                }

                if (
                    typeof config.enabled === 'undefined' ||
                    config.enabled === true
                ) {
                    timerId = setTimeout(showTimeoutDialog, sleepMS);
                }
            };

            // return public interface API.
            return {
                startTimer: startTimer,
            };
        };

        return that;
    })(up.jQuery);
