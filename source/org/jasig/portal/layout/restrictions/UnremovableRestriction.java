/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;


import org.jasig.portal.layout.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.ILayoutNode;

/**
 * UnremovableRestriction checks the restriction on the "unremovable"
 * property for a given ALNode object.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class UnremovableRestriction extends BooleanRestriction {


         public UnremovableRestriction(String name,String nodePath) {
           super(name,nodePath);
         }
         
         public UnremovableRestriction(String name) {
            super(name);
         }
         
         public UnremovableRestriction() {
            super();
         }

         /**
           * Gets the boolean property value for the specified node
         */
         protected boolean getBooleanPropertyValue( ILayoutNode node ) {
           IUserLayoutNodeDescription nodeDesc = node.getNodeDescription();
           return nodeDesc.isUnremovable();
         }


}
