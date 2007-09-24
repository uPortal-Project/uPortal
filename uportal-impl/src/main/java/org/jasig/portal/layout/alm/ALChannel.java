/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import org.jasig.portal.layout.node.IUserLayoutNodeDescription;


/**
 * UserLayoutFolder summary sentence goes here.
 * <p>
 * Company: Instructional Media &amp; Magic
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author Michael Ivanov mailto:mvi@immagic.com
 * @version $Revision$
 */


public class ALChannel extends ALNode {

    public ALChannel() {
        super();
    }

    public ALChannel ( IALChannelDescription nd ) {
        super (nd);
    }

    /**
     * Gets the node type
     * @return a node type
     */
     public int getNodeType() {
       return IUserLayoutNodeDescription.CHANNEL;
     }
}
