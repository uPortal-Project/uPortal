/*
 * Modern replacement for Fluid-based AutoComplete component
 * Replaces up-autocomplete.js with vanilla JavaScript implementation
 */
'use strict';

class ModernAutoComplete {
    constructor(container, options = {}) {
        this.container =
            typeof container === 'string'
                ? document.querySelector(container)
                : container;
        this.options = {
            initialText: '',
            searchFunction: null,
            minLength: 1,
            delay: 300,
            ...options,
        };

        this.state = {
            currentValue: null,
            isOpen: false,
            searchTimeout: null,
        };

        this.init();
    }

    init() {
        this.cacheElements();
        this.bindEvents();
        this.setupInitialState();
    }

    cacheElements() {
        this.elements = {
            input: this.container.querySelector('.up-autocomplete-searchterm'),
            dropdown: this.container.querySelector('.up-autocomplete-dropdown'),
            close: this.container.querySelector('.up-autocomplete-close'),
            matches: this.container.querySelector('.up-autocomplete-matches'),
            matchTemplate: this.container.querySelector(
                '.up-autocomplete-match'
            ),
            loadingMessage: this.container.querySelector(
                '.up-autocomplete-loading'
            ),
            noResultsMessage: this.container.querySelector(
                '.up-autocomplete-noresults'
            ),
        };
    }

    bindEvents() {
        if (this.elements.input) {
            this.elements.input.addEventListener(
                'input',
                this.handleInput.bind(this)
            );
            this.elements.input.addEventListener(
                'focus',
                this.handleFocus.bind(this)
            );
            this.elements.input.addEventListener(
                'blur',
                this.handleBlur.bind(this)
            );
            this.elements.input.addEventListener(
                'keydown',
                this.handleKeydown.bind(this)
            );
        }

        if (this.elements.close) {
            this.elements.close.addEventListener('click', (event_) => {
                event_.preventDefault();
                this.hideDropdown();
            });
        }

        // Close dropdown when clicking outside
        document.addEventListener('click', (event_) => {
            if (!this.container.contains(event_.target)) {
                this.hideDropdown();
            }
        });
    }

    setupInitialState() {
        if (this.elements.input && this.options.initialText) {
            this.elements.input.value = this.options.initialText;
        }

        if (this.elements.dropdown) {
            this.elements.dropdown.style.display = 'none';
        }
    }

    handleInput(event_) {
        const query = event_.target.value.trim();

        // Clear previous timeout
        if (this.state.searchTimeout) {
            clearTimeout(this.state.searchTimeout);
        }

        // Remove valid input class
        this.elements.input.classList.remove('up-autocomplete-validinput');
        this.state.currentValue = null;

        if (
            query.length >= this.options.minLength &&
            query !== this.options.initialText
        ) {
            // Debounce search
            this.state.searchTimeout = setTimeout(() => {
                this.performSearch(query);
            }, this.options.delay);
        } else {
            this.hideDropdown();
        }
    }

    handleFocus(event_) {
        const value = event_.target.value;
        if (value === this.options.initialText) {
            event_.target.value = '';
        }
    }

    handleBlur(event_) {
        // Delay to allow click events on dropdown items
        setTimeout(() => {
            const value = event_.target.value.trim();
            if (value === '') {
                event_.target.value = this.options.initialText;
            }
        }, 150);
    }

    handleKeydown(event_) {
        if (!this.state.isOpen) return;

        const items = this.elements.matches.querySelectorAll(
            '.up-autocomplete-match:not([style*="display: none"])'
        );
        let currentIndex = -1;

        // Find currently selected item
        for (const [index, item] of items.entries()) {
            if (item.classList.contains('selected')) {
                currentIndex = index;
            }
        }

        switch (event_.key) {
            case 'ArrowDown': {
                event_.preventDefault();
                this.selectItem(items, currentIndex + 1);
                break;
            }
            case 'ArrowUp': {
                event_.preventDefault();
                this.selectItem(items, currentIndex - 1);
                break;
            }
            case 'Enter': {
                event_.preventDefault();
                if (currentIndex >= 0 && items[currentIndex]) {
                    const link = items[currentIndex].querySelector(
                        '.up-autocomplete-match-link'
                    );
                    if (link) link.click();
                }
                break;
            }
            case 'Escape': {
                event_.preventDefault();
                this.hideDropdown();
                break;
            }
        }
    }

    selectItem(items, index) {
        // Remove previous selection
        for (const item of items) item.classList.remove('selected');

        // Add selection to new item
        if (index >= 0 && index < items.length) {
            items[index].classList.add('selected');
            items[index].scrollIntoView({block: 'nearest'});
        }
    }

    async performSearch(query) {
        if (!this.options.searchFunction) return;

        this.showLoading();

        try {
            const results = await this.options.searchFunction(query);
            this.renderResults(results);
        } catch (error) {
            console.error('AutoComplete search error:', error);
            this.showNoResults();
        }
    }

    renderResults(results) {
        // Always clear existing matches first
        this.elements.matches.innerHTML = '';

        if (!results || results.length === 0) {
            this.showNoResults();
            return;
        }

        for (const result of results) {
            const matchElement = this.createMatchElement(result);
            this.elements.matches.append(matchElement);
        }

        this.showDropdown();
        this.hideLoading();
        this.hideNoResults();
    }

    createMatchElement(result) {
        const matchElement = this.elements.matchTemplate.cloneNode(true);
        matchElement.style.display = '';

        const textElement = matchElement.querySelector(
            '.up-autocomplete-match-text'
        );
        const linkElement = matchElement.querySelector(
            '.up-autocomplete-match-link'
        );

        if (textElement) {
            textElement.textContent = result.text;
        }

        if (linkElement) {
            linkElement.title = result.text;
            linkElement.addEventListener('click', (event_) => {
                event_.preventDefault();
                this.selectResult(result);
            });
        }

        return matchElement;
    }

    selectResult(result) {
        this.elements.input.value = result.text;
        this.state.currentValue = result.value;
        this.elements.input.classList.add('up-autocomplete-validinput');
        this.hideDropdown();

        // Trigger change event
        this.elements.input.dispatchEvent(new Event('change', {bubbles: true}));
    }

    showDropdown() {
        if (this.elements.dropdown) {
            this.elements.dropdown.style.display = 'block';
            this.state.isOpen = true;
        }
    }

    hideDropdown() {
        if (this.elements.dropdown) {
            this.elements.dropdown.style.display = 'none';
            this.state.isOpen = false;
        }
    }

    showLoading() {
        if (this.elements.loadingMessage) {
            this.elements.loadingMessage.style.display = 'block';
        }
        this.showDropdown();
    }

    hideLoading() {
        if (this.elements.loadingMessage) {
            this.elements.loadingMessage.style.display = 'none';
        }
    }

    showNoResults() {
        if (this.elements.noResultsMessage) {
            this.elements.noResultsMessage.style.display = 'block';
        }
        this.showDropdown();
        this.hideLoading();
    }

    hideNoResults() {
        if (this.elements.noResultsMessage) {
            this.elements.noResultsMessage.style.display = 'none';
        }
    }

    getValue() {
        return this.state.currentValue;
    }

    setValue(value, text) {
        this.state.currentValue = value;
        if (this.elements.input) {
            this.elements.input.value = text || value;
            this.elements.input.classList.add('up-autocomplete-validinput');
        }
    }

    clear() {
        this.state.currentValue = null;
        if (this.elements.input) {
            this.elements.input.value = this.options.initialText;
            this.elements.input.classList.remove('up-autocomplete-validinput');
        }
        this.hideDropdown();
    }
}

// Global initialization function to replace Fluid component
window.up = window.up || {};
window.up.Autocomplete = function (container, options) {
    return new ModernAutoComplete(container, options);
};
