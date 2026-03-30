/*
 * Modern replacement for rating modal functionality
 * Replaces up-rating-option.js with vanilla JavaScript implementation
 */
'use strict';

class ModernRatingModal {
    constructor(element) {
        this.element = element;
        this.modal = null;
        this.ratingValue = 0;
        this.init();
    }

    init() {
        this.createModal();
        this.bindEvents();
    }

    createModal() {
        const closeLabel = this.element.dataset['close.button.label'] || 'Close';
        const saveLabel = this.element.dataset['save.button.label'] || 'Save';

        const modalHTML = `
            <div class="modal-dialog ratePortletModal-dialog" style="text-align:center; position:static">
                <div class="modal-content" style="display:inline-block">
                    <div class="modal-header">
                        <h4 class="modal-title" style="white-space: nowrap"><strong class="modal-title-text"></strong></h4>
                    </div>
                    <div class="modal-body" style="font-size:2em;">
                        <div class="form-text ratingModalInstruct" style="font-size:0.5em; white-space:normal; word-wrap:break-word;"></div>
                        <div class="rating-container">
                            ${this.createStarRating()}
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary modal-close-btn" data-bs-dismiss="modal"></button>
                        <button type="button" class="btn btn-primary ratingModalSaveButton disabled modal-save-btn"></button>
                    </div>
                </div>
            </div>
        `;

        this.element.innerHTML = modalHTML;

        this.element.querySelector('.modal-title-text').textContent = this.element.dataset.title || '';
        this.element.querySelector('.modal-close-btn').textContent = closeLabel;
        this.element.querySelector('.modal-save-btn').textContent = saveLabel;

        this.modal = new bootstrap.Modal(this.element);
        
        // Add backdrop cleanup
        this.element.addEventListener('hidden.bs.modal', () => {
            document.querySelectorAll('.modal-backdrop').forEach(backdrop => backdrop.remove());
            document.body.classList.remove('modal-open');
        });
    }

    createStarRating() {
        return `
            <div class="star-rating" data-rating="0">
                ${Array.from({length: 5}, (_, i) => 
                    `<span class="star" data-value="${i + 1}">★</span>`
                ).join('')}
            </div>
            <style>
                .star-rating { font-size: 2em; cursor: pointer; }
                .star { color: #ddd; transition: color 0.2s; }
                .star.active { color: #ffc107; }
                .star:hover, .star.hover { color: #ffc107; }
            </style>
        `;
    }

    bindEvents() {
        // Star rating events
        const stars = this.element.querySelectorAll('.star');
        const saveButton = this.element.querySelector('.ratingModalSaveButton');

        stars.forEach((star, index) => {
            star.addEventListener('mouseenter', () => this.highlightStars(index + 1));
            star.addEventListener('mouseleave', () => this.highlightStars(this.ratingValue));
            star.addEventListener('click', () => {
                this.ratingValue = index + 1;
                this.highlightStars(this.ratingValue);
                saveButton.classList.remove('disabled');
            });
        });

        // Save button event
        saveButton.addEventListener('click', () => this.saveRating());

        // Modal show event
        this.element.addEventListener('show.bs.modal', () => this.loadCurrentRating());
    }

    highlightStars(rating) {
        const stars = this.element.querySelectorAll('.star');
        stars.forEach((star, index) => {
            star.classList.toggle('active', index < rating);
        });
    }

    loadCurrentRating() {
        const saveButton = this.element.querySelector('.ratingModalSaveButton');
        const instructEl = this.element.querySelector('.ratingModalInstruct');
        const that = this;

        saveButton.classList.add('disabled');

        up.jQuery.ajax({
            url: this.element.dataset.geturl,
            success: function(data) {
                if (data.rating === null) {
                    that.ratingValue = 0;
                    that.highlightStars(0);
                    instructEl.textContent = that.element.dataset['rating.instructions.unrated'] || '';
                } else {
                    that.ratingValue = data.rating;
                    that.highlightStars(data.rating);
                    saveButton.classList.remove('disabled');
                    instructEl.textContent = that.element.dataset['rating.instructions.rated'] || '';
                }

                // Center modal dialog
                const dialog = that.element.querySelector('.modal-dialog');
                dialog.style.transform = 'translate(0, 50%)';
            },
            error: function() {
                ModernNotification.show(
                    that.element.dataset['get.rating.unsucessful'] || 'Error loading rating',
                    'TopCenter',
                    'error'
                );
            }
        });
    }

    saveRating() {
        if (this.ratingValue === 0) return;

        const that = this;
        up.jQuery.ajax({
            url: this.element.dataset.saveurl,
            data: { rating: this.ratingValue },
            type: 'POST',
            success: function() {
                that.modal.hide();
                ModernNotification.show(
                    that.element.dataset['rating.save.successful'] || 'Rating saved successfully',
                    'TopCenter',
                    'success'
                );
                setTimeout(() => {
                    const instructEl = that.element.querySelector('.ratingModalInstruct');
                    if (instructEl) {
                        instructEl.textContent = that.element.dataset['rating.instructions.rated'] || '';
                    }
                }, 1000);
            },
            error: function() {
                that.modal.hide();
                ModernNotification.show(
                    that.element.dataset['rating.save.unsuccessful'] || 'Error saving rating',
                    'TopCenter',
                    'error'
                );
            }
        });
    }

    show() {
        this.modal.show();
    }

    hide() {
        this.modal.hide();
    }
}

// Simple notification system (replaces noty dependency)
class ModernNotification {
    static show(message, position = 'TopCenter', type = 'info') {
        const notification = document.createElement('div');
        notification.className = `modern-notification notification-${type}`;
        notification.textContent = message;
        
        // Position styles
        const positions = {
            'TopCenter': { top: '20px', left: '50%', transform: 'translateX(-50%)' },
            'TopRight': { top: '20px', right: '20px' },
            'TopLeft': { top: '20px', left: '20px' }
        };
        
        Object.assign(notification.style, {
            position: 'fixed',
            zIndex: '9999',
            padding: '12px 20px',
            borderRadius: '4px',
            color: 'white',
            fontWeight: 'bold',
            maxWidth: '400px',
            ...positions[position] || positions.TopCenter
        });

        // Type-specific styles
        const typeColors = {
            success: '#28a745',
            error: '#dc3545',
            info: '#17a2b8',
            warning: '#ffc107'
        };
        
        notification.style.backgroundColor = typeColors[type] || typeColors.info;
        
        document.body.appendChild(notification);
        
        // Auto-remove after delay
        setTimeout(() => {
            notification.style.opacity = '0';
            notification.style.transition = 'opacity 0.3s';
            setTimeout(() => notification.remove(), 300);
        }, type === 'error' ? 5000 : 2000);
    }
}

// Global functions for compatibility
window.up = window.up || {};
window.up.notify = ModernNotification.show;

// jQuery plugin compatibility
if (window.jQuery) {
    window.jQuery.fn.createRatingModal = function() {
        return this.each(function() {
            if (!this.modernRatingModal) {
                this.modernRatingModal = new ModernRatingModal(this);
            }
        });
    };
}