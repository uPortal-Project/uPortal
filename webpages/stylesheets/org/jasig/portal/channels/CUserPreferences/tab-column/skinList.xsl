<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseActionURL">render.uP</xsl:param>
  <xsl:param name="currentSkin">java</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CUserPreferences/tab-column</xsl:variable>

  <xsl:template match="/">
        <form name="form1" method="post" action="{$baseActionURL}">
          <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td colspan="4" class="uportal-channel-table-header" valign="top">Design Skins</td>
            </tr>

<!--            <tr>
              <td colspan="4" class="uportal-background-dark" valign="top">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" />
              </td>
            </tr>-->

	    <!-- table headers -->
	    <tr class="uportal-background-light">
		<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>
	      <td align="center" class="uportal-channel-text" valign="middle">Name</td>
	      <td align="center" class="uportal-channel-text" valign="middle">Description</td>
	      <td align="center" class="uportal-channel-text" valign="middle">Icon</td>
  	    </tr>

            <xsl:call-template name="list" />

           <tr>
              <td class="uportal-text-small" colspan="4">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />
              </td>
            </tr>

            <tr>
              <td class="uportal-text-small" align="center" valign="top" colspan="4">
		<input type="hidden" name="action" value="completeEdit"/>
                <input type="submit" name="submitSave" value="Select Skin" class="uportal-button" />

                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />

                <input type="submit" name="submitCancel" value="Cancel" class="uportal-button" />
              </td>
            </tr>
          </table>
        </form>
  </xsl:template>

  <xsl:template name="list">
    <!-- Java skin -->
    <tr>
      <td align="center" class="uportal-channel-text" valign="middle">
	<xsl:choose>
 	 <xsl:when test="$currentSkin='java'">
          <input type="radio" name="skinName" value="java" checked="checked" />
	 </xsl:when>
         <xsl:otherwise>
          <input type="radio" name="skinName" value="java"/>
         </xsl:otherwise>
        </xsl:choose>
      </td>
      <td align="center" class="uportal-channel-text" valign="middle">
        Java
      </td>
      <td align="center" class="uportal-channel-text">
	SUN's Java color scheme
      </td>
      <td align="center" valign="top">
         <img alt="sample icon" src="{$mediaPath}/java_thumb.gif" width="120" height="90" border="0"/>
      </td>
    </tr>

    <!-- UOSM skin -->
    <tr>
      <td align="center" class="uportal-channel-text" valign="middle">
	<xsl:choose>
 	 <xsl:when test="$currentSkin='uosm'">
          <input type="radio" name="skinName" value="uosm" checked="checked" />
	 </xsl:when>
         <xsl:otherwise>
          <input type="radio" name="skinName" value="uosm"/>
         </xsl:otherwise>
        </xsl:choose>
      </td>
      <td align="center" class="uportal-channel-text" valign="middle">
        UOSM
      </td>
      <td align="center" class="uportal-channel-text">
	University of Santa Maria's colors (ask Justin about the admissions policy)
      </td>
      <td align="center" valign="top">
         <img alt="sample icon" src="{$mediaPath}/uosm_thumb.gif" width="120" height="90" border="0"/>
      </td>
    </tr>

    <!-- VSAC skin -->
    <tr>
      <td align="center" class="uportal-channel-text" valign="middle">
	<xsl:choose>
 	 <xsl:when test="$currentSkin='vsac'">
          <input type="radio" name="skinName" value="vsac" checked="checked" />
	 </xsl:when>
         <xsl:otherwise>
          <input type="radio" name="skinName" value="vsac"/>
         </xsl:otherwise>
        </xsl:choose>
      </td>
      <td align="center" class="uportal-channel-text" valign="middle">
        VSAC
      </td>
      <td align="center" class="uportal-channel-text">
	VSAC color scheme
      </td>
      <td align="center" valign="top">
         <img alt="sample icon" src="{$mediaPath}/vsac_thumb.gif" width="120" height="90" border="0"/>
      </td>
    </tr>

    <!-- IMM skin -->
    <tr>
      <td align="center" class="uportal-channel-text" valign="middle">
	<xsl:choose>
 	 <xsl:when test="$currentSkin='imm'">
          <input type="radio" name="skinName" value="imm" checked="checked" />
	 </xsl:when>
         <xsl:otherwise>
          <input type="radio" name="skinName" value="imm"/>
         </xsl:otherwise>
        </xsl:choose>

      </td>
      <td align="center" class="uportal-channel-text" valign="middle">
        IM&amp;M
      </td>
      <td align="center" class="uportal-channel-text">
	Instructional Media &amp; Magic scheme
      </td>
      <td align="center" valign="top">
         <img alt="sample icon" src="{$mediaPath}/imm_thumb.gif" width="120" height="90" border="0"/>
      </td>
    </tr>

    <!-- Matrix skin -->
    <tr>
      <td align="center" class="uportal-channel-text" valign="middle">
	<xsl:choose>
 	 <xsl:when test="$currentSkin='matrix'">
          <input type="radio" name="skinName" value="matrix" checked="checked" />
	 </xsl:when>
         <xsl:otherwise>
          <input type="radio" name="skinName" value="matrix"/>
         </xsl:otherwise>
        </xsl:choose>

      </td>
      <td align="center" class="uportal-channel-text" valign="middle">
        Matrix
      </td>
      <td align="center" class="uportal-channel-text">
	Skin based on the movie "Matrix"
      </td>
      <td align="center" valign="top">
         <img alt="sample icon" src="{$mediaPath}/matrix_thumb.gif" width="120" height="90" border="0"/>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>

