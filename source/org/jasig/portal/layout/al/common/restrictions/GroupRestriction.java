/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;


import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupService;
import org.jasig.portal.groups.IGroupServiceFactory;
import org.jasig.portal.groups.ReferenceGroupServiceFactory;
import org.jasig.portal.layout.ALNode;
import org.jasig.portal.layout.IALNodeDescription;
import org.jasig.portal.layout.al.common.node.ILayoutNode;


/**
 * GroupRestriction checks the group restriction for a given ALNode object.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class GroupRestriction extends ALRestriction {


         // This group key can be come from different sources, for instance, from IEntityGroup object
         private IEntityGroup groups[];
         private static IGroupService groupService;
         private static IGroupServiceFactory groupServiceFactory;
         
         static {
           try {
            if ( groupServiceFactory == null )
             groupServiceFactory = new ReferenceGroupServiceFactory();
            if ( groupService == null )
             groupService = groupServiceFactory.newGroupService();
           } catch ( Exception e ) {
             throw new RuntimeException(e.getMessage());
           }
         }  

         public GroupRestriction(String name,String nodePath) throws PortalException {
           super(name,nodePath);
         }

         public GroupRestriction(String name) throws PortalException {
           super(name);
         }
         
         public GroupRestriction() throws PortalException {
            super();
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
         public boolean checkRestriction( ILayoutNode node ) throws PortalException {
           if ( !(node instanceof ALNode) )	
               throw new PortalException ( "The node must be ALNode type!");  
           IEntityGroup group = groupService.findGroup(((IALNodeDescription)node).getGroup());
           for ( int i = 0; i < groups.length; i++ )
            if ( groups[i].contains(group) )
             return true;
             return false;
         }


}
