<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">ja_JP</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/CSecureInfo/</xsl:variable>

  <xsl:template match="secure">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td>
          <img src="{$baseMediaURL}wrenchworks.gif" width="112" height="119"/>
        </td>
        <td>
          <img src="{$baseMediaURL}transparent.gif" width="16" height="16"/>
        </td>
        <td class="uportal-channel-subtitle" width="100%">注意:<br/><span class="uportal-channel-error">
	    このチャネルは，セキュアプロトコル(例：https)を用いて表示する必要があります．</span>
          <br/>
          <br/>
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
