/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.serialize;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio..com">Assaf Arkin</a>
 * @see OutputFormat
 */
public final class LineSeparator
{
    
    
    /**
     * Line separator for Unix systems (<tt>\n</tt>).
     */
    public static final String Unix = "\n";
    
    
    /**
     * Line separator for Windows systems (<tt>\r\n</tt>).
     */
    public static final String Windows = "\r\n";
    
    
    /**
     * Line separator for Macintosh systems (<tt>\r</tt>).
     */
    public static final String Macintosh = "\r";
    
    
    /**
     * Line separator for the Web (<tt>\n</tt>).
     */
    public static final String Web = "\n";
    
    
}


