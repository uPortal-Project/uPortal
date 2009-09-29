/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.url;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AbstractPortalUrl {
    protected final HttpServletRequest request;
    protected final IUrlGenerator urlGenerator;
    protected final ConcurrentMap<String, List<String>> portalParameters = new ConcurrentHashMap<String, List<String>>();
    
    protected AbstractPortalUrl(HttpServletRequest request, IUrlGenerator urlGenerator) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(urlGenerator, "urlGenerator can not be null");
        
        this.request = request;
        this.urlGenerator = urlGenerator;
    }

    public final Map<String, List<String>> getPortalParameters() {
        return this.portalParameters;
    }

    public final void setPortalParameter(String name, String... values) {
        Validate.notNull(name, "name can not be null");
        Validate.noNullElements(values, "values can not be null or contain null elements");
        
        final List<String> valuesList = new ArrayList<String>(values.length);
        for (final String value : values) {
            valuesList.add(value);
        }
        
        this.portalParameters.put(name, valuesList);
    }

    public final void setPortalParameters(Map<String, List<String>> parameters) {
        this.portalParameters.clear();
        this.portalParameters.putAll(parameters);
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.portalParameters.toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-942605321, 2130461357)
            .append(this.portalParameters)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof AbstractPortalUrl)) {
            return false;
        }
        AbstractPortalUrl rhs = (AbstractPortalUrl) object;
        return new EqualsBuilder()
            .append(this.portalParameters, rhs.portalParameters)
            .isEquals();
    }
}