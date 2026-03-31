/*
 * Modern replacement for Fluid FragmentPermissionsManager
 * Handles permission dialogs and lock icon management
 */
'use strict';

class ModernFragmentPermissionsManager {
    constructor(container, options = {}) {
        this.container = container;
        this.options = {
            savePermissionsUrl: 'mvc/layout',
            selectors: {
                pageDialog: '.edit-page-permissions-dialog',
                pageDialogLink: '#editPagePermissionsLink',
                columnDialog: '.edit-column-permissions-dialog',
                columnDialogLink: '.portal-column-permissions-link',
                portletDialog: '.edit-portlet-permissions-dialog',
                portletDialogLink: '.portlet-permissions-link'
            },
            cssClassNames: {
                movable: 'movable',
                editable: 'editable',
                deletable: 'deletable',
                addChildAllowed: 'canAddChildren'
            },
            messages: {
                columnX: 'Column {0}'
            },
            ...options
        };
        
        this.menus = {};
        this.init();
    }
    
    init() {
        this.initializeDialogs();
        this.bindEvents();
    }
    
    initializeDialogs() {
        // Initialize page permissions dialog
        const pageDialog = document.querySelector(this.options.selectors.pageDialog);
        if (pageDialog) {
            this.menus.pagePermissionsManager = new PermissionsMenu(pageDialog, {
                savePermissionsUrl: this.options.savePermissionsUrl,
                elementExtractor: () => document.querySelector('#portalNavigationList li.active'),
                titleExtractor: () => {
                    const link = document.querySelector('#portalNavigationList li.active a.portal-navigation-link');
                    return link ? link.getAttribute('title') : '';
                }
            });
        }
        
        // Initialize column permissions dialog
        const columnDialog = document.querySelector(this.options.selectors.columnDialog);
        if (columnDialog) {
            this.menus.columnPermissionsManager = new PermissionsMenu(columnDialog, {
                savePermissionsUrl: this.options.savePermissionsUrl,
                elementExtractor: (link) => link.closest('.portal-page-column'),
                titleExtractor: (element) => {
                    const columns = document.querySelectorAll('.portal-page-column');
                    const index = Array.from(columns).indexOf(element) + 1;
                    return this.options.messages.columnX.replace('{0}', index);
                }
            });
        }
        
        // Initialize portlet permissions dialog
        const portletDialog = document.querySelector(this.options.selectors.portletDialog);
        if (portletDialog) {
            this.menus.portletPermissionsManager = new PermissionsMenu(portletDialog, {
                savePermissionsUrl: this.options.savePermissionsUrl,
                elementExtractor: (link) => link.closest('.up-portlet-wrapper'),
                titleExtractor: (element) => {
                    const titleLink = element.querySelector('.up-portlet-wrapper-inner h2 a:first-child');
                    return titleLink ? titleLink.textContent : '';
                },
                onUpdatePermissions: (element, newPermissions) => {
                    this.updatePortletLockIcon(element, newPermissions);
                }
            });
        }
    }
    
    bindEvents() {
        // Page permissions link
        const pageLink = document.querySelector(this.options.selectors.pageDialogLink);
        if (pageLink && this.menus.pagePermissionsManager) {
            pageLink.addEventListener('click', (e) => {
                e.preventDefault();
                this.menus.pagePermissionsManager.refresh(pageLink);
                this.openDialog(this.options.selectors.pageDialog);
            });
        }
        
        // Column permissions links (delegated event handling)
        document.addEventListener('click', (e) => {
            const columnLink = e.target.closest(this.options.selectors.columnDialogLink);
            if (columnLink && this.menus.columnPermissionsManager) {
                e.preventDefault();
                this.menus.columnPermissionsManager.refresh(columnLink);
                this.openDialog(this.options.selectors.columnDialog);
            }
        });
        
        // Portlet permissions links (delegated event handling)
        document.addEventListener('click', (e) => {
            const portletLink = e.target.closest(this.options.selectors.portletDialogLink);
            if (portletLink && this.menus.portletPermissionsManager) {
                e.preventDefault();
                this.menus.portletPermissionsManager.refresh(portletLink);
                this.openDialog(this.options.selectors.portletDialog);
            }
        });
    }
    
    openDialog(selector) {
        const dialog = document.querySelector(selector);
        if (dialog && window.up && window.up.jQuery && window.up.jQuery.fn.dialog) {
            const $dialog = window.up.jQuery(dialog);
            
            // Initialize dialog if not already done
            if (!$dialog.hasClass('ui-dialog-content')) {
                $dialog.dialog({
                    width: 550,
                    modal: true,
                    autoOpen: true
                });
            } else {
                $dialog.dialog('open');
            }
        }
    }
    
    updatePortletLockIcon(element, newPermissions) {
        if (newPermissions.movable) {
            element.classList.remove('locked');
            // Remove lock icon if it exists
            const lockIcon = element.querySelector('.locked-icon');
            if (lockIcon) {
                lockIcon.remove();
            }
        } else {
            element.classList.add('locked');
            // Add lock icon if it doesn't exist
            const controls = element.querySelector('.portlet-controls');
            if (controls && !controls.querySelector('.locked-icon')) {
                const lockIcon = document.createElement('div');
                lockIcon.className = 'locked-icon';
                lockIcon.title = 'This portlet may not be moved';
                lockIcon.innerHTML = `
                    <i class="fa fa-lock" aria-hidden="true"></i>
                    <span class="sr-only">This portlet may not be moved</span>
                `;
                controls.insertBefore(lockIcon, controls.firstChild);
            }
        }
    }
}

class PermissionsMenu {
    constructor(container, options = {}) {
        this.container = container;
        this.options = {
            savePermissionsUrl: 'mvc/layout',
            elementExtractor: null,
            titleExtractor: null,
            onUpdatePermissions: null,
            ...options
        };
        
        this.init();
    }
    
    init() {
        this.bindFormSubmission();
    }
    
    refresh(link) {
        const element = this.options.elementExtractor ? this.options.elementExtractor(link) : null;
        if (!element) return false;
        
        this.populateForm(element);
        this.currentElement = element;
        return false;
    }
    
    populateForm(element) {
        const form = this.container.querySelector('form');
        if (!form) return;
        
        // Set the hidden node ID
        const nodeIdInput = form.querySelector('[name=nodeId]');
        if (nodeIdInput) {
            nodeIdInput.value = this.extractNodeId(element);
        }
        
        // Set permission checkboxes based on CSS classes
        this.setCheckbox(form, 'movable', element.classList.contains('movable'));
        this.setCheckbox(form, 'deletable', element.classList.contains('deletable'));
        this.setCheckbox(form, 'editable', element.classList.contains('editable'));
        this.setCheckbox(form, 'addChildAllowed', element.classList.contains('canAddChildren'));
        
        // Set form title
        const titleElement = this.container.querySelector('h2');
        if (titleElement && this.options.titleExtractor) {
            titleElement.textContent = this.options.titleExtractor(element);
        }
    }
    
    setCheckbox(form, name, checked) {
        const checkbox = form.querySelector(`[name=${name}]`);
        if (checkbox) {
            checkbox.checked = checked;
        }
    }
    
    bindFormSubmission() {
        const form = this.container.querySelector('form');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.updatePermissions();
                return false;
            });
        }
    }
    
    async updatePermissions() {
        const form = this.container.querySelector('form');
        if (!form || !this.currentElement) return;
        
        const formData = new FormData(form);
        const data = {
            elementID: formData.get('nodeId'),
            action: 'updatePermissions',
            deletable: formData.has('deletable'),
            movable: formData.has('movable'),
            editable: formData.has('editable'),
            addChildAllowed: formData.has('addChildAllowed')
        };
        
        try {
            const response = await fetch(this.options.savePermissionsUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            });
            
            if (response.ok) {
                this.updatePermissionClasses(this.currentElement, data);
                this.closeDialog();
                
                // Fire update event
                if (this.options.onUpdatePermissions) {
                    this.options.onUpdatePermissions(this.currentElement, data);
                }
            } else {
                console.error('Failed to update permissions:', response.statusText);
            }
        } catch (error) {
            console.error('Error updating permissions:', error);
        }
    }
    
    updatePermissionClasses(element, permissions) {
        this.setClass(element, permissions.deletable, 'deletable');
        this.setClass(element, permissions.movable, 'movable');
        this.setClass(element, permissions.editable, 'editable');
        this.setClass(element, permissions.addChildAllowed, 'canAddChildren');
    }
    
    setClass(element, permission, className) {
        if (permission === undefined) return;
        
        if (permission) {
            element.classList.add(className);
        } else {
            element.classList.remove(className);
        }
    }
    
    closeDialog() {
        if (window.up && window.up.jQuery && window.up.jQuery.fn.dialog) {
            window.up.jQuery(this.container).dialog('close');
            // Force cleanup of any modal artifacts
            window.up.jQuery('.ui-widget-overlay, .modal-backdrop').remove();
            document.body.style.overflow = '';
        }
    }
    
    extractNodeId(element) {
        // Use the same logic as up.defaultNodeIdExtractor
        if (window.up && window.up.defaultNodeIdExtractor) {
            return window.up.defaultNodeIdExtractor(element);
        }
        
        // Fallback: extract from ID attribute
        const id = element.id;
        if (id) {
            const match = id.match(/^[^_]+_(.+)$/);
            return match ? match[1] : id;
        }
        
        return '';
    }
}

// Global initialization function to replace Fluid component
window.up = window.up || {};
window.up.FragmentPermissionsManager = function(container, options) {
    return new ModernFragmentPermissionsManager(container, options);
};