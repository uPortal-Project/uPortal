/**
 * ISkipDays.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public interface ISkipDays extends com.objectspace.xml.IDXMLInterface
  {

  // element Day
  public void addDay( String arg0  );
  public int getDayCount();
  public void setDays( Vector arg0 );
  public String[] getDays();
  public void setDays( String[] arg0 );
  public Enumeration getDayElements();
  public String getDayAt( int arg0 );
  public void insertDayAt( String arg0, int arg1 );
  public void setDayAt( String arg0, int arg1 );
  public boolean removeDay( String arg0 );
  public void removeDayAt( int arg0 );
  }