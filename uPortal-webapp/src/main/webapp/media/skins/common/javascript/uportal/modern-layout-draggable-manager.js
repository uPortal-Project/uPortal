/*
 * Modern replacement for Fluid-based LayoutDraggableManager component
 * Replaces up-layout-draggable-manager.js with HTML5 drag and drop API
 */
'use strict';

class ModernLayoutDraggableManager {
    constructor(container, options = {}) {
        this.container = container;
        this.options = {
            onDropTarget: null,
            pseudoDropTargetLabel: 'Drop Here',
            ...options
        };
        this.currentDragData = null;
        this.init();
    }
    
    init() {
        this.setupDragAndDrop();
    }
    
    setupDragAndDrop() {
        // Set up drag and drop when portlet list is rendered
        const observer = new MutationObserver(() => {
            this.initializeDraggables();
        });
        
        const portletList = this.container.querySelector('.portlet-list');
        if (portletList) {
            observer.observe(portletList, { childList: true, subtree: true });
            this.initializeDraggables();
        }
    }
    
    initializeDraggables() {
        // Handle both gallery portlets and page portlets
        const galleryPortlets = this.container.querySelectorAll('.portlet-list .portlet');
        const pagePortlets = document.querySelectorAll('[id*=portlet_].movable');
        
        // Initialize gallery portlets (for Add Stuff functionality)
        galleryPortlets.forEach(item => {
            if (item.draggable) return; // Already initialized
            
            const dragHandle = item.querySelector('.portlet-thumb-gripper');
            if (!dragHandle) return;
            
            // Make item draggable
            item.draggable = true;
            item.classList.add('draggable-portlet');
            
            // Store portlet data
            const titleEl = item.querySelector('.portlet-thumb-titlebar');
            const title = titleEl ? titleEl.textContent : 'Unknown';
            
            item.addEventListener('dragstart', (e) => {
                this.currentDragData = {
                    id: item.getAttribute('data-portlet-id') || item.id,
                    title: title,
                    element: item
                };
                
                // Set drag image
                const dragImage = item.cloneNode(true);
                dragImage.style.opacity = '0.8';
                dragImage.style.transform = 'rotate(5deg)';
                document.body.appendChild(dragImage);
                e.dataTransfer.setDragImage(dragImage, 50, 25);
                
                // Clean up drag image after drag starts
                setTimeout(() => {
                    if (document.body.contains(dragImage)) {
                        document.body.removeChild(dragImage);
                    }
                }, 0);
                
                // Enable drop zones
                this.enableDropZones();
                
                e.dataTransfer.effectAllowed = 'copy';
                e.dataTransfer.setData('text/plain', title);
            });
            
            item.addEventListener('dragend', () => {
                this.disableDropZones();
                this.currentDragData = null;
            });
        });
        
        // Initialize page portlets (for moving existing portlets)
        pagePortlets.forEach(item => {
            if (item.draggable) return; // Already initialized
            
            const dragHandle = item.querySelector('.grab-handle');
            if (!dragHandle) return;
            
            // Make item draggable
            item.draggable = true;
            item.classList.add('draggable-portlet');
            
            const titleEl = item.querySelector('.up-portlet-titlebar .up-portlet-title');
            const title = titleEl ? titleEl.textContent : 'Portlet';
            
            item.addEventListener('dragstart', (e) => {
                this.currentDragData = {
                    id: window.up.defaultNodeIdExtractor(item),
                    title: title,
                    element: item
                };
                
                // Enable drop zones
                this.enableDropZones();
                
                e.dataTransfer.effectAllowed = 'move';
                e.dataTransfer.setData('text/plain', title);
            });
            
            item.addEventListener('dragend', () => {
                this.disableDropZones();
                this.currentDragData = null;
            });
        });
    }
    
    enableDropZones() {
        const columns = document.querySelectorAll('#portalPageBodyColumns .portal-page-column.canAddChildren, #portalPageBodyColumns .portal-page-column.up-fragment-admin');
        
        columns.forEach(column => {
            const inner = column.querySelector('.portal-page-column-inner');
            if (!inner) return;
            
            // Create drop target
            const dropTarget = this.createDropTarget();
            this.positionDropTarget(column, dropTarget);
            
            // Set up drop events
            inner.addEventListener('dragover', this.handleDragOver.bind(this));
            inner.addEventListener('dragenter', this.handleDragEnter.bind(this));
            inner.addEventListener('dragleave', this.handleDragLeave.bind(this));
            inner.addEventListener('drop', this.handleDrop.bind(this));
            
            inner.classList.add('drop-enabled');
        });
    }
    
    disableDropZones() {
        const columns = document.querySelectorAll('#portalPageBodyColumns .portal-page-column-inner.drop-enabled');
        
        columns.forEach(inner => {
            inner.removeEventListener('dragover', this.handleDragOver.bind(this));
            inner.removeEventListener('dragenter', this.handleDragEnter.bind(this));
            inner.removeEventListener('dragleave', this.handleDragLeave.bind(this));
            inner.removeEventListener('drop', this.handleDrop.bind(this));
            inner.classList.remove('drop-enabled');
        });
        
        // Remove all drop targets
        document.querySelectorAll('.layout-draggable-drop-target').forEach(target => {
            target.remove();
        });
    }
    
    createDropTarget() {
        const dropTarget = document.createElement('div');
        dropTarget.className = 'layout-draggable-drop-target';
        dropTarget.innerHTML = `<span>${this.options.pseudoDropTargetLabel}</span>`;
        dropTarget.style.cssText = `
            background: #e6f3ff;
            border: 2px dashed #0066cc;
            padding: 20px;
            margin: 10px 0;
            text-align: center;
            color: #0066cc;
            font-weight: bold;
            border-radius: 4px;
            display: none;
        `;
        return dropTarget;
    }
    
    positionDropTarget(column, dropTarget) {
        const portlets = column.querySelectorAll('[id*=portlet_]');
        const lockedPortlets = column.querySelectorAll('[id*=portlet_].locked:not(.up-fragment-admin)');
        
        if (portlets.length > 0) {
            if (lockedPortlets.length > 0) {
                // Insert after last locked portlet
                const lastLocked = lockedPortlets[lockedPortlets.length - 1];
                lastLocked.parentNode.insertBefore(dropTarget, lastLocked.nextSibling);
            } else {
                // Insert before first movable portlet
                const firstMovable = column.querySelector('[id*=portlet_].movable, [id*=portlet_].up-fragment-admin');
                if (firstMovable) {
                    firstMovable.parentNode.insertBefore(dropTarget, firstMovable);
                } else {
                    column.appendChild(dropTarget);
                }
            }
        } else {
            // Empty column - add to end
            const inner = column.querySelector('.portal-page-column-inner');
            if (inner) {
                inner.appendChild(dropTarget);
            } else {
                column.appendChild(dropTarget);
            }
        }
    }
    
    handleDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'copy';
    }
    
    handleDragEnter(e) {
        e.preventDefault();
        const column = e.currentTarget.closest('.portal-page-column');
        const dropTarget = column.querySelector('.layout-draggable-drop-target');
        if (dropTarget) {
            dropTarget.style.display = 'block';
        }
    }
    
    handleDragLeave(e) {
        // Only hide if we're actually leaving the column
        const column = e.currentTarget.closest('.portal-page-column');
        const rect = column.getBoundingClientRect();
        
        if (e.clientX < rect.left || e.clientX > rect.right || 
            e.clientY < rect.top || e.clientY > rect.bottom) {
            const dropTarget = column.querySelector('.layout-draggable-drop-target');
            if (dropTarget) {
                dropTarget.style.display = 'none';
            }
        }
    }
    
    handleDrop(e) {
        e.preventDefault();
        
        if (!this.currentDragData) return;
        
        const column = e.currentTarget.closest('.portal-page-column');
        const dropTarget = column.querySelector('.layout-draggable-drop-target');
        
        if (dropTarget) {
            dropTarget.classList.add('layout-draggable-target-dropped');
            
            // Determine target ID and method
            let targetID, method;
            
            const portlets = column.querySelectorAll('[id*=portlet_]');
            if (portlets.length > 0) {
                const movablePortlets = column.querySelectorAll('[id*=portlet_].movable, [id*=portlet_].up-fragment-admin');
                if (movablePortlets.length > 0) {
                    // Check siblings around drop target
                    const prevSibling = dropTarget.previousElementSibling;
                    const nextSibling = dropTarget.nextElementSibling;
                    
                    if (prevSibling && prevSibling.id && prevSibling.id.includes('portlet_')) {
                        targetID = window.up.defaultNodeIdExtractor(prevSibling);
                        method = 'appendAfter';
                    } else if (nextSibling && nextSibling.id && nextSibling.id.includes('portlet_')) {
                        targetID = window.up.defaultNodeIdExtractor(nextSibling);
                        method = 'insertBefore';
                    } else {
                        targetID = window.up.defaultNodeIdExtractor(column);
                        method = 'appendAfter';
                    }
                } else {
                    // All portlets are locked
                    targetID = window.up.defaultNodeIdExtractor(column);
                    method = 'appendAfter';
                }
            } else {
                // Empty column
                targetID = window.up.defaultNodeIdExtractor(column);
                method = 'appendAfter';
            }
            
            // Fire drop event
            if (this.options.onDropTarget) {
                this.options.onDropTarget(method, targetID, this.currentDragData);
            }
        }
    }
}

// Global initialization function to replace Fluid component
window.up = window.up || {};
window.up.LayoutDraggableManager = function(container, options) {
    const element = typeof container === 'string' ? document.querySelector(container) : container;
    return new ModernLayoutDraggableManager(element, options);
};

// Export class globally for modern components
window.ModernLayoutDraggableManager = ModernLayoutDraggableManager;