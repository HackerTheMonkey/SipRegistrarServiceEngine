<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:jbiwrp="http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper"
                xmlns:greet="http://www.sun.com/jbi/examples/sample-service/greetings/types/" 
>
    <xsl:output method="xml" indent="yes"/>    
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="greet:user">
        <greet:hello>
            <greet:first-name><xsl:value-of select="greet:first-name"/></greet:first-name>
            <greet:last-name><xsl:value-of select="greet:last-name"/></greet:last-name>
            <greet:greetings>Hello <xsl:value-of select="greet:last-name"/>!</greet:greetings>
        </greet:hello>
    </xsl:template>    
</xsl:stylesheet>


