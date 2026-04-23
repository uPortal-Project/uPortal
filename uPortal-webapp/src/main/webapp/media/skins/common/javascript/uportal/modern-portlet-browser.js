/*
 * Modern replacement for Fluid-based PortletBrowser component
 * Replaces up-portlet-browser.js with vanilla JavaScript implementation
 */
'use strict';

class PortletBrowser {
    constructor(container, gallery, options = {}) {
        this.container = container;
        this.gallery = gallery;
        this.options = {buttonText: 'Add', buttonAction: 'add', ...options};
        this.registry = null;
        this.dragManager = null;
        this.state = {
            currentCategory: '',
            portletRegex: null,
        };
        this.init();
    }

    async init() {
        try {
            this.registry = new PortletRegistry(this.options.portletListUrl);
            await this.registry.load();

            // Initialize drag manager for Add Content tab
            if (this.options.buttonAction === 'add') {
                this.dragManager = window.up.LayoutDraggableManager(
                    this.container,
                    {
                        onDropTarget: (method, targetID, portletData) => {
                            this.onPortletDrop(portletData, method, targetID);
                        },
                    }
                );
            }

            // Initialize category and portlet views
            this.categoryView = new CategoryListView(this.container, this);
            this.portletView = new PortletListView(this.container, this);

            // Set up search
            this.setupSearch();

            setTimeout(() => {
                if (this.options.onLoad) this.options.onLoad();
            }, 1000);
        } catch (error) {
            console.error('FLOW: PortletBrowser init failed:', error);
            // Still call onLoad on error to hide loading spinner
            if (this.options.onLoad) {
                this.options.onLoad();
            }
        }
    }

    setupSearch() {
        const searchInput = this.container.querySelector(
            '.portlet-search-input'
        );
        if (searchInput) {
            searchInput.addEventListener('input', (event_) => {
                const query = event_.target.value.trim();
                this.state.portletRegex = query ? new RegExp(query, 'i') : null;
                this.portletView.refresh();
            });
        }
    }

    onCategorySelect(category) {
        this.state.currentCategory = category.id;
        this.categoryView.refresh();
        this.portletView.refresh();
    }

    onPortletSelect(portlet) {
        if (this.options.buttonAction === 'use') {
            // "Use It" functionality - redirect to portlet's maximized view
            window.location = `/uPortal/p/${portlet.fname}`;
            return;
        }

        // "Add" functionality - use the same persistence mechanism as the original Fluid implementation
        this.addPortletToPage(portlet);
    }

    onPortletDrop(portlet, method, targetID) {
        // Handle drag and drop portlet addition
        const options = {
            action: 'addPortlet',
            channelID: portlet.id,
            elementID: targetID,
            position: method,
        };

        // Use the existing persistence component
        if (window.up && window.up.LayoutPreferencesPersistence) {
            const persistence = window.up.LayoutPreferencesPersistence(
                document.body,
                {
                    saveLayoutUrl: '/uPortal/api/layout',
                }
            );

            persistence.update(options, (data) => {
                if (data.error) {
                    console.error('Error adding portlet:', data.error);
                } else {
                    // Reload page to show new portlet
                    window.location.reload();
                }
            });
        }
    }

    addPortletToPage(portlet) {
        const getActiveTabId = () => {
            const activeTab = document.querySelector(
                '#portalNavigationList li.active'
            );
            return activeTab
                ? window.up.defaultNodeIdExtractor(activeTab)
                : null;
        };

        const options = {
            action: 'addPortlet',
            channelID: portlet.id,
        };

        // Find the first movable portlet to insert before
        const firstChannel = document.querySelector(
            '[id^=portlet_].movable, [id^=portlet_].up-fragment-admin'
        );

        if (firstChannel) {
            // Insert before first movable portlet
            options.elementID = window.up.defaultNodeIdExtractor(firstChannel);
            options.position = 'insertBefore';
        } else {
            // No content on page, add to tab
            options.elementID = getActiveTabId();
        }

        // Use the modern persistence component
        const persistence = new window.ModernLayoutPreferencesPersistence(
            document.body,
            {
                saveLayoutUrl: '/uPortal/api/layout',
            }
        );

        persistence.update(options, (data) => {
            if (data.error) {
                console.error('Error adding portlet:', data.error);
            } else {
                // Reload page to show new portlet
                window.location.reload();
            }
        });
    }
}

class PortletRegistry {
    constructor(url) {
        this.url = url;
        this.categories = [];
        this.portlets = [];
    }

    async load() {
        return new Promise((resolve) => {
            up.jQuery.ajax({
                url: '/uPortal/api/portletList',
                success: (data) => {
                    this.processRegistryData(data);
                    resolve();
                },
                error: (xhr, status, error) => {
                    console.error('Failed to load portlet registry:', error);
                    this.categories = [];
                    this.portlets = [];
                    resolve();
                },
                dataType: 'json',
            });
        });
    }

    processRegistryData(data) {
        this.categories = [];
        this.portlets = [];

        if (data.registry && data.registry.categories) {
            for (const category of data.registry.categories) {
                this.processCategory(category);
            }
        }

        if (
            data.registry &&
            (data.registry.channels || data.registry.portlets)
        ) {
            const existingIds = new Set(this.portlets.map((p) => p.id));
            for (const channel of data.registry.channels ||
                data.registry.portlets) {
                if (!existingIds.has(channel.id)) {
                    this.portlets.push(this.createPortlet(channel));
                }
            }
        }
    }

    processCategory(categoryData) {
        const category = {
            id: categoryData.id,
            name: categoryData.name,
            description: categoryData.description,
            deepPortlets: [],
        };

        const channels = categoryData.channels || categoryData.portlets || [];
        const existingIds = new Set(this.portlets.map((p) => p.id));
        for (const channel of channels) {
            const portlet = this.createPortlet(channel);
            portlet.categoryId = categoryData.id;
            if (!existingIds.has(channel.id)) {
                this.portlets.push(portlet);
                existingIds.add(channel.id);
            }
            category.deepPortlets.push(portlet);
        }

        const subcats =
            categoryData.categories || categoryData.subcategories || [];
        for (const subCat of subcats) this.processCategory(subCat);

        this.categories.push(category);
    }

    createPortlet(channel) {
        return {
            id: channel.id,
            title: channel.title,
            name: channel.name,
            fname: channel.fname,
            description: channel.description,
            iconUrl:
                channel.iconUrl ||
                channel.parameters?.iconUrl?.value ||
                '/ResourceServingWebapp/rs/tango/0.8.90/32x32/categories/applications-other.png',
        };
    }

    getAllCategories() {
        return this.categories;
    }

    getAllPortlets() {
        return this.portlets;
    }

    getMemberPortlets(categoryId) {
        if (!categoryId) return this.portlets; // Return all for empty category (ALL)

        const category = this.categories.find((cat) => cat.id === categoryId);
        return category ? category.deepPortlets : [];
    }
}

class CategoryListView {
    constructor(container, browser) {
        this.container = container.querySelector('.categories');
        this.browser = browser;
        if (this.container) {
            this.refresh();
        }
    }

    refresh() {
        if (!this.container || !this.browser.registry) return;

        // Build categories list
        const categories = [
            {id: '', name: 'ALL', description: 'All Categories'},
            ...this.browser.registry
                .getAllCategories()
                .filter(
                    (cat) =>
                        cat.id !== 'local.1' &&
                        cat.name !== 'uPortal' &&
                        cat.deepPortlets &&
                        cat.deepPortlets.length > 0
                )
                .sort((a, b) => a.name.localeCompare(b.name)),
        ];

        // Remove existing category elements (but keep h4)
        const existingUls = this.container.querySelectorAll('ul');
        for (const ul of existingUls) {
            if (!ul.querySelector('.category-choice-container')) {
                ul.remove();
            }
        }

        // Find h4 element
        const h4 = this.container.querySelector('h4');
        if (!h4) return;

        // Create separate ul for each category in reverse order to maintain correct sequence
        for (let index = categories.length - 1; index >= 0; index--) {
            const category = categories[index];
            const isActive = category.id === this.browser.state.currentCategory;
            const ul = this.createCategoryElement(
                category,
                isActive,
                index === 0
            );
            h4.after(ul);
        }
    }

    createCategoryElement(category, isActive, isFirst = false) {
        const ul = document.createElement('ul');
        const li = document.createElement('li');
        li.className = `category-choice ${isActive ? 'active' : ''} ${isFirst ? 'first' : ''}`;

        li.innerHTML = `<a href="#" class="category-choice-link"><span class="category-choice-name">${category.name}</span></a>`;

        const linkElement = li.querySelector('.category-choice-link');
        if (linkElement) {
            linkElement.addEventListener('click', (event_) => {
                event_.preventDefault();
                this.browser.onCategorySelect(category);
            });
        }

        ul.append(li);
        return ul;
    }
}

class PortletListView {
    constructor(container, browser) {
        this.container = container;
        this.browser = browser;
        this.currentPage = 0;
        this.itemsPerRow = 3; // Default 3 columns
        this.rowsPerPage = 2; // Default 2 rows
        this.refresh();
        this.setupResizeObserver();
    }

    setupResizeObserver() {
        // Recalculate layout when container size changes
        if (window.ResizeObserver) {
            const observer = new ResizeObserver(() => {
                this.calculateLayout();
                this.refresh();
            });

            const listContainer = this.container.querySelector(
                '.portlet-list, .results-list'
            );
            if (listContainer) {
                observer.observe(listContainer);
            }
        }
    }

    calculateLayout() {
        const listContainer = this.container.querySelector(
            '.portlet-list, .results-list'
        );
        if (!listContainer) return;

        const containerWidth = listContainer.clientWidth;
        const containerHeight = listContainer.clientHeight;

        // Estimate portlet dimensions (similar to release version)
        const portletWidth = 648; // From release CSS analysis
        const portletHeight = 82; // From release CSS analysis
        const margin = 5; // From release margins

        // Calculate how many fit horizontally
        this.itemsPerRow = Math.max(
            1,
            Math.floor((containerWidth + margin) / (portletWidth + margin))
        );

        // Calculate how many rows fit vertically
        this.rowsPerPage = Math.max(
            1,
            Math.floor((containerHeight + margin) / (portletHeight + margin))
        );
    }

    get pageSize() {
        return 6; // Fixed: 2 rows × 3 columns like release
    }

    refresh() {
        if (!this.container || !this.browser.registry) return;

        // Get filtered portlets
        const portlets = this.getFilteredPortlets();

        // Simple pagination
        const startIndex = this.currentPage * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        const pagePortlets = portlets.slice(startIndex, endIndex);

        // Render portlets
        this.renderPortlets(pagePortlets);
        this.renderPagination(portlets.length);
    }

    getFilteredPortlets() {
        let portlets = this.browser.state.currentCategory
            ? this.browser.registry.getMemberPortlets(
                  this.browser.state.currentCategory,
                  true
              )
            : this.browser.registry.getAllPortlets();

        // Apply search filter
        if (this.browser.state.portletRegex) {
            portlets = portlets.filter(
                (portlet) =>
                    this.browser.state.portletRegex.test(portlet.title) ||
                    this.browser.state.portletRegex.test(portlet.name) ||
                    this.browser.state.portletRegex.test(portlet.fname) ||
                    this.browser.state.portletRegex.test(portlet.description)
            );
        }

        return portlets.sort((a, b) => a.title.localeCompare(b.title));
    }

    renderPortlets(portlets) {
        const listContainer = this.container.querySelector(
            '#addContentPortletList, #useContentPortletList, .portlet-list'
        );
        if (!listContainer) return;

        // Calculate layout first
        this.calculateLayout();

        listContainer.innerHTML = '';

        for (const portlet of portlets) {
            const portletElement = this.createPortletElement(portlet);
            listContainer.append(portletElement);
        }

        // Notify drag manager if it exists
        if (this.browser.dragManager) {
            // Small delay to ensure DOM is ready
            setTimeout(() => {
                this.browser.dragManager.initializeDraggables();
            }, 0);
        }
    }

    createPortletElement(portlet) {
        const portletElement = document.createElement('li');
        portletElement.className = 'result-item portlet';
        portletElement.title = `${portlet.title} (${portlet.name})`;
        portletElement.dataset.portletId = portlet.id;

        portletElement.innerHTML = `
            <div class="ri-wrapper portlet-wrapper">
                <a class="ri-utility portlet-thumb-gripper" href="#" title="Drag to add content"><span>Drag Handle</span></a>
                <a href="#" class="ri-link portlet-thumb-link">
                    <span>${this.browser.options.buttonText}</span>
                </a>
                <div class="ri-content portlet-thumb-content ui-helper-clearfix">
                    <div class="ri-titlebar portlet-thumb-titlebar">${portlet.title}</div>
                    <div class="ri-icon portlet-thumb-icon" style="background: url(${portlet.iconUrl || '/ResourceServingWebapp/rs/tango/0.8.90/32x32/categories/applications-other.png'}) top left no-repeat;"><span>Thumbnail</span></div>
                    <div class="ri-description portlet-thumb-description">${portlet.description || ''}</div>
                </div>
            </div>
        `;

        const linkElement = portletElement.querySelector('.portlet-thumb-link');
        if (linkElement) {
            linkElement.addEventListener('click', (event_) => {
                event_.preventDefault();
                this.browser.onPortletSelect(portlet);
            });
        }

        return portletElement;
    }

    renderPagination(totalItems) {
        const totalPages = Math.ceil(totalItems / this.pageSize);
        if (totalPages <= 1) {
            // Hide pager if only one page
            const pagerElement = this.container.querySelector('.pager');
            if (pagerElement) pagerElement.style.display = 'none';
            return;
        }

        const pagerElement = this.container.querySelector('.pager');
        if (pagerElement) {
            pagerElement.removeAttribute('style'); // Remove inline styles to allow CSS
            pagerElement.innerHTML = `
                <div class="pager-button-up flc-pager-previous ${this.currentPage === 0 ? 'fl-pager-disabled' : ''}">
                    <a class="pager-button-up-inner" href="#">
                        <span></span>
                    </a>
                </div>
                <div class="pager-button-down flc-pager-next ${this.currentPage === totalPages - 1 ? 'fl-pager-disabled' : ''}">
                    <a class="pager-button-down-inner" href="#">
                        <span></span>
                    </a>
                </div>
            `;

            const previousButton = pagerElement.querySelector(
                '.pager-button-up-inner'
            );
            const nextButton = pagerElement.querySelector(
                '.pager-button-down-inner'
            );

            previousButton?.addEventListener('click', (event_) => {
                event_.preventDefault();
                if (this.currentPage > 0) {
                    this.currentPage--;
                    this.refresh();
                }
            });

            nextButton?.addEventListener('click', (event_) => {
                event_.preventDefault();
                if (this.currentPage < totalPages - 1) {
                    this.currentPage++;
                    this.refresh();
                }
            });
        }
    }
}

// Global initialization function to replace Fluid component
window.up = window.up || {};
window.up.PortletBrowser = function (container, gallery, options) {
    return new PortletBrowser(container, gallery, options);
};
window.PortletBrowser = PortletBrowser;
