<?xml version="1.0" encoding="utf-8" ?> 

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="no" /> 

  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="mode"/>
  <xsl:param name="User-Pwd-Only-Mode"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param> 

  <xsl:template match="*">

  <table boder="1" width="70%" cellspacing="0" cellpadding="0" align="center" class="uportal-channel-text">
   <tr>
    <td align="center">
	   <xsl:if test="$User-Pwd-Only-Mode='yes' or $User-Pwd-Only-Mode='no'">
	     Since uPortal version 2.5.
	   </xsl:if>
	
	   <xsl:if test="$User-Pwd-Only-Mode='not-active'">
	     About for Administrators.
	   </xsl:if>

	</td>
   </tr>
   
   <tr>
    <td>
	 <xsl:text> </xsl:text>
	</td>
   </tr>
   
   <tr>
    <td align="center">
	 <a href='{$baseActionURL}'>done</a>
	</td>
   </tr>
  </table> 
	 
  </xsl:template>
  
</xsl:stylesheet>
