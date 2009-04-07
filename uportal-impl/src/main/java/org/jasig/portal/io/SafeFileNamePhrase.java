/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SafeFileNamePhrase implements Phrase {

    // Reserved names on Windows (see http://en.wikipedia.org/wiki/Filename)
    private static final String[] WINDOWS_INVALID_PATTERNS = new String[] {
                            "AUX",
                            "CLOCK\\$",
                            "COM\\d*",
                            "CON",
                            "LPT\\d*",
                            "NUL",
                            "PRN"
                        };

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
        //Replace slashes with .
        name = name.replaceAll("/|\\\\", ".");
        
        //Replace all non-ok char with _
        name = name.replaceAll("[^a-zA-Z0-9_.-]", "_");

        // Make sure the name doesn't violate a Windows reserved word...
        for (String pattern : WINDOWS_INVALID_PATTERNS) {
            if (name.toUpperCase().matches(pattern)) {
                name = "uP-" + name;
                break;
            }
        }

        return name;

    }

}
