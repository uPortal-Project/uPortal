/**
 * Rss.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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
import com.objectspace.xml.IClassDeclaration;
import com.objectspace.xml.xgen.ClassDecl;

public class Rss implements IRss
  {
  public Hashtable _Attributes = new Hashtable();
  public IChannel _Channel = null;
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.Rss" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Attributes
  
  public String getAttribute( String name )
    {
    String value = (String) _Attributes.get( name );

    if( value != null ) 
      return value;

    return null;
    }
  
  public Hashtable getAttributes()
    {
    Hashtable clone = (Hashtable) _Attributes.clone();

    return clone;
    }
  
  public void setAttribute( String name, String value )
    {
    _Attributes.put( name, value );
    }
  
  public String removeAttribute( String name )
    {
    return (String) _Attributes.remove( name );
    }
  
  public String getVersionAttribute()
    {
    return getAttribute( "version" );
    }
  
  public void setVersionAttribute( String value )
    {
    setAttribute( "version", value );
    }
  
  public String removeVersionAttribute()
    {
    return removeAttribute( "version" );
    }

  // element Channel
  
  public IChannel getChannel()
    {
    return _Channel;
    }
  
  public void setChannel( IChannel arg0 )
    {
    _Channel = arg0;
    }
  }