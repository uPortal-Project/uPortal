/**
 * Tab.java	Java 1.3.0 Mon Jan 15 13:20:35 EST 2001
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

public class Tab implements ITab
  {
  public Hashtable _Attributes = new Hashtable();
  public Vector _Column = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.layout.Tab" );
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

    if( "removable".equals( name ) )
      return "true";

    if( "hidden".equals( name ) )
      return "false";

    return null;
    }
  
  public Hashtable getAttributes()
    {
    Hashtable clone = (Hashtable) _Attributes.clone();

    if( clone.get( "removable" ) == null )
      clone.put( "removable", "true" );

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
  
  public String getRemovableAttribute()
    {
    return getAttribute( "removable" );
    }
  
  public void setRemovableAttribute( String value )
    {
    setAttribute( "removable", value );
    }
  
  public String removeRemovableAttribute()
    {
    return removeAttribute( "removable" );
    }
  
  public String getNameAttribute()
    {
    return getAttribute( "name" );
    }
  
  public void setNameAttribute( String value )
    {
    setAttribute( "name", value );
    }
  
  public String removeNameAttribute()
    {
    return removeAttribute( "name" );
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

  // element Column
  
  public void addColumn( IColumn arg0  )
    {
    if( _Column != null )
      _Column.addElement( arg0 );
    }
  
  public int getColumnCount()
    {
    return _Column == null ? 0 : _Column.size();
    }
  
  public void setColumns( Vector arg0 )
    {
    if( arg0 == null )
      {
      _Column = null;
      return;
      }

    _Column = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _Column.addElement( string );
      }
    }
  
  public IColumn[] getColumns()
    {
    if( _Column == null )
      return null;

    IColumn[] array = new IColumn[ _Column.size() ];
    _Column.copyInto( array );

    return array;
    }
  
  public void setColumns( IColumn[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _Column = v ;
    }
  
  public Enumeration getColumnElements()
    {
    return _Column == null ? null : _Column.elements();
    }
  
  public IColumn getColumnAt( int arg0 )
    {
    return _Column == null ? null :  (IColumn) _Column.elementAt( arg0 );
    }
  
  public void insertColumnAt( IColumn arg0, int arg1 )
    {
    if( _Column != null )
      _Column.insertElementAt( arg0, arg1 );
    }
  
  public void setColumnAt( IColumn arg0, int arg1 )
    {
    if( _Column != null )
      _Column.setElementAt( arg0, arg1 );
    }
  
  public boolean removeColumn( IColumn arg0 )
    {
    if( _Column == null )
      return false;

    return  _Column.removeElement( arg0 );
    }
  
  public void removeColumnAt( int arg0 )
    {
    if( _Column == null )
      return;

    Vector v = (Vector) _Column.clone();
    v.removeElementAt( arg0 );


    _Column.removeElementAt( arg0 );
    }
  }