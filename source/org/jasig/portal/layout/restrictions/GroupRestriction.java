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
import org.jasig.portal.utils.CommonUtils;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Vector;

import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupService;
import org.jasig.portal.groups.IGroupServiceFactory;
import org.jasig.portal.groups.ReferenceGroupServiceFactory;
import org.jasig.portal.layout.*;


/**
 * <p>Title: The Group Restriction class</p>
 * <p>Description: This class checks the group restriction for a given ALNode object</p>
 * <p>Company: Instructional Media & Magic </p>
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1
 */

public class GroupRestriction extends UserLayoutRestriction {


         // This group key can be come from different sources, for instance, from IEntityGroup object
         private IEntityGroup groups[];
         private static IGroupService groupService;
         private static IGroupServiceFactory groupServiceFactory;

         public GroupRestriction(String nodePath) throws PortalException {
           super(nodePath);
           try {
            if ( groupServiceFactory == null )
             groupServiceFactory = new ReferenceGroupServiceFactory();
            if ( groupService == null )
             groupService = groupServiceFactory.newGroupService();
           } catch ( Exception e ) {
             throw new PortalException(e.getMessage());
            }
         }

         public GroupRestriction() throws PortalException {
          this(null);
         }


         /**
           * Returns the type of the current restriction
           * @return a restriction type respresented in the <code>RestrictionTypes</code> interface
          */
         public int getRestrictionType() {
           return RestrictionTypes.GROUP_RESTRICTION|super.getRestrictionType();
         }


          /**
            * Parses the restriction expression of the current node
            * @exception PortalException
          */
         protected void parseRestrictionExpression () throws PortalException {
         try {
            String restrictionExp = getRestrictionExpression();
            List groupsList = new Vector();
            StringTokenizer st = new StringTokenizer(restrictionExp,",");
            while (st.hasMoreTokens()) {
                 String token = st.nextToken();
                 groupsList.add(groupService.findGroup(token));
            }
              groups = (IEntityGroup[]) groupsList.toArray();

           } catch ( Exception e ) {
             throw new PortalException(e.getMessage());
            }

         }

         /**
           * Checks the restriction for the specified property value
           * @param propertyValue a <code>String</code> property value to be checked
           * @exception PortalException
         */
         public boolean checkRestriction( String propertyValue ) throws PortalException {
           IEntityGroup group = groupService.findGroup(propertyValue);
           for ( int i = 0; i < groups.length; i++ )
            if ( groups[i].contains(group) )
             return true;
             return false;
         }

         /**
           * Checks the restriction for the current node
           * @exception PortalException
         */
         public boolean checkRestriction( ALNode node ) throws PortalException {

           IEntityGroup group = groupService.findGroup(((IALNodeDescription)node.getNodeDescription()).getGroup());
           for ( int i = 0; i < groups.length; i++ )
            if ( groups[i].contains(group) )
             return true;
             return false;
         }


}
