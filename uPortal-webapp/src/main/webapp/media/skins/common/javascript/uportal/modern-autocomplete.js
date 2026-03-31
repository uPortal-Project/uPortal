/*
 * Modern replacement for Fluid-based AutoComplete component
 * Replaces up-autocomplete.js with vanilla JavaScript implementation
 */
'use strict';

class ModernAutoComplete {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.options = {
            initialText: '',
            searchFunction: null,
            minLength: 1,
            delay: 300,
            ...options
        };
        
        this.state = {
            currentValue: null,
            isOpen: false,
            searchTimeout: null
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
            matchTemplate: this.container.querySelector('.up-autocomplete-match'),
            loadingMessage: this.container.querySelector('.up-autocomplete-loading'),
            noResultsMessage: this.container.querySelector('.up-autocomplete-noresults')
        };
    }
    
    bindEvents() {
        if (this.elements.input) {
            this.elements.input.addEventListener('input', this.handleInput.bind(this));
            this.elements.input.addEventListener('focus', this.handleFocus.bind(this));
            this.elements.input.addEventListener('blur', this.handleBlur.bind(this));
            this.elements.input.addEventListener('keydown', this.handleKeydown.bind(this));
        }
        
        if (this.elements.close) {
            this.elements.close.addEventListener('click', (e) => {
                e.preventDefault();
                this.hideDropdown();
            });
        }
        
        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!this.container.contains(e.target)) {
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
    
    handleInput(e) {
        const query = e.target.value.trim();
        
        // Clear previous timeout
        if (this.state.searchTimeout) {
            clearTimeout(this.state.searchTimeout);
        }
        
        // Remove valid input class
        this.elements.input.classList.remove('up-autocomplete-validinput');
        this.state.currentValue = null;
        
        if (query.length >= this.options.minLength && query !== this.options.initialText) {
            // Debounce search
            this.state.searchTimeout = setTimeout(() => {
                this.performSearch(query);
            }, this.options.delay);
        } else {
            this.hideDropdown();
        }
    }
    
    handleFocus(e) {
        const value = e.target.value;
        if (value === this.options.initialText) {
            e.target.value = '';
        }
    }
    
    handleBlur(e) {
        // Delay to allow click events on dropdown items
        setTimeout(() => {
            const value = e.target.value.trim();
            if (value === '') {
                e.target.value = this.options.initialText;
            }
        }, 150);
    }
    
    handleKeydown(e) {
        if (!this.state.isOpen) return;
        
        const items = this.elements.matches.querySelectorAll('.up-autocomplete-match:not([style*="display: none"])');
        let currentIndex = -1;
        
        // Find currently selected item
        items.forEach((item, index) => {
            if (item.classList.contains('selected')) {
                currentIndex = index;
            }
        });
        
        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                this.selectItem(items, currentIndex + 1);
                break;
            case 'ArrowUp':
                e.preventDefault();
                this.selectItem(items, currentIndex - 1);
                break;
            case 'Enter':
                e.preventDefault();
                if (currentIndex >= 0 && items[currentIndex]) {
                    const link = items[currentIndex].querySelector('.up-autocomplete-match-link');
                    if (link) link.click();
                }
                break;
            case 'Escape':
                e.preventDefault();
                this.hideDropdown();
                break;
        }
    }
    
    selectItem(items, index) {
        // Remove previous selection
        items.forEach(item => item.classList.remove('selected'));
        
        // Add selection to new item
        if (index >= 0 && index < items.length) {
            items[index].classList.add('selected');
            items[index].scrollIntoView({ block: 'nearest' });
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
        
        results.forEach(result => {
            const matchEl = this.createMatchElement(result);
            this.elements.matches.appendChild(matchEl);
        });
        
        this.showDropdown();
        this.hideLoading();
        this.hideNoResults();
    }
    
    createMatchElement(result) {
        const matchEl = this.elements.matchTemplate.cloneNode(true);
        matchEl.style.display = '';
        
        const textEl = matchEl.querySelector('.up-autocomplete-match-text');
        const linkEl = matchEl.querySelector('.up-autocomplete-match-link');
        
        if (textEl) {
            textEl.textContent = result.text;
        }
        
        if (linkEl) {
            linkEl.title = result.text;
            linkEl.addEventListener('click', (e) => {
                e.preventDefault();
                this.selectResult(result);
            });
        }
        
        return matchEl;
    }
    
    selectResult(result) {
        this.elements.input.value = result.text;
        this.state.currentValue = result.value;
        this.elements.input.classList.add('up-autocomplete-validinput');
        this.hideDropdown();
        
        // Trigger change event
        this.elements.input.dispatchEvent(new Event('change', { bubbles: true }));
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
window.up.Autocomplete = function(container, options) {
    return new ModernAutoComplete(container, options);
};