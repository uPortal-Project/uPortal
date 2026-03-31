/*
 * Modern replacement for Fluid ParameterEditor
 * Handles parameter configuration interface
 */
'use strict';

class ModernParameterEditor {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.options = {
            parameterNamePrefix: '',
            parameterBindName: '',
            auxiliaryBindName: '',
            useAuxiliaryCheckbox: false,
            dialog: null,
            multivalued: false,
            displayClasses: {
                deleteItemLink: 'delete-parameter-link',
                deleteItemLinkExtraClass: '',
                deleteValueLink: 'delete-parameter-value-link',
                deleteValueLinkExtraClass: '',
                addItemLink: 'add-parameter-link',
                addItemLinkExtraClass: '',
                addValueLink: 'add-parameter-value-link',
                addValueLinkExtraClass: '',
                inputElementExtraClass: ''
            },
            messages: {
                remove: 'Remove',
                removeParameter: 'Delete Preference',
                addValue: 'Add value'
            },
            selectors: {
                preferencesTable: 'tbody'
            },
            ...options
        };
        
        this.dialogInitialized = false;
        this.init();
    }
    
    init() {
        this.bindExistingElements();
        this.bindAddParameterAction();
    }
    
    bindExistingElements() {
        // Bind existing delete parameter links
        this.container.querySelectorAll(`.${this.options.displayClasses.deleteItemLink}`).forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.removeParameter(link);
            });
        });
        
        // Bind existing delete value links
        this.container.querySelectorAll(`.${this.options.displayClasses.deleteValueLink}`).forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.removeValue(link);
            });
        });
        
        // Bind existing add value links
        this.container.querySelectorAll(`.${this.options.displayClasses.addValueLink}`).forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.addValue(link);
            });
        });
    }
    
    bindAddParameterAction() {
        // Ensure add button exists
        this.ensureAddButton();
        
        // Bind add parameter link
        const addLinks = this.container.querySelectorAll(`.${this.options.displayClasses.addItemLink}`);
        addLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.showAddParameterDialog();
            });
        });
    }
    
    ensureAddButton() {
        // Check if add button already exists
        const existingAddLink = this.container.querySelector(`.${this.options.displayClasses.addItemLink}`);
        if (!existingAddLink && this.options.dialog) {
            // Create add button
            const addButtonContainer = document.createElement('p');
            const addButton = document.createElement('a');
            addButton.href = 'javascript:void(0)';
            addButton.className = `${this.options.displayClasses.addItemLink} btn btn-primary`;
            addButton.innerHTML = 'Add Preference&nbsp;&nbsp;<i class="fa fa-plus-circle"></i>';
            
            addButtonContainer.appendChild(addButton);
            this.container.appendChild(addButtonContainer);
        }
    }
    
    getParameterPath(name) {
        return `${this.options.parameterBindName}['${this.options.parameterNamePrefix}${name}'].value`;
    }
    
    getAuxiliaryPath(name) {
        return `${this.options.auxiliaryBindName}['${this.options.parameterNamePrefix}${name}'].value`;
    }
    
    addParameter(form) {
        const nameInput = form.querySelector('input[name=name]');
        if (!nameInput) return false;
        
        const name = nameInput.value;
        if (!name.trim()) return false;
        
        const parameterPath = this.getAuxiliaryPath(name);
        
        // Create new row
        const tr = document.createElement('tr');
        
        // Add parameter name cell
        const nameCell = document.createElement('td');
        nameCell.textContent = name;
        tr.appendChild(nameCell);
        
        // Add parameter value cell
        const valueCell = document.createElement('td');
        tr.appendChild(valueCell);
        
        if (this.options.multivalued) {
            // Create add value link for multivalued parameters
            const addValueLink = document.createElement('a');
            addValueLink.href = '#';
            addValueLink.setAttribute('paramName', name);
            addValueLink.className = `${this.options.displayClasses.addValueLink} ${this.options.displayClasses.addValueLinkExtraClass} btn btn-sm btn-info`;
            addValueLink.innerHTML = `${this.options.messages.addValue}&nbsp;&nbsp;<i class="fa fa-plus-circle"></i>`;
            
            addValueLink.addEventListener('click', (e) => {
                e.preventDefault();
                this.addValue(addValueLink);
            });
            
            valueCell.appendChild(addValueLink);
            
            // Add initial value
            this.addValue(addValueLink);
        } else {
            // Single value input
            const input = document.createElement('input');
            input.type = 'text';
            input.name = parameterPath;
            input.className = this.options.displayClasses.inputElementExtraClass;
            valueCell.appendChild(input);
        }
        
        // Add auxiliary checkbox if needed
        if (this.options.useAuxiliaryCheckbox) {
            const checkboxCell = document.createElement('td');
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.name = this.getAuxiliaryPath(name);
            checkbox.value = 'true';
            checkboxCell.appendChild(checkbox);
            tr.appendChild(checkboxCell);
        }
        
        // Add remove parameter cell
        const removeCell = document.createElement('td');
        const removeLink = document.createElement('a');
        removeLink.href = '#';
        removeLink.className = `${this.options.displayClasses.deleteItemLink} ${this.options.displayClasses.deleteItemLinkExtraClass} btn btn-warning`;
        removeLink.innerHTML = `${this.options.messages.removeParameter}&nbsp;&nbsp;<i class="fa fa-trash-o"></i>`;
        
        removeLink.addEventListener('click', (e) => {
            e.preventDefault();
            this.removeParameter(removeLink);
        });
        
        removeCell.appendChild(removeLink);
        tr.appendChild(removeCell);
        
        // Add row to table
        const tbody = this.container.querySelector(this.options.selectors.preferencesTable);
        if (tbody) {
            tbody.appendChild(tr);
        }
        
        // Close dialog
        if (this.options.dialog && up.jQuery) {
            up.jQuery(this.options.dialog).dialog('close');
        }
        
        return false;
    }
    
    removeParameter(link) {
        const row = link.closest('tr');
        if (row) {
            row.remove();
        }
    }
    
    addValue(link) {
        const paramName = link.getAttribute('paramName');
        if (!paramName) return;
        
        const parameterPath = this.getParameterPath(paramName);
        
        // Create value container
        const valueDiv = document.createElement('div');
        
        // Create input
        const input = document.createElement('input');
        input.type = 'text';
        input.name = parameterPath;
        input.className = this.options.displayClasses.inputElementExtraClass;
        valueDiv.appendChild(input);
        
        // Add spacing
        valueDiv.appendChild(document.createTextNode('  '));
        
        // Create remove link
        const removeLink = document.createElement('a');
        removeLink.href = '#';
        removeLink.className = `${this.options.displayClasses.deleteValueLink} ${this.options.displayClasses.deleteValueLinkExtraClass}`;
        removeLink.innerHTML = `${this.options.messages.remove}&nbsp;&nbsp;<i class="fa fa-minus-circle"></i>`;
        
        removeLink.addEventListener('click', (e) => {
            e.preventDefault();
            this.removeValue(removeLink);
        });
        
        valueDiv.appendChild(removeLink);
        
        // Insert before the add link
        link.parentNode.insertBefore(valueDiv, link);
    }
    
    removeValue(link) {
        const valueDiv = link.parentNode;
        if (valueDiv) {
            valueDiv.remove();
        }
    }
    
    showAddParameterDialog() {
        const dialog = this.options.dialog;
        if (!dialog) return;
        
        if (this.dialogInitialized) {
            // Reset form and open dialog
            const form = dialog.querySelector('form');
            if (form) {
                form.reset();
            }
            if (up.jQuery) {
                up.jQuery(dialog).dialog('open');
            }
        } else {
            // Initialize dialog
            const form = dialog.querySelector('form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    return this.addParameter(form);
                });
            }
            
            // Open dialog using jQuery UI
            if (up.jQuery) {
                up.jQuery(dialog).dialog();
            }
            
            this.dialogInitialized = true;
        }
    }
}

// Toggle chevron function for accordion
function toggleChevron(element) {
    const icon = element.querySelector('i.fa');
    if (icon) {
        if (icon.classList.contains('fa-chevron-down')) {
            icon.classList.remove('fa-chevron-down');
            icon.classList.add('fa-chevron-up');
        } else {
            icon.classList.remove('fa-chevron-up');
            icon.classList.add('fa-chevron-down');
        }
    }
}

// Make toggleChevron globally available
window.toggleChevron = toggleChevron;

// Global initialization function to replace Fluid component
window.up = window.up || {};
window.up.ParameterEditor = function(container, options) {
    return new ModernParameterEditor(container, options);
};