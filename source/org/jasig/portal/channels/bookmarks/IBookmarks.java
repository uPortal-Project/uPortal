/**
 * IBookmarks.java	Java 1.2.2 Mon Mar 06 11:06:32 PST 2000
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

import java.util.Enumeration;
import java.util.Vector;

public interface IBookmarks extends com.objectspace.xml.IDXMLInterface
  {

  // element Bookmark
  public void addBookmark( IBookmark arg0  );
  public int getBookmarkCount();
  public void setBookmarks( Vector arg0 );
  public IBookmark[] getBookmarks();
  public void setBookmarks( IBookmark[] arg0 );
  public Enumeration getBookmarkElements();
  public IBookmark getBookmarkAt( int arg0 );
  public void insertBookmarkAt( IBookmark arg0, int arg1 );
  public void setBookmarkAt( IBookmark arg0, int arg1 );
  public boolean removeBookmark( IBookmark arg0 );
  public void removeBookmarkAt( int arg0 );
  }