<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dlm="http://www.uportal.org/layout/dlm"
  xmlns:fname="http://xml.apache.org/xalan/java/org.jasig.portal.dao.usertype.FunctionalNameType"
  extension-element-prefixes="fname"
  exclude-result-prefixes="fname"
  version="1.0">

    <xsl:template match="layout">
        <layout xmlns:dlm="http://www.uportal.org/layout/dlm" script="classpath://org/jasig/portal/io/import-layout_v3-2.crn">
            <xsl:copy-of select="@username"/>
            <xsl:apply-templates />
        </layout>
    </xsl:template>
    
    <xsl:template match="channel[@fname != 'header' and @fname != 'footer']">
        <xsl:copy>
            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="param"/>
            <xsl:apply-templates />
        </xsl:copy>
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
    
    <xsl:template match="structure-attribute|theme-attribute">
        <xsl:copy-of select="."/>
    </xsl:template>
    
    <xsl:template match="preferences">
        <xsl:copy>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>
    <xsl:template match="entry">
      <xsl:copy>
            <xsl:attribute name="entity">
                <xsl:call-template name="fix-dlm-path-ref">
                    <xsl:with-param name="path-ref" select="@entity"/>
                </xsl:call-template>
            </xsl:attribute>
          <xsl:attribute name="channel">
                <xsl:value-of select="fname:makeValid(@channel)"/>
            </xsl:attribute>
          <xsl:copy-of select="@name|value|text()"/>
        </xsl:copy>
    </xsl:template>
  
    <!--
     | Recursive search/replace template for updating DLM path refs
     +-->
    <xsl:template name="fix-dlm-path-ref">
        <xsl:param name="path-ref"/>
      
        <xsl:variable name="ROOT_PATH">root/</xsl:variable>
        <xsl:variable name="ROOT_FOLDER">folder[@type='root']/</xsl:variable>
        
        <xsl:variable name="TAB_PATH">/tab</xsl:variable>
        <xsl:variable name="COLUMN_PATH">/column</xsl:variable>
        <xsl:variable name="REGULAR_FOLDER">/folder[@type='regular']</xsl:variable>

        <xsl:choose>
            <xsl:when test="contains($path-ref, $ROOT_PATH)">
                <xsl:call-template name="fix-dlm-path-ref">
                    <xsl:with-param name="path-ref" select="concat(substring-before($path-ref, $ROOT_PATH), $ROOT_FOLDER, substring-after($path-ref, $ROOT_PATH))"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains($path-ref, $TAB_PATH)">
                <xsl:call-template name="fix-dlm-path-ref">
                    <xsl:with-param name="path-ref" select="concat(substring-before($path-ref, $TAB_PATH), $REGULAR_FOLDER, substring-after($path-ref, $TAB_PATH))"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains($path-ref, $COLUMN_PATH)">
                <xsl:call-template name="fix-dlm-path-ref">
                    <xsl:with-param name="path-ref" select="concat(substring-before($path-ref, $COLUMN_PATH), $REGULAR_FOLDER, substring-after($path-ref, $COLUMN_PATH))"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$path-ref"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="@fname">
        <xsl:attribute name="fname">
            <xsl:value-of select="fname:makeValid(.)"/>
        </xsl:attribute>
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
