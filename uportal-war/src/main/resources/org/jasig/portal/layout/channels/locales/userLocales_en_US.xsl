<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="baseActionURL">baseActionURL not set</xsl:param>
    
  <xsl:template match="/">
      Please indicate your language preference:<br/>
      <xsl:apply-templates select="locales"/>
  </xsl:template>
  
  <xsl:template match="locales">
      <form action="{$baseActionURL}" method="post">
          <xsl:apply-templates select="locale"/>
          <input type="submit" name="submit" value="Submit" class="uportal-button"/>
      </form>
  </xsl:template>
  
  <xsl:template match="locale">
      <input type="radio" name="locale" value="{@code}" class="uportal-button">
      <xsl:if test="@selected='true'">
          <xsl:attribute name="checked">checked</xsl:attribute>
      </xsl:if>
      </input>
      <xsl:value-of select="@displayName"/><br/>
  </xsl:template>  
    
</xsl:stylesheet>