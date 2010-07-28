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

package org.jasig.portal.layout.dlm.providers;

import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * SubscribedTabEvaluatorFactory serves as the EvaluatorFactory implementation
 * for SubscribedTabEvaluators.  This evalutator factory does not recognize any
 * child elements.  The factory will automatically configure evaluators using
 * the ownerID attribute associated with the parent element fragmentDefinition
 * element.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class SubscribedTabEvaluatorFactory implements EvaluatorFactory {

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.layout.dlm.EvaluatorFactory#getEvaluator(org.w3c.dom.Node)
     */
    public Evaluator getEvaluator(Node audience) {
        
        // use the ownerID associated with the parent element
        Node parent = audience.getParentNode();
        NamedNodeMap attributes = parent.getAttributes();
        Node owner = attributes.getNamedItem("ownerID");
        
        String ownerId = owner.getNodeValue();
        return new SubscribedTabEvaluator(ownerId);
    }

}
