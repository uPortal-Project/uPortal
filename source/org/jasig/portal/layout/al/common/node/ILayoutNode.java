/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.node;



/**
 * ILayoutNode interface.
 * Defines the generic set of methods on the user layout node.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public interface ILayoutNode extends INodeDescription {
    public ILayoutNode getParentNode();
    public ILayoutNode getNextSiblingNode();
    public ILayoutNode getPreviousSiblingNode();
    public ILayoutNode getFirstChildNode();    
}
