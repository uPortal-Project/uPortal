/**
 * TitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

public class TitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays implements ITitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays
  {
  public StringWrapper _Title = null;
  public StringWrapper _Description = null;
  public StringWrapper _Link = null;
  public StringWrapper _Language = null;
  public Vector _Item = null;
  public StringWrapper _Rating = null;
  public IImage _Image = null;
  public ITextinput _Textinput = null;
  public StringWrapper _Copyright = null;
  public StringWrapper _PubDate = null;
  public StringWrapper _LastBuildDate = null;
  public StringWrapper _Docs = null;
  public StringWrapper _ManagingEditor = null;
  public StringWrapper _WebMaster = null;
  public ISkipHours _SkipHours = null;
  public ISkipDays _SkipDays = null;
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.jasig.portal.channels.rss.TitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Title
  
  public String getTitle()
    {
    return _Title == null ? null : _Title.getRecursiveValue();
    }
  
  public void setTitle( String arg0 )
    {
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Title = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Description
  
  public String getDescription()
    {
    return _Description == null ? null : _Description.getRecursiveValue();
    }
  
  public void setDescription( String arg0 )
    {
    _Title = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Description = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Link
  
  public String getLink()
    {
    return _Link == null ? null : _Link.getRecursiveValue();
    }
  
  public void setLink( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Link = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Language
  
  public String getLanguage()
    {
    return _Language == null ? null : _Language.getRecursiveValue();
    }
  
  public void setLanguage( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Language = arg0 == null ? null : new StringWrapper( arg0 );
    }

  // element Item
  
  public void addItem( IItem arg0  )
    {
    if( _Item != null )
      _Item.addElement( arg0 );
    }
  
  public int getItemCount()
    {
    return _Item == null ? 0 : _Item.size();
    }
  
  public void setItems( Vector arg0 )
    {
    if( arg0 == null )
      {
      _Item = null;
      return;
      }

    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Item = new Vector();

    for( Enumeration e = arg0.elements(); e.hasMoreElements(); )
      {
      String string = (String) e.nextElement();
      _Item.addElement( string );
      }
    }
  
  public IItem[] getItems()
    {
    if( _Item == null )
      return null;

    IItem[] array = new IItem[ _Item.size() ];
    _Item.copyInto( array );

    return array;
    }
  
  public void setItems( IItem[] arg0 )
    {
    Vector v = arg0 == null ? null : new Vector();

    if( arg0 != null )
      {
      for( int i = 0; i < arg0.length; i++ )
        v.addElement( arg0[ i ] );
      }

    _Item = v ;
    }
  
  public Enumeration getItemElements()
    {
    return _Item == null ? null : _Item.elements();
    }
  
  public IItem getItemAt( int arg0 )
    {
    return _Item == null ? null :  (IItem) _Item.elementAt( arg0 );
    }
  
  public void insertItemAt( IItem arg0, int arg1 )
    {
    if( _Item != null )
      _Item.insertElementAt( arg0, arg1 );
    }
  
  public void setItemAt( IItem arg0, int arg1 )
    {
    if( _Item != null )
      _Item.setElementAt( arg0, arg1 );
    }
  
  public boolean removeItem( IItem arg0 )
    {
    if( _Item == null )
      return false;

    return  _Item.removeElement( arg0 );
    }
  
  public void removeItemAt( int arg0 )
    {
    if( _Item == null )
      return;

    Vector v = (Vector) _Item.clone();
    v.removeElementAt( arg0 );


    _Item.removeElementAt( arg0 );
    }

  // element Rating
  
  public String getRating()
    {
    return _Rating == null ? null : _Rating.getRecursiveValue();
    }
  
  public void setRating( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Rating = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeRating()
    {
    _Rating = null;
    }

  // element Image
  
  public IImage getImage()
    {
    return _Image;
    }
  
  public void setImage( IImage arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Image = arg0;
    }
  
  public void removeImage()
    {
    _Image = null;
    }

  // element Textinput
  
  public ITextinput getTextinput()
    {
    return _Textinput;
    }
  
  public void setTextinput( ITextinput arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Textinput = arg0;
    }
  
  public void removeTextinput()
    {
    _Textinput = null;
    }

  // element Copyright
  
  public String getCopyright()
    {
    return _Copyright == null ? null : _Copyright.getRecursiveValue();
    }
  
  public void setCopyright( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Copyright = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeCopyright()
    {
    _Copyright = null;
    }

  // element PubDate
  
  public String getPubDate()
    {
    return _PubDate == null ? null : _PubDate.getRecursiveValue();
    }
  
  public void setPubDate( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _PubDate = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removePubDate()
    {
    _PubDate = null;
    }

  // element LastBuildDate
  
  public String getLastBuildDate()
    {
    return _LastBuildDate == null ? null : _LastBuildDate.getRecursiveValue();
    }
  
  public void setLastBuildDate( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _LastBuildDate = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeLastBuildDate()
    {
    _LastBuildDate = null;
    }

  // element Docs
  
  public String getDocs()
    {
    return _Docs == null ? null : _Docs.getRecursiveValue();
    }
  
  public void setDocs( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _Docs = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeDocs()
    {
    _Docs = null;
    }

  // element ManagingEditor
  
  public String getManagingEditor()
    {
    return _ManagingEditor == null ? null : _ManagingEditor.getRecursiveValue();
    }
  
  public void setManagingEditor( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _WebMaster = null;
    _SkipHours = null;
    _SkipDays = null;

    _ManagingEditor = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeManagingEditor()
    {
    _ManagingEditor = null;
    }

  // element WebMaster
  
  public String getWebMaster()
    {
    return _WebMaster == null ? null : _WebMaster.getRecursiveValue();
    }
  
  public void setWebMaster( String arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _SkipHours = null;
    _SkipDays = null;

    _WebMaster = arg0 == null ? null : new StringWrapper( arg0 );
    }
  
  public void removeWebMaster()
    {
    _WebMaster = null;
    }

  // element SkipHours
  
  public ISkipHours getSkipHours()
    {
    return _SkipHours;
    }
  
  public void setSkipHours( ISkipHours arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipDays = null;

    _SkipHours = arg0;
    }
  
  public void removeSkipHours()
    {
    _SkipHours = null;
    }

  // element SkipDays
  
  public ISkipDays getSkipDays()
    {
    return _SkipDays;
    }
  
  public void setSkipDays( ISkipDays arg0 )
    {
    _Title = null;
    _Description = null;
    _Link = null;
    _Language = null;
    _Item = null;
    _Rating = null;
    _Image = null;
    _Textinput = null;
    _Copyright = null;
    _PubDate = null;
    _LastBuildDate = null;
    _Docs = null;
    _ManagingEditor = null;
    _WebMaster = null;
    _SkipHours = null;

    _SkipDays = arg0;
    }
  
  public void removeSkipDays()
    {
    _SkipDays = null;
    }
  }