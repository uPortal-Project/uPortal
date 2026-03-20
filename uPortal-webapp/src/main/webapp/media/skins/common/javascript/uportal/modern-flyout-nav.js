/*
 * Modern replacement for Fluid FlyoutNav
 * Handles navigation flyout menus with hover interactions
 */
'use strict';

class ModernFlyoutNav {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.options = {
            orientation: 'horizontal',
            horzalign: 'left', 
            vertalign: 'bottom',
            offset: 0,
            selectors: {
                flyoutMenu: '.dropdown-menu',
                flyoutList: '.portal-subnav-list',
                dropdownToggle: '.dropdown-toggle'
            },
            styles: {
                onTop: 'on-top'
            },
            ...options
        };
        
        this.flyoutMenu = null;
        this.flyoutList = null;
        this.isOpen = false;
        
        this.init();
    }
    
    init() {
        this.flyoutMenu = this.container.querySelector(this.options.selectors.flyoutMenu);
        this.flyoutList = this.container.querySelector(this.options.selectors.flyoutList);
        const dropdownToggle = this.container.querySelector(this.options.selectors.dropdownToggle);
        
        if (!this.flyoutMenu || !this.flyoutList) {
            return; // No flyout elements found
        }
        
        this.dropdownToggle = dropdownToggle;
        this.bindEvents();
    }
    
    bindEvents() {
        this.container.addEventListener('mouseenter', () => {
            if (this.flyoutList.innerHTML.trim() !== '') {
                this.openFlyout();
            }
        });
        
        this.container.addEventListener('mouseleave', () => {
            this.closeFlyout();
        });

        if (this.dropdownToggle) {
            this.dropdownToggle.addEventListener('click', (e) => {
                e.preventDefault();
                if (this.isOpen) {
                    this.closeFlyout();
                } else if (this.flyoutList.innerHTML.trim() !== '') {
                    this.openFlyout();
                }
            });
        }
    }
    
    openFlyout() {
        if (this.isOpen) return;

        // Make measurable but invisible before positioning
        this.flyoutMenu.style.visibility = 'hidden';
        this.flyoutMenu.style.display = 'block';
        this.calculatePosition();
        this.flyoutMenu.style.visibility = '';
        if (this.dropdownToggle) {
            this.dropdownToggle.setAttribute('aria-expanded', 'true');
        }
        this.isOpen = true;
    }
    
    closeFlyout() {
        if (!this.isOpen) return;
        
        this.flyoutMenu.style.display = 'none';
        if (this.dropdownToggle) {
            this.dropdownToggle.setAttribute('aria-expanded', 'false');
        }
        this.isOpen = false;
    }
    
    calculatePosition() {
        const containerRect = this.container.getBoundingClientRect();
        const flyoutRect = this.flyoutMenu.getBoundingClientRect();
        
        let top, left;
        
        if (this.options.orientation === 'horizontal') {
            // Horizontal orientation
            left = this.options.horzalign === 'left' 
                ? 0 
                : containerRect.width - flyoutRect.width;
                
            top = this.options.vertalign === 'bottom'
                ? containerRect.height - this.options.offset
                : -flyoutRect.height;
        } else {
            // Vertical orientation  
            left = this.options.horzalign === 'left'
                ? -flyoutRect.width
                : containerRect.width;
                
            top = this.options.vertalign === 'bottom'
                ? 0
                : -(flyoutRect.height - containerRect.height);
        }
        
        this.flyoutMenu.style.top = `${top}px`;
        this.flyoutMenu.style.left = `${left}px`;
    }
}

// Global initialization function to replace Fluid component
window.uportal = window.uportal || {};
window.uportal.flyoutmenu = function(container, options) {
    return new ModernFlyoutNav(container, options);
};