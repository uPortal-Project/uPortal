/**
 * IColumn.java	Java 1.3.0 Mon Jan 15 13:20:35 EST 2001
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

package org.jasig.portal.layout;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

public interface IColumn extends com.objectspace.xml.IDXMLInterface ,com.objectspace.xml.IAttributeContainer
  {

  // element Attributes
  public String getWidthAttribute();
  public void setWidthAttribute( String value );
  public String removeWidthAttribute();

  // element Channel
  public void addChannel( IChannel arg0  );
  public int getChannelCount();
  public void setChannels( Vector arg0 );
  public IChannel[] getChannels();
  public void setChannels( IChannel[] arg0 );
  public Enumeration getChannelElements();
  public IChannel getChannelAt( int arg0 );
  public void insertChannelAt( IChannel arg0, int arg1 );
  public void setChannelAt( IChannel arg0, int arg1 );
  public boolean removeChannel( IChannel arg0 );
  public void removeChannelAt( int arg0 );
  public void removeAllChannels();
  }