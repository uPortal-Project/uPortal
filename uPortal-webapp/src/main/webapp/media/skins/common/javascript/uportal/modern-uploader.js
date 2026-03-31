/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
'use strict';
var up = up || {};

/**
 * Modern file uploader - no Fluid dependencies
 */
class ModernUploader {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        const defaultQueueSettings = {
            uploadURL: '/api/import',
            fileQueueLimit: 1
        };
        const providedQueueSettings = options.queueSettings || {};
        this.options = {
            ...options,
            queueSettings: {
                ...defaultQueueSettings,
                ...providedQueueSettings
            }
        };
        
        this.files = [];
        this.init();
    }

    init() {
        this.setupFileInput();
        this.setupQueue();
        this.setupButtons();
    }

    setupFileInput() {
        // Create hidden file input
        this.fileInput = document.createElement('input');
        this.fileInput.type = 'file';
        this.fileInput.multiple = this.options.queueSettings.fileQueueLimit > 1;
        this.fileInput.style.display = 'none';
        this.container.appendChild(this.fileInput);

        // Setup browse button
        const browseBtn = this.container.querySelector('.uploader-button-browse');
        if (browseBtn) {
            browseBtn.innerHTML = '<button type="button" class="btn btn-primary">Browse Files</button>';
            browseBtn.addEventListener('click', () => this.fileInput.click());
        }

        this.fileInput.addEventListener('change', (e) => this.handleFileSelect(e));
    }

    setupQueue() {
        this.queueTable = this.container.querySelector('.uploader-queue tbody');
        this.template = this.container.querySelector('.uploader-file-template');
        this.errorTemplate = this.container.querySelector('.uploader-file-error-template');
        this.totalProgress = this.container.querySelector('.uploader-total-progress-text p');
    }

    setupButtons() {
        this.uploadBtn = this.container.querySelector('.uploader-button-upload');
        this.pauseBtn = this.container.querySelector('.uploader-button-pause');

        if (this.uploadBtn) {
            this.uploadBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.uploadFiles();
            });
        }
    }

    handleFileSelect(event) {
        const files = Array.from(event.target.files);
        
        // Clear existing files if limit is 1
        if (this.options.queueSettings.fileQueueLimit === 1) {
            this.clearQueue();
        }

        files.forEach(file => this.addFile(file));
        this.updateUI();
    }

    addFile(file) {
        if (this.files.length >= this.options.queueSettings.fileQueueLimit) {
            return;
        }

        const fileObj = {
            id: Date.now() + Math.random(),
            file: file,
            name: file.name,
            size: file.size,
            status: 'queued'
        };

        this.files.push(fileObj);
        this.renderFileRow(fileObj);
    }

    renderFileRow(fileObj) {
        const row = this.template.cloneNode(true);
        row.classList.remove('uploader-file-template', 'd-none');
        row.setAttribute('data-file-id', fileObj.id);

        const nameCell = row.querySelector('.uploader-file-name');
        const sizeCell = row.querySelector('.uploader-file-size');
        const actionBtn = row.querySelector('.uploader-file-action');

        if (nameCell) nameCell.textContent = fileObj.name;
        if (sizeCell) sizeCell.textContent = this.formatFileSize(fileObj.size);
        
        if (actionBtn) {
            actionBtn.addEventListener('click', () => this.removeFile(fileObj.id));
        }

        this.queueTable.appendChild(row);
    }

    removeFile(fileId) {
        this.files = this.files.filter(f => f.id !== fileId);
        const row = this.container.querySelector(`[data-file-id="${fileId}"]`);
        if (row) row.remove();
        this.updateUI();
    }

    clearQueue() {
        this.files = [];
        const rows = this.queueTable.querySelectorAll('tr:not(.uploader-file-template):not(.uploader-file-error-template)');
        rows.forEach(row => row.remove());
        this.updateUI();
    }

    async uploadFiles() {
        if (this.files.length === 0) return;

        this.uploadBtn.disabled = true;
        if (this.pauseBtn) this.pauseBtn.classList.remove('d-none');

        for (const fileObj of this.files) {
            if (fileObj.status === 'queued') {
                await this.uploadFile(fileObj);
            }
        }

        this.uploadBtn.disabled = false;
        if (this.pauseBtn) this.pauseBtn.classList.add('d-none');

        const hasErrors = this.files.some(f => f.status === 'error');
        if (!hasErrors) {
            window.location.reload();
        }
    }

    async uploadFile(fileObj) {
        const formData = new FormData();
        formData.append('file', fileObj.file);

        try {
            fileObj.status = 'uploading';
            
            const response = await fetch(this.options.queueSettings.uploadURL, {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                fileObj.status = 'complete';
            } else {
                throw new Error(`Upload failed: ${response.statusText}`);
            }
        } catch (error) {
            fileObj.status = 'error';
            this.showError(fileObj, error.message);
        }
    }

    showError(fileObj, message) {
        const errorRow = this.errorTemplate.cloneNode(true);
        errorRow.classList.remove('uploader-file-error-template', 'd-none');
        
        const errorCell = errorRow.querySelector('.uploader-file-error');
        if (errorCell) {
            errorCell.textContent = `Error uploading ${fileObj.name}: ${message}`;
        }

        const fileRow = this.container.querySelector(`[data-file-id="${fileObj.id}"]`);
        if (fileRow) {
            fileRow.parentNode.insertBefore(errorRow, fileRow.nextSibling);
        }
    }

    updateUI() {
        const totalSize = this.files.reduce((sum, f) => sum + f.size, 0);
        if (this.totalProgress) {
            this.totalProgress.textContent = `Total: ${this.files.length} files (${this.formatFileSize(totalSize)})`;
        }

        if (this.uploadBtn) {
            this.uploadBtn.disabled = this.files.length === 0;
            this.uploadBtn.classList.toggle('opacity-50', this.files.length === 0);
        }
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    }
}

// Direct modern uploader - no Fluid compatibility needed
up.uploader = function(container, options) {
    return new ModernUploader(container, options);
};