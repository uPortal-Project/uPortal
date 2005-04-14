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

   <xsl:choose>

    <xsl:when test="$mode='choose'">   
          <input type="hidden" name="form_action" value="2"/>
   
          <tr>
		   <td colspan='3'>
          <select size='10' name='chosen' class="uportal-input-text">
           <xsl:apply-templates select="people"/>   
          </select>
		   </td>
		  </tr>
	
           <tr>
            <td colspan='3'><xsl:text> </xsl:text></td> 
           </tr>		
           <tr>
     	    <td align='right' colspan='1'>
         	 <input type="submit" value="Fetch" class='uportal-button'/>
		    </td>

     	    <td align='center' colspan='1'>
         	 <input type="button" value="Search" class='uportal-button' onclick="this.form.form_action.value=4;this.form.submit();"/>
		    </td>

     	    <td align='left' colspan='1'>
         	 <input type="button" value="Cancel" class='uportal-button' onClick="this.form.form_action.value=0;this.form.submit();"/>
		    </td>
           </tr>
    </xsl:when>   

    <xsl:when test="$mode='search'">   
          <input type="hidden" name="form_action" value="4"/>
		  
		   <tr>
		    <td colspan='2' align='left'>
			 Enter all or part of a User Name,
			</td>
		   </tr>	
		   <tr>
		    <td colspan='2' align='left'>
			 First Name or Last Name:
			</td>
		   </tr>	

		   <tr>
		    <td colspan='2' align='center'>
			 <input type='text' name='search-str' class="uportal-input-text"/>
			</td>
		   </tr>	
		  
           <tr>
            <td colspan='2'><xsl:text> </xsl:text></td> 
           </tr>		

           <tr>
     	    <td align='right' colspan='1'>
         	 <input type="submit" value="Search" class='uportal-button'/>
		    </td>
     	    <td align='left' colspan='1'>
         	 <input type="button" value="Cancel" class='uportal-button' onClick="this.form.form_action.value=0;this.form.submit();"/>
		    </td>
           </tr>

    </xsl:when>   
   </xsl:choose>
	
          </table>
        </form>
  </xsl:template>

  <xsl:template match="people">
     <xsl:apply-templates select="person"/>   
  </xsl:template>

  <xsl:template match="person"> 
    
     <xsl:element name="option">
       <xsl:attribute name="value"><xsl:value-of select="user_name"/></xsl:attribute>     
     </xsl:element>
     <xsl:value-of select="user_name"/> - <xsl:value-of select="last_name"/>, <xsl:value-of select="first_name"/>

     <!--
       IF YOU IMPLEMENT YOUR OWN PASSWORD HANDLER, YOU WILL WANT TO REMOVE
       OR ALTER THE CONDITION BELOW FOR OPERATOR READABILITY
     -->
	 <xsl:if test="ENCRPTD_PSWD = 'Acc.Is.Locked' or encrptd_pswd = 'Acc.Is.Locked'">
        (inactive)
	 </xsl:if>
	 
  </xsl:template>
  
</xsl:stylesheet>