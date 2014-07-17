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

(function(up, $) {
    'use strict';

    var seconds = 1000,
        minutes = 60 * seconds,
        dialog = {};

    if (up.SessionTimeout) {
        return;
    }



    up.SessionTimeout = function(config) {
        var startTimer,
            showTimeoutDialog,
            sleepTime,
            bufferTime,
            waitTime,
            dialogId,
            dialog;

        waitTime = config.waitTime || 5 * minutes;
        bufferTime = config.bufferTime || 30 * seconds;
        sleepTime = config.sleepTime || (30 * minutes) - waitTime - bufferTime;
        dialogId = config.dialogId;

        dialog = function(waitTime) {
            var display = function() {
                $(dialogId).dialog();
            };

            return {
                display: display
            };
        };


        showTimeoutDialog = function() {
        };


        startTimer = function() {
            setTimeout(showTimeoutDialog, sleepTime);
        };

        return {
            startTimer: startTimer
        };
    }

} (up, jQuery));