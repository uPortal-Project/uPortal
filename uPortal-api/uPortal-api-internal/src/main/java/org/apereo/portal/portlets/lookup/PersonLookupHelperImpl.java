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
package org.apereo.portal.portlets.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.portlets.search.DisplayNameComparator;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.NamedPersonImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.webflow.context.ExternalContext;

/** Implements logic and helper methods for the person-lookup web flow. */
public class PersonLookupHelperImpl implements IPersonLookupHelper {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private IPersonAttributeDao personAttributeDao;

    public IPersonAttributeDao getPersonAttributeDao() {
        return personAttributeDao;
    }

    /** The {@link IPersonAttributeDao} used to perform lookups. */
    public void setPersonAttributeDao(IPersonAttributeDao personLookupDao) {
        this.personAttributeDao = personLookupDao;
    }

    private int maxResults = 10;

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getMaxResults() {
        return maxResults;
    }

    private int searchThreadCount = 10;

    // Default to a high enough value that it doesn't cause issues during a load test
    private long searchThreadTimeoutSeconds = 60;

    /**
     * Set the number of concurrent threads process person search results in parallel.
     *
     * @param searchThreadCount number of concurrent threads for processing search results
     */
    public void setSearchThreadCount(int searchThreadCount) {
        this.searchThreadCount = searchThreadCount;
    }

    /**
     * Set the max number of seconds the threads doing the person search results should wait for a
     * result. <br>
     * TODO This is to prevent waiting indefinitely on a hung worker thread. In the future, would be
     * nice to get the portlet's timeout value and set this to a second or two fewer.
     *
     * @param searchThreadTimeoutSeconds Maximum number of seconds to wait for thread to respond
     */
    public void setSearchThreadTimeoutSeconds(long searchThreadTimeoutSeconds) {
        this.searchThreadTimeoutSeconds = searchThreadTimeoutSeconds;
    }

    private ExecutorService executor;

    @PostConstruct
    public void initializeSearchExecutor() {
        executor = Executors.newFixedThreadPool(searchThreadCount);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlets.swapper.IPersonLookupHelper#getQueryAttributes(org.springframework.webflow.context.ExternalContext)
     */
    @Override
    public Set<String> getQueryAttributes(ExternalContext externalContext) {
        final PortletRequest portletRequest = (PortletRequest) externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();

        final Set<String> queryAttributes;
        final String[] configuredAttributes =
                preferences.getValues(PERSON_LOOKUP_PERSON_LOOKUP_QUERY_ATTRIBUTES, null);
        final String[] excludedAttributes =
                preferences.getValues(PERSON_LOOKUP_PERSON_LOOKUP_QUERY_ATTRIBUTES_EXCLUDES, null);

        // If attributes are configured in portlet prefs just use them
        if (configuredAttributes != null) {
            queryAttributes = new LinkedHashSet<>(Arrays.asList(configuredAttributes));
        }
        // Otherwise provide all available attributes from the IPersonAttributeDao
        else {
            final Set<String> availableAttributes =
                    this.personAttributeDao.getAvailableQueryAttributes(null);
            queryAttributes = new TreeSet<>(availableAttributes);
        }

        // Remove excluded attributes
        if (excludedAttributes != null) {
            for (final String excludedAttribute : excludedAttributes) {
                queryAttributes.remove(excludedAttribute);
            }
        }

        return queryAttributes;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlets.swapper.IPersonLookupHelper#getDisplayAttributes(org.springframework.webflow.context.ExternalContext)
     */
    @Override
    public Set<String> getDisplayAttributes(ExternalContext externalContext) {
        final PortletRequest portletRequest = (PortletRequest) externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();

        final Set<String> displayAttributes;
        final String[] configuredAttributes =
                preferences.getValues(PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES, null);
        final String[] excludedAttributes =
                preferences.getValues(
                        PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES_EXCLUDES, null);

        // If attributes are configured in portlet prefs use those the user has
        if (configuredAttributes != null) {
            displayAttributes = new LinkedHashSet<>();
            displayAttributes.addAll(Arrays.asList(configuredAttributes));
        }
        // Otherwise provide all available attributes from the IPersonAttributes
        else {
            displayAttributes =
                    new TreeSet<>(personAttributeDao.getPossibleUserAttributeNames(null));
        }

        // Remove any excluded attributes
        if (excludedAttributes != null) {
            for (final String excludedAttribute : excludedAttributes) {
                displayAttributes.remove(excludedAttribute);
            }
        }

        return displayAttributes;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlets.lookup.IPersonLookupHelper#getSelf(org.springframework.webflow.context.ExternalContext)
     */
    @Override
    public IPersonAttributes getSelf(ExternalContext externalContext) {

        final PortletRequest portletRequest = (PortletRequest) externalContext.getNativeRequest();
        final String username = portletRequest.getRemoteUser();

        return this.personAttributeDao.getPerson(username, null);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlets.lookup.IPersonLookupHelper#searchForPeople(org.apereo.portal.security.IPerson, java.util.Map)
     */
    @Override
    public List<IPersonAttributes> searchForPeople(
            final IPerson searcher, final Map<String, Object> query) {

        // get the IAuthorizationPrincipal for the searching user
        final IAuthorizationPrincipal principal = getPrincipalForUser(searcher);

        // build a set of all possible user attributes the current user has
        // permission to view
        final Set<String> permittedAttributes = getPermittedAttributes(principal);

        // remove any query attributes that the user does not have permission
        // to view
        final Map<String, Object> inUseQuery = new HashMap<>();
        for (Map.Entry<String, Object> queryEntry : query.entrySet()) {
            final String attr = queryEntry.getKey();
            if (permittedAttributes.contains(attr)) {
                inUseQuery.put(attr, queryEntry.getValue());
            } else {
                this.logger.warn(
                        "User '"
                                + searcher.getName()
                                + "' attempted searching on attribute '"
                                + attr
                                + "' which is not allowed in the current configuration. The attribute will be ignored.");
            }
        }

        // ensure the query has at least one search attribute defined
        if (inUseQuery.keySet().size() == 0) {
            throw new IllegalArgumentException("Search query is empty");
        }

        // get the set of people matching the search query
        final Set<IPersonAttributes> people = this.personAttributeDao.getPeople(inUseQuery, null);
        if (people == null) {
            return Collections.emptyList();
        }

        // To improve efficiency and not do as many permission checks or person directory searches,
        // if we have too many results and all people in the returned set of personAttributes have
        // a displayName, pre-sort the set and limit it to maxResults. The typical use case is that
        // LDAP returns results that have the displayName populated.  Note that a disadvantage of
        // this
        // approach is that the smaller result set may have entries that permissions prevent the
        // current users from viewing the person and thus reduce the number of final results, but
        // that is rare (typical use case is users can't view administrative internal accounts or
        // the
        // system account, none of which tend to be in LDAP).  We could retain a few more than
        // maxResults
        // to offset that chance, but IMHO not worth the cost of extra external queries.

        List<IPersonAttributes> peopleList = new ArrayList<>(people);
        if (peopleList.size() > maxResults && allListItemsHaveDisplayName(peopleList)) {
            logger.debug(
                    "All items contained displayName; pre-sorting list of size {} and truncating to {}",
                    peopleList.size(),
                    maxResults);
            // sort the list by display name
            Collections.sort(peopleList, new DisplayNameComparator());
            peopleList = peopleList.subList(0, maxResults);
        }
        // Construct a new representation of the persons limited to attributes the searcher
        // has permissions to view.  Will change order of the list.
        List<IPersonAttributes> list =
                getVisiblePersons(principal, permittedAttributes, peopleList);

        // Sort the list by display name
        Collections.sort(list, new DisplayNameComparator());

        // limit the list to a maximum number of returned results
        if (list.size() > maxResults) {
            list = list.subList(0, maxResults);
        }

        return list;
    }

    /**
     * Returns a list of the personAttributes that this principal has permission to view. This
     * implementation does the check on the list items in parallel because personDirectory is
     * consulted for non-admin principals to get the person attributes which is really slow if done
     * on N entries in parallel because personDirectory often goes out to LDAP or another external
     * source for additional attributes. This processing will not retain list order.
     *
     * @param principal user performing the search
     * @param permittedAttributes set of attributes the principal has permission to view
     * @param peopleList list of people returned from the search
     * @return UNORDERED list of visible persons
     */
    private List<IPersonAttributes> getVisiblePersons(
            final IAuthorizationPrincipal principal,
            final Set<String> permittedAttributes,
            List<IPersonAttributes> peopleList) {
        List<Future<IPersonAttributes>> futures = new ArrayList<>();
        List<IPersonAttributes> list = new ArrayList<>();

        // Ugly.  PersonDirectory requires RequestContextHolder to be set for each thread, so pass
        // it
        // into the callable.
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // For each person in the list, check to see if the current user has permission to view this
        // user
        for (IPersonAttributes person : peopleList) {
            Callable<IPersonAttributes> worker =
                    new FetchVisiblePersonCallable(
                            principal, person, permittedAttributes, requestAttributes);
            Future<IPersonAttributes> task = executor.submit(worker);
            futures.add(task);
        }
        for (Future<IPersonAttributes> future : futures) {
            try {
                final IPersonAttributes visiblePerson =
                        future.get(searchThreadTimeoutSeconds, TimeUnit.SECONDS);
                if (visiblePerson != null) {
                    list.add(visiblePerson);
                }
            } catch (InterruptedException e) {
                logger.error("Processing person search interrupted", e);
            } catch (ExecutionException e) {
                logger.error("Error Processing person search", e);
            } catch (TimeoutException e) {
                future.cancel(true);
                logger.warn(
                        "Exceeded {} ms waiting for getVisiblePerson to return result",
                        searchThreadTimeoutSeconds);
            }
        }
        logger.debug("Found {} results", list.size());
        return list;
    }

    /**
     * Utility class for executor framework for search persons processing. Ugly - person directory
     * needs the requestAttributes in the RequestContextHolder set for each thread from the caller.
     */
    private class FetchVisiblePersonCallable implements Callable<IPersonAttributes> {
        private IAuthorizationPrincipal principal;
        private IPersonAttributes person;
        private Set<String> permittedAttributes;
        private RequestAttributes requestAttributes;

        public FetchVisiblePersonCallable(
                IAuthorizationPrincipal principal,
                IPersonAttributes person,
                Set<String> permittedAttributes,
                RequestAttributes requestAttributes) {
            this.principal = principal;
            this.person = person;
            this.permittedAttributes = permittedAttributes;
            this.requestAttributes = requestAttributes;
        }

        /**
         * If the current user has permission to view this person, construct a new representation of
         * the person limited to attributes the searcher has permissions to view, else return null
         * if user cannot view.
         *
         * @return person attributes user can view. Null if user can't view person.
         * @throws Exception
         */
        @Override
        public IPersonAttributes call() throws Exception {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            return getVisiblePerson(principal, person, permittedAttributes);
        }
    }

    /**
     * Utility class to determine if all items in the list of people have a displayName.
     *
     * @param people list of personAttributes
     * @return true if all list items have an attribute displayName
     */
    private boolean allListItemsHaveDisplayName(List<IPersonAttributes> people) {
        for (IPersonAttributes person : people) {
            if (person.getAttributeValue("displayName") == null) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlets.lookup.IPersonLookupHelper#findPerson(org.apereo.portal.security.IPerson, java.lang.String)
     */
    @Override
    public IPersonAttributes findPerson(final IPerson searcher, final String username) {

        // get the IAuthorizationPrincipal for the searching user
        final IAuthorizationPrincipal principal = getPrincipalForUser(searcher);

        // build a set of all possible user attributes the current user has
        // permission to view
        final Set<String> permittedAttributes = getPermittedAttributes(principal);

        // get the set of people matching the search query
        final IPersonAttributes person = this.personAttributeDao.getPerson(username, null);

        if (person == null) {
            logger.info("No user found with username matching " + username);
            return null;
        }

        // if the current user has permission to view this person, construct
        // a new representation of the person limited to attributes the
        // searcher has permissions to view
        return getVisiblePerson(principal, person, permittedAttributes);
    }

    /**
     * Get the authoriztaion principal matching the supplied IPerson.
     *
     * @param person
     * @return
     */
    protected IAuthorizationPrincipal getPrincipalForUser(final IPerson person) {
        final EntityIdentifier ei = person.getEntityIdentifier();
        return AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
    }

    /**
     * Get the set of all user attribute names defined in the portal for which the specified
     * principal has the attribute viewing permission.
     *
     * @param principal
     * @return
     */
    protected Set<String> getPermittedAttributes(final IAuthorizationPrincipal principal) {
        final Set<String> attributeNames = personAttributeDao.getPossibleUserAttributeNames(null);
        return getPermittedAttributes(principal, attributeNames);
    }

    /**
     * Filter the specified set of user attribute names to contain only those that the specified
     * principal may perform <code>IPermission.VIEW_USER_ATTRIBUTE_ACTIVITY</code>. These are user
     * attributes the user has general permission to view, for all visible users.
     *
     * @param principal
     * @param attributeNames
     * @return
     */
    protected Set<String> getPermittedAttributes(
            final IAuthorizationPrincipal principal, final Set<String> attributeNames) {
        final Set<String> permittedAttributes = new HashSet<>();
        for (String attr : attributeNames) {
            if (principal.hasPermission(
                    IPermission.PORTAL_USERS, IPermission.VIEW_USER_ATTRIBUTE_ACTIVITY, attr)) {
                permittedAttributes.add(attr);
            }
        }
        return permittedAttributes;
    }

    /**
     * Provide a complete set of user attribute names that the specified principal may view within
     * his or her own collection. These will be attributes for which the user has <em>either</em>
     * <code>IPermission.VIEW_USER_ATTRIBUTE_ACTIVITY</code> or <code>
     * IPermission.VIEW_OWN_USER_ATTRIBUTE_ACTIVITY</code>.
     *
     * @param principal Represents a portal user who wishes to view user attributes
     * @param generallyPermittedAttributes The collection of user attribute name this user may view
     *     for any visible user
     * @since 5.0
     */
    protected Set<String> getPermittedOwnAttributes(
            final IAuthorizationPrincipal principal,
            final Set<String> generallyPermittedAttributes) {

        // The permttedOwnAttributes collection includes all the generallyPermittedAttributes
        final Set<String> result = new HashSet<>(generallyPermittedAttributes);

        for (String attr : personAttributeDao.getPossibleUserAttributeNames(null)) {
            if (principal.hasPermission(
                    IPermission.PORTAL_USERS, IPermission.VIEW_OWN_USER_ATTRIBUTE_ACTIVITY, attr)) {
                result.add(attr);
            }
        }

        return result;
    }

    /**
     * Filter an IPersonAttributes for a specified viewing principal. The returned person will
     * contain only the attributes provided in the permitted attributes list. <code>null</code> if
     * the principal does not have permission to view the user.
     *
     * @param principal
     * @param person
     * @param generallyPermittedAttributes
     * @return
     */
    protected IPersonAttributes getVisiblePerson(
            final IAuthorizationPrincipal principal,
            final IPersonAttributes person,
            final Set<String> generallyPermittedAttributes) {

        // first check to see if the principal has permission to view this person.  Unfortunately
        // for
        // non-admin users, this will result in a call to PersonDirectory (which may go out to LDAP
        // or
        // other external systems) to find out what groups the person is in to see if the principal
        // has permission or deny through one of the contained groups.
        if (person.getName() != null
                && principal.hasPermission(
                        IPermission.PORTAL_USERS,
                        IPermission.VIEW_USER_ACTIVITY,
                        person.getName())) {

            // If the user has permission, filter the person attributes according
            // to the specified permitted attributes;  the collection of permitted
            // attributes can be different based on whether the user is trying to
            // access information about him/herself.
            final Set<String> permittedAttributes =
                    person.getName().equals(principal.getKey())
                            ? getPermittedOwnAttributes(principal, generallyPermittedAttributes)
                            : generallyPermittedAttributes;
            final Map<String, List<Object>> visibleAttributes = new HashMap<>();
            for (String attr : person.getAttributes().keySet()) {
                if (permittedAttributes.contains(attr)) {
                    visibleAttributes.put(attr, person.getAttributeValues(attr));
                }
            }

            // use the filtered attribute list to create and return a new
            // person object
            final IPersonAttributes visiblePerson =
                    new NamedPersonImpl(person.getName(), visibleAttributes);
            return visiblePerson;

        } else {
            logger.debug(
                    "Principal "
                            + principal.getKey()
                            + " does not have permissions to view user "
                            + person.getName());
            return null;
        }
    }
}
