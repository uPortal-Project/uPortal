
package org.jasig.portal.layout.dlm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

    /** Applies user prefs changes to the user's plf prior to persisting.
     */
public class TabColumnPrefsHandler
{
    public static final String RCS_ID = "@(#) $Header$";
    private static Log LOG = LogFactory.getLog(TabColumnPrefsHandler.class);

    /**
       This method is called from the TabColumnPrefsState class after a node
       has already been moved from its old parent to its new in the ILF. We can
       get at the new parent via the compViewNode moved but need a separate
       handle of the parent from whence it came. The goal of this method is to
       make the appropriate change in the PLF to persist this action take by
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
        if (LOG.isInfoEnabled())
            LOG.info("moving "
                + compViewNode.getAttribute( Constants.ATT_ID ) );
        Element compViewParent = (Element) compViewNode.getParentNode();

        if ( oldCompViewParent != compViewParent )
        {
            if (LOG.isInfoEnabled())
                LOG.info("reparenting from " +
                        oldCompViewParent.getAttribute( Constants.ATT_ID )
                        + " to " +
                        compViewParent.getAttribute( Constants.ATT_ID ));
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
                if (LOG.isInfoEnabled())
                    LOG.info("updating old parent's position set" );
            }
        }
        // now take care of the destination
        Element plfParent = HandlerUtils
            .getPLFNode( compViewParent, person,
                         true, // create parent if not found
                         false ); // don't create children
        if ( compViewNode.getAttribute( Constants.ATT_ID ).startsWith( "u" ) )
        {
            // ilf node being inserted
            if (LOG.isInfoEnabled())
                LOG.info("ilf node being moved, only update new parent pos set" );
            PositionManager.updatePositionSet( compViewParent,
                                               plfParent, person );
        }
        else
        {
            // plf node
            if (LOG.isInfoEnabled())
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
       Creates a new tab in the plf. This method really is not needed because
       all the functionality is provided by the called method but was added
       for clarity.
     
    public static void addTab( Element compViewNode, IPerson person )
        throws PortalException
    {
        HandlerUtils.createPlfNodeAndPath( compViewNode, false, person );
    }

    
    **
       Creates a new column along with any parent nodes if needed in the plf.
       This method really is not needed because
       all the functionality is provided by the called method but was added
       for clarity.
     *
    public static void addColumn( Element compViewNode, IPerson person )
        throws PortalException
    {
        HandlerUtils.createPlfNodeAndPath( compViewNode, false, person );
    }

    **
       Creates a new channel along with any parent nodes, if needed, and all
       child nodes in the plf. This method really is not needed because
       all the functionality is provided by the called method but was added
       for clarity.
     *
    public static void addChannel( Element compViewNode, IPerson person )
        throws Exception
    {
        HandlerUtils.createPlfNodeAndPath( compViewNode, true, person );
    }
    */

    public static Element getPlfChannel( Element compViewChannel, IPerson person )
        throws PortalException
    {
        if (LOG.isInfoEnabled())
            LOG.info("TabColumnPrefsHandler>>getPlfChannel():" );
        Element plf = HandlerUtils
            .getPLFNode( compViewChannel, person,
                         true, // create node if not found
                         false ); // don't create children
        if (LOG.isInfoEnabled())
            LOG.info("TabColumnPrefsHandler>>getPlfChannel(): retrieved PLFChannel" );
        return plf;
    }

    
    /**
       Records changes made to attributes of elements by users. Changes to
       the restriction attributes should not be passed to this method. Those
       are handled by the changeRestrictions method.
     */
    public static void editAttribute( Element compViewNode,
                                      String attributeName,
                                      IPerson person )
        throws PortalException
    {
        Element plfNode = HandlerUtils
        .getPLFNode( compViewNode, person,
                     true, // create node if not found
                     false ); // don't create children
        Attr attribNode = compViewNode.getAttributeNode( attributeName );
        Attr plfAttribNode = plfNode.getAttributeNode( attributeName );

        if ( attribNode == null ) // attribute deleted
        {
            if ( plfAttribNode != null )
                plfNode.removeAttributeNode( plfAttribNode );
        }
        else // attribute added or edited so add and/or edit 
        {
            if ( plfAttribNode == null )
            {
                Document plf = plfNode.getOwnerDocument();
                plfAttribNode = (Attr) plf.importNode( attribNode, true );
                plfNode.setAttributeNode( plfAttribNode );
            }
            else
                plfAttribNode.setValue( attribNode.getValue() );
        }
        if ( plfNode.getAttribute( Constants.ATT_ID ).startsWith( "u" ) )
        {
            // if ilf node being edited then need to add edit directive
            EditManager.addEditDirective( plfNode, attributeName, person );   
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
        String ID = compViewNode.getAttribute( Constants.ATT_ID );

        if ( ID.startsWith( "u" ) ) // ilf node
            DeleteManager.addDeleteDirective( compViewNode, ID, person );
        else 
        {
            // plf node
            Document plf = (Document) person.getAttribute( Constants.PLF );
            Element node = plf.getElementById( ID );

            if ( node == null )
                return;
            Element parent = (Element) node.getParentNode();
            if ( parent == null )
                return;
            parent.removeChild( node );
        }
    }

    private static void changeRestriction( String name,
                                           Element element,
                                           boolean value )
    {
        // The default value for these attributes,
        // if not included on an element, is true so we remove the attribute if
        // we see a parm value of true and add the attribute with a value of
        // false if a parm value is false. This also saves on the number of
        // rows in the up_layout_param table and hence on read and write time.
        
        if ( value == false )
            element.setAttributeNS( Constants.NS_URI, name, "false" );
        else
            element.removeAttribute( name );
    }
    
    public static void changeRestrictions( Element compViewNode, 
                                           boolean moveAllowed,
                                           boolean editAllowed,
                                           boolean addChildAllowed,
                                           boolean deleteAllowed,
                                           IPerson person ) 
        throws PortalException
    {
        Element plfNode = HandlerUtils
        .getPLFNode( compViewNode, person,
                     false, // don't create if not found
                     false ); // don't create children
        if ( plfNode == null )
            return;

        changeRestriction( Constants.ATT_MOVE_ALLOWED, plfNode, moveAllowed );
        changeRestriction( Constants.ATT_DELETE_ALLOWED,
                           plfNode, deleteAllowed );
        if ( plfNode.getAttribute( Constants.ATT_CHANNEL_ID ).equals( "" ) )
        {
            // if channel id is empty then this is not a channel so change.
            // editAllowed and childAllowed aren't defined on channels.
            changeRestriction( Constants.ATT_EDIT_ALLOWED, plfNode,
                               editAllowed );
            changeRestriction( Constants.ATT_ADD_CHILD_ALLOWED, plfNode,
                               addChildAllowed );
        }
        if (LOG.isInfoEnabled())
            LOG.info("changing restrictions:\nmoveAllowed=" +
                     moveAllowed + "\neditAllowed=" +
                     editAllowed + "\naddChildAllowed=" +
                     addChildAllowed + "\ndeleteAllowed=" +
                     deleteAllowed );
    }
}

