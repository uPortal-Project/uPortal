/**
 * IChannel.java	Java 1.3.0 Mon Dec 11 17:52:47 EST 2000
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

public interface IChannel extends com.objectspace.xml.IDXMLInterface ,com.objectspace.xml.IAttributeContainer
  {

  // element Attributes
  public String getMinimizedAttribute();
  public void setMinimizedAttribute( String value );
  public String removeMinimizedAttribute();
  public String getGlobalChannelIDAttribute();
  public void setGlobalChannelIDAttribute( String value );
  public String removeGlobalChannelIDAttribute();
  public String getInstanceIDAttribute();
  public void setInstanceIDAttribute( String value );
  public String removeInstanceIDAttribute();
  public String getHiddenAttribute();
  public void setHiddenAttribute( String value );
  public String removeHiddenAttribute();
  public String getClassAttribute();
  public void setClassAttribute( String value );
  public String removeClassAttribute();

  // element Parameter
  public void addParameter( IParameter arg0  );
  public int getParameterCount();
  public void setParameters( Vector arg0 );
  public IParameter[] getParameters();
  public void setParameters( IParameter[] arg0 );
  public Enumeration getParameterElements();
  public IParameter getParameterAt( int arg0 );
  public void insertParameterAt( IParameter arg0, int arg1 );
  public void setParameterAt( IParameter arg0, int arg1 );
  public boolean removeParameter( IParameter arg0 );
  public void removeParameterAt( int arg0 );
  public void removeAllParameters();
  }