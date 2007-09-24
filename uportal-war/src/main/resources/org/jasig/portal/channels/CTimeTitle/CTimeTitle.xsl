<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>

	<xsl:template match="/">
		<div class="uportal-channel-table-caption">
			This channel displays the time at which it was rendered in its content.
			And it returns the current time as its desired title via ChannelRuntimeProperties.
			It demonstrates the dynamic channel titles feature.</div>


		<xsl:apply-templates select="time"/>
	</xsl:template>

	<xsl:template match="time">
		<div class="uportal-text">
		    <xsl:value-of select="self::time"/>
		</div>
	</xsl:template>

</xsl:stylesheet>