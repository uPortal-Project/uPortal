/**
 * Copyright ? 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.jasig.portal;

import org.apache.pluto.om.common.PreferenceSet;

/**
 * The IPortletPreferenceStore allows a portlet to atomicly persist preferences at two
 * different levels. Definition level preferences are common to all instances of a
 * channel. Entity level preferences are specific for on a per user per instance basis.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public interface IPortletPreferencesStore {
    /**
     * Stores the definition level preferences described by the PreferenceSet 
     * interface. The store operation is atomic, it can be assumed that if the 
     * method completes with no exception being thrown the store was successfull. 
     * If an exception is thrown it can be assumed that no changes were made to 
     * the underlying persistant store.
     * 
     * @param chanId The id of the channel to store the preferences for.
     * @param prefs The PreferenceSet which describes the data to store.
     * @throws Exception If any error occurs while storing the data.
     */
    public abstract void setDefinitionPreferences(final int chanId, final PreferenceSet prefs) throws Exception;
    
    /**
     * Gets the definition level preferences for the specified channel into an
     * implementation of the PreferenceSet interface. 
     * 
     * @param chanId The id of the channel to get the preferences for.
     * @return An implementation of the PreferenceSet interface that contains the preferences.
     * @throws Exception If any error occurs while getting the data.
     */
    public abstract PreferenceSet getDefinitionPreferences(final int chanId) throws Exception;
    
    /**
     * Stores the entity level preferences described by the PreferenceSet 
     * interface. The store operation is atomic, it can be assumed that if the 
     * method completes with no exception being thrown the store was successfull. 
     * If an exception is thrown it can be assumed that no changes were made to 
     * the underlying persistant store. The userId, layoutId and structId make up
     * the primary key for the preferences.
     * 
     * @param userId The id of the user to store the preferences for.
     * @param layoutId The layout fragment id to store the preferences for.
     * @param structId The structure id to store the preferences for.
     * @param prefs The PreferenceSet which describes the data to store.
     * @throws Exception If any error occurs while storing the data.
     */
    public abstract void setEntityPreferences(final int userId, final int layoutId, final int structId, final PreferenceSet prefs) throws Exception;

    /**
     * Gets the enitity level preferences for the specified channel into an
     * implementation of the PreferenceSet interface. The userId, layoutId and
     * structId make up the primary key for the preferences.
     *  
     * @param userId The id of the user to get the preferences for.
     * @param layoutId The layout fragment id to get the preferences for.
     * @param structId The structure id to get the preferences for.
     * @return An implementation of the PreferenceSet interface that contains the preferences.
     * @throws Exception If any error occurs while getting the data.
     */
    public abstract PreferenceSet getEntityPreferences(final int userId, final int layoutId, final int structId) throws Exception;
}
