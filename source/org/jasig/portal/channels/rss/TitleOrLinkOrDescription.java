/**
 * TitleOrLinkOrDescription.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

import com.objectspace.xml.core.StringWrapper;
import com.objectspace.xml.IClassDeclaration;
import com.objectspace.xml.xgen.ClassDecl;

public class TitleOrLinkOrDescription implements ITitleOrLinkOrDescription
  {
  public StringWrapper _Title = null;
  public StringWrapper _Link = null;
  public StringWrapper _Description = null;
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.TitleOrLinkOrDescription" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Title
  
  public String getTitle()
    {
    return _Title == null ? null : _Title.getRecursiveValue();
    }
  
  public void setTitle( String arg0 )
    {
    _Link = null;
    _Description = null;

    _Title = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Link
  
  public String getLink()
    {
    return _Link == null ? null : _Link.getRecursiveValue();
    }
  
  public void setLink( String arg0 )
    {
    _Title = null;
    _Description = null;

    _Link = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Description
  
  public String getDescription()
    {
    return _Description == null ? null : _Description.getRecursiveValue();
    }
  
  public void setDescription( String arg0 )
    {
    _Title = null;
    _Link = null;

    _Description = arg0 == null ? null : new StringWrapper( arg0 );
    }
  }