<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xmsg="urn:x-lexica:xmsg:message:1.0">
  <xsl:output method="html" indent="yes" />
  <xsl:param name="baseActionURL">default </xsl:param>
  <xsl:param name="grpViewId">0 </xsl:param>
  <xsl:param name="grpMode" />
  <xsl:param name="commandResponse">null</xsl:param>
  <xsl:param name="grpServantMode">false </xsl:param>
  <xsl:param name="ignorePermissions" select="false()"/>
  <xsl:key name="can" match="//principal/permission[@type='GRANT']" use="concat(@activity,'|',@target)"/>

  <xsl:template match="/">
    <table width="100%" border="0">
      <xsl:apply-templates select="CGroupsManager/descendant::*[name()='group'][@id=$grpViewId]" />
    </table>
  </xsl:template>
  <xsl:template match="group[@id=$grpViewId]">
    <xsl:variable name="grpKey" select="@key" />
    <xsl:if test="not($commandResponse='null')">
      <tr>
        <td colspan="3" class="uportal-channel-warning">
          <xsl:value-of select="$commandResponse" />
        </td>
      </tr>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="$grpViewId='0'">
        <tr>
          <td colspan="3" class="uportal-channel-table-header">
            <xsl:text>
              My Groups:
            </xsl:text>
          </td>
        </tr>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td colspan="3" class="uportal-channel-table-header">
            <xsl:text>
              Group Name:
            </xsl:text>
          </td>
        </tr>
        <form action="{$baseActionURL}" method="POST">
          <input type="hidden" name="grpView" value="edit" /> <input type="hidden" name="grpViewId" value="{$grpViewId}" /> <input type="hidden" name="grpCommandIds" value="{$grpViewId}" />
          <tr>
            <td>
              <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" />
            </td>
            <td class="uportal-channel-text">
              <xsl:choose>
                <xsl:when test="not($ignorePermissions) and not(key('can',concat('UPDATE','|',@key)))">
                  <xsl:value-of select="RDF/Description/title" />
                </xsl:when>
                <xsl:otherwise>
                  <input type="text" size="40" maxsize="255" name="grpName" class="uportal-channel-text">
                    <xsl:attribute name="value">
                      <xsl:value-of select="RDF/Description/title" />
                    </xsl:attribute>
                  </input>
                </xsl:otherwise>
              </xsl:choose>
            </td>
            <td nowrap="true">
              <xsl:if test="$ignorePermissions or key('can',concat('UPDATE','|',@key))">
                <input type="submit" name="grpCommand" value="Update" class="uportal-button" />
              </xsl:if>
              <xsl:if test="$ignorePermissions or key('can',concat('DELETE','|',@key))">
                <input type="submit" name="grpCommand" value="Delete" class="uportal-button" />
              </xsl:if>
            </td>
          </tr>
        </form>
        <tr>
          <td colspan="3" class="uportal-channel-table-header">
            <xsl:text>
              Group Description:
            </xsl:text>
          </td>
        </tr>
        <tr>
          <td>
          </td>
          <td class="uportal-channel-text">
            <xsl:value-of select="RDF/Description/description" />
          </td>
        </tr>
        <tr>
          <td colspan="3" class="uportal-channel-table-header">
            <xsl:text>
              Members:
            </xsl:text>
          </td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="child::*[(name()='group') or (name()='entity')]">
      <xsl:with-param name="grpKey" select="$grpKey" />
    </xsl:apply-templates>
    <xsl:call-template name="AddAndCreateRows">
      <xsl:with-param name="grpKey" select="$grpKey" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="AddAndCreateRows">
    <xsl:param name="grpKey">null </xsl:param>
    <xsl:if test="$ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true')">
      <form action="{$baseActionURL}" method="POST">
        <input type="hidden" name="grpView" value="tree" /> <input type="hidden" name="grpMode" value="select" /> <input type="hidden" name="grpViewId" value="0" /> <input type="hidden" name="grpCommand" value="Add" /> <input type="hidden" name="grpCommandIds" value="{$grpViewId}" />
        <tr>
          <td>
            <xsl:text>
            </xsl:text>
          </td>
          <td colspan="2">
            <input type="submit" value="Add Members" class="uportal-button" />
          </td>
        </tr>
      </form>
    </xsl:if>
    <xsl:if test="not($grpServantMode='true')">
      <xsl:if test="not($grpViewId='0') and not($grpKey='null') and ($ignorePermissions or key('can',concat('ASSIGNPERMISSIONS','|',$grpKey)))">
        <form action="{$baseActionURL}" method="POST">
          <tr>
            <input type="hidden" name="grpView" value="AssignPermissions" /> <input type="hidden" name="grpViewKey" value="{$grpKey}" />
            <td>
              <xsl:text>
              </xsl:text>
            </td>
            <td colspan="2">
              <input type="submit" value="Assign Permissions" class="uportal-button" />
            </td>
          </tr>
        </form>
      </xsl:if>
      <xsl:if test="$ignorePermissions or key('can',concat('CREATE','|',$grpKey))">
        <form action="{$baseActionURL}" method="POST">
          <input type="hidden" name="grpView" value="edit" /> <input type="hidden" name="grpViewId" value="{$grpViewId}" /> <input type="hidden" name="grpCommand" value="Create" /> <input type="hidden" name="grpCommandIds" value="{$grpViewId}" />
          <tr>
            <td>
              <xsl:text>
              </xsl:text>
            </td>
            <td colspan="2" nowrap="true">
              <input type="submit" value="Create New Group" class="uportal-button" />
              <xsl:text>
              </xsl:text>
              <input type="text" size="20" name="grpName" value="(new group name)" class="uportal-channel-text" />
            </td>
          </tr>
        </form>
      </xsl:if>
    </xsl:if>
    <form action="{$baseActionURL}" method="POST">
      <input type="hidden" name="grpMode" value="Browse" /> <input type="hidden" name="grpView" value="tree" /> <input type="hidden" name="grpViewId" value="0" />
      <xsl:choose>
        <xsl:when test="$grpServantMode='true'">
          <input type="hidden" name="grpCommand" value="Cancel Selection" />
        </xsl:when>
        <xsl:otherwise>
          <input type="hidden" name="uP_root" value="root" />
        </xsl:otherwise>
      </xsl:choose>
      <tr>
        <td>
          <xsl:text>
          </xsl:text>
        </td>
        <td colspan="2">
          <input type="submit" value="Finished" class="uportal-button" />
        </td>
      </tr>
    </form>
  </xsl:template>

  <xsl:template match="group[@id!=$grpViewId]">
    <xsl:param name="grpKey">null</xsl:param>
    <xsl:if test="$ignorePermissions or key('can',concat('VIEW','|',@key))">
      <tr>
        <td>
          <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" />
        </td>
        <td width="100%" class="uportal-channel-table-row-even">
          <xsl:choose>
            <xsl:when test="not($grpServantMode='true')">
              <a href="{$baseActionURL}?grpView=edit&amp;grpViewId={@id}&amp;grpCommand=Expand&amp;grpCommandIds={@id}"> <span class="uportal-channel-table-row-even">
                  <xsl:value-of select="RDF/Description/title" />
                </span> </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="RDF/Description/title" />
            </xsl:otherwise>
          </xsl:choose>
        </td>
        <td align="right" valign="top" class="uportal-channel-table-row-even">
          <xsl:if test="$ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true')">
            <a href="{$baseActionURL}?grpView=edit&amp;grpViewId={$grpViewId}&amp;grpCommand=Remove&amp;grpCommandIds=parent.{parent::group/@id}|child.{@id}"> <span class="uportal-channel-table-row-even">
                <xsl:text>
                  Remove
                </xsl:text>
              </span> </a>
          </xsl:if>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="entity">
    <xsl:param name="grpKey">null</xsl:param>
    <tr>
      <td>
        <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" />
      </td>
      <td width="100%" class="uportal-channel-table-row-odd">
        <xsl:value-of select="@displayName" />
      </td>
      <td align="right" valign="top" class="uportal-channel-table-row-odd">
        <xsl:if test="$ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true')">
          <a href="{$baseActionURL}?grpView=edit&amp;grpViewId={$grpViewId}&amp;grpCommand=Remove&amp;grpCommandIds=parent.{parent::group/@id}|child.{@id}"> <span class="uportal-channel-table-row-odd">
              <xsl:text>
                Remove
              </xsl:text>
            </span> </a>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
