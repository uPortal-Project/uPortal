<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>

<xsl:template match="content">
  <xsl:choose>
    <xsl:when test="suggest">
      You have made <xsl:value-of select="guesses"/> guesses.<br/>
      Your last guess of <xsl:value-of select="guess"/> was incorrect.<br/>
      Try again, this time guess <strong><xsl:value-of select="suggest"/></strong>!<br/><br/>
    </xsl:when>
    <xsl:when test="answer">
      You got it after only <strong><xsl:value-of select="guesses"/></strong> tries!<br/>
      The answer was <strong><xsl:value-of select="answer"/></strong>!<br />
      <p>Please play again...</p>
    </xsl:when>
    <xsl:otherwise>This is a number guessing game.<br /></xsl:otherwise>
  </xsl:choose> 
  
  I am thinking of a number between <xsl:value-of select="minNum"/> and <xsl:value-of select="maxNum"/>.<br />
  What's your guess?
  <form action="{$baseActionURL}" method="post">
    <input type="text" name="guess" size="4"/>
    <input type="submit" value="Submit"/>
  </form>
</xsl:template>

</xsl:stylesheet> 
