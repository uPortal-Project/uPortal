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
import org.jasig.portal.layout.UserLayoutNode;
import org.jasig.portal.utils.CommonUtils;

/**
 * <p>Title: Priority Restriction class</p>
 * <p>Description: This class checks the priority restriction for a given UserLayoutNode object</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic </p>
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1
 */

public class PriorityRestriction extends UserLayoutRestriction {


         private int minPriority, maxPriority;

         public PriorityRestriction(UserLayoutNode node) {
           super(node);
         }

         /**
           * Returns the type of the current restriction
           * @return a restriction type respresented in the <code>RestrictionTypes</code> interface
          */
         public int getRestrictionType() {
          return RestrictionTypes.PRIORITY_RESTRICTION;
         }

          /**
            * Parses the restriction expression of the current node
            * @exception PortalException
            */
         protected void parseRestrictionExpression () throws PortalException {
          try {
            String restrictionExp = getRestrictionExpression();
            StringTokenizer st = new StringTokenizer(restrictionExp,"-");
             String min = st.nextToken();
             String max = st.nextToken();
             minPriority = CommonUtils.parseInt(min);
             maxPriority = CommonUtils.parseInt(max);
          } catch ( Exception e ) {
             throw new PortalException(e.getMessage());
            }
         }


         /**
           * Checks the restriction for the specified node
           * @param node a <code>UserLayoutNode</code> user layout node to be checked
           * @exception PortalException
         */
         public boolean checkRestriction( UserLayoutNode node ) throws PortalException {
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
