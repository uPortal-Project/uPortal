<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="https://source.jasig.org/schemas/uportal/io/user" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output indent="yes"/>
    
    <xsl:import href="upgrade-user_3-2.xsl"/>
    
    <xsl:template match="user">
        <template-user
            xmlns="https://source.jasig.org/schemas/uportal/io/user"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="4.0"
            xsi:schemaLocation="https://source.jasig.org/schemas/uportal/io/user https://source.jasig.org/schemas/uportal/io/user/user-4.0.xsd">
            
            <xsl:call-template name="upgradeUserData" />
        </template-user>
    </xsl:template>
    
</xsl:stylesheet>