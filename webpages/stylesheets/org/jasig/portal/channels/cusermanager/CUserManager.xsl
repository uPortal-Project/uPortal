<?xml version="1.0" encoding="utf-8" ?> 

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="no" /> 

  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="mode"/>
  <xsl:param name="message"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param> 

  <xsl:template match="/">

  <form action="{$baseActionURL}" method='post'>	

   <table border='0' cellspacing='2' colspacing='0' width='100%' class='uportal-channel-text' align='center'>

	<tr>
     <td width='20%'></td>

	 <td valign="top" align="center">
	 
      <table border='0' cellspacing='2' colspacing='0' class='uportal-channel-text' align='center'>

	  <!-- normal user display mode -->
          <input type="hidden" name="form_action" value="0"/>
	
           <xsl:apply-templates select="people"/>   

           <tr class='uportal-crumbtrail'>
            <td colspan='2'><hr width='90%' size='0'/></td> 
           </tr>		

           <tr>
     	    <td align='right' colspan='1'>
         	 <input type="button" value="Save/Update" class='uportal-button' onclick="this.form.form_action.value=1;this.form.submit();"/>
		    </td>
     	    <td align='left' colspan='1'>
         	 <input type="button" value="Set Password" class='uportal-button' onclick="this.form.form_action.value=7;this.form.submit();"/>
		    </td>
           </tr>

           <tr>
            <td colspan='2'><xsl:text> </xsl:text></td> 
           </tr>		

           <tr>
            <td colspan='1' align='right'>
			 <input type="button" value="Add New User" class='uportal-button' onclick="this.form.form_action.value=5;this.form.submit();"/>
			 <!--input type="button" value="Add New User" class='uportal-button' onclick="alert('This function suspended for JA-SIG CH.');"/-->
            </td> 
     	    <td align='left' colspan='1'>
         	 <input type="button" value="Select Other" class='uportal-button' onclick="this.form.form_action.value=3;this.form.submit();"/>
		    </td>
           </tr>		

           <tr>
            <td colspan='2'><xsl:text> </xsl:text></td> 
           </tr>		

           <tr>		
     	    <td align='right' colspan='1'>
         	 <input type="button" value="Delete This User" class='uportal-button' onclick="this.form.form_action.value=9;this.form.submit();"/>
		    </td>
     	    <td align='left' colspan='1'>
         	 <input type="button" value="Search Directory" class='uportal-button' onclick="this.form.form_action.value=4;this.form.submit();"/>
		    </td>
           </tr>		

          </table>
		  
		</td>

        <td valign="top" align="right" width='20%' class='uportal-channel-emphasis'>
         <xsl:value-of select="$message"/>
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

     <xsl:if test="name() != 'ENCRPTD_PSWD' and name() != 'encrptd_pswd'">
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
	 
	    <xsl:choose>
          <xsl:when test="name() = 'USER_NAME' or name() = 'LST_PSWD_CGH_DT' or name() = 'user_name' or name() = 'lst_pswd_cgh_dt'">
            <xsl:value-of select="."/>

            <xsl:element name="input">
              <xsl:attribute name="type">hidden</xsl:attribute>     
              <xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>     
              <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>     
            </xsl:element>
          </xsl:when>

          <xsl:otherwise>
           <xsl:element name="input">
             <xsl:attribute name="type">text</xsl:attribute>     
             <xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>     
             <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>     
             <xsl:attribute name="class">uportal-input-text</xsl:attribute>     
           </xsl:element>
         </xsl:otherwise>
	   </xsl:choose>
 		
        </th>  
       </tr>
     </xsl:if>

  </xsl:template>

</xsl:stylesheet>