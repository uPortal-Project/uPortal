/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.layout.restrictions;


import java.util.StringTokenizer;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.ALNode;
import org.jasig.portal.utils.CommonUtils;

/**
 * PriorityRestriction checks the priority restriction for a given ALNode object.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class PriorityRestriction extends UserLayoutRestriction {


         private int minPriority, maxPriority;

         public PriorityRestriction(String nodePath) {
           super(nodePath);
         }

         public PriorityRestriction() {
           super(LOCAL_RESTRICTION);
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
           * Returns the type of the current restriction
           * @return a restriction type respresented in the <code>RestrictionTypes</code> interface
          */
         public int getRestrictionType() {
          return RestrictionTypes.PRIORITY_RESTRICTION|super.getRestrictionType();
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
         public boolean checkRestriction( ALNode node ) throws PortalException {

          // if we have a related priority restriction we should check the priority ranges
          if ( nodePath != null && !nodePath.equals(LOCAL_RESTRICTION) ) {
             PriorityRestriction restriction = (PriorityRestriction) node.getRestriction(getRestrictionName(RestrictionTypes.PRIORITY_RESTRICTION,null));
             if ( restriction != null ) {
              int[] range = restriction.getRange();
              if ( minPriority > range[1] || maxPriority < range[0] )
                return false;
             }
                return true;
          }

           int priority = node.getPriority();
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
