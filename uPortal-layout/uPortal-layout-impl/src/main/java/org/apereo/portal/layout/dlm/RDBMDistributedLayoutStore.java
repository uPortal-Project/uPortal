/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.dlm;

import com.google.common.cache.Cache;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import net.sf.ehcache.Ehcache;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.*;
import org.apereo.portal.io.xml.IPortalDataHandlerService;
import org.apereo.portal.jdbc.RDBMServices;
import org.apereo.portal.layout.LayoutStructure;
import org.apereo.portal.layout.StructureParameter;
import org.apereo.portal.layout.StylesheetUserPreferencesImpl;
import org.apereo.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.apereo.portal.layout.simple.RDBMUserLayoutStore;
import org.apereo.portal.portlet.dao.IPortletEntityDao;
import org.apereo.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.apereo.portal.properties.PropertiesManager;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.provider.BrokenSecurityContext;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.utils.DocumentFactory;
import org.apereo.portal.utils.IFragmentDefinitionUtils;
import org.apereo.portal.utils.MapPopulator;
import org.apereo.portal.utils.Tuple;
import org.apereo.portal.xml.XmlUtilitiesImpl;
import org.dom4j.Namespace;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class extends RDBMUserLayoutStore and implements instantiating and storing layouts that
 * conform to the design of the Distributed Layout Management system. These layouts consist of two
 * types: layout fragments that are the layouts owned by a user specified in in a
 * fragment-definition.xml file, and composite view layouts which represent regular users with zero
 * or more UI elements incorporated from layout fragments.
 *
 * @since 2.5
 */
public class RDBMDistributedLayoutStore extends RDBMUserLayoutStore {

    private static final Pattern VALID_PATHREF_PATTERN = Pattern.compile(".+\\:/.+");
    private static final String BAD_PATHREF_MESSAGE = "## DLM: ORPHANED DATA ##";
    public static final String DEFAULT_LAYOUT_OWNER_PROPERTY =
            "org.apereo.portal.layout.dlm.defaultLayoutOwner";

    private FragmentActivator fragmentActivator;

    private Ehcache fragmentNodeInfoCache;

    private boolean errorOnMissingPortlet = true;
    private boolean errorOnMissingUser = true;

    // Used in Import/Export operations
    private final org.dom4j.DocumentFactory fac = new org.dom4j.DocumentFactory();
    private final ThreadLocal<DOMReader> reader =
            new ThreadLocal<DOMReader>() {
                @Override
                protected DOMReader initialValue() {
                    return new DOMReader();
                }
            };
    private final ThreadLocal<DOMWriter> writer =
            new ThreadLocal<DOMWriter>() {
                @Override
                protected DOMWriter initialValue() {
                    return new DOMWriter();
                }
            };
    private IUserIdentityStore userIdentityStore;
    private IStylesheetUserPreferencesDao stylesheetUserPreferencesDao;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletEntityDao portletEntityDao;
    private IPortalDataHandlerService portalDataHandlerService;
    private IFragmentDefinitionUtils fragmentUtils;

    @Autowired private NodeReferenceFactory nodeReferenceFactory;

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    @Autowired
    public void setPortalDataHandlerService(IPortalDataHandlerService portalDataHandlerService) {
        this.portalDataHandlerService = portalDataHandlerService;
    }

    @Autowired
    public void setPortletEntityDao(@Qualifier("transient") IPortletEntityDao portletEntityDao) {
        this.portletEntityDao = portletEntityDao;
    }

    @Autowired
    public void setIdentityStore(IUserIdentityStore identityStore) {
        this.userIdentityStore = identityStore;
    }

    @Autowired
    public void setStylesheetUserPreferencesDao(
            IStylesheetUserPreferencesDao stylesheetUserPreferencesDao) {
        this.stylesheetUserPreferencesDao = stylesheetUserPreferencesDao;
    }

    @Autowired
    public void setFragmentDefinitionUtils(IFragmentDefinitionUtils utils) {
        this.fragmentUtils = utils;
    }

    @Autowired
    public void setFragmentNodeInfoCache(
            @Qualifier(
                            "org.apereo.portal.layout.dlm.RDBMDistributedLayoutStore.fragmentNodeInfoCache")
                    Ehcache fragmentNodeInfoCache) {
        this.fragmentNodeInfoCache = fragmentNodeInfoCache;
    }

    @Value("${org.apereo.portal.io.layout.errorOnMissingPortlet:true}")
    public void setErrorOnMissingPortlet(boolean errorOnMissingPortlet) {
        this.errorOnMissingPortlet = errorOnMissingPortlet;
    }

    @Value("${org.apereo.portal.io.layout.errorOnMissingUser:true}")
    public void setErrorOnMissingUser(boolean errorOnMissingUser) {
        this.errorOnMissingUser = errorOnMissingUser;
    }

    /**
     * Method for acquiring copies of fragment layouts to assist in debugging. No infrastructure
     * code calls this but channels designed to expose the structure of the cached fragments use
     * this to obtain copies.
     *
     * @return Map
     */
    @Override
    public Map<String, Document> getFragmentLayoutCopies() {
        // since this is only visible in fragment list in administrative portlet, use default portal
        // locale
        final Locale defaultLocale = localeManagerFactory.getPortalLocales().get(0);

        final Map<String, Document> layouts = new HashMap<>();

        final List<FragmentDefinition> definitions = this.fragmentUtils.getFragmentDefinitions();
        for (final FragmentDefinition fragmentDefinition : definitions) {
            final Document layout = DocumentFactory.getThreadDocument();
            final UserView userView =
                    this.fragmentUtils.getUserView(fragmentDefinition, defaultLocale);
            if (userView == null) {
                logger.warn(
                        "No UserView found for FragmentDefinition {}, it will be skipped.",
                        fragmentDefinition.getName());
                continue;
            }
            final Node copy = layout.importNode(userView.getLayout().getDocumentElement(), true);
            layout.appendChild(copy);
            layouts.put(fragmentDefinition.getOwnerId(), layout);
        }
        return layouts;
    }

    @Autowired
    public void setFragmentActivator(FragmentActivator fragmentActivator) {
        this.fragmentActivator = fragmentActivator;
    }

    private IStylesheetUserPreferences loadDistributedStylesheetUserPreferences(
            IPerson person,
            IUserProfile profile,
            long stylesheetDescriptorId,
            Set<String> fragmentNames) {
        final boolean isFragmentOwner = this.isFragmentOwner(person);

        final Locale locale = profile.getLocaleManager().getLocales().get(0);
        final IStylesheetDescriptor stylesheetDescriptor =
                this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetDescriptorId);
        final IStylesheetUserPreferences stylesheetUserPreferences =
                this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(
                        stylesheetDescriptor, person, profile);

        final IStylesheetUserPreferences distributedStylesheetUserPreferences =
                new StylesheetUserPreferencesImpl();

        for (final String fragName : fragmentNames) {
            final FragmentDefinition fragmentDefinition =
                    this.fragmentUtils.getFragmentDefinitionByName(fragName);

            // UserView may be missing if the fragment isn't defined correctly
            final UserView userView = this.fragmentUtils.getUserView(fragmentDefinition, locale);
            if (userView == null) {
                logger.warn(
                        "No UserView is present for fragment {} it will be skipped when loading distributed stylesheet user preferences",
                        fragmentDefinition.getName());
                continue;
            }

            // IStylesheetUserPreferences only exist if something was actually set
            final IStylesheetUserPreferences fragmentStylesheetUserPreferences =
                    this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(
                            stylesheetDescriptor, userView.getUserId(), userView.getProfileId());
            if (fragmentStylesheetUserPreferences == null) {
                continue;
            }

            // Get the info needed to DLMify node IDs
            final Element root = userView.getLayout().getDocumentElement();
            final String labelBase = root.getAttribute(Constants.ATT_ID);

            boolean modified = false;

            // Copy all of the fragment preferences into the distributed preferences
            final Collection<String> allLayoutAttributeNodeIds =
                    fragmentStylesheetUserPreferences.getAllLayoutAttributeNodeIds();
            for (final String fragmentNodeId : allLayoutAttributeNodeIds) {
                final String userNodeId =
                        (isFragmentOwner
                                        || fragmentNodeId.startsWith(
                                                Constants.FRAGMENT_ID_USER_PREFIX))
                                ? fragmentNodeId
                                : labelBase + fragmentNodeId;

                final MapPopulator<String, String> layoutAttributesPopulator = new MapPopulator<>();
                fragmentStylesheetUserPreferences.populateLayoutAttributes(
                        fragmentNodeId, layoutAttributesPopulator);
                final Map<String, String> layoutAttributes = layoutAttributesPopulator.getMap();
                for (final Map.Entry<String, String> layoutAttributesEntry :
                        layoutAttributes.entrySet()) {
                    final String name = layoutAttributesEntry.getKey();
                    final String value = layoutAttributesEntry.getValue();

                    // Fragmentize the nodeId here
                    distributedStylesheetUserPreferences.setLayoutAttribute(
                            userNodeId, name, value);

                    // Clean out user preferences data that matches data from the fragment.
                    // Skip for fragment owners since their user preference data and the fragment
                    // stylesheet user prefs
                    // are identical and removing layout attributes here would affect the fragment
                    // layout.
                    if (stylesheetUserPreferences != null && !isFragmentOwner) {
                        final String userValue =
                                stylesheetUserPreferences.getLayoutAttribute(userNodeId, name);
                        if (userValue != null && userValue.equals(value)) {
                            stylesheetUserPreferences.removeLayoutAttribute(userNodeId, name);
                            EditManager.removePreferenceDirective(person, userNodeId, name);
                            modified = true;
                        }
                    }
                }
            }

            if (modified) {
                this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(
                        stylesheetUserPreferences);
            }
        }

        return distributedStylesheetUserPreferences;
    }

    @Override
    public double getFragmentPrecedence(long id) {
        final List<FragmentDefinition> definitions = this.fragmentUtils.getFragmentDefinitions();

        // must pass through the array looking for the fragment with this
        // index since the array was sorted by precedence and then index
        // within precedence.
        for (final FragmentDefinition fragmentDefinition : definitions) {
            if (fragmentDefinition.getId() == id) {
                return fragmentDefinition.getPrecedence();
            }
        }
        return 0D; // should never get here.
    }

    /**
     * Returns the layout for a user decorated with any specified decorator. The layout returned is
     * a composite layout for non fragment owners and a regular layout for layout owners. A
     * composite layout is made up of layout pieces from potentially multiple incorporated layouts.
     * If no layouts are defined then the composite layout will be the same as the user's personal
     * layout fragment or PLF, the one holding only those UI elements that they own or incorporated
     * elements that they have been allowed to changed.
     */
    @Override
    public DistributedUserLayout getUserLayout(IPerson person, IUserProfile profile) {

        final DistributedUserLayout layout = this._getUserLayout(person, profile);

        return layout;
    }

    /**
     * Provides a {@link Tuple} containing the &quot;fragmentized&quot; version of a DLM fragment
     * owner's layout, together with the username. This version of the layout consistent with what
     * DLM uses internally for fragments, and is created by FragmentActivator.fragmentizeLayout.
     * It's important that the version returned by this method matches what DLM uses internally
     * because it will be used to establish relationships between fragment layout nodes and user
     * customizations of DLM fragments.
     *
     * @param userName The username of the user for whom the layout is retrieved.
     * @param userId The unique identifier of the user.
     * @return A {@link Tuple} containing the username and the "fragmentized" version of the DLM
     *     fragment owner's layout.
     */
    @Override
    public Tuple<String, DistributedUserLayout> getUserLayoutTuple(String userName, int userId) {
        final PersonImpl person = new PersonImpl();
        person.setUserName(userName);
        person.setID(userId);
        person.setSecurityContext(new BrokenSecurityContext());

        final IUserProfile profile =
                this.getUserProfileByFname(person, UserProfile.DEFAULT_PROFILE_FNAME);
        final DistributedUserLayout userLayout = this.getUserLayout(person, profile);

        return new Tuple<>(userName, userLayout);
    }

    private boolean layoutExistsForUser(IPerson person) {

        // Assertions.
        if (person == null) {
            final String msg = "Argument 'person' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        final int struct_count =
                jdbcOperations.queryForObject(
                        "SELECT COUNT(*) FROM up_layout_struct WHERE user_id = ?",
                        Integer.class,
                        person.getID());
        return struct_count == 0 ? false : true;
    }

    @Override
    public org.dom4j.Element exportLayout(IPerson person, IUserProfile profile) {
        org.dom4j.Element layout = getExportLayoutDom(person, profile);

        final int userId = person.getID();
        final String userName = person.getUserName();
        final Set<IPortletEntity> portletEntities =
                this.portletEntityDao.getPortletEntitiesForUser(userId);

        org.dom4j.Element preferencesElement = null;
        for (final Iterator<IPortletEntity> entityItr = portletEntities.iterator();
                entityItr.hasNext(); ) {
            final IPortletEntity portletEntity = entityItr.next();
            final List<IPortletPreference> preferencesList = portletEntity.getPortletPreferences();

            // Only bother with entities that have preferences
            if (!preferencesList.isEmpty()) {
                final String layoutNodeId = portletEntity.getLayoutNodeId();
                final Pathref dlmPathref =
                        nodeReferenceFactory.getPathrefFromNoderef(userName, layoutNodeId, layout);
                if (dlmPathref == null) {
                    logger.warn(
                            "{} in user {}'s layout has no corresponding layout or portlet information and will be ignored",
                            portletEntity,
                            userName);
                    continue;
                }

                for (final IPortletPreference portletPreference : preferencesList) {
                    if (preferencesElement == null) {
                        if (layout == null) {
                            final org.dom4j.Document layoutDoc =
                                    new org.dom4j.DocumentFactory().createDocument();
                            layout = layoutDoc.addElement("layout");
                            layout.addNamespace("dlm", Constants.NS_URI);
                        }
                        preferencesElement = layout.addElement("preferences");
                    }

                    final org.dom4j.Element preferenceEntry =
                            preferencesElement.addElement("entry");
                    preferenceEntry.addAttribute("entity", dlmPathref.toString());
                    preferenceEntry.addAttribute("channel", dlmPathref.getPortletFname());
                    preferenceEntry.addAttribute("name", portletPreference.getName());

                    for (final String value : portletPreference.getValues()) {
                        final org.dom4j.Element valueElement = preferenceEntry.addElement("value");
                        if (value != null) {
                            valueElement.setText(value);
                        }
                    }
                }
            }
        }

        if (layout != null) {
            layout.addAttribute("script", "classpath://org/jasig/portal/io/import-layout_v3-2.crn");
            layout.addAttribute("username", userName);
        }

        return layout;
    }

    private org.dom4j.Element getExportLayoutDom(IPerson person, IUserProfile profile) {
        if (!this.layoutExistsForUser(person)) {
            return null;
        }

        org.dom4j.Document layoutDoc = null;
        try {
            final Document layoutDom = this._safeGetUserLayout(person, profile);
            person.setAttribute(Constants.PLF, layoutDom);
            layoutDoc = this.reader.get().read(layoutDom);
        } catch (final Throwable t) {
            final String msg =
                    "Unable to obtain layout & profile for user '"
                            + person.getUserName()
                            + "', profileId "
                            + profile.getProfileId();
            throw new RuntimeException(msg, t);
        }

        if (logger.isDebugEnabled()) {
            // Write out this version of the layout to the log for dev purposes...
            final StringWriter str = new StringWriter();
            final XMLWriter xml = new XMLWriter(str, new OutputFormat("  ", true));
            try {
                xml.write(layoutDoc);
                xml.close();
            } catch (final Throwable t) {
                throw new RuntimeException(
                        "Failed to write the layout for user '"
                                + person.getUserName()
                                + "' to the DEBUG log",
                        t);
            }
            logger.debug(
                    "Layout for user: {}\n{}", person.getUserName(), str.getBuffer().toString());
        }

        /*
         * Attempt to detect a corrupted layout; return null in such cases
         */

        if (isLayoutCorrupt(layoutDoc)) {
            logger.warn(
                    "Layout for user: {} is corrupt; layout structures will not be exported.",
                    person.getUserName());
            return null;
        }

        /*
         * Clean up the DOM for export.
         */

        // (1) Add structure & theme attributes...
        final int structureStylesheetId = profile.getStructureStylesheetId();
        this.addStylesheetUserPreferencesAttributes(
                person, profile, layoutDoc, structureStylesheetId, "structure");

        final int themeStylesheetId = profile.getThemeStylesheetId();
        this.addStylesheetUserPreferencesAttributes(
                person, profile, layoutDoc, themeStylesheetId, "theme");

        // (2) Remove locale info...
        final Iterator<org.dom4j.Attribute> locale =
                (Iterator<org.dom4j.Attribute>) layoutDoc.selectNodes("//@locale").iterator();
        while (locale.hasNext()) {
            final org.dom4j.Attribute loc = locale.next();
            loc.getParent().remove(loc);
        }

        // (3) Scrub unnecessary channel information...
        for (final Iterator<org.dom4j.Element> orphanedChannels =
                        (Iterator<org.dom4j.Element>)
                                layoutDoc.selectNodes("//channel[@fname = '']").iterator();
                orphanedChannels.hasNext(); ) {
            // These elements represent UP_LAYOUT_STRUCT rows where the
            // CHAN_ID field was not recognized by ChannelRegistryStore;
            // best thing to do is remove the elements...
            final org.dom4j.Element ch = orphanedChannels.next();
            ch.getParent().remove(ch);
        }
        final List<String> channelAttributeWhitelist =
                Arrays.asList(
                        new String[] {
                            "fname",
                            "unremovable",
                            "hidden",
                            "immutable",
                            "ID",
                            "dlm:plfID",
                            "dlm:moveAllowed",
                            "dlm:deleteAllowed"
                        });
        final Iterator<org.dom4j.Element> channels =
                (Iterator<org.dom4j.Element>) layoutDoc.selectNodes("//channel").iterator();
        while (channels.hasNext()) {
            final org.dom4j.Element oldCh = channels.next();
            final org.dom4j.Element parent = oldCh.getParent();
            final org.dom4j.Element newCh = this.fac.createElement("channel");
            for (final String aName : channelAttributeWhitelist) {
                final org.dom4j.Attribute a =
                        (org.dom4j.Attribute) oldCh.selectSingleNode("@" + aName);
                if (a != null) {
                    newCh.addAttribute(a.getQName(), a.getValue());
                }
            }
            parent.elements().add(parent.elements().indexOf(oldCh), newCh);
            parent.remove(oldCh);
        }

        // (4) Convert internal DLM noderefs to external form (pathrefs)...
        for (final Iterator<org.dom4j.Attribute> origins =
                        (Iterator<org.dom4j.Attribute>)
                                layoutDoc.selectNodes("//@dlm:origin").iterator();
                origins.hasNext(); ) {
            final org.dom4j.Attribute org = origins.next();
            final Pathref dlmPathref =
                    this.nodeReferenceFactory.getPathrefFromNoderef(
                            (String) person.getAttribute(IPerson.USERNAME),
                            org.getValue(),
                            layoutDoc.getRootElement());
            if (dlmPathref != null) {
                // Change the value only if we have a valid pathref...
                org.setValue(dlmPathref.toString());
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Layout element '{}' from user '{}' failed to match noderef '{}'",
                            org.getUniquePath(),
                            person.getAttribute(IPerson.USERNAME),
                            org.getValue());
                }
            }
        }
        for (final Iterator<org.dom4j.Attribute> it =
                        (Iterator<org.dom4j.Attribute>)
                                layoutDoc.selectNodes("//@dlm:target").iterator();
                it.hasNext(); ) {
            final org.dom4j.Attribute target = it.next();
            final Pathref dlmPathref =
                    this.nodeReferenceFactory.getPathrefFromNoderef(
                            (String) person.getAttribute(IPerson.USERNAME),
                            target.getValue(),
                            layoutDoc.getRootElement());
            if (dlmPathref != null) {
                // Change the value only if we have a valid pathref...
                target.setValue(dlmPathref.toString());
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Layout element '{}' from user '{}' failed to match noderef '{}'",
                            target.getUniquePath(),
                            person.getAttribute(IPerson.USERNAME),
                            target.getValue());
                }
            }
        }
        for (final Iterator<org.dom4j.Attribute> names =
                        (Iterator<org.dom4j.Attribute>)
                                layoutDoc.selectNodes("//dlm:*/@name").iterator();
                names.hasNext(); ) {
            final org.dom4j.Attribute n = names.next();
            if (n.getValue() == null || n.getValue().trim().length() == 0) {
                // Outer <dlm:positionSet> elements don't seem to use the name
                // attribute, though their childern do.  Just skip these so we
                // don't send a false WARNING.
                continue;
            }
            final Pathref dlmPathref =
                    this.nodeReferenceFactory.getPathrefFromNoderef(
                            (String) person.getAttribute(IPerson.USERNAME),
                            n.getValue(),
                            layoutDoc.getRootElement());
            if (dlmPathref != null) {
                // Change the value only if we have a valid pathref...
                n.setValue(dlmPathref.toString());
                // These *may* have fnames...
                if (dlmPathref.getPortletFname() != null) {
                    n.getParent().addAttribute("fname", dlmPathref.getPortletFname());
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Layout element '{}' from user '{}' failed to match noderef '{}'",
                            n.getUniquePath(),
                            person.getAttribute(IPerson.USERNAME),
                            n.getValue());
                }
            }
        }

        // Remove synthetic Ids, but from non-fragment owners only...
        if (!this.isFragmentOwner(person)) {

            /*
             * In the case of fragment owners, the original database Ids allow
             * us keep (not break) the associations that subscribers have with
             * nodes on the fragment layout.
             */

            // (5) Remove dlm:plfID...
            for (final Iterator<org.dom4j.Attribute> plfid =
                            (Iterator<org.dom4j.Attribute>)
                                    layoutDoc.selectNodes("//@dlm:plfID").iterator();
                    plfid.hasNext(); ) {
                final org.dom4j.Attribute plf = plfid.next();
                plf.getParent().remove(plf);
            }

            // (6) Remove database Ids...
            for (final Iterator<org.dom4j.Attribute> ids =
                            (Iterator<org.dom4j.Attribute>)
                                    layoutDoc.selectNodes("//@ID").iterator();
                    ids.hasNext(); ) {
                final org.dom4j.Attribute a = ids.next();
                a.getParent().remove(a);
            }
        }

        return layoutDoc.getRootElement();
    }

    /**
     * Attempts to detect known forms of corruption to avoid erroring-out on the export (or
     * subsequent import), and also to prevent migrating a bad layout. Users whose layouts are
     * culled in this fashion will have their layouts reset through migration.
     */
    private boolean isLayoutCorrupt(org.dom4j.Document layoutDoc) {

        boolean rslt = false; // until we find otherwise...

        for (FormOfLayoutCorruption form : KNOWN_FORMS_OF_LAYOUT_CORRUPTION) {
            if (form.detect(layoutDoc)) {
                logger.warn("Corrupt layout detected: {}", form.getMessage());
                rslt = true;
                break;
            }
        }

        return rslt;
    }

    private void addStylesheetUserPreferencesAttributes(
            IPerson person,
            IUserProfile profile,
            org.dom4j.Document layoutDoc,
            int stylesheetId,
            String attributeType) {
        final IStylesheetDescriptor structureStylesheetDescriptor =
                this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetId);

        final IStylesheetUserPreferences ssup =
                this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(
                        structureStylesheetDescriptor, person, profile);

        if (ssup != null) {
            final Collection<String> allLayoutAttributeNodeIds =
                    ssup.getAllLayoutAttributeNodeIds();
            for (final String nodeId : allLayoutAttributeNodeIds) {

                final MapPopulator<String, String> layoutAttributesPopulator = new MapPopulator<>();
                ssup.populateLayoutAttributes(nodeId, layoutAttributesPopulator);
                final Map<String, String> layoutAttributes = layoutAttributesPopulator.getMap();

                final org.dom4j.Element element = layoutDoc.elementByID(nodeId);
                if (element == null) {
                    logger.warn(
                            "No node with id '{}' found in layout for: {}. Stylesheet user preference layout attributes will be ignored: {}",
                            nodeId,
                            person.getUserName(),
                            layoutAttributes);
                    continue;
                }

                for (final Entry<String, String> attributeEntry : layoutAttributes.entrySet()) {
                    final String name = attributeEntry.getKey();
                    final String value = attributeEntry.getValue();

                    logger.debug(
                            "Adding structure folder attribute:  name={}, value={}", name, value);

                    final org.dom4j.Element structAttrElement =
                            this.fac.createElement(attributeType + "-attribute");
                    final org.dom4j.Element nameAttribute = structAttrElement.addElement("name");
                    nameAttribute.setText(name);
                    final org.dom4j.Element valueAttribute = structAttrElement.addElement("value");
                    valueAttribute.setText(value);
                    element.elements().add(0, structAttrElement);
                }
            }
        } else {
            logger.debug("no StylesheetUserPreferences found for {}, {}", person, profile);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional
    public void importLayout(org.dom4j.Element layout) {
        if (layout.getNamespaceForPrefix("dlm") == null) {
            layout.add(new Namespace("dlm", Constants.NS_URI));
        }

        // Remove comments from the DOM they break import
        final List<org.dom4j.Node> comments = layout.selectNodes("//comment()");
        for (final org.dom4j.Node comment : comments) {
            comment.detach();
        }

        // Get a ref to the prefs element and then remove it from the layout
        final org.dom4j.Node preferencesElement = layout.selectSingleNode("preferences");
        if (preferencesElement != null) {
            preferencesElement.getParent().remove(preferencesElement);
        }

        final String ownerUsername = layout.valueOf("@username");

        // Get a ref to the profile element and then remove it from the layout
        final org.dom4j.Node profileElement = layout.selectSingleNode("profile");
        if (profileElement != null) {
            profileElement.getParent().remove(profileElement);

            final org.dom4j.Document profileDocument =
                    new org.dom4j.DocumentFactory().createDocument();
            profileDocument.setRootElement((org.dom4j.Element) profileElement);
            profileDocument.setName(ownerUsername + ".profile");

            final DocumentSource profileSource = new DocumentSource(profileElement);
            this.portalDataHandlerService.importData(profileSource);
        }

        final IPerson person = new PersonImpl();
        person.setUserName(ownerUsername);

        int ownerId;
        try {
            // Can't just pass true for create here, if the user actually exists the create flag
            // also updates the user data
            ownerId = this.userIdentityStore.getPortalUID(person);
        } catch (final AuthorizationException t) {
            if (this.errorOnMissingUser) {
                throw new RuntimeException(
                        "Unrecognized user "
                                + person.getUserName()
                                + "; you must import users before their layouts or set org.apereo.portal.io.layout.errorOnMissingUser to false.",
                        t);
            }

            // Create the missing user
            ownerId = this.userIdentityStore.getPortalUID(person, true);
        }

        if (ownerId == -1) {
            throw new RuntimeException(
                    "Unrecognized user "
                            + person.getUserName()
                            + "; you must import users before their layouts or set org.apereo.portal.io.layout.errorOnMissingUser to false.");
        }
        person.setID(ownerId);

        IUserProfile profile = null;
        try {
            person.setSecurityContext(new BrokenSecurityContext());
            profile = this.getUserProfileByFname(person, "default");
        } catch (final Throwable t) {
            throw new RuntimeException(
                    "Failed to load profile for "
                            + person.getUserName()
                            + "; This user must have a profile for import to continue.",
                    t);
        }

        // (6) Add database Ids & (5) Add dlm:plfID ...
        int nextId = 1;
        for (final Iterator<org.dom4j.Element> it =
                        (Iterator<org.dom4j.Element>)
                                layout.selectNodes("folder | dlm:* | channel").iterator();
                it.hasNext(); ) {
            nextId = this.addIdAttributesIfNecessary(it.next(), nextId);
        }
        // Now update UP_USER...
        this.jdbcOperations.update(
                "UPDATE up_user SET next_struct_id = ? WHERE user_id = ?", nextId, person.getID());

        // (4) Convert external DLM pathrefs to internal form (noderefs)...
        for (final Iterator<org.dom4j.Attribute> itr =
                        (Iterator<org.dom4j.Attribute>)
                                layout.selectNodes("//@dlm:origin").iterator();
                itr.hasNext(); ) {
            final org.dom4j.Attribute a = itr.next();
            final Noderef dlmNoderef =
                    nodeReferenceFactory.getNoderefFromPathref(
                            ownerUsername, a.getValue(), null, true, layout);
            if (dlmNoderef != null) {
                // Change the value only if we have a valid pathref...
                a.setValue(dlmNoderef.toString());
                // For dlm:origin only, also use the noderef as the ID attribute...
                a.getParent().addAttribute("ID", dlmNoderef.toString());
            } else {
                // At least insure the value is between 1 and 35 characters
                a.setValue(BAD_PATHREF_MESSAGE);
            }
        }
        for (final Iterator<org.dom4j.Attribute> itr =
                        (Iterator<org.dom4j.Attribute>)
                                layout.selectNodes("//@dlm:target").iterator();
                itr.hasNext(); ) {
            final org.dom4j.Attribute a = itr.next();
            final Noderef dlmNoderef =
                    nodeReferenceFactory.getNoderefFromPathref(
                            ownerUsername, a.getValue(), null, true, layout);
            // Put in the correct value, or at least insure the value is between 1 and 35 characters
            a.setValue(dlmNoderef != null ? dlmNoderef.toString() : BAD_PATHREF_MESSAGE);
        }
        for (final Iterator<org.dom4j.Attribute> names =
                        (Iterator<org.dom4j.Attribute>)
                                layout.selectNodes("//dlm:*/@name").iterator();
                names.hasNext(); ) {
            final org.dom4j.Attribute a = names.next();
            final String value = a.getValue().trim();
            if (!VALID_PATHREF_PATTERN.matcher(value).matches()) {
                /* Don't send it to getDlmNoderef if we know in advance it's not
                 * going to work;  saves annoying/misleading log messages and
                 * possibly some processing.  NOTE this is _only_ a problem with
                 * the name attribute of some dlm:* elements, which seems to go
                 * unused intentionally in some circumstances
                 */
                continue;
            }
            final org.dom4j.Attribute fname = a.getParent().attribute("fname");
            Noderef dlmNoderef = null;
            if (fname != null) {
                dlmNoderef =
                        nodeReferenceFactory.getNoderefFromPathref(
                                ownerUsername, value, fname.getValue(), false, layout);
                // Remove the fname attribute now that we're done w/ it...
                fname.getParent().remove(fname);
            } else {
                dlmNoderef =
                        nodeReferenceFactory.getNoderefFromPathref(
                                ownerUsername, value, null, true, layout);
            }
            // Put in the correct value, or at least insure the value is between 1 and 35 characters
            a.setValue(dlmNoderef != null ? dlmNoderef.toString() : BAD_PATHREF_MESSAGE);
        }

        // (3) Restore chanID attributes on <channel> elements...
        for (final Iterator<org.dom4j.Element> it =
                        (Iterator<org.dom4j.Element>) layout.selectNodes("//channel").iterator();
                it.hasNext(); ) {
            final org.dom4j.Element c = it.next();
            final String fname = c.valueOf("@fname");
            final IPortletDefinition cd =
                    this.portletDefinitionRegistry.getPortletDefinitionByFname(fname);
            if (cd == null) {
                final String msg =
                        "No portlet with fname="
                                + fname
                                + " exists referenced by node "
                                + c.valueOf("@ID")
                                + " from layout for "
                                + ownerUsername;
                if (errorOnMissingPortlet) {
                    throw new IllegalArgumentException(msg);
                } else {
                    logger.warn(msg);
                    // Remove the bad channel node
                    c.getParent().remove(c);
                }
            } else {
                c.addAttribute("chanID", String.valueOf(cd.getPortletDefinitionId().getStringId()));
            }
        }

        // (2) Restore locale info...
        // (This step doesn't appear to be needed for imports)

        // (1) Process structure & theme attributes...
        Document layoutDom = null;
        try {

            final int structureStylesheetId = profile.getStructureStylesheetId();
            this.loadStylesheetUserPreferencesAttributes(
                    person, profile, layout, structureStylesheetId, "structure");

            final int themeStylesheetId = profile.getThemeStylesheetId();
            this.loadStylesheetUserPreferencesAttributes(
                    person, profile, layout, themeStylesheetId, "theme");

            // From this point forward we need the user's PLF set as DLM expects it...
            for (final Iterator<org.dom4j.Text> it =
                            (Iterator<org.dom4j.Text>)
                                    layout.selectNodes("descendant::text()").iterator();
                    it.hasNext(); ) {
                // How many years have we used Java & XML, and this still isn't easy?
                final org.dom4j.Text txt = it.next();
                if (txt.getText().trim().length() == 0) {
                    txt.getParent().remove(txt);
                }
            }

            final org.dom4j.Element copy = layout.createCopy();
            final org.dom4j.Document doc = this.fac.createDocument(copy);
            doc.normalize();
            layoutDom = this.writer.get().write(doc);
            person.setAttribute(Constants.PLF, layoutDom);

        } catch (final Throwable t) {
            throw new RuntimeException(
                    "Unable to set UserPreferences for user:  " + person.getUserName(), t);
        }

        // Finally store the layout...
        try {
            this.setUserLayout(person, profile, layoutDom, true, true);
        } catch (final Throwable t) {
            final String msg = "Unable to persist layout for user:  " + ownerUsername;
            throw new RuntimeException(msg, t);
        }

        if (preferencesElement != null) {
            final int ownerUserId = this.userIdentityStore.getPortalUserId(ownerUsername);
            // TODO this assumes a single layout, when multi-layout support exists portlet entities
            // will need to be re-worked to allow for a layout id to be associated with the entity

            // track which entities from the user's pre-existing set are touched (all non-touched
            // entities will be removed)
            final Set<IPortletEntity> oldPortletEntities =
                    new LinkedHashSet<>(
                            this.portletEntityDao.getPortletEntitiesForUser(ownerUserId));

            final List<org.dom4j.Element> entries = preferencesElement.selectNodes("entry");
            for (final org.dom4j.Element entry : entries) {
                final String dlmPathRef = entry.attributeValue("entity");
                final String fname = entry.attributeValue("channel");
                final String prefName = entry.attributeValue("name");

                final Noderef dlmNoderef =
                        nodeReferenceFactory.getNoderefFromPathref(
                                person.getUserName(), dlmPathRef, fname, false, layout);

                if (dlmNoderef != null && fname != null) {
                    final IPortletEntity portletEntity =
                            this.getPortletEntity(fname, dlmNoderef.toString(), ownerUserId);
                    oldPortletEntities.remove(portletEntity);

                    final List<IPortletPreference> portletPreferences =
                            portletEntity.getPortletPreferences();

                    final List<org.dom4j.Element> valueElements = entry.selectNodes("value");
                    final List<String> values = new ArrayList<>(valueElements.size());
                    for (final org.dom4j.Element valueElement : valueElements) {
                        values.add(valueElement.getText());
                    }

                    portletPreferences.add(
                            new PortletPreferenceImpl(
                                    prefName, false, values.toArray(new String[values.size()])));

                    this.portletEntityDao.updatePortletEntity(portletEntity);
                }
            }

            // Delete all portlet preferences for entities that were not imported
            for (final IPortletEntity portletEntity : oldPortletEntities) {
                portletEntity.setPortletPreferences(null);

                if (portletEntityRegistry.shouldBePersisted(portletEntity)) {
                    this.portletEntityDao.updatePortletEntity(portletEntity);
                } else {
                    this.portletEntityDao.deletePortletEntity(portletEntity);
                }
            }
        }
    }

    private IPortletEntity getPortletEntity(String fName, String layoutNodeId, int userId) {
        // Try getting the entity
        final IPortletEntity portletEntity =
                this.portletEntityDao.getPortletEntity(layoutNodeId, userId);
        if (portletEntity != null) {
            return portletEntity;
        }

        // Load the portlet definition
        final IPortletDefinition portletDefinition;
        try {
            portletDefinition = this.portletDefinitionRegistry.getPortletDefinitionByFname(fName);
        } catch (Exception e) {
            throw new DataRetrievalFailureException(
                    "Failed to retrieve ChannelDefinition for fName='" + fName + "'", e);
        }

        // The channel definition for the fName MUST exist for this class to function
        if (portletDefinition == null) {
            throw new EmptyResultDataAccessException(
                    "No ChannelDefinition exists for fName='" + fName + "'", 1);
        }

        // create the portlet entity
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        return this.portletEntityDao.createPortletEntity(portletDefinitionId, layoutNodeId, userId);
    }

    private void loadStylesheetUserPreferencesAttributes(
            IPerson person,
            IUserProfile profile,
            org.dom4j.Element layout,
            final int structureStylesheetId,
            final String nodeType) {

        final IStylesheetDescriptor stylesheetDescriptor =
                this.stylesheetDescriptorDao.getStylesheetDescriptor(structureStylesheetId);
        final List<org.dom4j.Element> structureAttributes =
                layout.selectNodes("//" + nodeType + "-attribute");

        IStylesheetUserPreferences ssup =
                this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(
                        stylesheetDescriptor, person, profile);
        if (structureAttributes.isEmpty()) {
            if (ssup != null) {
                this.stylesheetUserPreferencesDao.deleteStylesheetUserPreferences(ssup);
            }
        } else {
            if (ssup == null) {
                ssup =
                        this.stylesheetUserPreferencesDao.createStylesheetUserPreferences(
                                stylesheetDescriptor, person, profile);
            }

            final Map<String, Map<String, String>> oldLayoutAttributes = new HashMap<>();
            for (final String nodeId : ssup.getAllLayoutAttributeNodeIds()) {
                final MapPopulator<String, String> nodeAttributes = new MapPopulator<>();
                ssup.populateLayoutAttributes(nodeId, nodeAttributes);
                oldLayoutAttributes.put(nodeId, nodeAttributes.getMap());
            }

            for (final org.dom4j.Element structureAttribute : structureAttributes) {
                final org.dom4j.Element layoutElement = structureAttribute.getParent();
                final String nodeId = layoutElement.valueOf("@ID");
                if (StringUtils.isEmpty(nodeId)) {
                    logger.warn(
                            "@ID is empty for layout element, the attribute will be ignored: {}",
                            structureAttribute.asXML());
                }

                final String name = structureAttribute.valueOf("name");
                if (StringUtils.isEmpty(nodeId)) {
                    logger.warn(
                            "name is empty for layout element, the attribute will be ignored: {}",
                            structureAttribute.asXML());
                    continue;
                }

                final String value = structureAttribute.valueOf("value");
                if (StringUtils.isEmpty(nodeId)) {
                    logger.warn(
                            "value is empty for layout element, the attribute will be ignored: {}",
                            structureAttribute.asXML());
                    continue;
                }

                // Remove from the old attrs set as we've updated it
                final Map<String, String> oldAttrs = oldLayoutAttributes.get(nodeId);
                if (oldAttrs != null) {
                    oldAttrs.remove(name);
                }

                ssup.setLayoutAttribute(nodeId, name, value);

                // Remove the layout attribute element or DLM fails
                layoutElement.remove(structureAttribute);
            }

            // Purge orphaned entries
            for (final Entry<String, Map<String, String>> oldAttributeEntry :
                    oldLayoutAttributes.entrySet()) {
                final String nodeId = oldAttributeEntry.getKey();
                for (final String name : oldAttributeEntry.getValue().keySet()) {
                    ssup.removeLayoutAttribute(nodeId, name);
                }
            }

            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(ssup);
        }
    }

    @SuppressWarnings("unchecked")
    private final int addIdAttributesIfNecessary(org.dom4j.Element e, int nextId) {

        int idAfterThisOne = nextId; // default...
        final org.dom4j.Node idAttribute = e.selectSingleNode("@ID | @dlm:plfID");
        if (idAttribute == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "No ID or dlm:plfID attribute for the following node (one will be generated and added):  element{}, name={}, fname={}",
                        e.getName(),
                        e.valueOf("@name"),
                        e.valueOf("@fname"));
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

            final String origin = e.valueOf("@dlm:origin");
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
        } else {
            final String id = idAttribute.getText();
            try {
                idAfterThisOne = Integer.parseInt(id.substring(1)) + 1;
            } catch (final NumberFormatException nfe) {
                logger.warn(
                        "Could not parse int value from id: {} The next layout id will be: {}",
                        id,
                        idAfterThisOne,
                        nfe);
            }
        }

        // Now check children...
        for (final Iterator<org.dom4j.Element> itr =
                        (Iterator<org.dom4j.Element>)
                                e.selectNodes("folder | channel | dlm:*").iterator();
                itr.hasNext(); ) {
            final org.dom4j.Element child = itr.next();
            idAfterThisOne = this.addIdAttributesIfNecessary(child, idAfterThisOne);
        }

        return idAfterThisOne;
    }

    private final ThreadLocal<Cache<Tuple<String, String>, Document>> layoutCacheHolder =
            new ThreadLocal<>();

    @Override
    public void setLayoutImportExportCache(Cache<Tuple<String, String>, Document> layoutCache) {
        if (layoutCache == null) {
            layoutCacheHolder.remove();
        } else {
            this.layoutCacheHolder.set(layoutCache);
        }
    }

    public Cache<Tuple<String, String>, Document> getLayoutImportExportCache() {
        return layoutCacheHolder.get();
    }

    /**
     * Handles locking and identifying proper root and namespaces that used to take place in super
     * class.
     *
     * @param person
     * @param profile
     * @return @
     */
    private Document _safeGetUserLayout(IPerson person, IUserProfile profile) {

        Document layoutDoc;
        Tuple<String, String> key = null;

        final Cache<Tuple<String, String>, Document> layoutCache = getLayoutImportExportCache();
        if (layoutCache != null) {
            key = new Tuple<>(person.getUserName(), profile.getProfileFname());
            layoutDoc = layoutCache.getIfPresent(key);
            if (layoutDoc != null) {
                return (Document) layoutDoc.cloneNode(true);
            }
        }

        layoutDoc = super.getPersonalUserLayout(person, profile);
        Element layout = layoutDoc.getDocumentElement();
        layout.setAttribute(Constants.NS_DECL, Constants.NS_URI);

        if (layoutCache != null && key != null) {
            layoutCache.put(key, (Document) layoutDoc.cloneNode(true));
        }

        return layoutDoc;
    }

    /**
     * Returns the layout for a user. This method overrides the same method in the superclass to
     * return a composite layout for non fragment owners and a regular layout for layout owners. A
     * composite layout is made up of layout pieces from potentially multiple incorporated layouts.
     * If no layouts are defined then the composite layout will be the same as the user's personal
     * layout fragment or PLF, the one holding only those UI elements that they own or incorporated
     * elements that they have been allowed to changed.
     */
    private DistributedUserLayout _getUserLayout(IPerson person, IUserProfile profile) {

        final String userName = (String) person.getAttribute("username");
        final FragmentDefinition ownedFragment =
                this.fragmentUtils.getFragmentDefinitionByOwner(person);
        final boolean isLayoutOwnerDefault = this.isLayoutOwnerDefault(person);
        final Set<String> fragmentNames = new LinkedHashSet<>();

        final Document ILF;
        final Document PLF = this.getPLF(person, profile);

        // If this user is an owner then ownedFragment will be non null. For
        // fragment owners and owners of any default layout from which a
        // fragment owners layout is copied there should not be any imported
        // distributed layouts. Instead, load their PLF, mark as an owned
        // if a fragment owner, and return.
        if (ownedFragment != null || isLayoutOwnerDefault) {
            ILF = (Document) PLF.cloneNode(true);
            final Element layoutNode = ILF.getDocumentElement();

            final Element ownerDocument = layoutNode.getOwnerDocument().getDocumentElement();
            final NodeList channelNodes = ownerDocument.getElementsByTagName("channel");
            for (int i = 0; i < channelNodes.getLength(); i++) {
                Element channelNode = (Element) channelNodes.item(i);
                final Node chanIdNode = channelNode.getAttributeNode("chanID");
                if (chanIdNode == null
                        || MissingPortletDefinition.CHANNEL_ID.equals(chanIdNode.getNodeValue())) {
                    channelNode.getParentNode().removeChild(channelNode);
                }
            }

            if (ownedFragment != null) {
                fragmentNames.add(ownedFragment.getName());
                layoutNode.setAttributeNS(
                        Constants.NS_URI, Constants.ATT_FRAGMENT_NAME, ownedFragment.getName());
                logger.debug(
                        "User '{}' is owner of '{}' fragment.", userName, ownedFragment.getName());
            } else if (isLayoutOwnerDefault) {
                layoutNode.setAttributeNS(
                        Constants.NS_URI,
                        Constants.ATT_TEMPLATE_LOGIN_ID,
                        (String) person.getAttribute("username"));
            }
        } else {
            final Locale locale = profile.getLocaleManager().getLocales().get(0);
            final List<FragmentDefinition> applicableFragmentDefinitions =
                    this.fragmentUtils.getFragmentDefinitionsApplicableToPerson(person);
            final List<Document> applicableLayouts =
                    this.fragmentUtils.getFragmentDefinitionUserViewLayouts(
                            applicableFragmentDefinitions, locale);
            final IntegrationResult integrationResult = new IntegrationResult();
            ILF = this.createCompositeILF(person, PLF, applicableLayouts, integrationResult);
            // push optimizations made during merge back into db.
            if (integrationResult.isChangedPLF()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Saving PLF for {} due to changes during merge.",
                            person.getAttribute(IPerson.USERNAME));
                }
                super.setUserLayout(person, profile, PLF, false);
            }
            fragmentNames.addAll(
                    this.fragmentUtils.getFragmentNames(applicableFragmentDefinitions));
        }
        return this.createDistributedUserLayout(person, profile, ILF, fragmentNames);
    }

    private Document getPLF(final IPerson person, final IUserProfile profile) {
        Document PLF = (Document) person.getAttribute(Constants.PLF);
        if (null == PLF) {
            PLF = this._safeGetUserLayout(person, profile);
            person.setAttribute(Constants.PLF, PLF);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "PLF for {} immediately after loading\n{}",
                    person.getAttribute(IPerson.USERNAME),
                    XmlUtilitiesImpl.toString(PLF));
        }
        return PLF;
    }

    /**
     * Creates a composite ILF (incorporated layouts fragment) by first using the applicable
     * fragment layouts, then merging in the PLF (personal layout fragment).
     */
    private Document createCompositeILF(
            final IPerson person,
            final Document PLF,
            final List<Document> applicableLayouts,
            final IntegrationResult integrationResult) {
        final Document ILF = ILFBuilder.constructILF(PLF, applicableLayouts, person);
        PLFIntegrator.mergePLFintoILF(PLF, ILF, integrationResult);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "PLF for {} after MERGING\n{}",
                    person.getAttribute(IPerson.USERNAME),
                    XmlUtilitiesImpl.toString(PLF));
            logger.debug(
                    "ILF for {} after MERGING\n{}",
                    person.getAttribute(IPerson.USERNAME),
                    XmlUtilitiesImpl.toString(ILF));
        }
        return ILF;
    }

    private DistributedUserLayout createDistributedUserLayout(
            final IPerson person,
            final IUserProfile profile,
            final Document ILF,
            final Set<String> fragmentNames) {
        final int structureStylesheetId = profile.getStructureStylesheetId();
        final IStylesheetUserPreferences distributedStructureStylesheetUserPreferences =
                this.loadDistributedStylesheetUserPreferences(
                        person, profile, structureStylesheetId, fragmentNames);

        final int themeStylesheetId = profile.getThemeStylesheetId();
        final IStylesheetUserPreferences distributedThemeStylesheetUserPreferences =
                this.loadDistributedStylesheetUserPreferences(
                        person, profile, themeStylesheetId, fragmentNames);

        return new DistributedUserLayout(
                ILF,
                distributedStructureStylesheetUserPreferences,
                distributedThemeStylesheetUserPreferences);
    }

    /**
     * Convenience method for fragment activator to obtain raw layouts for fragments during
     * initialization.
     */
    @Override
    public Document getFragmentLayout(IPerson person, IUserProfile profile) {

        return this._safeGetUserLayout(person, profile);
    }

    /**
     * Generates a new struct id for directive elements that dlm places in the PLF version of the
     * layout tree. These elements are atifacts of the dlm storage model and used during merge but
     * do not appear in the user's composite view.
     */
    @Override
    public String getNextStructDirectiveId(IPerson person) {
        return super.getNextStructId(person, Constants.DIRECTIVE_PREFIX);
    }

    /**
     * Replaces the layout Document stored on a fragment definition with a new version. This is
     * called when a fragment owner updates their layout.
     */
    private void updateCachedLayout(
            Document layout, IUserProfile profile, FragmentDefinition fragment) {
        final Locale locale = profile.getLocaleManager().getLocales().get(0);
        // need to make a copy that we can fragmentize
        layout = (Document) layout.cloneNode(true);

        // Fix later to handle multiple profiles
        final Element root = layout.getDocumentElement();
        final UserView userView = this.fragmentUtils.getUserView(fragment, locale);
        if (userView == null) {
            throw new IllegalStateException(
                    "No UserView found for fragment: " + fragment.getName());
        }

        root.setAttribute(
                Constants.ATT_ID,
                Constants.FRAGMENT_ID_USER_PREFIX
                        + userView.getUserId()
                        + Constants.FRAGMENT_ID_LAYOUT_PREFIX
                        + "1");
        try {
            this.fragmentActivator.clearChacheForOwner(fragment.getOwnerId());
            this.fragmentUtils.getUserView(fragment, locale);
        } catch (final Exception e) {
            logger.error("An exception occurred attempting to update a layout.", e);
        }
    }

    /**
     * Returns true if the user is the owner of a layout which is copied as the default for any
     * fragment when first created.
     */
    private boolean isLayoutOwnerDefault(IPerson person) {
        final String userName = (String) person.getAttribute("username");

        final List<FragmentDefinition> definitions = this.fragmentUtils.getFragmentDefinitions();
        if (userName != null && definitions != null) {
            for (final FragmentDefinition fragmentDefinition : definitions) {
                if (fragmentDefinition.defaultLayoutOwnerID != null
                        && fragmentDefinition.defaultLayoutOwnerID.equals(userName)) {
                    return true;
                }
            }
        }
        final String globalDefault = PropertiesManager.getProperty(DEFAULT_LAYOUT_OWNER_PROPERTY);
        if (globalDefault != null && globalDefault.equals(userName)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isFragmentOwner(IPerson person) {
        return this.fragmentUtils.getFragmentDefinitionByOwner(person) != null;
    }

    @Override
    public boolean isFragmentOwner(String username) {

        boolean rslt = false; // default

        final List<FragmentDefinition> definitions = this.fragmentUtils.getFragmentDefinitions();
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
     * This method overrides the same method in the super class to persist only layout information
     * stored in the user's person layout fragment or PLF. If this person is a layout owner then
     * their changes are pushed into the appropriate layout fragment.
     */
    @Override
    public void setUserLayout(
            IPerson person, IUserProfile profile, Document layoutXML, boolean channelsAdded) {

        this.setUserLayout(person, profile, layoutXML, channelsAdded, true);
    }

    /**
     * This method overrides the same method in the super class to persist only layout information
     * stored in the user's person layout fragment or PLF. If fragment cache update is requested
     * then it checks to see if this person is a layout owner and if so then their changes are
     * pushed into the appropriate layout fragment.
     */
    @Override
    public void setUserLayout(
            IPerson person,
            IUserProfile profile,
            Document layoutXML,
            boolean channelsAdded,
            boolean updateFragmentCache) {

        final Document plf = (Document) person.getAttribute(Constants.PLF);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "PLF for {}\n{}",
                    person.getAttribute(IPerson.USERNAME),
                    XmlUtilitiesImpl.toString(plf));
        }
        super.setUserLayout(person, profile, plf, channelsAdded);

        if (updateFragmentCache) {
            final FragmentDefinition fragment =
                    this.fragmentUtils.getFragmentDefinitionByOwner(person);

            if (fragment != null) {
                this.updateCachedLayout(plf, profile, fragment);
            }
        }
    }

    @Override
    public FragmentChannelInfo getFragmentChannelInfo(String sId) {
        final FragmentNodeInfo node = this.getFragmentNodeInfo(sId);

        if (node != null && node instanceof FragmentChannelInfo) {
            return (FragmentChannelInfo) node;
        }
        return null;
    }

    @Override
    public FragmentNodeInfo getFragmentNodeInfo(String sId) {
        // grab local pointers to variables subject to change at any time
        final List<FragmentDefinition> fragments = this.fragmentUtils.getFragmentDefinitions();
        final Locale defaultLocale = localeManagerFactory.getPortalLocales().get(0);

        final net.sf.ehcache.Element element = this.fragmentNodeInfoCache.get(sId);
        FragmentNodeInfo info =
                element != null ? (FragmentNodeInfo) element.getObjectValue() : null;

        if (info == null) {
            for (final FragmentDefinition fragmentDefinition : fragments) {
                final UserView userView =
                        this.fragmentUtils.getUserView(fragmentDefinition, defaultLocale);
                if (userView == null) {
                    logger.warn(
                            "No UserView is present for fragment {} it will be skipped when fragment node information",
                            fragmentDefinition.getName());
                    continue;
                }

                final Element node = userView.getLayout().getElementById(sId);
                if (node != null) // found it
                {
                    if (node.getTagName().equals(Constants.ELM_CHANNEL)) {
                        info = new FragmentChannelInfo(node);
                    } else {
                        info = new FragmentNodeInfo(node);
                    }
                    this.fragmentNodeInfoCache.put(new net.sf.ehcache.Element(sId, info));
                    break;
                }
            }
        }
        return info;
    }

    @Override
    protected Element getStructure(Document doc, LayoutStructure ls) {
        Element structure;

        String type = ls.getType();

        if (ls.isChannel()) {
            final IPortletDefinition channelDef =
                    this.portletDefinitionRegistry.getPortletDefinition(
                            String.valueOf(ls.getChanId()));
            if (channelDef != null
                    && channelDef.getLifecycleState()
                            != null // Unusual, but lifecycle state may apparently be null
                    && channelDef
                            .getLifecycleState()
                            .isEqualToOrAfter(PortletLifecycleState.APPROVED)) {
                structure =
                        this.getElementForChannel(
                                doc, CHANNEL_PREFIX + ls.getStructId(), channelDef, ls.getLocale());
            } else {
                structure =
                        this.getElementForChannel(
                                doc,
                                CHANNEL_PREFIX + ls.getStructId(),
                                MissingPortletDefinition.INSTANCE,
                                null);
            }
        } else {
            // create folder objects including dlm new types in cp namespace
            if (type != null && type.startsWith(Constants.NS)) {
                structure = doc.createElementNS(Constants.NS_URI, type);
            } else {
                structure = doc.createElement("folder");
            }
            structure.setAttribute("name", ls.getName());
            structure.setAttribute("type", (type != null ? type : "regular"));
        }

        structure.setAttribute("hidden", (ls.isHidden() ? "true" : "false"));
        structure.setAttribute("immutable", (ls.isImmutable() ? "true" : "false"));
        structure.setAttribute("unremovable", (ls.isUnremovable() ? "true" : "false"));
        if (localeManagerFactory.isLocaleAware()) {
            structure.setAttribute("locale", ls.getLocale()); // for i18n by Shoji
        }

        /*
         * Parameters from up_layout_param are loaded slightly differently for
         * folders and channels. For folders all parameters are added as attributes
         * of the Element. For channels only those parameters with names starting
         * with the dlm namespace Constants.NS are added as attributes to the Element.
         * Others are added as child parameter Elements.
         */
        if (ls.getParameters() != null) {
            for (final Iterator itr = ls.getParameters().iterator(); itr.hasNext(); ) {
                final StructureParameter sp = (StructureParameter) itr.next();
                String pName = sp.getName();

                if (!ls.isChannel()) { // Folder
                    if (pName.startsWith(Constants.NS)) {
                        structure.setAttributeNS(Constants.NS_URI, pName, sp.getValue());
                    } else {
                        structure.setAttribute(pName, sp.getValue());
                    }
                } else // Channel
                {
                    // if dealing with a dlm namespace param add as attribute
                    if (pName.startsWith(Constants.NS)) {
                        structure.setAttributeNS(Constants.NS_URI, pName, sp.getValue());
                        itr.remove();
                    } else {
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
                        final NodeList nodeListParameters =
                                structure.getElementsByTagName("parameter");
                        for (int j = 0; j < nodeListParameters.getLength(); j++) {
                            final Element parmElement = (Element) nodeListParameters.item(j);
                            final NamedNodeMap nm = parmElement.getAttributes();

                            final String nodeName = nm.getNamedItem("name").getNodeValue();
                            if (nodeName.equals(pName)) {
                                final Node override = nm.getNamedItem("override");
                                if (override != null && override.getNodeValue().equals("yes")) {
                                    final Node valueNode = nm.getNamedItem("value");
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
            if (ls.isChannel()) {
                for (final Iterator itr = ls.getParameters().iterator(); itr.hasNext(); ) {
                    final StructureParameter sp = (StructureParameter) itr.next();
                    final Element parameter = doc.createElement("parameter");
                    parameter.setAttribute("name", sp.getName());
                    parameter.setAttribute("value", sp.getValue());
                    parameter.setAttribute("override", "yes");
                    structure.appendChild(parameter);
                }
            }
        }
        // finish setting up elements based on loaded params
        final String origin = structure.getAttribute(Constants.ATT_ORIGIN);
        final String prefix = ls.isChannel() ? CHANNEL_PREFIX : FOLDER_PREFIX;

        // if not null we are dealing with a node incorporated from another
        // layout and this node contains changes made by the user so handle
        // id swapping.
        if (!origin.equals("")) {
            structure.setAttributeNS(
                    Constants.NS_URI, Constants.ATT_PLF_ID, prefix + ls.getStructId());
            structure.setAttribute("ID", origin);
        } else if (!ls.isChannel())
        // regular folder owned by this user, need to check if this is a
        // directive or ui element. If the latter then use traditional id
        // structure
        {
            if (type != null && type.startsWith(Constants.NS)) {
                structure.setAttribute("ID", Constants.DIRECTIVE_PREFIX + ls.getStructId());
            } else {
                structure.setAttribute("ID", FOLDER_PREFIX + ls.getStructId());
            }
        } else {
            logger.debug("Adding identifier {}{}", FOLDER_PREFIX, ls.getStructId());
            structure.setAttribute("ID", CHANNEL_PREFIX + ls.getStructId());
        }
        structure.setIdAttribute(Constants.ATT_ID, true);
        return structure;
    }

    @Override
    protected int saveStructure(Node node, PreparedStatement structStmt, PreparedStatement parmStmt)
            throws SQLException {
        if (node == null) { // No more
            return 0;
        }
        if (node.getNodeName().equals("parameter")) {
            // parameter, skip it and go on to the next node
            return this.saveStructure(node.getNextSibling(), structStmt, parmStmt);
        }
        if (!(node instanceof Element)) {
            return 0;
        }

        final Element structure = (Element) node;

        if (logger.isDebugEnabled()) {
            logger.debug("saveStructure XML content: {}", XmlUtilitiesImpl.toString(node));
        }

        // determine the struct_id for storing in the db. For incorporated nodes in
        // the plf their ID is a system-wide unique ID while their struct_id for
        // storing in the db is cached in a dlm:plfID attribute.
        int saveStructId = -1;
        final String plfID = structure.getAttribute(Constants.ATT_PLF_ID);

        if (!plfID.equals("")) {
            saveStructId = Integer.parseInt(plfID.substring(1));
        } else {
            final String id = structure.getAttribute("ID");
            saveStructId = Integer.parseInt(id.substring(1));
        }

        int nextStructId = 0;
        int childStructId = 0;
        int chanId = -1;
        IPortletDefinition portletDef = null;
        final boolean isChannel = node.getNodeName().equals("channel");

        if (isChannel) {
            chanId = Integer.parseInt(node.getAttributes().getNamedItem("chanID").getNodeValue());
            portletDef =
                    this.portletDefinitionRegistry.getPortletDefinition(String.valueOf(chanId));
            if (portletDef == null) {
                // Portlet doesn't exist any more, drop the layout node
                return 0;
            }
        }

        if (node.hasChildNodes()) {
            childStructId = this.saveStructure(node.getFirstChild(), structStmt, parmStmt);
        }
        nextStructId = this.saveStructure(node.getNextSibling(), structStmt, parmStmt);
        structStmt.clearParameters();
        structStmt.setInt(1, saveStructId);
        structStmt.setInt(2, nextStructId);
        structStmt.setInt(3, childStructId);

        final String externalId = structure.getAttribute("external_id");
        if (externalId != null && externalId.trim().length() > 0) {
            final Integer eID = new Integer(externalId);
            structStmt.setInt(4, eID.intValue());
        } else {
            structStmt.setNull(4, java.sql.Types.NUMERIC);
        }
        if (isChannel) {
            structStmt.setInt(5, chanId);
            structStmt.setNull(6, java.sql.Types.VARCHAR);
        } else {
            structStmt.setNull(5, java.sql.Types.NUMERIC);
            structStmt.setString(6, structure.getAttribute("name"));
        }
        final String structType = structure.getAttribute("type");
        structStmt.setString(7, structType);
        structStmt.setString(8, RDBMServices.dbFlag(xmlBool(structure.getAttribute("hidden"))));
        structStmt.setString(9, RDBMServices.dbFlag(xmlBool(structure.getAttribute("immutable"))));
        structStmt.setString(
                10, RDBMServices.dbFlag(xmlBool(structure.getAttribute("unremovable"))));
        logger.debug(structStmt.toString());
        structStmt.executeUpdate();

        // code to persist extension attributes for dlm
        final NamedNodeMap attribs = node.getAttributes();
        for (int i = 0; i < attribs.getLength(); i++) {
            final Node attrib = attribs.item(i);
            final String name = attrib.getNodeName();

            if (name.startsWith(Constants.NS)
                    && !name.equals(Constants.ATT_PLF_ID)
                    && !name.equals(Constants.ATT_FRAGMENT)
                    && !name.equals(Constants.ATT_PRECEDENCE)) {
                // a cp extension attribute. Push into param table.
                parmStmt.clearParameters();
                parmStmt.setInt(1, saveStructId);
                parmStmt.setString(2, name);
                parmStmt.setString(3, attrib.getNodeValue());
                logger.debug(parmStmt.toString());
                parmStmt.executeUpdate();
            }
        }
        final NodeList parameters = node.getChildNodes();
        if (parameters != null && isChannel) {
            for (int i = 0; i < parameters.getLength(); i++) {
                if (parameters.item(i).getNodeName().equals("parameter")) {
                    final Element parmElement = (Element) parameters.item(i);
                    final NamedNodeMap nm = parmElement.getAttributes();
                    final String parmName = nm.getNamedItem("name").getNodeValue();
                    final String parmValue = nm.getNamedItem("value").getNodeValue();
                    final Node override = nm.getNamedItem("override");

                    // if no override specified then default to allowed
                    if (override != null && !override.getNodeValue().equals("yes")) {
                        // can't override
                    } else {
                        // override only for adhoc or if diff from chan def
                        final IPortletDefinitionParameter cp = portletDef.getParameter(parmName);
                        if (cp == null || !cp.getValue().equals(parmValue)) {
                            parmStmt.clearParameters();
                            parmStmt.setInt(1, saveStructId);
                            parmStmt.setString(2, parmName);
                            parmStmt.setString(3, parmValue);
                            logger.debug(parmStmt.toString());
                            parmStmt.executeUpdate();
                        }
                    }
                }
            }
        }
        return saveStructId;
    }

    public static Document getPLF(IPerson person) throws PortalException {
        try {
            return (Document) person.getAttribute(Constants.PLF);
        } catch (final Exception ex) {
            throw new PortalException(ex);
        }
    }

    private Element getElementForChannel(
            Document doc, String chanId, IPortletDefinition def, String locale) {
        final Element channel = doc.createElement("channel");

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

        for (final IPortletDefinitionParameter param : def.getParameters()) {
            final Element parameter = doc.createElement("parameter");
            parameter.setAttribute("name", param.getName());
            parameter.setAttribute("value", param.getValue());
            channel.appendChild(parameter);
        }

        return channel;
    }

    private interface FormOfLayoutCorruption {
        boolean detect(org.dom4j.Document layout);

        String getMessage();
    }

    private static final List<FormOfLayoutCorruption> KNOWN_FORMS_OF_LAYOUT_CORRUPTION =
            Collections.unmodifiableList(
                    Arrays.asList(
                            new FormOfLayoutCorruption[] {

                                // One <channel> element inside another
                                new FormOfLayoutCorruption() {
                                    @Override
                                    public boolean detect(org.dom4j.Document layoutDoc) {
                                        return !layoutDoc
                                                .selectNodes("//channel/descendant::channel")
                                                .isEmpty();
                                    }

                                    @Override
                                    public String getMessage() {
                                        return "one <channel> element inside another";
                                    };
                                }
                            }));
}
