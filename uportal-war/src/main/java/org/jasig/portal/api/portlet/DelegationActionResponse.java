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

package org.jasig.portal.api.portlet;

import javax.portlet.ActionResponse;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.url.IPortalUrlBuilder;

/**
 * The resulting state of the delegation action request
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DelegationActionResponse extends DelegationResponse {
    private final String redirectUrl;
    private final IPortalUrlBuilder renderUrl;

    public DelegationActionResponse(DelegateState delegateState, String redirectUrl) {
        super(delegateState);
        this.redirectUrl = redirectUrl;
        this.renderUrl = null;
    }

    public DelegationActionResponse(DelegateState delegateState, IPortalUrlBuilder renderUrl) {
        super(delegateState);
        this.redirectUrl = null;
        this.renderUrl = renderUrl;
    }

    /**
     * @return The url specified in {@link ActionResponse#sendRedirect(String)}, null if no redirect was sent
     */
    public String getRedirectUrl() {
        return this.redirectUrl;
    }

    /**
     * @return The render PortletUrl resulting from the delegate action completing, null if a redirect was sent
     */
    public IPortalUrlBuilder getRenderUrl() {
        return this.renderUrl;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DelegationActionResponse)) {
            return false;
        }
        DelegationActionResponse rhs = (DelegationActionResponse) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.redirectUrl, rhs.redirectUrl)
            .append(this.renderUrl, rhs.renderUrl)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1445247369, -1009176817)
            .appendSuper(super.hashCode())
            .append(this.redirectUrl)
            .append(this.renderUrl)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .appendSuper(super.toString())
            .append("redirectUrl", this.redirectUrl)
            .append("renderUrl", this.renderUrl)
            .toString();
    }
}
