/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Performs merging of PLF into ILF for DLM.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class PLFIntegrator
{
    public static final String RCS_ID = "@(#) $Header$";
    private static Log LOG = LogFactory.getLog(PLFIntegrator.class);

    /**
     * @return boolean true if PLF was changed, false otherwise.
     */
    public static void mergePLFintoILF( Document plf,
                                        Document ilf,
                                        IntegrationResult result )
        throws Exception
    {
        // we want to use the root folders located as the single child of
        // the layout elements which are the document elements.
        Element plfLayout = plf.getDocumentElement();
        Element plfRoot = (Element) plfLayout.getFirstChild();
        Element ilfLayout = ilf.getDocumentElement();
        Element ilfRoot = (Element) ilfLayout.getFirstChild();

        DeleteManager.applyAndUpdateDeleteSet( plf, ilf, result );
        applyChildChanges( plfRoot, ilfRoot, result );
    }

    private static void applyChildChanges( Element plfParent, 
                                           Element ilfParent,
                                           IntegrationResult result )
        throws Exception
    {
        Element positions = null;
        boolean changeOccurred = false;

        if( null == plfParent )
        {
            throw new java.lang.Error(
                "(Bug 20224)The PLF parent element is missing, so the database has been corrupted."
                );
        }
        
        Element node = (Element) plfParent.getFirstChild();
        
        while( node != null )
        {
            Element nextNode = (Element) node.getNextSibling();
            
            if ( node.getNodeName().equals( "folder" ) )
                mergeFolder( node, plfParent, ilfParent, result );
            else if ( node.getNodeName().equals( Constants.ELM_POSITION_SET ) )
                positions = node;
            else if ( node.getNodeName().equals( "channel" ) )
                mergeChannel( node, plfParent, ilfParent, result );
            node = nextNode;
        }

        if ( positions != null )
        {
            IntegrationResult posResult = new IntegrationResult();
            if (LOG.isInfoEnabled())
                LOG.info( "applying positions" );
            PositionManager.applyPositions( ilfParent,
                                            positions,
                                            posResult );
            if( posResult.changedILF == false )
            {
                if (LOG.isInfoEnabled())
                    LOG.info("removing positionSet");
                plfParent.removeChild( positions );
                result.changedPLF = true;
            }
            else
            {
                result.changedILF = true;
                if ( posResult.changedPLF )
                    result.changedPLF = true;
            }
        }
    }

    private static void mergeChannel( Element plfChild,
                                      Element plfParent,
                                      Element ilfParent,
                                      IntegrationResult result )
        throws Exception
    {
        String id = plfChild.getAttribute( Constants.ATT_ID );
                
        if ( id.startsWith( "u" ) )
        {
            // this should never happen. Moves of an incorporated channel
            // should be indicated via the position set of the parent
        }
        else // plf channel
        {
            if (LOG.isInfoEnabled())
                LOG.info("merging into ilf channel " + id);

            if ( ilfParent.getAttribute( Constants.ATT_ADD_CHILD_ALLOWED )
                 .equals( "false" ) )
            {
                if (LOG.isInfoEnabled())
                    LOG.info("removing from plf disallowed add of channel "
                            + id );
                plfParent.removeChild( plfChild );
                result.changedPLF = true;
            }
            else
            {
                appendChild( plfChild, ilfParent, true );
                result.changedILF = true;
            }
        }
    }
    
    private static void mergeFolder( Element plfChild,
                                     Element plfParent,
                                     Element ilfParent,
                                     IntegrationResult result )
        throws Exception
    {
        String id = plfChild.getAttribute( Constants.ATT_ID );
        
        if ( id.startsWith( "u" ) )
        {
            // incorporated folder - if a copy of an inc'd folder is in the
            // plf it is because either it has attribute edits or there are
            // nested child changes to be applied. It does not imply movement.
            // That is accomplished by the position set. So see if it still
            // exists in the ilf for applying changes
           
            Document ilf = ilfParent.getOwnerDocument();
            Element original = ilf.getElementById( id );

            if ( original == null )
            {
                // not there anymore, discard from plf
                plfParent.removeChild( plfChild );
                result.changedPLF = true;
                return;
            }

            // found it, apply changes and see if they had any affect
            boolean attributeChanged = false;
            IntegrationResult childChanges = new IntegrationResult();
            
            attributeChanged = EditManager.applyEditSet( plfChild, original );
            applyChildChanges( plfChild, original, childChanges );

            if ( attributeChanged == false &&
                 childChanges.changedILF == false )
            {
                // no changes were used so remove this guy from plf.
                plfParent.removeChild( plfChild );
                result.changedPLF = true;
            }
            else
                result.changedILF = true;
            // need to pass on up whether PLF changed in called methods
            if ( childChanges.changedPLF )
                result.changedPLF = true;
        }
        else 
        {
            // plf folder - the real node. What is being portrayed in this
            // case is a plf node that is supposed to be added into the ilf
            // parent
            
            if ( ilfParent
                 .getAttribute( Constants.ATT_ADD_CHILD_ALLOWED )
                 .equals( "false" ) )
            {
                // nope, delete directive from plf
                if (LOG.isInfoEnabled())
                    LOG.info("removing folder from plf " + id);
                plfParent.removeChild( plfChild );
                result.changedPLF = true;
                return;
            }
            Element ilfChild = appendChild( plfChild, ilfParent, false );
            result.changedILF = true;

            IntegrationResult childChanges = new IntegrationResult();
            applyChildChanges( plfChild, ilfChild, childChanges );
            
            if ( childChanges.changedPLF )
                result.changedPLF = true;
        }
    }

    /**
       This method copies a plf node and any of its children into the passed
       in compViewParent.
     */
     static Element appendChild( Element plfChild,
                                 Element parent,
                                 boolean copyChildren )
    {
        Document document = parent.getOwnerDocument();
        Element copy = (Element) document.importNode( plfChild, false );
        parent.appendChild( copy );
        
        // set the identifier for the doc if warrented
        String id = copy.getAttribute(Constants.ATT_ID);
        if (id != null && ! id.equals(""))
            copy.setIdAttribute(Constants.ATT_ID, true);

        if ( copyChildren )
        {
            NodeList children = plfChild.getChildNodes();
            for (int i=0; i<children.getLength(); i++)
            {
                if ( children.item( i ) instanceof Element )
                    appendChild( (Element) children.item( i ),
                                 copy, true );
            }
        }


        /* mrb DOM3 change
         * 
        // now make the xerces identifier in the doc so we can get nodes via
        // getElementById.
        IPortalDocument xercesDoc = (IPortalDocument) document;
        xercesDoc.putIdentifier( plfChild.getAttribute( Constants.ATT_ID ),
                                 copy );
        */
        return copy;
    }
    
}

