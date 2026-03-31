/*
 * Modern replacement for Fluid-based TabManager component
 * Replaces up-tab-manager.js with vanilla JavaScript implementation
 */
'use strict';

class ModernTabManager {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.options = {
            selectors: {
                text: '.flc-inlineEdit-text',
                edit: '.flc-inlineEditable', 
                remove: '.portal-navigation-delete',
                add: '.portal-navigation-add',
                columns: '.flc-reorderer-column',
                modules: '.movable,.up-fragment-admin',
                lockedModules: '.locked:not(.up-fragment-admin)',
                grabHandle: '.portal-navigation-gripper',
                tabList: '#portalNavigationList',
                tabListItems: '.portal-navigation',
                tabGroup: '#activeTabGroup'
            },
            styles: {
                lockedTab: 'locked',
                singleTab: 'single', 
                firstTab: 'first',
                lastTab: 'last'
            },
            addTabLabel: 'My Tab',
            addTabWidths: [50, 50],
            insertBefore: 'insertBefore',
            appendAfter: 'appendAfter',
            tabContext: 'header',
            numberOfPortlets: 0,
            submitOnEnter: true,
            ...options
        };

        this.events = {
            onTabEdit: options.onTabEdit || (() => {}),
            onTabRemove: options.onTabRemove || (() => {}),
            onTabAdd: options.onTabAdd || (() => {}),
            onTabMove: options.onTabMove || (() => {})
        };

        this.inlineEditor = null;
        this.reorderLayout = null;
        this.init();
    }

    init() {
        this.initializeInlineEdit();
        this.initializeRemoveHandler();
        this.initializeAddHandler();
        this.initializeTabReordering();
    }

    initializeInlineEdit() {
        const editElement = this.container.querySelector(this.options.selectors.edit);
        const textElement = this.container.querySelector(this.options.selectors.text);
        
        if (!editElement || !textElement) return;

        // Modern inline editing without Fluid dependency
        this.setupModernInlineEdit(editElement, textElement);

        // Add hover effects for text editing (only for span elements)
        if (textElement.tagName === 'SPAN') {
            textElement.addEventListener('mouseenter', () => {
                textElement.style.cursor = 'text';
                textElement.focus();
            });

            textElement.addEventListener('mouseleave', () => {
                textElement.style.cursor = 'pointer';
                textElement.blur();
            });
        }

        // Auto-trigger edit mode for new tabs
        const numberOfPortlets = parseInt(this.options.numberOfPortlets);
        if (numberOfPortlets === 0 && textElement.textContent.trim() === this.options.addTabLabel) {
            setTimeout(() => {
                textElement.click();
            }, 100);
        }
    }

    hideEditControls() {
        const removeEl = this.container.querySelector(this.options.selectors.remove);
        const gripperEl = this.container.querySelector(this.options.selectors.grabHandle);
        const editEl = this.container.querySelector(this.options.selectors.edit);
        
        if (removeEl) removeEl.style.display = 'none';
        if (gripperEl) gripperEl.style.display = 'none';
        if (editEl) editEl.style.display = 'none';
    }

    showEditControls() {
        const removeEl = this.container.querySelector(this.options.selectors.remove);
        const gripperEl = this.container.querySelector(this.options.selectors.grabHandle);
        const editEl = this.container.querySelector(this.options.selectors.edit);
        
        if (removeEl) removeEl.style.display = '';
        if (gripperEl && gripperEl.classList.contains('active')) gripperEl.style.display = '';
        if (editEl) editEl.style.display = '';
    }

    initializeRemoveHandler() {
        const removeElement = this.container.querySelector(this.options.selectors.remove);
        if (removeElement) {
            removeElement.addEventListener('click', (e) => {
                e.preventDefault();
                // Find the tab container (li element) that contains this delete button
                const tabElement = e.target.closest('.portal-navigation');
                this.events.onTabRemove(tabElement);
            });
        }
    }

    initializeAddHandler() {
        // Look for add button in the navigation list, not just within this container
        const addElement = document.querySelector(this.options.selectors.add);
        if (addElement) {
            const tabGroupElement = this.container.querySelector(this.options.selectors.tabGroup);
            const tabGroup = tabGroupElement ? tabGroupElement.textContent.trim() : '';
            
            addElement.addEventListener('click', (e) => {
                e.preventDefault();
                this.events.onTabAdd(
                    this.options.addTabLabel,
                    this.options.addTabWidths,
                    tabGroup
                );
            });
        }
    }

    initializeTabReordering() {
        this.manageLockedTabs();
        this.setupReorderLayout();
    }

    manageLockedTabs() {
        const tabList = this.container.querySelector(this.options.selectors.tabList);
        if (!tabList) return;

        const lockedTabs = tabList.querySelectorAll(this.options.selectors.lockedModules);
        const lastLockedTab = lockedTabs[lockedTabs.length - 1];

        if (lastLockedTab) {
            // Hide grab handle on last locked tab
            const grabHandle = lastLockedTab.querySelector(this.options.selectors.grabHandle);
            if (grabHandle) grabHandle.style.display = 'none';

            // Add locked class and hide grab handles on all previous tabs
            let current = lastLockedTab.previousElementSibling;
            while (current) {
                current.classList.add(this.options.styles.lockedTab);
                const handle = current.querySelector(this.options.selectors.grabHandle);
                if (handle) handle.style.display = 'none';
                current = current.previousElementSibling;
            }
        }
    }

    setupReorderLayout() {
        // Modern drag and drop without Fluid dependency
        this.setupModernDragDrop();
    }

    handleTabMove(item) {
        const tab = item;
        const tabShortId = window.up.defaultNodeIdExtractor(tab);
        let method = this.options.insertBefore;
        let targetTabShortId = null;
        let tabPosition = 1;

        const listItems = this.container.querySelectorAll(this.options.selectors.tabListItems);

        // Determine method and target based on position
        if (tab === tab.parentNode.lastElementChild) {
            method = this.options.appendAfter;
            const prevTab = tab.previousElementSibling;
            if (prevTab && prevTab.id) {
                targetTabShortId = prevTab.id.split('_')[1];
            }
        } else {
            const nextTab = tab.nextElementSibling;
            if (nextTab && nextTab.id) {
                targetTabShortId = nextTab.id.split('_')[1];
            }
        }

        // If we couldn't determine a valid target, don't proceed
        if (!targetTabShortId) {
            console.warn('TabManager: Invalid tab move - no valid target found');
            return;
        }

        // Update tab positions and styles
        listItems.forEach((li, index) => {
            if (li.id === tab.id) {
                tabPosition = index + 1;
            }

            // Apply appropriate styles based on position
            li.classList.remove(
                this.options.styles.singleTab,
                this.options.styles.firstTab,
                this.options.styles.lastTab
            );

            if (listItems.length === 1) {
                li.classList.add(this.options.styles.singleTab);
            } else if (index === 0) {
                li.classList.add(this.options.styles.firstTab);
            } else if (index === listItems.length - 1) {
                li.classList.add(this.options.styles.lastTab);
            }
        });

        // Fire tab move event
        this.events.onTabMove(tabShortId, method, targetTabShortId, tabPosition);
    }

    locate(selector) {
        const selectorPath = this.options.selectors[selector];
        return selectorPath ? this.container.querySelector(selectorPath) : null;
    }

    setupModernInlineEdit(editElement, textElement) {
        // Check if textElement is already an input (tab in edit mode)
        if (textElement.tagName === 'INPUT') {
            this.setupExistingInput(textElement, null);
            return;
        }
        
        let isEditing = false;
        
        textElement.addEventListener('click', (e) => {
            // Prevent the parent link from navigating when clicking to edit
            e.preventDefault();
            e.stopPropagation();
            
            if (isEditing) return;
            
            isEditing = true;
            
            const originalText = textElement.textContent;
            const input = document.createElement('input');
            input.type = 'text';
            input.value = originalText;
            input.className = 'portal-navigation-label';
            
            // Size input to match text width
            const textWidth = textElement.offsetWidth;
            input.style.cssText = `display: inline-block; border: 1px solid #ccc; padding: 2px 4px; font-size: inherit; font-family: inherit; background: white; width: ${Math.max(textWidth, 60)}px;`;
            
            let isFinishing = false;
            const finishEdit = () => {
                if (isFinishing) return;
                isFinishing = true;
                
                const newValue = input.value.trim();
                textElement.textContent = newValue || originalText;
                textElement.style.display = 'inline';
                if (input.parentNode) {
                    input.parentNode.removeChild(input);
                }
                isEditing = false;
                
                if (newValue && newValue !== originalText) {
                    this.events.onTabEdit(newValue, originalText, editElement, textElement);
                }
            };
            
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    finishEdit();
                } else if (e.key === 'Escape') {
                    input.value = originalText;
                    finishEdit();
                }
            });
            
            input.addEventListener('blur', () => {
                setTimeout(finishEdit, 100);
            });
            
            // Replace the text element with the input
            textElement.style.display = 'none';
            textElement.parentNode.insertBefore(input, textElement.nextSibling);
            input.focus();
            input.select();
        });
    }
    
    setupExistingInput(input, textSpan) {
        const originalText = input.value;
        let isFinishing = false;
        
        input.style.cssText = `display: inline-block; border: 1px solid #ccc; padding: 2px 4px; font-size: inherit; font-family: inherit; background: white; width: ${Math.max(input.value.length * 8 + 20, 60)}px;`;
        
        const finishEdit = () => {
            if (isFinishing) return;
            isFinishing = true;
            
            const newValue = input.value.trim();
            
            // Convert input to span
            const span = document.createElement('span');
            span.className = 'portal-navigation-label flc-inlineEdit-text';
            span.textContent = newValue || originalText;
            
            input.parentNode.replaceChild(span, input);
            
            // Re-initialize editing for the new span
            this.setupModernInlineEdit(input.parentNode, span);
            
            if (newValue && newValue !== originalText) {
                this.events.onTabEdit(newValue, originalText, input.parentNode, span);
            }
        };
        
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                finishEdit();
            } else if (e.key === 'Escape') {
                input.value = originalText;
                finishEdit();
            }
        });
        
        input.addEventListener('blur', () => {
            setTimeout(finishEdit, 100);
        });
        
        input.focus();
        input.select();
    }

    setupModernDragDrop() {
        const modules = this.container.querySelectorAll(this.options.selectors.modules);
        
        modules.forEach(module => {
            if (module.classList.contains('locked')) return;
            
            module.draggable = true;
            
            module.addEventListener('dragstart', (e) => {
                e.dataTransfer.setData('text/plain', module.id);
                module.classList.add('dragging');
            });
            
            module.addEventListener('dragend', () => {
                module.classList.remove('dragging');
            });
            
            module.addEventListener('dragover', (e) => {
                e.preventDefault();
            });
            
            module.addEventListener('drop', (e) => {
                e.preventDefault();
                const draggedId = e.dataTransfer.getData('text/plain');
                const draggedModule = document.getElementById(draggedId);
                
                if (draggedModule && draggedModule !== module) {
                    const parent = module.parentNode;
                    const rect = module.getBoundingClientRect();
                    const midpoint = rect.left + rect.width / 2;
                    
                    if (e.clientX < midpoint) {
                        parent.insertBefore(draggedModule, module);
                    } else {
                        parent.insertBefore(draggedModule, module.nextSibling);
                    }
                    
                    this.handleTabMove(draggedModule);
                }
            });
        });
    }

    refresh() {
        // Modern refresh without Fluid dependency
        this.manageLockedTabs();
    }
}

// Global initialization function to replace Fluid component
window.up = window.up || {};
window.up.TabManager = function(container, options) {
    return new ModernTabManager(container, options);
};