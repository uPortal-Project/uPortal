package org.jasig.portal;
import java.util.Hashtable;


/** 
 * Interface through which portal talks to the user preferences database
 * 
 * @author Peter Kharchenko
 * @version $Revision$
 */
public interface IUserPreferencesDB {


    // user profiles
    /** Obtain user profile
     * 
     * @param userId user id
     * @param userAgent User-Agent header string
     * @return user profile
     */
    public UserProfile getUserProfile(int userId, String userAgent);
    /** update user profile
     * 
     * @param userId user id
     * @param profile profile update
     */
    public void updateUserProfile(int userId,UserProfile profile);
    /** remove user profile from the database
     * 
     * @param userId user id
     * @param profileId profile id
     */
    public void deleteUserProfile(int userId,int profileId);
    /** 
     * Creates a new user profile in the database.
     * In the process, new profileId is assigned to the profile
     * 
     * @param userId user id
     * @param profile profile object (profile id in this object will be 
     *     overwritten)
     * @return profile object with the profile id set to the newly generated 
     *     id
     */
    public UserProfile addUserProfile(int userId,UserProfile profile);
    /**      * 
     * @param userId 
     * @param profileId 
     */
    public UserProfile getUserProfileById(int userId,int profileId);
    /** retreive a list of user profiles
     * 
     * @param userId user id
     * @return hashtable mapping user profile names to the profile objects
     */
    public Hashtable getUserProfileList(int userId);

    // syste profiles
    /** retreive a system profile
     * 
     * @param userAgent User-Agent header string
     * @return profile object
     */
    public UserProfile getSystemProfile(String userAgent);
    /** update system profile
     * 
     * @param profile profile object
     */
    public void updateSystemProfile(UserProfile profile);
    /** remove system profile from the database
     * 
     * @param profileId profile id
     */
    public void deleteSystemProfile(int profileId);
    /** add a new system profile to the database. During this process, a new profile id will be assigned to the profile.
     * 
     * @param profile profile object (profile id within will be overwritten)
     * @return profile with an newly assigned id
     */
    public UserProfile addSystemProfile(UserProfile profile);
    /**      * 
     * @param profileId 
     */
    public UserProfile getSystemProfileById(int profileId);
    /** obtain a list of system profiles
     * 
     * @return a hasbtable mapping system profile names to the profile objects
     */
    public Hashtable getSystemProfileList();

    /** establish a browser - user profile mapping
     * 
     * @param userId user id
     * @param userAgent User-Agent header string
     * @param profileId profile id to which given user agent will be mapped
     */
    public void setUserBrowserMapping(int userId,String userAgent,int profileId);
    /** establish system profile browser mapping
     * 
     * @param userAgent User-Agent header string
     * @param systemProfileId profile id of a profile to which given 
     *     user-agent will be mapped
     */
    public void setSystemBrowserMapping(String userAgent,int systemProfileId);

    // returns profileId
    /** Determine which profile a given browser mapped to
     * 
     * @param userId user id
     * @param userAgent User-Agent header string
     * @return profile id
     */
    public Integer getUserBrowserMapping(int userId,String userAgent);
    /** Determine which system profile given browser is mapped to
     * 
     * @param userAgent User-Agent header string
     * @return system profile id
     */
    public Integer getSystemBrowserMapping(String userAgent);


    /** Retreive the entire UserPreferences object
     * 
     * @param userId user id
     * @param profile profile
     * @return user preferences
     */
    public UserPreferences getUserPreferences(int userId, UserProfile profile);

    /** save user preferences 
     * 
     * @param userId user id
     * @param up user preferences object
     */
    public void putUserPreferences(int userId, UserPreferences up);

    /** Obtain structure stylesheet user preferences
     * 
     * @param userId user id
     * @param profileId profile id
     * @param stylesheetName structure stylesheet name
     * @return structure stylesheet user pferences
     */
    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(int userId,int profileId,String stylesheetName);
    /** Obtain theme styelsheet user preferences
     * 
     * @param userId user id
     * @param profileId profile id
     * @param stylesheetName theme stylesheet name
     * @return theme stylesheet user preferences
     */
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(int userId,int profileId,String stylesheetName);


    /** Save structure stylesheet user pferences
     * 
     * @param userId user id
     * @param profileId profile id
     * @param fsup structure stylesheet user preferences
     */
    public void setStructureStylesheetUserPreferences(int userId,int profileId, StructureStylesheetUserPreferences fsup);
    /** Save theme stylesheet user preferences
     * 
     * @param userId user id
     * @param profileId profile id
     * @param ssup structure stylesheet user preferneces
     */
    public void setThemeStylesheetUserPreferences(int userId,int profileId, ThemeStylesheetUserPreferences ssup);


}
