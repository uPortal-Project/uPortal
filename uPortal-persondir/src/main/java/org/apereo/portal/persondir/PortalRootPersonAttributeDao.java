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
package org.apereo.portal.persondir;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.AbstractFlatteningPersonAttributeDao;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.apereo.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.apereo.services.persondir.support.NamedPersonImpl;
import org.apereo.services.persondir.support.merger.IAttributeMerger;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * This bean is the root of the User Attributes subsystem in uPortal. It provides support for
 * overriding certain attributes for certain users. (By default it uses a concurrent hash map to
 * manage admin-specified overrides.) It will also do its best to fill required uPortal attributes
 * if they are absent.
 *
 * @since 5.0
 */
public class PortalRootPersonAttributeDao extends AbstractFlatteningPersonAttributeDao {

    protected static final String CUSTOMARY_FIRST_NAME_ATTRIBUTE = "givenName";
    protected static final String CUSTOMARY_LAST_NAME_ATTRIBUTE = "sn";

    private final IAttributeMerger attributeMerger = new ReplacingAttributeAdder();
    private Map<String, Map<String, List<Object>>> overridesMap = new ConcurrentHashMap<>();

    private IPersonAttributeDao delegatePersonAttributeDao;

    private IUsernameAttributeProvider usernameAttributeProvider;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** @return the delegatePersonAttributeDao */
    public IPersonAttributeDao getDelegatePersonAttributeDao() {
        return delegatePersonAttributeDao;
    }

    /** @param delegatePersonAttributeDao the delegatePersonAttributeDao to set */
    @Required
    public void setDelegatePersonAttributeDao(IPersonAttributeDao delegatePersonAttributeDao) {
        Validate.notNull(delegatePersonAttributeDao, "delegatePersonAttributeDao can not be null");
        this.delegatePersonAttributeDao = delegatePersonAttributeDao;
    }

    @Autowired
    public void setUsernameAttributeProvider(IUsernameAttributeProvider usernameAttributeProvider) {
        this.usernameAttributeProvider = usernameAttributeProvider;
    }

    /** @return the overridesMap */
    public Map<String, Map<String, List<Object>>> getAttributeOverridesMap() {
        return overridesMap;
    }

    /** @param overridesMap the overridesMap to set */
    public void setAttributeOverridesMap(Map<String, Map<String, List<Object>>> overridesMap) {
        Validate.notNull(overridesMap, "overridesMap can not be null");
        this.overridesMap = overridesMap;
    }

    public void setUserAttributeOverride(String uid, Map<String, Object> attributes) {
        // Not really a seed but the function still works
        final Map<String, List<Object>> multivaluedAttributes =
                MultivaluedPersonAttributeUtils.toMultivaluedMap(attributes);

        // Update the overrides map
        overridesMap.put(uid, multivaluedAttributes);
    }

    public void removeUserAttributeOverride(String uid) {
        // Remove the uid from the overrides map
        overridesMap.remove(uid);
    }

    /**
     * This method is for "filling" a specific individual.
     *
     * @param uid The username (identity) of a known individual
     * @return A completely "filled" person, including overrides, or <code>null</code> if the person
     *     is unknown
     */
    @Override
    public IPersonAttributes getPerson(String uid, IPersonAttributeDaoFilter filter) {

        final IPersonAttributes result = delegatePersonAttributeDao.getPerson(uid, null);
        if (result == null) {
            // Nothing we can do with that
            return null;
        }

        return postProcessPerson(result, uid);
    }

    /**
     * This method is for matching a search query. Each matching item will subsequently be passed to
     * <code>getPerson(uid)</code> for "filling."
     */
    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
            Map<String, List<Object>> query, IPersonAttributeDaoFilter filter) {
        final Set<IPersonAttributes> people =
                delegatePersonAttributeDao.getPeopleWithMultivaluedAttributes(query, filter);
        if (people == null) {
            return null;
        }

        final Set<IPersonAttributes> modifiedPeople = new LinkedHashSet<>();

        for (final IPersonAttributes person : people) {
            /*
             * WARNING:  Not safe to pass uidInQuery in this scenario;  results will be "filled" by
             * getPerson(uid) subsequently.
             */
            final IPersonAttributes mergedPerson = postProcessPerson(person, null);
            modifiedPeople.add(mergedPerson);
        }

        return modifiedPeople;
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(IPersonAttributeDaoFilter filter) {
        return delegatePersonAttributeDao.getPossibleUserAttributeNames(filter);
    }

    @Override
    public Set<String> getAvailableQueryAttributes(IPersonAttributeDaoFilter filter) {
        return delegatePersonAttributeDao.getAvailableQueryAttributes(filter);
    }

    /**
     * Implements support for overriding attributes by administrator choice and for intelligently
     * selecting (if possible) values for 'username' and 'displayName' if none have been provided by
     * other data sources.
     *
     * @param person The user
     * @param uidInQuery The username specified in the PersonDirectory query, if certain
     * @return The user, possibly with modifications
     */
    private IPersonAttributes postProcessPerson(IPersonAttributes person, String uidInQuery) {

        // Verify the person has a name
        final String name = person.getName();
        if (name == null) {
            logger.warn("IPerson '{}' has no name and therefore cannot be post-processed", person);
            return person;
        }

        // First apply specified overrides
        IPersonAttributes result =
                applyOverridesIfPresent(person); // default -- won't normally need to do anything

        /*
         * And secondly there are two attributes in uPortal that cause havoc if they're missing:
         *
         *   - username
         *   - displayName
         *
         * We need to do our best to provide these if the external data sources don't cover it.
         */
        result = selectUsernameIfAbsent(result);
        result = selectDisplayNameIfAbsent(result);

        logger.debug(
                "Post-processing of person with name='{}' produced the following person:  {}",
                person.getName(),
                result);

        return result;
    }

    protected IPersonAttributes applyOverridesIfPresent(IPersonAttributes person) {

        IPersonAttributes result = person; // default -- won't normally need to do anything

        // Check for & process overrides
        final Map<String, List<Object>> overrides = overridesMap.get(person.getName());
        if (overrides != null) {
            logger.debug(
                    "Overriding the following collection of attributes for user '{}':  {}",
                    person.getName(),
                    overrides);
            final Map<String, List<Object>> attributes = person.getAttributes();
            final Map<String, List<Object>> mutableMap = new LinkedHashMap<>(attributes);
            final Map<String, List<Object>> mergedAttributes =
                    attributeMerger.mergeAttributes(mutableMap, overrides);
            result = new NamedPersonImpl(person.getName(), mergedAttributes);
        }

        return result;
    }

    protected IPersonAttributes selectUsernameIfAbsent(IPersonAttributes person) {

        IPersonAttributes result = person; // default -- won't normally need to do anything

        final String usernameAttribute = usernameAttributeProvider.getUsernameAttribute();
        if (!result.getAttributes().containsKey(usernameAttribute) && person.getName() != null) {
            // Alias the person.name property as the username attribute
            logger.debug("Adding attribute username='{}' for person:  ", person.getName(), person);
            final Map<String, List<Object>> attributes = person.getAttributes();
            final Map<String, List<Object>> mutableMap = new LinkedHashMap<>(attributes);
            mutableMap.put(usernameAttribute, Collections.singletonList(person.getName()));
            result = new NamedPersonImpl(person.getName(), mutableMap);
        }

        return result;
    }

    protected IPersonAttributes selectDisplayNameIfAbsent(IPersonAttributes person) {

        IPersonAttributes result = person; // default -- won't normally need to do anything

        if (!result.getAttributes().containsKey(ILocalAccountPerson.ATTR_DISPLAY_NAME)) {

            /*
             * This one is tougher;  try some common attributes, but fall back on username
             */
            final StringBuilder displayName = new StringBuilder();

            final Map<String, List<Object>> attributes = person.getAttributes();
            final String firstName =
                    attributes.containsKey(CUSTOMARY_FIRST_NAME_ATTRIBUTE)
                            ? attributes.get(CUSTOMARY_FIRST_NAME_ATTRIBUTE).get(0).toString()
                            : null;
            final String lastName =
                    attributes.containsKey(CUSTOMARY_LAST_NAME_ATTRIBUTE)
                            ? attributes.get(CUSTOMARY_LAST_NAME_ATTRIBUTE).get(0).toString()
                            : null;

            if (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName)) {
                // Prefer "${firstName} ${lastName}"
                displayName.append(firstName).append(" ").append(lastName);
            } else {
                // But fall back on username (if present)
                final String usernameAttribute = usernameAttributeProvider.getUsernameAttribute();
                if (attributes.containsKey(usernameAttribute)) {
                    final List<Object> username = attributes.get(usernameAttribute);
                    if (username.size() > 0 && StringUtils.isNotBlank(username.get(0).toString())) {
                        displayName.append(username.get(0).toString());
                    }
                }
            }

            if (displayName.length() > 0) {
                logger.debug(
                        "Selected new displayName of '{}' for user '{}'",
                        displayName,
                        person.getName());
                final Map<String, List<Object>> mutableMap = new LinkedHashMap<>(attributes);
                mutableMap.put(
                        ILocalAccountPerson.ATTR_DISPLAY_NAME,
                        Collections.singletonList(displayName.toString()));
                result = new NamedPersonImpl(person.getName(), mutableMap);
            }
        }

        return result;
    }
}
