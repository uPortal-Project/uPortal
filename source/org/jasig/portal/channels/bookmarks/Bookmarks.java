/**
 * Bookmarks.java	Java 1.2.2 Mon Mar 06 11:06:32 PST 2000
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

import com.objectspace.xml.IClassDeclaration;
import java.util.Enumeration;
import java.util.Vector;
import com.objectspace.xml.xgen.ClassDecl;

public class Bookmarks implements IBookmarks
  {
  public Vector _Bookmark = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.bookmarks.Bookmarks" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Bookmark
  
  public void addBookmark( IBookmark arg0  )
    {
    if( _Bookmark != null )
      _Bookmark.addElement( arg0 );
    }
  
  public int getBookmarkCount()
    {
    return _Bookmark == null ? 0 : _Bookmark.size();
    }
  
  public void setBookmarks( Vector arg0 )
    {
    if( arg0 == null )
      {
      _Bookmark = null;
      return;
      }

    _Bookmark = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _Bookmark.addElement( string );
      }
    }
  
  public IBookmark[] getBookmarks()
    {
    if( _Bookmark == null )
      return null;

    IBookmark[] array = new IBookmark[ _Bookmark.size() ];
    _Bookmark.copyInto( array );

    return array;
    }
  
  public void setBookmarks( IBookmark[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _Bookmark = v ;
    }
  
  public Enumeration getBookmarkElements()
    {
    return _Bookmark == null ? null : _Bookmark.elements();
    }
  
  public IBookmark getBookmarkAt( int arg0 )
    {
    return _Bookmark == null ? null :  (IBookmark) _Bookmark.elementAt( arg0 );
    }
  
  public void insertBookmarkAt( IBookmark arg0, int arg1 )
    {
    if( _Bookmark != null )
      _Bookmark.insertElementAt( arg0, arg1 );
    }
  
  public void setBookmarkAt( IBookmark arg0, int arg1 )
    {
    if( _Bookmark != null )
      _Bookmark.setElementAt( arg0, arg1 );
    }
  
  public boolean removeBookmark( IBookmark arg0 )
    {
    if( _Bookmark == null )
      return false;

    return  _Bookmark.removeElement( arg0 );
    }
  
  public void removeBookmarkAt( int arg0 )
    {
    if( _Bookmark == null )
      return;

    Vector v = (Vector) _Bookmark.clone();
    v.removeElementAt( arg0 );


    _Bookmark.removeElementAt( arg0 );
    }
  }