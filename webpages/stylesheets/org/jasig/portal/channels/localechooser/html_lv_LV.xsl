<?xml version='1.0' encoding='utf-8' ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>
  <xsl:variable name="mediaPath" select="'media/org/jasig/portal/channels/localechooser'"/>

  <xsl:template match="locale-status">
    <form action="{$baseActionURL}?uP_root=root" method="post">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr class="uportal-background-dark">
        <!--
        <td nowrap="nowrap" class="uportal-background-dark" colspan="1" rowspan="1">
          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/>
          <strong>
   	  <xsl:value-of select="current-locale"/>
          </strong>
        </td>
        -->
        <td align="right" nowrap="nowrap" class="uportal-background-dark" colspan="1" rowspan="1">
  	  <select class="uportal-button" name="locale">
              <option value="lv_LV" selected="selected">English</option>
              <option value="ja_JP">Japanese</option>
              <option value="sv_SE">Swedish</option>
              <option value="de_DE">German</option>
	  </select>
          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/>
          <input type="submit" value="Go" name="Go" class="uportal-button"/>
          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/>
        </td>
      </tr>
    </table>
    </form>
  </xsl:template>

</xsl:stylesheet>

