<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="https://source.jasig.org/schemas/uportal/io/user" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output indent="yes"/>
    
    <xsl:template match="user">
        <user
            xmlns="https://source.jasig.org/schemas/uportal/io/user"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="4.0"
            xsi:schemaLocation="https://source.jasig.org/schemas/uportal/io/user https://source.jasig.org/schemas/uportal/io/user/user-4.0.xsd">
            
            <xsl:call-template name="upgradeUserData" />
        </user>
    </xsl:template>
    
    <xsl:template name="upgradeUserData">
        <xsl:copy-of select="@username"/>
        <xsl:if test="default-user">
            <default-user><xsl:value-of select="default-user"/></default-user>
        </xsl:if>
        <xsl:if test="person-directory/encrptd-pswd">
            <password><xsl:value-of select="person-directory/encrptd-pswd"/></password>
        </xsl:if>
        <xsl:apply-templates select="person-directory/*[name() != 'encrptd-pswd']" />
    </xsl:template>
    
    <xsl:template match="person-directory/*[name() != 'encrptd-pswd']">
        <attribute>
            <name><xsl:value-of select="name()"/></name>
            <value><xsl:value-of select="."/></value>
        </attribute>
    </xsl:template>
    
    
</xsl:stylesheet>