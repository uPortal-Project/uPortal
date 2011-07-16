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

package org.jasig.portal.io;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

/**
 * Used by Import/Export to provide a default <code>FILE_PATTERN</code> that
 * works on all operating systems.
 */
public class FilePatternPhrase implements Phrase {

    // Static Members.
    public static final String DEFAULT_VALUE = ".*";
    public static final String USE_DEFAULT_VALUE = "org.jasig.portal.FilePatternPhrase.USE_DEFAULT_VALUE";

    // Instance Members.
    private Phrase pattern;

    /*
     * Public API.
     */

    public static final Reagent PATTERN = new SimpleReagent("PATTERN", "descendant-or-self::text()",
                ReagentType.PHRASE, String.class, "File pattern to use in an Import operation.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {PATTERN};
        return new SimpleFormula(FilePatternPhrase.class, reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.pattern = (Phrase) config.getValue(PATTERN);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {

        String rslt = DEFAULT_VALUE;

        String p = ((String) pattern.evaluate(req, res)).trim();
        if (p != null && p.length() !=0 && !p.equals(USE_DEFAULT_VALUE)) {
            rslt = p;
        }

        return rslt;

    }

}
