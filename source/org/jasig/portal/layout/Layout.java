/**
 * Layout.java	Java 1.3.0 Mon Dec 11 17:52:48 EST 2000
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

public class Layout implements ILayout
  {
  public Hashtable _Attributes = new Hashtable();
  public Vector _Tab = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.layout.Layout" );
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
  
  public String getActiveTabAttribute()
    {
    return getAttribute( "activeTab" );
    }
  
  public void setActiveTabAttribute( String value )
    {
    setAttribute( "activeTab", value );
    }
  
  public String removeActiveTabAttribute()
    {
    return removeAttribute( "activeTab" );
    }
  
  public String getBgcolorAttribute()
    {
    return getAttribute( "bgcolor" );
    }
  
  public void setBgcolorAttribute( String value )
    {
    setAttribute( "bgcolor", value );
    }
  
  public String removeBgcolorAttribute()
    {
    return removeAttribute( "bgcolor" );
    }
  
  public String getActiveTabColorAttribute()
    {
    return getAttribute( "activeTabColor" );
    }
  
  public void setActiveTabColorAttribute( String value )
    {
    setAttribute( "activeTabColor", value );
    }
  
  public String removeActiveTabColorAttribute()
    {
    return removeAttribute( "activeTabColor" );
    }
  
  public String getFgcolorAttribute()
    {
    return getAttribute( "fgcolor" );
    }
  
  public void setFgcolorAttribute( String value )
    {
    setAttribute( "fgcolor", value );
    }
  
  public String removeFgcolorAttribute()
    {
    return removeAttribute( "fgcolor" );
    }
  
  public String getChannelHeadingColorAttribute()
    {
    return getAttribute( "channelHeadingColor" );
    }
  
  public void setChannelHeadingColorAttribute( String value )
    {
    setAttribute( "channelHeadingColor", value );
    }
  
  public String removeChannelHeadingColorAttribute()
    {
    return removeAttribute( "channelHeadingColor" );
    }
  
  public String getTabColorAttribute()
    {
    return getAttribute( "tabColor" );
    }
  
  public void setTabColorAttribute( String value )
    {
    setAttribute( "tabColor", value );
    }
  
  public String removeTabColorAttribute()
    {
    return removeAttribute( "tabColor" );
    }

  // element Tab
  
  public void addTab( ITab arg0  )
    {
    if( _Tab != null )
      _Tab.addElement( arg0 );
    }
  
  public int getTabCount()
    {
    return _Tab == null ? 0 : _Tab.size();
    }
  
  public void setTabs( Vector arg0 )
    {
    if( arg0 == null )
      {
      _Tab = null;
      return;
      }

    _Tab = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _Tab.addElement( string );
      }
    }
  
  public ITab[] getTabs()
    {
    if( _Tab == null )
      return null;

    ITab[] array = new ITab[ _Tab.size() ];
    _Tab.copyInto( array );

    return array;
    }
  
  public void setTabs( ITab[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _Tab = v ;
    }
  
  public Enumeration getTabElements()
    {
    return _Tab == null ? null : _Tab.elements();
    }
  
  public ITab getTabAt( int arg0 )
    {
    return _Tab == null ? null :  (ITab) _Tab.elementAt( arg0 );
    }
  
  public void insertTabAt( ITab arg0, int arg1 )
    {
    if( _Tab != null )
      _Tab.insertElementAt( arg0, arg1 );
    }
  
  public void setTabAt( ITab arg0, int arg1 )
    {
    if( _Tab != null )
      _Tab.setElementAt( arg0, arg1 );
    }
  
  public boolean removeTab( ITab arg0 )
    {
    if( _Tab == null )
      return false;

    return  _Tab.removeElement( arg0 );
    }
  
  public void removeTabAt( int arg0 )
    {
    if( _Tab == null )
      return;

    Vector v = (Vector) _Tab.clone();
    v.removeElementAt( arg0 );


    _Tab.removeElementAt( arg0 );
    }
  }