<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">Default</xsl:param>
<xsl:param name="categoryID">top</xsl:param>
<xsl:param name="modified">false</xsl:param>
<xsl:variable name="imageDir" select="'media/org/jasig/portal/channels/CSubscriber'"/>

<xsl:template match="/">
  <p align="center">Subscribe to Channels</p>
  <p align="center">
    <xsl:choose>
      <xsl:when test="$categoryID = 'top'">
        <xsl:for-each select="//registry">
          <xsl:call-template name="crumbTrail"/> 
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="//category[@ID=$categoryID]">
          <xsl:call-template name="crumbTrail"/>
        </xsl:for-each>
      </xsl:otherwise>
	  </xsl:choose>
	</p>
	  
  <form action="{$baseActionURL}" method="post">
    <input type="hidden" name="action" value="subscribe"/>

    <table align="center" border="1" cellspacing="0" cellpadding="5">
      <tr>
        <th><input type="submit" name="subTo" value="Subscribe to..."/></th>
        <th>Name</th>
        <th>Preview</th>
      </tr>

      <xsl:choose>
        <xsl:when test="$categoryID = 'top'">
          <xsl:for-each select="//registry">
            <xsl:call-template name="categoryRoot"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="//category[@ID=$categoryID]">
            <xsl:call-template name="categoryRoot"/>
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

<xsl:template name="categoryRoot">
  <xsl:apply-templates select="category|channel"/>
</xsl:template>

<xsl:template name="crumbTrail">Category: 

  <xsl:choose>
    <xsl:when test="name()='registry'">
      All Categories
    </xsl:when>
    <xsl:otherwise>    
      <a href="{$baseActionURL}action=browse&amp;categoryID=top">All Categories</a>
    </xsl:otherwise>
  </xsl:choose>

  <xsl:for-each select="ancestor::*">
    <a href="{$baseActionURL}action=browse&amp;categoryID={@ID}"><xsl:value-of select="@name"/></a>
    <xsl:text> &gt; </xsl:text>
  </xsl:for-each>

  <xsl:value-of select="@name"/>
</xsl:template>

<xsl:template match="category">
  <tr>
    <!-- Move checkbox -->
    <td align="right"><input type="checkbox" name="sub" value="{@ID}"/></td>
    
    <td>
      <a href="{$baseActionURL}action=browse&amp;categoryID={@ID}"><img src="{$imageDir}/folder_closed.gif" border="0" width="13" height="10"/>
      <xsl:value-of select="@name"/></a></td>
    <td><img src="{$imageDir}/blank.gif" border="0" /></td>
  </tr>
</xsl:template>

<xsl:template match="channel">
  <tr>
    <td align="right"><input type="checkbox" name="sub" value="{@ID}"/></td>
	  <td><xsl:value-of select="@name"/></td>
    <td><a href="{$baseActionURL}"><img src="{$imageDir}/preview.gif" border="0" /></a></td>
  </tr>
</xsl:template>

</xsl:stylesheet>
