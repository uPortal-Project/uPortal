<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="html" indent="yes" />

   <xsl:param name="baseActionURL">render.uP</xsl:param>

   <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CGenericXSLT</xsl:variable>

   <xsl:template match="rss">
      <html>
         <head>
            <title>uPortal 2.0</title>
         </head>

         <body>
            <xsl:apply-templates select="channel" />
         </body>
      </html>
   </xsl:template>

   <xsl:template match="channel">
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
         <tr align="left">
            <td width="100%" valign="bottom" class="uportal-channel-subtitle">
               <xsl:value-of select="description" />
            </td>

            <td>
               <a href="{image/link}" target="_blank">
                  <img alt="interface image" src="{image/url}" border="0" />
               </a>
            </td>
         </tr>
      </table>

      <br />

      <xsl:apply-templates select="item" />

      <br />

      <xsl:apply-templates select="textinput" />
   </xsl:template>

   <xsl:template match="item">
      <table width="100%" border="0" cellspacing="0" cellpadding="2">
         <tr>
            <td>
               <img alt="interface image" src="{$mediaPath}/bullet.gif" width="16" height="16" />
            </td>

            <td width="100%" class="uportal-channel-subtitle-reversed">
               <a href="{link}" target="_blank">
                  <xsl:value-of select="title" />
               </a>
            </td>
         </tr>

         <xsl:if test="description != ''">
            <tr class="uportal-channel-text">
               <td> </td>

               <td width="100%">
                  <xsl:value-of select="description" />
               </td>
            </tr>
         </xsl:if>
      </table>
   </xsl:template>

   <xsl:template match="textinput">
      <form action="{link}">
         <span class="uportal-label">
            <xsl:value-of select="description" />
         </span>

         <br />

         <input type="text" name="{name}" size="30" class="uportal-input-text" />

         <br />

         <input type="submit" name="Submit" value="Submit" class="uportal-button" />
      </form>
   </xsl:template>
</xsl:stylesheet>

