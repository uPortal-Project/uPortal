/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;


import org.jasig.portal.layout.al.common.node.ILayoutNode;

/**
 * HiddenRestriction checks the restriction on the "hidden" property for a given ALNode object.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class HiddenRestriction extends BooleanRestriction {


         public HiddenRestriction(String name,RestrictionPath nodePath) {
           super(name,nodePath);
         }

         public HiddenRestriction(String name) {
           super(name);
         }

         public HiddenRestriction() {
            super();
         }

         /**
          * Returns the type of the current restriction.
          * @return a <code>RestrictionType</code> type
          */
         protected RestrictionType getType() {
           return RestrictionType.HIDDEN_RESTRICTION;
         }
         
         /**
           * Gets the boolean property value for the specified node
         */
         protected boolean getBooleanPropertyValue( ILayoutNode node ) {
           return node.isHidden();
         }


}
