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
package org.apereo.portal.ldap;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.jdbc.RDBMServices;
import org.apereo.portal.utils.PortalApplicationContextLocator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * Provides LDAP access in a way similar to a relational DBMS. This class was modified for the 2.4
 * release to function more like {@link RDBMServices}. The class should be used via the static
 * {@link #getDefaultLdapServer()} and {@link #getLdapServer(String name)} methods. <br>
 * <br>
 * Post 3.0 this class looks for an ILdapServer in the portal spring context named
 * 'defaultLdapServer' to use as the default LDAP server.
 *
 * @deprecated The prefered way to access configured ldap servers is using dependency injection and
 *     accessing the LdapContext instances in the spring context.
 */
public final class LdapServices {
    private static final String DEFAULT_LDAP_SERVER_NAME = "defaultLdapServer";

    private static final Log LOG = LogFactory.getLog(LdapServices.class);

    /**
     * Get the default {@link ILdapServer} by looking for a ILdapServer bean named
     * 'defaultLdapServer' in the portal spring context.
     *
     * @return The default {@link ILdapServer}.
     */
    public static ILdapServer getDefaultLdapServer() {
        return getLdapServer(DEFAULT_LDAP_SERVER_NAME);
    }

    /**
     * Get the {@link ILdapServer} from the portal spring context with the specified name.
     *
     * @param name The name of the ILdapServer to return.
     * @return An {@link ILdapServer} with the specified name, <code>null</code> if there is no
     *     connection with the specified name.
     */
    public static ILdapServer getLdapServer(String name) {
        final ApplicationContext applicationContext =
                PortalApplicationContextLocator.getApplicationContext();

        ILdapServer ldapServer = null;
        try {
            ldapServer = (ILdapServer) applicationContext.getBean(name, ILdapServer.class);
        } catch (NoSuchBeanDefinitionException nsbde) {
            //Ignore the exception for not finding the named bean.
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found ILdapServer='" + ldapServer + "' for name='" + name + "'");
        }

        return ldapServer;
    }

    /**
     * Get a {@link Map} of {@link ILdapServer} instances from the spring configuration.
     *
     * @return A {@link Map} of {@link ILdapServer} instances.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ILdapServer> getLdapServerMap() {
        final ApplicationContext applicationContext =
                PortalApplicationContextLocator.getApplicationContext();
        final Map<String, ILdapServer> ldapServers =
                applicationContext.getBeansOfType(ILdapServer.class);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found Map of ILdapServers=" + ldapServers + "'");
        }

        return Collections.unmodifiableMap(ldapServers);
    }

    /** This class only provides static methods. */
    private LdapServices() {
        // private constructor prevents instantiation of this
        // static service-providing class
    }
}
