/**
 * ITitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays.java	Java 1.2.2 Mon Mar 06 11:01:14 PST 2000
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

import java.util.Enumeration;
import java.util.Vector;

public interface ITitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays extends com.objectspace.xml.IDXMLInterface
  {

  // element Title
  public String getTitle();
  public void setTitle( String arg0 );

  // element Description
  public String getDescription();
  public void setDescription( String arg0 );

  // element Link
  public String getLink();
  public void setLink( String arg0 );

  // element Language
  public String getLanguage();
  public void setLanguage( String arg0 );

  // element Item
  public void addItem( IItem arg0  );
  public int getItemCount();
  public void setItems( Vector arg0 );
  public IItem[] getItems();
  public void setItems( IItem[] arg0 );
  public Enumeration getItemElements();
  public IItem getItemAt( int arg0 );
  public void insertItemAt( IItem arg0, int arg1 );
  public void setItemAt( IItem arg0, int arg1 );
  public boolean removeItem( IItem arg0 );
  public void removeItemAt( int arg0 );

  // element Rating
  public String getRating();
  public void setRating( String arg0 );
  public void removeRating();

  // element Image
  public IImage getImage();
  public void setImage( IImage arg0 );
  public void removeImage();

  // element Textinput
  public ITextinput getTextinput();
  public void setTextinput( ITextinput arg0 );
  public void removeTextinput();

  // element Copyright
  public String getCopyright();
  public void setCopyright( String arg0 );
  public void removeCopyright();

  // element PubDate
  public String getPubDate();
  public void setPubDate( String arg0 );
  public void removePubDate();

  // element LastBuildDate
  public String getLastBuildDate();
  public void setLastBuildDate( String arg0 );
  public void removeLastBuildDate();

  // element Docs
  public String getDocs();
  public void setDocs( String arg0 );
  public void removeDocs();

  // element ManagingEditor
  public String getManagingEditor();
  public void setManagingEditor( String arg0 );
  public void removeManagingEditor();

  // element WebMaster
  public String getWebMaster();
  public void setWebMaster( String arg0 );
  public void removeWebMaster();

  // element SkipHours
  public ISkipHours getSkipHours();
  public void setSkipHours( ISkipHours arg0 );
  public void removeSkipHours();

  // element SkipDays
  public ISkipDays getSkipDays();
  public void setSkipDays( ISkipDays arg0 );
  public void removeSkipDays();
  }