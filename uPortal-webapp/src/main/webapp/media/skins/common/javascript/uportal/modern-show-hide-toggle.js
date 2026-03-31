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

/**
 * Modern replacement for up.showHideToggle
 * Simple show/hide toggle functionality without Fluid dependencies
 */
class ModernShowHideToggle {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.options = {
            showmessage: 'Show Stack Trace',
            hidemessage: 'Hide Stack Trace',
            selectors: {
                stacktracediv: '.stacktrace',
                stacktracetoggle: '.stacktracetoggle'
            },
            ...options
        };
        
        this.init();
    }
    
    init() {
        this.stacktraceDiv = this.container.querySelector(this.options.selectors.stacktracediv);
        this.toggleButton = this.container.querySelector(this.options.selectors.stacktracetoggle);
        
        if (!this.stacktraceDiv || !this.toggleButton) {
            console.warn('ModernShowHideToggle: Required elements not found');
            return;
        }
        
        // Initially hide the stacktrace
        this.stacktraceDiv.style.display = 'none';
        this.isVisible = false;
        
        // Set initial button text
        this.toggleButton.textContent = this.options.showmessage;
        
        // Bind click event
        this.toggleButton.addEventListener('click', (e) => {
            e.preventDefault();
            this.toggle();
        });
    }
    
    toggle() {
        if (this.isVisible) {
            this.hide();
        } else {
            this.show();
        }
    }
    
    show() {
        this.stacktraceDiv.style.display = 'block';
        this.toggleButton.textContent = this.options.hidemessage;
        this.isVisible = true;
    }
    
    hide() {
        this.stacktraceDiv.style.display = 'none';
        this.toggleButton.textContent = this.options.showmessage;
        this.isVisible = false;
    }
}

// Global namespace compatibility
window.up = window.up || {};
window.up.showHideToggle = function(container, options) {
    return new ModernShowHideToggle(container, options);
};