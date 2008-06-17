<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">ja_JP</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/CUserPreferences/</xsl:variable>
  <xsl:template match="profiles">
        <div align="center">
          <center>
	<p class="uportal-channel-warning">現在使用されているブラウザでは過去にアクセスされたことがありません．<br/>
適合するプロファイルを選択してください．</p>
            <table border="0" width="100%" cellspacing="0" cellpadding="2">
              <xsl:apply-templates select="system"/>
            </table>
          </center>
        </div>
  </xsl:template>

  <xsl:template match="system">
    <tr>
      <td class="uportal-background-med" valign="top" colspan="5">
        <p class="uportal-channel-title">システムプロファイル</p>
      </td>
    </tr>
    <tr>
      <td class="uportal-background-light" valign="bottom" align="center">
        <p class="uportal-channel-subtitle">アクション</p>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <img alt="" border="0" width="10" height="10">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
        </img>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <p class="uportal-channel-subtitle">プロファイル名</p>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <img alt="" border="0" width="10" height="10">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
        </img>
      </td>
      <td width="60%" class="uportal-background-light" valign="bottom">
        <p class="uportal-channel-subtitle">プロファイルの説明</p>
      </td>
    </tr>
    <xsl:apply-templates mode="system"/>
  </xsl:template>
  <xsl:template match="profile" mode="system">
        <tr>
          <td class="uportal-text" valign="top" align="center">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=map&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=system</xsl:attribute>
              <img border="0" width="16" height="16" alt="このプロファイルを現在のブラウザにマップ">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_map.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top" align="center"/>
          <td class="uportal-text" valign="top">
            <xsl:value-of select="@name"/>
          </td>
          <td class="uportal-text" valign="top" align="center"/>
          <td width="60%" class="uportal-text" valign="top">
            <xsl:value-of select="description"/>
          </td>
        </tr>
  </xsl:template>
</xsl:stylesheet>
