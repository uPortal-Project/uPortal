/**
 * IRss.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

import java.util.Hashtable;

public interface IRss extends com.objectspace.xml.IDXMLInterface ,com.objectspace.xml.IAttributeContainer
  {

  // element Attributes
  public String getVersionAttribute();
  public void setVersionAttribute( String value );
  public String removeVersionAttribute();

  // element Channel
  public IChannel getChannel();
  public void setChannel( IChannel arg0 );
  }