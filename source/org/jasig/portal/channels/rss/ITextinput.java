/**
 * ITextinput.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

package org.jasig.portal.channels.rss;

import java.util.Enumeration;
import java.util.Vector;

public interface ITextinput extends com.objectspace.xml.IDXMLInterface
  {

  // element TitleOrDescriptionOrNameOrLink
  public void addTitleOrDescriptionOrNameOrLink( ITitleOrDescriptionOrNameOrLink arg0  );
  public int getTitleOrDescriptionOrNameOrLinkCount();
  public void setTitleOrDescriptionOrNameOrLinks( Vector arg0 );
  public ITitleOrDescriptionOrNameOrLink[] getTitleOrDescriptionOrNameOrLinks();
  public void setTitleOrDescriptionOrNameOrLinks( ITitleOrDescriptionOrNameOrLink[] arg0 );
  public Enumeration getTitleOrDescriptionOrNameOrLinkElements();
  public ITitleOrDescriptionOrNameOrLink getTitleOrDescriptionOrNameOrLinkAt( int arg0 );
  public void insertTitleOrDescriptionOrNameOrLinkAt( ITitleOrDescriptionOrNameOrLink arg0, int arg1 );
  public void setTitleOrDescriptionOrNameOrLinkAt( ITitleOrDescriptionOrNameOrLink arg0, int arg1 );
  public boolean removeTitleOrDescriptionOrNameOrLink( ITitleOrDescriptionOrNameOrLink arg0 );
  public void removeTitleOrDescriptionOrNameOrLinkAt( int arg0 );
  public void removeAllTitleOrDescriptionOrNameOrLinks();
  }