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
package org.apereo.portal.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.Validate;
import org.apereo.portal.security.IPerson;

/**
 * @since 2.6
 */
public abstract class LayoutPortalEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;

    private final long layoutId;
    private final String layoutOwner;

    @JsonIgnore private final IPerson layoutOwnerPerson;

    LayoutPortalEvent() {
        super();
        this.layoutId = -1;
        this.layoutOwnerPerson = null;
        this.layoutOwner = null;
    }

    LayoutPortalEvent(PortalEventBuilder portalEventBuilder, IPerson layoutOwner, long layoutId) {
        super(portalEventBuilder);
        Validate.notNull(layoutOwner, "layoutOwner");

        this.layoutId = layoutId;
        this.layoutOwnerPerson = layoutOwner;
        this.layoutOwner = this.layoutOwnerPerson.getUserName();
    }

    /** @return the layoutId */
    public long getLayoutId() {
        return this.layoutId;
    }

    /** @return the layoutOwner */
    public String getLayoutOwner() {
        return this.layoutOwner;
    }

    /** @return the layoutOwnerPerson, may be null, if so fall back to {@link #getLayoutOwner()} */
    public IPerson getLayoutOwnerPerson() {
        return this.layoutOwnerPerson;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString()
                + ", layoutId="
                + this.layoutId
                + ", layoutOwner="
                + this.layoutOwner
                + ", layoutOwnerPerson="
                + this.layoutOwnerPerson;
    }
}
