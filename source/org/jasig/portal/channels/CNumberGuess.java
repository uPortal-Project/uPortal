/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.channels;

import java.io.StringWriter;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import java.lang.String;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;

/** <p>A number guessing game which asks the user to enter a number within
 * a certain range as determined by this channel's parameters.</p>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CNumberGuess implements IChannel
{
    private static final Log log = LogFactory.getLog(CNumberGuess.class);
  ChannelStaticData staticData = null;
  ChannelRuntimeData runtimeData = null;

  private static final String sslLocation = "CNumberGuess/CNumberGuess.ssl";
  private static final String bundleLocation = "/org/jasig/portal/channels/CNumberGuess/CNumberGuess";
  private int iMinNum = 0;
  private int iMaxNum = 0;
  private int iGuess = 0;
  private int iGuesses = 0;
  private int iAnswer = 0;
  private boolean bFirstTime = true;

  /** Constructs a CNumberGuess.
   */
  public CNumberGuess ()
  {
    this.staticData = new ChannelStaticData ();
    this.runtimeData = new ChannelRuntimeData ();
  }

  /** Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    // Channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }

  /** Processes layout-level events coming from the portal
   * @param ev a portal layout event
   */
  public void receiveEvent (PortalEvent ev)
  {
    // no events for this channel
  }

  /** Receive static channel data from the portal
   * @param sd static channel data
   */
  public void setStaticData (ChannelStaticData sd)
  {
    this.staticData = sd;

    String sMinNum = null;
    String sMaxNum = null;

    try
    {
      if ((sMinNum = sd.getParameter ("minNum")) != null )
        iMinNum = Integer.parseInt (sMinNum);

      if ((sMaxNum = sd.getParameter ("maxNum")) != null)
        iMaxNum = Integer.parseInt (sMaxNum);

      iAnswer = getRandomNumber (iMinNum, iMaxNum);
    }
    catch (NumberFormatException nfe)
    {
      iMinNum = 0;
      iMaxNum = 100;

      log.warn("CNumberGuess::setStaticData() : either " + sMinNum + " or " + sMaxNum + " (minNum, maxNum) is not a valid integer. Defaults " + iMinNum + " and " + iMaxNum + " will be used instead.");
    }
   }


  /** Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;

    String sGuess = runtimeData.getParameter ("guess");

    if (sGuess != null)
    {
      try
      {
        iGuess = Integer.parseInt (sGuess);
      }
      catch (NumberFormatException nfe)
      {
        // Assume that the guess was the same as last time
      }

      bFirstTime = false;
      iGuesses++;
    }
  }

  /** Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (ContentHandler out) throws PortalException
  {
    String sSuggest = null;

    ResourceBundle l10n = ResourceBundle.getBundle(bundleLocation,runtimeData.getLocales()[0]);

    if (iGuess < iAnswer)
        sSuggest = l10n.getString("HIGHER");
    else if (iGuess > iAnswer)
        sSuggest = l10n.getString("LOWER");

    String GUESS_SUGGEST = MessageFormat.format(l10n.getString("GUESS_SUGGEST"), new  String[] {sSuggest});
    String THE_ANSWER_WAS_X = MessageFormat.format(l10n.getString("THE_ANSWER_WAS_X"), new  String[] {String.valueOf(iAnswer)});
    String YOU_GOT_IT_AFTER_X_TRIES = MessageFormat.format(l10n.getString("YOU_GOT_IT_AFTER_X_TRIES"), new String[] {String.valueOf(iGuesses)});
    String YOU_HAVE_MADE_X_GUESSES = MessageFormat.format(l10n.getString("YOU_HAVE_MADE_X_GUESSES"), new String[] {String.valueOf(iGuesses)});
    String YOUR_GUESS_OF_GUESS_WAS_INCORRECT = MessageFormat.format(l10n.getString("YOUR_GUESS_OF_GUESS_WAS_INCORRECT"), new String[] {String.valueOf(iGuess)});
    String I_AM_THINKING_OF_A_NUMBER_BETWEEN_X_AND_Y = MessageFormat.format(l10n.getString("I_AM_THINKING_OF_A_NUMBER_BETWEEN_X_AND_Y"), new String[] {String.valueOf(iMinNum), String.valueOf(iMaxNum)});

    StringWriter w = new StringWriter ();
    w.write ("<?xml version='1.0'?>\n");
    w.write ("<content>\n");
    w.write ("  <minNum>" + iMinNum + "</minNum>\n");
    w.write ("  <maxNum>" + iMaxNum + "</maxNum>\n");
    w.write ("  <guesses>" + iGuesses + "</guesses>\n");
    w.write ("  <guess>" + iGuess + "</guess>\n");

    if (bFirstTime)
      ; // Do nothing
    else if (iGuess == iAnswer)
    {
      w.write ("  <answer>" + iAnswer + "</answer>\n");
      bFirstTime = true;
      iGuesses = 0;
      iAnswer = getRandomNumber (iMinNum, iMaxNum);
    }
    else
      w.write ("  <suggest>" + sSuggest + "</suggest>\n");

    w.write ("</content>\n");

    XSLT xslt = XSLT.getTransformer(this);
    xslt.setResourceBundle(l10n);
    xslt.setXML(w.toString());
    xslt.setXSL(sslLocation, "main", runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    xslt.setStylesheetParameter("guessSuggest", GUESS_SUGGEST);    
    xslt.setStylesheetParameter("theAnswerWasX", THE_ANSWER_WAS_X);    
    xslt.setStylesheetParameter("youHaveMadeXGuesses", YOU_HAVE_MADE_X_GUESSES);    
    xslt.setStylesheetParameter("youGotItAfterXTries", YOU_GOT_IT_AFTER_X_TRIES);
    xslt.setStylesheetParameter("YourGuessOfGuessWasIncorrect", YOUR_GUESS_OF_GUESS_WAS_INCORRECT);    
    xslt.setStylesheetParameter("IAmThinkingOfANumberBetweenXAndY", I_AM_THINKING_OF_A_NUMBER_BETWEEN_X_AND_Y);    
    xslt.transform();
  }

  private int getRandomNumber (int min, int max)
  {
    return new Double ((max - min) * Math.random () + min).intValue ();
  }
}
