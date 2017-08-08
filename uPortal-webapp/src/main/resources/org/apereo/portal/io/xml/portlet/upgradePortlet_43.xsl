<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:portlet="https://source.jasig.org/schemas/uportal/io/portlet-definition"
                xmlns:ns2="https://source.jasig.org/schemas/uportal">

    <xsl:output indent="yes"/>

    <xsl:template match="/">
        <portlet-definition xmlns="https://source.jasig.org/schemas/uportal/io/portlet-definition"
                xmlns:ns2="https://source.jasig.org/schemas/uportal"
                xmlns:ns3="https://source.jasig.org/schemas/uportal/io/permission-owner"
                xmlns:ns4="https://source.jasig.org/schemas/uportal/io/stylesheet-descriptor"
                xmlns:ns5="https://source.jasig.org/schemas/uportal/io/portlet-type"
                xmlns:ns6="https://source.jasig.org/schemas/uportal/io/user"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="https://source.jasig.org/schemas/uportal/io/portlet-definition https://source.jasig.org/schemas/uportal/io/portlet-definition/portlet-definition-5.0.xsd"
                version="5.0">
            <xsl:for-each select="*/*">
                <xsl:call-template name="child" />
            </xsl:for-each>
        </portlet-definition>
    </xsl:template>

    <xsl:template name="child">
        <xsl:choose>
            <!-- Only the lifecycle element(s) have changed -->
            <xsl:when test="local-name() = 'lifecycle'">
                <lifecycle>
                    <xsl:for-each select="*">
                        <entry name="{local-name()}" user="{@user}"><xsl:value-of select="text()" /></entry>
                    </xsl:for-each>
                    <xsl:if test="//*[local-name() = 'parameter']/*[local-name() = 'name' and text() = 'PortletLifecycleState.inMaintenanceMode']">
                        <!--
                         | Generating and formatting dates in XSLT is a special kind of torture;  we will use one
                         | second before the start of this millennium UTC (long before the advent of Import/Export)
                         | as a signal to use the current time.
                         +-->
                        <entry name="MAINTENANCE" user="system">1999-12-31T23:59:59.000Z</entry>
                    </xsl:if>
                </lifecycle>
            </xsl:when>
            <xsl:when test="local-name() = 'parameter' and child::*[local-name() = 'name' and text() = 'PortletLifecycleState.inMaintenanceMode']">
                <!-- Skip these here;  they're handled above -->
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="." />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
