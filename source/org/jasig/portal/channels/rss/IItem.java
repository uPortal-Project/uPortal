/**
 * IItem.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public interface IItem extends com.objectspace.xml.IDXMLInterface
  {

  // element TitleOrLinkOrDescription
  public void addTitleOrLinkOrDescription( ITitleOrLinkOrDescription arg0  );
  public int getTitleOrLinkOrDescriptionCount();
  public void setTitleOrLinkOrDescriptions( Vector arg0 );
  public ITitleOrLinkOrDescription[] getTitleOrLinkOrDescriptions();
  public void setTitleOrLinkOrDescriptions( ITitleOrLinkOrDescription[] arg0 );
  public Enumeration getTitleOrLinkOrDescriptionElements();
  public ITitleOrLinkOrDescription getTitleOrLinkOrDescriptionAt( int arg0 );
  public void insertTitleOrLinkOrDescriptionAt( ITitleOrLinkOrDescription arg0, int arg1 );
  public void setTitleOrLinkOrDescriptionAt( ITitleOrLinkOrDescription arg0, int arg1 );
  public boolean removeTitleOrLinkOrDescription( ITitleOrLinkOrDescription arg0 );
  public void removeTitleOrLinkOrDescriptionAt( int arg0 );
  public void removeAllTitleOrLinkOrDescriptions();
  }