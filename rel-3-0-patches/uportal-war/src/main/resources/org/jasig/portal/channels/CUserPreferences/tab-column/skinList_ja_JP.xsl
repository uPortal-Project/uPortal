<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="skinsPath">media/org/jasig/portal/layout/tab-column/nested-tables</xsl:param>
  <xsl:param name="currentSkin">java</xsl:param>
  <xsl:param name="locale">ja_JP</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/layout/tab-column/nested-tables</xsl:variable>

  <xsl:template match="/">
    <xsl:apply-templates select="skins"/>
  </xsl:template>

  <xsl:template match="skins">
        <form name="form1" method="post" action="{$baseActionURL}">
        <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
          <tr class="uportal-channel-text">
            <td><strong>スキン選択：</strong> ポータルのスキンを下で選択し，「適用」をクリックします．</td>
          </tr>
          <tr class="uportal-channel-text">
            <td>
              <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">
                <tr class="uportal-channel-table-header">
                  <td nowrap="nowrap">選択肢</td>
                  <td>
                    <img alt="インターフェイス画像" src="{$mediaPath}/{$currentSkin}/skin/transparent.gif" width="16" height="8"/>
                  </td>
                  <td nowrap="nowrap">サムネール</td>
                  <td>
                    <img alt="インターフェイス画像" src="{$mediaPath}/{$currentSkin}/skin/transparent.gif" width="16" height="8"/>
                  </td>
                  <td width="100%"><img alt="インターフェイス画像" src="{$mediaPath}/{$currentSkin}/skin/transparent.gif" width="1" height="1"/></td>
                </tr>
                <tr class="uportal-channel-table-header">
                  <td colspan="5">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                      <tr>
                        <td>
                          <img alt="インターフェイス画像" src="{$mediaPath}/{$currentSkin}/skin/transparent.gif" width="2" height="2"/>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <xsl:apply-templates select="skin">
                  <xsl:sort select="skin-name"/>
                </xsl:apply-templates>                
              </table>
            </td>
          </tr>
          <tr>
            <td>
              <input type="hidden" name="action" value="completeEdit"/>
              <input type="submit" name="submitSave" value="適用" class="uportal-button"/>
              <input type="submit" name="submitCancel" value="キャンセル" class="uportal-button"/>              
            </td>
          </tr>
        </table>
        </form>
  </xsl:template>


  <xsl:template match="skin">
    <tr valign="top">
      <td align="center">
        <xsl:choose>
          <xsl:when test="$currentSkin=skin">
            <input type="radio" name="skinName" value="{skin}" checked="checked"/>
          </xsl:when>
          <xsl:otherwise>
            <input type="radio" name="skinName" value="{skin}"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td><img alt="インターフェイス画像" src="{$mediaPath}/{$currentSkin}/skin/transparent.gif" width="1" height="1"/></td>
      <td>
        <img height="90" alt="{skin-name} thumbnail" src="{$skinsPath}/{skin}/skin/{skin}_thumb.gif" width="120" border="0"/>
      </td>
      <td><img alt="インターフェイス画像" src="{$mediaPath}/{$currentSkin}/skin/transparent.gif" width="1" height="1"/></td>
      <td class="uportal-channel-table-header">
        <table width="100%" border="0" cellspacing="0" cellpadding="2">
          <tr valign="top">
            <td class="uportal-channel-table-header" nowrap="nowrap">名前：</td>
            <td width="100%" class="uportal-channel-text">
              <strong>
                <xsl:value-of select="skin-name"/>
              </strong>
            </td>
          </tr>
          <tr valign="top">
            <td nowrap="nowrap" class="uportal-channel-table-header">説明：<img alt="インターフェイス画像" src="{$mediaPath}/{$currentSkin}/skin/transparent.gif" width="4" height="4"/></td>
            <td class="uportal-channel-text">
              <xsl:value-of select="skin-description"/>
            </td>
          </tr>
        </table>
      </td>
    </tr>
    <tr class="uportal-channel-table-header">
      <td colspan="5">
        <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
          <tr>
            <td>
              <img alt="インターフェイス画像" src="{$mediaPath}/{$currentSkin}/skin/transparent.gif" width="1" height="1"/>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
