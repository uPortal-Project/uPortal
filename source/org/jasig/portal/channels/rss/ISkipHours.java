/**
 * ISkipHours.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public interface ISkipHours extends com.objectspace.xml.IDXMLInterface
  {

  // element Hour
  public void addHour( String arg0  );
  public int getHourCount();
  public void setHours( Vector arg0 );
  public String[] getHours();
  public void setHours( String[] arg0 );
  public Enumeration getHourElements();
  public String getHourAt( int arg0 );
  public void insertHourAt( String arg0, int arg1 );
  public void setHourAt( String arg0, int arg1 );
  public boolean removeHour( String arg0 );
  public void removeHourAt( int arg0 );
  }