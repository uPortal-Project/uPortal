<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseActionURL">no parameter passed</xsl:param>
  <xsl:param name="mediaPath">media/org/jasig/portal/channels/CBookmarks</xsl:param>
  <xsl:template match="xbel" name="BookmarkTree">
    <xsl:param name="TreeMode">View</xsl:param>
    <!-- TreeMode: <xsl:value-of select="$TreeMode"/><br/> -->
    <table width="100%" border="0" cellpadding="0" cellspacing="0"><xsl:attribute name="class"><xsl:if test="$TreeMode != 'View'">uportal-background-med</xsl:if></xsl:attribute>
      <xsl:apply-templates select="/xbel/folder">
        <xsl:sort select="title"/>
        <xsl:with-param name="TreeMode">
          <xsl:value-of select="$TreeMode"/>
        </xsl:with-param>
      </xsl:apply-templates>
      <xsl:apply-templates select="/xbel/bookmark">
        <xsl:sort select="title"/>
        <xsl:with-param name="TreeMode">
          <xsl:value-of select="$TreeMode"/>
        </xsl:with-param>
      </xsl:apply-templates>
      <xsl:if test="$TreeMode='AddFolder' or $TreeMode='AddBookmark'">
        <tr align="left" valign="top" class="uportal-channel-text">
          <td align="left" valign="top" class="uportal-channel-text">
            <input type="radio" name="FolderRadioButton" value="RootLevel" checked="true" class="uportal-input-text"/>
          </td>
          <td align="left" valign="top" class="uportal-channel-text">New Top Level<xsl:value-of select="substring-after($TreeMode,'Add')"/></td>
        </tr>
      </xsl:if>
    </table>
  </xsl:template>
  <xsl:template match="folder">
    <xsl:param name="TreeMode">View</xsl:param>
    <tr align="left" valign="top"><!--<xsl:attribute name="class"><xsl:if test="$TreeMode != 'View'">uportal-background-light</xsl:if></xsl:attribute>-->
      <!-- Display a radio button or checkbox if in edit mode -->
      <td align="left" valign="top" class="uportal-channel-text">
        <xsl:choose>
          <xsl:when test="$TreeMode='AddFolder' or $TreeMode='AddBookmark'">
            <input type="radio" name="FolderRadioButton" value="{@id}" class="uportal-input-text"/>
          </xsl:when>
          <xsl:when test="$TreeMode='DeleteFolder'">
            <input type="checkbox" name="FolderCheckbox#{@id}" class="uportal-input-text"/>
          </xsl:when>
        </xsl:choose>
      </td>
      <td align="left" valign="top" class="uportal-channel-text">
        <!-- Indent the folder -->
        <img src="{$mediaPath}/transparent.gif" width="{(count(ancestor::*)-1) * 4 + (count(ancestor::*)-1) * 16}" height="16"/>
        <!-- Display an open or closed folder icon and the folder title -->
        <xsl:choose>
          <xsl:when test="@folded='yes'">
            <a href="{$baseActionURL}?command=unfold&amp;ID={@id}">
              <img src="{$mediaPath}/folded_yes.gif" border="0" alt="Closed Folder"/>
              <img src="{$mediaPath}/transparent.gif" border="0" width="4" height="16"/>
              <strong>
                <xsl:value-of select="title"/>
              </strong>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <a href="{$baseActionURL}?command=fold&amp;ID={@id}">
              <img src="{$mediaPath}/folded_no.gif" border="0" alt="Open Folder"/>
              <img src="{$mediaPath}/transparent.gif" border="0" width="4" height="16"/>
              <strong>
                <xsl:value-of select="title"/>
              </strong>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
    <!-- Recurse through the subtrees if the folder is open -->
    <xsl:if test="@folded='no'">
      <xsl:apply-templates select="folder">
        <xsl:sort select="title"/>
        <xsl:with-param name="TreeMode">
          <xsl:value-of select="$TreeMode"/>
        </xsl:with-param>
      </xsl:apply-templates>
      <xsl:apply-templates select="bookmark">
        <xsl:sort select="title"/>
        <xsl:with-param name="TreeMode">
          <xsl:value-of select="$TreeMode"/>
        </xsl:with-param>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
  <xsl:template match="bookmark">
    <xsl:param name="TreeMode">View</xsl:param>
    <tr align="left" valign="top"><xsl:attribute name="class"><xsl:if test="$TreeMode != 'View'">uportal-background-light</xsl:if></xsl:attribute>
      <!-- Display a checkbox if in edit mode -->
      <td align="left" valign="top" class="uportal-channel-text">
        <xsl:choose>
          <xsl:when test="$TreeMode='DeleteBookmark'">
            <input type="checkbox" name="BookmarkCheckbox#{@id}" class="uportal-input-text"/>
          </xsl:when>
        </xsl:choose>
      </td>
      <td align="left" valign="top" class="uportal-channel-text">
        <!-- Indent the bookmark -->
        <img src="{$mediaPath}/transparent.gif" width="{(count(ancestor::*)-1) * 4 + (count(ancestor::*)-1) * 16}" height="16"/>
        <a href="{@href}"><img src="{$mediaPath}/bookmark.gif" border="0"/><img src="{$mediaPath}/transparent.gif" border="0" width="4" height="16"/>
          <xsl:value-of select="title"/>
        </a>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios/>
</metaInformation>
-->
