/*
 * Modern replacement for favorite portlet functionality
 * Replaces up-favorite.js with vanilla JavaScript implementation
 */
'use strict';

class ModernFavoriteManager {
    constructor(context = '/uPortal') {
        this.context = context;
    }

    async addToFavorite(portletId) {
        try {
            const response = await fetch(
                `${this.context}/api/layout?action=addFavorite&channelId=${portletId}`,
                { method: 'POST' }
            );

            if (!response.ok) {
                const body = await response.json().catch(() => null);
                throw new Error(body?.response || `HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            if (window.up?.notify) window.up.notify(result.response, 'TopCenter', 'success');
            return result;
        } catch (error) {
            console.error('Error adding to favorites:', error);
            if (window.up?.notify) window.up.notify(error.message || 'Error adding to favorites', 'TopCenter', 'error');
            
            throw error;
        }
    }

    async removeFromFavorite(portletId) {
        try {
            const response = await fetch(
                `${this.context}/api/layout?action=removeFavorite&channelId=${portletId}`,
                { method: 'POST' }
            );

            if (!response.ok) {
                const body = await response.json().catch(() => null);
                throw new Error(body?.response || `HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            if (window.up?.notify) window.up.notify(result.response, 'TopCenter', 'success');
            return result;
        } catch (error) {
            console.error('Error removing from favorites:', error);
            if (window.up?.notify) window.up.notify(error.message || 'Error removing from favorites', 'TopCenter', 'error');
            
            throw error;
        }
    }

    async moveStuff(tabOrPortlet, item, context = this.context) {
        const sourceID = item.getAttribute('sourceid');
        const nextElement = item.nextElementSibling;
        const prevElement = item.previousElementSibling;
        
        const nextId = nextElement?.getAttribute('sourceid') || '';
        const previousId = prevElement?.getAttribute('sourceid') || '';

        if (tabOrPortlet === 'Tab') {
            return this.moveFavoriteGroup(sourceID, previousId, nextId, context);
        } else {
            return this.insertNode(sourceID, previousId, nextId, context);
        }
    }

    async insertNode(sourceId, previousNodeId, nextNodeId, context = this.context) {
        const url = `${context}/api/layout?action=movePortletAjax&sourceId=${sourceId}&previousNodeId=${previousNodeId}&nextNodeId=${nextNodeId}`;
        
        try {
            const response = await fetch(url, { method: 'POST' });

            if (!response.ok) {
                const body = await response.json().catch(() => null);
                throw new Error(body?.response || `HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error persisting move:', url, error);
            throw error;
        }
    }

    async moveFavoriteGroup(sourceId, previousNodeId, nextNodeId, context = this.context) {
        const method = nextNodeId === '' ? 'appendAfter' : 'insertBefore';
        const elementId = nextNodeId === '' ? previousNodeId : nextNodeId;
        const url = `${context}/api/layout?action=moveTab&sourceID=${sourceId}&method=${method}&elementID=${elementId}`;
        
        try {
            const response = await fetch(url, { method: 'POST' });

            if (!response.ok) {
                const body = await response.json().catch(() => null);
                throw new Error(body?.response || `HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error persisting favorite group reorder:', url, error);
            throw error;
        }
    }
}

// Global instance and compatibility functions
window.up = window.up || {};
window.up.favoriteManager = new ModernFavoriteManager();

// Compatibility functions that match the original API
window.up.addToFavorite = function(event) {
    const portletId = event.data?.portletId;
    const context = event.data?.context || '/uPortal';
    
    if (!portletId) {
        console.error('No portletId provided to addToFavorite');
        return Promise.reject(new Error('No portletId provided'));
    }
    
    const manager = new ModernFavoriteManager(context);
    return manager.addToFavorite(portletId);
};

window.up.removeFromFavorite = function(event) {
    const portletId = event.data?.portletId;
    const context = event.data?.context || '/uPortal';
    
    if (!portletId) {
        console.error('No portletId provided to removeFromFavorite');
        return Promise.reject(new Error('No portletId provided'));
    }
    
    const manager = new ModernFavoriteManager(context);
    return manager.removeFromFavorite(portletId);
};

window.up.moveStuff = function(tabOrPortlet, item, context) {
    const manager = new ModernFavoriteManager(context);
    return manager.moveStuff(tabOrPortlet, item, context);
};