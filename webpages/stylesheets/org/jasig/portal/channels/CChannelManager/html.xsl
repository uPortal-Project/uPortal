<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="no"/>
  <xsl:param name="baseActionURL">render.uP</xsl:param>
  <xsl:param name="action">customSettings</xsl:param>
  <xsl:param name="stepID">1</xsl:param>
  <xsl:param name="errorMessage">no parameter passed</xsl:param>
  <xsl:param name="mediaPath">C:\LaJolla\uPortal\webpages\media\org\jasig\portal\channels\CChannelManager</xsl:param>
  <xsl:variable name="defaultLength">10</xsl:variable>
  <xsl:variable name="defaultMaxLength">20</xsl:variable>
  <xsl:variable name="defaultTextCols">40</xsl:variable>
  <xsl:variable name="defaultTextRows">10</xsl:variable>
  <xsl:variable name="filterByID"><xsl:value-of select="//filterByID[1]"/></xsl:variable>
  <xsl:template match="/">
    <html>
      <head>
        <title>Untitled Document</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
        <link rel="stylesheet" href="C:\LaJolla\uPortal\webpages\media\org\jasig\portal\layout\tab-column\nested-tables\imm\imm.css" type="text/css"/>
      <xsl:comment></xsl:comment>
      </head>
      <body>
        <xsl:choose>
          <xsl:when test="$action='selectChannelType'">
            <xsl:call-template name="selectChannelType"/>
          </xsl:when>
          <xsl:when test="$action='selectGeneralSettings'">
            <xsl:call-template name="selectGeneralSettings"/>
          </xsl:when>
          <xsl:when test="$action='selectModifyChannel'">
            <xsl:call-template name="selectModifyChannel"/>
          </xsl:when>
          <xsl:when test="$action='channelDef'">
            <xsl:call-template name="beginChannelDef"/>
          </xsl:when>
          <xsl:when test="$action='selectControls'">
            <xsl:call-template name="selectControls"/>
          </xsl:when>
          <xsl:when test="$action='selectCategories'">
            <xsl:call-template name="selectCategories"/>
          </xsl:when>
          <xsl:when test="$action='selectRoles'">
            <xsl:call-template name="selectRoles"/>
          </xsl:when>
          <xsl:when test="$action='reviewChannel'">
            <xsl:call-template name="reviewChannel"/>
          </xsl:when>
          <xsl:when test="$action='customSettings'">
            <xsl:call-template name="customSettings"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="defaultView"/>
          </xsl:otherwise>
        </xsl:choose>
      </body>
    </html>
  </xsl:template>
  <xsl:template name="defaultView">
    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
      <tr>
        <td>
          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-channel-text">
            <tr class="uportal-channel-strong" valign="top">
              <td colspan="2">Options for Managing Channels:</td>
            </tr>
            <tr valign="top">
              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
            </tr>
            <tr class="uportal-channel-text" valign="top">
              <td>
                <a href="#">
                </a>
                <img alt="interface image" src="{$mediaPath}/bullet.gif" width="16" height="16"/>
              </td>
              <td width="100%">
                <a href="{$baseActionURL}?uPCM_action=selectChannelType">Publish a new channel</a>
              </td>
            </tr>
            <tr valign="top">
              <td>
                <img alt="interface image" src="{$mediaPath}/bullet.gif" width="16" height="16"/>
              </td>
              <td class="uportal-channel-text" width="100%">
                <a href="{$baseActionURL}?uPCM_action=selectModifyChannel">Modify a currently published channel</a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template name="selectChannelType">
    <xsl:call-template name="workflow"/>
    <!-- form begin -->
    <form name="workflow" method="post" action="{$baseActionURL}">
      <input type="hidden" name="uPCM_action" value="none"/>
      <input type="hidden" name="uPCM_capture" value="selectChannelType"/>
      <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
        <tr class="uportal-channel-text">
          <td>
            <strong>Channel Type:</strong> Select the type of channel to add by clicking a select icon in the option column</td>
        </tr>
        <tr>
          <td>
            <table width="100%" border="0" cellpadding="2" class="uportal-background-content" cellspacing="0">
              <tr>
                <td nowrap="nowrap" class="uportal-channel-table-header">Option</td>
                <td nowrap="nowrap" class="uportal-channel-table-header">
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                </td>
                <td nowrap="nowrap" class="uportal-channel-table-header">Channel Type</td>
                <td nowrap="nowrap" class="uportal-channel-table-header">
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                </td>
                <td width="100%" class="uportal-channel-table-header">Description</td>
              </tr>
              <tr class="uportal-channel-text" valign="top">
                <td nowrap="nowrap" colspan="5">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <xsl:for-each select="//selectChannelType//channelType">
                <tr class="uportal-channel-text" valign="top">
                  <td nowrap="nowrap" align="center">
                    <input type="radio" name="ID" value="{@ID}" checked="checked"/> </td>
                  <td nowrap="nowrap">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                  </td>
                  <td nowrap="nowrap">
                    <strong>
                      <xsl:value-of select="name"/>
                    </strong>
                  </td>
                  <td nowrap="nowrap">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                  </td>
                  <td width="100%">
                    <xsl:value-of select="description"/>
                  </td>
                </tr>
                <tr class="uportal-channel-text" valign="top">
                  <td colspan="5" align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </xsl:for-each>
            </table>
          </td>
        </tr>
        <tr>
          <td>
            <input type="submit" name="uPCM_submit" value="Next &gt;" onclick="document.workflow.uPCM_action.value='selectGeneralSettings'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Review" onclick="document.workflow.uPCM_action.value='selectReviewChannel'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Cancel" onclick="document.workflow.uPCM_action.value='cancel'" class="uportal-button"/> </td>
        </tr>
      </table>
    </form>
    <!-- form end -->
  </xsl:template>

  <xsl:template name="selectModifyChannel">

    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
      <tr>
        <td>
          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-channel-text">
            <tr class="uportal-channel-strong" valign="top">
              <td colspan="2">Modify a currently published channel:</td>
            </tr>
            <tr valign="top">
              <td colspan="2">Select an option by clicking one of the icons in the table below.</td>
            </tr>
            <tr class="uportal-channel-text" valign="top">
              <td colspan="2">
                <a href="#">
                </a>
                <hr/>
              </td>
            </tr>
            <tr valign="top">
              <td>
                <img alt="interface image" src="{$mediaPath}/bullet.gif" width="16" height="16"/>
              </td>
              <td class="uportal-channel-text" width="100%">
                <a href="{$baseActionURL}?uPCM_action=cancel">Cancel and return</a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <br/>
    <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-light">
      <tr class="uportal-channel-text" valign="top">
          <td nowrap="nowrap">
            <xsl:call-template name="pagingWidget">
              <xsl:with-param name="i" select="1"/>
            </xsl:call-template>
          </td>
          <td width="100%" class="uportal-background-med">Filter by category:<xsl:for-each select="//*[@ID = $filterByID]"><xsl:for-each select="ancestor::category"><a class="uportal-navigation-category-selected"><xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?uPCM_action=filterByCategory&amp;newCategory=<xsl:value-of select="@ID"/></xsl:attribute>
                  <em>
                    <xsl:value-of select="@name"/>
                  </em></a>::</xsl:for-each>
              <a href="#" class="uportal-navigation-category-selected">
                <em>
                  <xsl:value-of select="@name"/>
                </em>
              </a>--<select name="newCategory" class="uportal-input-text" size="1"><option value="{@ID}" selected="selected"/><xsl:for-each select="child::category"><option value="{@ID}"><xsl:value-of select="@name"/></option></xsl:for-each>
                <option value=" ">_________</option>
                <option>
                  <xsl:attribute name="value">
                    <xsl:value-of select="//registry[1]/@ID"/>
                  </xsl:attribute>No Filter</option></select>
              <input type="submit" name="uPCM_submit" value="go" class="uportal-input-text"/></xsl:for-each>
          </td>
      </tr>
      <tr>
        <td colspan="2">
          <table width="100%" border="0" cellpadding="2" class="uportal-background-content" cellspacing="0">
            <xsl:choose>
              <xsl:when test="(//*[@ID = $filterByID]//channel)">
                <tr class="uportal-channel-table-header">
                  <td colspan="3" align="center" valign="top">Option</td>
                  <td nowrap="nowrap" valign="top">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                  </td>
                  <td nowrap="nowrap" valign="top">Channel Name</td>
                  <td valign="top">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                  </td>
                  <td width="100%" valign="top">Description</td>
                </tr>
                <tr class="uportal-channel-text" valign="top">
                  <td nowrap="nowrap" colspan="7">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="2"/>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <xsl:for-each select="(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)])">
                  <xsl:sort select="@name"/>
                  <xsl:if test="(position() &gt; (//recordsPerPage * //currentPage)-//recordsPerPage) and (position() &lt;= //recordsPerPage * //currentPage)">
                    <tr class="uportal-channel-text" valign="top">
                      <td nowrap="nowrap" align="center">
                        <a href="{$baseActionURL}?uPCM_action=reviewChannelSettings&amp;channelID={@ID}">
                          <img src="{$mediaPath}/view.gif" width="16" height="16" border="0" alt="Review settings for {@ID}"/>
                        </a>
                      </td>
                      <td nowrap="nowrap" align="center">
                        <a href="{$baseActionURL}?uPCM_action=editChannelSettings&amp;channelID={@ID}">
                          <img src="{$mediaPath}/edit.gif" width="16" height="16" border="0" alt="Edit settings for {@ID}"/>
                        </a>
                      </td>
                      <td nowrap="nowrap" align="center">
                        <a href="{$baseActionURL}?uPCM_action=removePublishedChannel&amp;channelID={@ID}">
                          <img src="{$mediaPath}/remove.gif" width="16" height="16" border="0" alt="Remove published channel - {@ID}"/>
                        </a>
                      </td>
                      <td valign="top">
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                      <td nowrap="nowrap" valign="top">
                        <strong>
                          <xsl:value-of select="@name"/>
                        </strong>
                      </td>
                      <td valign="top">
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                      <td width="100%" valign="top">
                        <xsl:value-of select="@description"/>
                      </td>
                    </tr>
                    <tr class="uportal-channel-text" valign="top">
                      <td colspan="7" align="center">
                        <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                          <tr>
                            <td>
                              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </xsl:if>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <tr class="uportal-channel-table-header">
                  <td colspan="3" valign="top" nowrap="nowrap">
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>No channels to display</td>
                </tr>
              </xsl:otherwise>
            </xsl:choose>
          </table>
        </td>
      </tr>
      <tr class="uportal-channel-text">
        <form name="formRecordsDisplayed" method="post" action="{$baseActionURL}">
          <input type="hidden" name="uPCM_action" value="changeRecordsPerPage"/>
          <td nowrap="nowrap" valign="top">
            <xsl:call-template name="pagingWidget">
              <xsl:with-param name="i" select="1"/>
            </xsl:call-template>
          </td>
          <td width="100%" class="uportal-background-med" valign="top">Display<input type="text" name="recordsPerPage" size="2" class="uportal-input-text"><xsl:attribute name="value"><xsl:value-of select="//recordsPerPage"/></xsl:attribute></input>records at a time.<img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/><input type="submit" name="buttonRecordsDisplayed" value="go" class="uportal-button"/></td>
        </form>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="pagingWidget">
    <xsl:param name="i"/>
    <xsl:if test="$i &lt;= ceiling(count(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1])">
      <xsl:if test="$i = 1">page:</xsl:if>
      <xsl:if test="$i = 1 and //currentPage[1]=1">
        <img src="{$mediaPath}/arrow_left_off.gif" width="16" height="16" alt="Previous" border="0"/>
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="4"/>
      </xsl:if>
      <xsl:if test="$i = 1 and //currentPage[1] &gt; 1">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$baseActionURL"/>?uPCM_action=changePage&amp;newPage=<xsl:value-of select="(//currentPage)-1"/></xsl:attribute>
          <img src="{$mediaPath}/arrow_left.gif" width="16" height="16" alt="Go to page [{(//currentPage)-1}]" border="0"/>
        </a>
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="4"/>
      </xsl:if>
      <xsl:if test="$i = //currentPage[1]">
        <strong>
          <xsl:value-of select="$i"/>
        </strong>
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="4"/>
      </xsl:if>
      <xsl:if test="$i != //currentPage[1]">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$baseActionURL"/>?uPCM_action=changePage&amp;newPage=<xsl:value-of select="$i"/></xsl:attribute>
          <xsl:value-of select="$i"/>
        </a>
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="4"/>
      </xsl:if>
      <xsl:if test="$i = ceiling(count(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1]) and //currentPage[1]=ceiling(count(//*[@ID =   $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1])">
        <img src="{$mediaPath}/arrow_right_off.gif" width="16" height="16" alt="Next" border="0"/>
      </xsl:if>
      <xsl:if test="$i = ceiling(count(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1]) and //currentPage[1] &lt;   ceiling(count(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1])">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$baseActionURL"/>?uPCM_action=changePage&amp;newPage=<xsl:value-of select="(//currentPage)+1"/></xsl:attribute>
          <img src="{$mediaPath}/arrow_right.gif" width="16" height="16" alt="Go to page [{(//currentPage)+1}]" border="0"/>
        </a>
      </xsl:if>
      <xsl:call-template name="pagingWidget">
        <xsl:with-param name="i" select="$i + 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template name="workflow">
    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
      <tr>
        <td>
          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-channel-text">

            <tr class="uportal-channel-strong" valign="top">
              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
            </tr>
            <tr valign="top">
              <td colspan="2">
                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                  <tr>
                    <td class="uportal-channel-table-header" nowrap="nowrap">Workflow:<img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10"/></td>
                    <xsl:apply-templates select="manageChannels/*"/>
                  </tr>
                </table>
              </td>
            </tr>
            <tr class="uportal-channel-text" valign="top">
              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <br/>
  </xsl:template>
  <xsl:template match="manageChannels/*">
    <xsl:if test="name(.)=$action and ./params/step/ID = $stepID">
      <xsl:for-each select="preceding::step">
        <xsl:if test="position() != 1">
          <td width="{round(100 div count(//step))}%">
            <table border="0" cellspacing="0" cellpadding="0" width="100%">
              <tr>
                <td class="uportal-background-shadow">
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                </td>
              </tr>
            </table>
          </td>
        </xsl:if>
        <td>
          <table border="0" cellspacing="0" cellpadding="1" class="uportal-background-shadow">
            <tr>
              <td>
                <table border="0" cellspacing="0" cellpadding="2" class="uportal-background-med">
                  <tr>
                    <td class="uportal-text-small" align="center">
                      <a href="#"><xsl:attribute name="onclick">document.workflow.uPCM_action.value='<xsl:value-of select="name(../../.)"/>';<xsl:if test="name(../../.) = 'channelDef'">document.workflow.uPCM_step.value='<xsl:value-of select="ID"/>';</xsl:if>document.workflow.submit()</xsl:attribute>
                      <xsl:choose>
                      <xsl:when test="name !=''">
                      <xsl:value-of select="name"/>
                      </xsl:when><xsl:otherwise>Channel Parameters</xsl:otherwise></xsl:choose>
                      </a>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </xsl:for-each>
      <xsl:if test="position() != 1">
        <td width="{round(100 div count(//step))}%">
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td class="uportal-background-shadow">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
              </td>
            </tr>
          </table>
        </td>
      </xsl:if>
      <td>
        <img alt="interface image" src="{$mediaPath}/arrow_right_timeline.gif"/>
      </td>
      <td>
        <table border="0" cellspacing="0" cellpadding="1" class="uportal-background-dark">
          <tr>
            <td>
              <table border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">
                <tr>
                  <td class="uportal-text-small" align="center">
                      <a href="#"><xsl:attribute name="onclick">document.workflow.uPCM_action.value='<xsl:value-of select="name(.)"/>';<xsl:if test="name(.) = 'channelDef'">document.workflow.uPCM_step.value='<xsl:value-of select=".//step/ID"/>';</xsl:if>document.workflow.submit()</xsl:attribute>
                        <xsl:choose>
                        <xsl:when test=".//step/name != ''">
                        <xsl:value-of select=".//step/name"/>
                        </xsl:when><xsl:otherwise>Channel Parameters</xsl:otherwise></xsl:choose>
                    </a> </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
      <xsl:for-each select="following::step">
        <td width="{round(100 div count(//step))}%">
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td class="uportal-background-med">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
              </td>
            </tr>
          </table>
        </td>
        <td>
          <table border="0" cellspacing="0" cellpadding="1" class="uportal-background-dark">
            <tr>
              <td>
                <table border="0" cellspacing="0" cellpadding="2" class="uportal-background-light" width="8">
                  <tr>
                    <td class="uportal-text-small" align="center">
                      <a href="#"><xsl:attribute name="onclick">document.workflow.uPCM_action.value='<xsl:value-of select="name(../../.)"/>';<xsl:if test="name(../../.) = 'channelDef'">document.workflow.uPCM_step.value='<xsl:value-of select="ID"/>';</xsl:if>document.workflow.submit()</xsl:attribute>
                        <xsl:choose>
                        <xsl:when test="name !=''">
                        <xsl:value-of select="name"/>
                        </xsl:when><xsl:otherwise>Channel Parameters</xsl:otherwise></xsl:choose>

                      </a> </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="selectGeneralSettings">
    <xsl:call-template name="workflow"/>
    <!-- form begin -->
    <form name="workflow" method="post" action="{$baseActionURL}">
      <input type="hidden" name="uPCM_action" value="none"/>
      <input type="hidden" name="uPCM_capture" value="selectGeneralSettings"/>
      <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
        <tr class="uportal-channel-text">
          <td>
            <strong>Settings [one]:</strong> Complete the Settings form below</td>
        </tr>
        <tr>
          <td>
            <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">
              <tr class="uportal-channel-table-header" valign="bottom">
                <td align="center" nowrap="nowrap">User can<br/> Modify?</td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                </td>
                <td width="100%">General Settings</td>
              </tr>
              <tr class="uportal-channel-table-header">
                <td align="center" colspan="3">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr>
                <td align="center" valign="top">
                  <input type="checkbox" name="modifyName" value="checkbox"/> </td>
                <td>
                </td>
                <td>
                  <span class="uportal-label">Channel Name:</span> <span class="uportal-text-small">[example - StockCharts]<br/> <input type="text" name="name" size="50" class="uportal-input-text"/></span> </td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top" colspan="3">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr>
                <td align="center" valign="top">
                  <input type="checkbox" name="modifyTimeout" value="checkbox"/> </td>
                <td>
                </td>
                <td>
                  <span class="uportal-label">Channel Timeout:</span> <br/> <input type="text" name="timeout" size="6" class="uportal-input-text"/>milliseconds (1000 = 1 second)</td>
              </tr>
              <tr>
                <td colspan="3">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td>
            <input type="submit" name="uPCM_submit" value="&lt; Back" onclick="document.workflow.uPCM_action.value='selectChannelType'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Next &gt;" onclick="document.workflow.uPCM_action.value='channelDef'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Review" onclick="document.workflow.uPCM_action.value='selectReviewChannel'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Cancel" onclick="document.workflow.uPCM_action.value='cancel'" class="uportal-button"/> </td>
        </tr>
      </table>
    </form>
    <!-- form end -->
  </xsl:template>

  <xsl:template name="beginChannelDef">
    <xsl:call-template name="workflow"/>
    <xsl:apply-templates select="manageChannels/channelDef[1]" mode="dynamicSettings"/>
  </xsl:template>

  <xsl:template match="channelDef" mode="dynamicSettings">
    <!-- form begin -->
    <form name="workflow" method="post" action="{$baseActionURL}">
      <input type="hidden" name="uPCM_action" value="changeMe"/>
      <input type="hidden" name="uPCM_capture" value="channelDef"/>
      <input type="hidden" name="uPCM_step" value="changeMe"/>
      <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
        <tr class="uportal-channel-text">
          <td>
            <strong>
              <xsl:value-of select="params/step/name"/>:</strong>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="8" height="8"/>
            <xsl:value-of select="params/step/description"/>
          </td>
        </tr>
        <tr>
          <td>
            <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">
              <tr class="uportal-channel-table-header" valign="bottom">
                <td align="center" nowrap="nowrap">User can<br/> Modify?</td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                </td>
                <td width="100%">General Settings</td>
              </tr>
              <tr class="uportal-channel-table-header">
                <td align="center" colspan="3">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <xsl:apply-templates select="params/step[ID=$stepID]"/>
            </table>
          </td>
        </tr>
        <tr>
          <td>
            <input type="submit" name="uPCM_submit" value="&lt; Back" class="uportal-button">
              <xsl:attribute name="onclick">
                <xsl:choose>
                  <xsl:when test="$stepID = 1">document.workflow.uPCM_action.value='selectGeneralSettings';document.workflow.uPCM_step.value='<xsl:value-of select="$stepID"/>'</xsl:when>
                  <xsl:otherwise>document.workflow.uPCM_action.value='channelDef';document.workflow.uPCM_step.value='<xsl:value-of select="number($stepID) - 1"/>'</xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
            </input>
            <input type="submit" name="uPCM_submit" value="Next &gt;" class="uportal-button">
              <xsl:attribute name="onclick">
                <xsl:choose>
                  <xsl:when test="$stepID = count(params/step)">document.workflow.uPCM_action.value='selectControls';document.workflow.uPCM_step.value='<xsl:value-of select="$stepID"/>'</xsl:when>
                  <xsl:otherwise>document.workflow.uPCM_action.value='channelDef';document.workflow.uPCM_step.value='<xsl:value-of select="number($stepID) + 1"/>'</xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
            </input>
            <input type="submit" name="uPCM_submit" value="Review" onclick="document.workflow.uPCM_action.value='selectReviewChannel'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Cancel" onclick="document.workflow.uPCM_action.value='cancel'" class="uportal-button"/> </td>
        </tr>
      </table>
    </form>
    <!-- form end -->
  </xsl:template>

  <!-- The current step info-->
  <xsl:template match="step">
    <xsl:apply-templates select="parameter"/>
  </xsl:template>

  <!-- Display the parameters that are NOT subscribe-only-->
  <xsl:template match="parameter">
    <xsl:if test="@modify != 'subscribe-only'">
      <tr>
        <xsl:choose>
          <xsl:when test="type/@input='text'">
            <xsl:call-template name="text"/>
          </xsl:when>
          <xsl:when test="type/@input='single-choice'">
            <xsl:call-template name="single-choice"/>
          </xsl:when>
          <xsl:when test="type/@input='multi-choice'">
            <xsl:call-template name="multi-choice"/>
          </xsl:when>
        </xsl:choose>
      </tr>
    </xsl:if>
  </xsl:template>
  <!-- displays checkbox for publisher to allow subscribe time modification-->
  <xsl:template name="subscribe">
    <td align="center" valign="top">
      <!-- <xsl:value-of select="@modify"/>just for debug -->
      <xsl:choose>
        <xsl:when test="@modify!='publish-only'">
          <input type="checkbox" name="{name}_sub">
            <xsl:if test="@modify='subscribe'">
              <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
          </input>
        </xsl:when>
        <xsl:otherwise>
          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="8" height="8"/>
        </xsl:otherwise>
      </xsl:choose>
    </td>
    <td/>
  </xsl:template>
  <!-- display all the input fields with a base type of 'single-choice'-->
  <xsl:template name="single-choice">
    <xsl:choose>
      <xsl:when test="type/@display='drop-down'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <select name="{name}" class="uportal-input-text">
            <xsl:for-each select="type/restriction/value">
              <option value="{.}">
                <xsl:if test="@default='true'">
                  <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:choose>
                  <xsl:when test="@display">
                    <xsl:value-of select="@display"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </option>
            </xsl:for-each>
          </select>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='radio'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <xsl:for-each select="type/restriction/value">
            <input type="radio" name="{name}" value="{.}" class="uportal-input-text">
              <xsl:if test="@default='true'">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:choose>
              <xsl:when test="@display">
                <xsl:value-of select="@display"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="."/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </td>
      </xsl:when>
      <xsl:otherwise>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <select name="{name}" class="uportal-input-text">
            <xsl:for-each select="type/restriction/value">
              <xsl:call-template name="subscribe"/>
              <option value="{.}">
                <xsl:if test="@default='true'">
                  <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:choose>
                  <xsl:when test="@display">
                    <xsl:value-of select="@display"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </option>
            </xsl:for-each>
          </select>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- display all the input fields with a base type of 'multi-choice'-->
  <xsl:template name="multi-choice">
    <xsl:choose>
      <xsl:when test="type/@display='select-list'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <select name="{name}" size="6" multiple="multiple" class="uportal-input-text">
            <xsl:for-each select="type/restriction/value">
              <option value="{.}">
                <xsl:if test="@default='true'">
                  <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:choose>
                  <xsl:when test="@display">
                    <xsl:value-of select="@display"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </option>
            </xsl:for-each>
          </select>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='checkbox'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <xsl:for-each select="type/restriction/value">
            <input type="checkbox" name="{name}" value="{.}">
              <xsl:if test="@default='true'">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:choose>
              <xsl:when test="@display">
                <xsl:value-of select="@display"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="."/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </td>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <select name="{name}" size="6" multiple="multiple" class="uportal-input-text">
            <xsl:for-each select="type/restriction/value">
              <option value="{.}">
                <xsl:if test="@default='true'">
                  <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:choose>
                  <xsl:when test="@display">
                    <xsl:value-of select="@display"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </option>
            </xsl:for-each>
          </select>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- display all the input fields with a base type of 'text'-->
  <xsl:template name="text">
    <!-- since length and maxlength are not required test existence and use defaults if needed -->
    <xsl:variable name="length">
      <xsl:choose>
        <xsl:when test="type/length"> <xsl:value-of select="type/length"/> </xsl:when>
        <xsl:otherwise> <xsl:value-of select="$defaultLength"/> </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="maxlength">
      <xsl:choose>
        <xsl:when test="type/maxlength"> <xsl:value-of select="type/maxlength"/> </xsl:when>
        <xsl:otherwise> <xsl:value-of select="$defaultMaxLength"/> </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="type/@display='text'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <input type="text" name="{name}" value="{defaultValue}" maxlength="{$maxlength}" size="{$length}" class="uportal-input-text"/>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='textarea'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <textarea rows="{$defaultTextRows}" cols="{$defaultTextCols}" class="uportal-input-text">
            <xsl:value-of select="defaultValue"/>
          </textarea>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='hidden'">
        <input type="hidden" name="{name}" value="{defaultValue}"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <input type="text" name="{name}" value="{defaultValue}" maxlength="{$maxlength}" size="{$length}" class="uportal-input-text"/>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="label">
    <span class="uportal-label">
      <xsl:value-of select="."/>:</span>
  </xsl:template>

  <xsl:template match="example">
    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="8" height="8"/>
    <span class="uportal-text-small">[example - <xsl:value-of select="."/>]</span>
  </xsl:template>

  <xsl:template name="selectControls">
    <xsl:call-template name="workflow"/>
    <!-- form begin -->
    <form name="workflow" method="post" action="{$baseActionURL}">
      <input type="hidden" name="uPCM_action" value="changeMe"/>
      <input type="hidden" name="uPCM_capture" value="selectControls"/>

      <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
        <tr class="uportal-channel-text">
          <td>
            <strong>Channel Controls:</strong>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="4"/>Select channel controls in the form below</td>
        </tr>
        <tr>
          <td>
            <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">
              <tr class="uportal-channel-table-header">
                <td align="center">Select</td>
                <td align="center">
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                </td>
                <td nowrap="nowrap" align="center">Channel Controls</td>
                <td nowrap="nowrap" align="center">
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                </td>
                <td nowrap="nowrap" align="center">Icon</td>
                <td align="center">
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                </td>
                <td width="100%">Description</td>
              </tr>
              <tr class="uportal-channel-table-header">
                <td align="center" colspan="7">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top">
                  <input type="checkbox" name="minimizable" value="true"/> </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>
                  <strong>Minimizable</strong>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                </td>
                <td align="center">
                  <strong>
                    <img alt="interface image" src="{$mediaPath}/min.gif" width="16" height="16"/>
                  </strong>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>when selected, channel controls remain but channel content does not render</td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top" colspan="7">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top">
                  <input type="checkbox" name="editable" value="true"/> </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>
                  <strong>Editable</strong>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td align="center">
                  <img alt="interface image" src="{$mediaPath}/edit.gif" width="16" height="16"/>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>when selected, passes edit events</td>
              </tr>
              <tr>
                <td colspan="7">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top">
                  <input type="checkbox" name="hasHelp" value="true"/> </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>
                  <strong>Has Help</strong>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td align="center">
                  <img alt="interface image" src="{$mediaPath}/help.gif" width="16" height="16"/>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>when selected, passes help events</td>
              </tr>
              <tr>
                <td align="center" valign="top" colspan="7">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top">
                  <input type="checkbox" name="hasAbout" value="true"/> </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>
                  <strong>Has About</strong>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td align="center">
                  <img alt="interface image" src="{$mediaPath}/about.gif" width="16" height="16"/>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>when selected, passes about events</td>
              </tr>
              <tr>
                <td align="center" valign="top" colspan="7">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top">
                  <input type="checkbox" name="printable" value="true"/> </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>
                  <strong>Printable</strong>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td align="center">
                  <img alt="interface image" src="{$mediaPath}/print.gif" width="16" height="16"/>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>when selected, passes print events</td>
              </tr>
              <tr>
                <td align="center" valign="top" colspan="7">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top">
                  <input type="checkbox" name="removable" value="true"/> </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>
                  <strong>Removable</strong>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td align="center">
                  <img alt="interface image" src="{$mediaPath}/remove.gif" width="16" height="16"/>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>when selected, removes the channel from the layout</td>
              </tr>
              <tr>
                <td align="center" valign="top" colspan="7">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                    <tr>
                      <td>
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr class="uportal-channel-text">
                <td align="center" valign="top">
                  <input type="checkbox" name="detachable" value="true"/> </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>
                  <strong>Detachable</strong>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td align="center">
                  <img alt="interface image" src="{$mediaPath}/detach.gif" width="16" height="16"/>
                </td>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                </td>
                <td>when selected, renders the channel in a separate window</td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td>
            <input type="submit" name="uPCM_submit" value="&lt; Back" onclick="document.workflow.uPCM_action.value='channelDef'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Next &gt;" onclick="document.workflow.uPCM_action.value='selectCategories'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Review" onclick="document.workflow.uPCM_action.value='selectReviewChannel'" class="uportal-button"/>
            <input type="submit" name="uPCM_submit" value="Cancel" onclick="document.workflow.uPCM_action.value='cancel'" class="uportal-button"/> </td>
        </tr>
      </table>
    </form>
    <!-- form end -->
  </xsl:template>

  <xsl:template name="selectCategories">
    <xsl:variable name="catID">
      <xsl:value-of select="//browsingCategory[1]"/>
    </xsl:variable>

    <xsl:call-template name="workflow"/>

    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
      <tr class="uportal-channel-text">

        <td>
          <strong>Categories:</strong> Browse and add the categories in which this channel will appear</td>
      </tr>



      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">

            <tr valign="top">

              <td class="uportal-label">
                <!--
                <table width="100%" border="0">

                  <tr>

                    <td nowrap="nowrap"><img alt="interface image" src="{$mediaPath}/arrow_down.gif" width="16" height="16" /><select name="select4" class="uportal-input-text">

                      <option value="all">

                        Select All

                      </option>



                      <option value="cat02" selected="selected">

                        Applications

                      </option>



                      <option value="cat03">

                        Development

                      </option>



                      <option value="cat01">

                        News1

                      </option>

                    </select> <input type="submit" name="uPCM_submit" value="go" class="uportal-button" /> <input type="submit" name="uPCM_submit" value="add >>" class="uportal-button" /> </td>

                  </tr>

                </table>-->
                <!--<table width="100%" border="0">

                  <tr>

                    <td nowrap="nowrap"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16" /><img alt="interface image" src="{$mediaPath}/arrow_right.gif" width="16" height="16" /><select name="select5" class="uportal-input-text">

                      <option value="all">

                        Select All

                      </option>



                      <option value="cat02">

                        Applications

                      </option>



                      <option value="cat03">

                        Development

                      </option>



                      <option value="cat01" selected="selected">

                        News1

                      </option>

                    </select> <input type="submit" name="uPCM_submit" value="go" class="uportal-button" /> <input type="submit" name="Submit72" value="add" class="uportal-button" /> </td>

                  </tr>

                </table>
-->
                <!--Begin Steps table -->
                <table width="100%" border="0" class="uportal-channel-text">
                  <xsl:choose>
                    <xsl:when test="//registry">
                      <tr>
                        <td align="left" valign="top">
                          <xsl:choose>
                            <xsl:when test="$catID = 'top' or $catID = 'all'">

                              <form name="selectCategory" method="post" action="{$baseActionURL}">
                                <input type="hidden" name="uPCM_action" value="selectCategories"/>
                                <input type="hidden" name="uPCM_capture" value="selectCategories"/>

                              <table width="100%" border="0">
                                  <tr>
                                    <td nowrap="nowrap" align="left" valign="top">
                                      <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
                                      <img alt="interface image" src="{$mediaPath}/arrow_right.gif" width="16" height="16"/>
                                      <select name="selectedCategory" class="uportal-input-text">
                                        <option value=" " selected="selected"/>
                                        <xsl:for-each select="//registry/category">
                                          <xsl:sort select="@name"/>
                                          <option value="{@ID}">
                                            <xsl:value-of select="@name"/>
                                          </option>
                                        </xsl:for-each>
                                        <!--                                  <option value=" ">__________</option>
                                  <xsl:choose>
                                    <xsl:when test="$catID = 'all'">
                                      <option value="all" selected="selected">Select All</option>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <option value="all">Select All</option>
                                      <option value=" " selected="selected"/>
                                    </xsl:otherwise>
                                  </xsl:choose>
-->                                </select>
                                      <input type="submit" name="uPCM_browse" value="go" class="uportal-button"/>
                                      <input type="submit" name="uPCM_select" value="add" class="uportal-button"/>
                                    </td>
                                  </tr>
                              </table>
                              </form>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:for-each select="//registry//category[@ID=$catID]">
                                <xsl:for-each select="ancestor-or-self::category">

                                  <form name="selectCategory" method="post" action="{$baseActionURL}">
                                    <input type="hidden" name="uPCM_action" value="selectCategories"/>
                                    <input type="hidden" name="uPCM_capture" value="selectCategories"/>

                                  <table width="100%" border="0">
                                      <tr>
                                        <td nowrap="nowrap" align="left" valign="top">
                                          <img alt="interface image" src="{$mediaPath}/transparent.gif" height="16">
                                            <xsl:attribute name="width">
                                              <xsl:value-of select="(count(ancestor::category)+1)*16"/>
                                            </xsl:attribute>
                                          </img>
                                          <xsl:choose>
                                            <xsl:when test="position() = last()">
                                              <img alt="interface image" src="{$mediaPath}/arrow_right.gif" width="16" height="16"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                              <img alt="interface image" src="{$mediaPath}/arrow_down.gif" width="16" height="16"/>
                                            </xsl:otherwise>
                                          </xsl:choose>
                                          <select name="selectedCategory" class="uportal-input-text">
                                            <xsl:for-each select="ancestor::*[1]/category">
                                              <xsl:sort select="@name"/>
                                              <option value="{@ID}">
                                                <xsl:if test="@ID=$catID or descendant::category[@ID=$catID]">
                                                  <xsl:attribute name="selected">selected</xsl:attribute>
                                                </xsl:if>
                                                <xsl:value-of select="@name"/>
                                                <!--[subcategories:<xsl:value-of select="count(descendant::category)"/>, total channels:<xsl:value-of select="count(descendant::channel)"/>-->
                                              </option>
                                            </xsl:for-each>
                                            <!--                                      <xsl:if test="position() = 1">
                                        <option value=" ">_____________</option>
                                        <option value="all">Select All</option>
                                      </xsl:if>
-->                                    </select>
                                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
                                          <input type="submit" name="uPCM_browse" value="go" class="uportal-button"/>
                                          <input type="submit" name="uPCM_select" value="add" class="uportal-button"/>
                                        </td>
                                      </tr>
                                  </table>
                                  </form>
                                </xsl:for-each>
                                <xsl:if test="child::category">
                                  <table width="100%" border="0" class="uportal-channel-text">
                                    <tr>
                                      <td colspan="2">
                                        <hr/>
                                      </td>
                                    </tr>
                                  </table>
                                  <form name="selectCategory" method="post" action="{$baseActionURL}">
                                    <input type="hidden" name="uPCM_action" value="selectCategories"/>
                                    <input type="hidden" name="uPCM_capture" value="selectCategories"/>
                                  <table width="100%" border="0">
                                      <tr>
                                        <td nowrap="nowrap" align="left" valign="top">
                                          <img alt="interface image" src="{$mediaPath}/bullet.gif" width="16" height="16"/>
                                          <select name="selectedCategory" class="uportal-input-text">
                                            <xsl:for-each select="category">
                                              <xsl:sort select="@name"/>
                                              <option value="{@ID}">
                                                <xsl:value-of select="@name"/>
                                                <!--[subcategories:<xsl:value-of select="count(descendant::category)"/>, total channels:<xsl:value-of select="count(descendant::channel)"/>-->
                                              </option>
                                            </xsl:for-each>
                                            <option value=" ">____________________</option>
                                            <option value=" " selected="selected">Select a subcategory</option>
                                          </select>
                                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
                                          <input type="submit" name="uPCM_browse" value="go" class="uportal-button"/>
                                          <input type="submit" name="uPCM_select" value="add" class="uportal-button"/>
                                        </td>
                                      </tr>
                                  </table>
                                  </form>
                                </xsl:if>
                              </xsl:for-each>
                            </xsl:otherwise>
                          </xsl:choose>
                          <!--End Category Selection Table -->
                        </td>
                        <!--                  <td>
                    <img alt="interface image" src="{$mediaPath}/transparent.gif" width="32" height="16"/>
                  </td>
                  <td width="100%">
                    <xsl:if test="$catID != 'top'">
                      <table width="100%" border="0" class="uportal-channel-text">
                          <tr valign="top">
                            <td>
                              <strong>2.</strong>
                            </td>
                            <td width="100%">Select a channel<xsl:choose>
                                <xsl:when test="$catID = 'all'">from "All catagories"</xsl:when>
                                <xsl:otherwise> from the "<xsl:value-of select="//category[@ID=$catID]/@name"/>" category
                                <br/><span class="uportal-text-small">Description: <xsl:value-of select="//category[@ID=$catID]/@description"/></span>
                                </xsl:otherwise></xsl:choose>
                            </td>
                          </tr>
                          <tr>
                            <td>
                              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                            </td>
                            Begin Channel Listing
                            <td width="100%">
                              <select name="selectedChannel" size="5" class="uportal-input-text">
                                <xsl:choose>
                                  <xsl:when test="$catID = 'all'">
                                    <xsl:for-each select="//registry//channel[not(@ID=following::channel/@ID)]">
                                      <xsl:sort select="@name"/>
                                      <option value="{@ID}">
                                        <xsl:value-of select="@name"/>
                                      </option>
                                    </xsl:for-each>
                                    <option>
                                      <xsl:if test="not(//registry//channel[not(@ID=following::channel/@ID)])">-This category contains no channels-</xsl:if>
                                    </option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <xsl:for-each select="//registry//category[@ID=$catID]/channel">
                                      <xsl:sort select="@name"/>
                                      <option value="{@ID}">
                                        <xsl:value-of select="@name"/>
                                      </option>
                                    </xsl:for-each>
                                    <option>
                                      <xsl:if test="not(//registry//category[@ID=$catID]/channel)">-This category contains no channels-</xsl:if>
                                    </option>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </select>
                            </td>
                            End Channel Listing
                          </tr>
                          <tr valign="top">
                            <td>
                              <strong>3.</strong>
                            </td>
                            <td>Get more informaton about the selected channel:<input type="submit" name="uPCM_submit" value="?" class="uportal-button"/> [optional]</td>
                          </tr>
                          <tr valign="top">
                            <td>
                              <strong>4.</strong>
                            </td>
                            <td>Add the selected channel:<input type="submit" name="uPCM_submit" value="Add" class="uportal-button"/></td>
                          </tr>
                      </table>
                    </xsl:if>
                  </td>-->
                      </tr>
                    </xsl:when>
                    <xsl:otherwise>
                      <tr>
                        <td colspan="3">
                          <hr/>
                        </td>
                      </tr>
                      <tr>
                        <td colspan="3" class="uportal-channel-warning">
                          <b>No Channel registry data is available at this time...</b>
                        </td>
                      </tr>
                    </xsl:otherwise>
                  </xsl:choose>
                </table>
              </td>



              <td>
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>



              <td class="uportal-background-light">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
              </td>



              <td width="100%">

                <table width="100%" border="0" cellpadding="2" class="uportal-background-content" cellspacing="0">

                  <tr>

                    <td nowrap="nowrap" class="uportal-channel-table-header">Option</td>



                    <td nowrap="nowrap" class="uportal-channel-table-header">
                      <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                    </td>



                    <td nowrap="nowrap" class="uportal-channel-table-header" width="50%">Selected Category</td>



                    <td nowrap="nowrap" class="uportal-channel-table-header">
                      <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                    </td>



                    <td width="50%" class="uportal-channel-table-header">Description</td>
                  </tr>



                  <tr class="uportal-channel-text" valign="top">

                    <td nowrap="nowrap" colspan="5">

                      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                        <tr>

                          <td>
                            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="2"/>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>



                  <xsl:for-each select="//registry//category[@ID = //selectedCategory]">

                    <tr class="uportal-channel-text" valign="top">
                      <td nowrap="nowrap" align="center">
                        <a href="#">
                          <img src="{$mediaPath}/remove.gif" width="16" height="16" border="0" alt="Remove channel from {@name} category"/>
                        </a>
                      </td>
                      <td nowrap="nowrap">
                        <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                      </td>
                      <td>

                        <xsl:for-each select="ancestor-or-self::category">

                          <xsl:choose>

                            <xsl:when test="position() != last()">
                              <em>
                                <xsl:value-of select="@name"/>
                              </em>
                              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="4"/>::<img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="4"/></xsl:when>

                            <xsl:otherwise>
                              <strong>
                                <xsl:value-of select="@name"/>
                              </strong>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:for-each>
                      </td>

                      <td nowrap="nowrap">
                        <img alt="interface image" src="{$mediaPath}/transparent.gif"/>
                      </td>



                      <td width="100%">
                        <xsl:value-of select="@description"/>
                      </td>
                    </tr>



                    <tr class="uportal-channel-text" valign="top">

                      <td colspan="5" align="center">

                        <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                          <tr>

                            <td>
                              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </xsl:for-each>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>

      <!-- form begin -->
      <form name="workflow" method="post" action="{$baseActionURL}">
        <input type="hidden" name="uPCM_action" value="changeMe"/>
        <input type="hidden" name="uPCM_capture" value="selectCategories"/>

      <tr>
        <td>
          <input type="submit" name="uPCM_submit" value="&lt; Back" onclick="document.workflow.uPCM_action.value='selectControls'" class="uportal-button"/>
          <input type="submit" name="uPCM_submit" value="Next &gt;" onclick="document.workflow.uPCM_action.value='selectRoles'" class="uportal-button"/>
          <input type="submit" name="uPCM_submit" value="Review" onclick="document.workflow.uPCM_action.value='selectReviewChannel'" class="uportal-button"/>
          <input type="submit" name="uPCM_submit" value="Cancel" onclick="document.workflow.uPCM_action.value='cancel'" class="uportal-button"/>
        </td>
      </tr>

      </form>
      <!-- form end -->

    </table>
  </xsl:template>

  <xsl:template name="selectRoles">
    <xsl:call-template name="workflow"/>

    <!-- form begin -->
    <form name="workflow" method="post" action="{$baseActionURL}">
      <input type="hidden" name="uPCM_action" value="changeMe"/>
      <input type="hidden" name="uPCM_capture" value="channelDef"/>
    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">

      <tr class="uportal-channel-text">

        <td>
          <strong>Roles:</strong> Select roles which are allowed access to this channel</td>
      </tr>



      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="4" class="uportal-background-content">

            <tr>

              <td>

                <table width="100%" border="0" cellpadding="2" class="uportal-background-content" cellspacing="0">

                  <tr>

                    <td nowrap="nowrap" class="uportal-channel-table-header">Select</td>



                    <td nowrap="nowrap" class="uportal-channel-table-header">
                      <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                    </td>



                    <td nowrap="nowrap" class="uportal-channel-table-header">Roles</td>



                    <td nowrap="nowrap" class="uportal-channel-table-header">
                      <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                    </td>



                    <td width="100%" class="uportal-channel-table-header">Description</td>
                  </tr>



                  <tr class="uportal-channel-text" valign="top">

                    <td nowrap="nowrap" colspan="5">

                      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                        <tr>

                          <td>
                            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="2"/>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <xsl:choose>
                    <xsl:when test="//selectRoles//role">
                      <xsl:for-each select="//selectRoles//role">


                        <tr class="uportal-channel-text" valign="top">

                          <td nowrap="nowrap" align="center">
                            <input type="checkbox" name="checkbox" value="checkbox"/> </td>



                          <td nowrap="nowrap">
                            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                          </td>



                          <td nowrap="nowrap">
                            <strong>
                              <xsl:value-of select="name"/>
                            </strong>
                          </td>



                          <td nowrap="nowrap">
                            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="8"/>
                          </td>



                          <td width="100%">
                            <xsl:value-of select="description"/>
                          </td>
                        </tr>



                        <tr class="uportal-channel-text" valign="top">

                          <td colspan="5" align="center">

                            <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                              <tr>

                                <td>
                                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                      <tr class="uportal-channel-text" valign="top">

                        <td colspan="5" align="left">There are no roles to display</td>
                      </tr>
                    </xsl:otherwise>
                  </xsl:choose>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>

      <tr>
        <td>
          <input type="submit" name="uPCM_submit" value="&lt; Back" onclick="document.workflow.uPCM_action.value='selectCategories'" class="uportal-button"/>
          <input type="submit" name="uPCM_submit" value="Next &gt;" onclick="document.workflow.uPCM_action.value='selectReviewChannel'" class="uportal-button"/>
          <input type="submit" name="uPCM_submit" value="Review" onclick="document.workflow.uPCM_action.value='selectReviewChannel'" class="uportal-button"/>
          <input type="submit" name="uPCM_submit" value="Cancel" onclick="document.workflow.uPCM_action.value='cancel'" class="uportal-button"/>
        </td>
      </tr>
    </table>
    </form>
    <!-- form end -->
  </xsl:template>

  <xsl:template name="reviewChannel">
      <xsl:call-template name="workflow"/>

<table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">

      <!-- form begin -->



      <tr class="uportal-channel-text">

        <td><strong>Review:</strong> Please review the settings for accuracy (click workflow icons to return to a specific settings)</td>

      </tr>



      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">

            <tr class="uportal-channel-text">

              <td nowrap="nowrap"><strong>Channel Type:</strong></td>



              <td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16" /></td>



              <td width="100%">RDF Site Summary [RSS]</td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>

                  </tr>

                </table>

              </td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap"><strong>RSS URL:</strong></td>



              <td></td>



              <td>http://www.stockcharts.com/rss/stockcharts.rss</td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>

                  </tr>

                </table>

              </td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap"><strong>RSS Version:</strong></td>



              <td></td>



              <td>0.91</td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>

                  </tr>

                </table>

              </td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap"><strong>Channel Name:</strong></td>



              <td></td>



              <td>StockCharts</td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>

                  </tr>

                </table>

              </td>

            </tr>



            <tr class="uportal-channel-text">


              <td nowrap="nowrap"><strong>Selected Categories:</strong></td>



              <td></td>



              <td><em>Applications</em> :: <em>News</em> ::<strong><span></span></strong><span>Good News</span></td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap"></td>



              <td></td>



              <td><em>Applications</em> :: <em>News</em> ::<strong><span></span></strong>Bad News</td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap"></td>



              <td></td>



              <td><em>Applications</em> :: <em>News</em> ::<strong><span></span></strong>Seven O'clock News</td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>

                  </tr>

                </table>

              </td>

            </tr>



            <tr class="uportal-channel-text">

              <td nowrap="nowrap"><strong>Selected Roles:</strong></td>



              <td></td>



              <td>Everyone</td>

            </tr>

          </table>

        </td>

      </tr>



      <tr>

        <td><input type="submit" name="Submit4232" value="&lt; Back" class="uportal-button" /> <input type="submit" name="Submit5232" value="Finished" class="uportal-button" /> </td>

      </tr>

      <!-- form end -->

    </table>
  </xsl:template>

<xsl:template name="customSettings">
	      <xsl:call-template name="workflow"/>
<table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">

      <!-- form begin -->



      <tr class="uportal-channel-text">

        <td><strong>Add Parameters:</strong> Complete the form below to add custom channel parameters</td>

      </tr>



      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">

            <tr valign="top">

              <td class="uportal-label">

                <table width="100%" border="0" cellspacing="0" cellpadding="4">

                  <tr class="uportal-label">

                    <td>Name:<br />

                     <input type="text" name="textfield4" class="uportal-input-text" /> </td>

                  </tr>



                  <tr class="uportal-label">

                    <td>Value:<br />

                     <input type="text" name="textfield5" class="uportal-input-text" /> </td>

                  </tr>



                  <tr class="uportal-label">

                    <td align="right"><input type="submit" name="Submit5" value="add" class="uportal-button" /></td>

                  </tr>

                </table>

              </td>



              <td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16" /></td>



              <td class="uportal-background-light"><img alt="interface image" src="transparent.gif" width="2" height="2" /></td>



              <td width="100%">

                <table width="100%" border="0" cellpadding="2" class="uportal-background-content" cellspacing="0">

                  <tr>

                    <td nowrap="nowrap" class="uportal-channel-table-header">Option</td>



                    <td nowrap="nowrap" class="uportal-channel-table-header"><img alt="interface image" src="transparent.gif" width="16" height="8" /></td>



                    <td nowrap="nowrap" class="uportal-channel-table-header">Name</td>



                    <td nowrap="nowrap" class="uportal-channel-table-header"><img alt="interface image" src="transparent.gif" width="8" height="8" /></td>



                    <td width="100%" class="uportal-channel-table-header">Value</td>

                  </tr>



                  <tr class="uportal-channel-text" valign="top">

                    <td nowrap="nowrap" colspan="5">

                      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                        <tr>

                          <td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="2" /></td>

                        </tr>

                      </table>

                    </td>

                  </tr>

<xsl:choose>
<xsl:when test="//customSettings//listedParameter">
<xsl:for-each select="//customSettings//listedParameter">

                  <tr class="uportal-channel-text" valign="top">

                    <td nowrap="nowrap" align="center"><a href="#">
                    <img src="{$mediaPath}/remove.gif" width="16" height="16" border="0" alt="Remove this parameter" /></a></td>



                    <td nowrap="nowrap"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>



                    <td nowrap="nowrap"><strong><xsl:value-of select="name"/>:</strong></td>



                    <td nowrap="nowrap"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>



                    <td width="100%"><xsl:value-of select="value"/></td>

                  </tr>



                  <tr class="uportal-channel-text" valign="top">

                    <td colspan="5" align="center">

                      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                        <tr>

                          <td><img alt="interface image" src="transparent.gif" width="1" height="1" /></td>

                        </tr>

                      </table>

                    </td>

                  </tr>

</xsl:for-each>
</xsl:when>
<xsl:otherwise>
                  <tr class="uportal-channel-text" valign="top">

                    <td colspan="5" align="left">

                     No custom parameters

                    </td>

                  </tr>

                  </xsl:otherwise>
                  </xsl:choose>



                </table>

              </td>

            </tr>

          </table>

        </td>

      </tr>



      <tr>

        <td><input type="submit" name="Submit4222" value="&lt; Back" class="uportal-button" /> <input type="submit" name="Submit3222" value="Next &gt;" class="uportal-button" /> <input type="submit" name="Submit5342" value="Review" class="uportal-button" /> <input type="submit" name="Submit842" value="Cancel" class="uportal-button" /> </td>

      </tr>

      <!-- form end -->

    </table>
</xsl:template>
</xsl:stylesheet>

<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
