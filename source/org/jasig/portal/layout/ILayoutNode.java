/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;


/**
 * ILayoutNode interface.
 * Defines the generic set of methods on the user layout node.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public interface ILayoutNode {

     public String getId();

     public int getNodeType();

     public IUserLayoutNodeDescription getNodeDescription();

     public String getParentNodeId();

     public String getNextNodeId();

     public String getPreviousNodeId();
    
}
