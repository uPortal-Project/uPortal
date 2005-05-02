/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.util.Comparator;

/**
   A comparator of fragment objects that sorts first by precedence value with
   highest number being higher and if equal it then sorts by fragment index
   with the lowest number having highest precedence since it is an indication
   of the fragments location within the config file. For those with equal
   precedence the one defined first when loading the file should take
   precedence.
 */
   
public class FragmentComparator
    implements Comparator
{
    public static final String RCS_ID = "@(#) $Header$";

    public int compare(Object obj1, Object obj2)
    {
        FragmentDefinition frag1 = (FragmentDefinition) obj1;
        FragmentDefinition frag2 = (FragmentDefinition) obj2;
        
        if( frag1.precedence == frag2.precedence )
        {
            return frag1.index - frag2.index;
        }
        else
        {
            return (int)(frag2.precedence - frag1.precedence);
        }
    }
    public boolean equals(Object obj)
    {
        return obj.equals (this);
    }
}
