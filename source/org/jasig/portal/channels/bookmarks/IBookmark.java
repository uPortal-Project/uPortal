/**
 * IBookmark.java	Java 1.2.2 Mon Mar 06 11:06:32 PST 2000
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

public interface IBookmark extends com.objectspace.xml.IDXMLInterface ,com.objectspace.xml.IAttributeContainer
  {

  // element Attributes
  public String getUrlAttribute();
  public void setUrlAttribute( String value );
  public String removeUrlAttribute();
  public String getCommentsAttribute();
  public void setCommentsAttribute( String value );
  public String removeCommentsAttribute();
  public String getNameAttribute();
  public void setNameAttribute( String value );
  public String removeNameAttribute();
  }