/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;


import org.jasig.portal.layout.ALNode;
import org.jasig.portal.layout.IUserLayoutNodeDescription;

/**
 * HiddenRestriction checks the restriction on the "hidden" property for a given ALNode object.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class HiddenRestriction extends BooleanRestriction {


         public HiddenRestriction(String nodePath) {
           super(nodePath);
         }

         public HiddenRestriction() {
           super();
         }

         /**
           * Returns the type of the current restriction
           * @return a restriction type respresented in the <code>RestrictionTypes</code> interface
          */
         public int getRestrictionType() {
           return RestrictionTypes.HIDDEN_RESTRICTION|super.getRestrictionType();
         }


         /**
           * Gets the boolean property value for the specified node
         */
         protected boolean getBooleanPropertyValue( ALNode node ) {
           IUserLayoutNodeDescription nodeDesc = node.getNodeDescription();
           return nodeDesc.isHidden();
         }


}
