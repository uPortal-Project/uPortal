/**
 * SkipHours.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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
import java.util.Enumeration;
import java.util.Vector;
import com.objectspace.xml.xgen.ClassDecl;

public class SkipHours implements ISkipHours
  {
  public Vector _Hour = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.SkipHours" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Hour
  
  public void addHour( String arg0  )
    {
    if( _Hour != null )
      _Hour.addElement( arg0 == null ? null : new StringWrapper( arg0 ) );
    }
  
  public int getHourCount()
    {
    return _Hour == null ? 0 : _Hour.size();
    }
  
  public void setHours( Vector arg0 )
    {
    if( arg0 == null )
      {
      _Hour = null;
      return;
      }

    _Hour = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _Hour.addElement( string == null ? null : new StringWrapper( string ) );
      }
    }
  
  public String[] getHours()
    {
    if( _Hour == null )
      return null;

    String[] array = new String[ _Hour.size() ];
    int i = 0;

    for( Enumeration e = _Hour.elements(); e.hasMoreElements(); i++ )
      array[ i ] = ((StringWrapper) e.nextElement()).getRecursiveValue();

    return array;
    }
  
  public void setHours( String[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] == null ? null : new StringWrapper( arg0[ i ] ) );
      }

    _Hour = v ;
    }
  
  public Enumeration getHourElements()
    {
    if( _Hour == null )
      return null;

    Vector v = new Vector();

    for( Enumeration e = _Hour.elements(); e.hasMoreElements(); )
      v.addElement( ((StringWrapper) e.nextElement()).getRecursiveValue() );

    return v.elements();
    }
  
  public String getHourAt( int arg0 )
    {
    return _Hour == null ? null :  ((StringWrapper) _Hour.elementAt( arg0 )).getRecursiveValue();
    }
  
  public void insertHourAt( String arg0, int arg1 )
    {
    if( _Hour != null )
      _Hour.insertElementAt( arg0 == null ? null : new StringWrapper( arg0 ), arg1 );
    }
  
  public void setHourAt( String arg0, int arg1 )
    {
    if( _Hour != null )
      _Hour.setElementAt( arg0 == null ? null : new StringWrapper( arg0 ), arg1 );
    }
  
  public boolean removeHour( String arg0 )
    {
    if( _Hour == null )
      return false;

    int i = 0;

    for( Enumeration e = _Hour.elements(); e.hasMoreElements(); i++ )
      if( ((StringWrapper) e.nextElement()).getRecursiveValue().equals( arg0 ) )
        {
        _Hour.removeElementAt( i );
        return true;
        }

    return false;
    }
  
  public void removeHourAt( int arg0 )
    {
    if( _Hour == null )
      return;

    Vector v = (Vector) _Hour.clone();
    v.removeElementAt( arg0 );


    _Hour.removeElementAt( arg0 );
    }
  }