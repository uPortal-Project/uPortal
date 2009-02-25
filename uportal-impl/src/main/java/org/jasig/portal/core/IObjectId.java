/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.core;

import java.io.Serializable;


/**
 * An object identifier interface.
 * @author Michael Ivanov <a href="mailto:">mvi@immagic.com</a>
 * @version $Revision$
 */
public interface IObjectId extends Serializable {
    
    public int toInt();
    
    public long toLong();
    
    public String toString();
    
}
