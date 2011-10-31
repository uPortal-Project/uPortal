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
<xsl:stylesheet xmlns="https://source.jasig.org/schemas/uportal/io/user" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="upgrade-user_3-2.xsl"/>

    <xsl:output indent="yes"/>
    
    <xsl:template match="user">
        <template-user
            xmlns="https://source.jasig.org/schemas/uportal/io/user"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="4.0"
            xsi:schemaLocation="https://source.jasig.org/schemas/uportal/io/user https://source.jasig.org/schemas/uportal/io/user/user-4.0.xsd">
            
            <xsl:call-template name="upgradeUserData" />
        </template-user>
    </xsl:template>
    
</xsl:stylesheet>