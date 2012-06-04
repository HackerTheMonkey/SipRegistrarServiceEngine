/*
 * SEPluginProjectProperties.java
 *
 */

package sipregistrarserviceengine.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author chikkala
 */
public class SEPluginProjectProperties
{
    
    public static final String DEFAULT_PLATFORM = "default_platform"; // NOI18N
    
    public static final String SOURCES_TYPE_XML = "xml";
    public static final String SOURCES_TYPE_JAVA = "java";
    
    public static final String SE_PLUGIN_PROJECT_ICON_PATH = "sipregistrarserviceengine/project/resources/projectIcon.png"; // NOI18N"
    /**
     * comp app callable ant build targets see SEPluginProject.AntArtifactProviderImpl
     * for more info. When a composite application project builds the service assembly
     * it calls these targets to build a service unit added by this project.
     */
    public static final String BUILD_TARGET_DIST = "dist_se"; // NOI18N
    public static final String BUILD_TARGET_CLEAN = "clean"; // NOI18N
    /**
     * Ant artifact type value required for plugin to compapp project
     * see SEPluginProject.AntArtifactProviderImpl for more info.
     * The value used is "CAPS.asa:<TargetComponentName> where <TargetComponentName>
     * is the name of the service engine to which the service unit created by this
     * project will be deployed.
     */
    public static final String ARTIFACT_TYPE_JBI_SU_PREFIX = "CAPS.asa:"; // NOI18N
    /**
     * service unit target property. The value is the component name of the service
     * engine to which the service unit created by this project will be deployed.
     */
    public static final String JBI_SU_TARGET_NAME = "jbi.su.target.name"; // NOI18N
    /**
     * Name of the service engine
     */
    public static final String JBI_SU_TARGET_NAME_VALUE = "SipRegistrarServiceEngine"; // NOI18N
    
    public static final String ARTIFACT_TYPE_JAR = "jar"; // NOI18N
    
    public static final String JBI_SU_NAME = "jbi.su.name"; // NOI18N
    public static final String JBI_SU_NAME_VALUE = "ServiceUnit"; // NOI18N
    public static final String JBI_SU_DESCRIPTION = "jbi.su.description"; // NOI18N
    public static final String JBI_SU_DESCRIPTION_VALUE = "Service unit description"; // NOI18N
    
    public static final String JBI_SU_ZIP = "jbi.su.zip";
    /**
     * bug in the comp app project build system prevents us to define the service unit
     * archive file name and location to be any thing. You must define a hardcoded
     * name for the su archive.
     * TODO: will use the ${build.dir}/${jbi.su.name}.zip when compapp bug is fixed.
     */
    public static final String JBI_SU_ZIP_VALUE = "${build.dir}/SEDeployment.jar";
    // public static final String JBI_SU_ZIP_VALUE = "${build.dir}/${jbi.su.name}.zip";
    
    /**
     * these properties can be used in the build script that produces the service unit
     * archive file.
     */
    public static final String JAR_COMPRESS = "jar.compress"; // NOI18N
    public static final String BUILD_FILES_EXCLUDES = "build.files.excludes"; // NOI18N
    
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    public static final String BUILD_DIR_VALUE = "build"; // NOI18N
    public static final String JBI_SU_BUILD_DIR_VALUE = "${build.dir}/${jbi.su.name}"; // NOI18N
    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String SRC_DIR_VALUE = "src"; // NOI18N
    public static final String SU_JBI_XML_PATH = "META-INF/jbi.xml"; // NOI18N
    
    public static FileObject getSourceDirectory(SEPluginProject project )
    {
        AntProjectHelper helper = project.getAntProjectHelper();
        String srcDir = helper.getStandardPropertyEvaluator().getProperty(SRC_DIR); // NOI18N
        return helper.resolveFileObject(srcDir);
    }
    
    public static FileObject createDefaultSUDescriptor(FileObject srcFolder) throws IOException
    {
        FileObject jbiXmlFO = null;
        
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("<?xml version='1.0' encoding=\"UTF-8\" standalone=\"yes\" ?>");
        out.println("<jbi version=\"1.0\"");
        out.println("   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        out.println("   xmlns=\"http://java.sun.com/xml/ns/jbi\"");
        out.println(">");
        out.println("   <services binding-component=\"false\">");
        out.println("       <!-- TODO: add <provides> and <consumes> elements here -->");
        out.println("       <!--");
        out.println("           <provides service-name=\"ns1:MyService\"");
        out.println("               interface-name=\"ns1:MyPortType\"");
        out.println("               endpoint-name=\"ServiceEngine_JBIPort\">");
        out.println("           </provides>");
        out.println("       -->");
        out.println("   </services>");
        out.println("</jbi>");
        out.close();
        writer.close();
        
        jbiXmlFO = FileUtil.createData(srcFolder, SU_JBI_XML_PATH);
        saveToFileObject(jbiXmlFO, writer.getBuffer());
        
        return jbiXmlFO;
    }
    
    public static void saveToFileObject(FileObject outFO, StringBuffer srcBuff)
    {
        FileLock outLock = null;
        OutputStream outS = null;
        InputStream inS = null;
        
        try
        {
            inS = new ByteArrayInputStream(srcBuff.toString().getBytes());
            outLock = outFO.lock();
            outS = outFO.getOutputStream(outLock);
            FileUtil.copy(inS, outS);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (inS != null)
            {
                try
                {
                    inS.close();
                }
                catch (Exception ex)
                {
                    //ingore
                }
            }
            if (outS != null)
            {
                try
                {
                    outS.close();
                }
                catch (Exception ex)
                {
                    //ingore
                }
            }
            if (outLock != null)
            {
                outLock.releaseLock();
            }
        }
    }
}
