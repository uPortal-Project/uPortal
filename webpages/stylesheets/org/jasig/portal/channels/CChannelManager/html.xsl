<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="no"/>
  <xsl:param name="baseActionURL">render.uP</xsl:param>

  <xsl:param name="action">defaultView</xsl:param>
  <xsl:param name="stepID">1</xsl:param>

  <xsl:param name="errorMessage">no parameter passed</xsl:param>
  <xsl:variable name="filterByID">
    <xsl:value-of select="//filterByID[1]"/>
  </xsl:variable>
  <!--xsl:variable name="mediaPath">C:\LaJolla\uPortal\webpages\media\org\jasig\portal\channels\CPublisher</xsl:variable-->
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CChannelManager</xsl:variable>
  <xsl:template match="/">
    <html>
      <head>
        <title>Untitled Document</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
        <link rel="stylesheet" href="C:\LaJolla\uPortal\webpages\media\org\jasig\portal\layout\tab-column\nested-tables\imm\imm.css" type="text/css"/>
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
                <a href="{$baseActionURL}?action=newChannel">Publish a new channel</a>
              </td>
            </tr>
            <tr valign="top">
              <td>
                <img alt="interface image" src="{$mediaPath}/bullet.gif" width="16" height="16"/>
              </td>
              <td class="uportal-channel-text" width="100%">
                <a href="{$baseActionURL}?action=selectModifyChannel">Modify a currently published channel</a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="selectChannelType">




    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">

      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-channel-text">

            <tr class="uportal-channel-strong" valign="top">

              <td colspan="2">Publish a new channel:</td>
            </tr>



            <tr class="uportal-channel-strong" valign="top">

              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
            </tr>



            <tr valign="top">

              <td colspan="2">

                <xsl:call-template name="workflow"/>
              </td>
            </tr>



            <tr class="uportal-channel-text" valign="top">

              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
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
                <a href="#ChannelName">Cancel and return</a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>

    <br/>
    <!-- form begin -->





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
                  <input type="radio" name="radiobutton" value="radiobutton" checked="checked"/> </td>



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
          <input type="submit" name="Submit33" value="Next &gt;" class="uportal-button"/> <input type="submit" name="Submit53" value="Review" class="uportal-button"/> <input type="submit" name="Submit8" value="Cancel" class="uportal-button"/> </td>
      </tr>
    </table>
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
                <a href="{$baseActionURL}?action=cancel">Cancel and return</a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <br/>
    <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-light">
      <tr class="uportal-channel-text" valign="top">
        <form name="formFilterByCategory" method="post" action="{$baseActionURL}">
          <td nowrap="nowrap">
            <xsl:call-template name="pagingWidget">
              <xsl:with-param name="i" select="1"/>
            </xsl:call-template>
          </td>
          <td width="100%" class="uportal-background-med">Filter by category:<xsl:for-each select="//*[@ID = $filterByID]">
              <xsl:for-each select="ancestor::category">
                <a class="uportal-navigation-category-selected">
                  <xsl:attribute name="href">
                    <xsl:value-of select="$baseActionURL"/>?action=changeCategory&amp;newCategory=<xsl:value-of select="@ID"/></xsl:attribute>
                  <em>
                    <xsl:value-of select="@name"/>
                  </em></a>::</xsl:for-each>
              <a href="#" class="uportal-navigation-category-selected">
                <em>
                  <xsl:value-of select="@name"/>
                </em>
              </a>--<select name="newCategory" class="uportal-input-text" size="1">
                <option value="{@ID}" selected="selected"/>
                <xsl:for-each select="child::category">
                  <option value="{@ID}">
                    <xsl:value-of select="@name"/>
                  </option></xsl:for-each>
                <option value=" ">_________</option>
                <option>
                  <xsl:attribute name="value">
                    <xsl:value-of select="//registry[1]/@ID"/>
                  </xsl:attribute>No Filter</option></select>
              <input type="submit" name="buttonFilterByCategory" value="go" class="uportal-input-text"/></xsl:for-each>
          </td>
        </form>
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
                        <a href="$baseActionURL?action=reviewChannelSettings&amp;channelID={@ID}">
                          <img src="{$mediaPath}/view.gif" width="16" height="16" border="0" alt="Review settings for {@ID}"/>
                        </a>
                      </td>
                      <td nowrap="nowrap" align="center">
                        <a href="$baseActionURL?action=editChannelSettings&amp;channelID={@ID}">
                          <img src="{$mediaPath}/edit.gif" width="16" height="16" border="0" alt="Edit settings for {@ID}"/>
                        </a>
                      </td>
                      <td nowrap="nowrap" align="center">
                        <a href="$baseActionURL?action=removePublishedChannel&amp;channelID={@ID}">
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
                      <!--<xsl:value-of select="position()"/>-<xsl:value-of select="//recordsPerPage"/><xsl:value-of select="(//recordsPerPage * //currentPage)-//recordsPerPage"/><xsl:value-of select="//recordsPerPage * //currentPage"/>-->
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
          <td nowrap="nowrap" valign="top">
            <xsl:call-template name="pagingWidget">
              <xsl:with-param name="i" select="1"/>
            </xsl:call-template>
          </td>
          <td width="100%" class="uportal-background-med" valign="top">Display<input type="text" name="textfieldRecordsDisplayed" size="2" class="uportal-input-text">
            <xsl:attribute name="value">
              <xsl:value-of select="//recordsPerPage"/>
            </xsl:attribute></input>records at a time.<img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
            <input type="submit" name="buttonRecordsDisplayed" value="go" class="uportal-button"/></td>
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
            <xsl:value-of select="$baseActionURL"/>?action=changePage&amp;newPage=<xsl:value-of select="(//currentPage)-1"/></xsl:attribute>
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
            <xsl:value-of select="$baseActionURL"/>?action=changePage&amp;newPage=<xsl:value-of select="$i"/></xsl:attribute>
          <xsl:value-of select="$i"/>
        </a>
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="4"/>
      </xsl:if>
      <xsl:if test="$i = ceiling(count(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1]) and //currentPage[1]=ceiling(count(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1])">
        <img src="{$mediaPath}/arrow_right_off.gif" width="16" height="16" alt="Next" border="0"/>
      </xsl:if>
      <xsl:if test="$i = ceiling(count(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1]) and //currentPage[1] &lt; ceiling(count(//*[@ID = $filterByID]//channel[not(@ID=preceding::channel/@ID)]) div //recordsPerPage[1])">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$baseActionURL"/>?action=changePage&amp;newPage=<xsl:value-of select="(//currentPage)+1"/></xsl:attribute>
          <img src="{$mediaPath}/arrow_right.gif" width="16" height="16" alt="Go to page [{(//currentPage)+1}]" border="0"/>
        </a>
      </xsl:if>
      <xsl:call-template name="pagingWidget">
        <xsl:with-param name="i" select="$i + 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="workflow">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">

      <tr>

        <td class="uportal-channel-table-header" nowrap="nowrap">Workflow:<img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10"/> </td>

        <xsl:apply-templates select="manageChannels/*"/>
      </tr>
    </table>
  </xsl:template>


  <xsl:template match="manageChannels/*">
    <xsl:if test="name(.)=$action and ./params/step/ID = $stepID">


      <xsl:for-each select="preceding::step/name">

      <xsl:if test="position() != 1">
        <td width="{round(100 div count(//step/name))}%">

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
                      <a href="#">
                        <xsl:value-of select="."/>
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
        <td width="{round(100 div count(//step/name))}%">

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
                    <a href="#">
                      <xsl:value-of select=".//step/name"/>
                    </a> </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>







      <xsl:for-each select="following::step/name">

        <td width="{round(100 div count(//step/name))}%">

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
                      <a href="#">
                        <xsl:value-of select="."/>
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

   <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">

      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-channel-text">

            <tr class="uportal-channel-strong" valign="top">

              <td colspan="2">Publish a new channel:</td>
            </tr>



            <tr class="uportal-channel-strong" valign="top">

              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
            </tr>



            <tr valign="top">

              <td colspan="2">

                <xsl:call-template name="workflow"/>
              </td>
            </tr>



            <tr class="uportal-channel-text" valign="top">

              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
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
                <a href="#ChannelName">Cancel and return</a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>

    <br/>



    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
      <!-- form begin -->



      <tr class="uportal-channel-text">

        <td>
          <strong>Settings [one]:</strong> Complete the Settings form below</td>
      </tr>



      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">

            <tr class="uportal-channel-table-header" valign="bottom">

              <td align="center" nowrap="nowrap">User can<br/>

               Modify?</td>



              <td>
                <img alt="interface image" src="transparent.gif" width="16" height="8"/>
              </td>



              <td width="100%">General Settings</td>
            </tr>



            <tr class="uportal-channel-table-header">

              <td align="center" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td>
                      <img alt="interface image" src="transparent.gif" width="2" height="2"/>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>



            <tr>

              <td align="center" valign="top">
                <input type="checkbox" name="checkbox63" value="checkbox"/> </td>



              <td></td>



              <td>
                <span class="uportal-label">Channel Name:</span> <span class="uportal-text-small">[example - StockCharts]<br/>

               <input type="text" name="textfield222" size="50" class="uportal-input-text"/></span> </td>
            </tr>



            <tr class="uportal-channel-text">

              <td align="center" valign="top" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td>
                      <img alt="interface image" src="transparent.gif" width="1" height="1"/>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>







            

<tr>

              <td align="center" valign="top"><input type="checkbox" name="checkbox632" value="checkbox" /> </td>



              <td></td>



              <td><span class="uportal-label">Channel Timeout:</span> <br />

               <input type="text" name="textfield3" size="6" class="uportal-input-text" /> milliseconds (1000 = 1 second)</td>

            </tr>



            <tr>

              <td colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td><img alt="interface image" src="transparent.gif" width="1" height="1" /></td>

                  </tr>

                </table>

              </td>

            </tr>

            


            



            
          </table>
        </td>
      </tr>



      <tr>

        <td>
          <input type="submit" name="Submit4" value="&lt; Back" class="uportal-button"/> <input type="submit" name="Submit3" value="Next &gt;" class="uportal-button"/> <input type="submit" name="Submit532" value="Review" class="uportal-button"/> <input type="submit" name="Submit82" value="Cancel" class="uportal-button"/> </td>
      </tr>
      <!-- form end -->
    </table>
  </xsl:template>

<xsl:template name="beginChannelDef">

    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">

      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-channel-text">

            <tr class="uportal-channel-strong" valign="top">

              <td colspan="2">Publish a new channel:</td>
            </tr>



            <tr class="uportal-channel-strong" valign="top">

              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
            </tr>



            <tr valign="top">

              <td colspan="2">

                <xsl:call-template name="workflow"/>
              </td>
            </tr>



            <tr class="uportal-channel-text" valign="top">

              <td colspan="2">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
              </td>
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
                <a href="#ChannelName">Cancel and return</a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>

    <br/>
    
   <xsl:apply-templates select="//channelDef"/>
  </xsl:template>



<!--John's Templates-->
	<xsl:template match="channelDef">

lkj;laksjfdoijf;oajf;oajwfeojaoijfew



    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
      <!-- form begin -->



      <tr class="uportal-channel-text">

        <td>
          <strong><xsl:value-of select="params/step/name"/>:</strong> <xsl:value-of select="params/step/description"/></td>
      </tr>



      <tr>

        <td>

          <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">

            <tr class="uportal-channel-table-header" valign="bottom">

              <td align="center" nowrap="nowrap">User can<br/>

               Modify?</td>



              <td>
                <img alt="interface image" src="transparent.gif" width="16" height="8"/>
              </td>



              <td width="100%">General Settings</td>
            </tr>



            <tr class="uportal-channel-table-header">

              <td align="center" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td>
                      <img alt="interface image" src="transparent.gif" width="2" height="2"/>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>



            <tr>

              <td class="uportal-text-small" align="center" valign="top"><input type="checkbox" name="checkbox6" value="checkbox" /> </td>



              <td class="uportal-text-small"></td>



              <td class="uportal-text-small"><span class="uportal-label">RSS URL:</span> <span class="uportal-text-small">[example - http://www.stockcharts.com/rss/stockcharts.rss]</span> <br />

               <input type="text" name="textfield2" size="80" maxlength="100" class="uportal-input-text" /> </td>

            </tr>



            <tr>

              <td align="center" valign="top" colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td><img alt="interface image" src="transparent.gif" width="1" height="1" /></td>

                  </tr>

                </table>

              </td>

            </tr>



            <tr>

              <td align="center" valign="top"><input type="checkbox" name="checkbox62" value="checkbox" /> </td>



              <td></td>



              <td><span class="uportal-label">RSS Version:<br />

               <select name="select3" class="uportal-input-text">

                <option selected="selected">

                  0.9x

                </option>



                <option>

                  1.0x

                </option>

              </select></span> </td>

            </tr>



            <tr>

              <td colspan="3">

                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">

                  <tr>

                    <td><img alt="interface image" src="transparent.gif" width="1" height="1" /></td>

                  </tr>

                </table>

              </td>

            </tr>

            


            



            
          </table>
        </td>
      </tr>



      <tr>

        <td>
          <input type="submit" name="Submit4" value="&lt; Back" class="uportal-button"/> <input type="submit" name="Submit3" value="Next &gt;" class="uportal-button"/> <input type="submit" name="Submit532" value="Review" class="uportal-button"/> <input type="submit" name="Submit82" value="Cancel" class="uportal-button"/> </td>
      </tr>
      <!-- form end -->
    </table>
















	<xsl:apply-templates select="params/step[ID=$stepID]"/>

	</xsl:template>
	
	<!-- The current step info-->
	<xsl:template match="step">
		<p align="left">
			<xsl:value-of select="name"/>
		</p>
		<table align="center" border="1" cellpadding="5" cellspacing="0">
			<tr>
				<td>Label</td>
				<td>Data</td>
				<td>Subscribe?</td>
				<xsl:apply-templates select="parameter"/>
			</tr>
		</table>
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
		<td>
			<!-- <xsl:value-of select="@modify"/>  just for debug -->
			<xsl:choose>
				<xsl:when test="@modify!='publish-only'">
					<input type="checkbox" name="{name}_sub">
		                <xsl:if test="@modify='subscribe'">
		                    <xsl:attribute name="checked">checked</xsl:attribute>
		                </xsl:if>
					</input>
				</xsl:when>
				<xsl:otherwise>
				    <xsl:text>&#160;</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</td>
	</xsl:template>
	
	<!-- display all the input fields with a base type of 'single-choice'-->
	<xsl:template name="single-choice">
		<xsl:choose>
			<xsl:when test="type/@display='drop-down'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<select name="{name}">
						<xsl:for-each select="type/restriction/value">
							<option value="{.}">
								<xsl:if test="@default='true'">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:choose>
								  <xsl:when test="@display"><xsl:value-of select="@display"/></xsl:when>
								  <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
								</xsl:choose>
							</option>
						</xsl:for-each>
					</select>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:when test="type/@display='radio'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<xsl:for-each select="type/restriction/value">
						<input type="radio" name="{name}" value="{.}">
							<xsl:if test="@default='true'">
								<xsl:attribute name="checked">checked</xsl:attribute>
							</xsl:if>
						</input>
						<xsl:choose>
						  <xsl:when test="@display"><xsl:value-of select="@display"/></xsl:when>
						  <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:otherwise>
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<select name="{name}">
						<xsl:for-each select="type/restriction/value">
							<option value="{.}">
								<xsl:if test="@default='true'">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:choose>
								  <xsl:when test="@display"><xsl:value-of select="@display"/></xsl:when>
								  <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
								</xsl:choose>
							</option>
						</xsl:for-each>
					</select>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- display all the input fields with a base type of 'multi-choice'-->
	<xsl:template name="multi-choice">
		<xsl:choose>
			<xsl:when test="type/@display='select-list'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<select name="{name}" size="6" multiple="multiple">
						<xsl:for-each select="type/restriction/value">
							<option value="{.}">
								<xsl:if test="@default='true'">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:choose>
								  <xsl:when test="@display"><xsl:value-of select="@display"/></xsl:when>
								  <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
								</xsl:choose>
							</option>
						</xsl:for-each>
					</select>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:when test="type/@display='checkbox'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<xsl:for-each select="type/restriction/value">
						<input type="checkbox" name="{name}" value="{.}">
							<xsl:if test="@default='true'">
								<xsl:attribute name="checked">checked</xsl:attribute>
							</xsl:if>
						</input>
						<xsl:choose>
						  <xsl:when test="@display"><xsl:value-of select="@display"/></xsl:when>
						  <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:otherwise>
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<select name="{name}" size="6" multiple="multiple">
						<xsl:for-each select="type/restriction/value">
							<option value="{.}">
								<xsl:if test="@default='true'">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:choose>
								  <xsl:when test="@display"><xsl:value-of select="@display"/></xsl:when>
								  <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
								</xsl:choose>
							</option>
						</xsl:for-each>
					</select>
				</td>
				<xsl:call-template name="subscribe"/>
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
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<input type="text" name="{name}" value="{defaultValue}" maxlength="{$maxlength}" size="{$length}"/>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:when test="type/@display='textarea'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<textarea rows="{$defaultTextRows}" cols="{$defaultTextCols}">
						<xsl:value-of select="defaultValue"/>
					</textarea>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:when test="type/@display='hidden'">
				<input type="hidden" name="{name}" value="{defaultValue}"/>
			</xsl:when>
			<xsl:otherwise>
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<input type="text" name="{name}" value="{defaultValue}" maxlength="{$maxlength}" size="{$length}"/>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
<!--End John's Templates-->

</xsl:stylesheet>

<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios ><scenario default="no" name="emptyDocument" userelativepaths="no" url="file://c:\LaJolla\uPortal\webpages\media\org\jasig\portal\channels\CPublisher\empty.xml" htmlbaseurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/><scenario default="no" name="channelRegistry" userelativepaths="no" url="file://c:\LaJolla\uPortal\webpages\media\org\jasig\portal\channels\CPublisher\channelRegistry.xml" htmlbaseurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/><scenario default="yes" name="newchannel" userelativepaths="no" url="file://C:\LaJolla\uPortal\webpages\media\org\jasig\portal\channels\CGenericXSLT\RSS\rssTest.xml" htmlbaseurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperInfo  srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="no" />
</metaInformation>
-->
