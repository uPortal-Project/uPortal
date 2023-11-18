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
package org.apereo.portal.context.persondir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.sql.DataSource;
import net.sf.ehcache.Cache;
import org.apereo.portal.persondir.ILocalAccountDao;
import org.apereo.portal.persondir.ImpersonationStatusPersonAttributeDao;
import org.apereo.portal.persondir.LocalAccountPersonAttributeDao;
import org.apereo.portal.persondir.PortalRootPersonAttributeDao;
import org.apereo.portal.persondir.support.PersonManagerCurrentUserProvider;
import org.apereo.portal.utils.cache.MapCacheProvider;
import org.apereo.portal.utils.cache.PersonDirectoryCacheKeyGenerator;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.AdditionalDescriptors;
import org.apereo.services.persondir.support.AdditionalDescriptorsPersonAttributeDao;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.IAdditionalDescriptors;
import org.apereo.services.persondir.support.ICurrentUserProvider;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.apereo.services.persondir.support.MediatingAdditionalDescriptors;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.apereo.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;
import org.apereo.services.persondir.support.web.RequestAttributeSourceFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springmodules.cache.key.CacheKeyGenerator;

/**
 * This @Configuration class sets up (roughly) the same beans that personDirectoryContext.xml did in
 * uP4.
 *
 * @since 5.0
 */
@Configuration
public class PersonDirectoryConfiguration {

    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String REMOTE_USER_ATTRIBUTE = "remoteUser";
    private static final String SERVER_NAME_ATTRIBUTE = "serverName";
    private static final String USER_AGENT_KEY = "User-Agent";
    private static final String AGENT_DEVICE_ATTRIBUTE = "agentDevice";
    private static final String USER_INFO_CACHE_NAME =
            "org.apereo.services.persondir.USER_INFO.merged";
    private static final String GIVEN_NAME_ATTRIBUTE = "givenName";
    private static final String SIR_NAME_ATTRIBUTE = "sn";
    private static final String UID_ATTRIBUTE = "uid";
    private static final String USER_LOGIN_ID_ATTRIBUTE = "user.login.id";
    private static final String USERNAME_COLUMN_NAME = "USER_NAME";

    @Autowired
    @Qualifier(value = USER_INFO_CACHE_NAME)
    private Cache userInfoCache;

    @Resource(name = "localAccountDao")
    private ILocalAccountDao localAccountDao;

    @Resource(name = "PersonDB")
    private DataSource personDb;

    /** Provides user name for the current portal user if the thread is handling a request. */
    @Bean(name = "currentUserProvider")
    public ICurrentUserProvider getCurrentUserProvider() {
        return new PersonManagerCurrentUserProvider();
    }

    /** Provides the default username attribute to use to the rest of the DAOs. */
    @Bean(name = "usernameAttributeProvider")
    public IUsernameAttributeProvider getUsernameAttributeProvider() {
        final SimpleUsernameAttributeProvider result = new SimpleUsernameAttributeProvider();
        result.setUsernameAttribute(USERNAME_ATTRIBUTE);
        return result;
    }

    @Bean(name = "userAttributeCacheKeyGenerator")
    public CacheKeyGenerator getUserAttributeCacheKeyGenerator() {
        final PersonDirectoryCacheKeyGenerator result = new PersonDirectoryCacheKeyGenerator();
        result.setIgnoreEmptyAttributes(true);
        return result;
    }

    @Bean(name = "sessionScopeAdditionalDescriptors")
    @Scope(value = "globalSession", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public IAdditionalDescriptors getSessionScopeAdditionalDescriptors() {
        return new AdditionalDescriptors();
    }

    @Bean(name = "requestScopeAdditionalDescriptors")
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public IAdditionalDescriptors getRequestScopeAdditionalDescriptors() {
        return new AdditionalDescriptors();
    }

    /**
     * Session-scoped descriptors object. One of these will exist for each user in their session. It
     * will store the attributes from the request set by the requestAttributeSourceFilter. This must
     * hold both a session-scoped bean and request-scoped bean. See
     * http://permalink.gmane.org/gmane.comp.java.jasig.uportal/10771 for more information.
     */
    @Bean(name = "requestAdditionalDescriptors")
    public IAdditionalDescriptors getRequestAdditionalDescriptors() {
        final MediatingAdditionalDescriptors result = new MediatingAdditionalDescriptors();
        final List<IAdditionalDescriptors> delegateDescriptors = new ArrayList<>();
        delegateDescriptors.add(getSessionScopeAdditionalDescriptors());
        delegateDescriptors.add(getRequestScopeAdditionalDescriptors());
        result.setDelegateDescriptors(delegateDescriptors);
        return result;
    }

    /** Servlet filter that creates an attribute for the serverName */
    @Bean(name = "requestAttributeSourceFilter")
    public Filter getRequestAttributeSourceFilter() {
        final RequestAttributeSourceFilter result = new RequestAttributeSourceFilter();
        result.setAdditionalDescriptors(getRequestAdditionalDescriptors());
        result.setUsernameAttribute(REMOTE_USER_ATTRIBUTE); // Looks wrong, but correct
        result.setRemoteUserAttribute(REMOTE_USER_ATTRIBUTE);
        result.setServerNameAttribute(SERVER_NAME_ATTRIBUTE);

        /*
         * The processing position should be set to BOTH for uPortal because the session-scoped bean
         * sessionScopeAdditionalDescriptors gets cleared during /Login processing and request
         * attributes (particularly from HTTP Headers populated by Shibboleth) would be lost.  By
         * executing the filter both before and after, the latter execution will re-add the request
         * attributes to the session-scoped bean. See
         * http://permalink.gmane.org/gmane.comp.java.jasig.uportal/10771 for more information.
         */
        result.setProcessingPosition(RequestAttributeSourceFilter.ProcessingPosition.BOTH);

        Set<String> userAgent = new HashSet<>();
        userAgent.add(AGENT_DEVICE_ATTRIBUTE);
        Map<String, Set<String>> headerAttributeMapping = new HashMap<>();
        headerAttributeMapping.put(USER_AGENT_KEY, userAgent);
        result.setHeaderAttributeMapping(headerAttributeMapping);

        return result;
    }

    /**
     * Store attribute overrides in a session scoped map to ensure overrides don't show up for other
     * users and swapped attributes will be cleaned up on user logout.
     */
    @Bean(name = "sessionAttributesOverridesMap")
    @Scope(value = "globalSession", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Map getSessionAttributesOverridesMap() {
        return new ConcurrentHashMap();
    }

    /**
     * Overrides DAO acts as the root, it handles incorporating attributes from the attribute
     * swapper utility, wraps the caching DAO
     */
    @Bean(name = "personAttributeDao")
    @Qualifier("personAttributeDao")
    public IPersonAttributeDao getPersonAttributeDao() {
        final PortalRootPersonAttributeDao result = new PortalRootPersonAttributeDao();
        result.setDelegatePersonAttributeDao(getRequestAttributeMergingDao());
        result.setAttributeOverridesMap(getSessionAttributesOverridesMap());
        return result;
    }

    /** Merges attributes from the request with those from other DAOs. */
    @Bean(name = "requestAttributeMergingDao")
    @Qualifier("uPortalInternal")
    public IPersonAttributeDao getRequestAttributeMergingDao() {
        final MergingPersonAttributeDaoImpl result = new MergingPersonAttributeDaoImpl();
        result.setUsernameAttributeProvider(getUsernameAttributeProvider());
        result.setMerger(new ReplacingAttributeAdder());
        final List<IPersonAttributeDao> daos = new ArrayList<>();
        daos.add(getRequestAttributesDao());
        daos.add(getCachingPersonAttributeDao());
        result.setPersonAttributeDaos(daos);
        return result;
    }

    /**
     * The person attributes DAO that returns the attributes from the request. Uses a
     * currentUserProvider since the username may not always be provided by the request object.
     */
    @Bean(name = "requestAttributesDao")
    @Qualifier("uPortalInternal")
    public IPersonAttributeDao getRequestAttributesDao() {
        final AdditionalDescriptorsPersonAttributeDao result =
                new AdditionalDescriptorsPersonAttributeDao();
        result.setDescriptors(getRequestAdditionalDescriptors());
        result.setUsernameAttributeProvider(getUsernameAttributeProvider());
        result.setCurrentUserProvider(getCurrentUserProvider());
        return result;
    }

    /**
     * Defines the order that the data providing DAOs are called, results are cached by the outer
     * caching DAO.
     */
    @Bean(name = "cachingPersonAttributeDao")
    @Qualifier("uPortalInternal")
    public IPersonAttributeDao getCachingPersonAttributeDao() {
        final CachingPersonAttributeDaoImpl result = new CachingPersonAttributeDaoImpl();
        result.setUsernameAttributeProvider(getUsernameAttributeProvider());
        result.setCacheNullResults(true);
        result.setCacheKeyGenerator(getUserAttributeCacheKeyGenerator());
        result.setUserInfoCache(new MapCacheProvider<>(userInfoCache));
        result.setCachedPersonAttributesDao(getMergingPersonAttributeDao());
        return result;
    }

    @Bean(name = "mergingPersonAttributeDao")
    @Qualifier("uPortalInternal")
    public IPersonAttributeDao getMergingPersonAttributeDao() {
        final MergingPersonAttributeDaoImpl result = new MergingPersonAttributeDaoImpl();
        result.setUsernameAttributeProvider(getUsernameAttributeProvider());

        /*
         * This is a "first one wins" strategy. I.e. the first value found for any given result
         * attribute will be assigned to the user. Different values found in subsequently queried
         * attribute sources will be ignored. Suitable if uP-local attributes should always take
         * precedence.
         *
         * Other options (all in the same package):
         *   - MultivaluedAttributeMerger - Collects values from all DAOs into lists (does not
         *     filter out duplicate values, though)
         *   - ReplacingAttributeAdder - "Last one wins" strategy. I.e. the opposite of
         *     NoncollidingAttributeAdder.
         */
        result.setMerger(new NoncollidingAttributeAdder());

        /*
         * NB:  The beans in the  innerMergedPersonAttributeDaoList -- together with adopter-defined
         * IPersonAttributeDao beans -- will be added to the mergingPersonAttributeDao by the
         * AdopterDataSourcesIncorporator.
         */

        return result;
    }

    /**
     * IPersonAttributeDao beans defined by implementors will be added to this list when the
     * ApplicationContext comes up.
     */
    @Bean(name = "innerMergedPersonAttributeDaoList")
    public List<IPersonAttributeDao> getInnerMergedPersonAttributeDaoList() {
        final List<IPersonAttributeDao> result = new ArrayList<>();
        result.add(getImpersonationStatusPersonAttributeDao());
        result.add(getUPortalAccountUserSource());
        result.add(getUPortalJdbcUserSource());
        return result;
    }

    /**
     * Provides a single attribute (but only for the current logged in user):
     * impersonating='true'|'false'
     */
    @Bean(name = "impersonationStatusPersonAttributeDao")
    @Qualifier("uPortalInternal")
    public IPersonAttributeDao getImpersonationStatusPersonAttributeDao() {
        return new ImpersonationStatusPersonAttributeDao();
    }

    /**
     * Looks in the local person-directory data. This is only used for portal-local users such as
     * fragment owners All attributes are searchable via this configuration, results are cached by
     * the underlying DAO.
     */
    @Bean(name = "uPortalAccountUserSource")
    @Qualifier("uPortalInternal")
    public IPersonAttributeDao getUPortalAccountUserSource() {
        final LocalAccountPersonAttributeDao result = new LocalAccountPersonAttributeDao();
        result.setLocalAccountDao(localAccountDao);
        result.setUsernameAttributeProvider(getUsernameAttributeProvider());

        final Map<String, String> queryAttributeMapping = new HashMap<>();
        queryAttributeMapping.put(USERNAME_ATTRIBUTE, USERNAME_ATTRIBUTE);
        queryAttributeMapping.put(GIVEN_NAME_ATTRIBUTE, GIVEN_NAME_ATTRIBUTE);
        queryAttributeMapping.put(SIR_NAME_ATTRIBUTE, SIR_NAME_ATTRIBUTE);
        result.setQueryAttributeMapping(queryAttributeMapping);

        final Map<String, Set<String>> resultAttributeMapping = new HashMap<>();
        resultAttributeMapping.put(
                USERNAME_ATTRIBUTE,
                Stream.of(USERNAME_ATTRIBUTE, UID_ATTRIBUTE, USER_LOGIN_ID_ATTRIBUTE)
                        .collect(Collectors.toSet()));
        result.setResultAttributeMapping(resultAttributeMapping);

        return result;
    }

    /**
     * Looks in the base UP_USER table, doesn't find attributes but will ensure a result if it the
     * user exists in the portal database and is searched for by username, results are cached by the
     * outer caching DAO.
     */
    @Bean(name = "uPortalJdbcUserSource")
    @Qualifier("uPortalInternal")
    public IPersonAttributeDao getUPortalJdbcUserSource() {
        final String sql = "SELECT USER_NAME FROM UP_USER WHERE {0}";
        final SingleRowJdbcPersonAttributeDao result =
                new SingleRowJdbcPersonAttributeDao(personDb, sql);
        result.setUsernameAttributeProvider(getUsernameAttributeProvider());
        result.setQueryAttributeMapping(
                Collections.singletonMap(USERNAME_ATTRIBUTE, USERNAME_COLUMN_NAME));
        final Map<String, Set<String>> resultAttributeMapping = new HashMap<>();
        resultAttributeMapping.put(
                USERNAME_COLUMN_NAME,
                Stream.of(USERNAME_ATTRIBUTE, UID_ATTRIBUTE, USER_LOGIN_ID_ATTRIBUTE)
                        .collect(Collectors.toSet()));
        result.setResultAttributeMapping(resultAttributeMapping);
        return result;
    }
}
