<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="baseActionURL">デフォルト</xsl:param>
<xsl:param name="locale">ja_JP</xsl:param>
<xsl:variable name="imageDir" select="'media/org/jasig/portal/channels/CUserPreferences'"/>

<xsl:template match="/">
  <table border="0" cellspacing="0" cellpadding="5" align="center">
    <tr><td>移動したいフォルダを選択し，「移動」をクリックします．</td></tr>
    <tr><td align="center">
      <table><tr><td>
        <form action="{$baseActionURL}" method="post">
      	  <input type="hidden" name="action" value="moveTo"/>
      	  <p align="center"><input type="submit" name="move" value="移動"/></p>
      	    <input type="radio" name="destination" value="top"/>
            <img src="{$imageDir}/folder_closed.gif" border="0" width="13" height="10"/>
            マイチャネル<br/>      
            <xsl:apply-templates select="layout"/>      
      	  <p align="center"><input type="submit" name="move" value="移動"/></p>
        </form>
      </td></tr></table>
    </td></tr>
  </table>
</xsl:template>

<xsl:template match="folder">
  <!-- Indent according to position in hierarchy-->
  <xsl:for-each select="ancestor::*">
    <img src="{$imageDir}/transparent1x1.gif" width="20" height="1"/>
  </xsl:for-each>

  <input type="radio" name="destination" value="{@ID}"/>
  <img src="{$imageDir}/folder_closed.gif" border="0" width="13" height="10"/>
  <xsl:value-of select="@name"/><br/>
  <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
