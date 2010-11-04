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

package org.jasig.portal.channels;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.url.AbstractHttpServletRequestWrapper;

/**
 * Wrapper for all channels, scopes request attributes to just that channel
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ChannelHttpServletRequestWrapper extends AbstractHttpServletRequestWrapper {
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    
    public ChannelHttpServletRequestWrapper(HttpServletRequest httpServletRequest) {
        super(httpServletRequest);
    }

    @Override
    public Object getAttribute(String name) {
        final Object attribute = this.attributes.get(name);
        if (attribute != null) {
            return attribute;
        }
        
        return super.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        final Set<String> attributeNames = this.attributes.keySet();
        return Collections.enumeration(attributeNames);
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.attributes.put(name, o);
    }
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof ChannelHttpServletRequestWrapper)) {
            return false;
        }
        ChannelHttpServletRequestWrapper rhs = (ChannelHttpServletRequestWrapper) object;
        return new EqualsBuilder()
            .append(this.getWrappedRequest(), rhs.getWrappedRequest())
            .append(this.attributes, rhs.attributes)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(500546767, -122181035)
            .append(this.getWrappedRequest())
            .append(this.attributes)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("wrappedRequest", this.getWrappedRequest())
            .append("attributes", this.attributes)
            .toString();
    }

}
