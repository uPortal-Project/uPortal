/**
 * Factory.java	Java 1.2.2 Mon Mar 06 11:06:32 PST 2000
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

public class Factory
  {
  public static IBookmarks newBookmarks()
    {
    return new org.jasig.portal.channels.bookmarks.Bookmarks();
    }

  public static IBookmark newBookmark()
    {
    return new org.jasig.portal.channels.bookmarks.Bookmark();
    }

  }