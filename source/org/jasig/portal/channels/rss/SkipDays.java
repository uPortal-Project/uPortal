/**
 * SkipDays.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public class SkipDays implements ISkipDays
  {
  public Vector _Day = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.SkipDays" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Day
  
  public void addDay( String arg0  )
    {
    if( _Day != null )
      _Day.addElement( arg0 == null ? null : new StringWrapper( arg0 ) );
    }
  
  public int getDayCount()
    {
    return _Day == null ? 0 : _Day.size();
    }
  
  public void setDays( Vector arg0 )
    {
    if( arg0 == null )
      {
      _Day = null;
      return;
      }

    _Day = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _Day.addElement( string == null ? null : new StringWrapper( string ) );
      }
    }
  
  public String[] getDays()
    {
    if( _Day == null )
      return null;

    String[] array = new String[ _Day.size() ];
    int i = 0;

    for( Enumeration e = _Day.elements(); e.hasMoreElements(); i++ )
      array[ i ] = ((StringWrapper) e.nextElement()).getRecursiveValue();

    return array;
    }
  
  public void setDays( String[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] == null ? null : new StringWrapper( arg0[ i ] ) );
      }

    _Day = v ;
    }
  
  public Enumeration getDayElements()
    {
    if( _Day == null )
      return null;

    Vector v = new Vector();

    for( Enumeration e = _Day.elements(); e.hasMoreElements(); )
      v.addElement( ((StringWrapper) e.nextElement()).getRecursiveValue() );

    return v.elements();
    }
  
  public String getDayAt( int arg0 )
    {
    return _Day == null ? null :  ((StringWrapper) _Day.elementAt( arg0 )).getRecursiveValue();
    }
  
  public void insertDayAt( String arg0, int arg1 )
    {
    if( _Day != null )
      _Day.insertElementAt( arg0 == null ? null : new StringWrapper( arg0 ), arg1 );
    }
  
  public void setDayAt( String arg0, int arg1 )
    {
    if( _Day != null )
      _Day.setElementAt( arg0 == null ? null : new StringWrapper( arg0 ), arg1 );
    }
  
  public boolean removeDay( String arg0 )
    {
    if( _Day == null )
      return false;

    int i = 0;

    for( Enumeration e = _Day.elements(); e.hasMoreElements(); i++ )
      if( ((StringWrapper) e.nextElement()).getRecursiveValue().equals( arg0 ) )
        {
        _Day.removeElementAt( i );
        return true;
        }

    return false;
    }
  
  public void removeDayAt( int arg0 )
    {
    if( _Day == null )
      return;

    Vector v = (Vector) _Day.clone();
    v.removeElementAt( arg0 );


    _Day.removeElementAt( arg0 );
    }
  }