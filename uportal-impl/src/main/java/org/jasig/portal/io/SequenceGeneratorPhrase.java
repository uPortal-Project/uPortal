/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
import org.jasig.portal.services.SequenceGenerator;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SequenceGeneratorPhrase implements Phrase {

    public static final Reagent SEQ_NAME = new SimpleReagent("SEQ_NAME", "descendant-or-self::text()", ReagentType.PHRASE,
            String.class, "Name of the sequence to return the next id for");
    
    // Instance Members.
    private Phrase seqNamePhrase;

    /* (non-Javadoc)
     * @see org.danann.cernunnos.Bootstrappable#init(org.danann.cernunnos.EntityConfig)
     */
    public void init(EntityConfig config) {
        this.seqNamePhrase = (Phrase) config.getValue(SEQ_NAME);
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.Bootstrappable#getFormula()
     */
    public Formula getFormula() {
        return new SimpleFormula(SequenceGeneratorPhrase.class, new Reagent[] { SEQ_NAME });
    }
    
    /* (non-Javadoc)
     * @see org.danann.cernunnos.Phrase#evaluate(org.danann.cernunnos.TaskRequest, org.danann.cernunnos.TaskResponse)
     */
    public Object evaluate(TaskRequest req, TaskResponse res) {
        final String seqName = (String)this.seqNamePhrase.evaluate(req, res);
        
        final SequenceGenerator sequenceGenerator = SequenceGenerator.instance();
        try {
            return sequenceGenerator.getNextInt(seqName);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to retrieve next sequence in for sequance '" + seqName + "'", e);
        }
    }

    protected String getSafeFileName(String name) {
        //Replace slashes with .
        name = name.replaceAll("/|\\\\", ".");
        
        //Replace all non-ok char with _
        name = name.replaceAll("[^a-zA-Z0-9_.-]", "_");

        return name;
    }
}
