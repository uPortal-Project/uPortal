/*
 * Modern replacement for Fluid-based PortalGallery component
 * Replaces up-layout-gallery.js with vanilla JavaScript implementation
 */
'use strict';

class PortalGallery {
    constructor(container, options = {}) {
        this.container =
            typeof container === 'string'
                ? document.querySelector(container)
                : container;
        this.options = {...this.defaults, ...options};
        this.panes = new Map();
        this.isOpen = false;
        this.init();
    }

    get defaults() {
        return {
            isOpen: false,
            openSpeed: 500,
            closeSpeed: 50,
        };
    }

    init() {
        this.createPanes();
        this.bindEvents();

        this.isOpen = false;

        const outer = document.querySelector('#customizeOptions');
        const inner = this.container.querySelector('.gallery-inner');

        if (outer) outer.style.display = 'none';
        if (inner) inner.style.display = 'none';

        const handle = this.container.querySelector('.handle span');
        if (handle) handle.classList.remove('handle-arrow-up');

        if (this.options.isOpen) this.openGallery();
    }

    createPanes() {
        // Create browse content pane
        this.panes.set(
            'add-content',
            new BrowseContentPane(this.container, this, {
                key: 'add-content',
                selectors: {
                    pane: '.add-content',
                    paneLink: '.add-content-link',
                },
            })
        );

        // Create use content pane
        this.panes.set(
            'use-content',
            new UseContentPane(this.container, this, {
                key: 'use-content',
                selectors: {
                    pane: '.use-content',
                    paneLink: '.use-content-link',
                },
            })
        );

        this.panes.set(
            'skin',
            new SkinPane(this.container, this, {
                key: 'skin',
                selectors: {
                    pane: '.skins',
                    paneLink: '.skin-link',
                },
            })
        );

        this.panes.set(
            'layout',
            new LayoutPane(this.container, this, {
                key: 'layout',
                selectors: {
                    pane: '.layouts',
                    paneLink: '.layout-link',
                },
            })
        );
    }

    bindEvents() {
        // Gallery handle click
        const handle = this.container.querySelector('.handle span');
        if (handle) {
            handle.addEventListener('click', () => {
                this.isOpen ? this.closeGallery() : this.openGallery();
            });
        }

        // Customize button click
        setTimeout(() => {
            const customizeButton = document.querySelector('#customizeButton');
            if (customizeButton) {
                customizeButton.addEventListener('click', (event_) => {
                    event_.preventDefault();
                    event_.stopPropagation();
                    this.isOpen ? this.closeGallery() : this.openGallery();
                });
            }
        }, 100);

        // Close button
        const closeButton = this.container.querySelector('.close-button');
        if (closeButton) {
            closeButton.addEventListener('click', () => this.closeGallery());
        }
    }

    openGallery() {
        this.isOpen = true;
        const handle = this.container.querySelector('.handle span');
        const outer = document.querySelector('#customizeOptions');
        const inner = this.container.querySelector('.gallery-inner');

        const canAddChildren = document.querySelector(
            '#portalPageBodyColumns .portal-page-column.canAddChildren, #portalPageBodyColumns .portal-page-column.up-fragment-admin'
        );
        if (canAddChildren) {
            this.panes.get('add-content').showLoadingOnly();
        } else {
            this.panes.get('use-content').showLoadingOnly();
            this.hidePaneLink('add-content');
        }

        if (handle) handle.classList.add('handle-arrow-up');

        const customizeButton = document.querySelector('#customizeButton');
        const arrow = customizeButton?.querySelector('i');
        if (arrow) arrow.className = 'fa fa-caret-up';
        if (customizeButton)
            customizeButton.setAttribute('aria-expanded', 'true');

        if (outer && inner) {
            inner.style.display = 'block';
            up.jQuery(outer).slideDown(300, 'swing', () => {
                if (canAddChildren) {
                    this.panes.get('add-content').initializeContent();
                } else {
                    this.panes.get('use-content').initializeContent();
                }
            });
        }
    }

    closeGallery() {
        this.isOpen = false;
        const handle = this.container.querySelector('.handle span');
        const outer = document.querySelector('#customizeOptions');
        const inner = this.container.querySelector('.gallery-inner');

        if (handle) handle.classList.remove('handle-arrow-up');

        const customizeButton = document.querySelector('#customizeButton');
        const arrow = customizeButton?.querySelector('i');
        if (arrow) arrow.className = 'fa fa-caret-down';
        if (customizeButton)
            customizeButton.setAttribute('aria-expanded', 'false');

        if (outer && inner) {
            up.jQuery(outer).slideUp(300, 'swing', () => {
                inner.style.display = 'none';
            });
        }
    }

    showPane(key) {
        for (const [paneKey, pane] of this.panes.entries()) {
            if (paneKey === key) {
                pane.showPane();
            } else {
                pane.hidePane();
            }
        }
    }

    hidePaneLink(key) {
        const pane = this.panes.get(key);
        if (pane && pane.hidePaneLink) {
            pane.hidePaneLink();
        }
    }

    showPaneLink(key) {
        const pane = this.panes.get(key);
        if (pane && pane.showPaneLink) {
            pane.showPaneLink();
        }
    }

    showLoading() {
        const portletList = this.container.querySelector(
            '#addContentPortletList, #useContentPortletList, .portlet-list'
        );

        if (portletList) {
            portletList.innerHTML = `
                <div class="loading-indicator" style="
                    width: 100%;
                    height: 200px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    flex-direction: column;
                    color: #666;
                    font-size: 14px;
                ">
                    <div style="
                        width: 24px;
                        height: 24px;
                        border: 3px solid #ddd;
                        border-top: 3px solid #007bff;
                        border-radius: 50%;
                        animation: spin 1s linear infinite;
                        margin-bottom: 15px;
                    "></div>
                    Loading portlets...
                </div>
                <style>
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                </style>
            `;
        }
    }

    hideLoading() {
        // The loading indicator will be replaced by actual content in PortletListView.renderPortlets()
    }
}

class GalleryPane {
    constructor(container, gallery, options = {}) {
        this.container = container;
        this.gallery = gallery;
        this.options = options;
        this.initialized = false;
        this.bindEvents();
    }

    bindEvents() {
        const paneLink = this.container.querySelector(
            this.options.selectors.paneLink
        );
        if (paneLink) {
            paneLink.addEventListener('click', () => {
                this.gallery.showPane(this.options.key);
            });
        }
    }

    showPane() {
        const pane = this.container.querySelector(this.options.selectors.pane);
        const paneLink = this.container.querySelector(
            this.options.selectors.paneLink
        );

        if (pane) pane.style.display = 'block';
        if (paneLink) paneLink.classList.add('active');

        if (!this.initialized) {
            if (this.options.onInitialize) {
                this.options.onInitialize();
            }
            this.initialized = true;
        }
    }

    hidePane() {
        const pane = this.container.querySelector(this.options.selectors.pane);
        const paneLink = this.container.querySelector(
            this.options.selectors.paneLink
        );

        if (pane) pane.style.display = 'none';
        if (paneLink) paneLink.classList.remove('active');
    }

    hidePaneLink() {
        const pane = this.container.querySelector(this.options.selectors.pane);
        const paneLink = this.container.querySelector(
            this.options.selectors.paneLink
        );

        if (pane) pane.style.display = 'none';
        if (paneLink) {
            paneLink.style.display = 'none';
            paneLink.classList.remove('active');
        }
    }

    showPaneLink() {
        const paneLink = this.container.querySelector(
            this.options.selectors.paneLink
        );
        if (paneLink) paneLink.style.display = 'block';
    }

    showLoadingOnly() {
        const pane = this.container.querySelector(this.options.selectors.pane);
        const paneLink = this.container.querySelector(
            this.options.selectors.paneLink
        );

        if (pane) pane.style.display = 'block';
        if (paneLink) paneLink.classList.add('active');

        if (!this.initialized) {
            this.gallery.showLoading();
        }
    }
}

class BrowseContentPane extends GalleryPane {
    constructor(container, gallery, options) {
        super(container, gallery, options);
        this.portletBrowser = null;
    }

    initializeContent() {
        if (!this.initialized) {
            const pane = this.container.querySelector(
                this.options.selectors.pane
            );
            const startTime = Date.now();

            this.portletBrowser = new window.PortletBrowser(
                pane,
                this.gallery,
                {
                    portletListUrl: 'v4-3/dlm/portletRegistry.json',
                    buttonText: 'Add',
                    buttonAction: 'add',
                    onLoad: () => {
                        const delay = Math.max(
                            0,
                            1000 - (Date.now() - startTime)
                        );
                        setTimeout(() => this.gallery.hideLoading(), delay);
                    },
                }
            );
            this.initialized = true;
        }
    }

    showPane() {
        // For tab switching after initialization
        const pane = this.container.querySelector(this.options.selectors.pane);
        const paneLink = this.container.querySelector(
            this.options.selectors.paneLink
        );

        if (pane) pane.style.display = 'block';
        if (paneLink) paneLink.classList.add('active');
    }
}

class UseContentPane extends GalleryPane {
    constructor(container, gallery, options) {
        super(container, gallery, options);
        this.portletBrowser = null;
    }

    initializeContent() {
        if (!this.initialized) {
            const pane = this.container.querySelector(
                this.options.selectors.pane
            );
            const startTime = Date.now();

            this.portletBrowser = new window.PortletBrowser(
                pane,
                this.gallery,
                {
                    portletListUrl: 'v4-3/dlm/portletRegistry.json',
                    buttonText: 'Use',
                    buttonAction: 'use',
                    onLoad: () => {
                        const delay = Math.max(
                            0,
                            1000 - (Date.now() - startTime)
                        );
                        setTimeout(() => this.gallery.hideLoading(), delay);
                    },
                }
            );
            this.initialized = true;
        }
    }

    showPane() {
        // For tab switching after initialization
        const pane = this.container.querySelector(this.options.selectors.pane);
        const paneLink = this.container.querySelector(
            this.options.selectors.paneLink
        );

        if (pane) pane.style.display = 'block';
        if (paneLink) paneLink.classList.add('active');

        // Initialize content if not already done
        if (!this.initialized) {
            this.initializeContent();
        }
    }
}

class SkinPane extends GalleryPane {
    constructor(container, gallery, options) {
        super(container, gallery, options);
        this.skinSelector = null;
    }

    showPane() {
        if (!this.initialized) {
            this.gallery.showLoading();

            // Initialize skin selector
            const paneElement = this.container.querySelector(
                this.options.selectors.pane
            );
            if (paneElement) {
                this.skinSelector = new SkinSelector(paneElement, {
                    onSelectSkin: (skin) => {
                        // Use the modern persistence component
                        const persistence =
                            new window.ModernLayoutPreferencesPersistence(
                                document.body,
                                {
                                    saveLayoutUrl: '/uPortal/api/layout',
                                }
                            );

                        persistence.update(
                            {
                                action: 'chooseSkin',
                                skinName: skin.key,
                            },
                            () => {
                                window.location.reload();
                            }
                        );
                    },
                });
            }

            this.initialized = true;
            this.gallery.hideLoading();
        }

        super.showPane();
    }
}

// PortletBrowser is now in modern-portlet-browser.js

// PortletRegistry is now in modern-portlet-browser.js

// CategoryListView is now in modern-portlet-browser.js

// PortletListView is now in modern-portlet-browser.js

class LayoutPane extends GalleryPane {
    constructor(container, gallery, options) {
        super(container, gallery, options);
        this.layoutSelector = null;
    }

    showPane() {
        if (!this.initialized) {
            this.gallery.showLoading();

            // Initialize layout selector
            const paneElement = this.container.querySelector(
                this.options.selectors.pane
            );
            if (paneElement) {
                this.layoutSelector = new LayoutSelector(paneElement, {
                    onLayoutSelect: (layout) => {
                        // Use the modern persistence component
                        const persistence =
                            new window.ModernLayoutPreferencesPersistence(
                                document.body,
                                {
                                    saveLayoutUrl: '/uPortal/api/layout',
                                }
                            );

                        const getActiveTabId = () => {
                            const activeTab = document.querySelector(
                                '#portalNavigationList li.active'
                            );
                            return activeTab
                                ? window.up.defaultNodeIdExtractor(activeTab)
                                : null;
                        };

                        // Server expects at least 2 widths, pad single column with 0
                        const widths =
                            layout.columns.length === 1
                                ? [layout.columns[0], 0]
                                : layout.columns;

                        const options = {
                            action: 'changeColumns',
                            tabId: getActiveTabId(),
                            widths: widths,
                        };

                        persistence.update(options, (data) => {
                            if (data && data.error) {
                                console.error(
                                    'Layout update error:',
                                    data.error
                                );
                            } else {
                                window.location.reload();
                            }
                        });
                    },
                });
            }

            this.initialized = true;
            this.gallery.hideLoading();
        }

        super.showPane();
    }
}

class SkinSelector {
    constructor(container, options = {}) {
        this.container = container;
        this.options = options;
        this.skins = [];
        this.init();
    }

    async init() {
        try {
            await this.loadSkins();
            this.render();
        } catch (error) {
            console.error('Failed to load skins:', error);
        }
    }

    async loadSkins() {
        // Try to load skinList.xml from respondr skin directory
        try {
            const response = await fetch(
                '/uPortal/media/skins/respondr/skinList.xml'
            );
            if (response.ok) {
                const xmlText = await response.text();
                const parser = new DOMParser();
                const xmlDocument = parser.parseFromString(xmlText, 'text/xml');
                this.parseSkinListXML(xmlDocument);
            } else {
                this.useDefaultSkins();
            }
        } catch (error) {
            console.warn(
                'Could not load skinList.xml, using default skins:',
                error
            );
            this.useDefaultSkins();
        }
    }

    parseSkinListXML(xmlDocument) {
        const skinNodes = xmlDocument.querySelectorAll('skin');
        this.skins = [];

        for (const skinNode of skinNodes) {
            const key = skinNode.querySelector('skin-key')?.textContent;
            const name = skinNode.querySelector('skin-name')?.textContent;
            const description =
                skinNode.querySelector('skin-description')?.textContent;

            if (key && name) {
                this.skins.push({
                    key,
                    name,
                    description: description || name,
                    thumbnailPath: `/uPortal/media/skins/respondr/${key}/thumb.gif`,
                });
            }
        }
    }

    useDefaultSkins() {
        // Fallback to available skins
        this.skins = [
            {
                key: 'defaultSkin',
                name: 'Default Skin',
                description: 'Basic responsive skin',
                thumbnailPath:
                    '/uPortal/media/skins/respondr/defaultSkin/thumb.gif',
            },
        ];
    }

    render() {
        const skinsList = this.container.querySelector('.skins-list');
        if (!skinsList) return;

        skinsList.innerHTML = '';

        for (const skin of this.skins) {
            const skinElement = this.createSkinElement(skin);
            skinsList.append(skinElement);
        }
    }

    createSkinElement(skin) {
        const skinElement = document.createElement('li');
        skinElement.className = 'results-item skin';

        skinElement.innerHTML = `
            <div class="ri-wrapper skins-wrapper">
                <a class="ri-link skin-link" href="#">
                    <div class="ri-titlebar skin-titlebar">${skin.name}</div>
                    <div class="ri-content">
                        <div class="ri-icon skin-thumb" style="background: url(${skin.thumbnailPath}) top left no-repeat;">
                            <span>Thumbnail</span>
                        </div>
                    </div>
                </a>
            </div>
        `;

        const linkElement = skinElement.querySelector('.skin-link');
        if (linkElement) {
            linkElement.addEventListener('click', (event_) => {
                event_.preventDefault();

                // Remove active class from all skins
                for (const element of this.container.querySelectorAll(
                    '.skin.selected'
                )) {
                    element.classList.remove('selected');
                }

                // Add active class to selected skin
                skinElement.classList.add('selected');

                // Fire selection event
                if (this.options.onSelectSkin) {
                    this.options.onSelectSkin(skin);
                }
            });
        }

        return skinElement;
    }
}

class LayoutSelector {
    constructor(container, options = {}) {
        this.container = container;
        this.options = options;
        this.layouts = [
            {nameKey: 'fullWidth', columns: [100]},
            {nameKey: 'narrowWide', columns: [40, 60]},
            {nameKey: 'even', columns: [50, 50]},
            {nameKey: 'wideNarrow', columns: [60, 40]},
            {nameKey: 'even', columns: [33, 34, 33]},
            {nameKey: 'narrowWideNarrow', columns: [25, 50, 25]},
            {nameKey: 'even', columns: [25, 25, 25, 25]},
        ];
        this.strings = {
            fullWidth: 'Full-width',
            narrowWide: 'Narrow, wide',
            even: 'Even',
            wideNarrow: 'Wide, narrow',
            narrowWideNarrow: 'Narrow, wide, narrow',
            column: 'Column',
            columns: 'Columns',
        };
        this.currentLayout = this.getCurrentLayout().slice();
        this.init();
    }

    getCurrentLayout() {
        const columns = [];
        const columnElements = document.querySelectorAll(
            '#portalPageBodyColumns > [id^=column_]'
        );

        for (const col of columnElements) {
            const colMdClass = col.className.match(/col-md-(\d+)/);
            if (colMdClass) {
                columns.push(Math.round((Number(colMdClass[1]) / 12) * 100));
            } else {
                const flClass = col.className.match(/fl-container-flex(\d+)/);
                if (flClass) columns.push(Number(flClass[1]));
            }
        }

        if (columns.length === 0 && columnElements.length > 0) {
            const equalWidth = Math.floor(100 / columnElements.length);
            for (let index = 0; index < columnElements.length; index++)
                columns.push(equalWidth);
        }

        return columns.length > 0 ? columns : [100];
    }

    init() {
        this.render();
    }

    layoutsMatch(layout1, layout2) {
        if (layout1.length !== layout2.length) return false;

        // Allow for small differences due to Bootstrap grid rounding
        for (let index = 0; index < layout1.length; index++) {
            const diff = Math.abs(layout1[index] - layout2[index]);
            if (diff > 5) return false; // Allow up to 5% difference
        }
        return true;
    }

    render() {
        const layoutsList = this.container.querySelector('.layouts-list');
        if (!layoutsList) return;

        this.currentLayout = this.getCurrentLayout().slice();
        layoutsList.innerHTML = '';
        for (const layout of this.layouts) {
            layoutsList.append(this.createLayoutElement(layout));
        }
    }

    createLayoutElement(layout) {
        const layoutElement = document.createElement('li');
        const isSelected = this.layoutsMatch(
            this.currentLayout,
            layout.columns
        );

        layoutElement.className = `results-item layout ${isSelected ? 'selected' : ''}`;

        const columnText =
            layout.columns.length === 1
                ? this.strings.column
                : this.strings.columns;
        const layoutName = this.strings[layout.nameKey] || layout.nameKey;
        const layoutString = layout.columns.join('-');

        layoutElement.innerHTML = `
            <div class="ri-wrapper layout-wrapper">
                <a class="ri-link layout-link" href="#">
                    <div class="ri-titlebar layout-titlebar">${layout.columns.length} ${columnText}</div>
                    <div class="ri-content">
                        <div class="ri-icon layout-thumb" style="background: url(/uPortal/media/skins/respondr/common/images/layout_${layoutString}.svg) top left no-repeat;">
                            <span>Thumbnail</span>
                        </div>
                        <div class="ri-description layout-description">${layoutName}</div>
                    </div>
                </a>
            </div>
        `;

        const linkElement = layoutElement.querySelector('.layout-link');
        if (linkElement) {
            linkElement.addEventListener('click', (event_) => {
                event_.preventDefault();

                // Check if this is already the current layout
                if (this.layoutsMatch(this.currentLayout, layout.columns))
                    return;

                // Remove selected class from all layouts
                for (const element of this.container.querySelectorAll(
                    '.layout.selected'
                )) {
                    element.classList.remove('selected');
                }

                // Add selected class to clicked layout
                layoutElement.classList.add('selected');

                // Fire selection event BEFORE updating current layout
                if (this.options.onLayoutSelect) {
                    this.options.onLayoutSelect(layout);
                }

                // Update current layout after successful callback
                this.currentLayout = layout.columns.slice();
            });
        }

        return layoutElement;
    }
}

// LayoutDraggableManager is now in modern-layout-draggable-manager.js

// Global initialization function to replace Fluid component
window.up = window.up || {};
window.up.PortalGallery = function (container, options) {
    const gallery = new PortalGallery(container, options);
    window.up.gallery = gallery; // Store global reference
    return gallery;
};

// Export for use by up.LayoutPreferences
