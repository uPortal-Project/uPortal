/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
