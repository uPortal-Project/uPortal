/**
 * ITab.java	Java 1.3.0 Mon Jan 15 13:20:35 EST 2001
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

public interface ITab extends com.objectspace.xml.IDXMLInterface ,com.objectspace.xml.IAttributeContainer
  {

  // element Attributes
  public String getRemovableAttribute();
  public void setRemovableAttribute( String value );
  public String removeRemovableAttribute();
  public String getNameAttribute();
  public void setNameAttribute( String value );
  public String removeNameAttribute();
  public String getHiddenAttribute();
  public void setHiddenAttribute( String value );
  public String removeHiddenAttribute();

  // element Column
  public void addColumn( IColumn arg0  );
  public int getColumnCount();
  public void setColumns( Vector arg0 );
  public IColumn[] getColumns();
  public void setColumns( IColumn[] arg0 );
  public Enumeration getColumnElements();
  public IColumn getColumnAt( int arg0 );
  public void insertColumnAt( IColumn arg0, int arg1 );
  public void setColumnAt( IColumn arg0, int arg1 );
  public boolean removeColumn( IColumn arg0 );
  public void removeColumnAt( int arg0 );
  }