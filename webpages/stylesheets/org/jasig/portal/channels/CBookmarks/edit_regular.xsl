<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

<xsl:param name="baseActionURL">default</xsl:param>

  <xsl:template match="bookmarks">
      <table border="0" width="100%" cellspacing="5" cellpadding="0">
        <xsl:apply-templates select="bookmark"/>
      </table>
      <xsl:call-template name="buttons"/>
  </xsl:template>

  <xsl:template match="bookmark">
   <tr bgcolor="#eeeeee">
    <td> 
      <xsl:value-of select="@name"/>
    </td>
    <td>
     <a>
     <xsl:attribute name="href"><xsl:value-of select="@url"/></xsl:attribute>
      <xsl:value-of select="@url"/>
     </a>
    </td>
    <td>
     <xsl:value-of select="@comments"/>
    </td>
    <td>
     [<a><xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=edit&amp;bookmark=<xsl:number count="bookmark"/></xsl:attribute>edit</a>]
     [<a><xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?action=delete&amp;bookmark=<xsl:number count="bookmark"/></xsl:attribute>delete</a>]
    </td>
   </tr>
  </xsl:template>

  <xsl:template name="buttons">
   <table border="0"><tr><form>                                                                                
   <td><input type="button" name="add" value="Add Bookmark">
   <xsl:attribute name="onClick">location=<xsl:text>'</xsl:text><xsl:value-of select="$baseActionURL"/>?action=new<xsl:text>'</xsl:text></xsl:attribute></input>
   </td>
   <td><input type="button" name="finished" value="Finished">
      <xsl:attribute name="onClick">location='<xsl:value-of select="$baseActionURL"/>?action=doneEditing'</xsl:attribute></input>
   </td>
   </form></tr></table>
  </xsl:template>

</xsl:stylesheet>
