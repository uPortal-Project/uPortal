/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

import java.util.Comparator;

/**
 * A comparator of fragment objects that sorts first by precedence value with
 * highest number being higher and if equal it then sorts by fragment index
 * with the lowest number having highest precedence since it is an indication
 * of the fragments location within the config file. For those with equal
 * precedence the one defined first when loading the file should take
 * precedence.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
   
public class FragmentComparator
    implements Comparator
{
    public static final String RCS_ID = "@(#) $Header$";

    public int compare(Object obj1, Object obj2)
    {
        FragmentDefinition frag1 = (FragmentDefinition) obj1;
        FragmentDefinition frag2 = (FragmentDefinition) obj2;
        
        if( frag1.getPrecedence() == frag2.getPrecedence() )
        {
            return frag1.getIndex() - frag2.getIndex();
        }
        else
        {
            return (int)(frag2.getPrecedence() - frag1.getPrecedence());
        }
    }
    public boolean equals(Object obj)
    {
        return obj.equals (this);
    }
}
