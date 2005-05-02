/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;


import org.jasig.portal.layout.node.IUserLayoutNodeDescription;

/**
 * A class that embodies the logic for determining if a node can be moved to
 * the left or right of another node based on getMovedAllowed() and
 * getPrecedence().
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class MovementRules
{
    public static final String RCS_ID = "@(#) $Header$";
    
    /**
       Returns true if hopper is allowed to hop in the rightward, higher-
       sibling direction over the node being hopped; nbh. This is determined
       from the following tables. See notes from canHopLeft for details. In
       the tables below a zero equates to false and a one equates to true.
       Since moveAllowed is an attribute on a node a value of false equates
       to a string value of 'false'. The non-existence of a moveAllowed
       attribute defaults to making that node moveable. In otherwords the
       attribute is only included (asserted) when movement is being restricted.
       
       <pre>
       Scenario  |    NBH      |   hopper    |
                 | moveAllowed | moveAllowed |
       ----------+-------------+-------------+   
          A      |     0       |     0       |
          B      |     0       |     1       |
          C      |     1       |     0       |
          D      |     1       |     1       |
       ----------+-------------+-------------+
       </pre>

       In the table below Ph is the precedence of the hopper node. Pnbh is the
       precedence of the node being hopped. The values in the table indicate
       what values should be returned from this method. The scenarios A
       through D are as defined above for the moveAllowed values.

       <pre>
       Scenario --> | A | B | C | D |
       -------------+---+---+---+---+
        Pnbh = Ph   | 0 | 0 | 0 | 1 |
        Pnbh > Ph   | 0 | 1 | 1 | 1 |
        Pnbh < Ph   | 0 | 1 | 0 | 1 |
       -------------+---+---+---+---+
       </pre>
     */
    public static boolean canHopRight( IUserLayoutNodeDescription hopper,
                                       IUserLayoutNodeDescription nbh ) 
    {
        boolean nbhMoveAld = nbh.isMoveAllowed();
        boolean hopperMoveAld = hopper.isMoveAllowed();

        if ( nbhMoveAld == hopperMoveAld )
            return nbhMoveAld;

        // from the above test we know that if we get here, moveAld's differ
        double Pnbh = nbh.getPrecedence();
        double Ph = hopper.getPrecedence();

        if ( Pnbh > Ph )
            return true;

        if ( Pnbh < Ph &&
             hopperMoveAld == true )
            return true;
        
        return false;
    }

    /**
       Returns true if hopper is allowed to hop in the leftward, lower-
       sibling direction over the node being hopped; nbh. This is determined
       from the following tables. Since lower sibling order is tied to left or
       top most viewing locations on the screen these positions are considered
       to be superior in real-estate value. Hence to protect viewability as
       much as possible within a precedence a fragment owner can movement
       restrict a node, lets say a tab for example, to prevent users from
       placing nodes with equal or lesser precedence to its left and
       ultimately force that tab off of the right side of the screen.

       Sibling nodes are mapped left to right on the screen for lowest to
       highest child node index in the parent node for tabs and columns and
       from top to bottom on the screen for lowest to highest child node index
       in the parent node for channels. In the tables below a
       zero equates to false and a one equates to true.
       
       <pre>
       Scenario  |    NBH      |   hopper    |
                 | moveAllowed | moveAllowed |
       ----------+-------------+-------------+   
          A      |     0       |     0       |
          B      |     0       |     1       |
          C      |     1       |     0       |
          D      |     1       |     1       |
       ----------+-------------+-------------+
       </pre>

       In the table below Ph is the precedence of the hopper node. Pnbh is the
       precedence of the node being hopped. The values in the table indicate
       what values should be returned from this method. The scenarios are A
       through D are as defined above for the moveAllowed values.

       These return values were
       determined by setting up each scenario on paper with tabs as the nodes
       and determining what expected and reasonable behavior should be. A
       higher precedence value takes precedence over movement restrictions
       imposed by a node from a fragment with lesser precedence.
       
       <pre>
       Scenario --> | A | B | C | D |
       -------------+---+---+---+---+
        Pnbh = Ph   | 0 | 0 | 0 | 1 |
        Pnbh > Ph   | 0 | 0 | 1 | 1 |
        Pnbh < Ph   | 0 | 1 | 1 | 1 |
       -------------+---+---+---+---+
       </pre>

       Patterns in these tables were used to simplify checks needed to return
       the appropriate values. For example when the moveAllowed values are the
       same the returned value matches the moveAllowed value.
     */
    public static boolean canHopLeft( IUserLayoutNodeDescription hopper,
                                      IUserLayoutNodeDescription nbh )
    {
        boolean nbhMoveAld = nbh.isMoveAllowed();
        boolean hopperMoveAld = hopper.isMoveAllowed();

        if ( nbhMoveAld == hopperMoveAld )
            return nbhMoveAld;

        // from the above test we know that if we get here, moveAld's differ
        double Pnbh = nbh.getPrecedence();
        double Ph = hopper.getPrecedence();

        if ( Pnbh < Ph )
            return true;

        if ( Pnbh > Ph &&
             nbhMoveAld == true )
            return true;
        
        return false;
    }
}
