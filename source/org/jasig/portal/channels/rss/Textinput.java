/**
 * Textinput.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

import com.objectspace.xml.IClassDeclaration;
import java.util.Enumeration;
import java.util.Vector;
import com.objectspace.xml.xgen.ClassDecl;

public class Textinput implements ITextinput
  {
  public Vector _TitleOrDescriptionOrNameOrLink = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.Textinput" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element TitleOrDescriptionOrNameOrLink
  
  public void addTitleOrDescriptionOrNameOrLink( ITitleOrDescriptionOrNameOrLink arg0  )
    {
    if( _TitleOrDescriptionOrNameOrLink != null )
      _TitleOrDescriptionOrNameOrLink.addElement( arg0 );
    }
  
  public int getTitleOrDescriptionOrNameOrLinkCount()
    {
    return _TitleOrDescriptionOrNameOrLink == null ? 0 : _TitleOrDescriptionOrNameOrLink.size();
    }
  
  public void setTitleOrDescriptionOrNameOrLinks( Vector arg0 )
    {
    if( arg0 == null )
      {
      _TitleOrDescriptionOrNameOrLink = null;
      return;
      }

    _TitleOrDescriptionOrNameOrLink = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _TitleOrDescriptionOrNameOrLink.addElement( string );
      }
    }
  
  public ITitleOrDescriptionOrNameOrLink[] getTitleOrDescriptionOrNameOrLinks()
    {
    if( _TitleOrDescriptionOrNameOrLink == null )
      return null;

    ITitleOrDescriptionOrNameOrLink[] array = new ITitleOrDescriptionOrNameOrLink[ _TitleOrDescriptionOrNameOrLink.size() ];
    _TitleOrDescriptionOrNameOrLink.copyInto( array );

    return array;
    }
  
  public void setTitleOrDescriptionOrNameOrLinks( ITitleOrDescriptionOrNameOrLink[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _TitleOrDescriptionOrNameOrLink = v ;
    }
  
  public Enumeration getTitleOrDescriptionOrNameOrLinkElements()
    {
    return _TitleOrDescriptionOrNameOrLink == null ? null : _TitleOrDescriptionOrNameOrLink.elements();
    }
  
  public ITitleOrDescriptionOrNameOrLink getTitleOrDescriptionOrNameOrLinkAt( int arg0 )
    {
    return _TitleOrDescriptionOrNameOrLink == null ? null :  (ITitleOrDescriptionOrNameOrLink) _TitleOrDescriptionOrNameOrLink.elementAt( arg0 );
    }
  
  public void insertTitleOrDescriptionOrNameOrLinkAt( ITitleOrDescriptionOrNameOrLink arg0, int arg1 )
    {
    if( _TitleOrDescriptionOrNameOrLink != null )
      _TitleOrDescriptionOrNameOrLink.insertElementAt( arg0, arg1 );
    }
  
  public void setTitleOrDescriptionOrNameOrLinkAt( ITitleOrDescriptionOrNameOrLink arg0, int arg1 )
    {
    if( _TitleOrDescriptionOrNameOrLink != null )
      _TitleOrDescriptionOrNameOrLink.setElementAt( arg0, arg1 );
    }
  
  public boolean removeTitleOrDescriptionOrNameOrLink( ITitleOrDescriptionOrNameOrLink arg0 )
    {
    return _TitleOrDescriptionOrNameOrLink == null ? false : _TitleOrDescriptionOrNameOrLink.removeElement( arg0 );
    }
  
  public void removeTitleOrDescriptionOrNameOrLinkAt( int arg0 )
    {
    if( _TitleOrDescriptionOrNameOrLink == null )
      return;

    _TitleOrDescriptionOrNameOrLink.removeElementAt( arg0 );
    }
  
  public void removeAllTitleOrDescriptionOrNameOrLinks()
    {
    if( _TitleOrDescriptionOrNameOrLink == null )
      return;

    _TitleOrDescriptionOrNameOrLink.removeAllElements();
    }
  }