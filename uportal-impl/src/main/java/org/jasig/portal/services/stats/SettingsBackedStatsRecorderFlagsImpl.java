/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

/**
 * An IStatsRecorderFlags implementation backed by the StatsRecorderSettings
 * static singleton.
 * 
 * This implementation is required because StatsRecorderSettings is not just an
 * implementation that parses portal.properties - it also provides a static 
 * singleton mechanism to update that configuration at runtime.  This flags implementation
 * will immediately reflect any changes to that backing static singleton.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5.1
 */
public class SettingsBackedStatsRecorderFlagsImpl 
    implements IStatsRecorderFlags {

    public boolean isRecordChannelAddedToLayout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_ADDED_TO_LAYOUT);
    }

    public boolean isRecordChannelDefinitionModified() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_DEFINITION_MODIFIED);
    }

    public boolean isRecordChannelDefinitionPublished() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_DEFINITION_PUBLISHED); 
    }

    public boolean isRecordChannelDefinitionRemoved() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_DEFINITION_REMOVED);
    }

    public boolean isRecordChannelInstantiated() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_INSTANTIATED);
    }

    public boolean isRecordChannelMovedInLayout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_MOVED_IN_LAYOUT);
    }

    public boolean isRecordChannelRemovedFromLayout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_REMOVED_FROM_LAYOUT);
    }

    public boolean isRecordChannelRendered() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_RENDERED);
    }

    public boolean isRecordChannelTargeted() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_TARGETED);
    }

    public boolean isRecordChannelUpdatedInLayout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_CHANNEL_UPDATED_IN_LAYOUT);
    }

    public boolean isRecordFolderAddedToLayout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_FOLDER_ADDED_TO_LAYOUT);
    }

    public boolean isRecordFolderMovedInLayout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_FOLDER_MOVED_IN_LAYOUT);
    }

    public boolean isRecordFolderRemovedFromLayout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_FOLDER_REMOVED_FROM_LAYOUT);
    }

    public boolean isRecordFolderUpdatedInLayout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_FOLDER_UPDATED_IN_LAYOUT);
    }

    public boolean isRecordLogin() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_LOGIN);
    }

    public boolean isRecordLogout() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_LOGOUT);
    }

    public boolean isRecordSessionCreated() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_SESSION_CREATED);
    }

    public boolean isRecordSessionDestroyed() {
        return StatsRecorderSettings.instance().get(StatsRecorderSettings.RECORD_SESSION_DESTROYED);
    }

}

