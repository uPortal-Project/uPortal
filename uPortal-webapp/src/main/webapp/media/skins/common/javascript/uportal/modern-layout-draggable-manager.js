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
            ...options,
        };
        this.currentDragData = null;
        this.boundHandleDragOver = this.handleDragOver.bind(this);
        this.boundHandleDragEnter = this.handleDragEnter.bind(this);
        this.boundHandleDragLeave = this.handleDragLeave.bind(this);
        this.boundHandleDrop = this.handleDrop.bind(this);
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
            observer.observe(portletList, {childList: true, subtree: true});
            this.initializeDraggables();
        }
    }

    initializeDraggables() {
        // Handle both gallery portlets and page portlets
        const galleryPortlets = this.container.querySelectorAll(
            '.portlet-list .portlet'
        );
        const pagePortlets = document.querySelectorAll(
            '[id*=portlet_].movable'
        );

        // Initialize gallery portlets (for Add Stuff functionality)
        for (const item of galleryPortlets) {
            if (item.draggable) continue; // Already initialized

            const dragHandle = item.querySelector('.portlet-thumb-gripper');
            if (!dragHandle) continue;

            // Make item draggable
            item.draggable = true;
            item.classList.add('draggable-portlet');

            // Store portlet data
            const titleElement = item.querySelector('.portlet-thumb-titlebar');
            const title = titleElement ? titleElement.textContent : 'Unknown';

            item.addEventListener('dragstart', (event_) => {
                this.currentDragData = {
                    id: item.dataset.portletId || item.id,
                    title: title,
                    element: item,
                };

                // Set drag image
                const dragImage = item.cloneNode(true);
                dragImage.style.opacity = '0.8';
                dragImage.style.transform = 'rotate(5deg)';
                document.body.append(dragImage);
                event_.dataTransfer.setDragImage(dragImage, 50, 25);

                // Clean up drag image after drag starts
                setTimeout(() => {
                    if (document.body.contains(dragImage)) {
                        dragImage.remove();
                    }
                }, 0);

                // Enable drop zones
                this.enableDropZones();

                event_.dataTransfer.effectAllowed = 'copy';
                event_.dataTransfer.setData('text/plain', title);
            });

            item.addEventListener('dragend', () => {
                this.disableDropZones();
                this.currentDragData = null;
            });
        }

        // Initialize page portlets (for moving existing portlets)
        for (const item of pagePortlets) {
            if (item.draggable) continue; // Already initialized

            const dragHandle = item.querySelector('.grab-handle');
            if (!dragHandle) continue;

            // Make item draggable
            item.draggable = true;
            item.classList.add('draggable-portlet');

            const titleElement = item.querySelector(
                '.up-portlet-titlebar .up-portlet-title'
            );
            const title = titleElement ? titleElement.textContent : 'Portlet';

            item.addEventListener('dragstart', (event_) => {
                this.currentDragData = {
                    id: window.up.defaultNodeIdExtractor(item),
                    title: title,
                    element: item,
                };

                // Enable drop zones
                this.enableDropZones();

                event_.dataTransfer.effectAllowed = 'move';
                event_.dataTransfer.setData('text/plain', title);
            });

            item.addEventListener('dragend', () => {
                this.disableDropZones();
                this.currentDragData = null;
            });
        }
    }

    enableDropZones() {
        const columns = document.querySelectorAll(
            '#portalPageBodyColumns .portal-page-column.canAddChildren, #portalPageBodyColumns .portal-page-column.up-fragment-admin'
        );

        for (const column of columns) {
            const inner = column.querySelector('.portal-page-column-inner');
            if (!inner) continue;

            // Create drop target
            const dropTarget = this.createDropTarget();
            this.positionDropTarget(column, dropTarget);

            // Set up drop events
            inner.addEventListener('dragover', this.boundHandleDragOver);
            inner.addEventListener('dragenter', this.boundHandleDragEnter);
            inner.addEventListener('dragleave', this.boundHandleDragLeave);
            inner.addEventListener('drop', this.boundHandleDrop);

            inner.classList.add('drop-enabled');
        }
    }

    disableDropZones() {
        const columns = document.querySelectorAll(
            '#portalPageBodyColumns .portal-page-column-inner.drop-enabled'
        );

        for (const inner of columns) {
            inner.removeEventListener('dragover', this.boundHandleDragOver);
            inner.removeEventListener('dragenter', this.boundHandleDragEnter);
            inner.removeEventListener('dragleave', this.boundHandleDragLeave);
            inner.removeEventListener('drop', this.boundHandleDrop);
            inner.classList.remove('drop-enabled');
        }

        // Remove all drop targets
        for (const target of document.querySelectorAll(
            '.layout-draggable-drop-target'
        )) {
            target.remove();
        }
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
        const lockedPortlets = [
            ...column.querySelectorAll(
                '[id*=portlet_].locked:not(.up-fragment-admin)'
            ),
        ];

        if (portlets.length > 0) {
            if (lockedPortlets.length > 0) {
                // Insert after last locked portlet
                const lastLocked = lockedPortlets.at(-1);
                lastLocked.parentNode.insertBefore(
                    dropTarget,
                    lastLocked.nextSibling
                );
            } else {
                // Insert before first movable portlet
                const firstMovable = column.querySelector(
                    '[id*=portlet_].movable, [id*=portlet_].up-fragment-admin'
                );
                if (firstMovable) {
                    firstMovable.parentNode.insertBefore(
                        dropTarget,
                        firstMovable
                    );
                } else {
                    column.append(dropTarget);
                }
            }
        } else {
            // Empty column - add to end
            const inner = column.querySelector('.portal-page-column-inner');
            if (inner) {
                inner.append(dropTarget);
            } else {
                column.append(dropTarget);
            }
        }
    }

    handleDragOver(event_) {
        event_.preventDefault();
        event_.dataTransfer.dropEffect = 'copy';
    }

    handleDragEnter(event_) {
        event_.preventDefault();
        const column = event_.currentTarget.closest('.portal-page-column');
        const dropTarget = column.querySelector(
            '.layout-draggable-drop-target'
        );
        if (dropTarget) {
            dropTarget.style.display = 'block';
        }
    }

    handleDragLeave(event_) {
        // Only hide if we're actually leaving the column
        const column = event_.currentTarget.closest('.portal-page-column');
        const rect = column.getBoundingClientRect();

        if (
            event_.clientX < rect.left ||
            event_.clientX > rect.right ||
            event_.clientY < rect.top ||
            event_.clientY > rect.bottom
        ) {
            const dropTarget = column.querySelector(
                '.layout-draggable-drop-target'
            );
            if (dropTarget) {
                dropTarget.style.display = 'none';
            }
        }
    }

    handleDrop(event_) {
        event_.preventDefault();

        if (!this.currentDragData) return;

        const column = event_.currentTarget.closest('.portal-page-column');
        const dropTarget = column.querySelector(
            '.layout-draggable-drop-target'
        );

        if (dropTarget) {
            dropTarget.classList.add('layout-draggable-target-dropped');

            // Determine target ID and method
            let targetID, method;

            const portlets = column.querySelectorAll('[id*=portlet_]');
            if (portlets.length > 0) {
                const movablePortlets = column.querySelectorAll(
                    '[id*=portlet_].movable, [id*=portlet_].up-fragment-admin'
                );
                if (movablePortlets.length > 0) {
                    // Check siblings around drop target
                    const previousSibling = dropTarget.previousElementSibling;
                    const nextSibling = dropTarget.nextElementSibling;

                    if (
                        previousSibling &&
                        previousSibling.id &&
                        previousSibling.id.includes('portlet_')
                    ) {
                        targetID =
                            window.up.defaultNodeIdExtractor(previousSibling);
                        method = 'appendAfter';
                    } else if (
                        nextSibling &&
                        nextSibling.id &&
                        nextSibling.id.includes('portlet_')
                    ) {
                        targetID =
                            window.up.defaultNodeIdExtractor(nextSibling);
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
                this.options.onDropTarget(
                    method,
                    targetID,
                    this.currentDragData
                );
            }
        }
    }
}

// Global initialization function to replace Fluid component
window.up = window.up || {};
window.up.LayoutDraggableManager = function (container, options) {
    const element =
        typeof container === 'string'
            ? document.querySelector(container)
            : container;
    return new ModernLayoutDraggableManager(element, options);
};

// Export class globally for modern components
window.ModernLayoutDraggableManager = ModernLayoutDraggableManager;
