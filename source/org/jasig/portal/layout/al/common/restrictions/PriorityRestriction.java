/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;


import java.util.StringTokenizer;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.ALNode;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.utils.CommonUtils;

/**
 * PriorityRestriction checks the priority restriction for a given ALNode object.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class PriorityRestriction extends ALRestriction {


         private int minPriority, maxPriority;

         public PriorityRestriction(String name,String nodePath) {
           super(name,nodePath);
         }

         public PriorityRestriction(String name) {
           super(name);
         }
         
         public PriorityRestriction() {
            super();
          }

         /**
           * Returns the maximum value of the given restriction
           * @return a maxPriority
          */
         public int getMaxValue() {
            return maxPriority;
         }


         /**
           * Returns the minimum value of the given restriction
           * @return a minPriority
          */
         public int getMinValue() {
            return minPriority;
         }

          /**
           * Returns the minimum and maximum values of the given restriction as an integer array
           * @return an integer array
          */
         public int[] getRange() {
            return new int[] { minPriority, maxPriority };
         }

          /**
            * Parses the restriction expression of the current node
            * @exception PortalException
            */
         protected void parseRestrictionExpression () throws PortalException {
          try {
            String restrictionExp = getRestrictionExpression();
            if ( restrictionExp.indexOf('-') > 0 ) {
             StringTokenizer st = new StringTokenizer(restrictionExp,"-");
             String min = st.nextToken();
             String max = st.nextToken();
             minPriority = CommonUtils.parseInt(min,0);
             maxPriority = CommonUtils.parseInt(max,Integer.MAX_VALUE);
            } else {
                minPriority = CommonUtils.parseInt(restrictionExp,0);
                maxPriority = minPriority;
              }
          } catch ( Exception e ) {
             throw new PortalException(e.getMessage());
            }
         }


         /**
           * Checks the restriction for the specified node
           * @param propertyValue a <code>String</code> property value to be checked
           * @exception PortalException
         */
         public boolean checkRestriction( String propertyValue ) throws PortalException {
           int priority = CommonUtils.parseInt(propertyValue,0);
           if ( priority <= maxPriority && priority >= minPriority )
             return true;
             return false;
         }


         /**
           * Checks the restriction for the current node
           * @exception PortalException
         */
         public boolean checkRestriction( ILayoutNode node ) throws PortalException {
         
          if ( !(node instanceof ALNode) )
          	throw new PortalException ( "The node must be ALNode type!");
          ALNode alNode = (ALNode) node;

          // if we have a related priority restriction we should check the priority ranges
          if ( nodePath != null && !nodePath.equals(LOCAL_RESTRICTION_PATH) ) {
             PriorityRestriction restriction = (PriorityRestriction) alNode.getRestriction(getUniqueKey(RestrictionTypes.PRIORITY_RESTRICTION));
             if ( restriction != null ) {
              int[] range = restriction.getRange();
              if ( minPriority > range[1] || maxPriority < range[0] )
                return false;
             }
                return true;
          }

           int priority = alNode.getPriority();
           if ( priority <= maxPriority && priority >= minPriority )
             return true;
             return false;
         }

         public void setRestriction ( int minPriority, int maxPriority ) {
           this.minPriority = minPriority;
           this.maxPriority = maxPriority;
           setRestrictionExpression(new String(minPriority+"-"+maxPriority));
         }


}
