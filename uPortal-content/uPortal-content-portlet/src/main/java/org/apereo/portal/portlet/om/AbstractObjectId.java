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
package org.apereo.portal.portlet.om;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 */
public abstract class AbstractObjectId implements IObjectId {
    private static final long serialVersionUID = 1L;

    private final String objectId;

    public AbstractObjectId(String objectId) {
        Validate.notNull(objectId, "objectId can not be null");

        this.objectId = objectId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletEntityID#getStringId()
     */
    public String getStringId() {
        return this.objectId;
    }

    /** @see java.lang.Object#equals(Object) */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IObjectId)) {
            return false;
        }
        IObjectId rhs = (IObjectId) object;
        return new EqualsBuilder().append(this.objectId, rhs.getStringId()).isEquals();
    }

    /** @see java.lang.Object#hashCode() */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-917388291, 674832463).append(this.objectId).toHashCode();
    }

    /** @see java.lang.Object#toString() */
    @Override
    public String toString() {
        return this.getStringId();
    }
}
