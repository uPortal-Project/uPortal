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


import org.jasig.portal.PortalException;
import org.jasig.portal.layout.ALNode;
import org.jasig.portal.utils.CommonUtils;

/**
 * BooleanRestriction checks the restriction on the boolean property for a given ALNode object.
 * <p>
 * Company: Instructional Media &amp; Magic
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public abstract class BooleanRestriction extends UserLayoutRestriction {


         boolean boolValue1 = false, boolValue2 = false;

         public BooleanRestriction(String nodePath) {
           super(nodePath);
         }

         public BooleanRestriction() {
           super();
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
             boolValue1 = boolValue2 = CommonUtils.strToBool(restrictionExp);
            } else {
             boolValue1 = CommonUtils.strToBool(restrictionExp.substring(0,commaIndex));
             boolValue2 = CommonUtils.strToBool(restrictionExp.substring(commaIndex+1));
            }
          } catch ( Exception e ) {
             throw new PortalException(e.getMessage());
            }
         }


         /**
           * Gets the boolean property value for the specified node
         */
         protected abstract boolean getBooleanPropertyValue( ALNode node );

         /**
           * Checks the restriction for the specified node
           * @param node a <code>ALNode</code> user layout node to be checked
           * @exception PortalException
         */
         public boolean checkRestriction( ALNode node ) throws PortalException {
           boolean boolProperty = getBooleanPropertyValue(node);
           if ( boolProperty == boolValue1 || boolProperty == boolValue2 )
             return true;
             return false;
         }

         /**
           * Checks the restriction for the specified property
           * @param propertyValue a <code>String</code> property value
           * @exception PortalException
         */
         public boolean checkRestriction( String propertyValue ) throws PortalException {
           boolean boolProperty = CommonUtils.strToBool(propertyValue);
           if ( boolProperty == boolValue1 || boolProperty == boolValue2 )
             return true;
             return false;
         }


}
