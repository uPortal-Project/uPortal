/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.NodeType;



/**
 * UserLayoutFolder summary sentence goes here.
 * <p>
 * Company: Instructional Media &amp; Magic
 *
 * @author Michael Ivanov mailto:mvi@immagic.com
 * @version $Revision$
 */


public class ALChannel extends ALNode {

    public ALChannel ( IALChannelDescription nd ) {
        super (nd);
    }

    /**
     * Gets the node type
     * @return a node type
     */
     public NodeType getNodeType() {
       return NodeType.CHANNEL;
     }
}
