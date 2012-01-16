package org.jasig.portal.layout;

import org.w3c.dom.Document;

/**
 * Implementations of this interface should be used when searching for node identifier. For example, it might be tab
 * identifier lookup based on channel ID or channel ID lookup by its functional name. Implementations are used when
 * calling {@link IUserLayout#findNodeId(NodeIdFinder)}. The same behavior can be achieved using XPath expressions (and
 * vice versa), but traversing DOM "manually" is much faster in some situations. Note that with simple XPath expressions
 * it might not
 * 
 * @author Arvīds Grabovskis
 */
public interface INodeIdResolver {

    /**
     * Traverse the document in order to find the required node identifier.
     * 
     * @param document User layout document.
     * @return The ID of the resolved node, null if there is no match
     */
    public String traverseDocument(Document document);
}