<?xml version="1.0" encoding="utf-8" ?> 

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="no" /> 

  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="mode"/>
  <xsl:param name="message"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param> 

  <xsl:template match="/">

    <form action="{$baseActionURL}" method='post'>	
      <table border='0' cellspacing='2' colspacing='0' class='uportal-channel-text' align='center'>

	  <!-- add user display mode -->
      <input type="hidden" name="form_action" value="6"/>
	
       <xsl:apply-templates select="people"/>   

       <tr>
        <td colspan='2'><xsl:text> </xsl:text></td> 
       </tr>		
       <tr>
 	    <td align='right' colspan='1'>
       	 <input type="submit" value="Save" class='uportal-button'/>
	    </td>
   	    <td align='left' colspan='1'>
         <input type="button" value="Cancel" class='uportal-button' onClick="this.form.form_action.value=0;this.form.submit();"/>
        </td>
       </tr>

      </table>
    </form>
	
  </xsl:template>
  
  <xsl:template match="people">
     <xsl:apply-templates select="person"/>   
  </xsl:template>

  <xsl:template match="person">
      <xsl:apply-templates select="*"/>   
  </xsl:template>

  <!-- for display of fields -->
  <xsl:template match="*">

    <xsl:if test="name() != 'ENCRPTD_PSWD' and name() != 'LST_PSWD_CGH_DT' and name() != 'encrptd_pswd' and name() != 'lst_pswd_cgh_dt'">
       <tr>
        <td align="right">

	      <xsl:choose>
            <xsl:when test="name() = 'LST_PSWD_CGH_DT' or name() = 'lst_pswd_cgh_dt'">Last password change:</xsl:when>
            <xsl:when test="name() = 'USER_NAME' or name() = 'user_name'">User name:</xsl:when>
            <xsl:when test="name() = 'EMAIL' or name() = 'email'">Email address:</xsl:when>
            <xsl:when test="name() = 'FIRST_NAME' or name() = 'first_name'">First name:</xsl:when>
            <xsl:when test="name() = 'LAST_NAME' or name() = 'last_name'">Last name:</xsl:when>

            <!-- Displays unknown field names -->  
            <xsl:otherwise><xsl:value-of select="name()"/>:</xsl:otherwise>
	      </xsl:choose>

        </td>  

        <th align="left">
	 
         <xsl:element name="input">
           <xsl:attribute name="type">text</xsl:attribute>     
           <xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>     
           <xsl:attribute name="value"></xsl:attribute>     
           <xsl:attribute name="class">uportal-input-text</xsl:attribute>     
         </xsl:element>
 		
        </th>  
       </tr>
    </xsl:if>

  </xsl:template>
  
  

</xsl:stylesheet>