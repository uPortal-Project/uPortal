<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xmsg="urn:x-lexica:xmsg:message:1.0">
  <xsl:output method="html" indent="yes" />
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="grpViewId">0</xsl:param>
  <xsl:param name="grpMode" />
  <xsl:param name="grpServantMessage" select="false()"/>
  <xsl:param name="commandResponse">null</xsl:param>
  <xsl:param name="ignorePermissions" select="false()"/>
  <xsl:param name="blockFinishActions" select="false()"/>
  <xsl:param name="blockEntitySelect" select="false()"/>
  <xsl:key name="can" match="//principal/permission[@type='GRANT']" use="concat(@activity,'|',@target)" />
  <xsl:key name="selectedGroup" match="group[@selected='true']" use="@key"/>
  <xsl:key name="selectedEntity" match="entity[@selected='true']" use="@key"/>

  <xsl:template match="/">
    <center>
      <table border="0" cellpadding="2" cellspacing="2">
        <xsl:if test="not($commandResponse='null')">
          <tr>
            <td colspan="3" class="uportal-channel-warning">
              <xsl:value-of select="$commandResponse" />
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="$grpMode='select'">
          <tr>
            <td class="uportal-channel-table-header" colspan="2">
              <xsl:choose>
                <xsl:when test="$grpServantMessage">
                    <xsl:value-of select="$grpServantMessage"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>
                    Select Groups and People:
                  </xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </td>
          </tr>
        </xsl:if>
        <form action="{$baseActionURL}" method="POST">
          <input type="hidden" name="uP_root" value="me"/>
          <xsl:apply-templates select="//group[@id=$grpViewId]" />
          <xsl:if test="$grpMode='select'">
            <tr>
              <td colspan="2">
                <input type="hidden" name="grpMode" value="select" /> <input type="submit" name="grpCommand" value="Select" class="uportal-button" />
                <xsl:if test="not($blockFinishActions)">
                  <xsl:text>
                  </xsl:text>
                  <input type="submit" class="uportal-button" name="grpCommand" value="Done" />
                  <xsl:text>
                  </xsl:text>
                  <input type="submit" class="uportal-button" name="grpCommand" value="Cancel Selection" />
                </xsl:if>
              </td>
            </tr>
          </xsl:if>
        </form>
        <xsl:if test="($grpMode='select') and (count(descendant::*[@selected='true']))">
          <tr>
            <td colspan="2" align="center">
              <table width="90%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td height="1" width="5" class="uportal-background-highlight">
                    <xsl:text>
                    </xsl:text>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
          <form action="{$baseActionURL}" method="POST">
            <xsl:if test="count(descendant::group[@selected='true'])">
              <tr>
                <td colspan="3" class="uportal-channel-table-header">
                  Selected Groups:
                </td>
              </tr>
              <xsl:for-each select="descendant::group[@selected='true']">
                <xsl:variable name="id">
                  <xsl:value-of select="@id" />
                </xsl:variable>
                <xsl:if test="count(preceding::group[@selected='true' and @id=$id])=0">
                  <tr>
                    <td align="center" valign="top">
                      <input type="checkbox" name="grpDeselect//{@id}|group" value="true" />
                    </td>
                    <td width="100%" class="uportal-channel-table-row-even">
                      <xsl:value-of select="RDF/Description/title" />
                    </td>
                  </tr>
                </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="count(descendant::entity[@selected='true'])">
              <tr>
                <td colspan="3" class="uportal-channel-table-header">
                  Selected Entities:
                </td>
              </tr>
              <xsl:for-each select="descendant::entity[@selected='true']">
                <xsl:variable name="id">
                  <xsl:value-of select="@id" />
                </xsl:variable>
                <xsl:if test="count(preceding::entity[@selected='true'][@id=$id])=0">
                  <tr>
                    <td align="center" valign="top">
                      <input type="checkbox" name="grpDeselect//{@id}|entity" value="true" />
                    </td>
                    <td width="100%" class="uportal-channel-table-row-odd">
                      <xsl:value-of select="@displayName" />
                    </td>
                  </tr>
                </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <tr>
              <td colspan="3">
                <input type="hidden" name="grpMode" value="select" /> <input type="submit" name="grpCommand" value="Deselect" class="uportal-button" />
              </td>
            </tr>
          </form>
        </xsl:if>
      </table>
    </center>
  </xsl:template>
  <xsl:template match="group">
    <xsl:param name="depth">
      1
    </xsl:param>
    <xsl:if test="$ignorePermissions or key('can',concat('VIEW','|',@key)) or (@id=0)">
      <tr>
        <xsl:if test="$grpMode='select'">
          <td align="center" valign="top">
            <xsl:if test="not(@id=0) and ($ignorePermissions or key('can',concat('SELECT','|',@key)))">
              <xsl:choose>
                <xsl:when test="(@selected='true') or (key('selectedGroup',@key))">
                  <span class="uportal-channel-warning">
                    <xsl:text>
                      X
                    </xsl:text>
                  </span>
                </xsl:when>
                <xsl:otherwise>
                  <input type="checkbox" name="grpSelect//{@id}|group" value="true" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </td>
        </xsl:if>
        <td width="100%">
          <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
              <td>
                <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5">
                  <xsl:attribute name="width">
                    <xsl:value-of select="$depth*14" />
                  </xsl:attribute>
                </img>
              </td>
              <td>
                <xsl:if test="(@expanded='true') and (@hasMembers='true') and not(@id=0)">
                  <a href="{$baseActionURL}?grpCommand=Collapse&amp;grpCommandIds={@id}&amp;grpMode={$grpMode}"> <img border="0" height="14" width="14" src="media/org/jasig/portal/channels/CUserPreferences/tab-column/arrow_down_image.gif" /> </a>
                </xsl:if>
                <xsl:if test="(@expanded='false') and (@hasMembers='true') and not(@id=0)">
                  <a href="{$baseActionURL}?grpCommand=Expand&amp;grpCommandIds={@id}&amp;grpMode={$grpMode}"> <img border="0" height="14" width="14" src="media/org/jasig/portal/channels/CUserPreferences/tab-column/arrow_right_image.gif" /> </a>
                </xsl:if>
              </td>
              <td width="100%" class="uportal-channel-table-row-even">
                <xsl:if test="$grpMode!='select'">
                  <a href="{$baseActionURL}?uP_root=me&amp;grpView=edit&amp;grpViewId={@id}&amp;grpCommand=Expand&amp;grpCommandIds={@id}"> <span class="uportal-channel-table-row-even">
                      <xsl:value-of select="RDF/Description/title" />
                    </span> </a>
                </xsl:if>
                <xsl:if test="$grpMode='select'">
                  <xsl:value-of select="RDF/Description/title" />
                </xsl:if>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <xsl:if test="@expanded='true'">
        <xsl:apply-templates select="entity">
          <xsl:with-param name="emptyWidth">
            <xsl:value-of select="($depth*14)+14" />
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:if test="@expanded='true'">
        <xsl:apply-templates select="group">
          <xsl:with-param name="depth">
            <xsl:value-of select="$depth + 1" />
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  <xsl:template match="entity">
    <xsl:param name="emptyWidth">
      1
    </xsl:param>
    <tr>
      <xsl:if test="$grpMode='select'">
        <td align="center" valign="top">
          <xsl:if test="($ignorePermissions or key('can',concat('SELECT','|',parent::group/@key))) and not($blockEntitySelect)">
            <xsl:choose>
              <xsl:when test="(@selected='true') or key('selectedEntity',@key)">
                <span class="uportal-channel-warning">
                  <xsl:text>
                    X
                  </xsl:text>
                </span>
              </xsl:when>
              <xsl:otherwise>
                <input type="checkbox" name="grpSelect//{@id}|entity" value="true" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </td>
      </xsl:if>
      <td width="100%">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
          <tr>
            <td>
              <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="{$emptyWidth}" />
            </td>
            <td class="uportal-channel-table-row-odd" width="100%">
              <xsl:value-of select="@displayName" />
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
