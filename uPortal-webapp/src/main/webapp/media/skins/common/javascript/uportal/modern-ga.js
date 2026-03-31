/*
 * Modern replacement for Google Analytics integration
 * Replaces up-ga.js with vanilla JavaScript implementation
 */
'use strict';

class ModernGoogleAnalytics {
    constructor() {
        this.propertyConfig = null;
        this.init();
    }

    init() {
        this.propertyConfig = this.findPropertyConfig();
        
        if (!this.propertyConfig) {
            return;
        }

        this.configureDefaults(this.propertyConfig);
        this.createTracker(this.propertyConfig);
        
        const dimensions = this.getDimensions(this.propertyConfig);
        
        // Page Event
        const pageVariables = this.getPageVariables();
        
        // Don't send view in MAX WindowState
        if (window.up?.analytics?.pageData?.urlState !== 'MAX') {
            window.up.gtag('event', 'page_view', { ...pageVariables, ...dimensions });
        }

        // Timing event
        window.up.gtag('event', 'timing_complete', {
            event_category: 'tab',
            name: this.getTabUri(),
            value: window.up?.analytics?.pageData?.executionTimeNano,
            ...pageVariables,
            ...dimensions
        });

        // Portlet Events
        this.trackPortletEvents(dimensions);
        
        // Add event handlers
        this.addFlyoutHandlers();
        this.addExternalLinkHandlers();
        this.addMobileListTabHandlers();
    }

    findPropertyConfig() {
        if (!window.up?.analytics?.model) {
            return null;
        }

        const model = window.up.analytics.model;
        
        if (Array.isArray(model.hosts)) {
            const config = model.hosts.find(config => 
                config.name === window.up.analytics.host
            );
            if (config) return config;
        }

        return model.defaultConfig;
    }

    configureDefaults(propertyConfig) {
        const defaults = propertyConfig.config || [];
        defaults.forEach(setting => {
            Object.keys(setting).forEach(key => {
                window.up.gtag('set', key, setting[key]);
            });
        });
    }

    getDimensions(propertyConfig) {
        const dimensions = {};
        (propertyConfig.dimensionGroups || []).forEach(setting => {
            dimensions[`dimension${setting.name}`] = setting.value;
        });
        return dimensions;
    }

    createTracker(propertyConfig) {
        const createSettings = {};
        (propertyConfig.config || []).forEach(setting => {
            if (setting.name !== 'name') {
                createSettings[setting.name] = setting.value;
            }
        });
        
        window.up.gtag('config', propertyConfig.propertyId, {
            send_page_view: false
        });
    }

    getTabUri(fragmentName, tabName) {
        const pageData = window.up?.analytics?.pageData;
        if (pageData?.tab) {
            fragmentName = fragmentName || pageData.tab.fragmentName;
            tabName = tabName || pageData.tab.tabName;
        }

        let uri = '/';
        if (fragmentName) {
            uri += `tab/${fragmentName}`;
            if (tabName) {
                uri += `/${tabName}`;
            }
        }
        return uri;
    }

    getPageVariables(fragmentName, tabName) {
        const pageData = window.up?.analytics?.pageData;
        if (pageData?.tab) {
            fragmentName = fragmentName || pageData.tab.fragmentName;
            tabName = tabName || pageData.tab.tabName;
        }

        let title;
        if (tabName) {
            title = `Tab: ${tabName}`;
        } else if (!pageData?.urlState) {
            title = 'Portal Home';
        } else {
            title = 'No Tab';
        }

        return {
            page_location: this.getTabUri(fragmentName, tabName),
            page_title: title
        };
    }

    getPortletFname(windowId) {
        const portletData = window.up?.analytics?.portletData?.[windowId];
        return portletData?.fname || windowId;
    }

    getRenderedPortletTitle(windowId) {
        const wrapper = document.querySelector(`div.up-portlet-windowId-content-wrapper.${windowId}`);
        if (!wrapper) return this.getPortletFname(windowId);

        const portletWrapper = wrapper.closest('div.up-portlet-wrapper-inner');
        if (!portletWrapper) return this.getPortletFname(windowId);

        const titleLink = portletWrapper.querySelector('div.up-portlet-titlebar h2 a');
        return titleLink?.textContent?.trim() || this.getPortletFname(windowId);
    }

    getPortletUri(fname) {
        return `/portlet/${fname}`;
    }

    getPortletVariables(windowId, portletData) {
        const portletTitle = this.getRenderedPortletTitle(windowId);
        
        if (!portletData) {
            portletData = window.up?.analytics?.portletData?.[windowId];
        }
        
        return {
            page_title: `Portlet: ${portletTitle}`,
            page_location: this.getPortletUri(portletData.fname)
        };
    }

    getInfoClass(element, excludedClasses) {
        if (!Array.isArray(excludedClasses)) {
            excludedClasses = [excludedClasses];
        }

        const classAttribute = element?.className;
        if (!classAttribute) return null;

        const classes = classAttribute.split(/\s+/);
        return classes.find(cls => !excludedClasses.includes(cls));
    }

    getFlyoutFname(clickedLink) {
        const wrapper = clickedLink.closest('div.up-portlet-fname-subnav-wrapper');
        return this.getInfoClass(wrapper, 'up-portlet-fname-subnav-wrapper');
    }

    getExternalLinkWindowId(clickedLink) {
        const wrapper = clickedLink.closest('div.up-portlet-windowId-content-wrapper');
        return this.getInfoClass(wrapper, 'up-portlet-windowId-content-wrapper');
    }

    handleLinkClickEvent(event, clickedLink, eventOptions) {
        const newWindow = event.button === 1 || 
                         event.metaKey || 
                         event.ctrlKey || 
                         clickedLink.target;

        const clickFunction = newWindow ? 
            () => {} : 
            () => { document.location = clickedLink.href; };

        window.up.gtag('event', 'page_view', {
            event_callback: clickFunction,
            ...eventOptions
        });

        if (!newWindow) {
            setTimeout(clickFunction, 200);
            event.preventDefault();
        }
    }

    trackPortletEvents(dimensions) {
        const portletData = window.up?.analytics?.portletData || {};
        
        Object.entries(portletData).forEach(([windowId, data]) => {
            if (data.fname === 'google-analytics-config') {
                return;
            }

            const portletVariables = this.getPortletVariables(windowId, data);
            window.up.gtag('event', 'page_view', portletVariables);
            window.up.gtag('event', 'timing_complete', {
                event_category: 'tab',
                name: this.getTabUri(),
                value: window.up?.analytics?.pageData?.executionTimeNano,
                ...portletVariables,
                ...dimensions
            });
        });
    }

    addFlyoutHandlers() {
        document.addEventListener('click', (event) => {
            const link = event.target.closest('ul.fl-tabs li.portal-navigation a.portal-subnav-link');
            if (!link) return;

            const portletFlyoutTitle = link.querySelector('span.portal-subnav-label')?.textContent || '';
            const fname = this.getFlyoutFname(link);
            const pageVariables = this.getPageVariables();

            this.handleLinkClickEvent(event, link, {
                event_category: 'Flyout Link',
                event_action: this.getPortletUri(fname),
                event_label: portletFlyoutTitle,
                ...pageVariables
            });
        });
    }

    addExternalLinkHandlers() {
        document.addEventListener('click', (event) => {
            const link = event.target.closest('a');
            if (!link) return;

            const linkHost = new URL(link.href, document.baseURI).hostname;
            if (linkHost && linkHost !== document.domain) {
                const windowId = this.getExternalLinkWindowId(link);
                const eventVariables = windowId ? 
                    this.getPortletVariables(windowId) : 
                    this.getPageVariables();

                this.handleLinkClickEvent(event, link, {
                    event_category: 'Outbound Link',
                    event_action: link.href,
                    event_label: link.textContent,
                    ...eventVariables
                });
            }
        });
    }

    addMobileListTabHandlers() {
        document.addEventListener('click', (event) => {
            const tab = event.target.closest('ul.up-portal-nav li.up-tab');
            if (!tab || tab.classList.contains('up-tab-open')) return;

            const ownerDiv = tab.querySelector('div.up-tab-owner');
            const fragmentName = this.getInfoClass(ownerDiv, 'up-tab-owner');
            const tabName = tab.querySelector('span.up-tab-name')?.textContent?.trim();
            const pageVariables = this.getPageVariables(fragmentName, tabName);

            window.up.gtag('event', 'page_view', pageVariables);
        });
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new ModernGoogleAnalytics();
});