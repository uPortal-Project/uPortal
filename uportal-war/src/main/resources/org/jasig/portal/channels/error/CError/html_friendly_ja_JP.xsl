<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">ja_JP</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/error/CError/</xsl:variable>
  <xsl:param name="allowRefresh">true</xsl:param>
  <xsl:param name="allowReinstantiation">true</xsl:param>
  <xsl:param name="showStackTrace">true</xsl:param>

  <xsl:template match="error">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td>
          <img src="{$baseMediaURL}wrenchworks.gif" width="112" height="119"/>
        </td>
        <td>
          <img src="{$baseMediaURL}transparent.gif" width="16" height="16"/>
        </td>
        <td class="uportal-channel-subtitle" width="100%">エラー：<br/><span class="uportal-channel-error">
              <xsl:choose>
                <xsl:when test="@code='4'">チャネルがタイムアウトしました</xsl:when>
              <xsl:when test="@code='1'">レンダリングに失敗しました</xsl:when>
              <xsl:when test="@code='2'">初期化に失敗しました</xsl:when>
              <xsl:when test="@code='3'">必要なデータの取得に失敗しました</xsl:when>
              <xsl:when test="@code='0'">何らかのエラーが発生しました</xsl:when>
              <xsl:when test="@code='5'">PCS の取得に失敗しました</xsl:when>
              <xsl:when test="@code='6'">このチャネルを表示する権限がありません</xsl:when>
              <xsl:when test="@code='7'">このチャネルは利用可能ではありません</xsl:when>
              <xsl:when test="@code='-1'">何らかの uPortal エラーが発生しました</xsl:when></xsl:choose></span>
          <br/>
          <br/>

          <xsl:if test="$allowRefresh='true'">
            <a href="{$baseActionURL}?action=retry">
              <img src="{$baseMediaURL}error_refresh.gif" border="0" width="16" height="16" alt="Refresh the channel"/>
              <img src="{$baseMediaURL}transparent.gif" border="0" width="4" height="4"/>
              <span class="uportal-label">チャネルのリフレッシュ</span>
            </a>

            <br/>
          </xsl:if>

          <xsl:if test="$allowReinstantiation='true'">
            <a href="{$baseActionURL}?action=restart">
              <img src="{$baseMediaURL}error_reboot.gif" border="0" width="16" height="16" alt="Reboot the channel"/>
              <img src="{$baseMediaURL}transparent.gif" border="0" width="4" height="4"/>
              <span class="uportal-label">チャネルの再起動</span>
            </a>
          </xsl:if>
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
