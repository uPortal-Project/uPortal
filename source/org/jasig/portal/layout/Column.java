/**
 * Column.java	Java 1.2.2 Mon Mar 06 10:59:19 PST 2000
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
import com.objectspace.xml.IClassDeclaration;
import java.util.Enumeration;
import java.util.Vector;
import com.objectspace.xml.xgen.ClassDecl;

public class Column implements IColumn
  {
  public Hashtable _Attributes = new Hashtable();
  public Vector _Channel = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.layout.Column" );
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
  
  public String getWidthAttribute()
    {
    return getAttribute( "width" );
    }
  
  public void setWidthAttribute( String value )
    {
    setAttribute( "width", value );
    }
  
  public String removeWidthAttribute()
    {
    return removeAttribute( "width" );
    }

  // element Channel
  
  public void addChannel( IChannel arg0  )
    {
    if( _Channel != null )
      _Channel.addElement( arg0 );
    }
  
  public int getChannelCount()
    {
    return _Channel == null ? 0 : _Channel.size();
    }
  
  public void setChannels( Vector arg0 )
    {
    if( arg0 == null )
      {
      _Channel = null;
      return;
      }

    _Channel = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _Channel.addElement( string );
      }
    }
  
  public IChannel[] getChannels()
    {
    if( _Channel == null )
      return null;

    IChannel[] array = new IChannel[ _Channel.size() ];
    _Channel.copyInto( array );

    return array;
    }
  
  public void setChannels( IChannel[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _Channel = v ;
    }
  
  public Enumeration getChannelElements()
    {
    return _Channel == null ? null : _Channel.elements();
    }
  
  public IChannel getChannelAt( int arg0 )
    {
    return _Channel == null ? null :  (IChannel) _Channel.elementAt( arg0 );
    }
  
  public void insertChannelAt( IChannel arg0, int arg1 )
    {
    if( _Channel != null )
      _Channel.insertElementAt( arg0, arg1 );
    }
  
  public void setChannelAt( IChannel arg0, int arg1 )
    {
    if( _Channel != null )
      _Channel.setElementAt( arg0, arg1 );
    }
  
  public boolean removeChannel( IChannel arg0 )
    {
    return _Channel == null ? false : _Channel.removeElement( arg0 );
    }
  
  public void removeChannelAt( int arg0 )
    {
    if( _Channel == null )
      return;

    _Channel.removeElementAt( arg0 );
    }
  
  public void removeAllChannels()
    {
    if( _Channel == null )
      return;

    _Channel.removeAllElements();
    }
  }