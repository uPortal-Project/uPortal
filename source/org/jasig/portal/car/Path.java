/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

/**
   Holds onto a set of String path elements to assist with tracking where in
   an XML structure SAX processing is currently working. So an XML structure
   like the following would have a path of "<top>", "<next>", "<more>" when SAX
   processing issued a startElement event call for the "more" element.
   <code>
   &lt;top>
    &lt;next>
     &lt;more>
     ...
   </code>

   Use the fromXML method to create from a more visually symbolic view of
   what the path represents.
 */
public class Path
{
    public static final String RCS_ID = "@(#) $Header$";
    private LinkedList list = new LinkedList();

    /**
       Create a new empty path.
     */
    public Path()
    {
    }

    private static final int OUT_OF_TAG = 0;
    private static final int IN_TAG = 1;

    public static Path fromTag(String tagName)
    {
        return fromXML("<" + tagName + ">");
    }
    
    /** 
        Creates a Path from the XML structured snippet. The following call
        would create a path that contained "top", "next", and "more" in that
        order.
    */
    public static Path fromXML( String xmlPath )
    {
        Path path = new Path();

        if ( xmlPath == null )
            return path;
        
        int state = OUT_OF_TAG;
        StringBuffer label = new StringBuffer();
                
        for( int i=0; i<xmlPath.length(); i++ )
        {
            char c = xmlPath.charAt( i );
            
            if ( state == OUT_OF_TAG )
            {
                if ( c == '<' )
                    state = IN_TAG;
            }
            else // in tag
            {
                if ( c == '>' )
                {
                    state = OUT_OF_TAG;
                    path.append( label.toString() );
                    label.delete( 0, label.length() );
                }
                else
                    label.append( c );
            }
        }
        return path;
    }
    
    /**
       Add an item to the path.
     */
    public Path append( String item )
    {
        if ( item != null )
            list.add( item );
        return this;
    }

    /**
       Remove the last item off of the path.
     */
    public String removeLast()
    {
        if ( list.size() > 0 )
            return (String) list.removeLast();
        return null;
    }

    /**
       Returns true if the passed in object is a path with the same number of
       path items and all strings in the two paths are equal.
     */
    public boolean equals( Object o )
    {
        if ( ! ( o instanceof Path ) )
            return false;

        return list.equals( ((Path) o).list );
    }

    public String toString()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        
        for( Iterator i=list.iterator(); i.hasNext(); )
            pw.print( "<" + i.next() + ">" );
        pw.flush();
        return sw.toString();
    }
}
