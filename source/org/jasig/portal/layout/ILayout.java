/**
 * ILayout.java	Java 1.3.0 Mon Jan 15 13:20:35 EST 2001
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

public interface ILayout extends com.objectspace.xml.IDXMLInterface ,com.objectspace.xml.IAttributeContainer
  {

  // element Attributes
  public String getActiveTabAttribute();
  public void setActiveTabAttribute( String value );
  public String removeActiveTabAttribute();
  public String getBgcolorAttribute();
  public void setBgcolorAttribute( String value );
  public String removeBgcolorAttribute();
  public String getActiveTabColorAttribute();
  public void setActiveTabColorAttribute( String value );
  public String removeActiveTabColorAttribute();
  public String getFgcolorAttribute();
  public void setFgcolorAttribute( String value );
  public String removeFgcolorAttribute();
  public String getChannelHeadingColorAttribute();
  public void setChannelHeadingColorAttribute( String value );
  public String removeChannelHeadingColorAttribute();
  public String getTabColorAttribute();
  public void setTabColorAttribute( String value );
  public String removeTabColorAttribute();

  // element Tab
  public void addTab( ITab arg0  );
  public int getTabCount();
  public void setTabs( Vector arg0 );
  public ITab[] getTabs();
  public void setTabs( ITab[] arg0 );
  public Enumeration getTabElements();
  public ITab getTabAt( int arg0 );
  public void insertTabAt( ITab arg0, int arg1 );
  public void setTabAt( ITab arg0, int arg1 );
  public boolean removeTab( ITab arg0 );
  public void removeTabAt( int arg0 );
  }