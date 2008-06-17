/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class NodeInfo
{
    public static final String RCS_ID = "@(#) $Header$";

    String id = null;
    Element node = null;
    boolean differentParent = false;
    int indexInCVP = -1; // CVP = Composite View Parent
    Precedence precedence = null;
    Element positionDirective = null;
    
    NodeInfo( Element node )
    {
        this.node = node;
        precedence = Precedence
        .newInstance( node.getAttribute( Constants.ATT_FRAGMENT ) );
        id = node.getAttribute( Constants.ATT_ID );
    }
    
    NodeInfo( Element node, int indexInCVP )
    {
        this( node );
        this.indexInCVP = indexInCVP;
    }
    
    public boolean equals( Object o )
    {
        if ( o != null &&
             o instanceof NodeInfo &&
             ((NodeInfo) o).id.equals( id ) )
            return true;
        if ( o == this )
            return true;
        return false;
    }

    public String toString()
    {
        return "ni[ id:" + id +
        ", diffPrnt:" + differentParent +
        ", idxInCVP:" + indexInCVP +
        ", prec:" + precedence +
        " ]";
    }
}
