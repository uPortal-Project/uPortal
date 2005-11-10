/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.layout.dlm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.layout.simple.RDBMUserLayoutStore;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.channels.error.ErrorCode;
import org.jasig.portal.layout.LayoutStructure;
import org.jasig.portal.layout.StructureParameter;
import org.jasig.portal.rdbm.DatabaseMetaDataImpl;
import org.jasig.portal.rdbm.IDatabaseMetadata;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.SmartCache;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

/**
 * This class extends RDBMUserLayoutStore and implements instantiating and
 * storing layouts that conform to the design of the distribute layout
 * management system. These layouts consist of two types: layout fragments
 * that are the layouts owned by a user specified in dlm.xml, and composite
 * view layouts which represent regular users with zero or more UI elements
 * incorporated from layout fragments. Only a user's personal layout fragment
 * is
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5 
 */
public class RDBMDistributedLayoutStore
    extends RDBMUserLayoutStore
{
    public static final String RCS_ID = "@(#) $Header$";
    private static final Log LOG = LogFactory.getLog(RDBMDistributedLayoutStore.class);

    private String systemDefaultUser = null;
    private boolean systemDefaultUserLoaded = false;
    private Properties properties = null;
    private FragmentDefinition[] definitions = null;
    private Map fragmentInfoCache = null;
    private LayoutDecorator decorator = null;
    private FragmentActivator activator = null;
    private Object initializationLock = new Object();
    private boolean initialized = false;
    static final String TEMPLATE_USER_NAME
        = "org.jasig.portal.services.Authentication.defaultTemplateUserName";
    static final String DECORATOR_PROPERTY = "layoutDecorator";

    private static final int THEME = 0;
    private static final int STRUCT = 1;

    // Cache for theme stylesheet descriptors
    private static SmartCache tsdCache;
    // Cache for structure stylesheet descriptors
    private static SmartCache ssdCache;

    /** Map of read/writer lock objects; one per unique person. */
    private Map mLocks = new ConcurrentHashMap();

    /**
     * Method for acquiring copies of fragment layouts to assist in debugging.
     * No infrastructure code calls this but channels designed to expose the 
     * structure of the cached fragments use this to obtain copies.
     * @return
     */
    public Map getFragmentLayoutCopies()
    throws Exception
    {
        if ( ! initialized )
        {
            synchronized( initializationLock )
            {
                if ( ! initialized )
                {
                    initializationLock.wait();
                }
            }
        }
        Map layouts = new HashMap();
        
        for(int i=0; definitions != null && i<definitions.length; i++)
        {
            Document layout = DocumentFactory.getNewDocument();
            Node copy = layout.importNode(definitions[i].view.layout
                    .getDocumentElement(), true);
            layout.appendChild(copy);
            layouts.put(definitions[i].ownerID, layout);
        }
        return layouts;
    }
    private final ReadWriteLock getReadWriteLock(IPerson person)
    {
        Object key = new Integer(person.getID());

        ReadWriteLock lock = (ReadWriteLock) mLocks.get(key);

        if (null == lock)
        {
            lock = new ReentrantWriterPreferenceReadWriteLock();

            mLocks.put(key, lock);
        }

        return lock;
    }

    private void acquireReadLock(IPerson person) throws InterruptedException
    {
        getReadWriteLock(person).readLock().acquire();
    }

    private void releaseReadLock(IPerson person)
    {
        getReadWriteLock(person).readLock().release();
    }

    public RDBMDistributedLayoutStore ( )
        throws Exception
    {
        super();
        tsdCache = new SmartCache();
        ssdCache = new SmartCache();

        ConfigurationLoader.load( this );
        
        try
        {
            
            String decoratorClass = null;
            if ( properties != null )
                decoratorClass = properties.getProperty( DECORATOR_PROPERTY );

            if ( decoratorClass != null )
                decorator = DecoratorLoader.load( decoratorClass );
        }
        catch( Exception e ) 
        {
            LOG.error("\n\n---------- Warning ---------\nUnable to load "
                        + "layout decorator '"
                        + properties.getProperty(DECORATOR_PROPERTY)
                        + "' specified in dlm.xml. It will not be used.", e);
        }
        
        // activate fragments in a separate thread because many parts of the
        // system including activation rely on UserLayoutFactory to instantiate
        // and then hand out to callers a single instance of this class and if
        // this constructor has not returned then the variable which holds this
        // instance is null. Most calls coming in are targeted at other methods
        // than those provided in this class. Most are provided by the super
        // class and hence reentrance into an instance still being activated is
        // ok.

        activator = new FragmentActivator( this, definitions );
        Thread t = new Thread()
            {
                public void run()
                {
                    try
                    {
                        activator.activateFragments();
                    }
                    catch( Exception e )
                    {
                        LOG.error("Problem loading fragments.", e);
                    }
                }
            };
        t.setName("DLM Fragment Activator");
        t.start();

        // start fragment cleaning thread
        initializeFragmentCleaner();        
    }

    /**
     * Registers a NEW structure stylesheet with the database. This overloads
     * the version in the parent to add caching of stylesheets.
     * @param tsd Stylesheet description object
     */
    public Integer addStructureStylesheetDescription(StructureStylesheetDescription ssd)
        throws Exception
    {
        Integer id = super.addStructureStylesheetDescription(ssd);
        ssdCache.put(new Integer(id.intValue()), ssd);
        return id;                // Put into TSD cache
    }   
    
    /**
     * Registers a NEW theme stylesheet with the database. This overloads
     * the version in the parent to add caching of stylesheets.
     * @param tsd Stylesheet description object
     */
    public Integer addThemeStylesheetDescription(ThemeStylesheetDescription tsd)
        throws Exception
    {
        Integer id = super.addThemeStylesheetDescription(tsd);
        tsdCache.put(new Integer(id.intValue()), tsd);
        return id;
    }

    /**
     * Obtain structure stylesheet description object for a given structure
     * stylesheet id. Overloads parent version to add caching of stylesheets.
     * 
     * @para id id of the structure stylesheet
     * @return structure stylesheet description
     */
    public StructureStylesheetDescription getStructureStylesheetDescription(
            int stylesheetId) throws Exception
    {
        // See if it's in the cache
        StructureStylesheetDescription ssd = null;
        ssd = (StructureStylesheetDescription) ssdCache.get(new Integer(
                stylesheetId));
        
        if (ssd != null)
            return ssd;
        ssd = super.getStructureStylesheetDescription(stylesheetId);

        // Put this value in the cache
        ssdCache.put(new Integer(stylesheetId), ssd);
        return ssd;
    }    

    /**
     * Obtain theme stylesheet description object for a given theme stylesheet
     * id. Overloads a parent version to add caching.
     * @para id id of the theme stylesheet
     * @return theme stylesheet description
     */
    public ThemeStylesheetDescription getThemeStylesheetDescription(int stylesheetId)
        throws Exception
    {
        ThemeStylesheetDescription tsd = null;

        // Get it from the cache if it's there
        tsd =
            (ThemeStylesheetDescription) tsdCache.get(
                new Integer(stylesheetId));
        if (tsd != null)
        {
            return tsd;
        }
        tsd = super.getThemeStylesheetDescription(stylesheetId);

        // Put it in the cache.    
        tsdCache.put(new Integer(stylesheetId), tsd);
        return tsd;
    }

    /**
     * Removes a structure stylesheet description object for a given structure 
     * stylesheet id. Overloads a parent version for cache handling.
     * @para id id of the structure stylesheet
     */
    public void removeStructureStylesheetDescription(int stylesheetId)
            throws Exception
    {
        super.removeStructureStylesheetDescription(stylesheetId);

        // Remove it from the cache
        ssdCache.remove(new Integer(stylesheetId));
    }

    /**
     * Removes a theme stylesheet description object for a given theme 
     * stylesheet id. Overloads a parent version for cache handling.
     * @para id id of the theme stylesheet
     */
    public void removeThemeStylesheetDescription(int stylesheetId)
            throws Exception
    {
        super.removeThemeStylesheetDescription(stylesheetId);
        // Remove it from the cache
        tsdCache.remove(new Integer(stylesheetId));
    }    

    /**
     * Updates an existing structure stylesheet description with a new one. Old
     * stylesheet description is found based on the Id provided in the parameter
     * structure. Overloads version in parent to add cache support.
     * 
     * @param ssd
     *            new stylesheet description
     */
    public void updateStructureStylesheetDescription(StructureStylesheetDescription ssd)
        throws Exception
    {
            super.updateStructureStylesheetDescription(ssd);
            
            // Update the cached value
            ssdCache.put(new Integer(ssd.getId()), ssd);
    }

    /**
     * Updates an existing structure stylesheet description with a new one. Old
     * stylesheet description is found based on the Id provided in the parameter
     * structure. Overloads version in parent to add cache support.
     * 
     * @param ssd
     *            new stylesheet description
     */
    public void updateThemeStylesheetDescription(ThemeStylesheetDescription tsd)
        throws Exception
    {
        super.updateThemeStylesheetDescription(tsd);

        // Set the new one in the cache
        tsdCache.put(new Integer(tsd.getId()), tsd);
    }
    
    /**
     * Starts a Thread that is responsible for cleaning out the layout fragments
     * periodically. This is done so that changes made to the channels within a
     * layout are visible to the users who have that layout incorporated into
     * their own.
     * 
     * The interval at which this thread runs is set in the dlm.xml file as
     * 'org.jasig.portal.layout.dlm.RDBMDistributedLayoutStore.fragment_cache_refresh',
     * specified in minutes.
     */
    private void initializeFragmentCleaner()
    {
        Thread t2 = new Thread()
            {
                public void run()
                {
                    Hashtable owners = new Hashtable();
                    long wait_time;
                        
                    try
                    {
                        wait_time = (Integer.parseInt(getProperty(
                            "org.jasig.portal.layout.dlm.RDBMDistributedLayoutStore.fragment_cache_refresh" ))*60) * 1000;
                    }
                    catch( Exception e )
                    { 
                        wait_time = 60 * (1000 * 60); // default to one hour
                    }
                            
                    while( true )
                    {
                        try
                        {
                            if ( ! initialized )
                            {
                                synchronized( initializationLock )
                                {
                                    if ( ! initialized )
                                    {
                                        initializationLock.wait();
                                    }
                                }
                            }
                            
                            // sleep for specified period of time
                            sleep( wait_time );
                            
                            //get each layout owner
                            if ( null != definitions )
                            {
                                if ( null != owners && owners.size() == 0 )
                                {
                                    for( int i=0; i<definitions.length; i++ )
                                    {
                                        String ownerId = definitions[i].ownerID;
                                        int userId  = definitions[i].userID;
                                        
                                        if ( null != ownerId )
                                        {
                                            IPerson p = new PersonImpl();
                                            p.setID( userId );
                                            p.setAttribute( "username", ownerId );
                                            owners.put(p, definitions[i]);
                                        }
                                    }
                                }

                                // cycle through each layout owner and clear out their
                                // respective layouts so users fragments will be cleared
                                for ( Enumeration e = owners.keys(); e.hasMoreElements(); )
                                {
                                    IPerson p = (IPerson) e.nextElement();
                                    UserProfile profile = getUserProfileById(p, 1);
                                    // fix hard coded 1 later for profiling
                                    profile.setProfileId(1);
                                    Document layout = getFragmentLayout(p,profile);
                                    FragmentDefinition fragment = (FragmentDefinition) owners.get(p);
                                    updateCachedLayout( layout, profile, fragment );
                                }
                            }
                            fragmentInfoCache = new HashMap();
                        }
                        catch( Exception e )
                        {
                            LOG.error(" *** Error - DLM Fragment cleaner problem:  \n\n", e );
                        }                            
                    }
                }
            };
        t2.setName("DLM Fragment Updater");
        t2.setDaemon(true);
        t2.start();
    }
    
    /**
       Returns a double value indicating the precedence value declared for a
       fragment in the dlm.xml. Precedence is actually based on two elements in
       a fragment definition: the precedence and the index of the fragment
       definition in the dlm.xml file. If two fragments are given equal
       precedence then the index if relied upon to resolve conflicts with UI
       elements.
     */
    public double getFragmentPrecedence( int index )
    {
        if ( index < 0 ||
             index > definitions.length-1 )
            return 0;

        // must pass through the array looking for the fragment with this
        // index since the array was sorted by precedence and then index
        // within precedence.
        for ( int i=0; i<definitions.length; i++ )
            if ( definitions[i].index == index )
                return definitions[i].precedence;
        return 0; // should never get here.
    }

    /**
       Returns the layout for a user decorated with any specified decorator.
       The layout returned is a composite layout for non fragment owners
       and a regular layout for layout owners. A composite layout is made up
       of layout pieces from potentially multiple incorporated layouts. If
       no layouts are defined then the composite layout will be the same as
       the user's personal layout fragment or PLF, the one holding only those
       UI elements that they own or incorporated elements that they have been
       allowed to changed.
     */
    public Document getUserLayout (IPerson person,
                                   UserProfile profile)
        throws Exception
    {
        if ( ! initialized )
        {
            synchronized( initializationLock )
            {
                if ( ! initialized )
                {
                    initializationLock.wait();
                }
            }
        }
        
        Document layout = _getUserLayout( person, profile );
        
        if ( decorator != null )
            decorator.decorate( layout, person, profile );

        return layout;
    }

    /**
     * Handles locking and identifying proper root and namespaces that used to
     * take place in super class.
     * 
     * @param person
     * @param profile
     * @return
     * @throws Exception
     */
    private Document _safeGetUserLayout(IPerson person, UserProfile profile)
            throws Exception
    {
        Document layoutDoc = null;
        // acquireWriteLock a reader lock for loading raw layout from db.
        acquireReadLock(person);

        try
        {
            layoutDoc = super.getUserLayout(person, profile);
        } finally
        {
            // release the read lock
            releaseReadLock(person);
        }
        Element layout = layoutDoc.getDocumentElement();
        layout.setAttribute(Constants.NS_DECL, Constants.NS_URI);
        return layoutDoc;
    }
    
    /**
     * Returns the layout for a user. This method overrides the same
     * method in the superclass to return a composite layout for non
     * fragment owners and a regular layout for layout owners. A
     * composite layout is made up of layout pieces from potentially
     * multiple incorporated layouts. If no layouts are defined then
     * the composite layout will be the same as the user's personal
     * layout fragment or PLF, the one holding only those UI elements
     * that they own or incorporated elements that they have been
     * allowed to changed.
     **/
    private Document _getUserLayout (IPerson person,
                                     UserProfile profile)
        throws Exception
    {
        String userName = (String) person.getAttribute( "username" );
        FragmentDefinition ownedFragment = getOwnedFragment( person );
        boolean isLayoutOwnerDefault = isLayoutOwnerDefault( person );
        
        // if this user is an owner then ownedFragment will be non null. For
        // fragment owners and owners of any default layout from which a
        // fragment owners layout is copied there should not be any imported
        // distributed layouts. Instead, load their plf, mark as an owned
        // if a fragment owner, and return.

        if ( ownedFragment != null || isLayoutOwnerDefault )
        {
            Document PLF, ILF = null;
            PLF = _safeGetUserLayout(person, profile);
            ILF = XML.cloneDocument( PLF );

            Element layoutNode = ILF.getDocumentElement();

            if ( ownedFragment != null )
            {
                layoutNode.setAttributeNS( Constants.NS_URI,
                                           Constants.ATT_FRAGMENT_NAME,
                                           ownedFragment.name );
                if (LOG.isDebugEnabled())
                    LOG.debug("User '" + userName + "' is owner of '"
                            + ownedFragment.name + "' fragment.");
            }
            else if ( isLayoutOwnerDefault )
            {
                layoutNode.setAttributeNS( Constants.NS_URI,
                                           Constants.ATT_IS_TEMPLATE_USER,
                                           "true" );
                layoutNode.setAttributeNS( Constants.NS_URI,
                                           Constants.ATT_TEMPLATE_LOGIN_ID,
                                           (String) person.getAttribute( "username" ) );
            }
            // cache in person as PLF for storage later like normal users
            person.setAttribute( Constants.PLF, PLF );
            return ILF;
        }
        
        return getCompositeLayout( person, profile );
    }

    /**
     * Convenience method for fragment activator to obtain raw layouts for
     * fragments during initialization.
     */
    Document getFragmentLayout (IPerson person,
                                UserProfile profile)
        throws Exception
    {
        return _safeGetUserLayout( person, profile );
    }

    /**
     * Called by fragment activation after loading of all fragment layouts is
     * complete to allow other threads requesting layouts via getUserLayout
     * to continue.
     */
    void activationFinished()
    {
        synchronized( initializationLock )
        {
            initialized = true;
            initializationLock.notifyAll();
        }
    }
    
    /** 
     * Generates a new struct id for directive elements that dlm places in
     * the PLF version of the layout tree. These elements are atifacts of the
     * dlm storage model and used during merge but do not appear in the user's
     * composite view.
     */
    public String getNextStructDirectiveId (IPerson person) throws Exception {
        return  super.getNextStructId(person, Constants.DIRECTIVE_PREFIX );
    }
    
    /**
       Replaces the layout Document stored on a fragment definition with a new
       version. This is called when a fragment owner updates their layout.
     */
    private void updateCachedLayout( Document layout,
                                     UserProfile profile,
                                     FragmentDefinition fragment )
    {
        // need to make a copy that we can fragmentize
        layout = XML.cloneDocument(layout);

        // Fix later to handle multiple profiles
        Element root = layout.getDocumentElement();
        root.setAttribute( Constants.ATT_ID,
                           Constants.FRAGMENT_ID_USER_PREFIX + 
                           fragment.userID + 
                           Constants.FRAGMENT_ID_LAYOUT_PREFIX + "1" );
        UserView view = new UserView( profile,
                                      layout,
                                      fragment.view.structUserPrefs,
                                      fragment.view.themeUserPrefs );
        try
        {
            activator.fragmentizeLayout( view, fragment );
            fragment.view = view;
            this.fragmentInfoCache = new HashMap();
        }
        catch( Exception e )
        {
            LOG.error("An exception occurred attempting to update a layout.", e);
        }
    }

    /**
       Returns true is the user is the owner of a layout which is copied as the
       default for any fragment when first created.
    */
    private boolean isLayoutOwnerDefault( IPerson person )
    {
        String userName = (String) person.getAttribute( "username" );
        
        if ( userName != null && definitions != null )
        {
            for( int i=0; i<definitions.length; i++ )
                if ( definitions[i].defaultLayoutOwnerID != null &&
                     definitions[i].defaultLayoutOwnerID.equals( userName ) )
                    return true;
        }
        String globalDefault =  getProperty( "defaultLayoutOwner" );
        if ( globalDefault != null &&
             globalDefault.equals( userName ) )
            return true;
        
        if (!systemDefaultUserLoaded)
        {
            systemDefaultUserLoaded = true;
        try
        {
                systemDefaultUser = PropertiesManager
                        .getProperty(TEMPLATE_USER_NAME);
            } catch (RuntimeException re)
            {
                LOG.error("Property '" + TEMPLATE_USER_NAME + "' not defined.",
                        re);
            }
            if (systemDefaultUser != null && systemDefaultUser.equals(userName))
                return true;
        }
        
        return false;
    }

    /**
       Returns the fragment owned by this user if any. If this user is not a
       fragment owner then null is returned.
    */
    private FragmentDefinition getOwnedFragment( IPerson person )
    {
        int userId = person.getID();
        
        if ( definitions != null )
        {
            for( int i=0; i<definitions.length; i++ )
                if ( definitions[i].userID == userId )
                    return definitions[i];
        }
        return null;
    }

    /**
    This method passed through the set of ordered fragments asking each one if
    it is applicable to this user. If so then it is included in a list of
    applicable layout fragments. These are then combined into an ILF,
    incorporated layouts fragment, and finally the user's PLF, personal layout
    fragment, is merged in and the composite layout returned.
    */
    private Document getCompositeLayout( IPerson person,
                                         UserProfile profile )
        throws Exception
    {
        Vector applicables = new Vector();
        
        if ( definitions != null )
        {
            for( int i=0; i<definitions.length; i++ )
                if ( definitions[i].isApplicable(person) )
                {
                    applicables.add( definitions[i].view.layout );
                }
        }

        Document PLF = (Document) person.getAttribute( Constants.PLF );

        if ( null == PLF )
        {
            PLF = _safeGetUserLayout( person, profile );
        }
        if (LOG.isDebugEnabled())
            LOG.debug("PLF for " + person.getAttribute(IPerson.USERNAME) +
                    " immediately after loading\n" + XML.serializeNode(PLF));
        
        Document ILF = ILFBuilder.constructILF( PLF, applicables, person );
        person.setAttribute( Constants.PLF, PLF );
        IntegrationResult result = new IntegrationResult();
        PLFIntegrator.mergePLFintoILF( PLF, ILF, result );
        if (LOG.isDebugEnabled())
        {
            LOG.debug("PLF for " + person.getAttribute(IPerson.USERNAME) +
                    " after MERGING\n" + XML.serializeNode(PLF));
            LOG.debug("ILF for " + person.getAttribute(IPerson.USERNAME) +
                    " after MERGING\n" + XML.serializeNode(ILF));
        }
        // push optimizations made during merge back into db.
        if( result.changedPLF )
        {
            if (LOG.isDebugEnabled())
                LOG.debug("Saving PLF for " + 
                    person.getAttribute(IPerson.USERNAME) +
                    " due to changes during merge.");
            super.setUserLayout( person, profile, PLF, false );
        }

        return ILF;
    }

    /**
       This method overrides the same method in the super class to persist
       only layout information stored in the user's person layout fragment
       or PLF. If this person is a layout owner then their changes are pushed
       into the appropriate layout fragment.
     */
    public void setUserLayout (IPerson person, UserProfile profile,
                               Document layoutXML, boolean channelsAdded)
      throws Exception
    {
        setUserLayout(person, profile, layoutXML, channelsAdded, true);
    }

    /**
       This method overrides the same method in the super class to persist
       only layout information stored in the user's person layout fragment
       or PLF. If fragment cache update is requested then it checks to see if
       this person is a layout owner and if so then their changes are pushed
       into the appropriate layout fragment.
     */
    void setUserLayout (IPerson person, UserProfile profile,
                        Document layoutXML, boolean channelsAdded,
                        boolean updateFragmentCache)
      throws Exception
    {
        Document plf = (Document) person.getAttribute( Constants.PLF );
        if (LOG.isDebugEnabled())
            LOG.debug("PLF for " + person.getAttribute(IPerson.USERNAME) +
                    "\n" + XML.serializeNode(plf));
        super.setUserLayout( person, profile, plf, channelsAdded );

        if (updateFragmentCache)
        {
            FragmentDefinition fragment = getOwnedFragment(person);

            if (fragment != null)
                updateCachedLayout(plf, profile, fragment);
        }
    }
    
    /**
       Returns the number of properties loaded from the dlm.xml file.
     */
    public int getPropertyCount()
    {
        return properties.size();
    }

    /**
       Returns an enumerator of the property names loaded from dlm.xml.
     */
    public Enumeration getPropertyNames()
    {
        if ( properties == null )
        {
            return new Enumeration()
                {
                    public boolean hasMoreElements()
                    {
                        return false;
                    }
                    public Object nextElement()
                    {
                        throw new NoSuchElementException();
                    }
                };
        }
        return properties.propertyNames();
    }

    /**
       Returns the specified property loaded from dlm.xml or null if not found.
     */
    public String getProperty( String name ) 
    {
        if ( properties == null )
            return null;
        return properties.getProperty( name );
    }

    /**
       Sets the dlm propertys. Called by ConfigurationLoaded.
     */
    void setProperties( Properties props )
    {
        this.properties = props;
    }

    /**
       Sets the dlm fragment definitions. Called by ConfigurationLoader.
     */
    void setDefinitions( FragmentDefinition[] frags )
    {
        this.definitions = frags;
        this.fragmentInfoCache = new HashMap();
    }
    
    /**
     * Returns an object suitable for identifying channel attribute and
     * parameter values in a user's layout that differ from the values on the
     * same element in a fragment. This is used by the layout manager to know
     * which ones must be persisted.
     * 
     * @param sId
     * @return FragmentChannelInfo if available or null if not found.
     */
    FragmentChannelInfo getFragmentChannelInfo(String sId)
    {
        FragmentNodeInfo node = getFragmentNodeInfo(sId);

        if (node != null && (node instanceof FragmentChannelInfo))
            return (FragmentChannelInfo) node;
        return null;
    }
    /**
     * Returns an object suitable for identifying attribute values for folder
     * nodes and attribute and parameter values for channel nodes in a user's
     * layout that differ from the values on the same element in a fragment.
     * This is used by the layout manager to know which ones must be persisted.
     * 
     * @param sId
     * @return FragmentNodeInfo or null if folder not found.
     */
    FragmentNodeInfo getFragmentNodeInfo(String sId)
    {
        // grab local pointers to variables subject to change at any time
        Map infoCache = fragmentInfoCache;
        FragmentDefinition[] defs = definitions;
        
        FragmentNodeInfo info = (FragmentNodeInfo) infoCache.get(sId);
        
        if (info == null)
        {
            for(int i=0; i<defs.length; i++)
            {
                Element node = defs[i].view.layout.getElementById(sId);
                if (node != null) // found it
                {
                    if (node.getTagName().equals(Constants.ELM_CHANNEL))
                        
                        info = new FragmentChannelInfo(node);
                    else
                        info = new FragmentNodeInfo(node);
                    infoCache.put(sId, info);
                    break;
                }
            }
        }
        return info;
    }

    /**
     * Gets the configured dlm fragment definitions.
     */
    FragmentDefinition[] getDefinitions()
    {
        return this.definitions;
    }


    //////// User Preferences handling methods. //////////
    
    DistributedUserPreferences getDistributedSSUP( IPerson person,
                                                   int profileId,
                                                   int stylesheetId )
      throws Exception
    {
        return new DistributedUserPreferences
        ( /*super.*/_getStructureStylesheetUserPreferences( person,
                                                           profileId,
                                                           stylesheetId ) );
    }
    
    DistributedUserPreferences getDistributedTSUP( IPerson person,
                                                   int profileId,
                                                   int stylesheetId )
        throws Exception
    {
        return new DistributedUserPreferences
            ( super.getThemeStylesheetUserPreferences( person,
                                                       profileId,
                                                       stylesheetId ) );
    }
    
    public StructureStylesheetUserPreferences _getStructureStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) throws Exception {
        int userId = person.getID();
        StructureStylesheetUserPreferences ssup;
        Connection con = RDBMServices.getConnection();
        try {
            Statement stmt = con.createStatement();
            try {
                // get stylesheet description
                StructureStylesheetDescription ssd = getStructureStylesheetDescription(stylesheetId);

                // now look to see if this user has a layout or not. This is
                // important because preference values are stored by layout
                // element and if the user doesn't have a layout yet then the
                // default user's preferences need to be loaded.
                
                String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" +
                profileId;
                if (LOG.isDebugEnabled())
                    LOG.debug("RDBMUserLayoutStore::getUserLayout()1 " + subSelectString);
                int layoutId;
                ResultSet rs = stmt.executeQuery(subSelectString);
                try {
                    rs.next();
                    layoutId = rs.getInt(1);
                    if (rs.wasNull()) {
                        layoutId = 0;
                    }
                } finally {
                    rs.close();
                    stmt.close();
                }

                // if no layout then get the default user id for this user
                
                int origId = userId;
                if (layoutId == 0) { 
                    stmt = con.createStatement();
                    String sQuery = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=" + userId;
                    if (LOG.isDebugEnabled())
                        LOG.debug("RDBMUserLayoutStore::getUserLayout(): " + sQuery);
                    rs = stmt.executeQuery(sQuery);
                    try {
                        rs.next();
                        userId = rs.getInt(1);
                    } finally {
                        rs.close();
                        stmt.close();
                    }
                }

                // create the stylesheet user prefs object then fill
                // fill it with defaults from the stylesheet definition object
                
                ssup = new StructureStylesheetUserPreferences();
                ssup.setStylesheetId(stylesheetId);

                // fill stylesheet description with defaults
                for (Enumeration e = ssd.getStylesheetParameterNames(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    ssup.putParameterValue(pName, ssd.getStylesheetParameterDefaultValue(pName));
                }
                for (Enumeration e = ssd.getChannelAttributeNames(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    ssup.addChannelAttribute(pName, ssd.getChannelAttributeDefaultValue(pName));
                }
                for (Enumeration e = ssd.getFolderAttributeNames(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    ssup.addFolderAttribute(pName, ssd.getFolderAttributeDefaultValue(pName));
                }

                // Now load in the stylesheet parameter preferences
                // from the up_ss_user_param but only if they are defined
                // parameters in the stylesheet's .sdf file. 
                //
                // First, get the parameters for the effective user ID,
                // then for the original user ID.  These will differ if
                // the user has no layout in the database and is using
                // the default user layout.  The params from the original
                // user ID take precedence.

                String pstmtQuery =
                    "SELECT PARAM_NAME, PARAM_VAL " +
                    "FROM UP_SS_USER_PARM " +
                    "WHERE USER_ID=?" +
                    " AND PROFILE_ID=" + profileId + 
                    " AND SS_ID=" + stylesheetId + 
                    " AND SS_TYPE=1";

                PreparedStatement pstmt = con.prepareStatement(pstmtQuery);

                try {
                    pstmt.setInt(1, userId);
                    rs = pstmt.executeQuery();
                    
                    while (rs.next()) {
                        String pName = rs.getString(1);
                        if (ssd.containsParameterName(pName))
                            ssup.putParameterValue(pName, rs.getString(2));
                    }

                    if (userId != origId) {
                        pstmt.setInt(1, origId);
                        rs = pstmt.executeQuery();
                            
                        while (rs.next()) {
                            String pName = rs.getString(1);
                            if (ssd.containsParameterName(pName))
                                ssup.putParameterValue(pName, rs.getString(2));
                        }
                    }
                }
                finally {
                    rs.close();
                    pstmt.close();
                }

                // now load in the folder and channel attributes from the
                // up_ss_user_atts table pulling in dlm:origin from the
                // up_layout_param table indicating these values are for an
                // overriden value on an incorporated element.

                /***** replaced by Anthony for supporting differing outerjoin
                       syntax for multiple databases. See below.
                       
                sQuery = "SELECT PARAM_NAME, PARAM_VAL, PARAM_TYPE, ULS.STRUCT_ID, CHAN_ID, ULP.STRUCT_PARM_NM, ULP.STRUCT_PARM_VAL FROM UP_SS_USER_ATTS UUSA, UP_LAYOUT_STRUCT ULS, UP_LAYOUT_PARAM ULP WHERE UUSA.USER_ID=" + userId + " AND PROFILE_ID="
                + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND UUSA.STRUCT_ID = ULS.STRUCT_ID AND UUSA.USER_ID = ULS.USER_ID AND UUSA.STRUCT_ID = ULP.STRUCT_ID(+) AND UUSA.USER_ID = ULP.USER_ID(+)";
                *****/

                String sQuery = null;
                
                IDatabaseMetadata db = RDBMServices.getDbMetaData();
                if (db.supportsOuterJoins()) 
                {
                    if (db.getJoinQuery() 
                            instanceof DatabaseMetaDataImpl.JdbcDb) 
                    {
                        if (LOG.isDebugEnabled())
                            LOG.debug("RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  instanceof jdbcdb");      
                        sQuery = "SELECT PARAM_NAME, PARAM_VAL, PARAM_TYPE, ULS.STRUCT_ID, CHAN_ID, ULP.STRUCT_PARM_NM, ULP.STRUCT_PARM_VAL FROM UP_LAYOUT_STRUCT ULS, UP_SS_USER_ATTS UUSA LEFT OUTER JOIN UP_LAYOUT_PARAM ULP ON UUSA.STRUCT_ID = ULP.STRUCT_ID AND UUSA.USER_ID=" + userId + " AND UUSA.USER_ID = ULP.USER_ID AND PROFILE_ID=" + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND UUSA.STRUCT_ID = ULS.STRUCT_ID AND UUSA.USER_ID = ULS.USER_ID AND UUSA.USER_ID = ULP.USER_ID";
                    } 
                    else if (db.getJoinQuery() 
                            instanceof DatabaseMetaDataImpl.PostgreSQLDb) 
                    {
                        if (LOG.isDebugEnabled())
                            LOG.debug("RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  instanceof jpostgressqldbdbcdb");      

                        sQuery = "SELECT PARAM_NAME, PARAM_VAL, PARAM_TYPE," +
                                " ULS.STRUCT_ID, CHAN_ID, ULP.STRUCT_PARM_NM," +
                                " ULP.STRUCT_PARM_VAL " +
                                "FROM UP_LAYOUT_STRUCT ULS, " +
                                " UP_SS_USER_ATTS UUSA LEFT OUTER JOIN" +
                                " UP_LAYOUT_PARAM ULP " +
                                "ON UUSA.STRUCT_ID = ULP.STRUCT_ID" +
                                " AND UUSA.USER_ID = ULP.USER_ID " +
                                "WHERE UUSA.USER_ID=" + userId + 
                                " AND PROFILE_ID=" + profileId + 
                                " AND SS_ID=" + stylesheetId + 
                                " AND SS_TYPE=1" +
                                " AND UUSA.STRUCT_ID = ULS.STRUCT_ID" +
                                " AND UUSA.USER_ID = ULS.USER_ID";
                    } 
                    else if (db.getJoinQuery() 
                            instanceof DatabaseMetaDataImpl.OracleDb) 
                    {
                        if (LOG.isDebugEnabled())
                            LOG.debug("RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  instanceof oracledb");      
                        sQuery = "SELECT /*+ USE_NL(UP_LAYOUT_STRUCT) */ PARAM_NAME, PARAM_VAL, PARAM_TYPE, ULS.STRUCT_ID, CHAN_ID, ULP.STRUCT_PARM_NM, ULP.STRUCT_PARM_VAL FROM UP_SS_USER_ATTS UUSA, UP_LAYOUT_STRUCT ULS, UP_LAYOUT_PARAM ULP WHERE UUSA.USER_ID=" + userId + " AND PROFILE_ID="
                            + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND UUSA.STRUCT_ID = ULS.STRUCT_ID AND UUSA.USER_ID = ULS.USER_ID AND UUSA.STRUCT_ID = ULP.STRUCT_ID(+) AND UUSA.USER_ID = ULP.USER_ID(+)";



                    } else 
                    {
                        throw new Exception("Unknown database driver");
                    }
                }

                if (LOG.isDebugEnabled())
                    LOG.debug("RDBMUserLayoutStore::getStructureStylesheetUserPreferences(): " + sQuery);

                stmt = con.createStatement();
                rs = stmt.executeQuery(sQuery);
                try {
                    while (rs.next()) {
                        int param_type = rs.getInt(3);
                        int structId = rs.getInt(4);
                        if (rs.wasNull()) {
                            structId = 0;
                        }
                        String ulp_parmName = rs.getString(6);
                        String originId = rs.getString(7);
                        String pName = rs.getString(1);
                        int chanId = rs.getInt(5);
                        if (rs.wasNull()) {
                            chanId = 0;
                        }
                        if (param_type == 1) {
                            // stylesheet param
                            if (LOG.isDebugEnabled())
                                LOG.debug("RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  stylesheet global params should be specified in the user defaults table ! UP_SS_USER_ATTS is corrupt. (userId="
                                                      + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                                                      + ", param_name=\"" + pName + "\", param_type=" + Integer.toString(param_type));
                        }
                        else if (param_type == 2) {
                            // folder attribute
                            String folderStructId = null;
                            if ( ulp_parmName != null &&
                                 (ulp_parmName.equals( Constants.ATT_ORIGIN ) ||
                                  ulp_parmName.equals( Constants.LEGACY_ATT_ORIGIN )))
                                folderStructId = originId;
                            else
                                folderStructId = getStructId(structId,chanId);
                            if (ssd.containsFolderAttribute(pName))
                                ssup.setFolderAttributeValue(folderStructId, pName, rs.getString(2));
                        }       
                        else if (param_type == 3) {
                            // channel attribute
                            String channelStructId = null;
                            if ( ulp_parmName != null &&
                                 (ulp_parmName.equals( Constants.ATT_ORIGIN ) ||
                                  ulp_parmName.equals( Constants.LEGACY_ATT_ORIGIN )))
                                channelStructId = originId;
                            else
                                channelStructId = getStructId(structId,chanId);
                            if (ssd.containsChannelAttribute(pName))
                                ssup.setChannelAttributeValue(channelStructId, pName, rs.getString(2));
                        }
                        else {
                            // unknown param type
                                LOG.error("RDBMUserLayoutStore::getStructureStylesheetUserPreferences() : unknown param type encountered! DB corrupt. (userId="
                                            + Integer.toString(userId)
                                            + ", profileId="
                                            + Integer.toString(profileId)
                                            + ", stylesheetId="
                                            + Integer.toString(stylesheetId)
                                            + ", param_name=\""
                                            + rs.getString(1)
                                            + "\", param_type="
                                            + Integer.toString(param_type));
                        }
                    }
                } finally {
                    rs.close();
                    stmt.close();
                }
            } finally {
                //stmt.close();
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
        return  ssup;
    }

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences( IPerson person, int profileId, int stylesheetId)
        throws Exception
    {
        if ( ! initialized )
        {
            synchronized( initializationLock )
            {
                if ( ! initialized )
                {
                    initializationLock.wait();
                }
            }
        }
        
        DistributedUserPreferences ssup = getDistributedSSUP( person,
                                                              profileId,
                                                              stylesheetId );
        // if the user is a fragment owner or if they are a template user
        // from whom new users received a layout copy to own then don't
        // incorporate any incorporated user preferences
        FragmentDefinition ownedFragment = getOwnedFragment( person );
        boolean isLayoutOwnerDefault = isLayoutOwnerDefault( person );
        
        if ( ownedFragment != null || isLayoutOwnerDefault )
            return ssup;

        // regular user, find which layouts apply and include their set prefs
        
        if ( definitions != null )
        {
            for( int i=0; i<definitions.length; i++ )
                if ( definitions[i].isApplicable(person) )
                    loadIncorporatedPreferences
                    ( person, STRUCT, ssup,
                      definitions[i].view.structUserPrefs );
        }
        
        if (LOG.isDebugEnabled())
            LOG.debug("***** " + person.getAttribute( "username" )
              + "'s StructureStylesheetUserPrefereneces\n" +
              showFolderAttribs( ssup ) +
              showChannelAttribs( ssup ) );
        
        return ssup; 
    }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences( IPerson person, int profileId, int stylesheetId)
        throws Exception
    {
        if ( initialized )
        {
            synchronized( initializationLock )
            {
                if ( ! initialized )
                {
                    initializationLock.wait();
                }
            }
        }
        
        DistributedUserPreferences tsup = getDistributedTSUP( person,
                                                              profileId,
                                                              stylesheetId );
        // if the user is a fragment owner or if they are a template user
        // from whom new users received a layout copy to own then don't
        // incorporate any incorporated user preferences
        FragmentDefinition ownedFragment = getOwnedFragment( person );
        boolean isLayoutOwnerDefault = isLayoutOwnerDefault( person );
        
        if ( ownedFragment != null || isLayoutOwnerDefault )
            return tsup;

        // regular user, find which layouts apply and include their set prefs
        
        if ( definitions != null )
        {
            for( int i=0; i<definitions.length; i++ )
                if ( definitions[i].isApplicable(person) )
                    loadIncorporatedPreferences( person, THEME, tsup,
                                                 definitions[i].view.themeUserPrefs);
        }
        
        if (LOG.isDebugEnabled())
            LOG.debug("***** " + person.getAttribute( "username" )
              + "'s ThemeStylesheetUserPrefereneces\n" +
              showChannelAttribs( tsup ) );

        return tsup;
    }

    private void loadIncorporatedPreferences( IPerson person,
                                              int which,
                                              DistributedUserPreferences userPrefs,
                                              DistributedUserPreferences incdPrefs )
    {
        for ( Enumeration channels = incdPrefs.getChannels();
              channels.hasMoreElements(); )
        {
            String channel = (String) channels.nextElement();
            for ( Enumeration attribs = incdPrefs.getChannelAttributeNames();
                  attribs.hasMoreElements(); )
            {
                String attrib = (String) attribs.nextElement();
                String userValue = userPrefs
                    .getDefinedChannelAttributeValue( channel, attrib );
                String incdValue = incdPrefs
                    .getDefinedChannelAttributeValue( channel, attrib );
                if ( incdValue != null )
                    userPrefs.setIncorporatedChannelAttributeValue( channel,
                                                                    attrib,
                                                                    incdValue );
                // now see if user change is still pertinent or not
                if ( incdValue != null &&
                     userValue != null &&
                     incdValue.equals( userValue ) )
                {
                    userPrefs.removeDefinedChannelAttributeValue( channel,
                                                                  attrib );
                    EditManager.removePreferenceDirective( person,
                                                           channel, attrib );
                }
            }
        }

        // if theme stylesheet prefs don't do folders
        if ( which == THEME )
            return;
        
        for ( Enumeration folders = incdPrefs.getFolders();
              folders.hasMoreElements(); )
        {
            String folder = (String) folders.nextElement();
            for ( Enumeration attribs = incdPrefs.getFolderAttributeNames();
                  attribs.hasMoreElements(); )
            {
                String attrib = (String) attribs.nextElement();
                String userValue = userPrefs
                    .getDefinedFolderAttributeValue( folder, attrib );
                String incdValue = incdPrefs
                    .getDefinedFolderAttributeValue( folder, attrib );
                if ( incdValue != null )
                    userPrefs.setIncorporatedFolderAttributeValue( folder,
                                                                   attrib,
                                                                   incdValue );
                // now see if user change is still pertinent or not
                if ( incdValue != null &&
                     userValue != null &&
                     incdValue.equals( userValue ) )
                {
                    userPrefs.removeDefinedFolderAttributeValue( folder,
                                                                 attrib );
                    EditManager.removePreferenceDirective( person,
                                                           folder, attrib );
                }
            }
        }
    }
                                              
    private String showFolderAttribs( StructureStylesheetUserPreferences ssup )
    {
        StringWriter sw = new StringWriter ();
        PrintWriter pw = new PrintWriter( sw );

        pw.println( "\n*** Folder Attributes" ); 
        for ( Enumeration folders = ssup.getFolders();
              folders.hasMoreElements(); )
        {
            String folder = (String) folders.nextElement();
            for ( Enumeration attribs = ssup.getFolderAttributeNames();
                  attribs.hasMoreElements(); )
            {
                String attrib = (String) attribs.nextElement();
                String val = ssup.getFolderAttributeValue( folder, attrib );
                String defVal = ssup.getDefinedFolderAttributeValue( folder,
                                                                     attrib );
                pw.println( ( val != null ? "> " : "  " ) +
                            folder + "." + attrib + " = (" + defVal + ") "+
                            ( val != null ? val : "" ) );
            }
        }
        pw.close();
        return sw.toString();
    }

    private String showChannelAttribs( ThemeStylesheetUserPreferences tsup )
    {
        StringWriter sw = new StringWriter ();
        PrintWriter pw = new PrintWriter( sw );

        pw.println( "\n*** Channel Attributes" ); 
        for ( Enumeration channels = tsup.getChannels();
              channels.hasMoreElements(); )
        {
            String channel = (String) channels.nextElement();
            for ( Enumeration attribs = tsup.getChannelAttributeNames();
                  attribs.hasMoreElements(); )
            {
                String attrib = (String) attribs.nextElement();
                String val = tsup.getChannelAttributeValue( channel, attrib );
                String defVal = tsup.getDefinedChannelAttributeValue( channel,
                                                                      attrib );
                pw.println( ( val != null ? "> " : "  " ) +
                            channel + "." + attrib + " = (" + defVal + ") "+
                            ( val != null ? val : "" ) );
            }
        }
        pw.close();
        return sw.toString();
    }

    /**
       If the passed in user represents a layout owner then replace the
       cached structure stylesheet user preferences with the passed in one
       after modifying it for incorporation.
     */
    private void updateFragmentSSUP( IPerson person,
                                     DistributedUserPreferences ssup )
    {
        FragmentDefinition ownedFragment = getOwnedFragment( person );
        if ( ownedFragment == null )
            return;

        // make a copy so the original is unchanged for the user
        try
        {
            UserProfile profile = getUserProfileById(person, 1);
            ssup = new DistributedUserPreferences(
                    (StructureStylesheetUserPreferences) ssup);
            UserView view = new UserView(profile, ownedFragment.view.layout,
                    ssup, ownedFragment.view.themeUserPrefs);
            activator.fragmentizeSSUP(view, ownedFragment);
            ownedFragment.view = view;
        }
        catch( Exception e)
        {
            LOG.error(" *** Error - DLM unable to update fragment prefs:  \n\n", e );
        }
    }

    /**
       When user preferences are stored in the database for changes made to
       an incorporated node the node id can not be used because it does not
       represent a row in the up_layout_struct table for the user. The plfid
       must be used. Null will never be returned unless the layout or
       processing has really been screwed up. This is because changes made to
       the user prefs calls UserPrefsHandler which generates a shadow node in
       the db and sets the plfid of that node into the corresponding node in
       the PLF prior to the call to update the user prefs in the db.
     */
    private String getPlfId( Document PLF, String incdId )
    {
        Element element = PLF.getElementById( incdId );
        if ( element == null )
            return null;
        Attr attr = element.getAttributeNode( Constants.ATT_PLF_ID );
        if ( attr == null )
            return null;
        return attr.getValue();
    }
    
    protected Element getStructure(Document document, LayoutStructure ls) throws Exception {
        Document doc = document;
        Element structure = null;

        // handle migration of legacy namespace
        String type = ls.getType(); 
        if (type != null && type.startsWith(Constants.LEGACY_NS))
            type = Constants.NS + type.substring(Constants.LEGACY_NS.length());

  if (ls.isChannel()) {
    ChannelDefinition channelDef = crs.getChannelDefinition(ls.getChanId());
    if (channelDef != null && channelApproved(channelDef.getApprovalDate())) {
        if (localeAware) {
            channelDef.setLocale(ls.getLocale()); // for i18n by Shoji
            }
      structure = channelDef.getDocument(doc, channelPrefix + ls.getStructId());
    } else {
        // Create an error channel if channel is missing or not approved
        ChannelDefinition cd = new ChannelDefinition(ls.getChanId());
        cd.setTitle("Missing channel");
        cd.setName("Missing channel");
        cd.setTimeout(20000);
        String missingChannel = "Unknown";
        if (channelDef != null) {
            missingChannel = channelDef.getName();
        }
        structure = cd.getDocument(doc, channelPrefix + ls.getStructId(),
                "The '" + missingChannel + "' channel is no longer available. " +
                "Please remove it from your layout.",
                ErrorCode.CHANNEL_MISSING_EXCEPTION.getCode());
    }
  } else
        {
            // create folder objects including dlm new types in cp namespace
            if (type != null && (type.startsWith(Constants.NS)))
            {
                structure = doc.createElementNS(Constants.NS_URI, type);
            }
            else
                structure = doc.createElement("folder");
    structure.setAttribute(Constants.ATT_ID, folderPrefix + ls.getStructId());
    structure.setIdAttribute(Constants.ATT_ID, true);
    structure.setAttribute("name", ls.getName());
    structure.setAttribute("type", (type != null ? type : "regular"));
        }

        structure.setAttribute("hidden", (ls.isHidden() ? "true" : "false"));
        structure.setAttribute("immutable", (ls.isImmutable() ? "true" : "false"));
  structure.setAttribute("unremovable", (ls.isUnremovable() ? "true" : "false"));
  if (localeAware) {
      structure.setAttribute("locale", ls.getLocale());  // for i18n by Shoji
  }

  /*
   * Parameters from up_layout_param are loaded slightly differently for 
   * folders and channels. For folders all parameters are added as attributes
   * of the Element. For channels only those parameters with names starting
   * with the dlm namespace Constants.NS are added as attributes to the Element.
   * Others are added as child parameter Elements.
   */
  if (ls.getParameters() != null) {
    for (Iterator itr = ls.getParameters().iterator(); itr.hasNext();) {
      StructureParameter sp = (StructureParameter) itr.next();
      String pName = sp.getName();
      
      // handle migration of legacy namespace
      if (pName.startsWith(Constants.LEGACY_NS))
          pName = Constants.NS + sp.getName().substring(Constants.LEGACY_NS.length());
      
      if (!ls.isChannel())
      { // Folder
          if (pName.startsWith(Constants.NS))
              structure.setAttributeNS(Constants.NS_URI, pName, sp.getValue());
          else
              structure.setAttribute(pName, sp.getValue());
      } 
      else // Channel
      { 
          // if dealing with a dlm namespace param add as attribute
          if (pName.startsWith(Constants.NS))
          {
              structure.setAttributeNS(Constants.NS_URI, pName, sp.getValue());
              itr.remove();
          }
          else 
          {
              // do traditional override processing. some explanation is in
              // order. The structure element was created by the 
              // ChannelDefinition and only contains parameter children if the
              // definition had defined parameters. These are checked for each
              // layout loaded parameter as found in LayoutStructure.parameters.
              // If a name match is found then we need to see if overriding is
              // allowed and if so we set the value on the child parameter
              // element. At that point we are done with that version loaded 
              // from the layout so we remove it from the in-memory set of 
              // parameters that are being merged-in. Then, after all such have 
              // been checked against those added by the channel definition we
              // add in any remaining as adhoc, unregulated parameters.
              NodeList nodeListParameters =
                  structure.getElementsByTagName("parameter");
              for (int j = 0; j < nodeListParameters.getLength(); j++)
              {
                  Element parmElement = (Element)nodeListParameters.item(j);
                  NamedNodeMap nm = parmElement.getAttributes();

                  String nodeName = nm.getNamedItem("name").getNodeValue();
                  if (nodeName.equals(pName)) 
                  {
                      Node override = nm.getNamedItem("override");
                      if (override != null && override.getNodeValue().equals("yes")) 
                      {
                          Node valueNode = nm.getNamedItem("value");
                          valueNode.setNodeValue(sp.getValue());
                      }
                      itr.remove();
                      break; // found the corresponding one so skip the rest
                  }
              }
          }
      }
    }
    // For channels, add any remaining parameter elements loaded with the 
    // layout as adhoc, unregulated, parameter children that can be overridden.
    if (ls.isChannel())
    {
        for (Iterator itr = ls.getParameters().iterator(); itr.hasNext();) 
        {
            StructureParameter sp = (StructureParameter) itr.next();
            Element parameter = doc.createElement("parameter");
            parameter.setAttribute("name", sp.getName());
            parameter.setAttribute("value", sp.getValue());
            parameter.setAttribute("override", "yes");
            structure.appendChild(parameter);
        }
    }
  }
        // finish setting up elements based on loaded params
        String origin = structure.getAttribute(Constants.ATT_ORIGIN);
        String prefix = (ls.isChannel() ? channelPrefix : folderPrefix);

        // if not null we are dealing with a node incorporated from another
        // layout and this node contains changes made by the user so handle
        // id swapping.
        if (!origin.equals(""))
        {
            structure.setAttributeNS(
                Constants.NS_URI,
                Constants.ATT_PLF_ID,
                prefix + ls.getStructId());
            structure.setAttribute("ID", origin);
        }
        else if (!ls.isChannel())
            // regular folder owned by this user, need to check if this is a
            // directive or ui element. If the latter then use traditional id
            // structure
        {
            if (type != null && type.startsWith(Constants.NS))
            {
                structure.setAttribute(
                    "ID",
                    Constants.DIRECTIVE_PREFIX + ls.getStructId());
            }
            else
            {
                structure.setAttribute("ID", folderPrefix + ls.getStructId());
            }
        }
        else
        {
            if (LOG.isDebugEnabled())
                LOG.debug("Adding identifier " + folderPrefix + ls.getStructId() );
            structure.setAttribute("ID", channelPrefix + ls.getStructId());
        }
        
        return structure;
    }
    
    protected int saveStructure(
            Node node,
            PreparedStatement structStmt,
            PreparedStatement parmStmt)
            throws Exception
        {
            if (node == null || node.getNodeName().equals("parameter"))
            { // No more or parameter node
                return 0;
            }
            Element structure = (Element) node;

            if (LOG.isDebugEnabled())
                LOG.debug("saveStructure XML content: "
                    + XML.serializeNode(node));
            
            // determine the struct_id for storing in the db. For incorporated nodes in
            // the plf their ID is a system-wide unique ID while their struct_id for
            // storing in the db is cached in a dlm:plfID attribute.
            int saveStructId = -1;
            String plfID = structure.getAttribute(Constants.ATT_PLF_ID);

            if (!plfID.equals(""))
                saveStructId = Integer.parseInt(plfID.substring(1));
            else
                saveStructId =
                    Integer.parseInt(structure.getAttribute("ID").substring(1));

            int nextStructId = 0;
            int childStructId = 0;
            int chanId = -1;
            boolean isChannel = node.getNodeName().equals("channel");
            
            if (node.hasChildNodes())
            {
                childStructId =
                    saveStructure(node.getFirstChild(), structStmt, parmStmt);
            }
            nextStructId =
                saveStructure(node.getNextSibling(), structStmt, parmStmt);
            structStmt.clearParameters();
            structStmt.setInt(1, saveStructId);
            structStmt.setInt(2, nextStructId);
            structStmt.setInt(3, childStructId);

            String externalId = structure.getAttribute("external_id");
            if (externalId != null && externalId.trim().length() > 0)
            {
                Integer eID = new Integer(externalId);
                structStmt.setInt(4, eID.intValue());
            }
            else
            {
                structStmt.setNull(4, java.sql.Types.NUMERIC);

            }
            if (isChannel)
            {
                chanId =
                    Integer.parseInt(
                        node.getAttributes().getNamedItem("chanID").getNodeValue());
                structStmt.setInt(5, chanId);
                structStmt.setNull(6, java.sql.Types.VARCHAR);
            }
            else
            {
                structStmt.setNull(5, java.sql.Types.NUMERIC);
                structStmt.setString(6, structure.getAttribute("name"));
            }
            String structType = structure.getAttribute("type");
            structStmt.setString(7, structType);
            structStmt.setString(
                8,
                RDBMServices.dbFlag(xmlBool(structure.getAttribute("hidden"))));
            structStmt.setString(
                9,
                RDBMServices.dbFlag(xmlBool(structure.getAttribute("immutable"))));
            structStmt.setString(
                10,
                RDBMServices.dbFlag(
                    xmlBool(structure.getAttribute("unremovable"))));
            if (LOG.isDebugEnabled())
                LOG.debug("RDBMUserLayoutStore::saveStructure(): " + structStmt);
            structStmt.executeUpdate();

            // code to persist extension attributes for dlm
            NamedNodeMap attribs = node.getAttributes();
            for (int i = 0; i < attribs.getLength(); i++)
            {
                Node attrib = attribs.item(i);
                String name = attrib.getNodeName();

                if (name.startsWith(Constants.NS)
                    && !name.equals(Constants.ATT_PLF_ID)
                    && !name.equals(Constants.ATT_FRAGMENT)
                    && !name.equals(Constants.ATT_PRECEDENCE))
                {
                    // a cp extension attribute. Push into param table.
                    parmStmt.clearParameters();
                    parmStmt.setInt(1, saveStructId);
                    parmStmt.setString(2, name);
                    parmStmt.setString(3, attrib.getNodeValue());
                    if (LOG.isDebugEnabled())
                        LOG.debug("RDBMUserLayoutStore::saveStructure(): " + parmStmt);
                    parmStmt.executeUpdate();
                }
            }
            NodeList parameters = node.getChildNodes();
            if (parameters != null)
            {
                ChannelDefinition channelDef = crs.getChannelDefinition(chanId);
                for (int i = 0; i < parameters.getLength(); i++)
                {
                    if (parameters.item(i).getNodeName().equals("parameter"))
                    {
                        Element parmElement = (Element) parameters.item(i);
                        NamedNodeMap nm = parmElement.getAttributes();
                        String parmName = nm.getNamedItem("name").getNodeValue();
                        String parmValue = nm.getNamedItem("value").getNodeValue();
                        Node override = nm.getNamedItem("override");

                        // if no override specified then default to allowed
                        if (override != null
                            && !override.getNodeValue().equals("yes"))
                        {
                            // can't override
                        } else
                        {
                            // override only for adhoc or if diff from chan def
                            ChannelParameter cp = channelDef.getParameter(parmName);
                            if (cp == null || !cp.getValue().equals(parmValue))
                            {
                                parmStmt.clearParameters();
                                parmStmt.setInt(1, saveStructId);
                                parmStmt.setString(2, parmName);
                                parmStmt.setString(3, parmValue);
                                if (LOG.isDebugEnabled())
                                    LOG.debug(parmStmt);
                                parmStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
            return saveStructId;
        }

    public void setStructureStylesheetUserPreferences( IPerson person,
                                                       int profileId,
                                                       StructureStylesheetUserPreferences ssup )
        throws Exception
    {
        int userId = person.getID();
        Document PLF = (Document) person.getAttribute( Constants.PLF );
        if ( PLF == null )
            throw new Exception( "Unable to obtain user's PLF to translate" +
                                 " incorporated ids to plfIds." );
        int stylesheetId = ssup.getStylesheetId();
        StructureStylesheetDescription ssDesc =
            getStructureStylesheetDescription(stylesheetId);
        
        Connection con = RDBMServices.getConnection();
        try
        {
            // Set autocommit false for the connection
            RDBMServices.setAutoCommit(con, false);
            Statement stmt = con.createStatement();
            
            try
            {
                // before writing out params clean out old values
                String sQuery = "DELETE FROM UP_SS_USER_PARM " +
                "WHERE USER_ID=" + userId + " AND " +
                "PROFILE_ID=" + profileId + " AND " +
                "SS_ID=" + stylesheetId + " AND SS_TYPE=1";
                if (LOG.isDebugEnabled())
                    LOG.debug(sQuery);
                stmt.executeUpdate(sQuery);
                
                // write out params only if specified in stylesheet's .sdf file
                for (Enumeration e = ssup.getParameterValues().keys(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    if (ssDesc.containsParameterName(pName))
                    {
                        String pNameEscaped = RDBMServices.sqlEscape(pName);
                        sQuery = "INSERT INTO UP_SS_USER_PARM (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,PARAM_NAME,PARAM_VAL) VALUES (" + userId
                          + "," + profileId + "," + stylesheetId + ",1,'" + pNameEscaped + "','" + ssup.getParameterValue(pName) + "')";
                        if (LOG.isDebugEnabled())
                            LOG.debug(sQuery);
                        stmt.executeUpdate(sQuery);
                    }
                }

                // now before writing out folders and channels clean out old values
                sQuery = "DELETE FROM UP_SS_USER_ATTS " +
                "WHERE USER_ID=" + userId + " AND " +
                "PROFILE_ID=" + profileId + " AND " +
                "SS_ID=" + stylesheetId + " AND SS_TYPE=1";
                if (LOG.isDebugEnabled())
                    LOG.debug(sQuery);
                stmt.executeUpdate(sQuery);

                // write out folder attributes
                for (Enumeration e = ssup.getFolders(); e.hasMoreElements();) {
                    String folderId = (String)e.nextElement();
                    String plfFolderId = folderId;

                    if ( folderId.startsWith( Constants.FRAGMENT_ID_USER_PREFIX ) ) // icorporated node
                        plfFolderId = getPlfId( PLF, folderId );
                    if ( plfFolderId == null ) //couldn't translate, skip
                        continue;

                    for (Enumeration attre = ssup.getFolderAttributeNames(); attre.hasMoreElements();) {
                        String pName = (String)attre.nextElement();
                        if (ssDesc.containsFolderAttribute(pName)) 
                        {
                            String pValue = ssup.getDefinedFolderAttributeValue(folderId, pName);
                            if (pValue != null) {
                                // store user preferences
                                String pNameEscaped = RDBMServices.sqlEscape(pName);
                                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                                    + userId + "," + profileId + "," + stylesheetId + ",1,'" + plfFolderId.substring(1) + "','" + pNameEscaped + "',2,'" + pValue
                                    + "')";
                                if (LOG.isDebugEnabled())
                                    LOG.debug(sQuery);
                                stmt.executeUpdate(sQuery);
                            }
                        }
                    }
                }
                // write out channel attributes
                for (Enumeration e = ssup.getChannels(); e.hasMoreElements();) {
                    String channelId = (String)e.nextElement();
                    String plfChannelId = channelId;

                    if ( plfChannelId.startsWith( Constants.FRAGMENT_ID_USER_PREFIX ) ) // icorporated node
                        plfChannelId = getPlfId( PLF, channelId );
                    if ( plfChannelId == null ) //couldn't translate, skip
                        continue;

                    for (Enumeration attre = ssup.getChannelAttributeNames(); attre.hasMoreElements();) {
                        String pName = (String)attre.nextElement();
                        if (ssDesc.containsChannelAttribute(pName))
                        {
                            String pValue = ssup.getDefinedChannelAttributeValue(channelId, pName);
                            if (pValue != null) {
                                String pNameEscaped = RDBMServices.sqlEscape(pName);
                                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                                    + userId + "," + profileId + "," + stylesheetId + ",1,'" + plfChannelId.substring(1) + "','" + pNameEscaped + "',3,'" + pValue
                                    + "')";
                                if (LOG.isDebugEnabled())
                                    LOG.debug(sQuery);
                                stmt.executeUpdate(sQuery);
                            }
                        }
                    }
                }
                // Commit the transaction
                RDBMServices.commit(con);
                updateFragmentSSUP( person,
                                    (DistributedUserPreferences) ssup );
            } catch (Exception e) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Problem occurred ", e);
                // Roll back the transaction
                RDBMServices.rollback(con);
                throw new Exception("Exception setting Structure Sylesheet " +
                        "User Preferences",e);
            } finally {
                stmt.close();
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
    }

    public void setThemeStylesheetUserPreferences (IPerson person, int profileId, ThemeStylesheetUserPreferences tsup) throws Exception {
        int userId = person.getID();
        Document PLF = (Document) person.getAttribute( Constants.PLF );
        if ( PLF == null )
            throw new Exception( "Unable to obtain user's PLF to translate" +
                                 " incorporated ids to plfIds." );
        int stylesheetId = tsup.getStylesheetId();
        ThemeStylesheetDescription tsDesc =
            getThemeStylesheetDescription(stylesheetId);
        Connection con = RDBMServices.getConnection();
        try {
            // Set autocommit false for the connection
            RDBMServices.setAutoCommit(con, false);
            Statement stmt = con.createStatement();
            try {
                // before writing out params clean out old values
                String sQuery = "DELETE FROM UP_SS_USER_PARM " +
                "WHERE USER_ID=" + userId + " AND " +
                "PROFILE_ID=" + profileId + " AND " +
                "SS_ID=" + stylesheetId + " AND SS_TYPE=2";
                if (LOG.isDebugEnabled())
                    LOG.debug(sQuery);
                stmt.executeUpdate(sQuery);
                
                // write out params only if defined in stylesheet's .sdf file
                for (Enumeration e = tsup.getParameterValues().keys(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    if (tsDesc.containsParameterName(pName))
                    {
                        String pNameEscaped = RDBMServices.sqlEscape(pName);
                        sQuery = "INSERT INTO UP_SS_USER_PARM (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,PARAM_NAME,PARAM_VAL) VALUES (" + userId
                        + "," + profileId + "," + stylesheetId + ",2,'" + pNameEscaped + "','" + tsup.getParameterValue(pName) + "')";
                        if (LOG.isDebugEnabled())
                            LOG.debug(sQuery);
                        stmt.executeUpdate(sQuery);
                    }
                }
                // now before writing out channel atts clean out old values
                sQuery = "DELETE FROM UP_SS_USER_ATTS " +
                "WHERE USER_ID=" + userId + " AND " +
                "PROFILE_ID=" + profileId + " AND " +
                "SS_ID=" + stylesheetId + " AND SS_TYPE=2";
                if (LOG.isDebugEnabled())
                    LOG.debug(sQuery);
                stmt.executeUpdate(sQuery);

                // write out channel attributes
                for (Enumeration e = tsup.getChannels(); e.hasMoreElements();) {
                    String channelId = (String)e.nextElement();
                    String plfChannelId = channelId;

                    if ( plfChannelId.startsWith( Constants.FRAGMENT_ID_USER_PREFIX ) ) // icorporated node
                        plfChannelId = getPlfId( PLF, channelId );
                    if ( plfChannelId == null ) //couldn't translate, skip
                        continue;

                    for (Enumeration attre = tsup.getChannelAttributeNames(); attre.hasMoreElements();) {
                        String pName = (String)attre.nextElement();
                        if (tsDesc.containsChannelAttribute(pName))
                        {
                            String pValue = tsup.getDefinedChannelAttributeValue(channelId, pName);
                            if (pValue != null) {
                                String pNameEscaped = RDBMServices.sqlEscape(pName);
                                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                                + userId + "," + profileId + "," + stylesheetId + ",2,'" + plfChannelId.substring(1) + "','" + pNameEscaped + "',3,'" + pValue
                                + "')";
                                if (LOG.isDebugEnabled())
                                    LOG.debug(sQuery);
                                stmt.executeUpdate(sQuery);
                            }
                        }
                    }
                }
                // Commit the transaction
                RDBMServices.commit(con);
                // add a method nearly identical to updateFragmentSSUP() if
                // we want to push things like minimized state of a channel in
                // a fragment. (TBD: mboyd if needed)
                // updateFragmentTSUP();
            } catch (Exception e) {
                // Roll back the transaction
                RDBMServices.rollback(con);
                throw new Exception("Exception setting Theme Sylesheet " +
                        "User Preferences",e);
            } finally {
                stmt.close();
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
    }
    
}
