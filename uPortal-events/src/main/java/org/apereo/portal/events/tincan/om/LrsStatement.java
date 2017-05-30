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

public class LrsStatement {
    private final LrsActor actor;
    private final LrsVerb verb;
    private final LrsObject object;

    public LrsStatement(LrsActor actor, LrsVerb verb, LrsObject object) {
        this.actor = actor;
        this.verb = verb;
        this.object = object;
    }

    public LrsActor getActor() {
        return actor;
    }

    public LrsVerb getVerb() {
        return verb;
    }

    public LrsObject getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actor == null) ? 0 : actor.hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result + ((verb == null) ? 0 : verb.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LrsStatement other = (LrsStatement) obj;
        if (actor == null) {
            if (other.actor != null) return false;
        } else if (!actor.equals(other.actor)) return false;
        if (object == null) {
            if (other.object != null) return false;
        } else if (!object.equals(other.object)) return false;
        if (verb != other.verb) return false;
        return true;
    }

    @Override
    public String toString() {
        return "LrsStatement [actor=" + actor + ", verb=" + verb + ", object=" + object + "]";
    }
}
