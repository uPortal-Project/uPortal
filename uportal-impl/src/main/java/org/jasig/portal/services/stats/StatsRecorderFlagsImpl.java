/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

/**
 * Mutable JavaBean implementation of IStatsRecorderFlags.
 * Each IStatsRecorder method has an associated flag indicating whether we 
 * should attend to that method.
 * @version $Revision$ $Date$
 * @since uPortal 2.5.1
 * @see IStatsRecorder
 * @see IStatsRecorderFlags
 */
public class StatsRecorderFlagsImpl implements IStatsRecorderFlags {
    
    private boolean recordLogin = false;
    private boolean recordLogout = false;
    private boolean recordSessionCreated = false;
    private boolean recordSessionDestroyed = false;
    private boolean recordChannelDefinitionPublished = false;
    private boolean recordChannelDefinitionModified = false;
    private boolean recordChannelDefinitionRemoved = false;
    private boolean recordChannelAddedToLayout = false;
    private boolean recordChannelUpdatedInLayout = false;
    private boolean recordChannelMovedInLayout = false;
    private boolean recordChannelRemovedFromLayout = false;
    private boolean recordFolderAddedToLayout = false;
    private boolean recordFolderUpdatedInLayout = false;
    private boolean recordFolderMovedInLayout = false;
    private boolean recordFolderRemovedFromLayout = false;
    private boolean recordChannelInstantiated = false;
    private boolean recordChannelRendered = false;
    private boolean recordChannelTargeted = false;
    
    /**
     * Returns true if we should record when a channel is added to a layout;
     * false otherwise.
     * @return Returns the recordChannelAddedToLayout.
     */
    public boolean isRecordChannelAddedToLayout() {
        return this.recordChannelAddedToLayout;
    }
    
    /**
     * Set whether we should record when a channel is added to a layout.
     * @param recordChannelAddedToLayout true iff we should record when a channel is added to a layout
     */
    public void setRecordChannelAddedToLayout(boolean recordChannelAddedToLayout) {
        this.recordChannelAddedToLayout = recordChannelAddedToLayout;
    }
    
    /**
     * Returns true if we should record when a channel definition is modified.
     * @return Returns whether we should record when a channel definition is modified.
     */
    public boolean isRecordChannelDefinitionModified() {
        return this.recordChannelDefinitionModified;
    }
    
    /**
     * Set whether we should record when a channel definition is modified.
     * @param recordChannelDefinitionModified true iff we should record when a channel definition is modified.
     */
    public void setRecordChannelDefinitionModified(
            boolean recordChannelDefinitionModified) {
        this.recordChannelDefinitionModified = recordChannelDefinitionModified;
    }
    
    
    /**
     * Returns true if we should record when a channel definition is published, false otherwise.
     * @return Returns true iff we should record when a channel definition is published.
     */
    public boolean isRecordChannelDefinitionPublished() {
        return this.recordChannelDefinitionPublished;
    }
    
    /**
     * Set whether we should record when a channel definition is published.
     * @param recordChannelDefinitionPublished The recordChannelDefinitionPublished to set.
     */
    public void setRecordChannelDefinitionPublished(
            boolean recordChannelDefinitionPublished) {
        this.recordChannelDefinitionPublished = recordChannelDefinitionPublished;
    }
    
    /**
     * Returns true iff we should record when a channel definition is removed.
     * @return Returns true iff we should record when a channel definition is removed.
     */
    public boolean isRecordChannelDefinitionRemoved() {
        return this.recordChannelDefinitionRemoved;
    }
    
    /**
     * Set whether we should record when a channel definition is removed.
     * @param recordChannelDefinitionRemoved true iff we should record when a channel definition is removed.
     */
    public void setRecordChannelDefinitionRemoved(
            boolean recordChannelDefinitionRemoved) {
        this.recordChannelDefinitionRemoved = recordChannelDefinitionRemoved;
    }
    
    
    /**
     * Returns whether we should record when a channel is instantiated.
     * @return Returns whether we should record when a channel is instantiated.
     */
    public boolean isRecordChannelInstantiated() {
        return this.recordChannelInstantiated;
    }
    
    /**
     * Set whether we should record channel instantiation.
     * @param recordChannelInstantiated true iff we should record channel instantiation.
     */
    public void setRecordChannelInstantiated(boolean recordChannelInstantiated) {
        this.recordChannelInstantiated = recordChannelInstantiated;
    }
    
    /**
     * Returns whether we should record a channel being moved within a layout.
     * @return Returns true iff we should record a channel being moved within a layout.
     */
    public boolean isRecordChannelMovedInLayout() {
        return this.recordChannelMovedInLayout;
    }
    
    /**
     * Set whether we should record a channel being moved within a layout.
     * @param recordChannelMovedInLayout true iff we should record a channel being moved within a layout.
     */
    public void setRecordChannelMovedInLayout(boolean recordChannelMovedInLayout) {
        this.recordChannelMovedInLayout = recordChannelMovedInLayout;
    }
    
    /**
     * Returns true if we should record a channel being removed from a layout, false otherwise.
     * @return true if we should record a channel being removed from a layout, false otherwise.
     */
    public boolean isRecordChannelRemovedFromLayout() {
        return this.recordChannelRemovedFromLayout;
    }
    
    /**
     * Set whether we should record a channel being removed from a layout.
     * @param recordChannelRemovedFromLayout true iff we should record a channel being removed from a layout.
     */
    public void setRecordChannelRemovedFromLayout(
            boolean recordChannelRemovedFromLayout) {
        this.recordChannelRemovedFromLayout = recordChannelRemovedFromLayout;
    }
    
    
    /**
     * Returns true if we should record when a channel is rendered, false otherwise.
     * @return Returns true iff we should record when a channel is rendered.
     */
    public boolean isRecordChannelRendered() {
        return this.recordChannelRendered;
    }
    
    /**
     * Set whether we should record when a channel is rendered.
     * @param recordChannelRendered The recordChannelRendered to set.
     */
    public void setRecordChannelRendered(boolean recordChannelRendered) {
        this.recordChannelRendered = recordChannelRendered;
    }
    
    /**
     * Returns true if we should record when a channel is targetted, false otherwise.
     * @return Returns true iff we should record when a channel is targetted.
     */
    public boolean isRecordChannelTargeted() {
        return this.recordChannelTargeted;
    }
    
    /**
     * Set whether we should record when a channel is targetted.
     * @param recordChannelTargeted true iff we should record when a channel is targetted
     */
    public void setRecordChannelTargeted(boolean recordChannelTargeted) {
        this.recordChannelTargeted = recordChannelTargeted;
    }
    
    /**
     * Returns true if we should record when a channel is updated within a layout, false otherwise.
     * @return Returns true iff we should record when a channel is updated in a layout.
     */
    public boolean isRecordChannelUpdatedInLayout() {
        return this.recordChannelUpdatedInLayout;
    }
    
    /**
     * Set whether we should record a channel being updated within a layout.
     * @param recordChannelUpdatedInLayout true iff we should record a channel being updated within a layout.
     */
    public void setRecordChannelUpdatedInLayout(
            boolean recordChannelUpdatedInLayout) {
        this.recordChannelUpdatedInLayout = recordChannelUpdatedInLayout;
    }
    
    /**
     * Returns true if we should record when a folder is added to a layout, false otherwise.
     * @return Returns true iff we should record when a folder is added to a layout.
     */
    public boolean isRecordFolderAddedToLayout() {
        return this.recordFolderAddedToLayout;
    }
    
    /**
     * Set whether we should record when a folder is added to a layout.
     * @param recordFolderAddedToLayout true iff we should record when a folder is added to a layout.
     */
    public void setRecordFolderAddedToLayout(boolean recordFolderAddedToLayout) {
        this.recordFolderAddedToLayout = recordFolderAddedToLayout;
    }
    
    
    /**
     * Returns true if we should record that a folder is moved in a layout, false otherwise.
     * @return Returns true iff we should record that a folder is moved in a layout.
     */
    public boolean isRecordFolderMovedInLayout() {
        return this.recordFolderMovedInLayout;
    }
    
    /**
     * Set whether we should record that a folder is moved within a layout.
     * @param recordFolderMovedInLayout true iff we should record a folder being moved in a layout.
     */
    public void setRecordFolderMovedInLayout(boolean recordFolderMovedInLayout) {
        this.recordFolderMovedInLayout = recordFolderMovedInLayout;
    }
    
    /**
     * Returns true iff we should record a folder being removed from a layout.
     * @return Returns true if a folder is removed from a layout.
     */
    public boolean isRecordFolderRemovedFromLayout() {
        return this.recordFolderRemovedFromLayout;
    }
    
    /**
     * Set whether we should record a folder being removed from a layout.
     * @param recordFolderRemovedFromLayout true iff we should record a folder being removed from a layout
     */
    public void setRecordFolderRemovedFromLayout(
            boolean recordFolderRemovedFromLayout) {
        this.recordFolderRemovedFromLayout = recordFolderRemovedFromLayout;
    }
    
    /**
     * Returns true if we should record a folder being updated within a layout, false otherwise.
     * @return true iff we should record a folder being updated within a layout.
     */
    public boolean isRecordFolderUpdatedInLayout() {
        return this.recordFolderUpdatedInLayout;
    }
    
    /**
     * Set whether we should record a folder being updated within a layout.
     * @param recordFolderUpdatedInLayout true iff we should record a folder being updated within a layout.
     */
    public void setRecordFolderUpdatedInLayout(
            boolean recordFolderUpdatedInLayout) {
        this.recordFolderUpdatedInLayout = recordFolderUpdatedInLayout;
    }
    
    
    /**
     * Returns true iff we should record a user logging in.
     * @return true iff we should record a user logging in.
     */
    public boolean isRecordLogin() {
        return this.recordLogin;
    }
    
    /**
     * Set whether we should record a user logging in.
     * @param recordLogin true iff we should record a user logging in.
     */
    public void setRecordLogin(boolean recordLogin) {
        this.recordLogin = recordLogin;
    }
    
    /**
     * Returns true iff we should record a user logging out.
     * @return true iff we should record a user logging out.
     */
    public boolean isRecordLogout() {
        return this.recordLogout;
    }
    
    /**
     * Set whether we should record a user logging out.
     * @param recordLogout true iff we should record a user logging out.
     */
    public void setRecordLogout(boolean recordLogout) {
        this.recordLogout = recordLogout;
    }
    
    /**
     * Returns true if we should record a session being created, false otherwise.
     * @return true iff we should record when a session is created.
     */
    public boolean isRecordSessionCreated() {
        return this.recordSessionCreated;
    }
    
    /**
     * Set whether we should record a session being created.
     * @param recordSessionCreated true iff we should record when a session is created.
     */
    public void setRecordSessionCreated(boolean recordSessionCreated) {
        this.recordSessionCreated = recordSessionCreated;
    }
    
    
    /**
     * Return true iff we should record a session being destroyed.
     * @return true iff we should record a session being destroyed.
     */
    public boolean isRecordSessionDestroyed() {
        return this.recordSessionDestroyed;
    }
    
    /**
     * Set whether we should record a session being destroyed.
     * @param recordSessionDestroyed True iff we should record a session being destroyed.
     */
    public void setRecordSessionDestroyed(boolean recordSessionDestroyed) {
        this.recordSessionDestroyed = recordSessionDestroyed;
    }

}

