/**
 * Image.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public class Image implements IImage
  {
  public Vector _TitleOrUrlOrLinkOrWidthOrHeightOrDescription = new Vector();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.Image" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element TitleOrUrlOrLinkOrWidthOrHeightOrDescription
  
  public void addTitleOrUrlOrLinkOrWidthOrHeightOrDescription( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription arg0  )
    {
    if( _TitleOrUrlOrLinkOrWidthOrHeightOrDescription != null )
      _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.addElement( arg0 );
    }
  
  public int getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionCount()
    {
    return _TitleOrUrlOrLinkOrWidthOrHeightOrDescription == null ? 0 : _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.size();
    }
  
  public void setTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions( Vector arg0 )
    {
    if( arg0 == null )
      {
      _TitleOrUrlOrLinkOrWidthOrHeightOrDescription = null;
      return;
      }

    _TitleOrUrlOrLinkOrWidthOrHeightOrDescription = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.addElement( string );
      }
    }
  
  public ITitleOrUrlOrLinkOrWidthOrHeightOrDescription[] getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions()
    {
    if( _TitleOrUrlOrLinkOrWidthOrHeightOrDescription == null )
      return null;

    ITitleOrUrlOrLinkOrWidthOrHeightOrDescription[] array = new ITitleOrUrlOrLinkOrWidthOrHeightOrDescription[ _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.size() ];
    _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.copyInto( array );

    return array;
    }
  
  public void setTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _TitleOrUrlOrLinkOrWidthOrHeightOrDescription = v ;
    }
  
  public Enumeration getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionElements()
    {
    return _TitleOrUrlOrLinkOrWidthOrHeightOrDescription == null ? null : _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.elements();
    }
  
  public ITitleOrUrlOrLinkOrWidthOrHeightOrDescription getTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionAt( int arg0 )
    {
    return _TitleOrUrlOrLinkOrWidthOrHeightOrDescription == null ? null :  (ITitleOrUrlOrLinkOrWidthOrHeightOrDescription) _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.elementAt( arg0 );
    }
  
  public void insertTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionAt( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription arg0, int arg1 )
    {
    if( _TitleOrUrlOrLinkOrWidthOrHeightOrDescription != null )
      _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.insertElementAt( arg0, arg1 );
    }
  
  public void setTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionAt( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription arg0, int arg1 )
    {
    if( _TitleOrUrlOrLinkOrWidthOrHeightOrDescription != null )
      _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.setElementAt( arg0, arg1 );
    }
  
  public boolean removeTitleOrUrlOrLinkOrWidthOrHeightOrDescription( ITitleOrUrlOrLinkOrWidthOrHeightOrDescription arg0 )
    {
    return _TitleOrUrlOrLinkOrWidthOrHeightOrDescription == null ? false : _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.removeElement( arg0 );
    }
  
  public void removeTitleOrUrlOrLinkOrWidthOrHeightOrDescriptionAt( int arg0 )
    {
    if( _TitleOrUrlOrLinkOrWidthOrHeightOrDescription == null )
      return;

    _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.removeElementAt( arg0 );
    }
  
  public void removeAllTitleOrUrlOrLinkOrWidthOrHeightOrDescriptions()
    {
    if( _TitleOrUrlOrLinkOrWidthOrHeightOrDescription == null )
      return;

    _TitleOrUrlOrLinkOrWidthOrHeightOrDescription.removeAllElements();
    }
  }