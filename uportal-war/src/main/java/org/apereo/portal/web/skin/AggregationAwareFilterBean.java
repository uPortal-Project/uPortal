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
package org.apereo.portal.web.skin;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Pays attention to the state of skin aggregation and only applies the filter if it is disabled
 *
 */
public class AggregationAwareFilterBean implements Filter {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private Filter filter;
    private ResourcesElementsProvider elementsProvider;

    /** The filter to delegate to */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Autowired
    public void setElementsProvider(ResourcesElementsProvider elementsProvider) {
        this.elementsProvider = elementsProvider;
    }

    @Override
    public void destroy() {
        this.filter.destroy();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filter.init(filterConfig);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (this.elementsProvider.getIncludedType((HttpServletRequest) request)
                == Included.AGGREGATED) {
            if (logger.isDebugEnabled()) {
                logger.debug("Aggregation enabled, delegating to filter: " + this.filter);
            }
            this.filter.doFilter(request, response, chain);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Aggregation disabled, skipping filter: " + this.filter);
            }
            chain.doFilter(request, response);
        }
    }
}
