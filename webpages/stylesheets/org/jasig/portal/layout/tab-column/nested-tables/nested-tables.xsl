<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="no" />

  <xsl:param name="baseActionURL">render.uP</xsl:param>
  <xsl:param name="skin" select="'java'" />

  <xsl:variable name="mediaPath">media/org/jasig/portal/layout/tab-column/nested-tables</xsl:variable>

  <!-- This template is supposed to render a fragment of the layout. For example, during
       a detach mode, only <channel> element that's detached is passed along to the structure
       transformation.
       In general, it should render a fragment that contains not just a single channel, but 
       an entire column or a tab, perhaps :) But I am lazy, so I'll just flatten out all of 
       the channels into one big column.
       -peter.
  -->
  <xsl:template match="layout_fragment">
    <html>
      <head>
        <title>uPortal 2.0</title>
        <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/{$skin}.css" />

        <script language="JavaScript">
          function openBrWindow(theURL,winName,features) {
            window.open(theURL,winName,features);
          }
        </script>
      </head>
      <body>
         <xsl:for-each select="content//channel">
           <xsl:apply-templates select="." />
         </xsl:for-each>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="layout">
    <html>
      <head>
        <title>uPortal 2.0</title>
        <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/{$skin}.css" />

        <script language="JavaScript">
          function openBrWindow(theURL,winName,features) {
            window.open(theURL,winName,features);
          }
        </script>
      </head>

      <body>
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td valign="top">
              <table summary="add summary" width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td height="100" valign="top">
                    <img alt="interface image" src="{$mediaPath}/{$skin}/uportal_logo.gif" border="0" />
                  </td>
                
                  <xsl:apply-templates select="header"/>
                
                </tr>
              </table>
            </td>
          </tr>

          <xsl:apply-templates select="navigation" />

          <xsl:apply-templates select="content" />

          <tr>
            <td>
              <table width="100%" border="0" cellpadding="1" cellspacing="0">
                <tr>
                  <xsl:apply-templates select="footer" />
                </tr>
              </table>
            </td>
          </tr>
          
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="header">
    <xsl:for-each select="channel">
      <td align="center">
        <xsl:copy-of select="."/>
      </td>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="navigation">
    <tr>
      <td>
        <table summary="add summary" border="0" cellspacing="0" cellpadding="0" width="100%">
          <tr>
            <xsl:for-each select="tab">
              <xsl:choose>
                <xsl:when test="following-sibling::tab[1]/@activeTab = 'true'">
                  <td nowrap="nowrap" class="uportal-background-light">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />
                    <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                      <xsl:value-of select="@name" />
                    </a>
                  </td>

                  <td class="uportal-background-content">
                     <img alt="interface image" src="{$mediaPath}/{$skin}/before_active_tab.gif" width="11" height="28" />
                  </td>
                </xsl:when>

                <xsl:when test="position()=last() and @activeTab='false'">
                  <td nowrap="nowrap" class="uportal-background-light">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />

                     <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                       <xsl:value-of select="@name" />
                     </a>
                   </td>

                   <td width="100%">
                     <img alt="interface image" src="{$mediaPath}/{$skin}/after_last_tab_inactive.gif" width="11" height="28" />
                   </td>
                </xsl:when>

                <xsl:when test="position()=last() and @activeTab='true'">
                  <td nowrap="nowrap" class="uportal-background-content">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />

                    <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                      <xsl:value-of select="@name" />
                    </a>
                  </td>

                  <td width="100%">
                    <img alt="interface image" src="{$mediaPath}/{$skin}/after_last_tab_active.gif" width="11" height="28" />
                  </td>
                </xsl:when>

                <xsl:when test="@activeTab='false'">
                  <td nowrap="nowrap" class="uportal-background-light">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />

                    <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                      <xsl:value-of select="@name" />
                    </a>
                  </td>

                  <td class="uportal-background-med">
                    <img alt="interface image" src="{$mediaPath}/{$skin}/after_inactive_tab.gif" width="11" height="28" />
                  </td>
                </xsl:when>

                <xsl:when test="@activeTab='true'">
                  <td nowrap="nowrap" class="uportal-background-content">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />

                    <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                      <xsl:value-of select="@name" />
                    </a>
                  </td>

                  <td class="uportal-background-med">
                    <img alt="interface image" src="{$mediaPath}/{$skin}/after_active_tab.gif" width="11" height="28" />
                  </td>
                </xsl:when>
              </xsl:choose>
            </xsl:for-each>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="content">
    <tr>
      <td>
        <table border="0" cellspacing="0" cellpadding="0" class="uportal-background-content" width="100%">
          <tr>
            <xsl:call-template name="controlRow" />
          </tr>

          <tr>
            <xsl:call-template name="contentRow" />
          </tr>

          <tr>
            <xsl:call-template name="controlRow" />
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="controlRow">
    <xsl:for-each select="column">
      <xsl:choose>
        <xsl:when test="position()=1 and position()=last()">
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>

          <td width="100%">
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>
        </xsl:when>

        <xsl:when test="position()=1">
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>

          <td width="{@width}">
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>
        </xsl:when>

        <xsl:when test="position()=last()">
          <td width="{@width}">
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>
        </xsl:when>

        <xsl:when test="position()!=1 and position()!=last()">
          <td width="{@width}">
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="contentRow">
    <xsl:for-each select="column">
      <xsl:choose>
        <xsl:when test="position()=1 and position()=last()">
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>

          <td align="left" valign="top" width="100%">
            <xsl:apply-templates select="channel" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>
        </xsl:when>

        <xsl:when test="position()=1">
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>

          <td align="left" valign="top" width="{@width}">
            <xsl:apply-templates select="channel" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>

          <td class="uportal-background-dark">
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>
        </xsl:when>

        <xsl:when test="position()=last()">
          <td align="left" valign="top" width="{@width}">
            <xsl:apply-templates select="channel" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>
        </xsl:when>

        <xsl:when test="position()!=1 and position()!=last()">
          <td align="left" valign="top" width="{@width}">
            <xsl:apply-templates select="channel" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>

          <td class="uportal-background-dark">
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" />
          </td>

          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" />
          </td>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="channel">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr align="left" valign="bottom">
        <td class="uportal-channel-title">
          <xsl:value-of select="@name" />
        </td>

        <td align="right" nowrap="nowrap">
          <xsl:if test="not(@hasHelp='false')">
            <a href="{$baseActionURL}?uP_help_target={@ID}">
              <img alt="help" src="{$mediaPath}/help.gif" width="16" height="16" border="0" />
            </a>
          </xsl:if>

          <xsl:if test="not(@hasAbout='false')">
            <a href="{$baseActionURL}?uP_about_target={@ID}">
              <img alt="about" src="{$mediaPath}/about.gif" width="16" height="16" border="0" />
            </a>
          </xsl:if>

          <xsl:if test="not(@editable='false')">
            <a href="{$baseActionURL}?uP_edit_target={@ID}">
              <img alt="edit" src="{$mediaPath}/edit.gif" width="16" height="16" border="0" />
            </a>
          </xsl:if>

          <xsl:if test="@printable='true'">
            <a href="{$baseActionURL}?uP_print_target={@ID}">
              <img alt="print" src="{$mediaPath}/print.gif" width="16" height="16" border="0" />
            </a>
          </xsl:if>

          <xsl:if test="not(@minimizable='false')">
            <xsl:choose>
              <xsl:when test="@minimized='true'">
                <a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=false">
                  <img alt="maximize" src="{$mediaPath}/max.gif" width="16" height="16" border="0" />
                </a>
              </xsl:when>

              <xsl:otherwise>
                <a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=true">
                  <img alt="minimize" src="{$mediaPath}/min.gif" width="16" height="16" border="0" />
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>

          <xsl:if test="not(@detachable='false')">
            <a href="#" onClick="openBrWindow('{$baseActionURL}?uP_detach_target={@ID}','detachedChannel','toolbar=yes,location=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,width=640,height=480')">
              <img alt="detach" src="{$mediaPath}/detach.gif" width="16" height="16" border="0" />
            </a>
          </xsl:if>

          <xsl:if test="not(@unremovable='true')">
            <a href="{$baseActionURL}?uP_remove_target={@ID}" onClick="return confirm('Are you sure you want to remove this channel?')">
              <img alt="remove" src="{$mediaPath}/remove.gif" width="16" height="16" border="0" />
            </a>
          </xsl:if>
        </td>
      </tr>

      <tr class="uportal-background-dark">
        <td height="1" colspan="2">
          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" />
        </td>
      </tr>

      <tr>
        <td colspan="2">
          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="5" />
        </td>
      </tr>

      <tr>
        <td class="uportal-channel-text" colspan="2">
          <xsl:if test="@minimized != 'true'">
            <xsl:copy-of select="." />
          </xsl:if>
        </td>
      </tr>

      <tr>
        <td colspan="2">
          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" />
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="footer">
    <xsl:for-each select="channel">
      <td align="center">
        <xsl:copy-of select="."/>
      </td>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>

