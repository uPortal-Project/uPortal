<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:msxsl="urn:schemas-microsoft-com:xslt" xmlns:debug="http://www.ja-sig.org/uPortal">

<!-- a little JScript for debugging under MSXML -->
<msxsl:script language="JScript" implements-prefix="debug">
function xml(nodelist){
        return nodelist.nextNode().xml;
}
</msxsl:script>

	<xsl:param name="baseActionURL">Default</xsl:param>
	<xsl:param name="currentStep" select="1"/>
	<xsl:param name="numSteps" select="count(*/params/step)"/>
	<xsl:param name="modified">false</xsl:param>
	<xsl:param name="mode">publish</xsl:param>
	<xsl:param name="profileName">default profile</xsl:param>
	<xsl:variable name="imageDir" select="'media/org/jasig/portal/channels/CPublisher'"/>
	<xsl:param name="defaultLength">10</xsl:param>
	<xsl:param name="defaultMaxLength">20</xsl:param>
	<xsl:param name="defaultTextCols">40</xsl:param>
	<xsl:param name="defaultTextRows">10</xsl:param>
		
	<!-- Display the channel types available for publish-->
	<xsl:template match="channelTypes">
		<p align="center">Pick the channel type you wish to publish.		
		<form action="{$baseActionURL}" method="post">
			<input type="hidden" name="action" value="choose"/>
			<select name="channel">
				<option value="">select one...</option>
				<xsl:for-each select="channelType">
					<option value="{definition}">
						<xsl:value-of select="name"/>
					</option>
				</xsl:for-each>
			</select>
			<input type="submit" name="choose" value="Choose"/>
		</form>
		<br/>
		</p>
	</xsl:template>
	
	<xsl:template match="channelDef">
	<xsl:choose>
	<xsl:when test="$currentStep='end'">
	  	<p align="center">Channel is now published!</p>
	</xsl:when>
	<xsl:otherwise>
        <form action="{$baseActionURL}" method="post">
      	  <input type="hidden" name="action" value="publish"/>
	  <input type="hidden" name="currentStep" value="{$currentStep}"/>
	  <input type="hidden" name="numSteps" value="{$numSteps}"/>
		<!--<xsl:param name="numSteps" select="count(params/step)"/>-->
		<p align="center">
			<xsl:value-of select="description"/>
		</p>
		<p align="left">Step <xsl:value-of select="$currentStep"/> of <xsl:value-of select="$numSteps"/>
		</p>
		<xsl:apply-templates select="params/step[ID=$currentStep]"/>
      	  <p align="center"><input type="submit" value="Next"/></p>
        </form>
      </xsl:otherwise>
	</xsl:choose>
	<form action="{$baseActionURL}" method="post">
	<input type="hidden" name="action" value="cancel"/>
      	  <p align="center"><input type="submit"  value="Cancel"/></p>
	</form>
	</xsl:template>
	
	<!-- The current step info-->
	<xsl:template match="step">
		<!-- DEBUG <xsl:value-of select="debug:xml(.)"/> DEBUG-->
		<p align="left">
			<xsl:value-of select="name"/>
		</p>
		<table align="center" border="1" cellpadding="5" cellspacing="0">
			<tr>
				<td>Label</td>
				<td>Data</td>
				<td>Subscribe?</td>
				<xsl:apply-templates select="parameter"/>
			</tr>
		</table>
	</xsl:template>
	<!-- Display the parameters that are NOT subscribe-only-->
	<xsl:template match="parameter">
		<xsl:if test="@modify != 'subscribe-only'">
			<tr>
				<xsl:choose>
					<xsl:when test="type/base='text'">
						<xsl:call-template name="text"/>
					</xsl:when>
					<xsl:when test="type/base='single-choice'">
						<xsl:call-template name="single-choice"/>
					</xsl:when>
					<xsl:when test="type/base='multi-choice'">
						<xsl:call-template name="multi-choice"/>
					</xsl:when>
				</xsl:choose>
			</tr>
		</xsl:if>
	</xsl:template>
	
	<!-- displays checkbox for publisher to allow subscribe time modification-->
	<xsl:template name="subscribe">
		<td>
			<!-- <xsl:value-of select="@modify"/>  just for debug -->
			<xsl:if test="@modify!='publish-only'">
				<input type="checkbox" name="{name}_sub">
					<xsl:if test="@modify='subscribe'">
						<xsl:attribute name="checked">checked</xsl:attribute>
					</xsl:if>
				</input>
			</xsl:if>
		</td>
	</xsl:template>
	
	<!-- display all the input fields with a base type of 'single-choice'-->
	<xsl:template name="single-choice">
		<xsl:choose>
			<xsl:when test="type/display='drop-down'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<select name="{name}">
						<xsl:for-each select="type/restriction/value">
							<option value="{.}">
								<xsl:if test="@default='true'">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:when test="type/display='radio'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<xsl:for-each select="type/restriction/value">
						<input type="radio" name="{name}" value="{.}">
							<xsl:if test="@default='true'">
								<xsl:attribute name="checked">checked</xsl:attribute>
							</xsl:if>
						</input>
						<xsl:value-of select="."/>
					</xsl:for-each>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:otherwise>
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<select name="{name}">
						<xsl:for-each select="type/restriction/value">
							<option value="{.}">
								<xsl:if test="@default='true'">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- display all the input fields with a base type of 'multi-choice'-->
	<xsl:template name="multi-choice">
		<xsl:choose>
			<xsl:when test="type/display='select-list'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<select name="{name}" size="6" multiple="multiple">
						<xsl:for-each select="type/restriction/value">
							<option value="{.}">
								<xsl:if test="@default='true'">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:when test="type/display='checkbox'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<xsl:for-each select="type/restriction/value">
						<input type="checkbox" name="{name}" value="{.}">
							<xsl:if test="@default='true'">
								<xsl:attribute name="checked">checked</xsl:attribute>
							</xsl:if>
						</input>
						<xsl:value-of select="."/>
					</xsl:for-each>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:otherwise>
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<select name="{name}" size="6" multiple="multiple">
						<xsl:for-each select="type/restriction/value">
							<option value="{.}">
								<xsl:if test="@default='true'">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- display all the input fields with a base type of 'text'-->
	<xsl:template name="text">
		<!-- since length and maxlength are not required test existence and use defaults if needed -->
		<xsl:variable name="length">
			<xsl:choose>
				<xsl:when test="type/length"> <xsl:value-of select="type/length"/> </xsl:when>
				<xsl:otherwise> <xsl:value-of select="$defaultLength"/> </xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="maxlength">
			<xsl:choose>
				<xsl:when test="type/maxlength"> <xsl:value-of select="type/maxlength"/> </xsl:when>
				<xsl:otherwise> <xsl:value-of select="$defaultMaxLength"/> </xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="type/display='text'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<input type="text" name="{name}" value="{defaultValue}" maxlength="{$maxlength}" length="{$length}"/>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:when test="type/display='textarea'">
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<textarea rows="{$defaultTextRows}" cols="{$defaultTextCols}">
						<xsl:value-of select="defaultValue"/>
					</textarea>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:when>
			<xsl:when test="type/display='hidden'">
				<input type="hidden" name="{name}" value="{defaultValue}"/>
			</xsl:when>
			<xsl:otherwise>
				<td>
					<xsl:value-of select="label"/>:</td>
				<td>
					<input type="text" name="{name}" value="{defaultValue}" maxlength="{$maxlength}" length="{$length}"/>
				</td>
				<xsl:call-template name="subscribe"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
