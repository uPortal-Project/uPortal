/**
 * Bookmark.java	Java 1.2.2 Mon Mar 06 11:06:32 PST 2000
 *
 * Copyright 1999 by ObjectSpace, Inc.,
 * 14850 Quorum Dr., Dallas, TX, 75240 U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of ObjectSpace, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with ObjectSpace.
 */

package org.jasig.portal.channels.bookmarks;

import java.util.Hashtable;
import com.objectspace.xml.IClassDeclaration;
import com.objectspace.xml.xgen.ClassDecl;

public class Bookmark implements IBookmark
  {
  public Hashtable _Attributes = new Hashtable();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.bookmarks.Bookmark" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Attributes
  
  public String getAttribute( String name )
    {
    String value = (String) _Attributes.get( name );

    if( value != null ) 
      return value;

    return null;
    }
  
  public Hashtable getAttributes()
    {
    Hashtable clone = (Hashtable) _Attributes.clone();

    return clone;
    }
  
  public void setAttribute( String name, String value )
    {
    _Attributes.put( name, value );
    }
  
  public String removeAttribute( String name )
    {
    return (String) _Attributes.remove( name );
    }
  
  public String getUrlAttribute()
    {
    return getAttribute( "url" );
    }
  
  public void setUrlAttribute( String value )
    {
    setAttribute( "url", value );
    }
  
  public String removeUrlAttribute()
    {
    return removeAttribute( "url" );
    }
  
  public String getCommentsAttribute()
    {
    return getAttribute( "comments" );
    }
  
  public void setCommentsAttribute( String value )
    {
    setAttribute( "comments", value );
    }
  
  public String removeCommentsAttribute()
    {
    return removeAttribute( "comments" );
    }
  
  public String getNameAttribute()
    {
    return getAttribute( "name" );
    }
  
  public void setNameAttribute( String value )
    {
    setAttribute( "name", value );
    }
  
  public String removeNameAttribute()
    {
    return removeAttribute( "name" );
    }
  }