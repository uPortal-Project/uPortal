/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class CompositeEnumeration
    implements Enumeration
{
    public static final String RCS_ID = "@(#) $Header$";

    private Enumeration first = null;
    private Enumeration second = null;

    /**
     * Constructs an enumeration object that enumerates over the elements
     * in the first passed in enumeration object and then enumerates over
     * those in the second passed in enumeration object. 
     */
    CompositeEnumeration( Enumeration first,
                          Enumeration second )
    {
        this.first = first;
        this.second = second;
    }

    public boolean hasMoreElements()
    {
        return first.hasMoreElements() || second.hasMoreElements();
    }

    public Object nextElement()
        throws NoSuchElementException
    {
        if ( first.hasMoreElements() )
            return first.nextElement();
        return second.nextElement();
    }
}
