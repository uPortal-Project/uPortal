/**
 * TitleOrUrlOrLinkOrWidthOrHeightOrDescription.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public class TitleOrUrlOrLinkOrWidthOrHeightOrDescription implements ITitleOrUrlOrLinkOrWidthOrHeightOrDescription
  {
  public StringWrapper _Title = null;
  public StringWrapper _Url = null;
  public StringWrapper _Link = null;
  public StringWrapper _Width = null;
  public StringWrapper _Height = null;
  public StringWrapper _Description = null;
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.TitleOrUrlOrLinkOrWidthOrHeightOrDescription" );
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
    _Url = null;
    _Link = null;
    _Width = null;
    _Height = null;
    _Description = null;

    _Title = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Url
  
  public String getUrl()
    {
    return _Url == null ? null : _Url.getRecursiveValue();
    }
  
  public void setUrl( String arg0 )
    {
    _Title = null;
    _Link = null;
    _Width = null;
    _Height = null;
    _Description = null;

    _Url = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Link
  
  public String getLink()
    {
    return _Link == null ? null : _Link.getRecursiveValue();
    }
  
  public void setLink( String arg0 )
    {
    _Title = null;
    _Url = null;
    _Width = null;
    _Height = null;
    _Description = null;

    _Link = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Width
  
  public String getWidth()
    {
    return _Width == null ? null : _Width.getRecursiveValue();
    }
  
  public void setWidth( String arg0 )
    {
    _Title = null;
    _Url = null;
    _Link = null;
    _Height = null;
    _Description = null;

    _Width = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeWidth()
    {
    _Width = null;
    }

  // element Height
  
  public String getHeight()
    {
    return _Height == null ? null : _Height.getRecursiveValue();
    }
  
  public void setHeight( String arg0 )
    {
    _Title = null;
    _Url = null;
    _Link = null;
    _Width = null;
    _Description = null;

    _Height = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeHeight()
    {
    _Height = null;
    }

  // element Description
  
  public String getDescription()
    {
    return _Description == null ? null : _Description.getRecursiveValue();
    }
  
  public void setDescription( String arg0 )
    {
    _Title = null;
    _Url = null;
    _Link = null;
    _Width = null;
    _Height = null;

    _Description = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeDescription()
    {
    _Description = null;
    }
  }