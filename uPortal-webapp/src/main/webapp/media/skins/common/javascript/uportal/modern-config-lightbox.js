/*
 * Modern replacement for configuration lightbox functionality
 * Replaces up-config-lightbox.js with vanilla JavaScript implementation
 */
'use strict';

class ModernConfigLightbox {
    constructor(options = {}) {
        this.options = {
            selectors: {
                editLinks: '[data-lightbox-url]',
                lightbox: '#config-lightbox',
                title: '#config-lightbox .modal-title',
                loading: '#config-lightbox .loading',
                content: '#config-lightbox .modal-body-content'
            },
            lightboxOptions: {
                backdrop: 'static',
                show: true
            },
            ...options
        };
        
        this.modal = null;
        this.init();
    }

    init() {
        this.bindEvents();
    }

    bindEvents() {
        document.addEventListener('click', (event) => {
            const link = event.target.closest(this.options.selectors.editLinks);
            if (!link) return;

            event.preventDefault();
            event.stopPropagation();

            this.openLightbox(link);
        });
    }

    async openLightbox(link) {
        const url = link.dataset.lightboxUrl;
        const title = link.dataset.lightboxTitle;

        if (!url) {
            console.error('No lightbox URL found');
            return;
        }

        // Set title if provided
        if (title) {
            const titleElement = document.querySelector(this.options.selectors.title);
            if (titleElement) {
                titleElement.textContent = title;
            }
        }

        // Show loading, hide content
        const loadingElement = document.querySelector(this.options.selectors.loading);
        const contentElement = document.querySelector(this.options.selectors.content);
        
        if (contentElement) contentElement.style.display = 'none';
        if (loadingElement) loadingElement.style.display = 'block';

        // Show modal
        const lightboxElement = document.querySelector(this.options.selectors.lightbox);
        if (lightboxElement) {
            this.modal = new bootstrap.Modal(lightboxElement, this.options.lightboxOptions);
            this.modal.show();
        }

        try {
            const content = await this.loadContent(url);
            this.processContent(content);
            
            // Hide loading, show content
            if (loadingElement) {
                loadingElement.style.opacity = '0';
                loadingElement.style.transition = 'opacity 0.3s';
                setTimeout(() => {
                    loadingElement.style.display = 'none';
                    loadingElement.style.opacity = '1';
                }, 300);
            }
            
            if (contentElement) {
                contentElement.style.display = 'block';
                contentElement.style.opacity = '0';
                contentElement.style.transition = 'opacity 0.3s';
                setTimeout(() => {
                    contentElement.style.opacity = '1';
                }, 50);
            }
        } catch (error) {
            console.error('Error loading lightbox content:', error);
            // Fallback to classic inline method
            window.location.href = link.href;
        }
    }

    async loadContent(url) {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'text/html'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.text();
    }

    processContent(content) {
        const contentElement = document.querySelector(this.options.selectors.content);
        if (!contentElement) return;

        // Create temporary container to process HTML
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = content;

        // Rewrite form action URLs
        tempDiv.querySelectorAll('form').forEach(form => {
            const action = form.getAttribute('action');
            if (action) {
                form.setAttribute('action', this.convertExclusiveUrlToPageUrl(action));
            }
        });

        // Rewrite link href URLs
        tempDiv.querySelectorAll('a').forEach(link => {
            const href = link.getAttribute('href');
            if (href) {
                link.setAttribute('href', this.convertExclusiveUrlToPageUrl(href));
            }
        });

        // Clear content and append processed HTML
        contentElement.innerHTML = '';
        contentElement.appendChild(tempDiv);
    }

    convertExclusiveUrlToPageUrl(url) {
        if (!url) return url;

        let newUrl = url;
        let currentPagePortletId = null;
        let portletId = null;
        let state = 'normal';

        // Extract current page portlet ID
        const currentMatches = /\/p\/([^/]+)\//.exec(window.location.pathname);
        if (currentMatches && currentMatches[1]) {
            currentPagePortletId = currentMatches[1];
        }

        // Extract URL portlet ID
        const urlMatches = /\/p\/([^/]+)\//.exec(url);
        if (urlMatches && urlMatches[1]) {
            portletId = urlMatches[1];
        }

        // Try to retain state if editing the same portlet
        if (currentPagePortletId && portletId === currentPagePortletId) {
            const stateMatches = /\/(normal|maximized|exclusive|detached)\/render\.uP/.exec(
                window.location.pathname
            );
            if (stateMatches && stateMatches[1]) {
                state = stateMatches[1];
            }
        }

        // Replace exclusive state with determined state
        newUrl = url.replace('/exclusive/', `/${state}/`);

        return newUrl;
    }

    hide() {
        if (this.modal) {
            this.modal.hide();
        }
    }
}

// Global initialization
window.up = window.up || {};
window.up.lightboxConfig = {
    init: function(options) {
        if (!window.up.modernConfigLightbox) {
            window.up.modernConfigLightbox = new ModernConfigLightbox(options);
        }
        return window.up.modernConfigLightbox;
    }
};

// Auto-initialize with default options
document.addEventListener('DOMContentLoaded', () => {
    window.up.lightboxConfig.init();
});