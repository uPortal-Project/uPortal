<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseActionURL">render.uP</xsl:param>
  <xsl:param name="currentSkin">java</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CUserPreferences/tab-column</xsl:variable>

  <xsl:template match="/">
    <xsl:apply-templates select = "root" />
  </xsl:template>

  <xsl:template match="root">
    <form name="form1" method="post" action="{$baseActionURL}">
      <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td colspan="4" class="uportal-channel-table-header" valign="top">Design Skins</td>
        </tr>
        <!-- table headers -->
        <tr class="uportal-background-light">
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
          </td>
          <td align="center" class="uportal-channel-text" valign="middle">Name</td>
          <td align="center" class="uportal-channel-text" valign="middle">Description</td>
          <td align="center" class="uportal-channel-text" valign="middle">Icon</td>
        </tr>
        <xsl:apply-templates select = "skin">
          <xsl:sort select="Skin-Name"/>
        </xsl:apply-templates>
        <tr>
          <td class="uportal-text-small" colspan="4">
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10"/>
          </td>
        </tr>
        <tr>
          <td class="uportal-text-small" align="center" valign="top" colspan="4">
            <input type="hidden" name="action" value="completeEdit"/>
            <input type="submit" name="submitSave" value="Select Skin" class="uportal-button"/>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10"/>
            <input type="submit" name="submitCancel" value="Cancel" class="uportal-button"/>
          </td>
        </tr>
      </table>
    </form>
  </xsl:template>


  <xsl:template match="skin">
    <tr>
      <td align="center" class="uportal-channel-text" valign="middle">
        <xsl:choose>
          <xsl:when test="$currentSkin=Skin-Folder-Name">
            <input type="radio" name="skinName" value="{Skin-Folder-Name}" checked="checked"/>
          </xsl:when>
          <xsl:otherwise>
            <input type="radio" name="skinName" value="{Skin-Folder-Name}"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td align="center" class="uportal-channel-text" valign="middle">
        <xsl:value-of disable-output-escaping="yes" select="Skin-Name" />
      </td>
      <td align="center" class="uportal-channel-text">
	    <xsl:value-of disable-output-escaping="yes" select="Skin-Description" />
      </td>
      <td align="center" valign="top">
        <img alt="sample icon" src="{$mediaPath}/{Skin-Thumbnail-File}" width="120" height="90" border="0"/>
      </td>
    </tr>  
  </xsl:template>

</xsl:stylesheet>