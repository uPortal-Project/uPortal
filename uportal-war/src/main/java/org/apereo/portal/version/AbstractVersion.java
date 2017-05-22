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
package org.apereo.portal.version;

import java.io.Serializable;
import org.apereo.portal.version.om.Version;

/**
 * Base class for versions that implements a "correct" equals hashCode, equals and toString
 *
 */
public abstract class AbstractVersion implements Version, Serializable {
    private static final long serialVersionUID = 1L;

    private int hashCode = 0;

    @Override
    public final boolean isBefore(Version other) {
        return compareTo(other) < 0;
    }

    @Override
    public final boolean isAfter(Version other) {
        return compareTo(other) > 1;
    }

    @Override
    public final int compareTo(Version o) {
        int diff = getMajor() - o.getMajor();
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }

        diff = getMinor() - o.getMinor();
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }

        diff = getPatch() - o.getPatch();
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }

        final Integer l = getLocal();
        final Integer ol = o.getLocal();
        if (l == ol) {
            return 0;
        }
        if (l == null) {
            return -1;
        }
        if (ol == null) {
            return 1;
        }
        return l.compareTo(ol);
    }

    @Override
    public final int hashCode() {
        int result = hashCode;
        if (result == 0) {
            final int prime = 31;
            result = 1;
            result = prime * result + getMajor();
            result = prime * result + getMinor();
            result = prime * result + getPatch();
            final Integer local = getLocal();
            result = prime * result + ((local == null) ? 0 : local.hashCode());
            hashCode = result;
        }
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.hashCode() != this.hashCode()) return false;
        if (!(obj instanceof Version)) return false;
        Version other = (Version) obj;
        if (getMajor() != other.getMajor()) return false;
        if (getMinor() != other.getMinor()) return false;
        if (getPatch() != other.getPatch()) return false;
        final Integer local = getLocal();
        final Integer oLocal = other.getLocal();
        if (local == null) {
            if (oLocal != null) return false;
        } else if (!local.equals(oLocal)) return false;
        return true;
    }

    @Override
    public final String toString() {
        final Integer local = getLocal();
        if (local == null) {
            return getMajor() + "." + getMinor() + "." + getPatch();
        }
        return getMajor() + "." + getMinor() + "." + getPatch() + "." + local;
    }
}
