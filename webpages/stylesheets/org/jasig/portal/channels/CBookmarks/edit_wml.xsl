<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

<xsl:param name="baseActionURL">default</xsl:param>

  <xsl:template match="bookmarks">
     <do type="done" label="Done Editing">
      <go>
	<xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>action=doneEditing</xsl:attribute>
      </go>
     </do>   
     <do type="addnew" label="Add New Bookmark">
      <go>
	<xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>action=new</xsl:attribute>
      </go>
     </do>
  
     <p> <xsl:text>Selected bookmark: </xsl:text> </p>
     <p align="center">
	<select name="selectedb">
         <xsl:apply-templates select="bookmark"/>
	</select>
     </p>

     <do type="prev" label="Edit">
      <go>
        <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=edit&amp;bookmark=<xsl:text disable-output-escaping="yes">$(selectedb)</xsl:text></xsl:attribute>
      </go>
     </do>
   
     <do type="deleteb" label="Delete Bookmark">
      <go>
        <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=delete&amp;bookmark=<xsl:text disable-output-escaping="yes">$(selectedb)</xsl:text></xsl:attribute>
      </go>
     </do>
  

  </xsl:template>

  <xsl:template match="bookmark">
    <option>
       <xsl:attribute name="value"><xsl:number count="bookmark"/></xsl:attribute>
       <xsl:value-of select="@name"/> 
    </option>
  </xsl:template>

</xsl:stylesheet>