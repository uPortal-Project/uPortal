<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" />

   <xsl:param name="baseActionURL">default</xsl:param>

   <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CBookmarks</xsl:variable>

   <xsl:template match="bookmarks">
      <table width="100%" border="0" cellspacing="" cellpadding="5">
         <tr class="uportal-background-light">
            <td class="uportal-channel-table-caption">Action</td>

            <td class="uportal-channel-table-caption">Name</td>

            <td class="uportal-channel-table-caption">Location</td>

            <td class="uportal-channel-table-caption">Description</td>
         </tr>

         <xsl:apply-templates select="bookmark" />
      </table>

      <xsl:call-template name="buttons" />
   </xsl:template>

   <xsl:template match="bookmark">
      <xsl:variable name="thisBookmark">
         <xsl:value-of select="position()" />
      </xsl:variable>

      <tr align="left" valign="top" class="uportal-text">
         <td>
            <a href="{$baseActionURL}?action=edit&amp;bookmark={$thisBookmark}">
               <img src="{$mediaPath}/edit.gif" width="16" height="16" alt="edit bookmark" border="0" />
            </a>

            <img src="{$mediaPath}/transparent.gif" width="5" height="16" border="0" />

            <a href="{$baseActionURL}?action=delete&amp;bookmark={$thisBookmark}">
               <img src="{$mediaPath}/remove.gif" width="16" height="16" alt="remove bookmark" border="0" />
            </a>
         </td>

         <td nowrap="nowrap" class="uportal-channel-subtitle-reversed">
            <xsl:value-of select="@name" /><xsl:value-of select="position()" />
         </td>

         <td nowrap="nowrap" class="uportal-channel-table-row-even">
            <a href="{@url}" target="_blank">
               <xsl:value-of select="@url" />
            </a>
         </td>

         <td width="100%">
            <xsl:value-of select="@comments" />
         </td>
      </tr>
   </xsl:template>

   <xsl:template name="buttons">
      <table border="0">
         <tr>
            <td>
               <form method="post" action="{$baseActionURL}?action=new">
                  <input type="submit" name="add" value="New bookmark" class="uportal-button" />
               </form>
            </td>

            <td>
               <form method="post" action="{$baseActionURL}?action=doneEditing">
                  <input type="submit" name="" value="Save and return" class="uportal-button" />
               </form>
            </td>
         </tr>
      </table>
   </xsl:template>
</xsl:stylesheet>

