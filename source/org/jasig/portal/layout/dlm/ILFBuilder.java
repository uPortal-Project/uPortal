/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.util.Enumeration;
import java.util.Vector;

import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Performs merging of layout fragments into a single document containing
 * all incorporated layout fragment elements from the set of fragments
 * passed in. This merge is trivial, appending all children of each
 * fragment into the composite document and recording their identifiers
 * in the document identifier cache. No changes are made to the source
 * fragments passed in.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class ILFBuilder
{
    public static final String RCS_ID = "@(#) $Header$";

    public static Document constructILF( Document PLF, Vector sequence )
    throws javax.xml.parsers.ParserConfigurationException
    {
        // first construct the destination document and root element. The root
        // element should be a complete copy of the PLF's root including its
        // node identifier in the new document. This requires the use of
        // the implementation class to set the identifier for that node
        // in the document.

        Document result = DocumentFactory.getNewDocument();
        /*
         * mboyd: 2.4.1 DLM merge notes: the layout container element does not
         * have an ID of "root" anymore. Hence the changes below to get the 
         * outermost element, < layout >, rather than a node with ID of "root".
         */
        Element plfLayout = PLF.getDocumentElement();
        Element ilfLayout = (Element) result.importNode( plfLayout, false );
        result.appendChild( ilfLayout );
        Element plfRoot = (Element) plfLayout.getFirstChild();
        Element ilfRoot = (Element) result.importNode( plfRoot, false);
        ilfLayout.appendChild(ilfRoot);
        String id = ilfRoot.getAttribute("ID");
        Element el = result.getElementById(id);
        
        if (ilfRoot.getAttribute(Constants.ATT_ID) != null)
            ilfRoot.setIdAttribute(Constants.ATT_ID, true);
        el = result.getElementById(id);

        // now merge fragments one at a time into ILF document
        Enumeration fragments = sequence.elements();

        while( fragments.hasMoreElements() )
            mergeFragment( (Document) fragments.nextElement(), result );
        return result;
    }

    /**
     * Passes the layout root of each of these documents to mergeChildren
     * causing all children of newLayout to be merged into compositeLayout
     * following merging protocal for distributed layout management.
    **/
    public static void mergeFragment( Document fragment,
                                      Document composite )
    {
        Element fragmentLayout = fragment.getDocumentElement();
        Element fragmentRoot = (Element) fragmentLayout.getFirstChild();
        Element compositeLayout = composite.getDocumentElement();
        Element compositeRoot = (Element) compositeLayout.getFirstChild();
        mergeChildren( fragmentRoot, compositeRoot );
    }

    private static void mergeChildren( Element source, // parent of children
                                       Element dest )  // receiver of children
    {
        Document destDoc = dest.getOwnerDocument();

        NodeList children = source.getChildNodes();
        int length = children.getLength();
        
        for ( int i=0; i<length; i++ )
        {
            Object item = children.item(i);
            
            if ( ! ( item instanceof Element ) )
                continue;
            
            Element child = (Element) item;
            Element newChild = null;

            if( null != child )
            {
                newChild = (Element) destDoc.importNode( child, false );
                dest.appendChild( newChild );
                /* mrb DOM3 change
                destDoc.putIdentifier( newChild.getAttribute( "ID" ), newChild );
                The code below will ensure that any imported child having an
                attribute of "ID" will be locatable by doc.getElementById(XXX)
                where XXX is the value of that "ID" attribute.
                */
                String id = newChild.getAttribute(Constants.ATT_ID);
                if (id != null && ! id.equals(""))
                    newChild.setIdAttribute(Constants.ATT_ID, true);
                mergeChildren( child, newChild );
            }
        }
    }
}

