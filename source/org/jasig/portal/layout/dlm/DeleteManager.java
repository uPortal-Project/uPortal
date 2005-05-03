/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** 
 * Looks for, applies against the ilf, and updates accordingly the delete
 * set within a plf.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class DeleteManager
{
    public static final String RCS_ID = "@(#) $Header$";
    private static final Log LOG = LogFactory.getLog(DeleteManager.class);

    private static RDBMDistributedLayoutStore dls = null;

    /**
     * Hands back the single instance of RDBMDistributedLayoutStore. There is
     * already a method
     * for aquiring a single instance of the configured layout store so we
     * delegate over there so that all references refer to the same instance.
     * This method is solely for convenience so that we don't have to keep
     * calling UserLayoutStoreFactory and casting the resulting class.
     */
    private static RDBMDistributedLayoutStore getDLS()
    {
        if ( dls == null )
        {
            IUserLayoutStore uls = null;
            uls = UserLayoutStoreFactory.getUserLayoutStoreImpl();
            dls = (RDBMDistributedLayoutStore) uls;
        }
        return dls;
    }

    /**
       Get the delete set if any from the plf and process each delete command
       removing any that fail from the delete set so that the delete set is
       self cleaning.
    */
    static void applyAndUpdateDeleteSet( Document plf,
                                         Document ilf,
                     IntegrationResult result )
    {

        Element dSet = null;
        try
        {
            dSet = getDeleteSet( plf, null, false );
        }
        catch( Exception e )
        {
            LOG.error("Exception occurred while getting user's DLM delete-set.", 
                    e);
        }

        if ( dSet == null )
            return;

        NodeList deletes = dSet.getChildNodes();

        for( int i=deletes.getLength()-1; i>=0; i-- )
        {
            if ( applyDelete( (Element) deletes.item(i), ilf ) == false )
            {
                dSet.removeChild( deletes.item(i) );
                result.changedPLF = true;
            }
            else
            {
                result.changedILF = true;
            }
        }

        if ( dSet.getChildNodes().getLength() == 0 )
        {
            plf.getDocumentElement().removeChild( dSet );
            result.changedPLF = true;
        }
    }

    /**
       Attempt to apply a single delete command and return true if it succeeds
       or false otherwise. If the delete is disallowed or the target element
       no longer exists in the document the delete command fails and returns
       false.
    */
    private static boolean applyDelete( Element delete, Document ilf )
    {
        String nodeID = delete.getAttribute( Constants.ATT_NAME );

        Element e = ilf.getElementById( nodeID );

        if ( e == null )
            return false;

        String deleteAllowed = e.getAttribute( Constants.ATT_DELETE_ALLOWED );
        if ( deleteAllowed.equals( "false" ) )
            return false;

        Element p = (Element) e.getParentNode();
        e.setIdAttribute(Constants.ATT_ID, false);
        p.removeChild( e );
        return true;
    }

    /**
       Get the delete set if any stored in the root of the document or create
       it is passed in create flag is true.
    */
    private static Element getDeleteSet( Document plf,
                                         IPerson person,
                                         boolean create )
        throws PortalException
    {
        Node root = plf.getDocumentElement();
        Node child = root.getFirstChild();

        while( child != null )
        {
            if ( child.getNodeName().equals( Constants.ELM_DELETE_SET ) )
            return (Element) child;
            child = child.getNextSibling();
        }

        if ( create == false )
            return null;

        String ID = null;

        try
        {
            ID = getDLS().getNextStructDirectiveId( person );
        }
        catch (Exception e)
        {
            throw new PortalException( "Exception encountered while " +
                                       "generating new delete set node " +
                                       "Id for userId=" + person.getID(), e );
        }
        Element delSet = plf.createElement( Constants.ELM_DELETE_SET );
        delSet.setAttribute( Constants.ATT_TYPE,
                     Constants.ELM_DELETE_SET );
        delSet.setAttribute( Constants.ATT_ID, ID );
        root.appendChild( delSet );
        return delSet;
    }

    /**
       Create and append a delete directive to delete the node identified by
       the passed in element id. If this node contains any incorporated
       elements then they must also have a delete directive added in here to
       prevent incorporated channels originating in another column from
       reappearing in that column because the position set entry that pulled
       them into this column was now removed. (ie: the user moved an inc'd
       channel to this column and then deleted the column means that the inc'd
       channel should be deleted also.) This was designed to add a delete
       directive for each nested element having an ID so as to work for the
       future case of a tree view.
    */
    public static void addDeleteDirective( Element compViewNode,
                                           String elementID,
                                           IPerson person )
        throws PortalException
    {
        Document plf = (Document) person.getAttribute( Constants.PLF );
        Element delSet = getDeleteSet( plf, person, true );
        addDeleteDirective( compViewNode, elementID, person, plf, delSet );
    }
    /**
       This method does the actual work of adding a delete directive and then
       recursively calling itself for any incoporated children that need to be
       deleted as well.
    */
    private static void addDeleteDirective( Element compViewNode,
                                            String elementID,
                                            IPerson person,
                                            Document plf,
                                            Element delSet )
        throws PortalException
    {
        String ID = null;

        try
        {
            ID = getDLS().getNextStructDirectiveId( person );
        }
        catch (Exception e)
        {
            throw new PortalException( "Exception encountered while " +
                                       "generating new delete node " +
                                       "Id for userId=" + person.getID(), e );
        }
        Element delete = plf.createElement( Constants.ELM_DELETE );
        delete.setAttribute( Constants.ATT_TYPE, Constants.ELM_DELETE );
        delete.setAttribute( Constants.ATT_ID, ID );
        delete.setAttributeNS( Constants.NS_URI,
                       Constants.ATT_NAME, elementID );
        delSet.appendChild( delete );

        // now pass through children and add delete directives for those with
        // IDs indicating that they were incorporated
        Element child = (Element) compViewNode.getFirstChild();

        while( child != null )
        {
            String childID = child.getAttribute( "ID" );
            if ( childID.startsWith( "u" ) )
                addDeleteDirective( child, childID, person, plf, delSet );
            child = (Element) child.getNextSibling();
        }
    }
}
