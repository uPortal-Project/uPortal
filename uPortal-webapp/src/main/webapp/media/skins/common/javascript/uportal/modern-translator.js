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
 * Modern replacement for up.TranslatorPortlet
 * Translation interface without Fluid dependencies
 */
class ModernTranslatorPortlet {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.options = {
            namespace: 'uptrans',
            locale: null,
            entityType: null,
            entity: null,
            resourceUrl: '',
            selectors: {
                entityForm: '#uptrans-form',
                locale: '#uptrans-locale',
                entityType: '#uptrans-entityType',
                entity: '.uptrans-entity',
                entities: '#uptrans-entities',
                entityList: '#uptrans-entityList',
                portletForm: '#uptrans-portletForm',
                messageForm: '#uptrans-messageForm',
                formContainer: '#uptrans-formContainer',
                addMessage: '#uptrans-addMessage'
            },
            messages: {
                messageTranslationSaved: 'Message translation has been successfully saved',
                portletTranslationSaved: 'Portlet definition translation has been successfully saved'
            },
            ...options
        };
        
        this.init();
    }
    
    init() {
        this.bindElements();
        this.bindEvents();
        
        // Initialize state
        this.options.locale = this.localeSelect.value;
        this.entityTypeSelectionChanged();
    }
    
    bindElements() {
        this.entityTypeSelect = this.container.querySelector(this.options.selectors.entityType);
        this.localeSelect = this.container.querySelector(this.options.selectors.locale);
        this.entitiesContainer = this.container.querySelector(this.options.selectors.entities);
        this.entityList = this.container.querySelector(this.options.selectors.entityList);
        this.portletForm = this.container.querySelector(this.options.selectors.portletForm);
        this.messageForm = this.container.querySelector(this.options.selectors.messageForm);
        this.formContainer = this.container.querySelector(this.options.selectors.formContainer);
        this.addMessageBtn = this.container.querySelector(this.options.selectors.addMessage);
    }
    
    bindEvents() {
        this.entityTypeSelect.addEventListener('change', () => this.entityTypeSelectionChanged());
        this.localeSelect.addEventListener('change', () => this.localeChanged());
        this.portletForm.addEventListener('submit', (e) => this.submitPortletForm(e));
        this.messageForm.addEventListener('submit', (e) => this.submitMessageForm(e));
        this.addMessageBtn.addEventListener('click', () => this.addMessage());
        
        // Prevent entity form submission
        const entityForm = this.container.querySelector(this.options.selectors.entityForm);
        if (entityForm) {
            entityForm.addEventListener('submit', (e) => {
                e.preventDefault();
                return false;
            });
        }
    }
    
    async entityTypeSelectionChanged() {
        const entityType = this.entityTypeSelect.value;
        this.options.entityType = entityType;
        this.options.entity = null;
        
        if (entityType) {
            try {
                const response = await fetch(this.options.resourceUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({
                        entity: entityType,
                        action: 'getEntityList'
                    })
                });
                const data = await response.json();
                this.refreshEntities(data);
            } catch (error) {
                console.error('Error loading entities:', error);
            }
        }
    }
    
    refreshEntities(data) {
        this.formContainer.style.display = 'none';
        
        if (this.options.entityType === 'message') {
            this.addMessageBtn.style.display = 'block';
        } else {
            this.addMessageBtn.style.display = 'none';
        }
        
        // Clear existing entities
        this.entitiesContainer.innerHTML = '';
        
        // Sort entities by title using original sorting function
        const sortedEntities = data.entities.sort(up.getStringPropertySortFunction('title'));
        
        // Create entity buttons with keyboard support
        sortedEntities.forEach(entity => {
            const button = document.createElement('button');
            button.className = 'btn btn-secondary uptrans-entity';
            button.textContent = entity.title;
            button.setAttribute('tabindex', '0');
            
            // Click handler
            const selectEntity = () => {
                this.options.entity = entity;
                this.updateForm();
            };
            
            button.addEventListener('click', selectEntity);
            
            // Keyboard handler (Enter key)
            button.addEventListener('keydown', (e) => {
                if (e.keyCode === 13) { // Enter key
                    selectEntity();
                }
            });
            
            this.entitiesContainer.appendChild(button);
        });
    }
    
    localeChanged() {
        this.options.locale = this.localeSelect.value;
        this.updateForm();
    }
    
    updateForm() {
        if (this.options.entity) {
            if (this.options.entityType === 'portlet') {
                this.fillPortletForm();
            } else if (this.options.entityType === 'message') {
                this.fillMessageForm();
            }
        }
    }
    
    async fillPortletForm() {
        try {
            const response = await fetch(this.options.resourceUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({
                    action: 'getEntity',
                    id: this.options.entity.id,
                    locale: this.options.locale,
                    entity: this.options.entityType
                })
            });
            const data = await response.json();
            const portlet = data.portlet;
            
            // Fill form fields
            this.setFormValue('.uptrans-pfrm-id', portlet.id);
            this.setFormValue('.uptrans-pfrm-loc', portlet.locale);
            this.setFormValue('.uptrans-pfrm-orig-title', portlet.original.title);
            this.setFormValue('.uptrans-pfrm-loc-title', portlet.localized.title);
            this.setFormValue('.uptrans-pfrm-orig-name', portlet.original.name);
            this.setFormValue('.uptrans-pfrm-loc-name', portlet.localized.name);
            this.setFormValue('.uptrans-pfrm-orig-descr', portlet.original.description);
            this.setFormValue('.uptrans-pfrm-loc-descr', portlet.localized.description);
            
            this.messageForm.style.display = 'none';
            this.formContainer.style.display = 'block';
            this.portletForm.style.display = 'block';
        } catch (error) {
            console.error('Error loading portlet data:', error);
        }
    }
    
    async fillMessageForm() {
        try {
            const response = await fetch(this.options.resourceUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({
                    action: 'getEntity',
                    id: this.options.entity.id,
                    locale: this.options.locale,
                    entity: this.options.entityType
                })
            });
            const data = await response.json();
            let message = data.message;
            
            // If no message, initialize with empty values
            if (!message) {
                message = {
                    code: this.options.entity.id,
                    locale: this.options.locale,
                    value: ''
                };
            }
            
            // Fill form fields
            this.setFormValue('.uptrans-mfrm-code', message.code);
            this.setFormValue('.uptrans-mfrm-loc', message.locale);
            this.setFormValue('.uptrans-mfrm-value', message.value);
            
            this.portletForm.style.display = 'none';
            this.formContainer.style.display = 'block';
            this.messageForm.style.display = 'block';
            this.messageForm.querySelector('.uptrans-mfrm-code-fieldset').style.display = 'none';
        } catch (error) {
            console.error('Error loading message data:', error);
        }
    }
    
    addMessage() {
        this.options.entity = null;
        
        // Fill form with empty values for new message
        this.setFormValue('.uptrans-mfrm-code', '');
        this.setFormValue('.uptrans-mfrm-loc', this.options.locale);
        this.setFormValue('.uptrans-mfrm-value', '');
        
        this.messageForm.querySelector('.uptrans-mfrm-code-fieldset').style.display = 'block';
        this.portletForm.style.display = 'none';
        this.formContainer.style.display = 'block';
        this.messageForm.style.display = 'block';
    }
    
    async submitPortletForm(e) {
        e.preventDefault();
        
        try {
            const formData = new FormData(this.portletForm);
            const response = await fetch(this.portletForm.action, {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                this.showSuccessMessage(this.options.messages.portletTranslationSaved);
            }
        } catch (error) {
            console.error('Error submitting portlet form:', error);
        }
        
        return false;
    }
    
    async submitMessageForm(e) {
        e.preventDefault();
        
        try {
            const formData = new FormData(this.messageForm);
            const response = await fetch(this.messageForm.action, {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                // If this was a new message (entity was null), reload entity list
                if (this.options.entity === null) {
                    await this.entityTypeSelectionChanged();
                }
                this.showSuccessMessage(this.options.messages.messageTranslationSaved);
            }
        } catch (error) {
            console.error('Error submitting message form:', error);
        }
        
        return false;
    }
    
    setFormValue(selector, value) {
        const element = this.container.querySelector(selector);
        if (element) {
            if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
                element.value = value || '';
            } else {
                element.textContent = value || '';
            }
        }
    }
    
    showSuccessMessage(message) {
        const messageEl = this.formContainer.querySelector('.portlet-msg-success');
        if (messageEl) {
            messageEl.innerHTML = message; // Use innerHTML like original
            messageEl.style.display = 'block';
            
            // Fade out after 4 seconds like original
            setTimeout(() => {
                let opacity = 1;
                const fadeOut = setInterval(() => {
                    opacity -= 0.1;
                    messageEl.style.opacity = opacity;
                    if (opacity <= 0) {
                        clearInterval(fadeOut);
                        messageEl.style.display = 'none';
                        messageEl.style.opacity = 1; // Reset for next time
                    }
                }, 100);
            }, 4000);
        }
    }
}

// Global namespace compatibility
window.up = window.up || {};
window.up.TranslatorPortlet = function(container, options) {
    return new ModernTranslatorPortlet(container, options);
};