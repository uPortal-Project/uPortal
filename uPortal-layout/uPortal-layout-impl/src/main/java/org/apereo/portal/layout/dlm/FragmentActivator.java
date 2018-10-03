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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.AuthorizationException;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.UserProfile;
import org.apereo.portal.i18n.LocaleManager;
import org.apereo.portal.i18n.LocaleManagerFactory;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.properties.PropertiesManager;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.provider.PersonImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** @since 2.5 */
@Service
public class FragmentActivator {
    private static final String NEWLY_CREATED_ATTR = "newlyCreated";
    private static final Log LOG = LogFactory.getLog(FragmentActivator.class);

    private final LoadingCache<String, List<Locale>> fragmentOwnerLocales =
            CacheBuilder.newBuilder()
                    .<String, List<Locale>>build(
                            new CacheLoader<String, List<Locale>>() {
                                @Override
                                public List<Locale> load(String key) throws Exception {
                                    return new CopyOnWriteArrayList<Locale>();
                                }
                            });

    private Ehcache userViews;
    private Ehcache userViewErrors;
    private IUserIdentityStore identityStore;
    private IUserLayoutStore userLayoutStore;
    private ConfigurationLoader configurationLoader;
    private LocaleManagerFactory localeManagerFactory;

    private static final String PROPERTY_ALLOW_EXPANDED_CONTENT =
            "org.apereo.portal.layout.dlm.allowExpandedContent";
    private static final Pattern STANDARD_PATTERN =
            Pattern.compile("\\A[Rr][Ee][Gg][Uu][Ll][Aa][Rr]\\z");
    private static final Pattern EXPANDED_PATTERN = Pattern.compile(".*");

    @Autowired
    public void setUserViewErrors(
            @Qualifier("org.apereo.portal.layout.dlm.FragmentActivator.userViewErrors")
                    Ehcache userViewErrors) {
        this.userViewErrors = userViewErrors;
    }

    @Autowired
    public void setUserViews(
            @Qualifier("org.apereo.portal.layout.dlm.FragmentActivator.userViews")
                    Ehcache userViews) {
        this.userViews =
                new SelfPopulatingCache(
                        userViews,
                        new CacheEntryFactory() {
                            @Override
                            public Object createEntry(Object key) throws Exception {
                                final UserViewKey userViewKey = (UserViewKey) key;

                                // Check if there was an exception the last time a load attempt was
                                // made and re-throw
                                final net.sf.ehcache.Element exceptionElement =
                                        userViewErrors.get(userViewKey);
                                if (exceptionElement != null) {
                                    throw (Exception) exceptionElement.getObjectValue();
                                }

                                try {
                                    return activateFragment(userViewKey);
                                } catch (Exception e) {
                                    userViewErrors.put(new net.sf.ehcache.Element(userViewKey, e));
                                    throw e;
                                }
                            }
                        });
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
    public void setLocaleManagerFactory(LocaleManagerFactory localeManagerFactory) {
        this.localeManagerFactory = localeManagerFactory;
    }

    private static class UserViewKey implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String ownerId;
        private final Locale locale;
        private final int hashCode;

        public UserViewKey(String ownerId, Locale locale) {
            this.ownerId = ownerId;
            this.locale = locale;
            this.hashCode = internalHashCode();
        }

        public String getOwnerId() {
            return ownerId;
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

        public int internalHashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
            result = prime * result + ((locale == null) ? 0 : locale.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            UserViewKey other = (UserViewKey) obj;
            if (ownerId == null) {
                if (other.ownerId != null) return false;
            } else if (!ownerId.equals(other.ownerId)) return false;
            if (locale == null) {
                if (other.locale != null) return false;
            } else if (!locale.equals(other.locale)) return false;
            return true;
        }

        @Override
        public String toString() {
            return "UserViewKey [ownerId=" + ownerId + ", locale=" + locale + "]";
        }
    }

    private UserView activateFragment(final UserViewKey userViewKey) {
        final String ownerId = userViewKey.getOwnerId();
        final FragmentDefinition fd = configurationLoader.getFragmentByOwnerId(ownerId);

        final Locale locale = userViewKey.getLocale();

        fragmentOwnerLocales.getUnchecked(ownerId).add(locale);

        if (fd.isNoAudienceIncluded()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "Skipping activation of FragmentDefinition "
                                + fd.getName()
                                + ", no evaluators found. "
                                + fd);
            }

            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Activating FragmentDefinition " + fd.getName() + " with locale " + locale);
        }

        IPerson owner = bindToOwner(fd);
        UserView view = new UserView(owner.getID());
        loadLayout(view, fd, owner, locale);

        // if owner just created we need to push the layout into
        // the db so that our fragment template user is used and
        // not the default template user as determined by
        // the user identity store.
        if (owner.getAttribute(NEWLY_CREATED_ATTR) != null) {
            owner.setAttribute(Constants.PLF, view.getLayout());
            try {
                saveLayout(view, owner);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to save layout for newly created fragment owner "
                                + owner.getUserName(),
                        e);
            }
        }

        loadPreferences(view, fd);
        fragmentizeLayout(view, fd);

        if (LOG.isInfoEnabled()) {
            LOG.info("Activated FragmentDefinition " + fd.getName() + " with locale " + locale);
        }
        return view;
    }

    public UserView getUserView(final FragmentDefinition fd, final Locale locale) {
        final UserViewKey userViewKey = new UserViewKey(fd.getOwnerId(), locale);
        final net.sf.ehcache.Element userViewElement = this.userViews.get(userViewKey);
        return (UserView) userViewElement.getObjectValue();
    }

    /**
     * Saves the loaded layout in the database for the user and profile.
     *
     * @param view
     * @param owner
     * @throws Exception
     */
    private void saveLayout(UserView view, IPerson owner) throws Exception {
        IUserProfile profile = new UserProfile();
        profile.setProfileId(view.getProfileId());
        userLayoutStore.setUserLayout(owner, profile, view.getLayout(), true, false);
    }

    private IPerson bindToOwner(FragmentDefinition fragment) {
        IPerson owner = new PersonImpl();
        owner.setAttribute("username", fragment.getOwnerId());
        int userID = -1;

        try {
            userID = identityStore.getPortalUID(owner, false);
        } catch (AuthorizationException ae) {
            // current implementation of RDMBUserIdentityStore throws an
            // auth exception if the user doesn't exist even if
            // create data is false as we have it here. So this exception
            // can be discarded since we check for the userID being -1
            // meaning that the user wasn't found to trigger creating
            // that user.
        }
        if (userID == -1) {
            userID = createOwner(owner, fragment);
            owner.setAttribute(NEWLY_CREATED_ATTR, "" + (userID != -1));
        }

        owner.setID(userID);
        return owner;
    }

    private int createOwner(IPerson owner, FragmentDefinition fragment) {
        int userID;

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Owner '"
                            + fragment.getOwnerId()
                            + "' of fragment '"
                            + fragment.getName()
                            + "' not found.");
        }

        try {
            userID = identityStore.getPortalUID(owner, true);
        } catch (AuthorizationException ae) {
            throw new RuntimeException(
                    "\n\nWARNING: Anomaly occurred while creating owner '"
                            + fragment.getOwnerId()
                            + "' of fragment '"
                            + fragment.getName()
                            + "'. The fragment will not be "
                            + "available for inclusion into user layouts.",
                    ae);
        }

        return userID;
    }

    private void loadLayout(
            UserView view, FragmentDefinition fragment, IPerson owner, Locale locale) {
        // if fragment not bound to user can't return any layouts.
        if (view.getUserId() == -1) return;

        // this area is hacked right now. Time won't permit how to handle
        // matching up multiple profiles for a fragment with an appropriate
        // one for incorporating into a user's layout based on their profile
        // when they log in with a certain user agent. The challenge is
        // being able to match up profiles for a user with those of a
        // fragment. Until this is resolved only one profile will be supported
        // and will have a hard coded id of 1 which is the default for profiles.
        // If anyone changes this user all heck could break loose for dlm. :-(

        Document layout = null;

        try {
            // fix hard coded 1 later for multiple profiles
            IUserProfile profile = userLayoutStore.getUserProfileByFname(owner, "default");
            final LocaleManager localeManager =
                    localeManagerFactory.createLocaleManager(
                            owner, Collections.singletonList(locale));
            profile.setLocaleManager(localeManager);

            // see if we have structure & theme stylesheets for this user yet.
            // If not then fall back on system's selected stylesheets.
            if (profile.getStructureStylesheetId() == 0 || profile.getThemeStylesheetId() == 0)
                profile = userLayoutStore.getSystemProfileByFname(profile.getProfileFname());

            view.setProfileId(profile.getProfileId());
            view.setLayoutId(profile.getLayoutId());

            layout = userLayoutStore.getFragmentLayout(owner, profile);
            Element root = layout.getDocumentElement();
            root.setAttribute(
                    Constants.ATT_ID,
                    Constants.FRAGMENT_ID_USER_PREFIX
                            + view.getUserId()
                            + Constants.FRAGMENT_ID_LAYOUT_PREFIX
                            + view.getLayoutId());
            view.setLayout(layout);
        } catch (Exception e) {
            LOG.error(
                    "Anomaly occurred while loading layout for fragment '"
                            + fragment.getName()
                            + "'. The fragment will not be "
                            + "available for inclusion into user layouts.",
                    e);
        }
    }

    private void loadPreferences(UserView view, FragmentDefinition fragment) {
        // if fragment not bound to user can't return any preferences.
        if (view.getUserId() == -1) return;

        IPerson p = new PersonImpl();
        p.setID(view.getUserId());
        p.setAttribute("username", fragment.getOwnerId());
    }

    /**
     * Removes unwanted and hidden folders, then changes all node ids to their globally safe
     * incorporated version.
     */
    private void fragmentizeLayout(UserView view, FragmentDefinition fragment) {
        // if fragment not bound to user or layout empty due to error, return
        if (view.getUserId() == -1 || view.getLayout() == null) {
            return;
        }

        // Choose what types of content to apply from the fragment
        Pattern contentPattern = STANDARD_PATTERN; // default
        boolean allowExpandedContent =
                Boolean.parseBoolean(
                        PropertiesManager.getProperty(PROPERTY_ALLOW_EXPANDED_CONTENT));
        if (allowExpandedContent) {
            contentPattern = EXPANDED_PATTERN;
        }

        // remove all non-regular or hidden top level folders
        // skip root folder that is only child of top level layout element
        Element layout = view.getLayout().getDocumentElement();
        Element root = (Element) layout.getFirstChild();
        NodeList children = root.getChildNodes();

        // process the children backwards since as we delete some the indices
        // shift around
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("folder")) {
                Element folder = (Element) node;

                // strip out folder types 'header', 'footer' and regular,
                // hidden folder "User Preferences" since users have their own
                boolean isApplicable =
                        contentPattern.matcher(folder.getAttribute("type")).matches();
                if (!isApplicable || folder.getAttribute("hidden").equals("true")) {
                    try {
                        root.removeChild(folder);
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Anomaly occurred while stripping out "
                                        + " portions of layout for fragment '"
                                        + fragment.getName()
                                        + "'. The fragment will not be available for "
                                        + "inclusion into user layouts.",
                                e);
                    }
                }
            }
        }
        // now re-lable all remaining nodes below root to have a safe system
        // wide id.

        setIdsAndAttribs(
                layout,
                layout.getAttribute(Constants.ATT_ID),
                "" + fragment.getId(),
                "" + fragment.getPrecedence());
    }

    /**
     * Recursive method that passes through a layout tree and changes all ids from the regular
     * format of sXX or nXX to the globally safe incorporated id of form uXlXsXX or uXlXnXX
     * indicating the user id and layout id from which this node came.
     */
    private void setIdsAndAttribs(
            Element parent, String labelBase, String index, String precedence) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) children.item(i);
                String id = child.getAttribute(Constants.ATT_ID);
                if (!id.equals("")) {
                    String newId = labelBase + id;
                    child.setAttribute(Constants.ATT_ID, newId);
                    child.setIdAttribute(Constants.ATT_ID, true);
                    child.setAttributeNS(Constants.NS_URI, Constants.ATT_FRAGMENT, index);
                    child.setAttributeNS(Constants.NS_URI, Constants.ATT_PRECEDENCE, precedence);
                    setIdsAndAttribs(child, labelBase, index, precedence);
                }
            }
        }
    }

    public void clearChacheForOwner(final String ownerId) {
        final List<Locale> locales = fragmentOwnerLocales.getIfPresent(ownerId);
        if (locales == null) {
            // Nothing to purge
            return;
        }

        for (final Locale locale : locales) {
            final UserViewKey userViewKey = new UserViewKey(ownerId, locale);
            userViews.remove(userViewKey);
        }
    }
}
