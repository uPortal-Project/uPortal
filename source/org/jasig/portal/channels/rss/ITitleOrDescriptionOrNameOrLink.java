/**
 * ITitleOrDescriptionOrNameOrLink.java	Java 1.2.2 Mon Mar 06 11:01:13 PST 2000
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

public interface ITitleOrDescriptionOrNameOrLink extends com.objectspace.xml.IDXMLInterface
  {

  // element Title
  public String getTitle();
  public void setTitle( String arg0 );

  // element Description
  public String getDescription();
  public void setDescription( String arg0 );

  // element Name
  public String getName();
  public void setName( String arg0 );

  // element Link
  public String getLink();
  public void setLink( String arg0 );
  }