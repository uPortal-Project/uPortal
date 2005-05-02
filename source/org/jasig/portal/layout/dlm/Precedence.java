/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.UserLayoutStoreFactory;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public final class Precedence
{
    public static final String RCS_ID = "@(#) $Header$";

    private double precedence = 0.0;
    private int index = -1;
    private static Precedence userPrecedence = new Precedence();

    private Precedence()
    {
    }

    static Precedence newInstance( String fragmentIdx )
    {
        if ( fragmentIdx == null ||
             fragmentIdx.equals( "" ) )
            return userPrecedence;
        return new Precedence( fragmentIdx );
    }

    public String toString()
    {
        return "p[" + precedence + ", " + index + "]";
    }
    
    private Precedence ( String fragmentIdx )
    {
        int fragmentIndex = 0;
        try
        {
            fragmentIndex = Integer.parseInt( fragmentIdx );
        }
        catch( Exception e )
        {
            // if unparsable default to lowest priority.
            return;
        }
                
        RDBMDistributedLayoutStore dls = ( RDBMDistributedLayoutStore )
            UserLayoutStoreFactory.getUserLayoutStoreImpl();
                
        this.precedence = dls.getFragmentPrecedence( fragmentIndex );
        this.index = fragmentIndex;
    }

    /**
       Returns true of this complete precedence is less than the complete
       precedence of the passed in Precedence object. The complete
       precedence takes into account the location in the configuration
       file of the fragment definition. If the "precedence" value is
       equal then the precedence object with the lowest index has the
       higher complete precedence. And index of -1 indicates the highest
       index in the file.
    */
    public boolean isLessThan ( Precedence p )
    {
        if ( this.precedence < p.precedence ||
             ( this.precedence == p.precedence &&
               ( this.index == -1 && p.index > -1 ||
                 this.index > p.index ) ) )
            return true;
        return false;
    }

    public boolean isEqualTo ( Precedence p )
    {
        return this.precedence == p.precedence;
    }
    
    public static Precedence getUserPrecedence()
    {
        return userPrecedence;
    }
}
