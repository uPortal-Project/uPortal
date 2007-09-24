/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
    private static final Log LOG = LogFactory.getLog(ILFBuilder.class);

    public static Document constructILF( Document PLF, Vector sequence, IPerson person)
    throws javax.xml.parsers.ParserConfigurationException, AuthorizationException
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Constructing ILF for IPerson='" + person + "'");
        }
        // first construct the destination document and root element. The root
        // element should be a complete copy of the PLF's root including its
        // node identifier in the new document. This requires the use of
        // the implementation class to set the identifier for that node
        // in the document.

        Document result = DocumentFactory.getNewDocument();
        Element plfLayout = PLF.getDocumentElement();
        Element ilfLayout = (Element) result.importNode( plfLayout, false );
        result.appendChild( ilfLayout );
        Element plfRoot = (Element) plfLayout.getFirstChild();
        Element ilfRoot = (Element) result.importNode( plfRoot, false);
        ilfLayout.appendChild(ilfRoot);
        
        if (ilfRoot.getAttribute(Constants.ATT_ID) != null)
            ilfRoot.setIdAttribute(Constants.ATT_ID, true);

        // build the auth principal for determining if pushed channels can be 
        // used by this user
        EntityIdentifier ei = person.getEntityIdentifier();
        AuthorizationService authS = AuthorizationService.instance();
        IAuthorizationPrincipal ap = authS.newPrincipal(ei.getKey(), 
                ei.getType());

        // now merge fragments one at a time into ILF document
        Enumeration fragments = sequence.elements();

        while( fragments.hasMoreElements() )
            mergeFragment( (Document) fragments.nextElement(), result, ap );
        return result;
    }

    /**
     * Passes the layout root of each of these documents to mergeChildren
     * causing all children of newLayout to be merged into compositeLayout
     * following merging protocal for distributed layout management.
     * @throws AuthorizationException
    **/
    public static void mergeFragment( Document fragment,
                                      Document composite,
                                      IAuthorizationPrincipal ap ) 
    throws AuthorizationException
    {
        Element fragmentLayout = fragment.getDocumentElement();
        Element fragmentRoot = (Element) fragmentLayout.getFirstChild();
        Element compositeLayout = composite.getDocumentElement();
        Element compositeRoot = (Element) compositeLayout.getFirstChild();
        mergeChildren( fragmentRoot, compositeRoot, ap, new HashSet() );
    }    


    /**
     * @param source parent of children
     * @param dest receiver of children
     * @param ap User's authorization principal for determining if they can view a channel
     * @param visitedNodes A Set of nodes from the source tree that have been visited to get to this node, used to ensure a loop doesn't exist in the source tree.
     * @throws AuthorizationException
     */
    private static void mergeChildren( Element source,
                                       Element dest, 
                                       IAuthorizationPrincipal ap,
                                       Set visitedNodes ) 
    throws AuthorizationException
    {
        //Record this node in the visited nodes set. If add returns false a loop has been detected
        if (!visitedNodes.add(source)) {
            final String msg = "mergeChildren has encountered a loop in the source DOM. currentNode='" + source + "', currentDepth='" + visitedNodes.size() + "', visitedNodes='" + visitedNodes + "'";
            final IllegalStateException ise = new IllegalStateException(msg);
            LOG.error(msg, ise);
            
            printNodeToDebug(source, "Source");
            printNodeToDebug(dest, "Dest");
            
            throw ise;
        }
        
        Document destDoc = dest.getOwnerDocument();

        Node item = source.getFirstChild();
        while (item != null) {
            if (item instanceof Element) {
                
                Element child = (Element) item;
                Element newChild = null;
    
                if( null != child && mergeAllowed( child, ap ))
                {
                    newChild = (Element) destDoc.importNode( child, false );
                    dest.appendChild( newChild );
                    String id = newChild.getAttribute(Constants.ATT_ID);
                    if (id != null && ! id.equals(""))
                        newChild.setIdAttribute(Constants.ATT_ID, true);
                    mergeChildren( child, newChild, ap, visitedNodes );
                }
            }
            
            item = item.getNextSibling();
        }
        
        //Remove this node from the visited nodes set
        visitedNodes.remove(source);
    }
    
    /**
     * Tests to see if channels to be merged from ILF can be rendered by the
     * end user. If not then they are discarded from the merge.
     * 
     * @param child
     * @param person
     * @return
     * @throws AuthorizationException
     * @throws NumberFormatException
     */
    private static boolean mergeAllowed( Element child, 
            IAuthorizationPrincipal ap ) 
    throws AuthorizationException
    {
        if (! child.getTagName().equals("channel"))
            return true;
        
        String channelPublishId = child.getAttribute("chanID");
        return ap.canRender(Integer.parseInt(channelPublishId));
    }

    private static void printNodeToDebug(Node n, String name) throws TransformerFactoryConfigurationError {
        if (!LOG.isDebugEnabled()) {
            return;
        }

        final StringWriter writer = new StringWriter();
        
        try {
            final TransformerFactory transFactory = TransformerFactory.newInstance();
            final Transformer trans = transFactory.newTransformer();
            
            final Source xmlSource = new DOMSource(n);
            
            final Result transResult = new StreamResult(writer);
            trans.transform(xmlSource, transResult);
            
            final String xmlStr = writer.toString();
            LOG.debug(name + " DOM Tree:\n\n" + xmlStr);
        }
        catch (Exception e) {
            LOG.error("Error printing out " + name + " DOM Tree", e);
            final String xmlStr = writer.toString();
            LOG.debug("Partial " + name + " DOM Tree:\n\n" + xmlStr);
        }
    }
}
