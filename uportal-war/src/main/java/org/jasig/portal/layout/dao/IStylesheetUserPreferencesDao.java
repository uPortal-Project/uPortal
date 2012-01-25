/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.layout.dao;

import java.util.List;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.security.IPerson;

/**
 * Describes CRUD operations on {@link IStylesheetUserPreferences}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IStylesheetUserPreferencesDao {
    public IStylesheetUserPreferences createStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, IPerson person, IUserProfile profile);
    /**
     * @deprecated Use {@link #createStylesheetUserPreferences(IStylesheetDescriptor, IPerson, IUserProfile)}
     */
    @Deprecated
    public IStylesheetUserPreferences createStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, int userId, int profileId);
    
    public List<? extends IStylesheetUserPreferences> getStylesheetUserPreferences();
    
    public List<? extends IStylesheetUserPreferences> getStylesheetUserPreferencesForUser(int personId);
    
    public IStylesheetUserPreferences getStylesheetUserPreferences(long id);
    
    public IStylesheetUserPreferences getStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, IPerson person, IUserProfile profile);
    /**
     * @deprecated Use {@link #getStylesheetUserPreferences(IStylesheetDescriptor, IPerson, IUserProfile)}
     */
    @Deprecated
    public IStylesheetUserPreferences getStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, int personId, int profileId);
    
    public void storeStylesheetUserPreferences(IStylesheetUserPreferences stylesheetUserPreferences);
    
    public void deleteStylesheetUserPreferences(IStylesheetUserPreferences stylesheetUserPreferences);
}
