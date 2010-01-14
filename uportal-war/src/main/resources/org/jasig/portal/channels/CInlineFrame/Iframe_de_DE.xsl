<?xml version='1.0'?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="locale">de_DE</xsl:param>

    <xsl:template match="iframe">
        <iframe src="{url}" height="{height}" frameborder="no" width="100%">Dieser Browser unterstützt keine Inlineframes.<br/>
            <a href="{url}" target="_blank">Klicken Sie hier um den Inhalt,</a> in einem seperaten Fanster zu sehen.</iframe>
    </xsl:template>
</xsl:stylesheet>
