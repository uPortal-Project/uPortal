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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated Use the {@link GenerateEntityFileNameTask} instead
 */
@Deprecated
public class SafeFileNamePhrase implements Phrase {

    // Reserved names on Windows (see http://en.wikipedia.org/wiki/Filename)
    private static final Pattern[] WINDOWS_INVALID_PATTERNS = new Pattern[] {
                            Pattern.compile("AUX"),
                            Pattern.compile("CLOCK\\$"),
                            Pattern.compile("COM\\d*"),
                            Pattern.compile("CON"),
                            Pattern.compile("LPT\\d*"),
                            Pattern.compile("NUL"),
                            Pattern.compile("PRN")
                        };
    
    private static final Map<Pattern, String> REPLACEMENT_PAIRS;
    
    static {
        final Map<Pattern, String> pairs = new LinkedHashMap<Pattern, String>();
        pairs.put(Pattern.compile("/|\\\\"), ".");
        pairs.put(Pattern.compile("[~`@\\|\\s#$\\*]"), "_");
        REPLACEMENT_PAIRS = Collections.unmodifiableMap(pairs);
    }

    public static final Reagent HUMAN_FILE_NAME = new SimpleReagent("HUMAN_FILE_NAME", "descendant-or-self::text()", ReagentType.PHRASE,
            String.class, "Human readable version of the file name to make safe");
    
    // Instance Members.
    private Phrase humanFileNamePhrase;

    /* (non-Javadoc)
     * @see org.danann.cernunnos.Bootstrappable#init(org.danann.cernunnos.EntityConfig)
     */
    public void init(EntityConfig config) {
        this.humanFileNamePhrase = (Phrase) config.getValue(HUMAN_FILE_NAME);
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.Bootstrappable#getFormula()
     */
    public Formula getFormula() {
        return new SimpleFormula(SafeFileNamePhrase.class, new Reagent[] { HUMAN_FILE_NAME });
    }
    
    /* (non-Javadoc)
     * @see org.danann.cernunnos.Phrase#evaluate(org.danann.cernunnos.TaskRequest, org.danann.cernunnos.TaskResponse)
     */
    public Object evaluate(TaskRequest req, TaskResponse res) {
        final String humanFileName = (String)this.humanFileNamePhrase.evaluate(req, res);
        
        return this.getSafeFileName(humanFileName);
    }

    protected String getSafeFileName(String name) {
        //Replace invalid characters
        for (final Map.Entry<Pattern, String> pair : REPLACEMENT_PAIRS.entrySet()) {
            final Pattern pattern = pair.getKey();
            final Matcher matcher = pattern.matcher(name);
            name = matcher.replaceAll(pair.getValue());
        }

        // Make sure the name doesn't violate a Windows reserved word...
        final String upperCaseName = name.toUpperCase();
        for (Pattern pattern : WINDOWS_INVALID_PATTERNS) {
            if (pattern.matcher(upperCaseName).matches()) {
                name = "uP-" + name;
                break;
            }
        }

        return name;

    }

}
