<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output indent="no" method="html"/>
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="stepID">1</xsl:param>
  <xsl:param name="errorMessage">no parameter passed</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:variable name="defaultLength">10</xsl:variable>
  <xsl:variable name="defaultMaxLength">20</xsl:variable>
  <xsl:variable name="defaultTextCols">40</xsl:variable>
  <xsl:variable name="defaultTextRows">10</xsl:variable>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CUserPreferences/tab-column</xsl:variable>

<xsl:template match="/">
<!-- form begin -->
<form action="{$baseActionURL}" method="post" name="parameters">
<input name="uPTCUP_action" type="hidden" value="changeMe"/>
<table border="0" cellpadding="10" cellspacing="0" class="uportal-background-med" width="100%">
   <tr>
        <td class="uportal-channel-text"><strong>Channel Settings: </strong><xsl:value-of select="$THE_CHANNEL_YOU_HAVE_SELECTED_HAS_SETTINGS_WHICH_MAY_BE_MODIFIED"/></td>
   </tr>
   <tr>
        <td>
        <table border="0" cellpadding="2" cellspacing="0" class="uportal-background-content" width="100%">
              <xsl:choose>
                <xsl:when test="$errorMessage = 'no parameter passed'">
                  <xsl:choose>
                    <xsl:when test="not(/userPrefParams/channelDef)">
           <tr class="uportal-channel-table-header" valign="bottom"><td width="100%"><xsl:value-of select="$CUSTOM_CHANNEL_SETTINGS"/></td></tr>
           <tr class="uportal-channel-table-header">
                <td align="center">
                <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-med" width="100%">
                   <tr><td><img height="2" src="{$mediaPath}/transparent.gif" width="1"/></td></tr>
                </table>
                </td>
           </tr>
                      <xsl:for-each select="/userPrefParams/channel/parameter[@override = 'yes']">
           <tr class="uportal-channel-table-header" valign="bottom">
                <td width="100%">
                            <span class="uportal-label">
                              <xsl:value-of select="@name"/>:</span>
                            <br/>
                            <input class="uportal-input-text" name="{@name}" size="40" type="text">
                              <xsl:attribute name="value">
                                <xsl:value-of select="@value"/>
                              </xsl:attribute>
                            </input>
                </td>
           </tr>
                      </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>

           <tr>
                <td align="center" class="uportal-text12-bold" nowrap="nowrap"><img height="8" src="{$mediaPath}/transparent.gif" width="16"/><xsl:value-of select="$HELP"/><img height="8" src="{$mediaPath}/transparent.gif" width="16"/></td>
                <td class="uportal-text12-bold" width="100%"><xsl:value-of select="$CHANNEL_SETTINGS"/></td>
           </tr>
           <tr>
                <td align="center" colspan="4">
                <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-med" width="100%">
                   <tr><td><img height="2" src="{$mediaPath}/transparent.gif" width="1"/></td></tr>
                </table>
                </td>
           </tr>

                      <xsl:apply-templates select="/userPrefParams/channelDef//parameter[child::name = /userPrefParams/channel/parameter[@override='yes']/@name]"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
           <tr class="uportal-channel-table-header" valign="bottom"><td class="uportal-channel-text" width="100%"><xsl:value-of select="$ERROR_MESSAGE"/></td></tr>
           <tr>
                <td>
                <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-med" width="100%">
                   <tr><td><img height="2" src="{$mediaPath}/transparent.gif" width="1"/></td></tr>
                </table>
                </td>
           </tr>
           <tr class="uportal-channel-table-header" valign="bottom">
                <td width="100%"><xsl:value-of select="$errorMessage"/></td>
           </tr>
                </xsl:otherwise> 
            </xsl:choose>
        </table>
        </td>
   </tr>
</table>

<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
   <TR><TD><IMG SRC="{$mediaPath}/transparent.gif" height="10" width="1"/></TD></TR>
   <TR><TD CLASS="uportal-channel-text"><xsl:value-of select="$YOU_MUST_LOGOUT_FOR_CHANGES_TO_TAKE_EFFECT"/></TD></TR>
   <TR><TD><IMG SRC="{$mediaPath}/transparent.gif" height="10" width="1"/></TD></TR>
   <TR><TD CLASS="uportal-background-med"><IMG SRC="{$mediaPath}/transparent.gif" height="1" width="1"/></TD></TR>
   <TR><TD CLASS="uportal-background-light">

   <input type="submit" name="uPTCUP_submit" onclick="document.parameters.uPTCUP_action.value='back'" class="uportal-button"><xsl:attribute name="value"><xsl:value-of select="$BUTTON_BACK"/></xsl:attribute></input>
   <input type="submit" name="uPTCUP_submit" onclick="document.parameters.uPTCUP_action.value='finished'" class="uportal-button"><xsl:attribute name="value"><xsl:value-of select="$BUTTON_FINISHED"/></xsl:attribute></input>
   <input type="submit" name="uPTCUP_submit" onclick="document.parameters.uPTCUP_action.value='cancel'" class="uportal-button"><xsl:attribute name="value"><xsl:value-of select="$BUTTON_CANCEL"/></xsl:attribute></input>

   </TD></TR>
   <TR><TD CLASS="bgtabon"><IMG SRC="{$mediaPath}/transparent.gif" height="1" width="1"/></TD></TR>
</TABLE>
</form>  

</xsl:template>


<xsl:template match="parameter">
    <xsl:choose>
      <xsl:when test="type/@display != 'hidden'">
   <tr>
        <td align="center" valign="top">
            <xsl:call-template name="help"/>
        </td>
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
   <tr>
        <td align="center" colspan="4">
        <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-med" width="100%">
           <tr><td><img height="2" src="{$mediaPath}/transparent.gif" width="1"/></td></tr>
        </table>
        </td>
   </tr>
      </xsl:when>
      <xsl:otherwise>
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
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- displays checkbox for publisher to allow subscribe time modification-->
  <xsl:template name="subscribe">
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
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>


              <option value="{.}">

                <xsl:if test="/userPrefParams/channel/parameter[@name=$paramName]/@value=$paramValue">
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
                      
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
            <input name="{name}" type="radio" value="{.}">
              <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
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
          <select class="uportal-input-text" name="{name}">
            <xsl:for-each select="type/restriction/value">
                        
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
              <xsl:call-template name="subscribe"/>
              <option value="{.}">
                <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
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
          <select class="uportal-input-text" multiple="multiple" name="{name}" size="6">
            <xsl:for-each select="type/restriction/value">
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
              <option value="{.}">
                <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
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
                      
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
            <input name="{name}" type="checkbox" value="{.}">
              <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
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
          <select class="uportal-input-text" multiple="multiple" name="{name}" size="6">
            <xsl:for-each select="type/restriction/value">
                        
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
              <option value="{.}">
                <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
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
    <xsl:variable name="paramName">
      <xsl:value-of select="name"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="type/@display='text'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <input class="uportal-input-text" maxlength="{$maxlength}" name="{name}" size="{$length}" type="text">
            <xsl:attribute name="value">
              <xsl:value-of select="/userPrefParams/channel/parameter[@name = $paramName]/@value"/>
            </xsl:attribute>
          </input>
          <xsl:apply-templates select="units"/>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='textarea'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <textarea class="uportal-input-text" cols="{$defaultTextCols}" name="{name}" rows="{$defaultTextRows}">
            <xsl:value-of select="/userPrefParams/channel/parameter[@name = $paramName]/@value"/>
          </textarea>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='hidden'">
        <input name="{name}" type="hidden" value="{/userPrefParams/channel/parameter[@name = $paramName]/@value}"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <input class="uportal-input-text" maxlength="{$maxlength}" name="{name}" size="{$length}" type="text">
            <xsl:attribute name="value">
              <xsl:value-of select="/userPrefParams/channel/parameter[@name = $paramName]/@value"/>
            </xsl:attribute>
          </input>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="label">
    <span class="uportal-label">
      <xsl:value-of select="."/>:</span>
  </xsl:template>

  <xsl:template match="example">
    <img height="8" src="{$mediaPath}/transparent.gif" width="8"/>
    <span class="uportal-text-small"><xsl:value-of select="$EXAMPLE_"/><xsl:value-of select="."/>]</span>
  </xsl:template>

  <xsl:template match="units">
    <img height="8" src="{$mediaPath}/transparent.gif" width="8"/>
    <span class="uportal-text-small">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template name="help">
    <a>
      <xsl:attribute name="href">javascript:alert('Name: <xsl:value-of select="label"/><xsl:value-of select="$NEXAMPLE"/><xsl:value-of select="example"/><xsl:value-of select="$NNDESCRIPTION"/><xsl:value-of select="description"/>')</xsl:attribute>
      <img border="0" height="16" width="16"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_DISPLAY_HELP_INFORMATION"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_HELP_GIF"/></xsl:attribute></img>
    </a>
  </xsl:template>
</xsl:stylesheet>