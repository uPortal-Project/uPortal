/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

/**
 * Interface for objects conveying a bundle of boolean flags configuring what
 * IStatsRecorder events should propogate.
 * @version $Revision$ $Date$
 */
public interface IStatsRecorderFlags {
    /**
     * Returns true if we should record when a channel is added to a layout;
     * false otherwise.
     * @return Returns the recordChannelAddedToLayout.
     */
    public boolean isRecordChannelAddedToLayout();

    /**
     * Returns true if we should record when a channel definition is modified.
     * @return Returns whether we should record when a channel definition is modified.
     */
    public boolean isRecordChannelDefinitionModified();

    /**
     * Returns true if we should record when a channel definition is published, false otherwise.
     * @return Returns true iff we should record when a channel definition is published.
     */
    public boolean isRecordChannelDefinitionPublished();

    /**
     * Returns true iff we should record when a channel definition is removed.
     * @return Returns true iff we should record when a channel definition is removed.
     */
    public boolean isRecordChannelDefinitionRemoved();

    /**
     * Returns whether we should record when a channel is instantiated.
     * @return Returns whether we should record when a channel is instantiated.
     */
    public boolean isRecordChannelInstantiated();

    /**
     * Returns whether we should record a channel being moved within a layout.
     * @return Returns true iff we should record a channel being moved within a layout.
     */
    public boolean isRecordChannelMovedInLayout();

    /**
     * Returns true if we should record a channel being removed from a layout, false otherwise.
     * @return true if we should record a channel being removed from a layout, false otherwise.
     */
    public boolean isRecordChannelRemovedFromLayout();

    /**
     * Returns true if we should record when a channel is rendered, false otherwise.
     * @return Returns true iff we should record when a channel is rendered.
     */
    public boolean isRecordChannelRendered();

    /**
     * Returns true if we should record when a channel is targetted, false otherwise.
     * @return Returns true iff we should record when a channel is targetted.
     */
    public boolean isRecordChannelTargeted();

    /**
     * Returns true if we should record when a channel is updated within a layout, false otherwise.
     * @return Returns true iff we should record when a channel is updated in a layout.
     */
    public boolean isRecordChannelUpdatedInLayout();

    /**
     * Returns true if we should record when a folder is added to a layout, false otherwise.
     * @return Returns true iff we should record when a folder is added to a layout.
     */
    public boolean isRecordFolderAddedToLayout();

    /**
     * Returns true if we should record that a folder is moved in a layout, false otherwise.
     * @return Returns true iff we should record that a folder is moved in a layout.
     */
    public boolean isRecordFolderMovedInLayout();

    /**
     * Returns true iff we should record a folder being removed from a layout.
     * @return Returns true if a folder is removed from a layout.
     */
    public boolean isRecordFolderRemovedFromLayout();

    /**
     * Returns true if we should record a folder being updated within a layout, false otherwise.
     * @return true iff we should record a folder being updated within a layout.
     */
    public boolean isRecordFolderUpdatedInLayout();

    /**
     * Returns true iff we should record a user logging in.
     * @return true iff we should record a user logging in.
     */
    public boolean isRecordLogin();

    /**
     * Returns true iff we should record a user logging out.
     * @return true iff we should record a user logging out.
     */
    public boolean isRecordLogout();

    /**
     * Returns true if we should record a session being created, false otherwise.
     * @return true iff we should record when a session is created.
     */
    public boolean isRecordSessionCreated();

    /**
     * Return true iff we should record a session being destroyed.
     * @return true iff we should record a session being destroyed.
     */
    public boolean isRecordSessionDestroyed();
}