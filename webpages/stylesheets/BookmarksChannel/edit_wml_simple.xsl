<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

<xsl:param name="baseActionURL">default</xsl:param>

  <xsl:template match="bookmarks">
     <do type="addnew" label="Add New Bookmark">
      <go>
	<xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>action=new</xsl:attribute>
      </go>
     </do>
     <do type="done" label="Done Editing">
      <go>
	<xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>action=doneEditing</xsl:attribute>
      </go>
     </do>     
	 
     <p><em>Select a bookmark to edit</em></p>
     <p>
      <table columns="1">
        <xsl:apply-templates select="bookmark"/>
      </table>
     </p>
  </xsl:template>

  <xsl:template match="bookmark">
   <tr>
    <td>
      <a>
      <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=edit&amp;bookmark=<xsl:number count="bookmark"/></xsl:attribute>
      <xsl:value-of select="@name"/> 
      </a>
    </td>
   </tr>
  </xsl:template>

</xsl:stylesheet>