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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMUserIdentityStore;
import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.channel.ChannelLifecycleState;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.channel.XmlGeneratingBaseChannelDefinition;
import org.jasig.portal.layout.LayoutStructure;
import org.jasig.portal.layout.StructureParameter;
import org.jasig.portal.layout.simple.RDBMUserLayoutStore;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.BrokenSecurityContext;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.spring.locator.ConfigurationLoaderLocator;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.SmartCache;
import org.jasig.portal.utils.XML;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    private final static Pattern USER_NODE_PATTERN = Pattern.compile("\\A([a-zA-Z]\\d*)\\z");

    private String systemDefaultUser = null;
    private boolean systemDefaultUserLoaded = false;
    private ConfigurationLoader configurationLoader;
    private Map<String, FragmentNodeInfo> fragmentInfoCache = new ConcurrentHashMap<String, FragmentNodeInfo>();
    private LayoutDecorator decorator = null;

    // fragmentActivator
    private FragmentActivator fragmentActivator;
    private final SingletonDoubleCheckedCreator<FragmentActivator> fragmentActivatorCreator = new SingletonDoubleCheckedCreator<FragmentActivator>() {
        protected FragmentActivator createSingleton(Object... args) {
            // be sure we only do this once...

            RDBMDistributedLayoutStore parent = (RDBMDistributedLayoutStore) args[0];
            FragmentActivator rslt = new FragmentActivator(parent, configurationLoader.getFragments());
            rslt.activateFragments();
            return rslt;
        }
    };
    
    static final String TEMPLATE_USER_NAME
        = "org.jasig.portal.services.Authentication.defaultTemplateUserName";
    static final String DECORATOR_PROPERTY = "layoutDecorator";
    
    private static final int THEME = 0;
    private static final int STRUCT = 1;

    final static String DELETE_FROM_UP_SS_USER_ATTS_SQL = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID = ? AND PROFILE_ID = ? AND SS_ID = ? AND SS_TYPE = ?";
    final static String DELETE_FROM_UP_USER_PARM = "DELETE FROM UP_SS_USER_PARM WHERE USER_ID=?  AND PROFILE_ID=? AND SS_ID=? AND SS_TYPE=?";
    
    // Cache for theme stylesheet descriptors
    private static SmartCache tsdCache;
    // Cache for structure stylesheet descriptors
    private static SmartCache ssdCache;
    
    // Used in Import/Export operations
    private final org.dom4j.DocumentFactory fac = new org.dom4j.DocumentFactory();
    private final DOMReader reader = new DOMReader();
    private final DOMWriter writer = new DOMWriter();
    private Task lookupNoderefTask;
    private Task lookupPathrefTask;
    private final IUserIdentityStore identityStore = new RDBMUserIdentityStore();
    
    public void setLookupNoderefTask(Task k) {
        this.lookupNoderefTask = k;
    }

    public void setLookupPathrefTask(Task k) {
        this.lookupPathrefTask = k;
    }

    /**
     * Method for acquiring copies of fragment layouts to assist in debugging.
     * No infrastructure code calls this but channels designed to expose the
     * structure of the cached fragments use this to obtain copies.
     * @return Map
     */
    public Map<String, Document> getFragmentLayoutCopies()
    throws Exception
    {

        FragmentActivator activator = this.getFragmentActivator();

        Map<String, Document> layouts = new HashMap<String, Document>();

        final List<FragmentDefinition> definitions = configurationLoader.getFragments();
        for (final FragmentDefinition fragmentDefinition : definitions) {
            Document layout = DocumentFactory.getNewDocument();
            Node copy = layout.importNode(activator.getUserView(fragmentDefinition)
                        .layout.getDocumentElement(), true);
            layout.appendChild(copy);
            layouts.put(fragmentDefinition.getOwnerId(), layout);
        }
        return layouts;
    }

    public RDBMDistributedLayoutStore ( )
        throws Exception
    {
        super();
        
        tsdCache = new SmartCache();
        ssdCache = new SmartCache();
        
        this.configurationLoader = ConfigurationLoaderLocator.getConfigurationLoader();

        try
        {

            String decoratorClass = configurationLoader.getProperty( DECORATOR_PROPERTY );

            if ( decoratorClass != null )
                decorator = DecoratorLoader.load( decoratorClass );
        }
        catch( Exception e )
        {
            LOG.error("\n\n---------- Warning ---------\nUnable to load "
                        + "layout decorator '"
                        + configurationLoader.getProperty(DECORATOR_PROPERTY)
                        + "' specified in dlm.xml. It will not be used.", e);
        }

    }
    
    private FragmentActivator getFragmentActivator() {
        return this.fragmentActivatorCreator.get(this);
    }

    /**
     * Registers a NEW structure stylesheet with the database. This overloads
     * the version in the parent to add caching of stylesheets.
     * @param ssd Stylesheet description object
     * @return Integer
     */
    @Override
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
    @Override
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
     * @param stylesheetId id of the structure stylesheet
     * @return structure stylesheet description
     */
    @Override
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
     * @param stylesheetId id of the theme stylesheet
     * @return theme stylesheet description
     */
    @Override
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
     * @param stylesheetId id of the structure stylesheet
     */
    @Override
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
     * @param stylesheetId id of the theme stylesheet
     */
    @Override
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
    @Override
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
     * @param tsd new stylesheet description
     */
    @Override
    public void updateThemeStylesheetDescription(ThemeStylesheetDescription tsd)
        throws Exception
    {
        super.updateThemeStylesheetDescription(tsd);

        // Set the new one in the cache
        tsdCache.put(new Integer(tsd.getId()), tsd);
    }

    /**
     * Cleans out the layout fragments. This is done so that changes made to
     * the channels within a layout are visible to the users who have that layout
     * incorporated into their own.
     *
     * The interval at which this thread runs is set in the dlm.xml file as
     * 'org.jasig.portal.layout.dlm.RDBMDistributedLayoutStore.fragment_cache_refresh',
     * specified in minutes.
     */
    public void cleanFragments() {
        
        FragmentActivator activator = this.getFragmentActivator();

        //get each layout owner
        final List<FragmentDefinition> definitions = configurationLoader.getFragments();
        if ( null != definitions ) {
            
            final Map<IPerson, FragmentDefinition> owners = new HashMap<IPerson, FragmentDefinition>();
            for (final FragmentDefinition fragmentDefinition : definitions) {
                String ownerId = fragmentDefinition.getOwnerId();
                int userId  = activator.getUserView(fragmentDefinition).getUserId();

                if ( null != ownerId )
                {
                    IPerson p = new PersonImpl();
                    p.setID( userId );
                    p.setAttribute( "username", ownerId );
                    owners.put(p, fragmentDefinition);
                }
            }

            // cycle through each layout owner and clear out their
            // respective layouts so users fragments will be cleared
            for (final Map.Entry<IPerson, FragmentDefinition> ownerEntry : owners.entrySet()) {
                final IPerson person = ownerEntry.getKey();
                final UserProfile profile;
                try {
                    profile = getUserProfileByFname(person, "default");
                }
                catch (Exception e) {
                    this.log.error("Failed to retrieve UserProfile for person " + person + " while cleaning fragment cache, person will be skipped", e);
                    continue;
                }
                
                // TODO fix hard coded "default" later for profiling
                profile.setProfileFname("default");
                
                final Document layout;
                try {
                    layout = getFragmentLayout(person, profile);
                }
                catch (Exception e) {
                    this.log.error("Failed to retrieve layout for person " + person + " and profile " + profile + " while cleaning fragment cache, person will be skipped", e);
                    continue;
                }
                
                FragmentDefinition fragment = ownerEntry.getValue();
                updateCachedLayout( layout, profile, fragment );
            }
        }
        fragmentInfoCache = new ConcurrentHashMap<String, FragmentNodeInfo>();
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
        final List<FragmentDefinition> definitions = configurationLoader.getFragments();
        if ( index < 0 ||
             index > definitions.size()-1 )
            return 0;

        // must pass through the array looking for the fragment with this
        // index since the array was sorted by precedence and then index
        // within precedence.
        for (final FragmentDefinition fragmentDefinition : definitions) {
            if ( fragmentDefinition.getIndex() == index ) {
                return fragmentDefinition.getPrecedence();
            }
        }
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
    @Override
    public Document getUserLayout (IPerson person,
                                   UserProfile profile)
        throws Exception
    {

        Document layout = _getUserLayout( person, profile );

        if ( decorator != null )
            decorator.decorate( layout, person, profile );

        return layout;
    }
    
    private boolean layoutExistsForUser(IPerson person) {
        
        // Assertions.
        if (person == null) {
            String msg = "Argument 'person' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        final SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(RDBMServices.getDataSource());
        final int struct_count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM up_layout_struct WHERE user_id = ?", person.getID());
        return struct_count == 0 ? false : true;
        
    }
    
    @SuppressWarnings("unchecked")
    public org.dom4j.Element exportLayout(IPerson person, UserProfile profile) {
        
        if (!layoutExistsForUser(person)) {
            return null;
        }

        org.dom4j.Document layoutDoc = null;
        UserPreferences up = null;
        try {
            Document layoutDom = _safeGetUserLayout(person, profile);
            person.setAttribute(Constants.PLF, layoutDom);
            layoutDoc = reader.read(layoutDom);
            up = this.getUserPreferences(person, profile);
        } catch (Throwable t) {
            String msg = "Unable to obtain layout & profile for user '" 
                            + person.getUserName() + "', profileId " 
                            + profile.getProfileId();
            throw new RuntimeException(msg, t);
        }
        
        if (log.isDebugEnabled()) {
            // Write out this version of the layout to the log for dev purposes...
            StringWriter str = new StringWriter();
            XMLWriter xml = new XMLWriter(str, new OutputFormat("  ", true));
            try {
                xml.write(layoutDoc);
                xml.close();
            } catch (Throwable t) {
                throw new RuntimeException("Failed to write the layout for user '" 
                            + person.getUserName() + "' to the DEBUG log", t);
            }
            log.debug("Layout for user:  " + person.getUserName() 
                        + "\n" + str.getBuffer().toString());
        }

        /*
         * Clean up the DOM for export.
         */
        
        // (1) Add structure & theme attributes...
        StructureStylesheetUserPreferences ssup = up.getStructureStylesheetUserPreferences();
        // The structure transform supports both 'folder' and 'channel' attributes...
        List<String> structFolderAttrNames = Collections.list(ssup.getFolderAttributeNames());
        for (Iterator<org.dom4j.Element> fldrs = (Iterator<org.dom4j.Element>) layoutDoc.selectNodes("//folder").iterator(); fldrs.hasNext();) {
            org.dom4j.Element fld = fldrs.next();
            for (String attr : structFolderAttrNames) {
                String val = ssup.getDefinedFolderAttributeValue(fld.valueOf("@ID"), attr);
                if (val != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding structure folder attribute:  name=" + attr + ", value=" + val);
                    }
                    org.dom4j.Element sa = fac.createElement("structure-attribute");
                    org.dom4j.Element n = sa.addElement("name");
                    n.setText(attr);
                    org.dom4j.Element v = sa.addElement("value");
                    v.setText(val);
                    fld.elements().add(0, sa);
                }
            }
        }
        List<String> structChannelAttrNames = Collections.list(ssup.getChannelAttributeNames());
        for (Iterator<org.dom4j.Element> chnls = (Iterator<org.dom4j.Element>) layoutDoc.selectNodes("//channel").iterator(); chnls.hasNext();) {
            org.dom4j.Element chnl = chnls.next();
            for (String attr : structChannelAttrNames) {
                String val = ssup.getDefinedChannelAttributeValue(chnl.valueOf("@ID"), attr);
                if (val != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding structure channel attribute:  name=" + attr + ", value=" + val);
                    }
                    org.dom4j.Element sa = fac.createElement("structure-attribute");
                    org.dom4j.Element n = sa.addElement("name");
                    n.setText(attr);
                    org.dom4j.Element v = sa.addElement("value");
                    v.setText(val);
                    chnl.elements().add(0, sa);
                }
            }
        }
        // The theme transform supports only 'channel' attributes...
        ThemeStylesheetUserPreferences tsup = up.getThemeStylesheetUserPreferences();
        List<String> themeChannelAttrNames = Collections.list(tsup.getChannelAttributeNames());
        for (Iterator<org.dom4j.Element> chnls = (Iterator<org.dom4j.Element>) layoutDoc.selectNodes("//channel").iterator(); chnls.hasNext();) {
            org.dom4j.Element chnl = chnls.next();
            for (String attr : themeChannelAttrNames) {
                String val = tsup.getDefinedChannelAttributeValue(chnl.valueOf("@ID"), attr);
                if (val != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding theme channel attribute:  name=" + attr + ", value=" + val);
                    }
                    org.dom4j.Element ta = fac.createElement("theme-attribute");
                    org.dom4j.Element n = ta.addElement("name");
                    n.setText(attr);
                    org.dom4j.Element v = ta.addElement("value");
                    v.setText(val);
                    chnl.elements().add(0, ta);
                }
            }
        }
                
        // (2) Remove locale info...
        Iterator<org.dom4j.Attribute> locale = (Iterator<org.dom4j.Attribute>) layoutDoc.selectNodes("//@locale").iterator();
        while (locale.hasNext()) {
            org.dom4j.Attribute loc = locale.next();
            loc.getParent().remove(loc);
        }

        // (3) Scrub unnecessary channel information...
        for (Iterator<org.dom4j.Element> orphanedChannels = (Iterator<org.dom4j.Element>) layoutDoc.selectNodes("//channel[@fname = '']").iterator(); orphanedChannels.hasNext();) {
            // These elements represent UP_LAYOUT_STRUCT rows where the 
            // CHAN_ID field was not recognized by ChannelRegistryStore;  
            // best thing to do is remove the elements...
            org.dom4j.Element ch = orphanedChannels.next();
            ch.getParent().remove(ch);
        }
        List<String> channelAttributeWhitelist = Arrays.asList(new String[] { 
                        "fname", 
                        "unremovable", 
                        "hidden", 
                        "immutable",
                        "ID",
                        "dlm:plfID"
                    });
        Iterator<org.dom4j.Element> channels = (Iterator<org.dom4j.Element>) layoutDoc.selectNodes("//channel").iterator();
        while (channels.hasNext()) {
            org.dom4j.Element oldCh = channels.next();
            org.dom4j.Element parent = oldCh.getParent();
            org.dom4j.Element newCh = fac.createElement("channel");
            for (String aName : channelAttributeWhitelist) {
                org.dom4j.Attribute a = (org.dom4j.Attribute) oldCh.selectSingleNode("@" + aName);
                if (a != null) {
                    newCh.addAttribute(a.getName(), a.getValue());
                }
            }
            parent.elements().add(parent.elements().indexOf(oldCh), newCh);
            parent.remove(oldCh);
        }
                
        // (4) Convert internal DLM noderefs to external form (pathrefs)...
        for (Iterator<org.dom4j.Attribute> origins = (Iterator<org.dom4j.Attribute>) layoutDoc.selectNodes("//@dlm:origin").iterator(); origins.hasNext();) {
            org.dom4j.Attribute org = origins.next();
            String[] pathTokens = getDlmPathref((String) person.getAttribute(IPerson.USERNAME), person.getID(), org.getValue(), layoutDoc.getRootElement());
            if (pathTokens != null) {
                // Change the value only if we have a valid pathref...
                org.setValue(pathTokens[0] + ":" + pathTokens[1]);
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Layout element '" + org.getUniquePath() 
                            + "' from user '" + person.getAttribute(IPerson.USERNAME) 
                            + "' failed to match noderef '" + org.getValue() + "'");
                }
            }
        }
        for (Iterator<org.dom4j.Attribute> it = (Iterator<org.dom4j.Attribute>) layoutDoc.selectNodes("//@dlm:target").iterator(); it.hasNext();) {
            org.dom4j.Attribute target = it.next();
            String[] pathTokens = getDlmPathref((String) person.getAttribute(IPerson.USERNAME), person.getID(), target.getValue(), layoutDoc.getRootElement());
            if (pathTokens != null) {
                // Change the value only if we have a valid pathref...
                target.setValue(pathTokens[0] + ":" + pathTokens[1]);
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Layout element '" + target.getUniquePath() 
                            + "' from user '" + person.getAttribute(IPerson.USERNAME) 
                            + "' failed to match noderef '" + target.getValue() + "'");
                }
            }
        }
        for (Iterator<org.dom4j.Attribute> names = (Iterator<org.dom4j.Attribute>) layoutDoc.selectNodes("//dlm:*/@name").iterator(); names.hasNext();) {
            org.dom4j.Attribute n = names.next();
            if (n.getValue() == null || n.getValue().trim().length() == 0) {
                // Outer <dlm:positionSet> elements don't seem to use the name 
                // attribute, though their childern do.  Just skip these so we 
                // don't send a false WARNING.
                continue;
            }
            String[] pathTokens = getDlmPathref((String) person.getAttribute(IPerson.USERNAME), person.getID(), n.getValue(), layoutDoc.getRootElement());
            if (pathTokens != null) {
                // Change the value only if we have a valid pathref...
                n.setValue(pathTokens[0] + ":" + pathTokens[1]);
                // These *may* have fnames...
                if (pathTokens[2] != null && pathTokens[2].trim().length() != 0) {
                    n.getParent().addAttribute("fname", pathTokens[2]);                
                } 
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Layout element '" + n.getUniquePath() 
                            + "' from user '" + person.getAttribute(IPerson.USERNAME) 
                            + "' failed to match noderef '" + n.getValue() + "'");
                }
            }
        }

        // Remove synthetic Ids, but from non-fragment owners only...
        if (!isFragmentOwner(person)) {
            
            /*
             * In the case of fragment owners, the original database Ids allow 
             * us keep (not break) the associations that subscribers have with 
             * nodes on the fragment layout.
             */ 
            
            // (5) Remove dlm:plfID...
            for (Iterator<org.dom4j.Attribute> plfid = (Iterator<org.dom4j.Attribute>) layoutDoc.selectNodes("//@dlm:plfID").iterator(); plfid.hasNext();) {
                org.dom4j.Attribute plf = plfid.next();
                plf.getParent().remove(plf);
            }

            // (6) Remove database Ids...
            for (Iterator<org.dom4j.Attribute> ids = (Iterator<org.dom4j.Attribute>) layoutDoc.selectNodes("//@ID").iterator(); ids.hasNext();) {
                org.dom4j.Attribute a = ids.next();
                a.getParent().remove(a);
            }
        }

        return layoutDoc.getRootElement();

    }
        
    @SuppressWarnings("unchecked")
    public void importLayout(org.dom4j.Element layout) {
        
        String ownerUsername = layout.valueOf("@username");
        IPerson person = null;
        UserProfile profile = null;
        try {
            person = new PersonImpl();
            person.setUserName(ownerUsername);
            int ownerId = identityStore.getPortalUID(person);
            if (ownerId == -1) {
                String msg = "No userId for username=" + ownerUsername;
                throw new RuntimeException(msg);
            }
            person.setID(ownerId);
            person.setSecurityContext(new BrokenSecurityContext());
            profile = this.getUserProfileByFname(person, "default");
        } catch (Throwable t) {
            String msg = "Unrecognized user " + person.getUserName() + "; you must import users before their layouts.";
            throw new RuntimeException(msg, t);
        }
        
        // (6) Add database Ids & (5) Add dlm:plfID ...
        int nextId = 1;
        for (Iterator<org.dom4j.Element> it = (Iterator<org.dom4j.Element>) layout.selectNodes("folder | dlm:*").iterator(); it.hasNext();) {
            nextId = addIdAttributesIfNecessary(it.next(), nextId);
        }
        // Now update UP_USER...
        final SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(RDBMServices.getDataSource());
        jdbcTemplate.update("UPDATE up_user SET next_struct_id = ? WHERE user_id = ?", nextId, person.getID());

        // (4) Convert external DLM pathrefs to internal form (noderefs)...
        for (Iterator<org.dom4j.Attribute> itr = (Iterator<org.dom4j.Attribute>) layout.selectNodes("//@dlm:origin").iterator(); itr.hasNext();) {
            org.dom4j.Attribute a = itr.next();
            String noderef = getDlmNoderef(ownerUsername, a.getValue(), null, true, layout);
            if (noderef != null) {
                // Change the value only if we have a valid pathref...
                a.setValue(noderef);
                // For dlm:origin only, also use the noderef as the ID attribute...
                a.getParent().addAttribute("ID", noderef);
            }
         }
        for (Iterator<org.dom4j.Attribute> itr = (Iterator<org.dom4j.Attribute>) layout.selectNodes("//@dlm:target").iterator(); itr.hasNext();) {
            org.dom4j.Attribute a = itr.next();
            String noderef = getDlmNoderef(ownerUsername, a.getValue(), null, true, layout);
            if (noderef != null) {
                // Change the value only if we have a valid pathref...
                a.setValue(noderef);
            }
        }
        for (Iterator<org.dom4j.Attribute> names = (Iterator<org.dom4j.Attribute>) layout.selectNodes("//dlm:*/@name").iterator(); names.hasNext();) {
            org.dom4j.Attribute a = names.next();
            org.dom4j.Attribute fname = a.getParent().attribute("fname");
            String noderef = null;
            if (fname != null) {
                noderef = getDlmNoderef(ownerUsername, a.getValue(), fname.getValue(), false, layout);
                // Remove the fname attribute now that we're done w/ it...
                fname.getParent().remove(fname);
            } else {
                noderef = getDlmNoderef(ownerUsername, a.getValue(), null, true, layout);
            }
            if (noderef != null) {
                // Change the value only if we have a valid pathref...
                a.setValue(noderef);
            }
        }

        // (3) Restore chanID attributes on <channel> elements...
        try {
            for (Iterator<org.dom4j.Element> it = (Iterator<org.dom4j.Element>) layout.selectNodes("//channel").iterator(); it.hasNext();) {
                org.dom4j.Element c = it.next();
                IChannelDefinition cd = this.channelRegistryStore.getChannelDefinition(c.valueOf("@fname"));
                c.addAttribute("chanID", String.valueOf(cd.getId()));
            }
        } catch (Throwable t) {
            String msg = "Error linking channels contained in layout for user:  " + ownerUsername;
            throw new RuntimeException(msg, t);
        }
        
        // (2) Restore locale info...
        // (This step doesn't appear to be needed for imports)

        // (1) Process structure & theme attributes...
        Document layoutDom = null;
        try {

//            UserPreferences up = this.getUserPreferences(person, profile);
            UserPreferences up = new UserPreferences(profile);
            up.setStructureStylesheetUserPreferences(this.getDistributedSSUP(person, 
                        profile.getProfileId(), profile.getStructureStylesheetId()));
            up.setThemeStylesheetUserPreferences(this.getDistributedTSUP(person, 
                        profile.getProfileId(), profile.getThemeStylesheetId()));
            
            // Structure Attributes.
            boolean saSet = false;
            StructureStylesheetUserPreferences ssup = up.getStructureStylesheetUserPreferences();
            // ssup must be manually cleaned out.
            for (Enumeration<String> names = (Enumeration<String>) ssup.getFolderAttributeNames(); names.hasMoreElements();) {
                String n = names.nextElement();
                for (Enumeration<String> fIds = (Enumeration<String>) ssup.getFolders(); fIds.hasMoreElements();) {
                    String f = fIds.nextElement();
                    if (ssup.getDefinedFolderAttributeValue(f, n) != null) {
                        ssup.removeFolder(f);
                    }
                }
            }
            for (Enumeration<String> names = (Enumeration<String>) ssup.getChannelAttributeNames(); names.hasMoreElements();) {
                String n = names.nextElement();
                for (Enumeration<String> chds = (Enumeration<String>) ssup.getChannels(); chds.hasMoreElements();) {
                    String c = chds.nextElement();
                    if (ssup.getDefinedChannelAttributeValue(c, n) != null) {
                        ssup.removeChannel(c);
                    }
                }
            }
            for (Iterator<org.dom4j.Element> it = (Iterator<org.dom4j.Element>) layout.selectNodes("//structure-attribute").iterator(); it.hasNext();) {
                org.dom4j.Element sa = (org.dom4j.Element) it.next();
                String idAttr = sa.getParent().valueOf("@ID");
                if (sa.getParent().getName().equals("folder")) {
                    ssup.setFolderAttributeValue(idAttr, sa.valueOf("name"), sa.valueOf("value"));
                    saSet = true;
                } else if (sa.getParent().getName().equals("channel")) {
                    ssup.setChannelAttributeValue(idAttr, sa.valueOf("name"), sa.valueOf("value"));
                    saSet = true;
                } else {
                    String msg = "Unrecognized parent element for user preferences attribute:  " + sa.getParent().getName();
                    throw new RuntimeException(msg);
                }
                // Remove these elements or else DLM will choke...
                sa.getParent().remove(sa);
            }
            
            // Theme Attributes.
            boolean taSet = false;
            ThemeStylesheetUserPreferences tsup = up.getThemeStylesheetUserPreferences();
            // tsup must be manually cleaned out.
            for (Enumeration<String> names = (Enumeration<String>) tsup.getChannelAttributeNames(); names.hasMoreElements();) {
                String n = names.nextElement();
                for (Enumeration<String> chds = (Enumeration<String>) tsup.getChannels(); chds.hasMoreElements();) {
                    String c = chds.nextElement();
                    if (tsup.getDefinedChannelAttributeValue(c, n) != null) {
                        tsup.removeChannel(c);
                    }
                }
            }
            for (Iterator<org.dom4j.Element> it = (Iterator<org.dom4j.Element>) layout.selectNodes("//theme-attribute").iterator(); it.hasNext();) {
                org.dom4j.Element ta = (org.dom4j.Element) it.next();
                String idAttr = ta.getParent().valueOf("@ID");
                // Theme attributes are channels only...
                tsup.setChannelAttributeValue(idAttr, ta.valueOf("name"), ta.valueOf("value"));
                taSet = true;
                // Remove these elements or else DLM will choke...
                ta.getParent().remove(ta);
            }

            // From this point forward we need the user's PLF set as DLM expects it...
            for (Iterator<org.dom4j.Text> it = (Iterator<org.dom4j.Text>) layout.selectNodes("descendant::text()").iterator(); it.hasNext();) {
                // How many years have we used Java & XML, and this still isn't easy?
                org.dom4j.Text txt = it.next();
                if (txt.getText().trim().length() == 0) {
                    txt.getParent().remove(txt);
                }
            }
            
            org.dom4j.Element copy = layout.createCopy();
            org.dom4j.Document doc = fac.createDocument(copy);
            doc.normalize();
            layoutDom = writer.write(doc);
            person.setAttribute(Constants.PLF, layoutDom);

            if (saSet) {
              this.setStructureStylesheetUserPreferences(person, profile.getProfileId(), ssup);
            }
            if (taSet) {
                this.setThemeStylesheetUserPreferences(person, profile.getProfileId(), tsup);
            }
            
        } catch (Throwable t) {
            log.error("Unable to set UserPreferences for user:  " + person.getUserName());
            throw new RuntimeException(t);
        }
        
        // Finally store the layout...
        try {
            this.setUserLayout(person, profile, layoutDom, true, false);
        } catch (Throwable t) {
            String msg = "Unable to persist layout for user:  " + ownerUsername;
            throw new RuntimeException(msg, t);
        }

    }
    
    @SuppressWarnings("unchecked")
    private final int addIdAttributesIfNecessary(org.dom4j.Element e, int nextId) {
        
        int idAfterThisOne = nextId;  // default...
        if (e.selectSingleNode("@ID | @dlm:plfID") == null) {
            if (log.isDebugEnabled()) {
                log.debug("No ID or dlm:plfID attribute for the following node "
                		    + "(one will be generated and added):  element" 
                            + e.getName() + ", name=" + e.valueOf("@name") 
                            + ", fname=" + e.valueOf("@fname"));
            }
            
            // We need to add an ID to this node...
            char prefix;
            if (e.getName().equals("folder")) {
                prefix = 's';
            } else if (e.getName().equals("channel")) {
                prefix = 'n';
            } else if (e.getQName().getNamespacePrefix().equals("dlm")) {
                prefix = 'd';
            } else {
                throw new RuntimeException("Unrecognized element type:  " + e.getName());
            }

            String origin = e.valueOf("@dlm:origin");
            // 'origin' may be null if the dlm:origin attribute is an 
            // empty string (which also shouldn't happen);  'origin' 
            // will be zero-length if dlm:origin is not defined...
            if (origin != null && origin.length() != 0) {
                // Add as dlm:plfID, if necessary...
                e.addAttribute("dlm:plfID", prefix + String.valueOf(nextId));
            } else {
                // Do the standard thing, if necessary...
                e.addAttribute("ID", prefix + String.valueOf(nextId));
            }

            ++idAfterThisOne;
        }

        // Now check children...
        for (Iterator<org.dom4j.Element> itr = (Iterator<org.dom4j.Element>) e.selectNodes("folder | channel | dlm:*").iterator(); itr.hasNext();) {
            org.dom4j.Element child = itr.next();
            idAfterThisOne = addIdAttributesIfNecessary(child, idAfterThisOne);
        }
        
        return idAfterThisOne;
    
    }

    private final String[] getDlmPathref(String layoutOwnerUsername, int layoutOwnerUserId, String dlmNoderef, org.dom4j.Element layout) {
        
        // Assertions.
        if (layoutOwnerUsername == null) {
            String msg = "Argument 'layoutOwnerUsername' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (dlmNoderef == null) {
            String msg = "Argument 'dlmNoderef' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (layout == null) {
            String msg = "Argument 'layout' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        String[] rslt = null;  // This will be the response if we can't make a match...
        
        final Matcher m = USER_NODE_PATTERN.matcher(dlmNoderef);
        if (m.find()) {
            // We need a pathref based on the new style of layout b/c on 
            // import this users own layout will not be in the database 
            // when the path in computed back to an Id...
            String structId = m.group(1);
            org.dom4j.Node target = layout.selectSingleNode("//*[@ID = '" + structId + "']");
            if (target != null) {
                rslt = new String[3];
                rslt[0] = layoutOwnerUsername;
                rslt[1] = target.getUniquePath();
                if (target.getName().equals("channel")) {
                    rslt[2] = target.valueOf("@fname");
                }
            } else {
                log.warn("no match found on layout for user '"+ layoutOwnerUsername 
                            + "' for the specified dlmNoderef:  " + dlmNoderef);
            }
        } else {
            ReturnValueImpl rvi = new ReturnValueImpl();
            RuntimeRequestResponse tr = new RuntimeRequestResponse();
            tr.setAttribute(Attributes.RETURN_VALUE, rvi);
            tr.setAttribute("USER_NAME", layoutOwnerUsername);
            tr.setAttribute("DLM_NODEREF", dlmNoderef);
            // The 'layoutStoreProvider' attribute could use some rework;  
            // adding it like this to avoid circular dependency issues
            final RDBMDistributedLayoutStore layoutStore = this;
            tr.setAttribute("layoutStoreProvider", new LayoutStoreProvider() {
                @Override
                public RDBMDistributedLayoutStore getLayoutStore() {
                    return layoutStore;
                }
            });
            this.lookupNoderefTask.perform(tr, new RuntimeRequestResponse());
            
            rslt = (String[]) rvi.getValue();
        }
        
        return rslt;

    }

    private final String getDlmNoderef(String layoutOwner, String pathref, String fname, boolean isStructRef, org.dom4j.Element layoutElement) {
        
        // Assertions.
        if (layoutOwner == null) {
            String msg = "Argument 'layoutOwner' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (pathref == null) {
            String msg = "Argument 'pathref' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  Argument 'fname' may be null...
        if (layoutElement == null) {
            String msg = "Argument 'layoutElement' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        String rslt = pathref; // This will be the response if we can't make a match...
        
        if (pathref.startsWith(layoutOwner + ":")) {
            // This an internal reference (our own layout);  we have to 
            // use the layoutExment (instead of load-limited-layout) b/c 
            // our layout may not be in the db...
            String[] pathTokens = pathref.split("\\:");
            org.dom4j.Element target = (org.dom4j.Element) layoutElement.selectSingleNode(pathTokens[1]);
            if (target != null) {
                rslt = target.valueOf("@ID");
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Unable to resolve pathref '" + pathref 
                            + "' for layoutOwner '" 
                            + layoutOwner + "'");
                }
            }
        } else {
            ReturnValueImpl rvi = new ReturnValueImpl();
            RuntimeRequestResponse tr = new RuntimeRequestResponse();
            tr.setAttribute(Attributes.RETURN_VALUE, rvi);
            tr.setAttribute("USER_NAME", layoutOwner);
            tr.setAttribute("DLM_PATHREF", pathref);
            if (fname != null) {
                tr.setAttribute("FNAME", fname);
            }
            if (isStructRef) {
                tr.setAttribute("IS_STRUCT_REF", Boolean.TRUE);
            }
            // The 'layoutStoreProvider' attribute could use some rework;  
            // adding it like this to avoid circular dependency issues
            final RDBMDistributedLayoutStore layoutStore = this;
            tr.setAttribute("layoutStoreProvider", new LayoutStoreProvider() {
                @Override
                public RDBMDistributedLayoutStore getLayoutStore() {
                    return layoutStore;
                }
            });
            this.lookupPathrefTask.perform(tr, new RuntimeRequestResponse());
            
            String val = (String) rvi.getValue();
            if (val != null) {
                rslt = val;
            }
        }
        
        // Data got orphaned, nothing we can do;  we need to 
        // be sure not to leave any that are too long...
        return rslt.length() <= 35 ? rslt : "";

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
        Document layoutDoc = super.getUserLayout(person, profile);
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
                                           ownedFragment.getName() );
                if (LOG.isDebugEnabled())
                    LOG.debug("User '" + userName + "' is owner of '"
                            + ownedFragment.getName() + "' fragment.");
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
    
//    void waitForActivation() {
//        this.activator.activateFragments();
//    }

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

        FragmentActivator activator = this.getFragmentActivator();

        // Fix later to handle multiple profiles
        Element root = layout.getDocumentElement();
        final UserView userView = activator.getUserView(fragment);
        root.setAttribute( Constants.ATT_ID,
                           Constants.FRAGMENT_ID_USER_PREFIX +
                           userView.getUserId() +
                           Constants.FRAGMENT_ID_LAYOUT_PREFIX + "1" );
        UserView view = new UserView( userView.getUserId(),
                                      profile,
                                      layout,
                                      userView.structUserPrefs,
                                      userView.themeUserPrefs );
        try
        {
            activator.fragmentizeLayout( view, fragment );
            activator.setUserView(fragment.getOwnerId(), view);
            this.fragmentInfoCache = new HashMap<String, FragmentNodeInfo>();
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

        final List<FragmentDefinition> definitions = configurationLoader.getFragments();
        if ( userName != null && definitions != null )
        {
            for (final FragmentDefinition fragmentDefinition : definitions) {
                if ( fragmentDefinition.defaultLayoutOwnerID != null &&
                        fragmentDefinition.defaultLayoutOwnerID.equals( userName ) ) {
                    return true;
                }
            }
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
     * Determines if a user is a fragment owner.
     * @param person
     * @return
     */
    public boolean isFragmentOwner(IPerson person)
    {
        return getOwnedFragment(person) != null;
    }
    
    /**
       Returns the fragment owned by this user if any. If this user is not a
       fragment owner then null is returned.
    */
    private FragmentDefinition getOwnedFragment( IPerson person )
    {
        int userId = person.getID();

        FragmentActivator activator = this.getFragmentActivator();

        final List<FragmentDefinition> definitions = configurationLoader.getFragments();
        if ( definitions != null )
        {
            for (final FragmentDefinition fragmentDefinition : definitions) {
                final UserView userView = activator.getUserView(fragmentDefinition);
                if (userView != null) {
                    int fdId = userView.getUserId();
                    if ( fdId == userId ) {
                        return fragmentDefinition;
                    }
                }
            }
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
        Vector<Document> applicables = new Vector<Document>();

        final List<FragmentDefinition> definitions = configurationLoader.getFragments();
        
        if (log.isDebugEnabled()) {
            log.debug("About to check applicability of " + definitions.size() + " fragments");
        }
        
        FragmentActivator activator = this.getFragmentActivator();

        if ( definitions != null )
        {
            for (final FragmentDefinition fragmentDefinition : definitions) {

                if (log.isDebugEnabled()) {
                    log.debug("Checking applicability of the following fragment:  " + fragmentDefinition.getName());
                }

                if ( fragmentDefinition.isApplicable(person) )
                {
                    applicables.add( activator.getUserView(fragmentDefinition).layout );
                }
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
        return configurationLoader.getPropertyCount();
    }

    /**
       Returns the specified property loaded from dlm.xml or null if not found.
     */
    public String getProperty( String name )
    {
        return configurationLoader.getProperty( name );
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
        Map<String, FragmentNodeInfo> infoCache = fragmentInfoCache;
        final List<FragmentDefinition> fragments = configurationLoader.getFragments();

        FragmentActivator activator = this.getFragmentActivator();

        FragmentNodeInfo info = infoCache.get(sId);

        if (info == null)
        {
            for (final FragmentDefinition fragmentDefinition : fragments) {
                Element node = activator.getUserView(fragmentDefinition).layout.getElementById(sId);
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
            ( /*super.*/_getThemeStylesheetUserPreferences( person,
                                                       profileId,
                                                       stylesheetId ) );
    }
    
    private Map<Integer, String> getOriginIds(Connection con, int userId, int profileId, int stylesheetType, int stylesheetId) throws SQLException {
        /*
         * Now we need to load in the folder and channel attributes from
        * the up_ss_user_atts table. But before doing so to avoid using
        * an outer join and to work around an oracle restriction of
        * having only on LONG type returned per result set we first
        * load dlm:origin IDs for the corresponding nodes it any. These
        * origin IDs indicate that the node whose attribute is being
        * overridden came from a fragment and that origin ID must then
        * be used for the node id when setting them on the structure
        * stylesheet user preferences object.
        */
        
        /*
         * the list of origin Ids if any for nodes having overridden
         * structure stylesheet attributes.
         */
        String originIdQuery = "SELECT struct_id, struct_parm_val "
                                        + "FROM up_layout_param "
                                        + "WHERE user_id=?"
                                        + " AND layout_id = 1 and "
                                        + "(struct_parm_nm=?"
                                        + " OR struct_parm_nm=?"
                                        + ") AND struct_id IN ("
                                            + "SELECT struct_id FROM up_ss_user_atts "
                                            + "WHERE user_id=?"
                                            + " AND profile_id=?"
                                            + " AND ss_type=?"
                                            + " AND ss_id=?)";

        final PreparedStatement pstmt = con.prepareStatement(originIdQuery);
        
        pstmt.setInt(1,userId);
        pstmt.setString(2,Constants.ATT_ORIGIN);
        pstmt.setString(3,Constants.LEGACY_ATT_ORIGIN);
        // for structIdsWithCustomUserValues select
        pstmt.setInt(4,userId);
        pstmt.setInt(5,profileId);
        pstmt.setInt(6,stylesheetType);
        pstmt.setInt(7,stylesheetId);
        Map<Integer, String> originIds = null;
        try {
            final ResultSet rs = pstmt.executeQuery();
            try {
                while (rs.next()) {
                    if (originIds == null)
                            originIds = new HashMap<Integer, String>();
                    // get the LONG column first so Oracle doesn't toss a
                    // java.sql.SQLException: Stream has already been closed
                    String originId = rs.getString(2);
                    int structId = rs.getInt(1);
                    originIds.put(new Integer(structId), originId);
                }
            }
            finally {
                close(rs);
            }
        }
        finally
        {
            close(pstmt);
        }
        
        return originIds;
    }

    private StructureStylesheetUserPreferences _getStructureStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) throws Exception {
        int userId = person.getID();
        StructureStylesheetUserPreferences ssup;
        
        // get stylesheet description
        StructureStylesheetDescription ssd = getStructureStylesheetDescription(stylesheetId);
        
        Connection con = RDBMServices.getConnection();
        try {
            int origId;
            int origProfileId;
            String sQuery = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID = ?";
            final PreparedStatement pstmt1 = con.prepareStatement(sQuery);
            try {
                // now look to see if this user has a layout or not. This is
                // important because preference values are stored by layout
                // element and if the user doesn't have a layout yet then the
                // default user's preferences need to be loaded.
                int layoutId = this.getLayoutID(userId, profileId);

                // if no layout then get the default user id for this user

                origId = userId;
                origProfileId = profileId;
                if (layoutId == 0) {
                    
                    pstmt1.setInt(1,userId);
                    if (LOG.isDebugEnabled())
                        LOG.debug(sQuery + " VALUE " + userId);
                    final ResultSet rs = pstmt1.executeQuery();
                    try {
                        rs.next();
                        userId = rs.getInt(1);
                        
                        // get the profile ID for the default user
                        UserProfile profile = getUserProfileById(person, profileId);
                		IPerson defaultProfilePerson = new PersonImpl();
                		defaultProfilePerson.setID(userId);
                        profileId = getUserProfileByFname(defaultProfilePerson, profile.getProfileFname()).getProfileId();

                    } finally {
                        close(rs);
                    }
                }
            } finally {
                close(pstmt1);
            }

            // create the stylesheet user prefs object then fill
            // it with defaults from the stylesheet definition object

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
                " AND PROFILE_ID=?"+
                " AND SS_ID=?" +
                " AND SS_TYPE=1";

            final PreparedStatement pstmt2 = con.prepareStatement(pstmtQuery);

            try {
                pstmt2.setInt(1, userId);
                pstmt2.setInt(2,profileId);
                pstmt2.setInt(3,stylesheetId);
                final ResultSet rs1 = pstmt2.executeQuery();
                try {
                    while (rs1.next()) {
                        String pName = rs1.getString(1);
                        if (ssd.containsParameterName(pName))
                            ssup.putParameterValue(pName, rs1.getString(2));
                    }
                }
                finally {
                    close(rs1);
                }

                if (userId != origId) {
                    pstmt2.setInt(1, origId);
                    pstmt2.setInt(2,origProfileId);
                    final ResultSet rs2 = pstmt2.executeQuery();
                    try {
                        while (rs2.next()) {
                            String pName = rs2.getString(1);
                            if (ssd.containsParameterName(pName))
                                ssup.putParameterValue(pName, rs2.getString(2));
                        }
                    }
                    finally {
                        close(rs2);
                    }
                }
            }
            finally {
                close(pstmt2);
            }

            
            Map<Integer, String> originIds = getOriginIds(con, userId, profileId, 1, stylesheetId);

            /*
             * now go get the overridden values and compare them against the
             * map for their origin ids.
             */
                    sQuery = "SELECT PARAM_NAME, PARAM_VAL, PARAM_TYPE," +
            " ULS.STRUCT_ID, CHAN_ID " +
                            "FROM UP_LAYOUT_STRUCT ULS, " +
            " UP_SS_USER_ATTS UUSA " +
                            "WHERE UUSA.USER_ID=?"+
                            " AND PROFILE_ID=?" +
                            " AND SS_ID=?" +
                            " AND SS_TYPE=1" +
                            " AND UUSA.STRUCT_ID = ULS.STRUCT_ID" +
                            " AND UUSA.USER_ID = ULS.USER_ID";

            if (LOG.isDebugEnabled())
                LOG.debug(sQuery + "VALUES ");

            final PreparedStatement pstmt4 = con.prepareStatement(sQuery);
            try {
                pstmt4.setInt(1,userId);
                pstmt4.setInt(2,profileId);
                pstmt4.setInt(3,stylesheetId);
                final ResultSet rs = pstmt4.executeQuery();
                try {
                    while (rs.next()) {
                        // get the LONG column first so Oracle doesn't toss a
                        // java.sql.SQLException: Stream has already been closed
                        String param_val = rs.getString(2);
                        int structId = rs.getInt(4);
                        String originId = null;
                        if (originIds != null)
                            originId = originIds.get(new Integer(structId));

                        int param_type = rs.getInt(3);
                        if (rs.wasNull()) {
                            structId = 0;
                        }
                        String pName = rs.getString(1);
                        int chanId = rs.getInt(5);
                        if (rs.wasNull()) {
                            chanId = 0;
                        }
                        /*
                         * ignore unexpected param_types since persisting
                         * removes all entries in table and it will get resolved
                         * without a log entry to point out the error.
                         */
                        if (param_type == 2) {
                            // folder attribute
                            String folderStructId = null;
                            if ( originId != null )
                                folderStructId = originId;
                            else
                                folderStructId = getStructId(structId,chanId);
                            if (ssd.containsFolderAttribute(pName))
                                ssup.setFolderAttributeValue(folderStructId, pName, param_val);
                        }
                        else if (param_type == 3) {
                            // channel attribute
                            String channelStructId = null;
                            if ( originId != null )
                                channelStructId = originId;
                            else
                                channelStructId = getStructId(structId,chanId);
                            if (ssd.containsChannelAttribute(pName))
                                ssup.setChannelAttributeValue(channelStructId, pName, param_val);
                        }
                    }
                } finally {
                    close(rs);
                }
            } finally {
                close(pstmt4);
            }
        }
        finally {
            RDBMServices.releaseConnection(con);
        }
        return  ssup;
    }

    private ThemeStylesheetUserPreferences _getThemeStylesheetUserPreferences(
            IPerson person, int profileId, int stylesheetId) throws Exception
    {
        int userId = person.getID();
        ThemeStylesheetUserPreferences tsup;
        Connection con = RDBMServices.getConnection();
        try
        {
            // get stylesheet description
            ThemeStylesheetDescription tsd = getThemeStylesheetDescription(stylesheetId);
            // get user defined defaults

            int layoutId = this.getLayoutID(userId, profileId);
            
            // if no layout then get the default user id for this user
            int origId = userId;
            int origProfileId = profileId;
            if (layoutId == 0)
            { // First time, grab the default layout for this user
                String sQuery = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=?";
                if (log.isDebugEnabled())
                    log.debug(sQuery + " VALUE = " + userId);
                final PreparedStatement pstmt1 = con.prepareStatement(sQuery);
                try {
                    pstmt1.setInt(1,userId);
                    final ResultSet rs = pstmt1.executeQuery();
                    try
                    {
                        rs.next();
                        userId = rs.getInt(1);
                        
                        // get the profile ID for the default user
                        UserProfile profile = getUserProfileById(person, profileId);
                		IPerson defaultProfilePerson = new PersonImpl();
                		defaultProfilePerson.setID(userId);
                        profileId = getUserProfileByFname(defaultProfilePerson, profile.getProfileFname()).getProfileId();

                    } finally
                    {
                        close(rs);
                    }
                }
                finally {
                    close(pstmt1);
                }
            }

            // create the stylesheet user prefs object then fill
            // it with defaults from the stylesheet definition object

            tsup = new ThemeStylesheetUserPreferences();
            tsup.setStylesheetId(stylesheetId);
            // fill stylesheet description with defaults
            for (Enumeration e = tsd.getStylesheetParameterNames(); e
                    .hasMoreElements();)
            {
                String pName = (String) e.nextElement();
                tsup.putParameterValue(pName, tsd
                        .getStylesheetParameterDefaultValue(pName));
            }
            for (Enumeration e = tsd.getChannelAttributeNames(); e
                    .hasMoreElements();)
            {
                String pName = (String) e.nextElement();
                tsup.addChannelAttribute(pName, tsd
                        .getChannelAttributeDefaultValue(pName));
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

            String sQuery2 =
                "SELECT PARAM_NAME, PARAM_VAL " +
                "FROM UP_SS_USER_PARM " +
                "WHERE USER_ID=?" +
                " AND PROFILE_ID=?" +
                " AND SS_ID=?" +
                " AND SS_TYPE=2";

            if (log.isDebugEnabled())
                log.debug(sQuery2 + " VALUES " +  userId + "," + profileId + "," + stylesheetId);
            
            final PreparedStatement pstmt2 = con.prepareStatement(sQuery2);
            try
            {
                pstmt2.setInt(1, userId);
                pstmt2.setInt(2,profileId);
                pstmt2.setInt(3,stylesheetId);
                final ResultSet rs = pstmt2.executeQuery();
                try {
                    while (rs.next())
                    {
                        // stylesheet param
                        String pName = rs.getString(1);
                        if (tsd.containsParameterName(pName))
                        	tsup.putParameterValue(pName, rs.getString(2));
                    }
                }
                finally {
                    close(rs);
                }
                
                if (userId != origId) {
                    pstmt2.setInt(1, origId);
                    pstmt2.setInt(2, origProfileId);
                    final ResultSet rs2 = pstmt2.executeQuery();
                    try {
                        while (rs2.next()) {
                            String pName = rs.getString(1);
                            if (tsd.containsParameterName(pName))
                            	tsup.putParameterValue(pName, rs2.getString(2));
                        }
                    }
                    finally {
                        close(rs2);
                    }
                }
            } finally
            {
                close(pstmt2);
            }
            
            Map<Integer, String> originIds = getOriginIds(con, userId, profileId, 2, stylesheetId);

            // Now load in the channel attributes preferences from the
            // up_ss_user_atts table

            final String sQuery3 = "SELECT PARAM_TYPE, PARAM_NAME, PARAM_VAL, " +
                    "ULS.STRUCT_ID, CHAN_ID " +
                    "FROM UP_SS_USER_ATTS UUSA, UP_LAYOUT_STRUCT ULS " +
                    "WHERE UUSA.USER_ID=?" +
                    " AND PROFILE_ID=?"  +
                    " AND SS_ID=?"  +
                    " AND SS_TYPE=2" +
                    " AND UUSA.STRUCT_ID = ULS.STRUCT_ID" +
                    " AND UUSA.USER_ID = ULS.USER_ID";
            if (log.isDebugEnabled())
                log.debug("SQL to load theme channel attribute prefs: "
                                + sQuery3 + " VALUES " + userId + "," + profileId + "," + stylesheetId);
            final PreparedStatement pstmt3 = con.prepareStatement(sQuery3);
            try {
                pstmt3.setInt(1,userId);
                pstmt3.setInt(2,profileId);
                pstmt3.setInt(3,stylesheetId);
                
                final ResultSet rs = pstmt3.executeQuery();
                try {
                    while (rs.next())
                    {
                        int param_type = rs.getInt(1);
                        if (rs.wasNull())
                        {
                            param_type = 0;
                        }
                        int structId = rs.getInt(4);
                        String originId = null;
                        if (originIds != null)
                                originId = originIds.get(new Integer(structId));
                        
                        if (rs.wasNull())
                        {
                            structId = 0;
                        }
                        int chanId = rs.getInt(5);
                        if (rs.wasNull())
                        {
                            chanId = 0;
                        }
                        // only use channel attributes ignoring any others.
                        // we should never get any others in here unless there
                        // is db corruption and since all are flushed when
                        // writting back to the db it should be self correcting
                        // if it ever does occur somehow.
                        if (param_type == 3)
                        {
                            // channel attribute
                            String channelStructId = null;
                            if ( originId != null )
                                channelStructId = originId;
                            else
                                channelStructId = getStructId(structId,chanId);
                            tsup.setChannelAttributeValue(channelStructId, rs.getString(2), rs.getString(3));
                        }
                    }
                } finally
                {
                    close(rs);
                }
            } finally
            {
                close(pstmt3);
            }
        } finally
        {
            RDBMServices.releaseConnection(con);
        }
        return tsup;
    }



    @Override
    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences( IPerson person, int profileId, int stylesheetId)
        throws Exception
    {

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

        FragmentActivator activator = this.getFragmentActivator();

        // regular user, find which layouts apply and include their set prefs
        final List<FragmentDefinition> fragments = configurationLoader.getFragments();
        if ( fragments != null )
        {
            for (final FragmentDefinition fragmentDefinition : fragments) {
                if ( fragmentDefinition.isApplicable(person) ) {
                    loadIncorporatedPreferences( person, STRUCT, ssup,
                            activator.getUserView(fragmentDefinition).structUserPrefs );
                }
            }
        }

        if (LOG.isDebugEnabled())
            LOG.debug("***** " + person.getAttribute( "username" )
              + "'s StructureStylesheetUserPrefereneces\n" +
              showFolderAttribs( ssup ) +
              showChannelAttribs( ssup ) );

        return ssup;
    }

    @Override
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences( IPerson person, int profileId, int stylesheetId)
        throws Exception
    {

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

        FragmentActivator activator = this.getFragmentActivator();

        // regular user, find which layouts apply and include their set prefs
        final List<FragmentDefinition> fragments = configurationLoader.getFragments();
        if ( fragments != null )
        {
            for (final FragmentDefinition fragmentDefinition : fragments) {
                if ( fragmentDefinition.isApplicable(person) ) {
                    loadIncorporatedPreferences( person, THEME, tsup,
                            activator.getUserView(fragmentDefinition).themeUserPrefs);
                }
            }
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

        FragmentActivator activator = this.getFragmentActivator();

        // make a copy so the original is unchanged for the user
        try
        {
            UserProfile profile = getUserProfileByFname(person, "default");
            ssup = new DistributedUserPreferences(
                    (StructureStylesheetUserPreferences) ssup);
            final UserView userView = activator.getUserView(ownedFragment);
            UserView view = new UserView(userView.getUserId(), profile, 
                        userView.layout, ssup, userView.themeUserPrefs);
            activator.fragmentizeSSUP(view, ownedFragment);
            activator.setUserView(ownedFragment.getOwnerId(), view);
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
        Element element = null;
        try {
            XPathFactory fac = XPathFactory.newInstance();
            XPath xp = fac.newXPath();
            element = (Element) xp.evaluate("//*[@ID = '" + incdId + "']", PLF, XPathConstants.NODE);
        } catch (XPathExpressionException xpee) {
            throw new RuntimeException(xpee);
        }
        if ( element == null ) {
            log.warn("The specified folderId was not found in the user's PLF:  " + incdId);
            return null;            
        }
        Attr attr = element.getAttributeNode( Constants.ATT_PLF_ID );
        if ( attr == null )
            return null;
        return attr.getValue();
    }

    @Override
    protected Element getStructure(Document doc, LayoutStructure ls) throws Exception {
        Element structure = null;

        // handle migration of legacy namespace
        String type = ls.getType();
        if (type != null && type.startsWith(Constants.LEGACY_NS))
            type = Constants.NS + type.substring(Constants.LEGACY_NS.length());

  if (ls.isChannel()) {
    IChannelDefinition channelDef = channelRegistryStore.getChannelDefinition(ls.getChanId());
    if (channelDef != null && channelApproved(channelDef.getApprovalDate())) {
        if (localeAware) {
            channelDef.setLocale(ls.getLocale()); // for i18n by Shoji
            }
      structure = channelDef.getDocument(doc, channelPrefix + ls.getStructId());
    } else {
        // Create an error channel if channel is missing or not approved
        String missingChannel = "Unknown";
        if (channelDef != null) {
            missingChannel = channelDef.getName();
        }
        structure = MissingChannelDefinition.INSTANCE.getDocument(doc, channelPrefix + ls.getStructId());
//        structure = MissingChannelDefinition.INSTANCE.getDocument(doc, channelPrefix + ls.getStructId(),
//                "The '" + missingChannel + "' channel is no longer available. " +
//                "Please remove it from your layout.",
//                -1);
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
              /*
               * do traditional override processing. some explanation is in
               * order. The structure element was created by the
               * ChannelDefinition and only contains parameter children if the
               * definition had defined parameters. These are checked for each
               * layout loaded parameter as found in LayoutStructure.parameters.
               * If a name match is found then we need to see if overriding is
               * allowed and if so we set the value on the child parameter
               * element. At that point we are done with that version loaded
               * from the layout so we remove it from the in-memory set of
               * parameters that are being merged-in. Then, after all such have
               * been checked against those added by the channel definition we
               * add in any remaining as adhoc, unregulated parameters.
               */
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
        structure.setIdAttribute(Constants.ATT_ID, true);
        return structure;
    }

    @Override
    protected int saveStructure(
            Node node,
            PreparedStatement structStmt,
            PreparedStatement parmStmt)
            throws Exception
        {
            if (node == null)
            { // No more
                return 0;
            }
            if (node.getNodeName().equals("parameter")) {
                //parameter, skip it and go on to the next node
                return saveStructure(node.getNextSibling(), structStmt, parmStmt);
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
                LOG.debug(structStmt.toString());
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
                        LOG.debug(parmStmt.toString());
                    parmStmt.executeUpdate();
                }
            }
            NodeList parameters = node.getChildNodes();
            if (parameters != null && isChannel)
            {
                IChannelDefinition channelDef = channelRegistryStore.getChannelDefinition(chanId);
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
                            IChannelParameter cp = channelDef.getParameter(parmName);
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

    public static Document getPLF( IPerson person )
    throws PortalException
    {
        try
        {
            return (Document) person.getAttribute( Constants.PLF );
        }
        catch ( Exception ex )
        {
            throw new PortalException( ex );
        }
    }

    @Override
    public void setStructureStylesheetUserPreferences( IPerson person,
                                                       int profileId,
                                                       StructureStylesheetUserPreferences ssup )
        throws Exception
    {
        DistributedUserPreferences dssup = (DistributedUserPreferences) ssup;
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
            con.setAutoCommit(false);
            try
            {
                // before writing out params clean out old values
                deleteFromUpSsUserParm(con, userId, profileId, stylesheetId,1);

                // write out params only if specified in stylesheet's .sdf file
                for (Enumeration e = ssup.getParameterValues().keys(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    if (ssDesc.containsParameterName(pName) &&
                        ! ssDesc.getStylesheetParameterDefaultValue(pName)
                            .equals(ssup.getParameterValue(pName)))
                    {
                        //String pNameEscaped = RDBMServices.sqlEscape(pName);
                        String sQuery = "INSERT INTO UP_SS_USER_PARM (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,PARAM_NAME,PARAM_VAL) VALUES (?,?,?,1,?,?)";
                        final PreparedStatement pstmt2 = con.prepareStatement(sQuery);
                        try {
                            pstmt2.setInt(1,userId);
                            pstmt2.setInt(2,profileId);
                            pstmt2.setInt(3,stylesheetId);
                            pstmt2.setString(4,pName);
                            pstmt2.setString(5,ssup.getParameterValue(pName));
                            if (LOG.isDebugEnabled())
                                LOG.debug(sQuery);
                            pstmt2.executeUpdate();
                        }
                        finally {
                            close(pstmt2);
                        }
                    }
                }

              
                // now before writing out folders and channels clean out old values
                deleteFromUpSsUserAtts(con, userId, profileId, stylesheetId,1);

                // write out folder attributes
                for (Enumeration e = ssup.getFolders(); e.hasMoreElements();) {
                    String folderId = (String)e.nextElement();
                    String plfId = folderId;

                    if ( folderId.startsWith( Constants.FRAGMENT_ID_USER_PREFIX ) ) // icorporated node
                        plfId = getPlfId( PLF, folderId );
                    if ( plfId == null ) {
                        //couldn't translate, skip
                        log.warn("Unable to translate the specified folderId " +
                        		    "to a folder on the PLF:  " + folderId);
                        continue;
                    }

                    for (Enumeration attre = ssup.getFolderAttributeNames(); attre.hasMoreElements();) {
                        String pName = (String)attre.nextElement();
                        String pValue = ssup.getDefinedFolderAttributeValue(folderId, pName);

                        /*
                         * Persist folder attributes defined in the stylesheet
                         * description only if the user value is non null and
                         * there is no default or the user value
                         * differs from the default.
                         */
                        if (ssDesc.containsFolderAttribute(pName))
                        {
                            String deflt = dssup
                            .getDefaultFolderAttributeValue(folderId, pName);
                            if(pValue != null && (deflt == null ||
                                    ! pValue.equals(deflt)))
                                insertIntoUpSsUserAtts(con, userId,
                                        profileId, stylesheetId, 1,
                                        plfId, 2, pName, pValue);
                        }
                    }
                }
                // write out channel attributes
                for (Enumeration e = ssup.getChannels(); e.hasMoreElements();) {
                    String channelId = (String)e.nextElement();
                    String plfId = channelId;

                    if ( plfId.startsWith( Constants.FRAGMENT_ID_USER_PREFIX ) ) // icorporated node
                        plfId = getPlfId( PLF, channelId );
                    if ( plfId == null ) //couldn't translate, skip
                        continue;

                    for (Enumeration attre = ssup.getChannelAttributeNames(); attre.hasMoreElements();) {
                        String pName = (String)attre.nextElement();
                        String pValue = ssup.getDefinedChannelAttributeValue(channelId, pName);

                        /*
                         * Persist channel attributes defined in the stylesheet
                         * description only if the user value is non null and
                         * there is no default or the user value
                         * differs from the default.
                         */
                        if (ssDesc.containsChannelAttribute(pName))
                        {
                            String deflt = dssup
                            .getDefaultChannelAttributeValue(channelId, pName);
                            if(pValue != null && (deflt == null ||
                                    ! pValue.equals(deflt)))
                                insertIntoUpSsUserAtts(con, userId,
                                        profileId, stylesheetId, 1,
                                        plfId, 3, pName, pValue);
                        }
                    }
                }
                // Commit the transaction
                RDBMServices.commit(con);
                if (this.fragmentActivatorCreator.isCreated()) {
                    // We want to update cached fragment SSUPs, but not at 
                    // the cost of triggering fragment activation;  if we 
                    // activate fragments while fragment layouts are being 
                    // imported we'll choke...
                    updateFragmentSSUP( person, (DistributedUserPreferences) ssup);
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Problem occurred ", e);
                // Roll back the transaction
                RDBMServices.rollback(con);
                throw new Exception("Exception setting Structure Sylesheet " +
                        "User Preferences",e);
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
    }

    @Override
    public void setThemeStylesheetUserPreferences (IPerson person,
            int profileId, ThemeStylesheetUserPreferences tsup)
    throws Exception {
        DistributedUserPreferences dtsup = (DistributedUserPreferences) tsup;
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
            con.setAutoCommit(false);
            //Statement pstmt = null;
            try {
                // before writing out params clean out old values
                deleteFromUpSsUserParm(con, userId, profileId, stylesheetId,2);

                // write out params only if defined in stylesheet's .sdf file
                // and user's value differs from default
                for (Enumeration e = tsup.getParameterValues().keys(); e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    if (tsDesc.containsParameterName(pName) &&
                        ! tsDesc.getStylesheetParameterDefaultValue(pName)
                            .equals(tsup.getParameterValue(pName)))
                    {
                        //String pNameEscaped = RDBMServices.sqlEscape(pName);
                        String sQuery = "INSERT INTO UP_SS_USER_PARM (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,PARAM_NAME,PARAM_VAL) VALUES (?,?,?,2,?,?)";
                        final PreparedStatement pstmt2 = con.prepareStatement(sQuery);
                        try {
                            pstmt2.setInt(1,userId);
                            pstmt2.setInt(2,profileId);
                            pstmt2.setInt(3,stylesheetId);
                            pstmt2.setString(4, pName);
                            pstmt2.setString(5,tsup.getParameterValue(pName));
                            if (LOG.isDebugEnabled())
                                LOG.debug(sQuery + "VALUE " + userId + "," + profileId + "," + stylesheetId + "," + pName + "," + tsup.getParameterValue(pName));
                            pstmt2.executeUpdate();
                        }
                        finally {
                            close(pstmt2);
                        }
                    }
                }
                // now before writing out channel atts clean out old values
                deleteFromUpSsUserAtts(con, userId, profileId, stylesheetId,2);

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
                        String pValue = tsup.getDefinedChannelAttributeValue(channelId, pName);

                        /*
                         * Persist channel attributes defined in the stylesheet
                         * description only if the user value is non null and
                         * there is no default or the user value
                         * differs from the default.
                         */
                        if (tsDesc.containsChannelAttribute(pName))
                        {
                            String deflt = dtsup
                            .getDefaultChannelAttributeValue(channelId, pName);
                            if(pValue != null && (deflt == null ||
                                    ! pValue.equals(deflt)))
                                insertIntoUpSsUserAtts(con, userId,
                                        profileId, stylesheetId, 2,
                                        plfChannelId, 3, pName, pValue);
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
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
    }


    private static final String INSERT__INTO__UP_SS_USER_ATTS
    = "INSERT INTO UP_SS_USER_ATTS " +
            "(USER_ID," +
            "PROFILE_ID," +
            "SS_ID," +
            "SS_TYPE," + // 1=structure, 2=theme
            "STRUCT_ID," +
            "PARAM_TYPE," + // 2=folder, 3=channel, 1=sheet parm not allowed
            "PARAM_NAME," +
            "PARAM_VAL) " +
            "VALUES (?,?,?,?,?,?,?,?)";
    

    /**
     * Handles inserts into the UP_SS_USER_ATTS table.
     * 
     * @param pstmt
     * @param userId
     * @param profileId
     * @param stylesheetId
     * @param stylesheetType
     *            (1=structure, 2=theme)
     * @param nodeId
     * @param parmType
     *            (1=channel, 2=folder)
     * @param parmName
     * @param parmValue
     * @throws SQLException
     */
    private static void insertIntoUpSsUserAtts(final Connection con,
            int userId, int profileId, int stylesheetId, int stylesheetType,
            String nodeId, int parmType, String parmName, String parmValue)
            throws SQLException {
        int structId = Integer.parseInt(nodeId.substring(1));

        if (LOG.isDebugEnabled()) {
            LOG.debug(INSERT__INTO__UP_SS_USER_ATTS + ": with values"
                    + " USER_ID=" + userId + ", PROFILE_ID=" + profileId
                    + ", SS_ID=" + stylesheetId + ", SS_TYPE=" + stylesheetType
                    + ", STRUCT_ID=" + structId + ", PARAM_TYPE=" + parmType
                    + ", PARAM_NAME=" + parmName + ", PARAM_VAL=" + parmValue);
        }
        final PreparedStatement pstmt = con
                .prepareStatement(INSERT__INTO__UP_SS_USER_ATTS);
        try {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, profileId);
            pstmt.setInt(3, stylesheetId);
            pstmt.setInt(4, stylesheetType);
            pstmt.setInt(5, structId);
            pstmt.setInt(6, parmType);
            pstmt.setString(7, parmName);
            pstmt.setString(8, parmValue);

            pstmt.execute();
        } finally {
            close(pstmt);
        }
    }

    /**
     * Handles deletes from UP_SS_USER_PARM table.
     * 
     * @param stmt
     * @param userId
     * @param profileId
     * @param stylesheetId
     * @param stylesheetType
     * @throws SQLException
     */
    private static void deleteFromUpSsUserParm(final Connection con,
            int userId, int profileId, int stylesheetId, int stylesheetType)
            throws SQLException {
        /*
         * String sQuery = "DELETE FROM UP_SS_USER_PARM " + "WHERE USER_ID=" +
         * userId + " AND " + "PROFILE_ID=" + profileId + " AND " + "SS_ID=" +
         * stylesheetId + " AND SS_TYPE=" + stylesheetType;
         */
        if (LOG.isDebugEnabled())
            LOG.debug(DELETE_FROM_UP_USER_PARM + " VALUES = " + userId + ","
                    + profileId + "," + stylesheetId + "," + stylesheetType);

        final PreparedStatement pstmt = con
                .prepareStatement(DELETE_FROM_UP_USER_PARM);
        try {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, profileId);
            pstmt.setInt(3, stylesheetId);
            pstmt.setInt(4, stylesheetType);
            pstmt.executeUpdate();
        } finally {
            close(pstmt);
        }
    }

    /**
     * Handles deletes from UP_SS_USER_ATTS table.
     * 
     * @param stmt
     * @param userId
     * @param profileId
     * @param stylesheetId
     * @param stylesheetType
     * @throws SQLException
     */
    private static void deleteFromUpSsUserAtts(final Connection con,
            int userId, int profileId, int stylesheetId, int stylesheetType)
            throws SQLException {
        /*
         * String sQuery = "DELETE FROM UP_SS_USER_ATTS " + "WHERE USER_ID=" +
         * userId + " AND " + "PROFILE_ID=" + profileId + " AND " + "SS_ID=" +
         * stylesheetId + " AND SS_TYPE=" + stylesheetType;
         */
        if (LOG.isDebugEnabled()) {
            LOG.debug(DELETE_FROM_UP_SS_USER_ATTS_SQL + " VALUES = " + userId
                    + "," + profileId + "," + stylesheetId + ","
                    + stylesheetType);
        }
        final PreparedStatement pstmt = con
                .prepareStatement(DELETE_FROM_UP_SS_USER_ATTS_SQL);
        try {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, profileId);
            pstmt.setInt(3, stylesheetId);
            pstmt.setInt(4, stylesheetType);
            pstmt.executeUpdate();
        } finally {
            close(pstmt);
        }
    }
    
    private static void close(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
            LOG.warn("failed to close resultset", e);
        }
    }

    private static void close(Statement statement) {
        try {
            statement.close();
        } catch (SQLException e) {
            LOG.warn("failed to close statement", e);
        }
    }

    private static void rollback(Connection con) {
        LOG.warn("problem encountered, attempting to roll back");
        boolean goodRollBack = true;
        try {
            con.rollback();
        } catch (SQLException e) {
            goodRollBack = false;
        }
        if (goodRollBack) {
            LOG.info("rollback successful");
        } else {
            LOG.warn("rollback failed!");
        }
    }
    

    private static final class MissingChannelDefinition extends XmlGeneratingBaseChannelDefinition {
        public static final IChannelDefinition INSTANCE = new MissingChannelDefinition();
        
        public String getName() {
            return "Missing channel";
        }
        public String getName(String locale) {
            return "Missing channel";
        }
        public int getTimeout() {
            return 20000;
        }
        public String getTitle() {
            return "Missing channel";
        }
        public String getTitle(String locale) {
            return "Missing channel";
        }
        public String getFName() {
            return "DLMStaticMissingChannel";
        }
        
        public void addLocalizedDescription(String locale, String chanDesc) {
        }
        public void addLocalizedName(String locale, String chanName) {
        }
        public void addLocalizedTitle(String locale, String chanTitle) {
        }
        public void addParameter(IChannelParameter parameter) {
        }
        public void addParameter(String name, String value, boolean override) {
        }
        @Deprecated
        public void addParameter(String name, String value, String override) {
        }
        public void clearParameters() {
        }
        public Date getApprovalDate() {
            return null;
        }
        public int getApproverId() {
            return 0;
        }
        public String getDescription() {
            return null;
        }
        public String getDescription(String locale) {
            return null;
        }
        public EntityIdentifier getEntityIdentifier() {
            return null;
        }
        public Date getExpirationDate() {
            return null;
        }
        public int getExpirerId() {
            return 0;
        }
        public int getId() {
            return -1;
        }
        public String getJavaClass() {
            return null;
        }
        public String getLocale() {
            return null;
        }
        public IChannelParameter getParameter(String key) {
            return null;
        }
        public Set<IChannelParameter> getParameters() {
            return null;
        }
        public Map<String, IChannelParameter> getParametersAsUnmodifiableMap() {
            return null;
        }
        public IPortletPreference[] getPortletPreferences() {
            return null;
        }
        public Date getPublishDate() {
            return null;
        }
        public int getPublisherId() {
            return 0;
        }
        @Deprecated
        public int getTypeId() {
            return 0;
        }
        public boolean hasAbout() {
            return false;
        }
        public boolean hasHelp() {
            return false;
        }
        public boolean isEditable() {
            return false;
        }
        public boolean isPortlet() {
            return false;
        }
        public boolean isSecure() {
            return false;
        }
        public IPortletDefinition getPortletDefinition() {
            return null;
        }
        public void removeParameter(IChannelParameter parameter) {
        }
        public void removeParameter(String name) {
        }
        public void replaceParameters(Set<IChannelParameter> parameters) {
        }
        public void replacePortletPreference(List<IPortletPreference> portletPreferences) {
        }
        public void setApprovalDate(Date approvalDate) {
        }
        public void setApproverId(int approvalId) {
        }
        public void setDescription(String descr) {
        }
        public void setEditable(boolean editable) {
        }
        public void setExpirationDate(Date expirationDate) {
        }
        public void setExpirerId(int expirerId) {
        }
        public void setFName(String fname) {
        }
        public void setHasAbout(boolean hasAbout) {
        }
        public void setHasHelp(boolean hasHelp) {
        }
        public void setIsSecure(boolean isSecure) {
        }
        public void setJavaClass(String javaClass) {
        }
        public void setLocale(String locale) {
        }
        public void setName(String name) {
        }
        public void setParameters(Set<IChannelParameter> parameters) {
        }
        public void setPublishDate(Date publishDate) {
        }
        public void setPublisherId(int publisherId) {
        }
        public void setTimeout(int timeout) {
        }
        public void setTitle(String title) {
        }
        public IChannelType getType() {
            return null;
        }
        public void setType(IChannelType channelType) {
        }
		public ChannelLifecycleState getLifecycleState() {
			return null;
		}
    }
}
