<?xml version="1.0" encoding="utf-8" ?> 

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="no" /> 

  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="mode"/>
  <xsl:param name="User-Pwd-Only-Mode"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param> 

  <xsl:template match="*">

  <table boder="1" width="95%" cellspacing="0" cellpadding="0" align="center" class="uportal-channel-text">
   <tr>
    <td>

	   <xsl:if test="$User-Pwd-Only-Mode='yes'">
	     Changes your password for uPortal access.  You must enter your current password as verification
		 and the new password twice to ensure it was typed correctly.
		 <br/><br/>
	   </xsl:if>
	
	   <xsl:if test="$User-Pwd-Only-Mode='no'">
	     <ul>
		  <li>
		   Users are created in a simulated directory represented by the table "up_person_dir"
		   (see the "uPortal Data Dictionary" channel). 
           <br/><br/>
		  </li>
		  
		  <li>
		   If you need to assign group membership to users created within this channel, you must create
		   them and then login as that user once and logout again.  The first login will create the user
		   in uPortal.
           <br/><br/>
		  </li>
		  
		  <li>
		   For more information on uPortal interaction with directories for identity management, see the
		   PersonDirs.xml file in the properties directory.
           <br/><br/>
		  </li>
		  
		  <li>
           This channel uses a data handler interface.  A default implementation of this interface is provided 
		   to access the simulated directory table “up_person_dir” in the HSQL database provided with uPortal
		   quick-start.  Use of other identity mechanisms, and even databases, may require a different interface
		   implementation to allow this channel to correctly operate.  If you need to specify a different data
		   handler, you must make a channel parameter entry in the "up_chanel_param" table; this can be easily
		   done with the following SQL statement:
           <br/><br/>
		   
           <font class="uportal-channel-emphasis">
             insert into up_channel_param values ( (select chan_id from up_channel where chan_name='Password Management'),
			  'IDataHandler.class', '' ,'org.jasig.portal.channels.cusermanager.provider.DefaultDataHandlerImpl', 'N')
           </font>
           <br/><br/>
           replace the channel name and the implementation class name as appropriate.
           <br/><br/>
		  </li>

		  <li>
		   See also:
           <ul>
            <li><a target="_blank" href="http://www.uportal.org/implementors/usermgmt.html">uPortal User Management</a></li>
            <li><a target="_blank" href="http://www.uportal.org/implementors/services/ldap.html">Integrating LDAP into uPortal</a></li>
            <li><a target="_blank" href="http://www.uportal.org/docs.html">uPortal Documentation</a></li>
		   </ul>

           <br/>
		  </li>
		 </ul>
	   </xsl:if>

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
