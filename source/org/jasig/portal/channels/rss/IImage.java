/**
 * IImage.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public interface IImage extends com.objectspace.xml.IDXMLInterface
  {

  // element TitleOrUrlOrLinkOrWidthOrHeightOrDescription
  public void addTitleOrUrlOrLinkOrWidthOrHeightOrDescription( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription arg0  );
  public int getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionCount();
  public void setTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions( Vector arg0 );
  public ITitleOrUrlOrLinkOrWidthOrHeightOrDescription[] getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions();
  public void setTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription[] arg0 );
  public Enumeration getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionElements();
  public ITitleOrUrlOrLinkOrWidthOrHeightOrDescription getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionAt( int arg0 );
  public void insertTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionAt( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription arg0, int arg1 );
  public void setTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionAt( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription arg0, int arg1 );
  public boolean removeTitleOrUrlOrLinkOrWidthOrHeightOrDescription( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription arg0 );
  public void removeTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionAt( int arg0 );
  public void removeAllTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions();
  }