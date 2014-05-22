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

package org.jasig.portal.persondir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import org.jasig.services.persondir.util.CaseCanonicalizationMode;

/**
 * LocalAccountPersonAttributeDao provides a person directory implementation
 * that uses uPortal's internal account store.  This implementation overrides
 * several methods of AbstractQueryPersonAttributeDao to allow for the use of
 * arbitrary user attributes without requiring an administrator to add new
 * entries to the result or query attribute mappings.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class LocalAccountPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    public static final CaseCanonicalizationMode DEFAULT_CASE_CANONICALIZATION_MODE = CaseCanonicalizationMode.LOWER;
    public static final CaseCanonicalizationMode DEFAULT_USERNAME_CASE_CANONICALIZATION_MODE = CaseCanonicalizationMode.NONE;
    private ILocalAccountDao localAccountDao;
    private Map<String, Set<String>> resultAttributeMapping;
    private Map<String, CaseCanonicalizationMode> caseInsensitiveResultAttributes;
    private Map<String, CaseCanonicalizationMode> caseInsensitiveQueryAttributes;
    private CaseCanonicalizationMode defaultCaseCanonicalizationMode = DEFAULT_CASE_CANONICALIZATION_MODE;
    private CaseCanonicalizationMode usernameCaseCanonicalizationMode = DEFAULT_USERNAME_CASE_CANONICALIZATION_MODE;
    private Locale caseCanonicalizationLocale = Locale.getDefault();
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
        final Map<String, Set<String>> parsedQueryAttributeMapping = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(queryAttributeMapping);
        
        if (parsedQueryAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
        
        this.queryAttributeMapping = parsedQueryAttributeMapping;
    }
    
    /**
     * @return the resultAttributeMapping
     */
    public Map<String, Set<String>> getResultAttributeMapping() {
        return resultAttributeMapping;
    }
    /**
     * Set the {@link Map} to use for mapping from a data layer name to an attribute name or {@link Set} of attribute
     * names. Data layer names that are specified but have null mappings will use the column name for the attribute
     * name. Data layer names that are not specified as keys in this {@link Map} will be ignored.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values of type {@link String} or a {@link Set} 
     * of {@link String}.
     * 
     * @param resultAttributeMapping {@link Map} from column names to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setResultAttributeMapping(Map<String, ?> resultAttributeMapping) {
        final Map<String, Set<String>> parsedResultAttributeMapping = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(resultAttributeMapping);
        
        if (parsedResultAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
        
        final Collection<String> userAttributes = MultivaluedPersonAttributeUtils.flattenCollection(parsedResultAttributeMapping.values());
        
        this.resultAttributeMapping = parsedResultAttributeMapping;
        this.possibleUserAttributes = Collections.unmodifiableSet(new LinkedHashSet<String>(userAttributes));
    }
    
    /**
     * @return the userNameAttribute
     */
    public String getUnmappedUsernameAttribute() {
        return unmappedUsernameAttribute;
    }
    /**
     * The returned attribute to use as the userName for the mapped IPersons. If null the {@link #setDefaultAttributeName(String)}
     * value will be used and if that is null the {@link AttributeNamedPersonImpl#DEFAULT_USER_NAME_ATTRIBUTE} value is
     * used. 
     * 
     * @param userNameAttribute the userNameAttribute to set
     */
    public void setUnmappedUsernameAttribute(String userNameAttribute) {
        this.unmappedUsernameAttribute = userNameAttribute;
    }

    /**
     * Return the list of all possible attribute names.  This implementation
     * queries the database to provide a list of all mapped attribute names, 
     * plus all attribute keys currently in-use in the database.
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
     * Return the list of all possible query attributes.  This implementation
     * queries the database to provide a list of all mapped query attribute names, 
     * plus all attribute keys currently in-use in the database.
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
    public final Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
        Validate.notNull(query, "query may not be null.");
        
        //Generate the query to pass to the subclass
        final LocalAccountQuery queryBuilder = this.generateQuery(query);
        if (queryBuilder == null) {
            this.logger.debug("No queryBuilder was generated for query " + query + ", null will be returned");
            
            return null;
        }
        
        //Get the username from the query, if specified
        final IUsernameAttributeProvider usernameAttributeProvider = this.getUsernameAttributeProvider();
        
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
                String value = canonicalizeAttribute(attrName, queryEntry.getValue(), caseInsensitiveQueryAttributes).get(0).toString();
                queryBuilder.setUserName(value);
            } else {

                Set<String> mappedAttrNames = null;
                if ( this.queryAttributeMapping != null ) {
                    mappedAttrNames = this.queryAttributeMapping.get(attrName);
                }
                if ( mappedAttrNames == null || mappedAttrNames.isEmpty() ) {
                    mappedAttrNames = new HashSet<String>();
                    mappedAttrNames.add(attrName);
                }

                for ( String mappedAttrName : mappedAttrNames ) {
                    if ( userNameAttribute.equals(mappedAttrName) ) {
                        // Special handling since username isn't actually a proper attribute in the underlying data
                        // store
                        String value = canonicalizeAttribute(attrName, queryEntry.getValue(), caseInsensitiveQueryAttributes).get(0).toString();
                        queryBuilder.setUserName(value);
                        continue;
                    }
                    List<Object> values = new ArrayList<Object>();
                    for (Object o : queryEntry.getValue()) {
                        values.add(o.toString());
                    }
                    values = canonicalizeAttribute(attrName, values, caseInsensitiveQueryAttributes);
                    List<String> valueStrs = new ArrayList<String>();
                    for (Object o : values) {
                        valueStrs.add((String)o);
                    }
                    queryBuilder.setAttribute(mappedAttrName, valueStrs);
                }
            }
            
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Generated query builder '" + queryBuilder + "' from query Map " + query + ".");
        }
        
        return queryBuilder;
    }

    /**
     * This implementation uses the result attribute mapping to supplement, rather
     * than replace, the attributes returned from the database.
     */
    protected IPersonAttributes mapPersonAttributes(final ILocalAccountPerson person) {

        Map<String, List<Object>> mappedAttributes = new LinkedHashMap<String, List<Object>>();
        mappedAttributes.putAll(person.getAttributes());

        // map the user's username to the portal's username attribute in the
        // attribute map
        mappedAttributes.put(this.getUsernameAttributeProvider().getUsernameAttribute(), Collections
                .<Object> singletonList(usernameCaseCanonicalizationMode.canonicalize(person.getName())));

        if (this.getResultAttributeMapping() == null) {
            if (caseInsensitiveResultAttributes != null && !(caseInsensitiveResultAttributes.isEmpty())) {
                Map<String,List<Object>> canonicalizedMappedAttributes = new LinkedHashMap<String, List<Object>>();
                for ( Map.Entry<String,List<Object>> attribute : mappedAttributes.entrySet() ) {
                    String attributeName = attribute.getKey();
                    canonicalizedMappedAttributes.put(attributeName, canonicalizeAttribute(attributeName, attribute.getValue(), caseInsensitiveResultAttributes));
                }
                mappedAttributes = canonicalizedMappedAttributes;
            }
        } else {
            for (final Map.Entry<String, Set<String>> resultAttrEntry : this.getResultAttributeMapping().entrySet()) {
                final String dataKey = resultAttrEntry.getKey();

                //Only map found data attributes
                if (mappedAttributes.containsKey(dataKey)) {
                    Set<String> resultKeys = resultAttrEntry.getValue();

                    //If dataKey has no mapped resultKeys just use the dataKey
                    if (resultKeys == null) {
                        resultKeys = Collections.singleton(dataKey);
                    }

                    //Add the value to the mapped attributes for each mapped key
                    List<Object> value = mappedAttributes.get(dataKey);
                    for (final String resultKey : resultKeys) {
                        value = canonicalizeAttribute(resultKey, value, caseInsensitiveResultAttributes);
                        if (resultKey == null) {
                            //TODO is this possible?
                            if (!mappedAttributes.containsKey(dataKey)) {
                                mappedAttributes.put(dataKey, value);
                            }
                        }
                        else if (!mappedAttributes.containsKey(resultKey)) {
                            mappedAttributes.put(resultKey, value);
                        }
                    }
                }
            }
        }

        // if the user does not have a display name attribute set, attempt
        // to build one from the first and last name attributes.
        //
        // this goes *after* the case canonicalization because we're potentially
        // calculating what amounts to an application layer (i.e. logical)
        // attribute value from what are expected to be data-layer
        // (i.e. physical) attributes, so the data-layer attributes must have
        // already been fully processed.
        if (!mappedAttributes.containsKey(displayNameAttribute) || mappedAttributes.get(displayNameAttribute).size() == 0 || StringUtils.isBlank((String) mappedAttributes.get(displayNameAttribute).get(0))) {
            final List<Object> firstNames = mappedAttributes.get("givenName");
            final List<Object> lastNames = mappedAttributes.get("sn");
            final StringBuilder displayName = new StringBuilder();
            if (firstNames != null && firstNames.size() > 0) {
                displayName.append(firstNames.get(0)).append(" ");
            }
            if (lastNames != null && lastNames.size() > 0) {
                displayName.append(lastNames.get(0));
            }
            mappedAttributes.put(displayNameAttribute, Collections.<Object>singletonList(displayName.toString()));
        }

        final IPersonAttributes newPerson;

        final String name = person.getName();
        if (name != null) {
            newPerson = new NamedPersonImpl(usernameCaseCanonicalizationMode.canonicalize(name), mappedAttributes);
        }
        else {
            final String userNameAttribute = this.getConfiguredUserNameAttribute();
            final IPersonAttributes tmpNewPerson = new AttributeNamedPersonImpl(userNameAttribute, mappedAttributes);
            newPerson = new NamedPersonImpl(usernameCaseCanonicalizationMode.canonicalize(tmpNewPerson.getName()), mappedAttributes);
        }

        return newPerson;
    }

    protected List<Object> canonicalizeAttribute(String key, List<Object> value, Map<String, CaseCanonicalizationMode> config) {
        if (value == null || value.isEmpty() || config == null || !(config.containsKey(key))) {
            return value;
        }
        CaseCanonicalizationMode canonicalizationMode = config.get(key);
        if ( canonicalizationMode == null ) {
            // Intentionally late binding of the default to
            // avoid unexpected behavior if you wait to assign
            // the default until after you've injected the list
            // of case-insensitive fields
            canonicalizationMode = defaultCaseCanonicalizationMode;
        }
        List<Object> canonicalizedValues = new ArrayList<Object>(value.size());
        for ( Object origValue : value ) {
            if ( origValue instanceof String ) {
                canonicalizedValues.add(canonicalizationMode.canonicalize((String) origValue, caseCanonicalizationLocale));
            } else {
                canonicalizedValues.add(origValue);
            }
        }
        return canonicalizedValues;
    }


        /**
         * @return The appropriate attribute to user for the user name. Since {@link #getDefaultAttributeName()} should
         * never return null this method should never return null either.
         */
    protected String getConfiguredUserNameAttribute() {
        //If configured explicitly use it
        if (this.unmappedUsernameAttribute != null) {
            return this.unmappedUsernameAttribute;
        }
        
        final IUsernameAttributeProvider usernameAttributeProvider = this.getUsernameAttributeProvider();
        return usernameAttributeProvider.getUsernameAttribute();
    }

    /**
     * @see #setCaseInsensitiveResultAttributes(java.util.Map)
     *
     * @return
     */
    public Map<String,CaseCanonicalizationMode> getCaseInsensitiveResultAttributes() {
        return caseInsensitiveResultAttributes;
    }

    /**
     * Keys are app-layer attributes, values are the casing canonicalization
     * modes for each, as applied when mapping from data-layer to application
     * attributes. {@code null} values treated as
     * {@link #DEFAULT_CASE_CANONICALIZATION_MODE} unless that default mode is
     * overridden with
     * {@link #setDefaultCaseCanonicalizationMode(org.jasig.services.persondir.util.CaseCanonicalizationMode)}.
     *
     * <p>Most commonly used for canonicalizing attributes used as unique
     * identifiers, usually username. In that case it's a good idea to
     * set that configuration here as well as via
     * {@link #setUsernameCaseCanonicalizationMode(org.jasig.services.persondir.util.CaseCanonicalizationMode)}
     * to ensure you don't end up with different results from
     * {@link org.jasig.services.persondir.IPersonAttributes#getName()} and
     * {@link IPersonAttributes#getAttributeValue(String)}.</p>
     *
     * <p>This config is separate from {@link #setCaseInsensitiveQueryAttributes(java.util.Map)}
     * because you may want to support case-insensitive searching on data
     * layer attributes, but respond with attribute values which preserve
     * the original data layer casing.</p>
     *
     * @param caseInsensitiveResultAttributes
     */
    public void setCaseInsensitiveResultAttributes(Map<String, CaseCanonicalizationMode> caseInsensitiveResultAttributes) {
        this.caseInsensitiveResultAttributes = caseInsensitiveResultAttributes;
    }

    /**
     * Configuration convenience same as passing the given {@code Set} as the
     * keys in the {@link #setCaseInsensitiveResultAttributes(java.util.Map)}
     * {@code Map} and implicitly accepting the default canonicalization mode
     * for each. Note that this setter will not assign canonicalization modes,
     * meaning that you needn't ensure
     * {@link #setDefaultCaseCanonicalizationMode(org.jasig.services.persondir.util.CaseCanonicalizationMode)}
     * has already been called.
     *
     * @param caseInsensitiveResultAttributes
     */
    public void setCaseInsensitiveResultAttributesAsCollection(Collection<String> caseInsensitiveResultAttributes) {
        if (caseInsensitiveResultAttributes == null || caseInsensitiveResultAttributes.isEmpty()) {
            setCaseInsensitiveResultAttributes(null);
        } else {
            Map<String, CaseCanonicalizationMode> asMap = new HashMap<String, CaseCanonicalizationMode>();
            for ( String attrib : caseInsensitiveResultAttributes ) {
                asMap.put(attrib, null);
            }
            setCaseInsensitiveResultAttributes(asMap);
        }
    }

    /**
     * Configuration convenience same as passing the given {@code Set} as the
     * keys in the {@link #setCaseInsensitiveQueryAttributes(java.util.Map)}
     * {@code Map} and implicitly accepting the default canonicalization mode
     * for each. Note that this setter will not assign canonicalization modes,
     * meaning that you needn't ensure
     * {@link #setDefaultCaseCanonicalizationMode(org.jasig.services.persondir.util.CaseCanonicalizationMode)}
     * has already been called.
     *
     * @param caseInsensitiveQueryAttributes
     */
    public void setCaseInsensitiveQueryAttributesAsCollection(Collection<String> caseInsensitiveQueryAttributes) {
        if (caseInsensitiveQueryAttributes == null || caseInsensitiveQueryAttributes.isEmpty()) {
            setCaseInsensitiveQueryAttributes(null);
        } else {
            Map<String, CaseCanonicalizationMode> asMap = new HashMap<String, CaseCanonicalizationMode>();
            for ( String attrib : caseInsensitiveQueryAttributes ) {
                asMap.put(attrib, null);
            }
            setCaseInsensitiveQueryAttributes(asMap);
        }
    }

    /**
     * Keys are app-layer attributes, values are the casing canonicalization
     * modes for each, as applied when mapping from an application layer
     * query to a data layer query. {@code null} values treated as
     * {@link #DEFAULT_CASE_CANONICALIZATION_MODE} unless that default mode is
     * overridden with
     * {@link #setDefaultCaseCanonicalizationMode(org.jasig.services.persondir.util.CaseCanonicalizationMode)}.
     *
     * <p>Use this for any attribute for which you'd like to support
     * case-insensitive search and where the underlying data layer is
     * case-sensitive. Of course, if the data layer does not store these
     * attributes in the canonical casing, the data layer itself would also need
     * to be canonicalized in a subclass-specific fashion. For example, see
     * {@link org.jasig.services.persondir.support.jdbc.AbstractJdbcPersonAttributeDao#setCaseInsensitiveDataAttributes(java.util.Map)}.</p>
     *
     * <p>This config is separate from {@link #setCaseInsensitiveResultAttributes(java.util.Map)}
     * because you may want to support case-insensitive searching on data
     * layer attributes, but respond with attribute values which preserve
     * the original data layer casing.</p>
     *
     * @param caseInsensitiveQueryAttributes
     */
    public void setCaseInsensitiveQueryAttributes(Map<String, CaseCanonicalizationMode> caseInsensitiveQueryAttributes) {
        this.caseInsensitiveQueryAttributes = caseInsensitiveQueryAttributes;
    }

    /**
     * @see #setCaseInsensitiveQueryAttributes(java.util.Map)
     * @return
     */
    public Map<String, CaseCanonicalizationMode> getCaseInsensitiveQueryAttributes() {
        return caseInsensitiveQueryAttributes;
    }

    /**
     * Assign the {@link Locale} in which all casing canonicaliztions will occur.
     * A {@code null} will be treated as {@link java.util.Locale#getDefault()}.
     *
     * @param caseCanonicalizationLocale
     */
    public void setCaseCanonicalizationLocale(Locale caseCanonicalizationLocale) {
        if ( caseCanonicalizationLocale == null ) {
            this.caseCanonicalizationLocale = Locale.getDefault();
        } else {
            this.caseCanonicalizationLocale = caseCanonicalizationLocale;
        }
    }

    public Locale getCaseCanonicalizationLocale() {
        return caseCanonicalizationLocale;
    }

    /**
     * Override the default {@link CaseCanonicalizationMode}
     * ({@link #DEFAULT_CASE_CANONICALIZATION_MODE}). Cannot be unset. A
     * {@link null} will have the same effect as reverting to the default.
     *
     * @param defaultCaseCanonicalizationMode
     */
    public void setDefaultCaseCanonicalizationMode(CaseCanonicalizationMode defaultCaseCanonicalizationMode) {
        if ( defaultCaseCanonicalizationMode == null ) {
            this.defaultCaseCanonicalizationMode = DEFAULT_CASE_CANONICALIZATION_MODE;
        } else {
            this.defaultCaseCanonicalizationMode = defaultCaseCanonicalizationMode;
        }
    }

    public CaseCanonicalizationMode getDefaultCaseCanonicalizationMode() {
        return defaultCaseCanonicalizationMode;
    }

    /**
     * Username canonicalization is a special case because
     * {@link #mapPersonAttributes(org.jasig.services.persondir.IPersonAttributes)}
     * doesn't know where it came from. It might have come from a data layer
     * attribute. Or it might have been derived from the original query itself.
     * There is no general way to query the {@link IPersonAttributes} to
     * determine exactly what happened.
     *
     * <p>If you need to guarantee case-insensitive usernames
     * <em>in {@link org.jasig.services.persondir.IPersonAttributes#getName()}</em>,
     * set this property to the same mode that you set for your app-layer
     * username attribute(s) via
     * {@link #setCaseInsensitiveResultAttributes(java.util.Map)}.</p>
     *
     * <p>Otherwise, leave this property alone and no attempt will be made
     * to canonicalize the value returned from
     * {@link org.jasig.services.persondir.IPersonAttributes#getName()}</p>
     *
     * <p>That default behavior is consistent with the legacy behavior of
     * being case sensitive w/r/t usernames.</p>
     *
     * @param usernameCaseCanonicalizationMode
     */
    public void setUsernameCaseCanonicalizationMode(CaseCanonicalizationMode usernameCaseCanonicalizationMode) {
        if ( usernameCaseCanonicalizationMode == null ) {
            this.usernameCaseCanonicalizationMode = DEFAULT_USERNAME_CASE_CANONICALIZATION_MODE;
        } else {
            this.usernameCaseCanonicalizationMode = usernameCaseCanonicalizationMode;
        }
    }

    /**
     * @see #setUsernameCaseCanonicalizationMode(org.jasig.services.persondir.util.CaseCanonicalizationMode)
     * @return
     */
    public CaseCanonicalizationMode getUsernameCaseCanonicalizationMode() {
        return this.usernameCaseCanonicalizationMode;
    }
}
