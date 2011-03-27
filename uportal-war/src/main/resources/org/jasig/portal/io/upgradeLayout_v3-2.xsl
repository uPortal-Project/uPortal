<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dlm="http://www.uportal.org/layout/dlm" version="1.0">
    <xsl:template match="layout">
        <layout xmlns:dlm="http://www.uportal.org/layout/dlm" script="classpath://org/jasig/portal/io/import-layout_v3-2.crn">
            <xsl:copy-of select="@username"/>
            <xsl:apply-templates />
        </layout>
    </xsl:template>
    
    <xsl:template match="root|header|footer|tab|column">
        <folder>
            <xsl:attribute name="type">
                <xsl:choose>
                    <xsl:when test="name() = 'tab' or name() = 'column'">
                        <xsl:text>regular</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="name()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="param"/>
            <xsl:apply-templates />
        </folder>
    </xsl:template>
    
    <xsl:template match="channel">
        <xsl:copy>
            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="param"/>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="param[starts-with(name/text(), 'cp:')]">
        <xsl:attribute name="{concat('dlm:', substring-after(name/text(), 'cp:'))}">
            <xsl:value-of select="value"/>
        </xsl:attribute>
    </xsl:template>
    
    <xsl:template match="param">
        <!-- ignore -->
    </xsl:template>
    
    <xsl:template match="profile">
        <profile script="classpath://org/jasig/portal/io/import-profile_v3-2.crn">
            <xsl:attribute name="username">
                <xsl:value-of select="/layout/@username"/>
            </xsl:attribute>
            <name><xsl:value-of select="@name"/></name>
            <fname>default</fname>
            <xsl:copy-of select="description|structure|theme"/>
        </profile>
    </xsl:template>
    
    <xsl:template match="structure-attribute|theme-attribute|preferences">
        <xsl:copy-of select="."/>
    </xsl:template>
    
    <xsl:template match="@*[.='Y']">
        <xsl:attribute name="{name()}">
            <xsl:text>true</xsl:text>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="@*[.='N']|@*[.='']">
        <xsl:attribute name="{name()}">
            <xsl:text>false</xsl:text>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="@*">
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>
