<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="skinsPath">media/skins/universality</xsl:param>
  <xsl:param name="currentSkin">java</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:variable name="mediaPath">media/skins/universality</xsl:variable>

  <xsl:template match="/">
    <xsl:apply-templates select="skins"/>
  </xsl:template>

  <xsl:template match="skins">
        <form name="form1" method="post" action="{$baseActionURL}">
        <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
          <tr class="uportal-channel-text">
            <td><strong><xsl:value-of select="$SKIN_SELECTION"/></strong><xsl:value-of select="$SELECT_A_PORTAL_SKIN_BELOW_THEN_CLICK_APPLY_"/></td>
          </tr>
          <tr class="uportal-channel-text">
            <td>
              <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">
                <tr class="uportal-channel-table-header">
                  <td nowrap="nowrap"><xsl:value-of select="$OPTION"/></td>
                  <td>
                    <img src="{$mediaPath}/{$currentSkin}/images/transparent.gif" width="16" height="8"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
                  </td>
                  <td nowrap="nowrap"><xsl:value-of select="$THUMBNAIL"/></td>
                  <td>
                    <img src="{$mediaPath}/{$currentSkin}/images/transparent.gif" width="16" height="8"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
                  </td>
                  <td width="100%"><img src="{$mediaPath}/{$currentSkin}/images/transparent.gif" width="1" height="1"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td>
                </tr>
                <tr class="uportal-channel-table-header">
                  <td colspan="5">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                      <tr>
                        <td>
                          <img src="{$mediaPath}/{$currentSkin}/images/transparent.gif" width="2" height="2"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
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
              <input type="submit" name="submitSave" class="uportal-button"><xsl:attribute name="value"><xsl:value-of select="$APPLY"/></xsl:attribute></input>
              <input type="submit" name="submitCancel" class="uportal-button"><xsl:attribute name="value"><xsl:value-of select="$CANCEL"/></xsl:attribute></input>              
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
            <input type="radio" name="skinName" value="{skin-key}" checked="checked"/>
          </xsl:when>
          <xsl:otherwise>
            <input type="radio" name="skinName" value="{skin-key}"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td><img src="{$mediaPath}/{$currentSkin}/images/transparent.gif" width="1" height="1"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td>
      <td>
        <img height="90" width="120" border="0"><xsl:attribute name="src"><xsl:value-of select="$skinsPath"/>/<xsl:value-of select="skin-key"/>/<xsl:value-of select="skin-key"/><xsl:value-of select="$IMAGE_SRC_THUMB_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="skin-name"/><xsl:text> </xsl:text><xsl:value-of select="$SKINNAME_THUMBNAIL"/></xsl:attribute></img>
      </td>
      <td><img src="{$mediaPath}/{$currentSkin}/images/transparent.gif" width="1" height="1"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td>
      <td class="uportal-channel-table-header">
        <table width="100%" border="0" cellspacing="0" cellpadding="2">
          <tr valign="top">
            <td class="uportal-channel-table-header"><xsl:value-of select="$NAME"/></td>
            <td width="100%" class="uportal-channel-text">
              <strong>
                <xsl:value-of select="skin-name"/>
              </strong>
            </td>
          </tr>
          <tr valign="top">
            <td nowrap="nowrap" class="uportal-channel-table-header"><xsl:value-of select="$DESCRIPTION"/><img src="{$mediaPath}/{$currentSkin}/images/transparent.gif" width="4" height="4"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td>
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
              <img src="{$mediaPath}/{$currentSkin}/images/transparent.gif" width="1" height="1"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>