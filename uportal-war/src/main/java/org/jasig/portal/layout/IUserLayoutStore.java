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

 package org.jasig.portal.layout;

/**
 * Interface by which portal talks to the database
 * @author George Lindholm
 * @version $Revision$
 */

import java.util.Hashtable;
import java.util.Map;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.dlm.DistributedUserLayout;
import org.jasig.portal.layout.dlm.FragmentChannelInfo;
import org.jasig.portal.layout.dlm.FragmentNodeInfo;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.Tuple;
import org.w3c.dom.Document;

import com.google.common.cache.Cache;

public interface IUserLayoutStore {
    
    public void setLayoutImportExportCache(Cache<Tuple<String, String>, Document> layoutCache);

    /**
     * Retrieve a user layout document.
     *
     * @param Person an <code>IPerson</code> object specifying the user
     * @param profile a user profile 
     * @return a <code>Document</code> containing user layout (conforms to userLayout.dtd)
     * @exception Exception if an error occurs
     */
    public DistributedUserLayout getUserLayout (IPerson Person, IUserProfile profile);

    /**
     * Returns an <code>Element</code> representing the user's layout and 
     * <code>UserPreferences</code> (but not portlet preferences) formatted for 
     * export.  This element <em>must</em> have an element name of &lt;layout&gt;.  
     * Exported documents <em>must not</em> reference database identifiers and 
     * <em>should</em> exclude unnecessary items like channel publishing 
     * parameters, etc.  Layout store implementations are <em>may</em> return 
     * <code>null</code> for users that don't have layout or preferences 
     * customizations.
     *
     * @param person An <code>IPerson</code> object specifying the user
     * @param profile A valid profile for <code>person</code> 
     * @return A streamlined <code>Document</code> containing user layout and 
     * <code>UserPreferences</code> data
     * @exception Exception if an error occurs
     */
    public org.dom4j.Element exportLayout(IPerson person, IUserProfile profile);

    /**
     * Performs the reverse of <code>exportLayout</code>.  The specified element 
     * <em>must</em> have an element name of &lt;layout&gt; and <emshould</em> 
     * contain both content and <code>UserPreferences</code> data. 
     * 
     * @param layout XML representing a user's layout and <code>UserPreferences</code>
     */
    public void importLayout(org.dom4j.Element layout);

    /**
     * Persist user layout document.
     *
     * @param Person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param layoutXML a <code>Document</code> containing user layout (conforming to userLayout.dtd)
     * @param channelsAdded a boolean flag specifying if new channels have been added to the current user layout (for performance optimization purposes)
     * @exception Exception if an error occurs
     */
    public void setUserLayout (IPerson Person, IUserProfile  profile,Document layoutXML, boolean channelsAdded);

    // user profiles
    /** update user profile
     *
     * @param person User
     * @param profile profile update
     */
    public void updateUserProfile (IPerson person,IUserProfile profile);

    /** remove user profile from the database
     *
     * @param person User
     * @param profileId profile id
     */
    public void deleteUserProfile (IPerson person,int profileId);

    /**
     * Creates a new user profile in the database.
     * In the process, new profileId is assigned to the profile
     *
     * @param person User
     * @param profile profile object (profile id in this object will be
     *     overwritten)
     * @return profile object with the profile id set to the newly generated
     *     id
     */
    public IUserProfile addUserProfile (IPerson person,IUserProfile profile);

    /**  Obtains a user profile by profile id.    
     * @param person an <code>IPerson</code> object representing the user 
     * @param profileId profile id
     */
    public IUserProfile getUserProfileById (IPerson person,int profileId);

    /**  Obtains a user profile by profile functional name.    
     * @param person an <code>IPerson</code> object representing the user 
     * @param profileFname profile functional name
     */
    public IUserProfile getUserProfileByFname (IPerson person,String profileFname);
    
    /**
     * Cache used during import/export operations
     */
    public void setProfileImportExportCache(Cache<Tuple<String, String>, UserProfile> profileCache);

    /** retreive a list of profiles associated with a user
     *
     * @param person User
     * @return a <code>Hashtable</code> mapping user profile ids (<code>Integer</code> objects) to the {@link UserProfile} objects
     */
    public Hashtable<Integer, UserProfile> getUserProfileList (IPerson person);

    /** update system profile
     *
     * @param profile profile object
     */
    public void updateSystemProfile (IUserProfile profile);

    /** remove system profile from the database
     *
     * @param profileId profile id
     */
    public void deleteSystemProfile (int profileId);

    /** add a new system profile to the database. During this process, a new profile id will be assigned to the profile.
     *
     * @param profile profile object (profile id within will be overwritten)
     * @return profile with an newly assigned id
     */
    public IUserProfile addSystemProfile (IUserProfile profile);

    /** Obtain a system profile
     * @param profileId system profile id
     */
    public IUserProfile getSystemProfileById (int profileId);

    public IUserProfile getSystemProfileByFname (String profileFname);

    /** obtain a list of system profiles
     *
     * @return a <code>Hashtable</code> mapping system profile ids (<code>Integer</code> objects) to the {@link UserProfile} objects
     */
    public Hashtable getSystemProfileList ();

    /** establish a browser - user profile mapping
     *
     * @param person User
     * @param userAgent User-Agent header string
     * @param profileId profile id to which given user agent will be mapped
     */
    public void setUserBrowserMapping (IPerson person,String userAgent,int profileId);

    /** establish system profile browser mapping
     *
     * @param userAgent User-Agent header string
     * @param systemProfileId profile id of a profile to which given
     *     user-agent will be mapped
     */
    public void setSystemBrowserMapping (String userAgent,int systemProfileId);


  /* ChannelRegistry */
  /**
   * Generate an instance id for a channel being added to the user layout
   *
   * @param person an <code>IPerson</code> value
   * @return a <code>String</code> value
   * @exception Exception if an error occurs
   */
    public String generateNewChannelSubscribeId (IPerson person);
    
    /**
     * Generate a folder id for a folder being added to the user layout
     *
     * @param person an <code>IPerson</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    public String generateNewFolderId (IPerson person);

    /**
     * Method for acquiring copies of fragment layouts to assist in debugging.
     * No infrastructure code calls this but channels designed to expose the
     * structure of the cached fragments use this to obtain copies.
     * @return Map
     */
    public Map<String, Document> getFragmentLayoutCopies();
    
    /**
     * Returns an object suitable for identifying channel attribute and
     * parameter values in a user's layout that differ from the values on the
     * same element in a fragment. This is used by the layout manager to know
     * which ones must be persisted.
     *
     * @param sId
     * @return FragmentChannelInfo if available or null if not found.
     */
    public FragmentChannelInfo getFragmentChannelInfo(String sId);
    
    /**
     * Returns an object suitable for identifying attribute values for folder
     * nodes and attribute and parameter values for channel nodes in a user's
     * layout that differ from the values on the same element in a fragment.
     * This is used by the layout manager to know which ones must be persisted.
     *
     * @param sId
     * @return FragmentNodeInfo or null if folder not found.
     */
    public FragmentNodeInfo getFragmentNodeInfo(String sId);
    
    /**
     * Determines if a user is a fragment owner.
     * @param person
     * @return
     */
    public boolean isFragmentOwner(IPerson person);
    public boolean isFragmentOwner(String username);
    
    public void setUserLayout (IPerson person, IUserProfile profile,
            Document layoutXML, boolean channelsAdded,
            boolean updateFragmentCache);
    
    public Document getFragmentLayout (IPerson person,
            IUserProfile profile);
    
    /**
     * Generates a new struct id for directive elements that dlm places in
     * the PLF version of the layout tree. These elements are atifacts of the
     * dlm storage model and used during merge but do not appear in the user's
     * composite view.
     */
    public String getNextStructDirectiveId (IPerson person);

    /**
    Returns a double value indicating the precedence value declared for a
    fragment in the dlm.xml. Precedence is actually based on two elements in
    a fragment definition: the precedence and the index of the fragment
    definition in the dlm.xml file. If two fragments are given equal
    precedence then the index if relied upon to resolve conflicts with UI
    elements.
      */
    public double getFragmentPrecedence(int index);
}

