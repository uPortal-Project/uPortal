<?xml version="1.0" encoding="utf-8" ?> 

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="no" /> 

  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="mode"/>
  <xsl:param name="message"/>
  <xsl:param name="User-Pwd-Only-Mode"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param> 

  <xsl:template match="/">
  
     <script language="javascript">
	  function _dbl_check_passwords( dform ) {
	  
        if( dform.encrptd_pswd.value != dform.ensure_encrptd_pswd.value ) {
           alert( "New and re-entered passwords do not match!" );
		   dform.encrptd_pswd.value = "";
		   dform.ensure_encrptd_pswd.value = "";
		   dform.encrptd_pswd.focus();
		 }else
           dform.submit();		

	  }//_dbl_check_passwords
     </script>

     <form action="{$baseActionURL}" method='post'>	
      <table border='0' cellspacing='2' colspacing='0' class='uportal-channel-text' align='center'>

	  <!-- set pwd -->
      <input type="hidden" name="form_action" value="8"/>

      <xsl:element name="input">
        <xsl:attribute name="type">hidden</xsl:attribute>     
        <xsl:attribute name="name">user_name</xsl:attribute>     
        <xsl:attribute name="value"><xsl:value-of select="people/person/user_name"/></xsl:attribute>     
      </xsl:element>

      <xsl:if test="$User-Pwd-Only-Mode='yes'">
       <tr>
        <td colspan='1' align='right'>Enter current password: </td> 
        <td colspan='1' align='left'>
		 <input type="password" name="pswd" class='uportal-input-text'/>
		</td> 
       </tr>		
	  
       <tr>
        <td colspan='2'><xsl:text> </xsl:text></td> 
       </tr>		
      </xsl:if> 

       <tr>
        <td colspan='1' align='right'>
		  Enter new password<xsl:if test="$User-Pwd-Only-Mode='no'">
		   (<xsl:value-of select="people/person/user_name"/>)</xsl:if>:
		   
        </td> 
        <td colspan='1' align='left'><input type="password" name="encrptd_pswd" class='uportal-input-text'/></td> 
       </tr>		

       <tr>
        <td colspan='1' align='right'>Re-enter new password:</td> 
        <td colspan='1' align='left'><input type="password" name="ensure_encrptd_pswd" class='uportal-input-text'/></td> 
       </tr>		
	
       <tr>
        <td colspan='2'><xsl:text> </xsl:text></td> 
       </tr>		
       <tr>
	   
	   <xsl:choose>
        <xsl:when test="$User-Pwd-Only-Mode='yes'">
         <td align='center' colspan='2'>
          <input type="button" value="Set Password" class='uportal-button' onClick="_dbl_check_passwords( this.form );"/>
	     </td>
        </xsl:when>

        <xsl:otherwise>
         <td align='right' colspan='1'>
          <input type="button" value="Set Password" class='uportal-button' onClick="_dbl_check_passwords( this.form );"/>
	     </td>

  	     <td align='left' colspan='1'>
          <input type="button" value="Cancel" class='uportal-button' onClick="this.form.form_action.value=0;this.form.submit();"/>
	     </td>
        </xsl:otherwise>
	   </xsl:choose>
       </tr>

       <!-- If there is a message, display it. -->
	   <xsl:if test="$message != ''">
	    <tr><td colspan="2"><xsl:text> </xsl:text></td></tr>
        <tr><td colspan="2" align="center" class="uportal-channel-emphasis"><xsl:value-of select="$message"/></td></tr>
       </xsl:if>
      </table>
     </form>
  </xsl:template>

</xsl:stylesheet>