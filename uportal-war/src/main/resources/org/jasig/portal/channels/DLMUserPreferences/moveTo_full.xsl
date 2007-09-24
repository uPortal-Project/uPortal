<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="baseActionURL">Default</xsl:param>
<xsl:param name="locale">en_US</xsl:param>
<xsl:variable name="imageDir" select="'media/org/jasig/portal/channels/CUserPreferences'"/>

<xsl:template match="/">
  <table align="center" border="0" cellpadding="5" cellspacing="0">
    <tr><td><xsl:value-of select="$CHOOSE_A_FOLDER_TO_MOVE_SELECTED_ITEMS_TO_AND_THEN_CLICK_MOVE_"/></td></tr>
    <tr><td align="center">
      <table><tr><td>
        <form action="{$baseActionURL}" method="post">
      	  <input name="action" type="hidden" value="moveTo"/>
      	  <p align="center"><input type="submit"><xsl:attribute name="name"><xsl:value-of select="$MOVE"/></xsl:attribute><xsl:attribute name="value"><xsl:value-of select="$MOVE"/></xsl:attribute></input></p>
      	    <input name="destination" type="radio"><xsl:attribute name="value"><xsl:value-of select="$TOP"/></xsl:attribute></input>
            <img border="0" height="10" width="13"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_FOLDER_CLOSED_GIF"/></xsl:attribute></img><xsl:value-of select="$MY_CHANNELS"/><br/>      
            <xsl:apply-templates select="layout"/>      
      	  <p align="center"><input type="submit"><xsl:attribute name="name"><xsl:value-of select="$MOVE"/></xsl:attribute><xsl:attribute name="value"><xsl:value-of select="$MOVE"/></xsl:attribute></input></p>
        </form>
      </td></tr></table>
    </td></tr>
  </table>
</xsl:template>

<xsl:template match="folder">
  <!-- Indent according to position in hierarchy-->
  <xsl:for-each select="ancestor::*">
    <img height="1" src="{$imageDir}/transparent1x1.gif" width="20"/>
  </xsl:for-each>

  <input name="destination" type="radio" value="{@ID}"/>
  <img border="0" height="10" width="13"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_FOLDER_CLOSED_GIF"/></xsl:attribute></img>
  <xsl:value-of select="@name"/><br/>
  <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>