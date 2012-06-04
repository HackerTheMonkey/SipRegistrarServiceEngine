/*
 * SEPluginProjectGenerator.java
 *
 */

package sipregistrarserviceengine.project;

import java.io.File;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a Deployment Plugin Project for Service Engine.
 * @author chikkala
 */
public class SEPluginProjectGenerator
{
    
    private File mPrjDir;
    private String mPrjName;
    private String mSUName;
    private String mSUDesc;
    private String mSUTarget;
    
    public SEPluginProjectGenerator()
    {
        this.mSUName = SEPluginProjectProperties.JBI_SU_NAME_VALUE;
        this.mSUDesc = SEPluginProjectProperties.JBI_SU_DESCRIPTION_VALUE;
        this.mSUTarget = SEPluginProjectProperties.JBI_SU_TARGET_NAME_VALUE;
    }
    
    public File getProjectDirectory()
    {
        return this.mPrjDir;
    }
    
    public String getProjectName()
    {
        return this.mPrjName;
    }
    
    public String getSUName()
    {
        return this.mSUName;
    }
    
    public void setSUName(String suName)
    {
        this.mSUName = suName;
    }
    
    public String getSUDescription()
    {
        return this.mSUDesc;
    }
    
    public void setSUDescription(String suDesc)
    {
        this.mSUDesc = suDesc;
    }
    
    public String getSUTarget()
    {
        return this.mSUTarget;
    }
    
    public void setSUTarget(String suTarget)
    {
        this.mSUTarget = suTarget;
    }
    
    private void createPrimaryConfigurationData(AntProjectHelper prjHelper)
    {
        
        Element data = prjHelper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        
        Element nameEl = doc.createElementNS(SEPluginProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(this.getProjectName()));
        data.appendChild(nameEl);
        
        prjHelper.putPrimaryConfigurationData(data, true);
    }
    
    private void createProjectPrivateProperties(AntProjectHelper prjHelper)
    {
        
        EditableProperties ep = prjHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        
        //TODO:  add any project private properties here.
        // ep.setProperty("application.args", ""); // NOI18N
        prjHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
    }
    
    private void createProjectProperties(AntProjectHelper prjHelper)
    {
        
        EditableProperties ep = prjHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        ep.setProperty(SEPluginProjectProperties.SRC_DIR, SEPluginProjectProperties.SRC_DIR_VALUE);
        ep.setComment(SEPluginProjectProperties.SRC_DIR, new String[]{"# service unit source directory "}, false); // NOI18N
        ep.setProperty(SEPluginProjectProperties.BUILD_DIR, SEPluginProjectProperties.BUILD_DIR_VALUE);
        
        ep.setProperty(SEPluginProjectProperties.BUILD_DIR, SEPluginProjectProperties.BUILD_DIR_VALUE);
        
        ep.setProperty(SEPluginProjectProperties.JBI_SU_ZIP, SEPluginProjectProperties.JBI_SU_ZIP_VALUE);
        
        ep.setProperty(SEPluginProjectProperties.JBI_SU_NAME, getSUName());
        ep.setProperty(SEPluginProjectProperties.JBI_SU_DESCRIPTION, getSUDescription());
        ep.setProperty(SEPluginProjectProperties.JBI_SU_TARGET_NAME, getSUTarget());
        
        // save properties to file.
        prjHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }
    
    public AntProjectHelper createProject(File prjDir, String prjName) throws IOException
    {
        AntProjectHelper prjHelper = null;
        
        this.mPrjDir = prjDir;
        this.mPrjName = prjName;
        if (SEPluginProjectProperties.JBI_SU_NAME_VALUE.equals(this.getSUName()))
        {
            // default value. so set the su name to project name.
            String suName = PropertyUtils.getUsablePropertyName(this.getProjectName());
            this.setSUName(suName);
        }
        
        FileObject prjDirFO = createProjectDir(this.getProjectDirectory());
        
        prjHelper = ProjectGenerator.createProject(prjDirFO, SEPluginProjectType.TYPE);
        
        createPrimaryConfigurationData(prjHelper);
        createProjectProperties(prjHelper);
        createProjectPrivateProperties(prjHelper);
        
        FileObject srcFolder = FileUtil.createFolder(prjDirFO, SEPluginProjectProperties.SRC_DIR_VALUE); // NOI18N
        
        // create su jbi.xml
        SEPluginProjectProperties.createDefaultSUDescriptor(srcFolder);
        
        //TODO:  create any service unit specifc default artifacts here.
        
        Project p = ProjectManager.getDefault().findProject(prjDirFO);
        ProjectManager.getDefault().saveProject(p);
        
        return prjHelper;
    }
    
    private static FileObject createProjectDir(File dir) throws IOException
    {
        FileObject dirFO;
        if (!dir.exists())
        {
            //Refresh before mkdir not to depend on window focus, refreshFileSystem does not work correctly
            refreshFolder(dir);
            if (!dir.mkdirs())
            {
                throw new IOException("Can not create project folder."); //NOI18N
            }
            refreshFileSystem(dir);
        }
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir; // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        return dirFO;
    }
    
    private static void refreshFileSystem(final File dir) throws FileStateInvalidException
    {
        File rootF = dir;
        while (rootF.getParentFile() != null)
        {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }
    
    private static void refreshFolder(File dir)
    {
        while (!dir.exists())
        {
            dir = dir.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject(dir);
        if (fo != null)
        {
            fo.getChildren();
            fo.refresh();
        }
    }
}
