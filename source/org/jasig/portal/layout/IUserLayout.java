/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.util.Enumeration;
import org.jasig.portal.PortalException;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * An interface representing the user layout.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public interface IUserLayout {

    /**
     * Writes user layout content (with appropriate markings) into
     * a <code>ContentHandler</code>
     *
     * @param ch a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
    public void writeTo(ContentHandler ch) throws PortalException;

    /**
     * Writes subtree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>ContentHandler</code>
     *
     * @param nodeId a <code>String</code> a node determining a user layout subtree.
     * @param ch a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
    public void writeTo(String nodeId, ContentHandler ch) throws PortalException;

    /**
     * Writes user layout content (with appropriate markings) into
     * a <code>Document</code> object
     *
     * @param document a <code>Document</code> value
     * @exception PortalException if an error occurs
     */
    public void writeTo(Document document) throws PortalException;

    /**
     * Writes subtree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>Document</code>
     *
     * @param nodeId a <code>String</code> a node determining a user layout subtree.
     * @param document a <code>Document</code> object
     * @exception PortalException if an error occurs
     */
    public void writeTo(String nodeId, Document document) throws PortalException;

    /**
     * Obtain a description of a node (channel or a folder) in a given user layout.
     *
     * @param nodeId a <code>String</code> channel subscribe id or folder id.
     * @return an <code>UserLayoutNodeDescription</code> value
     * @exception PortalException if an error occurs
     */
    public IUserLayoutNodeDescription getNodeDescription(String nodeId) throws PortalException;

    /**
     * Returns an Id of a parent user layout node.
     * The user layout root node always has ID="root"
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public String getParentId(String nodeId) throws PortalException;

    /**
     * Returns a list of child node Ids for a given node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>Enumeration</code> of <code>String</code> child node Ids.
     * @exception PortalException if an error occurs
     */
    public Enumeration getChildIds(String nodeId) throws PortalException;

    /**
     * Determine an Id of a next sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a next sibling node, or <code>null</code> if this is the last sibling.
     * @exception PortalException if an error occurs
     */
    public String getNextSiblingId(String nodeId) throws PortalException;


    /**
     * Determine an Id of a previous sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a previous sibling node, or <code>null</code> if this is the first sibling.
     * @exception PortalException if an error occurs
     */
    public String getPreviousSiblingId(String nodeId) throws PortalException;

    /**
     * Return a cache key, uniqly corresponding to the composition and the structure of the user layout.
     *
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public String getCacheKey() throws PortalException;


    /**
     * Register a layout event listener
     *
     * @param l a <code>LayoutEventListener</code> object
     * @return a <code>boolean</code> success status
     */
    public boolean addLayoutEventListener(LayoutEventListener l);


    /**
     * Remove a registered layout event listener.
     *
     * @param l a <code>LayoutEventListener</code> object
     * @return a <code>boolean</code> success status
     */
    public boolean removeLayoutEventListener(LayoutEventListener l);

    /**
     * Returns a layout Id associated with this manager/
     *
     * @return an <code>String</code> layout Id value;
     */
    public String getId();

    /**
     * Returns a node id associated with the supplied functional name.
     *
     * @param fname  the functional name to lookup
     * @return a <code>String</code> subscription id
     * @exception PortalException if an error occurs
     */
    public String getNodeId(String fname) throws PortalException;

     /**
     * Returns a list of node Ids in the layout.
     *
     * @return a <code>Enumeration</code> of node Ids
     * @exception PortalException if an error occurs
     */
    public Enumeration getNodeIds() throws PortalException;

    /**
     * Returns an id of the root node.
     *
     * @return a <code>String</code> value
     */
    public String getRootId();


}
