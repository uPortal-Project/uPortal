<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>

<!--******************************************************************
     A fix for dispatching an IXMLChannel in uPortal 1.0: This section 
     of the stylesheet replaces the target jsp file with dispatch.jsp 
     and then appends the two parameters that dispatch.jsp needs:  
     'channelID' and 'method' name. When migrating to uPortal 2.0, this 
     section of the stylesheet can be removed.-->

<xsl:variable name="baseActionURL">
  <xsl:choose>
    <xsl:when test="not(starts-with($baseActionURL, 'dispatch.jsp'))">
      <xsl:value-of select="concat('dispatch.jsp', '?', substring-after($baseActionURL, '?'), 'method=render&amp;channelID=', substring-before(substring-after($baseActionURL, 'channelTarget='), '&amp;'))"/>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="$baseActionURL"/></xsl:otherwise>
  </xsl:choose>
</xsl:variable>
<!--******************************************************************-->

<xsl:template match="content">
  <xsl:choose>
    <xsl:when test="suggest">
      Your guess of <xsl:value-of select="guess"/> was incorrect.
      Try again -- guess <strong><xsl:value-of select="suggest"/></strong>!<br />
      You have made <xsl:value-of select="guesses"/> guesses.
    </xsl:when>
    <xsl:when test="answer">
      You got it after <strong><xsl:value-of select="guesses"/></strong> tries!
      The answer was <strong><xsl:value-of select="answer"/></strong>!<br />
      <p>Please play again...</p>
    </xsl:when>
    <xsl:otherwise>This is a number guessing game.<br /></xsl:otherwise>
  </xsl:choose> 
  
  I am thinking of a number between 
  <xsl:value-of select="minNum"/> and 
  <xsl:value-of select="maxNum"/>.<br />
  What's your guess?
    <form action="{$baseActionURL}" method="post">
      <input type="text" name="guess" size="4"/>
      <input type="submit" value="Submit"/>
    </form>
</xsl:template>

</xsl:stylesheet> 
