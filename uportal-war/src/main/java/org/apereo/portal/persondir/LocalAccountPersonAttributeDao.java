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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.AttributeNamedPersonImpl;
import org.jasig.services.persondir.support.IUsernameAttributeProvider;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.jasig.services.persondir.support.NamedPersonImpl;

/**
 * LocalAccountPersonAttributeDao provides a person directory implementation that uses uPortal's
 * internal account store. This implementation overrides several methods of
 * AbstractQueryPersonAttributeDao to allow for the use of arbitrary user attributes without
 * requiring an administrator to add new entries to the result or query attribute mappings.
 *
 */
public class LocalAccountPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    private ILocalAccountDao localAccountDao;
    private Map<String, Set<String>> resultAttributeMapping;
    private Set<String> possibleUserAttributes;
    private String unmappedUsernameAttribute = null;
    private String displayNameAttribute = "displayName";
    private Map<String, Set<String>> queryAttributeMapping;

    /**
     * Set the local portal account DAO.
     *
     * @param localAccountDao
     */
    public void setLocalAccountDao(ILocalAccountDao localAccountDao) {
        this.localAccountDao = localAccountDao;
    }

    public void setQueryAttributeMapping(final Map<String, ?> queryAttributeMapping) {
        final Map<String, Set<String>> parsedQueryAttributeMapping =
                MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(
                        queryAttributeMapping);

        if (parsedQueryAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException(
                    "The map from attribute names to attributes must not have any empty keys.");
        }

        this.queryAttributeMapping = parsedQueryAttributeMapping;
    }

    /** @return the resultAttributeMapping */
    public Map<String, Set<String>> getResultAttributeMapping() {
        return resultAttributeMapping;
    }
    /**
     * Set the {@link Map} to use for mapping from a data layer name to an attribute name or {@link
     * Set} of attribute names. Data layer names that are specified but have null mappings will use
     * the column name for the attribute name. Data layer names that are not specified as keys in
     * this {@link Map} will be ignored. <br>
     * The passed {@link Map} must have keys of type {@link String} and values of type {@link
     * String} or a {@link Set} of {@link String}.
     *
     * @param resultAttributeMapping {@link Map} from column names to attribute names, may not be
     *     null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setResultAttributeMapping(Map<String, ?> resultAttributeMapping) {
        final Map<String, Set<String>> parsedResultAttributeMapping =
                MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(
                        resultAttributeMapping);

        if (parsedResultAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException(
                    "The map from attribute names to attributes must not have any empty keys.");
        }

        final Collection<String> userAttributes =
                MultivaluedPersonAttributeUtils.flattenCollection(
                        parsedResultAttributeMapping.values());

        this.resultAttributeMapping = parsedResultAttributeMapping;
        this.possibleUserAttributes =
                Collections.unmodifiableSet(new LinkedHashSet<String>(userAttributes));
    }

    /** @return the userNameAttribute */
    public String getUnmappedUsernameAttribute() {
        return unmappedUsernameAttribute;
    }
    /**
     * The returned attribute to use as the userName for the mapped IPersons. If null the {@link
     * #setDefaultAttributeName(String)} value will be used and if that is null the {@link
     * AttributeNamedPersonImpl#DEFAULT_USER_NAME_ATTRIBUTE} value is used.
     *
     * @param userNameAttribute the userNameAttribute to set
     */
    public void setUnmappedUsernameAttribute(String userNameAttribute) {
        this.unmappedUsernameAttribute = userNameAttribute;
    }

    /**
     * Return the list of all possible attribute names. This implementation queries the database to
     * provide a list of all mapped attribute names, plus all attribute keys currently in-use in the
     * database.
     *
     * @return Set
     */
    public Set<String> getPossibleUserAttributeNames() {
        final Set<String> names = new HashSet<String>();
        names.addAll(this.possibleUserAttributes);
        names.addAll(localAccountDao.getCurrentAttributeNames());
        names.add(displayNameAttribute);
        return names;
    }

    /**
     * Return the list of all possible query attributes. This implementation queries the database to
     * provide a list of all mapped query attribute names, plus all attribute keys currently in-use
     * in the database.
     *
     * @return Set
     */
    public Set<String> getAvailableQueryAttributes() {
        if (this.queryAttributeMapping == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(this.queryAttributeMapping.keySet());
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    public final Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
            Map<String, List<Object>> query) {
        Validate.notNull(query, "query may not be null.");

        //Generate the query to pass to the subclass
        final LocalAccountQuery queryBuilder = this.generateQuery(query);
        if (queryBuilder == null) {
            this.logger.debug(
                    "No queryBuilder was generated for query " + query + ", null will be returned");

            return null;
        }

        //Get the username from the query, if specified
        final IUsernameAttributeProvider usernameAttributeProvider =
                this.getUsernameAttributeProvider();

        //Execute the query in the subclass
        final List<ILocalAccountPerson> unmappedPeople = localAccountDao.getPeople(queryBuilder);
        if (unmappedPeople == null) {
            return null;
        }

        //Map the attributes of the found people according to resultAttributeMapping if it is set
        final Set<IPersonAttributes> mappedPeople = new LinkedHashSet<IPersonAttributes>();
        for (final ILocalAccountPerson unmappedPerson : unmappedPeople) {
            final IPersonAttributes mappedPerson = this.mapPersonAttributes(unmappedPerson);
            mappedPeople.add(mappedPerson);
        }

        return Collections.unmodifiableSet(mappedPeople);
    }

    protected LocalAccountQuery generateQuery(Map<String, List<Object>> query) {
        LocalAccountQuery queryBuilder = new LocalAccountQuery();

        String userNameAttribute = this.getConfiguredUserNameAttribute();

        for (final Map.Entry<String, List<Object>> queryEntry : query.entrySet()) {

            String attrName = queryEntry.getKey();

            if (userNameAttribute.equals(attrName)) {
                String value = queryEntry.getValue().get(0).toString();
                queryBuilder.setUserName(value);
            } else {
                List<String> values = new ArrayList<String>();
                for (Object o : queryEntry.getValue()) {
                    values.add(o.toString());
                }
                queryBuilder.setAttribute(attrName, values);
            }
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug(
                    "Generated query builder '" + queryBuilder + "' from query Map " + query + ".");
        }

        return queryBuilder;
    }

    /**
     * This implementation uses the result attribute mapping to supplement, rather than replace, the
     * attributes returned from the database.
     */
    protected IPersonAttributes mapPersonAttributes(final ILocalAccountPerson person) {

        final Map<String, List<Object>> mappedAttributes =
                new LinkedHashMap<String, List<Object>>();
        mappedAttributes.putAll(person.getAttributes());

        // map the user's username to the portal's username attribute in the
        // attribute map
        mappedAttributes.put(
                this.getUsernameAttributeProvider().getUsernameAttribute(),
                Collections.<Object>singletonList(person.getName()));

        // if the user does not have a display name attribute set, attempt
        // to build one from the first and last name attributes
        if (!mappedAttributes.containsKey(displayNameAttribute)
                || mappedAttributes.get(displayNameAttribute).size() == 0
                || StringUtils.isBlank(
                        (String) mappedAttributes.get(displayNameAttribute).get(0))) {
            final List<Object> firstNames = mappedAttributes.get("givenName");
            final List<Object> lastNames = mappedAttributes.get("sn");
            final StringBuilder displayName = new StringBuilder();
            if (firstNames != null && firstNames.size() > 0) {
                displayName.append(firstNames.get(0)).append(" ");
            }
            if (lastNames != null && lastNames.size() > 0) {
                displayName.append(lastNames.get(0));
            }
            mappedAttributes.put(
                    displayNameAttribute,
                    Collections.<Object>singletonList(displayName.toString()));
        }

        for (final Map.Entry<String, Set<String>> resultAttrEntry :
                this.getResultAttributeMapping().entrySet()) {
            final String dataKey = resultAttrEntry.getKey();

            //Only map found data attributes
            if (mappedAttributes.containsKey(dataKey)) {
                Set<String> resultKeys = resultAttrEntry.getValue();

                //If dataKey has no mapped resultKeys just use the dataKey
                if (resultKeys == null) {
                    resultKeys = Collections.singleton(dataKey);
                }

                //Add the value to the mapped attributes for each mapped key
                final List<Object> value = mappedAttributes.get(dataKey);
                for (final String resultKey : resultKeys) {
                    if (resultKey == null) {
                        //TODO is this possible?
                        if (!mappedAttributes.containsKey(dataKey)) {
                            mappedAttributes.put(dataKey, value);
                        }
                    } else if (!mappedAttributes.containsKey(resultKey)) {
                        mappedAttributes.put(resultKey, value);
                    }
                }
            }
        }

        final IPersonAttributes newPerson;

        final String name = person.getName();
        if (name != null) {
            newPerson = new NamedPersonImpl(name, mappedAttributes);
        } else {
            final String userNameAttribute = this.getConfiguredUserNameAttribute();
            newPerson = new AttributeNamedPersonImpl(userNameAttribute, mappedAttributes);
        }

        return newPerson;
    }

    /**
     * @return The appropriate attribute to user for the user name. Since {@link
     *     #getDefaultAttributeName()} should never return null this method should never return null
     *     either.
     */
    protected String getConfiguredUserNameAttribute() {
        //If configured explicitly use it
        if (this.unmappedUsernameAttribute != null) {
            return this.unmappedUsernameAttribute;
        }

        final IUsernameAttributeProvider usernameAttributeProvider =
                this.getUsernameAttributeProvider();
        return usernameAttributeProvider.getUsernameAttribute();
    }
}
