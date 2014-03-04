/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Applies user prefs changes to the user's plf prior to persisting.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class TabColumnPrefsHandler
{
    public static final String RCS_ID = "@(#) $Header$";
    private static Logger LOG = LoggerFactory.getLogger(TabColumnPrefsHandler.class);

    /**
       This method is called from the TabColumnPrefsState class after a node
       has already been moved from its old parent to its new in the ILF. We can
       get at the new parent via the compViewNode moved but need a separate
       handle of the parent from whence it came. The goal of this method is to
       make the appropriate change in the PLF to persist this action taken by
       the user. For ILF nodes this generally means adding an entry to the
       position set for the new parent and removing any entry if it existed in
       the position set in the old parent. For nodes that are owned by the
       user (PLF owned nodes) the nodes are moved outright and now position
       set is needed unless the new parent contains ILF nodes as well
       requiring preservation of the user's ordering of the nodes for when the
       ILF and PLF are merged again later on.
     */
    public static void moveElement( Element compViewNode,
                                    Element oldCompViewParent,
                                    IPerson person )
        throws PortalException
    {

        LOG.info("moving {} from parent {} for {}",
                XmlUtilitiesImpl.toString(compViewNode),
                XmlUtilitiesImpl.toString(oldCompViewParent),
                person);

        Element compViewParent = (Element) compViewNode.getParentNode();

        if ( oldCompViewParent != compViewParent )
        {

            LOG.info("Re-parenting {} from {} to {} for {}",
                XmlUtilitiesImpl.toString(compViewNode),
                XmlUtilitiesImpl.toString(oldCompViewParent),
                XmlUtilitiesImpl.toString(compViewParent),
                person);

            // update previous parent if found in PLF
            Element plfParent = HandlerUtils
            .getPLFNode( oldCompViewParent,
                         person, // only needed if creating
                         false, // only look, don't create
                         false ); // also not needed
            if ( plfParent != null )
            {
                PositionManager.updatePositionSet( oldCompViewParent,
                                                   plfParent, person );
                LOG.info("Updating old parent {} 's position set for {}",
                        XmlUtilitiesImpl.toString(oldCompViewParent), person);
            }
        }
        // now take care of the destination
        Element plfParent = HandlerUtils
            .getPLFNode( compViewParent, person,
                         true, // create parent if not found
                         false ); // don't create children
        if (compViewNode.getAttribute(Constants.ATT_ID).startsWith(
                Constants.FRAGMENT_ID_USER_PREFIX))
        {
            // ilf node being inserted
            LOG.info("ilf node being moved, only update new parent pos set" );
            PositionManager.updatePositionSet( compViewParent,
                                               plfParent, person );
        }
        else
        {
            // plf node
            LOG.info("plf node being moved, updating old parent's position set" );
            Document plf = (Document) person.getAttribute( Constants.PLF );
            HandlerUtils
            .createOrMovePLFOwnedNode( compViewNode, compViewParent,
                                       false, // should always be found
                                       false, // irrelevant, not creating
                                       plf, plfParent, person );
        }
    }
    
    /**
       Handles user requests to delete UI elements. For ILF owned nodes it
       delegates to the DeleteManager to add a delete directive. For PLF
       owned nodes it deletes the node outright.
     */
    public static void deleteNode( Element compViewNode,
                                   Element compViewParent,
                                   IPerson person )
        throws PortalException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Attempting to delete node {} with parent {} for {}",
                XmlUtilitiesImpl.toString(compViewNode),
                XmlUtilitiesImpl.toString(compViewParent),
                person);
        }

        String ID = compViewNode.getAttribute( Constants.ATT_ID );

        if ( ID.startsWith( Constants.FRAGMENT_ID_USER_PREFIX ) ) {// ilf node
            LOG.trace("Moribund node's ID {} started with {} so considering an ilf node, DeleteManger suitable.",
                    ID, Constants.FRAGMENT_ID_USER_PREFIX);
            DeleteManager.addDeleteDirective( compViewNode, ID, person );
        } else
        {
            LOG.trace("Moribund node's ID {} did not start with {} so considering a plf node.",
                ID, Constants.FRAGMENT_ID_USER_PREFIX);
            // plf node
            Document plf = (Document) person.getAttribute( Constants.PLF );
            Element node = plf.getElementById( ID );

            if ( node == null ) {
                LOG.warn("deleteNode did not find node ID {} in plf {} so taking no action.",
                        ID, XmlUtilitiesImpl.toString(plf) );
                return;
            }
            Element parent = (Element) node.getParentNode();
            if ( parent == null ) {
                LOG.error("deleteNode found node ID {} in plf {} " +
                        "but it did not have a parent node so taking no action",
                        ID, XmlUtilitiesImpl.toString(plf) );
                return;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing child node {} from parent node {}.",
                    XmlUtilitiesImpl.toString(node),
                    XmlUtilitiesImpl.toString(parent) );
            }

            parent.removeChild( node );
        }
    }

}
