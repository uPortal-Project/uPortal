/**
 * Channel.java	Java 1.3.0 Mon Dec 11 17:52:47 EST 2000
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
import com.objectspace.xml.xgen.ClassDecl;
import com.objectspace.xml.IClassDeclaration;

public class Channel implements IChannel
  {
  public Hashtable _Attributes = new Hashtable();
  public Vector _Parameter = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.layout.Channel" );
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

    if( "minimized".equals( name ) )
      return "true";

    if( "hidden".equals( name ) )
      return "false";

    return null;
    }
  
  public Hashtable getAttributes()
    {
    Hashtable clone = (Hashtable) _Attributes.clone();

    if( clone.get( "minimized" ) == null )
      clone.put( "minimized", "true" );

    if( clone.get( "hidden" ) == null )
      clone.put( "hidden", "false" );

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
  
  public String getMinimizedAttribute()
    {
    return getAttribute( "minimized" );
    }
  
  public void setMinimizedAttribute( String value )
    {
    setAttribute( "minimized", value );
    }
  
  public String removeMinimizedAttribute()
    {
    return removeAttribute( "minimized" );
    }
  
  public String getGlobalChannelIDAttribute()
    {
    return getAttribute( "globalChannelID" );
    }
  
  public void setGlobalChannelIDAttribute( String value )
    {
    setAttribute( "globalChannelID", value );
    }
  
  public String removeGlobalChannelIDAttribute()
    {
    return removeAttribute( "globalChannelID" );
    }
  
  public String getInstanceIDAttribute()
    {
    return getAttribute( "instanceID" );
    }
  
  public void setInstanceIDAttribute( String value )
    {
    setAttribute( "instanceID", value );
    }
  
  public String removeInstanceIDAttribute()
    {
    return removeAttribute( "instanceID" );
    }
  
  public String getHiddenAttribute()
    {
    return getAttribute( "hidden" );
    }
  
  public void setHiddenAttribute( String value )
    {
    setAttribute( "hidden", value );
    }
  
  public String removeHiddenAttribute()
    {
    return removeAttribute( "hidden" );
    }
  
  public String getClassAttribute()
    {
    return getAttribute( "class" );
    }
  
  public void setClassAttribute( String value )
    {
    setAttribute( "class", value );
    }
  
  public String removeClassAttribute()
    {
    return removeAttribute( "class" );
    }

  // element Parameter
  
  public void addParameter( IParameter arg0  )
    {
    if( _Parameter != null )
      _Parameter.addElement( arg0 );
    }
  
  public int getParameterCount()
    {
    return _Parameter == null ? 0 : _Parameter.size();
    }
  
  public void setParameters( Vector arg0 )
    {
    if( arg0 == null )
      {
      _Parameter = null;
      return;
      }

    _Parameter = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _Parameter.addElement( string );
      }
    }
  
  public IParameter[] getParameters()
    {
    if( _Parameter == null )
      return null;

    IParameter[] array = new IParameter[ _Parameter.size() ];
    _Parameter.copyInto( array );

    return array;
    }
  
  public void setParameters( IParameter[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _Parameter = v ;
    }
  
  public Enumeration getParameterElements()
    {
    return _Parameter == null ? null : _Parameter.elements();
    }
  
  public IParameter getParameterAt( int arg0 )
    {
    return _Parameter == null ? null :  (IParameter) _Parameter.elementAt( arg0 );
    }
  
  public void insertParameterAt( IParameter arg0, int arg1 )
    {
    if( _Parameter != null )
      _Parameter.insertElementAt( arg0, arg1 );
    }
  
  public void setParameterAt( IParameter arg0, int arg1 )
    {
    if( _Parameter != null )
      _Parameter.setElementAt( arg0, arg1 );
    }
  
  public boolean removeParameter( IParameter arg0 )
    {
    return _Parameter == null ? false : _Parameter.removeElement( arg0 );
    }
  
  public void removeParameterAt( int arg0 )
    {
    if( _Parameter == null )
      return;

    _Parameter.removeElementAt( arg0 );
    }
  
  public void removeAllParameters()
    {
    if( _Parameter == null )
      return;

    _Parameter.removeAllElements();
    }
  }