<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="baseActionURL">Default</xsl:param>
<xsl:param name="locale">en_US</xsl:param>
<xsl:param name="folderID">root</xsl:param>
<xsl:param name="modified">false</xsl:param>
<xsl:param name="profileName">default profile</xsl:param>
<xsl:variable name="imageDir" select="'media/org/jasig/portal/channels/CUserPreferences'"/>

<xsl:template match="/">

<!-- header table -->

<form action="{$baseActionURL}" method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input name="action" type="hidden" value="submitEditChoice"/>
<table border="0" width="100%"><tr><td align="left"><xsl:value-of select="$PROFILE_NAME"/><xsl:value-of select="$profileName"/></td><td align="right">
<input name="submit" type="submit"><xsl:attribute name="value"><xsl:value-of select="$MANAGE"/></xsl:attribute></input>
<select name="userPreferencesAction">
<option selected=""><xsl:value-of select="$CHANNELS_AND_FOLDERS"/><xsl:attribute name="value"><xsl:value-of select="$LAYOUT"/></xsl:attribute></option>
<option value="gpref"><xsl:value-of select="$GLOBAL_PREFERENCES"/></option>
<option value="manageProfiles"><xsl:value-of select="$PROFILES"/></option>
</select>
</td>
</tr></table>
</form>

<!-- end of the header table -->
	
  <!--p align="center">Arrange your channels and folders...</p-->
  <p align="center">
    <xsl:choose>
      <xsl:when test="$folderID = 'root'">
        <xsl:for-each select="//layout">
          <xsl:call-template name="crumbTrail"/> 
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="//folder[@ID=$folderID]">
          <xsl:call-template name="crumbTrail"/>
        </xsl:for-each>
      </xsl:otherwise>
	  </xsl:choose>
	</p>
	  
  <form action="{$baseActionURL}" method="post">
    <input name="action" type="hidden"><xsl:attribute name="value"><xsl:value-of select="$MOVE"/></xsl:attribute></input>

    <table align="center" border="1" cellpadding="5" cellspacing="0">
      <tr>
        <th><input name="moveTo" type="submit"><xsl:attribute name="value"><xsl:value-of select="$MOVE_TO"/></xsl:attribute></input></th>
        <th><xsl:value-of select="$REORDER"/></th>
        <th><xsl:value-of select="$NAME"/></th>
        <th><xsl:value-of select="$EDIT"/></th>
      </tr>

      <xsl:choose>
        <xsl:when test="$folderID = 'root'">
          <xsl:for-each select="//layout">
            <xsl:call-template name="folderRoot"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="//folder[@ID=$folderID]">
            <xsl:call-template name="folderRoot"/>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </form>    
  <xsl:if test="$modified = 'true'">
    <p align="center">
    <form action="{$baseActionURL}" method="post">
      <input name="action" type="hidden" value="saveChanges"/>
      <input name="saveChanges" type="submit"><xsl:attribute name="value"><xsl:value-of select="$SAVE_CHANGES"/></xsl:attribute></input>    
    </form>
    </p><p/>
  </xsl:if>    
</xsl:template>

<xsl:template name="folderRoot">
  <xsl:apply-templates select="folder[not(@type='header' or @type='footer') and @hidden='false']|channel"/>
</xsl:template>

<xsl:template name="crumbTrail"><xsl:value-of select="$FOLDER"/><xsl:choose>
    <xsl:when test="name()='layout'"><xsl:value-of select="$MY_CHANNELS"/></xsl:when>
    <xsl:otherwise>    
      <a href="{$baseActionURL}?action=browse&amp;folderID=root"><xsl:value-of select="$MY_CHANNELS"/></a>
    </xsl:otherwise>
  </xsl:choose>

  <xsl:for-each select="ancestor::*">
    <a href="{$baseActionURL}?action=browse&amp;folderID={@ID}"><xsl:value-of select="@name"/></a>
    <xsl:text> &gt; </xsl:text>
  </xsl:for-each>

  <xsl:value-of select="@name"/>
</xsl:template>

<xsl:template match="folder">
  <tr>
    <!-- Move checkbox -->
    <td align="right"><input type="checkbox" value="{@ID}"><xsl:attribute name="$NAME"><xsl:value-of select="$MOVE"/></xsl:attribute></input></td>

    <!-- Up and down buttons -->
    <td align="center">

      <xsl:if test="position() != 1">
        <a href="{$baseActionURL}?action=reorder&amp;dir=up&amp;elementID={@ID}"><img border="0" height="17" width="15"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_ARROW_UP_GIF"/></xsl:attribute></img></a>
      </xsl:if>

    	<xsl:if test="position() != last()">
    	  <a href="{$baseActionURL}?action=reorder&amp;dir=down&amp;elementID={@ID}"><img border="0" height="17" width="15"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_ARROW_DOWN_GIF"/></xsl:attribute></img></a>
    	</xsl:if>

      <xsl:if test="position() = 1 and position() = last()">
        <img border="0" height="17" src="{$imageDir}/transparent1x1.gif" width="15"/>
  	  </xsl:if>

    </td>
    
    <td>
      <a href="{$baseActionURL}?action=browse&amp;folderID={@ID}"><img border="0" height="10" width="13"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_FOLDER_CLOSED_GIF"/></xsl:attribute></img>
      <xsl:value-of select="@name"/></a></td>
    <td><a href="{$baseActionURL}?action=editElement&amp;folderID={@ID}"><img border="0" height="12" width="12"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_EDIT_GIF"/></xsl:attribute></img></a></td>
  </tr>
</xsl:template>

<xsl:template match="channel">
  <tr>
    <td align="right"><input type="checkbox" value="{@ID}"><xsl:attribute><xsl:value-of select="$MOVE"/><xsl:attribute name="name"><xsl:value-of select="$NAME"/></xsl:attribute></xsl:attribute></input></td>
    <td align="center">
       
  	  <xsl:if test="position() != 1">
  	  	<a href="{$baseActionURL}?action=reorder&amp;dir=up&amp;elementID={@ID}"><img border="0" height="17" width="15"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_ARROW_UP_GIF"/></xsl:attribute></img></a>
  	  </xsl:if>

  	  <xsl:if test="position() != last()">
  	  	<a href="{$baseActionURL}?action=reorder&amp;dir=down&amp;elementID={@ID}"><img border="0" height="17" width="15"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_ARROW_DOWN_GIF"/></xsl:attribute></img></a>
      </xsl:if>

  	  <xsl:if test="position() = 1 and position() = last()">
  	    <img border="0" height="17" src="{$imageDir}/transparent1x1.gif" width="15"/>
  	  </xsl:if>

	  </td>   
	  <td><xsl:value-of select="@name"/></td>
    <td><a href="{$baseActionURL}?action=editElement&amp;folderID={@ID}"><img border="0" height="12" width="12"><xsl:attribute name="src"><xsl:value-of select="$IMAGE_SRC_IMAGEDIR_EDIT_GIF"/></xsl:attribute></img></a></td>
  </tr>
</xsl:template>

</xsl:stylesheet>