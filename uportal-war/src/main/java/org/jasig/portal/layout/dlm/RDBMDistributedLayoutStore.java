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

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.jasig.portal.IUserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.layout.LayoutStructure;
import org.jasig.portal.layout.StructureParameter;
import org.jasig.portal.layout.StylesheetUserPreferencesImpl;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.layout.simple.RDBMUserLayoutStore;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletDescriptorKey;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.om.PortletLifecycleState;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.BrokenSecurityContext;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
    private FragmentActivator fragmentActivator;

    private final Map<String, FragmentNodeInfo> fragmentInfoCache = new ConcurrentHashMap<String, FragmentNodeInfo>();
    
    static final String TEMPLATE_USER_NAME
        = "org.jasig.portal.services.Authentication.defaultTemplateUserName";
    
    final static String DELETE_FROM_UP_SS_USER_ATTS_SQL = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID = ? AND PROFILE_ID = ? AND SS_ID = ? AND SS_TYPE = ?";
    final static String DELETE_FROM_UP_USER_PARM = "DELETE FROM UP_SS_USER_PARM WHERE USER_ID=?  AND PROFILE_ID=? AND SS_ID=? AND SS_TYPE=?";
    
    // Used in Import/Export operations
    private final org.dom4j.DocumentFactory fac = new org.dom4j.DocumentFactory();
    private final DOMReader reader = new DOMReader();
    private final DOMWriter writer = new DOMWriter();
    private Task lookupNoderefTask;
    private Task lookupPathrefTask;
    private IUserIdentityStore identityStore;
    private IStylesheetUserPreferencesDao stylesheetUserPreferencesDao;
    
    @Autowired
    public void setIdentityStore(IUserIdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Autowired
    public void setStylesheetUserPreferencesDao(IStylesheetUserPreferencesDao stylesheetUserPreferencesDao) {
        this.stylesheetUserPreferencesDao = stylesheetUserPreferencesDao;
    }

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
    
    @Autowired
    public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }
    
    @Autowired
    public void setFragmentActivator(FragmentActivator fragmentActivator) {
        this.fragmentActivator = fragmentActivator;
    }

    private FragmentActivator getFragmentActivator() {
        return this.fragmentActivator;
    }

    @Override
    public void cleanFragments() {
        
        FragmentActivator activator = this.getFragmentActivator();

        //get each layout owner
        final List<FragmentDefinition> definitions = configurationLoader.getFragments();
        if ( null != definitions ) {
            
            final Map<IPerson, FragmentDefinition> owners = new HashMap<IPerson, FragmentDefinition>();
            for (final FragmentDefinition fragmentDefinition : definitions) {
                String ownerId = fragmentDefinition.getOwnerId();
                final UserView userView = activator.getUserView(fragmentDefinition);
                if (userView != null) {
                    int userId  = userView.getUserId();
    
                    if ( null != ownerId )
                    {
                        IPerson p = new PersonImpl();
                        p.setID( userId );
                        p.setAttribute( "username", ownerId );
                        owners.put(p, fragmentDefinition);
                    }
                }
            }

            // cycle through each layout owner and clear out their
            // respective layouts so users fragments will be cleared
            for (final Map.Entry<IPerson, FragmentDefinition> ownerEntry : owners.entrySet()) {
                final IPerson person = ownerEntry.getKey();
                final IUserProfile profile;
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
        fragmentInfoCache.clear();
    }
    
    
    protected IStylesheetUserPreferences loadDistributedStylesheetUserPreferences(IPerson person, IUserProfile profile, long stylesheetDescriptorId, Set<String> fragmentNames) {
        if (this.isFragmentOwner(person)) {
            return null;
        }
        
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetDescriptorId);
        final IStylesheetUserPreferences stylesheetUserPreferences = this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(stylesheetDescriptor, person, profile);
        
        final IStylesheetUserPreferences distributedStylesheetUserPreferences = new StylesheetUserPreferencesImpl(stylesheetDescriptorId);
    
        final FragmentActivator fragmentActivator = this.getFragmentActivator();
        
        for (final String fragmentOwnerId : fragmentNames) {
            final FragmentDefinition fragmentDefinition = this.configurationLoader.getFragmentByName(fragmentOwnerId);
        
            //UserView may be missing if the fragment isn't defined correctly
            final UserView userView = fragmentActivator.getUserView(fragmentDefinition);
            if (userView == null) {
                log.warn("No UserView is present for fragment " + fragmentDefinition.getName() + " it will be skipped when loading distributed stylesheet user preferences");
                continue;
            }
            
            //IStylesheetUserPreferences only exist if something was actually set
            final IStylesheetUserPreferences fragmentStylesheetUserPreferences = this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(stylesheetDescriptor, userView.getUserId(), userView.profileId);
            if (fragmentStylesheetUserPreferences == null) {
                continue;
            }
            
            //Get the info needed to DLMify node IDs
            final Element root = userView.layout.getDocumentElement();
            final String labelBase = root.getAttribute( Constants.ATT_ID );
            
            boolean modified = false;
        
            //Copy all of the fragment preferences into the distributed preferences
            final Map<String, Map<String, String>> allLayoutAttributes = fragmentStylesheetUserPreferences.getAllLayoutAttributes();
            for (final Map.Entry<String, Map<String, String>> layoutNodeAttributesEntry : allLayoutAttributes.entrySet()) {
                String nodeId = layoutNodeAttributesEntry.getKey();
                
                if (!nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX)) {
                    nodeId = labelBase + nodeId;
                }
                
                final Map<String, String> layoutAttributes = layoutNodeAttributesEntry.getValue();
                for (final Map.Entry<String, String> layoutAttributesEntry : layoutAttributes.entrySet()) {
                    final String name = layoutAttributesEntry.getKey();
                    final String value = layoutAttributesEntry.getValue();
                    
                    //Fragmentize the nodeId here
                    distributedStylesheetUserPreferences.setLayoutAttribute(nodeId, name, value);
                    
                    //Clean out user preferences data that matches data from the fragment.
                    if (stylesheetUserPreferences != null) {
                        final String userValue = stylesheetUserPreferences.getLayoutAttribute(nodeId, name);
                        if (userValue != null && userValue.equals(value)) {
                            stylesheetUserPreferences.removeLayoutAttribute(nodeId, name);
                            EditManager.removePreferenceDirective(person, nodeId, name);
                            modified = true;
                        }
                    }
                }
            }
            
            if (modified) {
                this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
            }
        }
        
        return distributedStylesheetUserPreferences;
    }
    
    @Override
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
    public DistributedUserLayout getUserLayout (IPerson person,
                                   IUserProfile profile)
        
    {

        DistributedUserLayout layout = _getUserLayout( person, profile );

        return layout;
    }
    
    private boolean layoutExistsForUser(IPerson person) {
        
        // Assertions.
        if (person == null) {
            String msg = "Argument 'person' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        final int struct_count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM up_layout_struct WHERE user_id = ?", person.getID());
        return struct_count == 0 ? false : true;
        
    }
    
    @SuppressWarnings("unchecked")
    public org.dom4j.Element exportLayout(IPerson person, IUserProfile profile) {
        
        if (!layoutExistsForUser(person)) {
            return null;
        }
        
        org.dom4j.Document layoutDoc = null;
        try {
            Document layoutDom = _safeGetUserLayout(person, profile);
            person.setAttribute(Constants.PLF, layoutDom);
            layoutDoc = reader.read(layoutDom);
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
        final int structureStylesheetId = profile.getStructureStylesheetId();
        addStylesheetUserPreferencesAttributes(person,
                profile,
                layoutDoc,
                structureStylesheetId,
                "structure");
        
        final int themeStylesheetId = profile.getThemeStylesheetId();
        addStylesheetUserPreferencesAttributes(person,
                profile,
                layoutDoc,
                themeStylesheetId,
                "theme");
                
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
                        "dlm:plfID",
                        "dlm:moveAllowed",
                        "dlm:deleteAllowed"
                    });
        Iterator<org.dom4j.Element> channels = (Iterator<org.dom4j.Element>) layoutDoc.selectNodes("//channel").iterator();
        while (channels.hasNext()) {
            org.dom4j.Element oldCh = channels.next();
            org.dom4j.Element parent = oldCh.getParent();
            org.dom4j.Element newCh = fac.createElement("channel");
            for (String aName : channelAttributeWhitelist) {
                org.dom4j.Attribute a = (org.dom4j.Attribute) oldCh.selectSingleNode("@" + aName);
                if (a != null) {
                    newCh.addAttribute(a.getQName(), a.getValue());
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

    protected void addStylesheetUserPreferencesAttributes(IPerson person, IUserProfile profile,
            org.dom4j.Document layoutDoc, int stylesheetId, String attributeType) {
        final IStylesheetDescriptor structureStylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetId);
        
        final IStylesheetUserPreferences ssup = this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(structureStylesheetDescriptor, person, profile);
        
        final Map<String, Map<String, String>> allLayoutAttributes = ssup.getAllLayoutAttributes();
        for (final Entry<String, Map<String, String>> nodeEntry : allLayoutAttributes.entrySet()) {
            final String nodeId = nodeEntry.getKey();
            final Map<String, String> attributes = nodeEntry.getValue();
            
            final org.dom4j.Element element = layoutDoc.elementByID(nodeId);
            if (element == null) {
                this.log.warn("No layout node with id '" + nodeId + "' found attributes will be ignored: " + attributes);
                continue;
            }
            
            for (final Entry<String, String> attributeEntry : attributes.entrySet()) {
                final String name = attributeEntry.getKey();
                final String value = attributeEntry.getValue();
                
                if (log.isDebugEnabled()) {
                    log.debug("Adding structure folder attribute:  name=" + name + ", value=" + value);
                }
                org.dom4j.Element structAttrElement = fac.createElement(attributeType + "-attribute");
                org.dom4j.Element nameAttribute = structAttrElement.addElement("name");
                nameAttribute.setText(name);
                org.dom4j.Element valueAttribute = structAttrElement.addElement("value");
                valueAttribute.setText(value);
                element.elements().add(0, structAttrElement);
            }
        }
    }
        
    @Override
    @SuppressWarnings("unchecked")
    @Transactional
    public void importLayout(org.dom4j.Element layout) {
        
        String ownerUsername = layout.valueOf("@username");
        IPerson person = null;
        IUserProfile profile = null;
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
        for (Iterator<org.dom4j.Element> it = (Iterator<org.dom4j.Element>) layout.selectNodes("//channel").iterator(); it.hasNext();) {
            org.dom4j.Element c = it.next();
            final String fname = c.valueOf("@fname");
            IPortletDefinition cd = this.portletDefinitionRegistry.getPortletDefinitionByFname(fname);
            if (cd == null) {
                throw new IllegalArgumentException("No published channel for fname=" + fname + " referenced by layout for " + ownerUsername);
            }
            c.addAttribute("chanID", String.valueOf(cd.getPortletDefinitionId().getStringId()));
        }
        
        // (2) Restore locale info...
        // (This step doesn't appear to be needed for imports)

        // (1) Process structure & theme attributes...
        Document layoutDom = null;
        try {

            // Structure Attributes.
            boolean saSet = false;
            
            final int structureStylesheetId = profile.getStructureStylesheetId();
            loadStylesheetUserPreferencesAttributes(person,
                    profile,
                    layout,
                    structureStylesheetId,
                    "structure");
            
            final int themeStylesheetId = profile.getThemeStylesheetId();
            loadStylesheetUserPreferencesAttributes(person,
                    profile,
                    layout,
                    themeStylesheetId,
                    "theme");

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

    protected void loadStylesheetUserPreferencesAttributes(
            IPerson person, IUserProfile profile, org.dom4j.Element layout,
            final int structureStylesheetId, final String nodeType) {
        
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptor(structureStylesheetId);
        final List<org.dom4j.Element> structureAttributes = layout.selectNodes("//" + nodeType + "-attribute");
        
        IStylesheetUserPreferences ssup = this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(stylesheetDescriptor, person, profile);
        if (structureAttributes.isEmpty()) {
            if (ssup != null) {
                this.stylesheetUserPreferencesDao.deleteStylesheetUserPreferences(ssup);
            }
        }
        else {
            if (ssup == null) {
                ssup = this.stylesheetUserPreferencesDao.createStylesheetUserPreferences(stylesheetDescriptor, person, profile);
            }
            
            ssup.clearAllLayoutAttributes();
            
            for (final org.dom4j.Element structureAttribute : structureAttributes) {
                final org.dom4j.Element layoutElement = structureAttribute.getParent();
                final String nodeId = layoutElement.valueOf("@ID");
                
                final String name = structureAttribute.valueOf("name");
                final String value = structureAttribute.valueOf("value");
                
                ssup.setLayoutAttribute(nodeId, name, value);
                
                // Remove the layout attribute element or DLM fails
                layoutElement.remove(structureAttribute);
            }
            
            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(ssup);
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
            tr.setAttribute("userLayoutStore", this);
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
            tr.setAttribute("userLayoutStore", this);
            if (fname != null) {
                tr.setAttribute("FNAME", fname);
            }
            if (isStructRef) {
                tr.setAttribute("IS_STRUCT_REF", Boolean.TRUE);
            }
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
     * @
     */
    private Document _safeGetUserLayout(IPerson person, IUserProfile profile)
            
    {
        Document layoutDoc = super.getPersonalUserLayout(person, profile);
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
    private DistributedUserLayout _getUserLayout (IPerson person,
                                     IUserProfile profile)
        
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
            ILF = (Document)PLF.cloneNode(true);

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
            return new DistributedUserLayout(ILF);
        }

        return getCompositeLayout( person, profile );
    }

    /**
     * Convenience method for fragment activator to obtain raw layouts for
     * fragments during initialization.
     */
    public Document getFragmentLayout (IPerson person,
                                IUserProfile profile)
        
    {
        return _safeGetUserLayout( person, profile );
    }
    
    /**
     * Generates a new struct id for directive elements that dlm places in
     * the PLF version of the layout tree. These elements are atifacts of the
     * dlm storage model and used during merge but do not appear in the user's
     * composite view.
     */
    @Override
    public String getNextStructDirectiveId (IPerson person)  {
        return  super.getNextStructId(person, Constants.DIRECTIVE_PREFIX );
    }

    /**
       Replaces the layout Document stored on a fragment definition with a new
       version. This is called when a fragment owner updates their layout.
     */
    private void updateCachedLayout( Document layout,
                                     IUserProfile profile,
                                     FragmentDefinition fragment )
    {
        // need to make a copy that we can fragmentize
        layout = (Document)layout.cloneNode(true);

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
                                      layout);
        try
        {
            activator.fragmentizeLayout( view, fragment );
            activator.setUserView(fragment.getOwnerId(), view);
            this.fragmentInfoCache.clear();
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

    @Override
    public boolean isFragmentOwner(IPerson person)
    {
        return getOwnedFragment(person) != null;
    }
    
    @Override
    public boolean isFragmentOwner(String username) {

        boolean rslt = false;  // default
        
        final List<FragmentDefinition> definitions = configurationLoader.getFragments();
        if (definitions != null) {
            for (final FragmentDefinition fragmentDefinition : definitions) {
                if (fragmentDefinition.getOwnerId().equals(username)) {
                    rslt = true;
                    break;
                }
            }
        }

        return rslt;

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
    private DistributedUserLayout getCompositeLayout( IPerson person,
                                         IUserProfile profile )
        
    {
        final Set<String> fragmentNames = new LinkedHashSet<String>();
        final List<Document> applicables = new LinkedList<Document>();

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
                    final UserView userView = activator.getUserView(fragmentDefinition);
                    applicables.add( userView.layout );
                    fragmentNames.add(fragmentDefinition.getName());
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
                    " immediately after loading\n" + XmlUtilitiesImpl.toString(PLF));

        Document ILF = ILFBuilder.constructILF( PLF, applicables, person );
        person.setAttribute( Constants.PLF, PLF );
        IntegrationResult result = new IntegrationResult();
        PLFIntegrator.mergePLFintoILF( PLF, ILF, result );
        if (LOG.isDebugEnabled())
        {
            LOG.debug("PLF for " + person.getAttribute(IPerson.USERNAME) +
                    " after MERGING\n" + XmlUtilitiesImpl.toString(PLF));
            LOG.debug("ILF for " + person.getAttribute(IPerson.USERNAME) +
                    " after MERGING\n" + XmlUtilitiesImpl.toString(ILF));
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
        
        final int structureStylesheetId = profile.getStructureStylesheetId();
        final IStylesheetUserPreferences distributedStructureStylesheetUserPreferences = this.loadDistributedStylesheetUserPreferences(person, profile, structureStylesheetId, fragmentNames);
        
        final int themeStylesheetId = profile.getThemeStylesheetId();
        final IStylesheetUserPreferences distributedThemeStylesheetUserPreferences = this.loadDistributedStylesheetUserPreferences(person, profile, themeStylesheetId, fragmentNames);


        return new DistributedUserLayout(ILF, fragmentNames, distributedStructureStylesheetUserPreferences, distributedThemeStylesheetUserPreferences);
    }

    /**
       This method overrides the same method in the super class to persist
       only layout information stored in the user's person layout fragment
       or PLF. If this person is a layout owner then their changes are pushed
       into the appropriate layout fragment.
     */
    public void setUserLayout (IPerson person, IUserProfile profile,
                               Document layoutXML, boolean channelsAdded)
      
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
    @Override
    public void setUserLayout (IPerson person, IUserProfile profile,
                        Document layoutXML, boolean channelsAdded,
                        boolean updateFragmentCache)
      
    {
        Document plf = (Document) person.getAttribute( Constants.PLF );
        if (LOG.isDebugEnabled())
            LOG.debug("PLF for " + person.getAttribute(IPerson.USERNAME) +
                    "\n" + XmlUtilitiesImpl.toString(plf));
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

    @Override
    public FragmentChannelInfo getFragmentChannelInfo(String sId)
    {
        FragmentNodeInfo node = getFragmentNodeInfo(sId);

        if (node != null && (node instanceof FragmentChannelInfo))
            return (FragmentChannelInfo) node;
        return null;
    }

    @Override
    public FragmentNodeInfo getFragmentNodeInfo(String sId)
    {
        // grab local pointers to variables subject to change at any time
        Map<String, FragmentNodeInfo> infoCache = fragmentInfoCache;
        final List<FragmentDefinition> fragments = configurationLoader.getFragments();

        FragmentActivator activator = this.getFragmentActivator();

        FragmentNodeInfo info = infoCache.get(sId);

        if (info == null)
        {
            for (final FragmentDefinition fragmentDefinition : fragments) {
                final UserView userView = activator.getUserView(fragmentDefinition);
                if (userView == null) {
                    log.warn("No UserView is present for fragment " + fragmentDefinition.getName() + " it will be skipped when fragment node information");
                    continue;
                }
                
                Element node = userView.layout.getElementById(sId);
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
    protected Element getStructure(Document doc, LayoutStructure ls)  {
        Element structure = null;

        // handle migration of legacy namespace
        String type = ls.getType();
        if (type != null && type.startsWith(Constants.LEGACY_NS))
            type = Constants.NS + type.substring(Constants.LEGACY_NS.length());

  if (ls.isChannel()) {
	  IPortletDefinition channelDef = portletDefinitionRegistry.getPortletDefinition(String.valueOf(ls.getChanId()));
    if (channelDef != null && channelApproved(channelDef.getApprovalDate())) {
      structure = getElementForChannel(doc, channelPrefix + ls.getStructId(), channelDef, ls.getLocale());
    } else {
        // Create an error channel if channel is missing or not approved
        String missingChannel = "Unknown";
        if (channelDef != null) {
            missingChannel = channelDef.getName();
        }
        structure = getElementForChannel(doc, channelPrefix + ls.getStructId(), MissingChannelDefinition.INSTANCE, null);
//        structure = MissingChannelDefinition.INSTANCE.getDocument(doc, channelPrefix + ls.getStructId());
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
            throws SQLException
        {
            if (node == null)
            { // No more
                return 0;
            }
            if (node.getNodeName().equals("parameter")) {
                //parameter, skip it and go on to the next node
                return saveStructure(node.getNextSibling(), structStmt, parmStmt);
            }
            if (!(node instanceof Element)) {
                return 0;
            }
                    
            Element structure = (Element) node;

            if (LOG.isDebugEnabled())
                LOG.debug("saveStructure XML content: "
                    + XmlUtilitiesImpl.toString(node));

            // determine the struct_id for storing in the db. For incorporated nodes in
            // the plf their ID is a system-wide unique ID while their struct_id for
            // storing in the db is cached in a dlm:plfID attribute.
            int saveStructId = -1;
            String plfID = structure.getAttribute(Constants.ATT_PLF_ID);

            if (!plfID.equals("")) {
                saveStructId = Integer.parseInt(plfID.substring(1));
            }
            else {
                final String id = structure.getAttribute("ID");
                saveStructId = Integer.parseInt(id.substring(1));
            }

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
            	IPortletDefinition channelDef = portletDefinitionRegistry.getPortletDefinition(String.valueOf(chanId));
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
                            IPortletDefinitionParameter cp = channelDef.getParameter(parmName);
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
    
    private Element getElementForChannel(Document doc, String chanId, IPortletDefinition def, String locale) {
        Element channel = doc.createElement("channel");

        // the ID attribute is the identifier for the Channel element
        channel.setAttribute("ID", chanId);
        channel.setIdAttribute("ID", true);

        channel.setAttribute("chanID", def.getPortletDefinitionId().getStringId());
        channel.setAttribute("timeout", String.valueOf(def.getTimeout()));
        if (locale != null) {
            channel.setAttribute("name", def.getName(locale));
            channel.setAttribute("title", def.getTitle(locale));
            channel.setAttribute("description", def.getDescription(locale));
            channel.setAttribute("locale", locale);
        } else {
            channel.setAttribute("name", def.getName());
            channel.setAttribute("title", def.getTitle());
            channel.setAttribute("description", def.getDescription());
        }
        channel.setAttribute("fname", def.getFName());

        // chanClassArg is so named to highlight that we are using the argument
        // to the method rather than the instance variable chanClass
        channel.setAttribute("typeID", String.valueOf(def.getType().getId()));

        for (IPortletDefinitionParameter param : def.getParameters()) {
            Element parameter = doc.createElement("parameter");
            parameter.setAttribute("name", param.getName());
            parameter.setAttribute("value", param.getValue());
            channel.appendChild(parameter);
        }

        return channel;

    }
    
    private static final class MissingChannelDefinition implements IPortletDefinition {
        public static final IPortletDefinition INSTANCE = new MissingChannelDefinition();
        
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
        
        @Override
        public String getDataId() {
            return null;
        }
        @Override
        public String getDataTitle() {
            return this.getName();
        }
        @Override
        public String getDataDescription() {
            return this.getDescription();
        }
        public void addLocalizedDescription(String locale, String chanDesc) {
        }
        public void addLocalizedName(String locale, String chanName) {
        }
        public void addLocalizedTitle(String locale, String chanTitle) {
        }
        public void addParameter(IPortletDefinitionParameter parameter) {
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
        public IPortletDefinitionParameter getParameter(String key) {
            return null;
        }
        public Set<IPortletDefinitionParameter> getParameters() {
            return null;
        }
        public Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap() {
            return null;
        }
        public Date getPublishDate() {
            return null;
        }
        public int getPublisherId() {
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
        public void removeParameter(IPortletDefinitionParameter parameter) {
        }
        public void removeParameter(String name) {
        }
        public void replaceParameters(Set<IPortletDefinitionParameter> parameters) {
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
        public void setName(String name) {
        }
        public void setParameters(Set<IPortletDefinitionParameter> parameters) {
        }
        public void setPublishDate(Date publishDate) {
        }
        public void setPublisherId(int publisherId) {
        }
        public void setTimeout(int timeout) {
        }
        public void setTitle(String title) {
        }
        public IPortletType getType() {
            return new MissingPortletType();
        }
        public void setType(IPortletType channelType) {
        }
		public PortletLifecycleState getLifecycleState() {
			return null;
		}
		public IPortletDefinitionId getPortletDefinitionId() {
			return new MissingPortletDefinitionId();
		}
		public IPortletPreferences getPortletPreferences() {
			return null;
		}
		public void setPortletPreferences(IPortletPreferences portletPreferences) {
		}
		public void addParameter(String name, String value) {
		}
		@Override
		public void setPortletPreferences(
				List<IPortletPreference> portletPreferences) {
			// TODO Auto-generated method stub
			
		}
        @Override
        public IPortletDescriptorKey getPortletDescriptorKey() {
            // TODO Auto-generated method stub
            return null;
        }

    }
    
    private static final class MissingPortletDefinitionId implements IPortletDefinitionId {

		public String getStringId() {
			return "-1";
		}

    	
    }

    private static final class MissingPortletType implements IPortletType {

		public int getId() {
			// TODO Auto-generated method stub
			return -1;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getCpdUri() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setDescription(String descr) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCpdUri(String cpdUri) {
			// TODO Auto-generated method stub
			
		}

    	
    }

}
