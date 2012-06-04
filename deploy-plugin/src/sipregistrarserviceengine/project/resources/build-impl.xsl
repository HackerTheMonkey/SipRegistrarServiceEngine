<?xml version="1.0" encoding="UTF-8"?>
<!--
The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.

You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.

When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:jbiproject="http://www.netbeans.org/ns/jbimodules/SipRegistrarServiceEngine/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                exclude-result-prefixes="xalan p projdeps projdeps2">
<!-- XXX should use namespaces for NB in-VM tasks from ant/browsetask and debuggerjpda/ant (Ant 1.6.1 and higher only) -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***

For the purpose of easier reading the script
is divided into following sections:

  - initialization
  - compilation
  - jar
  - cleanup

]]></xsl:comment>

        <xsl:variable name="name" select="/p:project/p:configuration/jbiproject:data/jbiproject:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            
            <target name="default">
                <xsl:attribute name="depends">jar</xsl:attribute>
                <xsl:attribute name="description">Build whole project.</xsl:attribute>
            </target>

            <xsl:comment> 
    ======================
    INITIALIZATION SECTION 
    ======================
    </xsl:comment>

            <target name="-pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-private">
                <xsl:attribute name="depends">-pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>

            <target name="-init-user">
                <xsl:attribute name="depends">-pre-init,-init-private</xsl:attribute>
                <property file="${{user.properties.file}}"/>
            </target>

            <target name="-init-project">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>

            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project</xsl:attribute>
                <property name="build.jbi.su.dir" location="${{build.dir}}/${{jbi.su.name}}" />
            </target>

            <target name="-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-check">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-do-init</xsl:attribute>
                <fail unless="src.dir">Must set src.dir</fail>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="jbi.su.name">Must set jbi.su.name</fail>
                <fail unless="jbi.su.zip">Must set jbi.su.zip</fail>
            </target>            
            
            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-do-init,-post-init,-init-check</xsl:attribute>
            </target>

            <xsl:comment>
    ===================
    COMPILATION SECTION
    ===================
    </xsl:comment>
                        
            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{build.jbi.su.dir}}"/>
            </target>

            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-compile">
                <xsl:attribute name="depends">init,-pre-pre-compile,-pre-compile</xsl:attribute>
                <mkdir dir="${{build.jbi.su.dir}}"/>
                <copy file="${{src.dir}}/META-INF/jbi.xml" todir="${{build.jbi.su.dir}}/META-INF" overwrite="true" />
                <copy  todir="${{build.jbi.su.dir}}" overwrite="true" >
                    <fileset dir="${{src.dir}}" >
                    </fileset>
                </copy>        
            </target>

            <target name="-post-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="compile">
                <xsl:attribute name="depends">init,-pre-pre-compile,-pre-compile,-do-compile,-post-compile</xsl:attribute>
                <xsl:attribute name="description">Compile project.</xsl:attribute>
            </target>

            <xsl:comment>
    ====================
    JAR BUILDING SECTION
    ====================
    </xsl:comment>

            <target name="-pre-pre-jar">
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{build.dir}}"/>                
            </target>

            <target name="-pre-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-jar">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar</xsl:attribute>
                <zip destfile="${{jbi.su.zip}}" basedir="${{build.jbi.su.dir}}" />
            </target>
            
            <target name="-post-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="jar">
                <xsl:attribute name="depends">init,compile,-pre-jar,-do-jar,-post-jar</xsl:attribute>
                <xsl:attribute name="description">Build JAR.</xsl:attribute>
            </target>

            <target name="dist">
                <xsl:attribute name="depends">init,jar</xsl:attribute>
            </target>
            
            <target name="dist_se">
                <xsl:attribute name="depends">init,jar</xsl:attribute>
            </target>
            
    <xsl:comment>
    ===============
    CLEANUP SECTION
    ===============
    </xsl:comment>

            <target name="-do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <delete dir="${{build.dir}}"/>
                <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
            </target>

            <target name="-post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="clean">
                <xsl:attribute name="depends">init,-do-clean,-post-clean</xsl:attribute>
                <xsl:attribute name="description">Clean build products.</xsl:attribute>
            </target>

        </project>

    </xsl:template>

</xsl:stylesheet>
