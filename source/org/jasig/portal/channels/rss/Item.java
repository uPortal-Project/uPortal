/**
 * Item.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public class Item implements IItem
  {
  public Vector _TitleOrLinkOrDescription = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.Item" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element TitleOrLinkOrDescription
  
  public void addTitleOrLinkOrDescription( ITitleOrLinkOrDescription arg0  )
    {
    if( _TitleOrLinkOrDescription != null )
      _TitleOrLinkOrDescription.addElement( arg0 );
    }
  
  public int getTitleOrLinkOrDescriptionCount()
    {
    return _TitleOrLinkOrDescription == null ? 0 : _TitleOrLinkOrDescription.size();
    }
  
  public void setTitleOrLinkOrDescriptions( Vector arg0 )
    {
    if( arg0 == null )
      {
      _TitleOrLinkOrDescription = null;
      return;
      }

    _TitleOrLinkOrDescription = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _TitleOrLinkOrDescription.addElement( string );
      }
    }
  
  public ITitleOrLinkOrDescription[] getTitleOrLinkOrDescriptions()
    {
    if( _TitleOrLinkOrDescription == null )
      return null;

    ITitleOrLinkOrDescription[] array = new ITitleOrLinkOrDescription[ _TitleOrLinkOrDescription.size() ];
    _TitleOrLinkOrDescription.copyInto( array );

    return array;
    }
  
  public void setTitleOrLinkOrDescriptions( ITitleOrLinkOrDescription[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _TitleOrLinkOrDescription = v ;
    }
  
  public Enumeration getTitleOrLinkOrDescriptionElements()
    {
    return _TitleOrLinkOrDescription == null ? null : _TitleOrLinkOrDescription.elements();
    }
  
  public ITitleOrLinkOrDescription getTitleOrLinkOrDescriptionAt( int arg0 )
    {
    return _TitleOrLinkOrDescription == null ? null :  (ITitleOrLinkOrDescription) _TitleOrLinkOrDescription.elementAt( arg0 );
    }
  
  public void insertTitleOrLinkOrDescriptionAt( ITitleOrLinkOrDescription arg0, int arg1 )
    {
    if( _TitleOrLinkOrDescription != null )
      _TitleOrLinkOrDescription.insertElementAt( arg0, arg1 );
    }
  
  public void setTitleOrLinkOrDescriptionAt( ITitleOrLinkOrDescription arg0, int arg1 )
    {
    if( _TitleOrLinkOrDescription != null )
      _TitleOrLinkOrDescription.setElementAt( arg0, arg1 );
    }
  
  public boolean removeTitleOrLinkOrDescription( ITitleOrLinkOrDescription arg0 )
    {
    return _TitleOrLinkOrDescription == null ? false : _TitleOrLinkOrDescription.removeElement( arg0 );
    }
  
  public void removeTitleOrLinkOrDescriptionAt( int arg0 )
    {
    if( _TitleOrLinkOrDescription == null )
      return;

    _TitleOrLinkOrDescription.removeElementAt( arg0 );
    }
  
  public void removeAllTitleOrLinkOrDescriptions()
    {
    if( _TitleOrLinkOrDescription == null )
      return;

    _TitleOrLinkOrDescription.removeAllElements();
    }
  }