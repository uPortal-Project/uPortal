<xsl:template match="navigationBar">
 <table borde="0" cellpadding="2" cellspacing="3" width="100%">
 <tr><xsl:attribute name="bgcolour><xsl:value-of select="@bgcolour"/></xsl:attribute>
 <td><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL,'&amp;action=setActiveFolder&amp;folder=', @inbox)"/></xsl:attribute>Inbox</a></td>
 <td><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL,'&amp;action=listFolders&amp;folder=1')"/></xsl:attribute>Folders</a></td>
 <td><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL,'&amp;action=composeMessage')"/></xsl:attribute>Compose</a></td>
 <td>Addresses</td>
 <td>Set-ups</td>
 <td><a><xsl:attribute name="href"><xsl:value-of select="JavaScript:openWin('/help/hlp_email.htm', 'detachWindow', 500, 500)'"/></xsl:attribute>Help</a></td>
 </tr>
 </table>
</xsl:template>

 
