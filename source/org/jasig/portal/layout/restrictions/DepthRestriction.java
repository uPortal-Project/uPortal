/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;


import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.jasig.portal.PortalException;
import org.jasig.portal.utils.CommonUtils;

/**
 * DepthRestriction checks the priority restriction for a given UserLayoutNode object.
 * <p>
 * Company: Instructional Media &amp; Magic
 * 
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class DepthRestriction extends ALRestriction {


         private Integer[] minDepthArray, maxDepthArray, depthArray;

         public DepthRestriction(String nodePath) {
            super(nodePath);
         }

         public DepthRestriction() {
            super();
         }

         /**
           * Returns the type of the current restriction
           * @return a restriction type respresented in the <code>RestrictionTypes</code> interface
           */
          public int getRestrictionType() {
           return RestrictionTypes.DEPTH_RESTRICTION;
          }

          /**
            * Parses the restriction expression of the current node
            * @exception PortalException
            */
         protected void parseRestrictionExpression() throws PortalException {
          try {
            String restrictionExp = getRestrictionExpression();
            List minDepthList = new Vector();
            List maxDepthList = new Vector();
            List depthList = new Vector();
            StringTokenizer st = new StringTokenizer(restrictionExp,",");
            while (st.hasMoreTokens()) {
             String token = st.nextToken();
             int index = token.indexOf('-');
             if ( index > 0 ) {
                 minDepthList.add(token.substring(0,index));
                 maxDepthList.add(token.substring(index+1));
             } else
                 depthList.add(token);
            }
               int size = minDepthList.size();
               minDepthArray = new Integer[size];
               maxDepthArray = new Integer[size];
               for ( int i = 0; i < size; i++ ) {
                 minDepthArray[i] = Integer.valueOf((String)minDepthList.get(i));
                 maxDepthArray[i] = Integer.valueOf((String)maxDepthList.get(i));
               }
               size = depthList.size();
               depthArray = new Integer[size];
               for ( int i = 0; i < size; i++ )
                depthArray[i] = Integer.valueOf((String)depthList.get(i));

           } catch ( Exception e ) {
             e.printStackTrace();
             throw new PortalException(e.getMessage());
            }

         }

         /**
           * Checks the restriction for the specified node
           * @param propertyValue a <code>String</code> property value to be checked
           * @exception PortalException
         */
         public boolean checkRestriction(String propertyValue) throws PortalException {
           int depth = CommonUtils.parseInt(propertyValue);
           for ( int i = 0; i < minDepthArray.length || i < depthArray.length; i++ ) {
             if ( i < minDepthArray.length )
               if ( depth <= maxDepthArray[i].intValue() && depth >= minDepthArray[i].intValue() )
                 return true;
             if ( i < depthArray.length )
              if ( depthArray[i].intValue() == depth )
                 return true;
           }
              return false;
         }

}