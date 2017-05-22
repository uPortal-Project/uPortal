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
package org.apereo.portal.groups.pags.dao;

import java.io.Serializable;
import org.apereo.portal.EntityIdentifier;

/** @since 5.0 */
/* package-private */ final class MembershipCacheKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EntityIdentifier groupId;
    private final EntityIdentifier memberId;

    public MembershipCacheKey(final EntityIdentifier groupId, final EntityIdentifier memberId) {
        this.groupId = groupId;
        this.memberId = memberId;
    }

    public EntityIdentifier getMemberId() {
        return memberId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((memberId == null) ? 0 : memberId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MembershipCacheKey other = (MembershipCacheKey) obj;
        if (groupId == null) {
            if (other.groupId != null) return false;
        } else if (!groupId.equals(other.groupId)) return false;
        if (memberId == null) {
            if (other.memberId != null) return false;
        } else if (!memberId.equals(other.memberId)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "MembershipCacheKey [groupId=" + groupId + ", memberId=" + memberId + "]";
    }
}
