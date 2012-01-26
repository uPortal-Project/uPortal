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

package org.jasig.portal.layout.dlm;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import net.sf.ehcache.Ehcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.UserProfile;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.utils.ConcurrentMapUtils;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.threading.ReadResult;
import org.jasig.portal.utils.threading.ReadWriteCallback;
import org.jasig.portal.utils.threading.ReadWriteLockTemplate;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Service
public class FragmentActivator extends SingletonDoubleCheckedCreator<Boolean>
{
    public static final String RCS_ID = "@(#) $Header$";
    private static final Log LOG = LogFactory.getLog(FragmentActivator.class);

    private final ConcurrentMap<String, ReadWriteLock> userViewLocks = new ConcurrentHashMap<String, ReadWriteLock>();
    
    private Ehcache userViews;
    private IUserIdentityStore identityStore;
    private IUserLayoutStore userLayoutStore;
    private IStylesheetUserPreferencesDao stylesheetUserPreferencesDao;
    private ConfigurationLoader configurationLoader;

    private static final int CHANNELS = 0;
    private static final int FOLDERS = 1;
    
    private static final String PROPERTY_ALLOW_EXPANDED_CONTENT = "org.jasig.portal.layout.dlm.allowExpandedContent";
    private static final Pattern STANDARD_PATTERN = Pattern.compile("\\A[Rr][Ee][Gg][Uu][Ll][Aa][Rr]\\z");
    private static final Pattern EXPANDED_PATTERN = Pattern.compile(".*");

    @Autowired
    public void setUserViews(@Qualifier("org.jasig.portal.layout.dlm.FragmentActivator.userViews") Ehcache userViews) {
        this.userViews = userViews;
    }

    @Autowired
    public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    @Autowired
    public void setIdentityStore(IUserIdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Autowired
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    @Autowired
    public void setStylesheetUserPreferencesDao(IStylesheetUserPreferencesDao stylesheetUserPreferencesDao) {
        this.stylesheetUserPreferencesDao = stylesheetUserPreferencesDao;
    }
    

    /**
     * Activation will only be run once and will return immediately for every call once activation
     * is complete.
     */
    void activateFragments() {
        this.get();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator#createSingleton(java.lang.Object[])
     */
    @Override
    protected Boolean createSingleton(Object... args) {
        final List<FragmentDefinition> fragments = this.configurationLoader.getFragments();
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("\n\n------ Distributed Layout ------\n" +
              "fragment definitions loaded = " +
              ( fragments == null ? 0 : fragments.size() ) +
              "\n\n------ Beginning Activation ------\n" );
        }
        
        if ( fragments == null )
        {
            if (LOG.isDebugEnabled()) {
                LOG.debug("\n\nNo Fragments to Activate." );
            }
        }
        else
        {
            for (final FragmentDefinition fragmentDefinition : fragments) {
                for (Locale locale : LocaleManager.getPortalLocales()) {
                    activateFragment(fragmentDefinition, locale);
                }
            }
        }
        
        // now let other threads in to get their layouts.
        if (LOG.isDebugEnabled()) {
            LOG.debug("\n\n------ done with Activation ------\n" );
        }
        
        return true;
    }
    
    private UserView activateFragment(FragmentDefinition fd, Locale locale) {
        
        // Assertions.
        if (fd == null) {
            String msg = "Argument 'fd' [FragmentDefinition] cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        if (fd.isNoAudienceIncluded())
        {
            if (LOG.isDebugEnabled()) {
                LOG.debug("\n\n------ skipping " + fd + " - " +
                        fd.getName() + ", no evaluators found" );
            }
        }
        else
        {
            if (LOG.isDebugEnabled()) {
                LOG.debug("\n\n------ activating " + fd + " - " + fd.getName() + " (locale = " + locale.toString() + ")");
            }

            try
            {
                IPerson owner = bindToOwner(fd);
                UserView view = new UserView(owner.getID());
                loadLayout( view, fd, owner, locale );
                
                // if owner just created we need to push the layout into
                // the db so that our fragment template user is used and
                // not the default template user as determined by
                // the user identity store.
                if (owner.getAttribute("newlyCreated") != null)
                {
                    owner.setAttribute( Constants.PLF, view.layout );
                    saveLayout( view, owner );
                }
                loadPreferences( view, fd);
                fragmentizeLayout( view, fd);
                this.setUserView(fd.getOwnerId(), locale, view);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\n\n------ done activating " + fd.getName() + " (locale = " + locale.toString() + ")" );
                }
                
                return view;
            }
            catch( Exception e )
            {
                final String msg = "Problem activating DLM fragment '" + fd.getName() + "' no content for this fragment will be included in user layouts.";
                if (LOG.isDebugEnabled()) {
                    LOG.debug(msg, e);
                }
                else {
                    LOG.warn(msg + " Enable DEBUG logging for the full stack trace.");
                }
            }
            
        }

        return null;
    }
    
    public UserView getUserView(final FragmentDefinition fd, final Locale locale) {
        
        // Assertions...
        if (fd == null) {
            String msg  = "Argument 'fd' [FragmentDefinition] cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        // Activate the fragment just-in-time if it's new using some locking to make sure the same
        // fragment isn't being loaded by multiple concurrent threads
        final String ownerId = fd.getOwnerId();
        final ReadWriteLock userViewLock = getUserViewLock(ownerId);
        final UserView rslt = 
            ReadWriteLockTemplate.doWithLock(userViewLock, new ReadWriteCallback<UserView>() {
                public ReadResult<UserView> doInReadLock() {
                    final net.sf.ehcache.Element element = getUserView(ownerId, locale);
                    final UserView userView = element != null ? (UserView)element.getObjectValue() : null;
                    if (userView == null) {
                        return new ReadResult<UserView>(true);
                    }
                    
                    return new ReadResult<UserView>(false, userView);
                }
    
                public UserView doInWriteLock(ReadResult<UserView> readResult) {
                    final UserView userView = activateFragment(fd, locale);
                    
                    if (userView == null) {
                        // This is worrysome...
                        LOG.warn("No UserView object could be activated for owner '" + ownerId 
                                                    + "' -- null will be returned");
                    }
                    
                    return userView;
                }
            });
            
        return rslt;
        
    }

    protected ReadWriteLock getUserViewLock(final String ownerId) {
        final ReadWriteLock userViewLock = userViewLocks.get(ownerId);
        if (userViewLock != null) {
            return userViewLock;
        }
        
        return ConcurrentMapUtils.putIfAbsent(userViewLocks, ownerId, new ReentrantReadWriteLock(true));
    }
    
    public void setUserView(String ownerId, Locale locale, UserView v) {
        
        // Assertions.
        if (ownerId == null) {
            String msg = "Argument 'ownerId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (v == null) {
            String msg = "Argument 'v' [UserView] cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting UserView instance for user:  " + ownerId);
        }
        
        userViews.put(new net.sf.ehcache.Element(new Tuple<String, String>(ownerId, locale.toString()), v));
        
    }
    
    public boolean hasUserView(FragmentDefinition fd, Locale locale) {

        // Assertions...
        if (fd == null) {
            String msg  = "Argument 'fd' [FragmentDefinition] cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return getUserView(fd.getOwnerId(), locale) != null;

    }
    
    /**
     * Saves the loaded layout in the database for the user and profile.
     * @param view
     * @param owner
     * @throws Exception
     */
    private void saveLayout(UserView view, IPerson owner) throws Exception
    {
        IUserProfile profile = new UserProfile();
        profile.setProfileId(view.profileId);
        userLayoutStore.setUserLayout(owner, profile, view.layout, true, false);
    }

    private IPerson bindToOwner( FragmentDefinition fragment )
    {
        IPerson owner = new PersonImpl();
        owner.setAttribute( "username", fragment.getOwnerId() );
        int userID = -1;
        
        try
        {
            userID = identityStore.getPortalUID( owner, false );
        }
        catch( AuthorizationException ae )
        {
            // current implementation of RDMBUserIdentityStore throws an
            // auth exception if the user doesn't exist even if 
            // create data is false as we have it here. So this exception
            // can be discarded since we check for the userID being -1
            // meaning that the user wasn't found to trigger creating
            // that user.
        }
        if (userID == -1)
        {
            userID = createOwner( owner, fragment );
            owner.setAttribute("newlyCreated", "" + (userID != -1));
        }

        owner.setID(userID);
        return owner;
    }
    
    private int createOwner( IPerson owner, FragmentDefinition fragment )
    {
        String defaultUser = null;
        int userID = -1;
            
        if ( fragment.defaultLayoutOwnerID != null ) {
            defaultUser = fragment.defaultLayoutOwnerID;
        }
        else {
            final String defaultLayoutOwner = PropertiesManager.getProperty("org.jasig.portal.layout.dlm.defaultLayoutOwner");
            if ( defaultLayoutOwner != null ) {
                defaultUser = defaultLayoutOwner;
            }
            else {
                try
                {
                    defaultUser = PropertiesManager.getProperty( RDBMDistributedLayoutStore.TEMPLATE_USER_NAME );
                }
                catch( RuntimeException re )
                {
                    throw new RuntimeException(
                            "\n\n WARNING: defaultLayoutOwner is not specified" +
                            " in dlm.xml and no default user is configured for " +
                            "the system. Owner '" + fragment.getOwnerId() + "' for " +
                            "fragment '" + fragment.getName() + "' can not be " +
                            "created. The fragment will not be available for " +
                            "inclusion into user layouts.\n", re );
                }
            }
        }

            if (LOG.isDebugEnabled())
                LOG.debug("\n\nOwner '" + fragment.getOwnerId() +
                "' of fragment '" + fragment.getName() +
                "' not found. Creating as copy of '" +
                defaultUser + "'\n" );

        if ( defaultUser != null )
            owner.setAttribute( "uPortalTemplateUserName", defaultUser );
        
        try
        {
            userID = identityStore.getPortalUID( owner, true );
        }
        catch( AuthorizationException ae )
        {
            throw new RuntimeException(
                  "\n\nWARNING: Anomaly occurred while creating owner '" +
                  fragment.getOwnerId() + "' of fragment '" + fragment.getName() +
                  "'. The fragment will not be " +
                  "available for inclusion into user layouts.", ae );
        }
        return userID;
    }
    private void loadLayout( UserView view,
                             FragmentDefinition fragment,
                             IPerson owner, Locale locale )
    {
        // if fragment not bound to user can't return any layouts.
        if ( view.getUserId() == -1 )
            return;

        // this area is hacked right now. Time won't permit how to handle
        // matching up multiple profiles for a fragment with an appropriate
        // one for incorporating into a user's layout based on their profile
        // when they log in with a certain user agent. The challenge is
        // being able to match up profiles for a user with those of a 
        // fragment. Until this is resolved only one profile will be supported
        // and will have a hard coded id of 1 which is the default for profiles.
        // If anyone changes this user all heck could break loose for dlm. :-(
        
        Document layout = null;

        try
        {
            // fix hard coded 1 later for multiple profiles
            IUserProfile profile = userLayoutStore.getUserProfileByFname(owner, "default");
            profile.setLocaleManager(new LocaleManager(owner, new Locale[] { locale }));
            
            // see if we have structure & theme stylesheets for this user yet.
            // If not then fall back on system's selected stylesheets.
            if (profile.getStructureStylesheetId() == 0 ||
                    profile.getThemeStylesheetId() == 0)
                profile = userLayoutStore.getSystemProfileByFname(profile.getProfileFname());
            
            view.profileId = profile.getProfileId();
            view.profileFname = profile.getProfileFname();
            view.layoutId = profile.getLayoutId();
//            view.structureStylesheetId = profile.getStructureStylesheetId();
//            view.themeStylesheetId = profile.getThemeStylesheetId();
            
            layout = userLayoutStore.getFragmentLayout( owner, profile ); 
            Element root = layout.getDocumentElement();
            root.setAttribute( Constants.ATT_ID, 
                    Constants.FRAGMENT_ID_USER_PREFIX + view.getUserId() +
                    Constants.FRAGMENT_ID_LAYOUT_PREFIX + view.layoutId );
            view.layout = layout;
        }
        catch( Exception e )
        {
            throw new RuntimeException(
                  "Anomaly occurred while loading layout for fragment '" +
                  fragment.getName() +
                  "'. The fragment will not be " +
                  "available for inclusion into user layouts.", e );
        }
    }

    private void loadPreferences( UserView view,
                                  FragmentDefinition fragment )
    {
        // if fragment not bound to user can't return any preferences.
        if ( view.getUserId() == -1 )
            return;

        IPerson p = new PersonImpl();
        p.setID( view.getUserId() );
        p.setAttribute( "username", fragment.getOwnerId() );
    }

    /**
     * Removes unwanted and hidden folders, then changes all node ids to their 
     * globally safe incorporated version.
     */
    void fragmentizeLayout( UserView view,
                            FragmentDefinition fragment )
    {
        // if fragment not bound to user or layout empty due to error, return
        if ( view.getUserId() == -1 ||
             view.layout == null )
            return;

        // Choose what types of content to apply from the fragment
        Pattern contentPattern = STANDARD_PATTERN;  // default
        boolean allowExpandedContent = Boolean.parseBoolean(PropertiesManager.getProperty(PROPERTY_ALLOW_EXPANDED_CONTENT));
        if (allowExpandedContent) {
            contentPattern = EXPANDED_PATTERN;
        }

        // remove all non-regular or hidden top level folders
        // skip root folder that is only child of top level layout element
        Element layout = view.layout.getDocumentElement();
        Element root = (Element) layout.getFirstChild();
        NodeList children = root.getChildNodes();

        // process the children backwards since as we delete some the indices
        // shift around
        for( int i=children.getLength()-1; i>=0; i-- )
        {
            Node node = children.item(i);
            if ( node.getNodeType() == Node.ELEMENT_NODE &&
                 node.getNodeName().equals("folder") )
            {
                Element folder = (Element) node;

                // strip out folder types 'header', 'footer' and regular, 
                // hidden folder "User Preferences" since users have their own
                boolean isApplicable = contentPattern.matcher(folder.getAttribute("type")).matches();
                if (!isApplicable || folder.getAttribute("hidden").equals("true")) {
                    try
                    {
                        root.removeChild( folder );
                    }
                    catch( Exception e )
                    {
                        throw new RuntimeException(
                              "Anomaly occurred while stripping out " +
                              " portions of layout for fragment '" +
                              fragment.getName() +
                              "'. The fragment will not be available for " +
                              "inclusion into user layouts.", e );
                    }
                }
            }
        }
        // now re-lable all remaining nodes below root to have a safe system
        // wide id.

        setIdsAndAttribs( layout, layout.getAttribute( Constants.ATT_ID ),
                          "" + fragment.getIndex(),
                          "" + fragment.getPrecedence() );
    }

    /**
     * Recursive method that passes through a layout tree and changes all ids
     * from the regular format of sXX or nXX to the globally safe incorporated
     * id of form uXlXsXX or uXlXnXX indicating the user id and layout id from
     * which this node came.
     */
    private void setIdsAndAttribs( Element parent,
                                   String labelBase,
                                   String index,
                                   String precedence )
    {
        NodeList children = parent.getChildNodes();

        for ( int i=0; i<children.getLength(); i++ )
        {
            if ( children.item(i).getNodeType() == Node.ELEMENT_NODE )
            {
                Element child = (Element) children.item(i);
                String id = child.getAttribute( Constants.ATT_ID );
                if ( ! id.equals( "" ) )
                {
                    String newId = labelBase + id;
                    child.setAttribute( Constants.ATT_ID, newId );
                    child.setIdAttribute(Constants.ATT_ID, true);
                    child.setAttributeNS( Constants.NS_URI,
                                          Constants.ATT_FRAGMENT,
                                          index );
                    child.setAttributeNS( Constants.NS_URI,
                                          Constants.ATT_PRECEDENCE,
                                          precedence );
                    setIdsAndAttribs( child, labelBase, index, precedence );
                }
            }
        }
    }
    
    private net.sf.ehcache.Element getUserView(String ownerId, Locale locale) {
        return userViews.get(new Tuple<String, String>(ownerId, locale.toString()));
    }
    
    public void clearChacheForOwner(final String ownerId) {
        final ReadWriteLock userViewLock = getUserViewLock(ownerId);
        ReadWriteLockTemplate.doWithLock(userViewLock, new ReadWriteCallback<UserView>() {
            @Override
            public ReadResult<UserView> doInReadLock() {
                // continue with write lock
                return new ReadResult<UserView>(true);
            }

            @Override
            public UserView doInWriteLock(ReadResult<UserView> readResult) {
                List<?> keys = userViews.getKeys();
                for (Object key : keys) {
                    Tuple<?, ?> tuple = (Tuple<?, ?>) key;
                    if (ownerId.equals(tuple.first)) {
                        userViews.remove(key);
                    }
                }
                return null;
            }
        });
    }
}
