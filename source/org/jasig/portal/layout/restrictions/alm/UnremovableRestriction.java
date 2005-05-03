/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions.alm;


import org.jasig.portal.layout.node.ILayoutNode;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;

/**
 * UnremovableRestriction checks the restriction on the "unremovable"
 * property for a given ALNode object.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.restrictions.
 * It was moved to its present package to reflect that it is part of Aggregated Layouts.
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
