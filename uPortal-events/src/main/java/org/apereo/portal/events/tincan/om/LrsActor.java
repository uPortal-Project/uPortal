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
package org.apereo.portal.events.tincan.om;

public class LrsActor {
    private final String mbox;
    private final String name;
    private final String objectType = "Agent";

    public LrsActor(String mbox, String name) {
        this.mbox = mbox;
        this.name = name;
    }

    public String getMbox() {
        return mbox;
    }

    public String getName() {
        return name;
    }

    public String getObjectType() {
        return objectType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mbox == null) ? 0 : mbox.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LrsActor other = (LrsActor) obj;
        if (mbox == null) {
            if (other.mbox != null) return false;
        } else if (!mbox.equals(other.mbox)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "LrsActor [mbox=" + mbox + ", name=" + name + ", objectType=" + objectType + "]";
    }
}
