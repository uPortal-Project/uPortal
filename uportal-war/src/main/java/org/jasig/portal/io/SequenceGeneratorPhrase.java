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
import org.jasig.portal.spring.locator.CounterStoreLocator;
import org.jasig.portal.utils.ICounterStore;

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
        
        final ICounterStore counterStore = CounterStoreLocator.getCounterStore();
        try {
            return counterStore.getNextId(seqName);
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
