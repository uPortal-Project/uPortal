<?xml version="1.0"?>
<!--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="locale">en_US</xsl:param>
<xsl:param name="guessSuggest">default</xsl:param>
<xsl:param name="theAnswerWasX">default</xsl:param>
<xsl:param name="youHaveMadeXGuesses">default</xsl:param>
<xsl:param name="youGotItAfterXTries">default</xsl:param>
<xsl:param name="YourGuessOfGuessWasIncorrect">default</xsl:param>
<xsl:param name="IAmThinkingOfANumberBetweenXAndY">default</xsl:param>
<xsl:template match="content">
  <xsl:choose>
    <xsl:when test="suggest">
      <xsl:value-of select="$YourGuessOfGuessWasIncorrect"/>
      <xsl:value-of select="$TRY_AGAIN"/> -- <xsl:value-of select="$guessSuggest"/><br />
      <xsl:value-of select="$youHaveMadeXGuesses"/>
    </xsl:when>
    <xsl:when test="answer">
      <xsl:value-of select="$youGotItAfterXTries"/><br />
      <xsl:value-of select="$theAnswerWasX"/><br />
      <p><xsl:value-of select="$PLEASE_PLAY_AGAIN"/></p>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="$THIS_IS_A_NUMBER_GUESSING_GAME"/><br /></xsl:otherwise>
  </xsl:choose> 
  
  <xsl:value-of select="$IAmThinkingOfANumberBetweenXAndY"/><br />
  <xsl:value-of select="$WHATS_YOUR_GUESS"/>
    <form action="{$baseActionURL}" method="post">
      <input type="hidden" name="uP_root" value="me"/>
      <input type="text" name="guess" size="4" class="uportal-input-text"/>
      <input type="submit" value="{$SUBMIT}" class="uportal-button"/>
    </form>
</xsl:template>

</xsl:stylesheet> 
