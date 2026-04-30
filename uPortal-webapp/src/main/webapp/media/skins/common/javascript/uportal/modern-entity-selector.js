/*
 * Modern replacement for Fluid EntitySelector - follows original logic exactly
 */
'use strict';

const createTestMaps = (groupNames, testAttribute) => {
    return groupNames.map((name) => ({
        testValue: name,
        attributeName: testAttribute,
        testerClassName:
            'org.apereo.portal.groups.pags.testers.AdHocGroupTester',
    }));
};

class ModernEntitySelector {
    constructor(container, options = {}) {
        this.container = container;
        this.options = {
            entityTypes: [],
            selected: [],
            initialFocusedEntity: 'GROUP:local.0',
            selectMultiple: true,
            entitiesUrl: '/uPortal/api/entities',
            selectors: {
                selectionBasket: '#selectionBasket',
                breadcrumbs: '#entityBreadcrumbs',
                currentEntityName: '#currentEntityName',
                entityBrowserContent: '#entityBrowserContent',
                searchForm: '#searchForm',
                searchDropDown: '#searchDropDown',
                searchResults: '#searchResults',
                searchResultsNoMembers: '#searchResultsNoMembers',
                currentSelectBtn: '#currentSelectBtn',
                buttonPrimary: '#buttonPrimary',
            },
            ...options,
        };

        this.currentEntity = null;
        this.entities = new Map();

        container._modernEntitySelector = this;
        this.init();
    }

    async init() {
        this.setupSearch();
        this.setupCurrentEntityButton();
        this.fixModalDisplay();
        await this.browseEntity(this.options.initialFocusedEntity);
    }

    fixModalDisplay() {
        // Fix Bootstrap modal display issues
        const modal = document.querySelector('#adhocGroupModal');
        if (modal) {
            modal.addEventListener('shown.bs.modal', () => {
                // Ensure modal is visible when shown
                modal.style.opacity = '1';
                modal.style.visibility = 'visible';
                const dialog = modal.querySelector('.modal-dialog');
                if (dialog) {
                    dialog.style.transform = 'translate(0, 0)';
                }

                // Initialize jsTree widgets if not already done
                this.initializeJSTrees();
            });
        }
    }

    initializeJSTrees() {
        const $ = up.jQuery;
        const includesTree = $('[id$="dataIncludes"]');
        const excludesTree = $('[id$="dataExcludes"]');

        // Only initialize if not already done
        if (includesTree.length > 0 && !includesTree.jstree(true)) {
            includesTree.jstree({
                core: {
                    data: (object, callback) => {
                        this.getJSTreeData(object, callback);
                    },
                },
                checkbox: {
                    keep_selected_style: false,
                    three_state: false,
                },
                plugins: ['checkbox'],
            });
        }

        if (excludesTree.length > 0 && !excludesTree.jstree(true)) {
            excludesTree.jstree({
                core: {
                    data: (object, callback) => {
                        this.getJSTreeData(object, callback);
                    },
                },
                checkbox: {
                    keep_selected_style: false,
                    three_state: false,
                },
                plugins: ['checkbox'],
            });
        }

        // Setup form submission
        this.setupAdHocForm();
    }

    getJSTreeData(object, callback) {
        // Provide jsTree data (like original Fluid code)
        if (object.id === '#') {
            // Root node - return initial entity
            const entity =
                this.currentEntity ||
                this.entities.get(this.options.initialFocusedEntity);
            if (entity) {
                callback([
                    {
                        id: `${entity.entityType}:${entity.id}`,
                        text: entity.name,
                        children: entity.children
                            ? entity.children
                                  .filter((c) => c.entityType === 'GROUP')
                                  .map((c) => ({
                                      id: `${c.entityType}:${c.id}`,
                                      text: c.name,
                                      state: {loaded: false},
                                  }))
                            : [],
                    },
                ]);
            } else {
                callback([]);
            }
        } else {
            // Child node - get entity children
            const entity = this.entities.get(object.id);
            if (entity && entity.children) {
                const children = entity.children
                    .filter((c) => c.entityType === 'GROUP')
                    .map((c) => ({
                        id: `${c.entityType}:${c.id}`,
                        text: c.name,
                        children: c.children
                            ? c.children.some((gc) => gc.entityType === 'GROUP')
                            : false,
                    }));
                callback(children);
            } else {
                callback([]);
            }
        }
    }

    setupCurrentEntityButton() {
        const currentSelectButton =
            this.container.querySelector('#currentSelectBtn') ||
            document.querySelector(this.options.selectors.currentSelectBtn);
        if (currentSelectButton) {
            currentSelectButton.addEventListener('click', (event_) => {
                event_.preventDefault();
                const currentEntityName =
                    this.container.querySelector('#currentEntityName') ||
                    document.querySelector(
                        this.options.selectors.currentEntityName
                    );
                const key = currentEntityName?.getAttribute('key');
                if (key) {
                    this.selectEntity(key);
                }
            });
        }
    }

    setupSearch() {
        const searchForm = this.container.querySelector('form');
        const searchDropDown = this.container.querySelector('.search-dropdown');

        if (searchForm) {
            searchForm.addEventListener('submit', (event_) => {
                event_.preventDefault();
                const searchInput = searchForm.querySelector(
                    'input[type="search"]'
                );
                if (searchInput) {
                    this.performSearch(searchInput.value.trim());
                }
            });
        }

        // Close search dropdown when clicking close button (like Fluid)
        const closeSearch = this.container.querySelector('.search-close a');
        if (closeSearch) {
            closeSearch.addEventListener('click', (event_) => {
                event_.preventDefault();
                searchDropDown.style.display = 'none';
            });
        }

        // Close dropdown when clicking outside (like Fluid)
        document.addEventListener('click', (event_) => {
            if (!event_.target.closest('.portlet-search')) {
                searchDropDown.style.display = 'none';
            }
        });
    }

    async performSearch(query) {
        const searchDropDown = this.container.querySelector('.search-dropdown');
        const resultsList = this.container.querySelector('.search-list');
        const noResults = this.container.querySelector('.portlet-msg');

        if (!query) {
            searchDropDown.style.display = 'none';
            return;
        }

        try {
            // Use same search logic as ModernAutoComplete
            const parameters = new URLSearchParams();
            parameters.append('q', query);
            for (const type of this.options.entityTypes) {
                parameters.append('entityType[]', type.toUpperCase());
            }

            const response = await fetch(
                `${this.options.entitiesUrl}.json?${parameters.toString()}`
            );
            if (!response.ok) throw new Error(`HTTP ${response.status}`);

            const data = await response.json();
            const results = data.jsonEntityBeanList || [];

            // Clear and populate results (exactly like Fluid)
            resultsList.innerHTML = '';

            if (results.length === 0) {
                noResults.style.display = 'block';
                resultsList.style.display = 'none';
            } else {
                noResults.style.display = 'none';
                resultsList.style.display = 'block';

                for (const entity of results) {
                    const entityType = entity.entityType.toLowerCase();
                    const key = `${entity.entityType}:${entity.id}`;
                    this.entities.set(key, entity);

                    const li = document.createElement('li');
                    li.className = entityType;
                    li.innerHTML = `<a href="#" title="${entity.name}"><span key="${key}">${entity.name}</span></a>`;

                    const link = li.querySelector('a');
                    link.addEventListener('click', (event_) => {
                        event_.preventDefault();
                        this.selectEntity(key);
                        this.updateSearchSelectionStates();
                    });

                    resultsList.append(li);
                }

                this.updateSearchSelectionStates();
            }

            searchDropDown.style.display = 'block';
        } catch (error) {
            console.error('Search error:', error);
            noResults.style.display = 'block';
            resultsList.style.display = 'none';
            searchDropDown.style.display = 'block';
        }

        // Hide any loading spinners
        const loader = this.container.querySelector('.search-loader');
        if (loader) {
            loader.style.display = 'none';
        }
    }

    updateSearchSelectionStates() {
        const resultsList = this.container.querySelector('.search-list');
        if (!resultsList) return;

        for (const li of resultsList.querySelectorAll('li')) {
            const span = li.querySelector('span');
            const key = span?.getAttribute('key');
            if (key && this.options.selected.includes(key)) {
                li.classList.add('selected');
            } else {
                li.classList.remove('selected');
            }
        }
    }

    async browseEntity(key) {
        const entity = await this.loadEntity(key);
        if (!entity) return;

        this.currentEntity = entity;

        // Update current entity name (exactly like Fluid)
        const currentEntityName = document.querySelector(
            this.options.selectors.currentEntityName
        );
        if (currentEntityName) {
            currentEntityName.textContent = entity.name;
            currentEntityName.setAttribute('key', key);
        }

        // Clear all tables (exactly like Fluid)
        const content = document.querySelector(
            this.options.selectors.entityBrowserContent
        );
        for (const table of content.querySelectorAll('table')) {
            table.innerHTML = '';
            table.style.display = 'none';
        }

        // For each child entity, create table row (exactly like Fluid)
        // Show ALL entity types for navigation, but disable selection for disallowed types
        if (entity.children)
            for (const child of entity.children) {
                const objectType = child.entityType.toLowerCase();
                const isAllowedType =
                    this.options.entityTypes.includes(objectType);
                const childKey = `${child.entityType}:${child.id}`;

                // Create row elements (exactly like Fluid)
                const tr = document.createElement('tr');

                // Create entity name cell
                const tdChild = document.createElement('td');
                let a;
                if (objectType === 'person' || objectType === 'portlet') {
                    a = document.createElement('span');
                    a.textContent = child.name;
                } else {
                    a = document.createElement('a');
                    a.href = 'javascript:;';
                    a.textContent = child.name;
                    a.setAttribute('key', childKey);
                    a.addEventListener('click', () =>
                        this.browseEntity(childKey)
                    );
                }
                a.className = 'member-link';
                tdChild.append(a);

                // Create button cell
                const tdButtons = document.createElement('td');
                const divButtons = document.createElement('div');
                divButtons.className = 'btn-group pull-right';
                divButtons.setAttribute('role', 'group');

                const selButton = document.createElement('button');
                selButton.setAttribute('key', childKey);

                if (isAllowedType) {
                    selButton.className = 'btn btn-select btn-success btn-sm';
                    selButton.innerHTML =
                        'Add to Selection <i class="fa fa-plus-circle"></i>';
                    selButton.addEventListener('click', (event_) => {
                        event_.preventDefault();
                        this.selectEntity(childKey);
                    });
                } else {
                    selButton.className = 'btn btn-secondary btn-sm';
                    selButton.innerHTML =
                        'Cannot Select <i class="fa fa-ban"></i>';
                    selButton.disabled = true;
                }

                divButtons.append(selButton);
                tdButtons.append(divButtons);

                tr.append(tdChild);
                tr.append(tdButtons);

                // Find table by entity type and append row (exactly like Fluid)
                const table = content.querySelector(
                    `.${objectType} .member-list`
                );
                if (table) {
                    table.append(tr);
                    table.style.display = 'table';
                }
            }

        // Update no-members messages (exactly like Fluid)
        for (const noMembers of content.querySelectorAll('.no-members')) {
            const parent = noMembers.parentElement;
            const table = parent.querySelector('table');
            const hasRows = table && table.querySelectorAll('tr').length > 0;
            noMembers.style.display = hasRows ? 'none' : 'block';
        }

        // Update button states after rendering
        this.updateButtonStates();
        this.updateCurrentEntityButton();
        this.updateSearchSelectionStates();

        // Cache current entity for jsTree
        this.currentEntity = entity;
    }

    updateCurrentEntityButton() {
        const currentSelectButton =
            this.container.querySelector('#currentSelectBtn') ||
            document.querySelector(this.options.selectors.currentSelectBtn);
        const currentEntityName =
            this.container.querySelector('#currentEntityName') ||
            document.querySelector(this.options.selectors.currentEntityName);

        if (currentSelectButton && currentEntityName) {
            const key = currentEntityName.getAttribute('key');
            if (key) {
                const [entityType] = key.split(':');
                const isAllowedType = this.options.entityTypes.includes(
                    entityType.toLowerCase()
                );

                if (!isAllowedType) {
                    // Hide button for disallowed entity types
                    currentSelectButton.style.display = 'none';
                    return;
                }

                currentSelectButton.style.display = 'inline-block';
                const isSelected = this.options.selected.includes(key);

                if (isSelected) {
                    currentSelectButton.innerHTML =
                        'Remove from Selection <i class="fa fa-minus-circle"></i>';
                    currentSelectButton.className = 'btn btn-danger btn-sm';
                } else {
                    currentSelectButton.innerHTML =
                        'Add to Selection <i class="fa fa-plus-circle"></i>';
                    currentSelectButton.className = 'btn btn-success btn-sm';
                }
            }
        }
    }

    async loadEntity(key) {
        if (this.entities.has(key)) return this.entities.get(key);

        const [type, id] = key.split(':');

        try {
            const url = `${this.options.entitiesUrl}/${type}/${id}.json`;
            const response = await fetch(url);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);

            const data = await response.json();
            const entity = data.jsonEntityBean;
            if (entity) this.entities.set(key, entity);
            return entity;
        } catch (error) {
            console.error('Load entity error:', error);
            return null;
        }
    }

    async selectEntity(key) {
        if (!this.options.selected.includes(key)) {
            this.options.selected.push(key);
            const entity = await this.loadEntity(key);
            if (entity && entity.id && entity.name && entity.entityType) {
                this.addToSelectionBasket(entity, key);
            } else {
                console.error('Entity invalid or failed to load:', {
                    key,
                    entity,
                });
                this.options.selected.pop();
                window.up.notify(
                    'Failed to load entity data. Please try again.',
                    'TopCenter',
                    'error'
                );
            }
        }
        this.updateButtonStates();
    }

    addToSelectionBasket(entity, key) {
        if (!entity || !entity.id || !entity.name) {
            console.error('Entity missing required fields:', {entity});
            return;
        }

        const basket = document.querySelector(
            this.options.selectors.selectionBasket
        );
        if (!basket) {
            console.error('Selection basket not found!');
            return;
        }

        const ul = basket.querySelector('ul');
        if (!ul) {
            console.error('UL element not found in basket!');
            return;
        }

        const li = document.createElement('li');
        li.innerHTML = `
            <a href="#" key="${key}">${entity.name}</a>
            <input type="hidden" name="groups" value="${key}"/>
        `;

        li.querySelector('a').addEventListener('click', (event_) => {
            event_.preventDefault();
            this.deselectEntity(key);
        });

        ul.append(li);
    }

    deselectEntity(key) {
        // Remove from selected array
        const index = this.options.selected.indexOf(key);
        if (index > -1) {
            this.options.selected.splice(index, 1);
        }

        // Remove from UI
        const basket = document.querySelector(
            this.options.selectors.selectionBasket
        );
        const link = basket.querySelector(`a[key="${key}"]`);
        if (link) {
            link.parentElement.remove();
        }

        this.updateButtonStates();
    }

    updateButtonStates() {
        // Update all buttons to show correct add/remove state
        const content = document.querySelector(
            this.options.selectors.entityBrowserContent
        );
        for (const button of content.querySelectorAll('button.btn-select')) {
            const key = button.getAttribute('key');
            const isSelected = this.options.selected.includes(key);

            // Remove existing event listeners by cloning
            const newButton = button.cloneNode(true);
            button.parentNode.replaceChild(newButton, button);

            if (isSelected) {
                newButton.innerHTML =
                    'Remove from Selection <i class="fa fa-minus-circle"></i>';
                newButton.className = 'btn btn-select btn-danger btn-sm';
                newButton.addEventListener('click', (event_) => {
                    event_.preventDefault();
                    this.deselectEntity(key);
                });
            } else {
                newButton.innerHTML =
                    'Add to Selection <i class="fa fa-plus-circle"></i>';
                newButton.className = 'btn btn-select btn-success btn-sm';
                newButton.addEventListener('click', (event_) => {
                    event_.preventDefault();
                    this.selectEntity(key);
                });
            }
        }
    }

    setupAdHocForm() {
        const modal = document.querySelector('#adhocGroupModal');
        const saveButton = document.querySelector('[id$="saveAdHocButton"]');

        // Clear form when modal opens (using Bootstrap 5 event)
        modal.addEventListener('shown.bs.modal', () => {
            document.querySelector('#groupName').value = '';
            document.querySelector('#groupDesc').value = '';
            saveButton.disabled = true;
            document
                .querySelector('#groupName')
                .parentElement.classList.remove('is-invalid');
        });

        // Handle save button click (like original Fluid component)
        saveButton.addEventListener('click', (event_) => {
            event_.preventDefault();
            this.submitAdHocGroup();
        });
    }

    submitAdHocGroup() {
        const $ = up.jQuery;
        const parentKey =
            this.container
                .querySelector('#currentEntityName')
                ?.getAttribute('key') ||
            document
                .querySelector(this.options.selectors.currentEntityName)
                ?.getAttribute('key');
        const parentName =
            this.container.querySelector('#currentEntityName')?.textContent ||
            document.querySelector(this.options.selectors.currentEntityName)
                ?.textContent;
        const groupName = $('#groupName').val();
        const groupDesc = $('#groupDesc').val();

        if (!groupName || !parentName) {
            console.error('Missing required fields');
            return;
        }

        // Get includes and excludes from jsTree selection lists
        const includes = [];
        $('[id$="dataIncludesList"] li').each(function () {
            includes.push($(this).text());
        });

        const excludes = [];
        $('[id$="dataExcludesList"] li').each(function () {
            excludes.push($(this).text());
        });

        // Create test maps like original
        const tests = createTestMaps(includes, 'group-member').concat(
            createTestMaps(excludes, 'not-group-member')
        );

        const pagsGroup = {
            name: groupName,
            description: groupDesc,
            testGroups: [{tests: tests}],
        };

        const json = JSON.stringify(pagsGroup);

        // Use XMLHttpRequest like original
        const xmlhttp = new XMLHttpRequest();
        xmlhttp.addEventListener('readystatechange', () => {
            if (xmlhttp.readyState === 4) {
                if (xmlhttp.status >= 200 && xmlhttp.status <= 202) {
                    // Success - refresh entity browser
                    this.entities.delete(parentKey); // Clear cache
                    this.browseEntity(parentKey);
                }
                this.displayResponseMessage(xmlhttp);
            }
        });

        // Submit to PAGS API endpoint like original
        xmlhttp.open('POST', `/uPortal/api/v4-3/pags/${parentName}.json`, true);
        xmlhttp.send(json);
    }

    displayResponseMessage(xmlhttp) {
        const $ = up.jQuery;

        // Hide all alerts first
        $(
            '[id$="alertSuccess"], [id$="alertInvalidParent"], [id$="alertGroupExists"], [id$="alertUnauthorized"], [id$="alertUnknown"]'
        ).hide();

        switch (xmlhttp.status) {
            case 200: // SC_OK
            case 201: // SC_CREATED
            case 202: {
                // SC_ACCEPTED
                $('[id$="alertSuccess"]').show();
                // Close modal on success
                const modal = document.querySelector('#adhocGroupModal');
                const bsModal = bootstrap.Modal.getInstance(modal);
                if (bsModal) bsModal.hide();
                break;
            }
            case 400: {
                // SC_BAD_REQUEST -> bad parent
                $('[id$="alertInvalidParent"]').show();
                break;
            }
            case 409: {
                // SC_CONFLICT -> group exists
                $('[id$="alertGroupExists"]').show();
                break;
            }
            case 401: // SC_UNAUTHORIZED
            case 403: {
                // SC_FORBIDDEN
                $('[id$="alertUnauthorized"]').show();
                break;
            }
            default: {
                $('[id$="alertUnknown"]').show();
                break;
            }
        }
    }
}

// Replace the global function
window.up = window.up || {};
window.up.entityselection = function (container, options) {
    return new ModernEntitySelector(container, options);
};
