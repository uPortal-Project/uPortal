/**
 * Factory.java	Java 1.2.2 Mon Mar 06 11:01:13 PST 2000
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

public class Factory
  {
  public static ITitleOrDescriptionOrNameOrLink newTitleOrDescriptionOrNameOrLink()
    {
    return new org.jasig.portal.channels.rss.TitleOrDescriptionOrNameOrLink();
    }

  public static IChannel newChannel()
    {
    return new org.jasig.portal.channels.rss.Channel();
    }

  public static IItem newItem()
    {
    return new org.jasig.portal.channels.rss.Item();
    }

  public static ITitleOrLinkOrDescription newTitleOrLinkOrDescription()
    {
    return new org.jasig.portal.channels.rss.TitleOrLinkOrDescription();
    }

  public static IRss newRss()
    {
    return new org.jasig.portal.channels.rss.Rss();
    }

  public static IImage newImage()
    {
    return new org.jasig.portal.channels.rss.Image();
    }

  public static ISkipHours newSkipHours()
    {
    return new org.jasig.portal.channels.rss.SkipHours();
    }

  public static ITitleOrUrlOrLinkOrWidthOrHeightOrDescription newTitleOrUrlOrLinkOrWidthOrHeightOrDescription()
    {
    return new org.jasig.portal.channels.rss.TitleOrUrlOrLinkOrWidthOrHeightOrDescription();
    }

  public static ITextinput newTextinput()
    {
    return new org.jasig.portal.channels.rss.Textinput();
    }

  public static ITitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays newTitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays()
    {
    return new org.jasig.portal.channels.rss.TitleOrDescriptionOrLinkOrLanguageOrOneOrMoreItemOrRatingOrImageOrTextinputOrCopyrightOrPubDateOrLastBuildDateOrDocsOrManagingEditorOrWebMasterOrSkipHoursOrSkipDays();
    }

  public static ISkipDays newSkipDays()
    {
    return new org.jasig.portal.channels.rss.SkipDays();
    }

  }