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
 * Modern PortletRegistry - no Fluid dependencies
 */
class ModernPortletRegistry {
    constructor(container, options = {}) {
        this.container = container;
        if (!options.portletListUrl) {
            throw new Error('ModernPortletRegistry: portletListUrl is required');
        }
        this.options = {
            portletListUrl: null,
            allCategoriesName: 'All',
            ...options
        };
        this.state = {
            portlets: [],
            categories: []
        };
        this.events = {
            onLoad: []
        };
        
        this.getRegistry();
    }

    // Event system
    on(event, callback) {
        if (!this.events[event]) this.events[event] = [];
        this.events[event].push(callback);
    }

    fire(event, ...args) {
        if (this.events[event]) {
            this.events[event].forEach(callback => callback(...args));
        }
    }

    createPortlet(json) {
        return {
            id: json.id,
            description: json.description,
            fname: json.fname,
            title: json.title,
            name: json.name,
            state: json.state,
            type: json.typeId,
            iconUrl: json.iconUrl,
        };
    }

    createCategory(json) {
        return {
            id: json.id,
            name: json.name,
            description: json.description,
            categories: [],
            deepCategories: [],
            portlets: [],
            deepPortlets: [],
        };
    }

    processCategory(category) {
        if (this.state.categories[category.id]) {
            return this.state.categories[category.id];
        }

        const c = this.createCategory(category);

        // Process subcategories
        if (category.categories) {
            category.categories.forEach(subCategory => {
                const processed = this.processCategory(subCategory);
                c.categories.push(processed);
                c.categories[processed.id] = processed;

                if (!c.deepCategories[processed.id]) {
                    c.deepCategories.push(processed);
                    c.deepCategories[processed.id] = processed;
                }

                // Add deep members
                processed.deepCategories.forEach(member => {
                    if (!c.deepCategories[member.id]) {
                        c.deepCategories.push(member);
                        c.deepCategories[member.id] = member;
                    }
                });

                processed.deepPortlets.forEach(member => {
                    if (!c.deepPortlets['portlet.' + member.id]) {
                        c.deepPortlets.push(member);
                        c.deepPortlets['portlet.' + member.id] = member;
                    }
                });
            });
        }

        // Process portlets
        if (category.channels) {
            category.channels.forEach(json => {
                const portlet = this.createPortlet(json);
                c.portlets.push(portlet);
                c.portlets['portlet.' + portlet.id] = portlet;

                if (!this.state.portlets['portlet.' + portlet.id]) {
                    this.state.portlets.push(portlet);
                    this.state.portlets['portlet.' + portlet.id] = portlet;
                }

                if (!c.deepPortlets['portlet.' + portlet.id]) {
                    c.deepPortlets.push(portlet);
                    c.deepPortlets['portlet.' + portlet.id] = portlet;
                }
            });
        }

        this.state.categories.push(c);
        this.state.categories[c.id] = c;
        return c;
    }

    async getRegistry() {
        try {
            const response = await fetch(this.options.portletListUrl);
            const data = await response.json();

            this.state.portlets = [];
            this.state.categories = [];

            if (data.registry.categories) {
                data.registry.categories.forEach(category => {
                    this.processCategory(category);
                });
            }

            // Handle uncategorized channels
            if (data.registry.channels && data.registry.channels.length > 0) {
                this.processCategory({
                    id: 'uncat',
                    name: 'Uncategorized',
                    description: 'Uncategorized Channels',
                    categories: [],
                    channels: data.registry.channels,
                });
            }

            this.fire('onLoad');
        } catch (error) {
            console.error('Failed to load portlet registry:', error);
        }
    }

    refreshRegistry() {
        return this.getRegistry();
    }

    getPortlet(id) {
        return this.state.portlets['portlet.' + id];
    }

    getCategory(key) {
        return this.state.categories[key];
    }

    getMemberPortlets(key, deepMembers) {
        const category = this.state.categories[key];
        return deepMembers ? category.deepPortlets : category.portlets;
    }

    getMemberCategories(key, deepMembers) {
        const category = this.state.categories[key];
        return deepMembers ? category.deepCategories : category.categories;
    }

    getAllCategories() {
        return this.state.categories;
    }

    getAllPortlets() {
        return this.state.portlets;
    }
}

// Maintain backward compatibility
up.PortletRegistry = function(container, options) {
    return new ModernPortletRegistry(container, options);
};