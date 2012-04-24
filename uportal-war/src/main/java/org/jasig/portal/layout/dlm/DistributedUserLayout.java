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

package org.jasig.portal.layout.dlm;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.w3c.dom.Document;

/**
 * A user's layout and meta-data about that layout
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DistributedUserLayout {
    private final Document layout;
    private final Set<String> fragmentNames;
    private final IStylesheetUserPreferences distributedStructureStylesheetUserPreferences;
    private final IStylesheetUserPreferences distributedThemeStylesheetUserPreferences;

    /**
     * This constructor seems to be used with fragment owners.
     * 
     * @param layout
     */
    public DistributedUserLayout(Document layout) {
        this.layout = layout;
        this.fragmentNames = Collections.emptySet();
        this.distributedStructureStylesheetUserPreferences = null;
        this.distributedThemeStylesheetUserPreferences = null;
    }

    /**
     * This constructor seems to be used with non-fragment owners.
     * 
     * @param layout
     */
    public DistributedUserLayout(Document layout, Set<String> fragmentNames,
            IStylesheetUserPreferences distributedStructureStylesheetUserPreferences,
            IStylesheetUserPreferences distributedThemeStylesheetUserPreferences) {
        this.layout = layout;
        this.fragmentNames = Collections.unmodifiableSet(new LinkedHashSet<String>(fragmentNames));
        this.distributedStructureStylesheetUserPreferences = distributedStructureStylesheetUserPreferences;
        this.distributedThemeStylesheetUserPreferences = distributedThemeStylesheetUserPreferences;
    }

    /**
     * @return The layout document
     */
    public Document getLayout() {
        return this.layout;
    }

    /**
     * @return The names of all of the fragments incorperated into the layout
     */
    public Set<String> getFragmentNames() {
        return this.fragmentNames;
    }

    public IStylesheetUserPreferences getDistributedStructureStylesheetUserPreferences() {
        return this.distributedStructureStylesheetUserPreferences;
    }

    public IStylesheetUserPreferences getDistributedThemeStylesheetUserPreferences() {
        return this.distributedThemeStylesheetUserPreferences;
    }
}
