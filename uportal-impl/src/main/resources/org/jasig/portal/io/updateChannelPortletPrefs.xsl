<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:key name="distinct-parameter" match="name" use="."/>
    
    <xsl:template match="channel-definition">
        <channel-definition script="classpath://org/jasig/portal/io/import-channel_v3-1.crn"> 
            <xsl:apply-templates select="node()"/>
        </channel-definition>
    </xsl:template>

    <xsl:template match="parameters">
        <xsl:copy>
            <!-- Copy all non-portlet parameters -->
            <xsl:for-each select="parameter[not(starts-with(name, 'PORTLET.'))]">
                <xsl:copy>
                    <xsl:apply-templates/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:copy>

        <xsl:if test="count(parameter[starts-with(name, 'PORTLET.')]) > 0">
            <portletPreferences>
                <!-- Select all portlet preference parameters, then filter for unique names -->
                <xsl:for-each select="parameter[starts-with(name, 'PORTLET.')][generate-id(name)=generate-id(key('distinct-parameter',name))]">
                    <xsl:sort select="substring-after(name, 'PORTLET.')"/>
                    <portletPreference>
                        <name>
                            <xsl:value-of select="substring-after(name, 'PORTLET.')"/>
                        </name>
                        <read-only>
                            <xsl:choose>
                                <xsl:when test="ovrd = 'Y'">FALSE</xsl:when>
                                <xsl:otherwise>TRUE</xsl:otherwise>
                            </xsl:choose>
                        </read-only>
                        <values>
                            <xsl:variable name="PREF_NAME" select="name"/>
                            <xsl:for-each select="/channel-definition/parameters/parameter[name=$PREF_NAME]">
                                <value>
                                    <xsl:value-of select="value"/>
                                </value>
                            </xsl:for-each>
                        </values>
                    </portletPreference>
                </xsl:for-each>
            </portletPreferences>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
