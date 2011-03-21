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


import org.danann.cernunnos.AttributePhrase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.ReturnValue;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.dom4j.Element;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.dlm.RDBMDistributedLayoutStore;

/**
 * Cernunnos <code>Task</code> that inspects a finished XML entity document and 
 * generates an appropriate file name, including the appropriate extension.  
 * <em>Appropriate</em> means, in this case, that 2 logically separate entities 
 * will never produce the same file name, and that generated file names will be 
 * legal on all supported operating systems.
 * 
 * @author Drew Wills
 * @version $Revision$
 */
public class GenerateEntityFileNameTask implements Task {

    public static final Reagent ENTITY_ELEMENT = new SimpleReagent("ENTITY_ELEMENT", 
                "@entity-element", ReagentType.PHRASE, Element.class, 
                "Root element of the finished XML entity document", 
                new AttributePhrase(Attributes.NODE));

    public static final Reagent LAYOUT_STORE = new SimpleReagent("LAYOUT_STORE", 
                "@layout-store", ReagentType.PHRASE, IUserLayoutStore.class, 
                "The portal's running IUserLayoutStore instance", 
                new AttributePhrase("layoutStore"));

    // Instance Members.
    private Phrase entityElement;
    private Phrase layoutStore;

    /* (non-Javadoc)
     * @see org.danann.cernunnos.Bootstrappable#init(org.danann.cernunnos.EntityConfig)
     */
    public void init(EntityConfig config) {
        
        // Lock & load reagents
        this.entityElement = (Phrase) config.getValue(ENTITY_ELEMENT);
        this.layoutStore = (Phrase) config.getValue(LAYOUT_STORE);
        
    }

    /* (non-Javadoc)
     * @see org.danann.cernunnos.Bootstrappable#getFormula()
     */
    public Formula getFormula() {
        return new SimpleFormula(GenerateEntityFileNameTask.class, new Reagent[] { ENTITY_ELEMENT, LAYOUT_STORE });
    }
    
    /* (non-Javadoc)
     * @see org.danann.cernunnos.Phrase#evaluate(org.danann.cernunnos.TaskRequest, org.danann.cernunnos.TaskResponse)
     */
    public void perform(TaskRequest req, TaskResponse res) {
        final Element rootElement = (Element) entityElement.evaluate(req, res);
        final IUserLayoutStore rdbmdls = (IUserLayoutStore) layoutStore.evaluate(req, res);
        SupportedFileTypes y = SupportedFileTypes.getApplicableFileType(rootElement, rdbmdls);
        String entityFileName = y.getSafeFileNameWithExtension(rootElement);
        ReturnValue rslt = (ReturnValue) req.getAttribute(Attributes.RETURN_VALUE);
        rslt.setValue(entityFileName);
    }

}
