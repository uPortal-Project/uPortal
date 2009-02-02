<?xml version='1.0' encoding='utf-8' ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="no"/>
    
    <!-- ========== VARIABLES & PARAMETERS ========== -->
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="unauthenticated">true</xsl:param>
    <xsl:param name="locale">en_US</xsl:param>
    <xsl:param name="mediaPath" select="'media/org/jasig/portal/channels/CLogin'"/>
    <xsl:param name="portalInstitutionName">JA-SIG</xsl:param>
    <xsl:param name="portalName">uPortal</xsl:param>
    <xsl:param name="forgotLoginUrl">http://www.uportal.org/</xsl:param>
    <xsl:param name="contactAdminUrl">http://www.uportal.org/</xsl:param>
    <xsl:param name="casLoginUrl"></xsl:param>
    <xsl:param name="casNewUserUrl"></xsl:param>
  <!-- ========== VARIABLES & PARAMETERS ========== -->
    
    <!-- ~ -->
    <!-- ~ Match on root element then check if the user is NOT authenticated-->
    <!-- ~ -->
    <xsl:template match="/">
        <xsl:if test="$unauthenticated='true'">
	        <xsl:apply-templates/>
        </xsl:if>
        <xsl:if test="$unauthenticated='false'">
          <div id="portalWelcome">
            <div id="portalWelcomeInner">
              <p>Welcome <xsl:value-of select="//login-status/full-name"/>. <span class="logout-label"><a href="Logout" title="Sign out">Sign out</a></span>
              </p>
            </div>
          </div>
        </xsl:if>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user is not authenticated insert login form-->
    <!-- ~ -->
    <xsl:template match="login-status">
		<div id="portalLogin">
    	  <xsl:choose>
    	    <!-- CAS Login link -->
    	    <xsl:when test="$casLoginUrl!= ''">
            <div id="portalLoginInner">
              <div id="portalCASLogin">
                <a id="portalCASLoginLink" href="{$casLoginUrl}" title="Sign In">
                  <span>Sign In <span class="via-cas">with CAS</span></span>
                </a>
                <p>New user? <a id="portalCASLoginNewLink" href="{$casNewUserUrl}" title="New User">Start here</a>.</p>
              </div>
            </div>
    	    </xsl:when>
    	    <!-- Username/password login form -->
    	    <xsl:otherwise>
    	      <h2>Sign In</h2>
    	      <xsl:apply-templates/>
    	      
    	      <form id="portalLoginForm" action="Login" method="post">
    	        <input type="hidden" name="action" value="login"/>
    	        <label for="userName">Username:</label>
    	        <input type="text" name="userName" size="15" value="{failure/@attemptedUserName}"/>
    	        <label for="password">Password:</label>
    	        <input type="password" name="password" size="15"/>
    	        <input type="submit" value="Sign In" name="Login" id="portalLoginButton" class="portlet-form-button"/>
    	      </form>
    	      <p><a href="{$forgotLoginUrl}">Forgot your username or password?</a></p>
      		</xsl:otherwise>
		  </xsl:choose>
		</div>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user login fails present error message box-->
    <!-- ~ -->
    <xsl:template match="failure">
    	<div id="portalLoginMessage">
        	<h2>Important Message</h2>
        	<p>The username and password you entered do not match any accounts on record. Please make sure that you have correctly entered the username associated with your <xsl:value-of select="$portalName"/> account.</p>
          <p><a href="{$forgotLoginUrl}">Forgot your username or password?</a></p>
        </div>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user login encounters an error present error message box-->
    <!-- ~ -->
    <xsl:template match="error">
        <div id="portalLoginMessage">
        	<h2>Important Message</h2>
        	<p><xsl:value-of select="$portalName"/> is unable to complete your login request at this time. It is possible the system is down for maintenance or other reasons. Please try again later. If this problem persists, contact <a href="{$contactAdminUrl}"><xsl:value-of select="$portalInstitutionName"/></a></p>
        </div>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ error message box-->
    <!-- ~ -->
    <xsl:template name="message">
       <xsl:param name="messageString"/>
       <div id="portalLoginMessage">
			<h2>Important Message</h2>
            <xsl:value-of select="$messageString"/>
       </div>
    </xsl:template>
</xsl:stylesheet>
