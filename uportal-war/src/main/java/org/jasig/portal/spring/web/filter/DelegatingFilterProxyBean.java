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
package org.jasig.portal.spring.web.filter;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Variation on {@link DelegatingFilterProxy} that accepts {@link FilterConfig}
 * overrides as {@link Properties}. Essentially the reverse of the original use case
 * for {@Link GenericFilterBean}. I.e. instead of mapping from {@link FilterConfig}
 * to individual Bean-style properties, we're going from a single Bean-style property
 * to {@link FilterConfig}. Would use this when you're trying to treat an existing
 * servlet {@link Filter} as a Spring bean, but that {@link Filter} needs its config
 * as a {@link FilterConfig} instance. In this case, you'd define a
 * {@link DelegatingFilterProxy} in {@code web.xml} with
 * {@code targetFilterLifecycle=true}) pointing to an instance of this
 * class defined as a Spring Bean, which would then points to the actual Filter, also
 * defined as a Spring Bean. That way, the top-level {@link FilterConfig} is passed
 * all the way down, but with the opportunity to override that config with
 * Spring-defined {@link Properties}.
 */
public class DelegatingFilterProxyBean extends DelegatingFilterProxy {

    private Properties filterConfigOverrides;

    public DelegatingFilterProxyBean() {
        // Can still disable this if you want, but would render this instance pointless
        super.setTargetFilterLifecycle(true);
    }

    /**
     * Wraps current {@link FilterConfig} with injected config before
     * passing it to the delegate. Most of this method is copy/paste from
     * the overridden method, but has to be factored this way because
     * {@link org.springframework.web.filter.DelegatingFilterProxy#getFilterConfig()}
     * and {@link org.springframework.web.filter.GenericFilterBean#init(javax.servlet.FilterConfig)}
     * are final.
     *
     * @param wac
     * @return
     * @throws ServletException
     */
    @Override
    protected Filter initDelegate(WebApplicationContext wac) throws ServletException {
        Filter delegate = wac.getBean(getTargetBeanName(), Filter.class);
        if (isTargetFilterLifecycle()) {
            delegate.init(getFilterConfigWithOverrides());
        }
        return delegate;
    }

    protected FilterConfig getFilterConfigWithOverrides() {
        Properties copyOfOverrides = new Properties();
        if ( filterConfigOverrides != null ) {
            copyOfOverrides.putAll(filterConfigOverrides);
        }
        return new WrappedFilterConfig(getFilterConfig(), copyOfOverrides);
    }

    public void setFilterConfigOverrides(Properties overrides) {
        this.filterConfigOverrides = overrides;
    }

    protected static class WrappedFilterConfig implements FilterConfig {
        private final Properties filterConfigOverrides;
        private FilterConfig filterConfig;
        private Set<String> filterConfigNames;

        public WrappedFilterConfig(FilterConfig filterConfig, Properties filterConfigOverrides) {
            this.filterConfig = filterConfig;
            this.filterConfigOverrides = filterConfigOverrides == null ? new Properties() : filterConfigOverrides;
            this.filterConfigNames = new HashSet<String>();
            final Enumeration<String> origNames = filterConfig.getInitParameterNames();
            while ( origNames.hasMoreElements() ) {
                filterConfigNames.add(origNames.nextElement());
            }
            final Enumeration<String> overrideNames = (Enumeration<String>) filterConfigOverrides.propertyNames();
            while ( overrideNames.hasMoreElements() ) {
                filterConfigNames.add(overrideNames.nextElement());
            }
        }

        @Override
        public String getFilterName() {
            return filterConfig.getFilterName();
        }

        @Override
        public ServletContext getServletContext() {
            return filterConfig.getServletContext();
        }

        @Override
        public String getInitParameter(String name) {
            return filterConfigOverrides.containsKey(name)
                    ? filterConfigOverrides.getProperty(name) : filterConfig.getInitParameter(name);
        }

        @Override
        public Enumeration getInitParameterNames() {
            return new IteratorEnumeration(filterConfigNames.iterator());
        }
    }
}
