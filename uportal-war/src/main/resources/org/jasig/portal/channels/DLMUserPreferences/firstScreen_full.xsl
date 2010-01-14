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

<!-- $Header$ --><!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'--><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="locale">en_US</xsl:param>

<xsl:template match="profile">
<html>
<body>

<p align="center"><xsl:value-of select="$WHAT_WOULD_YOU_LIKE_TO_EDIT_"/></p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input name="action" type="hidden" value="submitEditChoice"/>
<p align="center">
<select name="manageTarget">
<option selected=""><xsl:value-of select="$MANAGE_USER_LAYOUT"/><xsl:attribute name="value"><xsl:value-of select="$LAYOUT"/></xsl:attribute></option>
<option value="gpref"><xsl:value-of select="$MANAGE_GLOBAL_PREFERENCES"/></option>
<option><xsl:value-of select="$PROFILES"/><xsl:attribute name="value"><xsl:value-of select="$PROF"/></xsl:attribute></option>
</select>
<br/>
<input name="submit" type="submit"><xsl:attribute name="value"><xsl:value-of select="$PROCEED"/></xsl:attribute></input>
</p>




</form>
</body>
</html>
</xsl:template>
</xsl:stylesheet>