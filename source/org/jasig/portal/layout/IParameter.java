/**
 * IParameter.java	Java 1.3.0 Mon Jan 15 13:20:35 EST 2001
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

import java.util.Hashtable;

public interface IParameter extends com.objectspace.xml.IDXMLInterface ,com.objectspace.xml.IAttributeContainer
  {

  // element Attributes
  public String getNameAttribute();
  public void setNameAttribute( String value );
  public String removeNameAttribute();
  public String getValueAttribute();
  public void setValueAttribute( String value );
  public String removeValueAttribute();
  }