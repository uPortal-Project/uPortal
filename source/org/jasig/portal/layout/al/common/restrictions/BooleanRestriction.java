/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.layout.al.common.restrictions;


import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.utils.CommonUtils;

/**
 * BooleanRestriction checks the restriction on the boolean property for a given ILayoutNode object.
 * <p>
 * Company: Instructional Media &amp; Magic
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public abstract class BooleanRestriction extends ALRestriction {


         private boolean boolValue1 = false, boolValue2 = false;

         public BooleanRestriction(String name, String nodePath) {
           super(name,nodePath);
         }

         public BooleanRestriction(String name) {
           super(name);
         }
         
         public BooleanRestriction() {
            super();
         }

         private boolean strToBool ( String boolStr ) {
           return ("Y".equalsIgnoreCase(boolStr))?true:false;
         }

          /**
            * Parses the restriction expression of the current node
            * @exception PortalException
            */
         protected void parseRestrictionExpression () throws PortalException {
          try {
            String restrictionExp = getRestrictionExpression();
            int commaIndex = restrictionExp.indexOf(',');
            if ( commaIndex < 0 ) {
             boolValue1 = boolValue2 = strToBool(restrictionExp);
            } else {
             boolValue1 = strToBool(restrictionExp.substring(0,commaIndex));
             boolValue2 = strToBool(restrictionExp.substring(commaIndex+1));
            }
          } catch ( Exception e ) {
             throw new PortalException(e.getMessage());
            }
         }


         /**
           * Gets the boolean property value for the specified node
         */
         protected abstract boolean getBooleanPropertyValue( ILayoutNode node );
         
         protected boolean checkRestriction( boolean boolProperty ) throws PortalException {
            if ( boolProperty == boolValue1 || boolProperty == boolValue2 )
              return true;
              return false;
         }

         /**
           * Checks the restriction for the specified node
           * @param node a <code>ILayoutNode</code> user layout node to be checked
           * @exception PortalException
         */
         public boolean checkRestriction( ILayoutNode node ) throws PortalException {
           boolean boolProperty = getBooleanPropertyValue(node);
           return checkRestriction(boolProperty);
         }

         /**
           * Checks the restriction for the specified property
           * @param propertyValue a <code>String</code> property value
           * @exception PortalException
         */
         public boolean checkRestriction( String propertyValue ) throws PortalException {
           boolean boolProperty = CommonUtils.strToBool(propertyValue);
           return checkRestriction(boolProperty);
         }


}
