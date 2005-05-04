/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 * Utility functions for applying user changes to the user's plf prior
 * to persisting.
 * 
 * @version $Revision$Date$
 * @since uPortal 2.5
 */
public class HandlerUtils
{
    public static final String RCS_ID = "@(#) $Header$";

    /**
       This method returns the PLF version of the passed in compViewNode. If
       create is false and a node with the same id is not found in the PLF then
       null is returned. If the create is true then an attempt is made to
       create the node along with any necessary ancestor nodes needed to
       represent the path along the tree.
     */
    public static Element getPLFNode( Element compViewNode,
                                      IPerson person,
                                      boolean create,
                                      boolean includeChildNodes )
        throws PortalException
    {
        Document plf = (Document) person.getAttribute( Constants.PLF );
        String ID = compViewNode.getAttribute( Constants.ATT_ID );
        Element plfNode = plf.getElementById( ID );

        if ( plfNode != null )
            return plfNode;

        // make sure that the root isn't what we are trying to get. Root is not
        // registered with document under an id.

        if ( compViewNode.getNodeName().equals( "layout" ) )
            return plf.getDocumentElement();
        
        // if not found then create if indicated.
        if ( create == true )
            return createPlfNodeAndPath( compViewNode, includeChildNodes,
                                         person );
        return null;
    }

    /**
       Creates a copy of the passed in ILF node in the PLF if not already
       there as well as creating any ancestor nodes along the path from this
       node up to the layout root if they are not there.
     */
    public static Element createPlfNodeAndPath( Element compViewNode,
                                                boolean includeChildNodes,
                                                IPerson person )
        throws PortalException
    {
        // first attempt to get parent
        Element compViewParent = (Element) compViewNode.getParentNode();
        Element plfParent = getPLFNode( compViewParent, person, true, false );
        Document plf = (Document) person.getAttribute( Constants.PLF );

        // if ilf copy being created we can append to parent and use the
        // position set to place it.
        
        if ( compViewNode.getAttribute( Constants.ATT_ID ).startsWith( "u" ) )
            return createILFCopy( compViewNode, compViewParent,
                                  includeChildNodes,
                                  plf, plfParent, person );
        return createOrMovePLFOwnedNode( compViewNode, compViewParent,
                                         true, // create if not found
                                         includeChildNodes,
                                         plf, plfParent, person );
    }

    /**
       Creates a copy of an ilf node in the plf and sets up necessary storage
       attributes.
     */
    private static Element createILFCopy( Element compViewNode,
                                          Element compViewParent,
                                          boolean includeChildNodes,
                                          Document plf,
                                          Element plfParent,
                                          IPerson person )
        throws PortalException
    {
        Element plfNode = (Element) plf.importNode( compViewNode,
                                                    includeChildNodes );
        String ID = plfNode.getAttribute( Constants.ATT_ID );
        plfNode.setIdAttribute(Constants.ATT_ID, true);

        IUserLayoutStore uls = null;
        uls = UserLayoutStoreFactory.getUserLayoutStoreImpl();
        
        if ( plfNode.getAttribute( Constants.ATT_PLF_ID ).equals( "" ) )
        {
            String plfID = null;
                
            try
            {
                if ( ! plfNode.getAttribute( Constants.ATT_CHANNEL_ID )
                     .equals( "" ) ) // dealing with a channel
                    plfID = uls.generateNewChannelSubscribeId( person );
                else
                    plfID = uls.generateNewFolderId( person );
            }
            catch (Exception e)
            {
                throw new PortalException( "Exception encountered while " +
                                           "generating new user layout node " +
                                           "Id for userId=" + person.getID(), e );
            }
                
            plfNode.setAttributeNS( Constants.NS_URI,
                                    Constants.ATT_PLF_ID, plfID );
            plfNode.setAttributeNS( Constants.NS_URI,
                                    Constants.ATT_ORIGIN, ID );
        }
            
        plfParent.appendChild( plfNode );
        PositionManager.updatePositionSet( compViewParent, plfParent,
                                           person );
        return plfNode;
    }
        
    /**
       Creates or moves the plf copy of a node in the composite view and
       inserting it before its next highest sibling so that if dlm is not used
       then the model ends up exactly like the original non-dlm persistance
       version. The position set is also updated and if no ilf copy nodes are
       found in the sibling list the set is cleared if it exists.
        */
    static Element createOrMovePLFOwnedNode( Element compViewNode,
                                             Element compViewParent,
                                             boolean createIfNotFound,
                                             boolean createChildNodes,
                                             Document plf,
                                             Element plfParent,
                                             IPerson person )
        throws PortalException
    {
        Element child = (Element) compViewParent.getFirstChild();
        Element nextOwnedSibling = null;
        boolean insertionPointFound = false;

        while( child != null )
        {
            if ( insertionPointFound &&
                 nextOwnedSibling == null &&
                 ! child.getAttribute( Constants.ATT_ID ).startsWith( "u" ) )
                nextOwnedSibling = child;

            if ( child == compViewNode )
                insertionPointFound = true;
            child = (Element) child.getNextSibling();
        }

        if ( insertionPointFound == false )
            return null;

        String nextSibID = null;
        Element nextPlfSib = null;

        if ( nextOwnedSibling != null )
        {
             nextSibID = nextOwnedSibling.getAttribute( Constants.ATT_ID );
             nextPlfSib = plf.getElementById( nextSibID );
        }

        String plfNodeID = compViewNode.getAttribute( Constants.ATT_ID );
        Element plfNode = plf.getElementById( plfNodeID );

        if ( plfNode == null )
        {
            if ( createIfNotFound == true )
            {
                plfNode = (Element) plf.importNode( compViewNode,
                                                    createChildNodes );
                String ID = plfNode.getAttribute( Constants.ATT_ID );
                plfNode.setIdAttribute(Constants.ATT_ID, true);
            }
            else
                return null;
        }
        
        if ( nextPlfSib == null )
            plfParent.appendChild( plfNode );
        else
            plfParent.insertBefore( plfNode, nextPlfSib );

        PositionManager.updatePositionSet( compViewParent, plfParent, person );
        return (Element) plfNode;
    }
}
