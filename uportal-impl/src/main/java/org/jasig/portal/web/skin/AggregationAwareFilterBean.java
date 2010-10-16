/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.web.skin;

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
import org.jasig.resource.aggr.om.Included;
import org.jasig.resource.aggr.util.ResourcesElementsProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Pays attention to the state of skin aggregation and only applies the fitler if it is disabled
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AggregationAwareFilterBean implements Filter {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private Filter filter;
    private ResourcesElementsProvider elementsProvider;

    /**
     * The filter to delegate to
     */
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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (this.elementsProvider.getIncludedType((HttpServletRequest)request) == Included.AGGREGATED) {
            if (logger.isDebugEnabled()) {
                logger.debug("Aggregation enabled, delegating to filter: " + this.filter);
            }
            this.filter.doFilter(request, response, chain);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Aggregation disabled, skipping filter: " + this.filter);
            }
            chain.doFilter(request, response);
        }
    }
}
