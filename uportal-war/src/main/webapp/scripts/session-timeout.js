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

up.SessionTimeout = up.SessionTimeout || (function($) {
    'use strict';

    var SECONDS = 1000,
        MINUTES = 60 * SECONDS,
        timeoutDialog,
        timerId,
        that = {};


    /**
     * Factory for timeoutDialog instances.
     * @param config
     * @returns the timeoutDialog instance
     */
    timeoutDialog = function(config) {
        var display,
            updateCountdown,
            doLogout,
            doRefresh,
            hideDialog,
            countdownTimerId;


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
            var promise, success, fail;

            promise = $.ajax({
                url: config.resetSessionURL
            });

            success = function() {
                hideDialog();
                config.restartTimer();
            };

            fail = function() {
                alert("Error resetting your session!");
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
         * Update the countdown time on the dialog.  If countdown
         * reaches 0, will auto-logout.
         */
        updateCountdown = function() {
            var now,
                remaining;

            now = new Date().getTime();
            // # of seconds before auto-logout...
            remaining = Math.round((config.logoutTime - now) / 1000);

            // time's up!  Log out...
            if (remaining <= 0) {
                hideDialog();
                doLogout();
                return;
            }

            config.dialogEl.find('.session-timeout-remaining').text(remaining);
            countdownTimerId = setTimeout(updateCountdown, 500);
        };


        /**
         * Display the timeout dialog.
         */
        display = function() {
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
                resizable: false
            });

            // start the timer that counts down # of seconds...
            countdownTimerId = setTimeout(updateCountdown, 500);
        };

        return {
            display: display
        };
    };


    /**
     * Factory for creating the sessionTimeout instance.
     *
     * @param config object.
     * @returns {{startTimer: startTimer}}
     * @constructor
     */
    that = function(config) {
        var startTimer;

        // start the timer that tracks when a session has expired.
        startTimer = function() {
            var showTimeoutDialog,
                sessionTimeoutMS,
                dialogDisplayMS,
                sleepMS,
                bufferMS,
                now = new Date().getTime(),
                logoutTime;

            // figure out when the dialog should pop and how long it should remain visible.
            sessionTimeoutMS = config.sessionTimeoutMS || 30 * MINUTES;
            bufferMS = config.bufferTimeMS || 30 * SECONDS;
            dialogDisplayMS = config.dialogDisplayMS || 1 * MINUTES;

            sleepMS = Math.max(sessionTimeoutMS - bufferMS - dialogDisplayMS, 0);

            // calculate when auto-logout should occur...
            logoutTime = now + sleepMS + dialogDisplayMS;


            showTimeoutDialog = function() {
                timeoutDialog({
                    logoutTime: logoutTime,
                    dialogEl: $('#' + config.dialogId),
                    logoutURL: config.logoutURL,
                    resetSessionURL: config.resetSessionURL,
                    restartTimer: startTimer
                }).display();
            };

            if (timerId) {
                clearTimeout(timerId);
            }

            if (typeof config.enabled === 'undefined' || config.enabled === true) {
                timerId = setTimeout(showTimeoutDialog, sleepMS);
            }
        };

        // return public interface API.
        return {
            startTimer: startTimer
        };
    };

    return that;
} (up.jQuery));