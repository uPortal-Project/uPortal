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
package org.apereo.portal.layout.dlm;

import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.spring.locator.UserLayoutStoreLocator;

/** @since 2.5 */
public final class Precedence {
    private double precedence = 0.0;
    private long fragmentId = -1;
    private static Precedence userPrecedence = new Precedence();

    private Precedence() {}

    static Precedence newInstance(String fragmentIdx) {
        if (fragmentIdx == null || fragmentIdx.equals("")) return userPrecedence;
        return new Precedence(fragmentIdx);
    }

    @Override
    public String toString() {
        return "p[" + precedence + ", " + fragmentId + "]";
    }

    private Precedence(String fragmentId) {
        long id;
        try {
            id = Long.parseLong(fragmentId);
        } catch (Exception e) {
            // if unparsable default to lowest priority.
            return;
        }

        final IUserLayoutStore dls = UserLayoutStoreLocator.getUserLayoutStore();

        this.precedence = dls.getFragmentPrecedence(id);
        this.fragmentId = id;
    }

    /**
     * Returns true of this complete precedence is less than the complete precedence of the passed
     * in Precedence object. The complete precedence takes into account the Id of the fragment
     * definition (so that no two fragments may have exactly the same precedence). If the
     * "precedence" value is equal then the precedence object with the lowest fragmentId has the
     * higher complete precedence.
     */
    public boolean isLessThan(Precedence p) {
        if (this.precedence < p.precedence
                || (this.precedence == p.precedence && this.fragmentId > p.fragmentId)) return true;
        return false;
    }

    public boolean isEqualTo(Precedence p) {
        return this.precedence == p.precedence;
    }

    public static Precedence getUserPrecedence() {
        return userPrecedence;
    }
}
