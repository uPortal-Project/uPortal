<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

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
<input type="hidden" name="action" value="submitEditChoice"/>
<table width="100%" border="0"><tr><td align="left">profile name: <xsl:value-of select="$profileName"/></td><td align="right">
<input type="submit" name="submit" value="manage"/>
<select name="userPreferencesAction">
<option selected="" value="layout">channels and folders</option>
<option value="gpref">global preferences</option>
<option value="manageProfiles">profiles</option>
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
    <input type="hidden" name="action" value="move"/>

    <table align="center" border="1" cellspacing="0" cellpadding="5">
      <tr>
        <th><input type="submit" name="moveTo" value="Move to..."/></th>
        <th>Reorder</th>
        <th>Name</th>
        <th>Edit</th>
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
      <input type="hidden" name="action" value="saveChanges"/>
      <input type="submit" name="saveChanges" value="Save changes"/>    
    </form>
    </p><p/>
  </xsl:if>    
</xsl:template>

<xsl:template name="folderRoot">
  <xsl:apply-templates select="folder[not(@type='header' or @type='footer') and @hidden='false']|channel"/>
</xsl:template>

<xsl:template name="crumbTrail">Folder: 

  <xsl:choose>
    <xsl:when test="name()='layout'">
      My Channels
    </xsl:when>
    <xsl:otherwise>    
      <a href="{$baseActionURL}?action=browse&amp;folderID=root">My Channels</a>
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
    <td align="right"><input type="checkbox" name="move" value="{@ID}"/></td>

    <!-- Up and down buttons -->
    <td align="center">

      <xsl:if test="position() != 1">
        <a href="{$baseActionURL}?action=reorder&amp;dir=up&amp;elementID={@ID}"><img src="{$imageDir}/arrow_up.gif" border="0" width="15" height="17"/></a>
      </xsl:if>

    	<xsl:if test="position() != last()">
    	  <a href="{$baseActionURL}?action=reorder&amp;dir=down&amp;elementID={@ID}"><img src="{$imageDir}/arrow_down.gif" border="0" width="15" height="17"/></a>
    	</xsl:if>

      <xsl:if test="position() = 1 and position() = last()">
        <img src="{$imageDir}/transparent1x1.gif" border="0" width="15" height="17"/>
  	  </xsl:if>

    </td>
    
    <td>
      <a href="{$baseActionURL}?action=browse&amp;folderID={@ID}"><img src="{$imageDir}/folder_closed.gif" border="0" width="13" height="10"/>
      <xsl:value-of select="@name"/></a></td>
    <td><a href="{$baseActionURL}?action=editElement&amp;folderID={@ID}"><img src="{$imageDir}/edit.gif" border="0" width="12" height="12"/></a></td>
  </tr>
</xsl:template>

<xsl:template match="channel">
  <tr>
    <td align="right"><input type="checkbox" name="move" value="{@ID}"/></td>
    <td align="center">
       
  	  <xsl:if test="position() != 1">
  	  	<a href="{$baseActionURL}?action=reorder&amp;dir=up&amp;elementID={@ID}"><img src="{$imageDir}/arrow_up.gif" border="0" width="15" height="17"/></a>
  	  </xsl:if>

  	  <xsl:if test="position() != last()">
  	  	<a href="{$baseActionURL}?action=reorder&amp;dir=down&amp;elementID={@ID}"><img src="{$imageDir}/arrow_down.gif" border="0" width="15" height="17"/></a>
      </xsl:if>

  	  <xsl:if test="position() = 1 and position() = last()">
  	    <img src="{$imageDir}/transparent1x1.gif" border="0" width="15" height="17"/>
  	  </xsl:if>

	  </td>   
	  <td><xsl:value-of select="@name"/></td>
    <td><a href="{$baseActionURL}?action=editElement&amp;folderID={@ID}"><img src="{$imageDir}/edit.gif" border="0" width="12" height="12"/></a></td>
  </tr>
</xsl:template>

</xsl:stylesheet>
