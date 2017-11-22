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
package org.apereo.portal.layout.profile;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.security.IPerson;
import org.springframework.context.ApplicationEvent;

/**
 * Event : the indicated user requested the indicated profile fname in the context of the indicated
 * request.
 *
 * <p>Equals: two ProfileSelectionEvents are equal if they represent the same user requesting the
 * same profile key, regardless of what HttpServletRequest they are associated with.
 *
 * @since 4.2
 */
public class ProfileSelectionEvent extends ApplicationEvent {

    private final IPerson person;
    private final HttpServletRequest request;
    private final String requestedProfileKey;

    /**
     * Create a new ProfileSelectionEvent.
     *
     * @param source the component that published the event (never <code>null</code>)
     * @param requestedProfilekey non-null key of requested profile (not necessarily the fname)
     * @param person the non-null Person requesting the profile selection
     * @param request the non-null servlet request in the context of which the request is made
     */
    public ProfileSelectionEvent(
            final Object source,
            final String requestedProfilekey,
            final IPerson person,
            final HttpServletRequest request) {
        super(source);

        Validate.notNull(requestedProfilekey, "Users cannot select a null profile key.");
        Validate.notNull(person, "Null persons cannot request profiles.");
        Validate.notNull(request, "Persons cannot request profiles via null HttpServletRequests.");

        this.requestedProfileKey = requestedProfilekey;
        this.person = person;
        this.request = request;
    }

    public String getRequestedProfileKey() {
        return requestedProfileKey;
    }

    public IPerson getPerson() {
        return person;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    /*
     * ProfileSelectionEvents with equal source, requested profile key, and requesting person are equal.
     * The HttpServletRequest context for the event is ignored.
     */
    @Override
    public boolean equals(Object other) {

        if (null == other) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProfileSelectionEvent)) {
            return false;
        }

        ProfileSelectionEvent otherEvent = (ProfileSelectionEvent) other;

        return new EqualsBuilder()
                .append(this.source, otherEvent.source)
                .append(this.requestedProfileKey, otherEvent.requestedProfileKey)
                .append(this.person, otherEvent.person)
                .isEquals();
    }

    /*
     * See .equals().
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.source)
                .append(this.requestedProfileKey)
                .append(this.person)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", this.source)
                .append("requestedProfileKey", this.requestedProfileKey)
                .append("person", this.person)
                .toString();
    }
}
