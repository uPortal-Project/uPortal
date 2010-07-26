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
