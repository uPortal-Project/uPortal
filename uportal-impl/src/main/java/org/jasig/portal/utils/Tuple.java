/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils;

/**
 * Simple object that contains two values whos references are immutable once initialized.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Tuple<A, B> {
    public final A first;
    public final B second;
    
    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }
}
