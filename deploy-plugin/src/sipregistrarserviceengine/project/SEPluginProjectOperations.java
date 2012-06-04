/*
 * SEPluginProjectOperations.java
 *
 */

package sipregistrarserviceengine.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author chikkala
 */
public class SEPluginProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation
{
    
    private SEPluginProject project;
    
    public SEPluginProjectOperations(SEPluginProject project)
    {
        this.project = project;
    }
    
    public void notifyDeleting() throws IOException
    {
        SEPluginProjectActionProvider ap = project.getLookup().lookup(SEPluginProjectActionProvider.class);
        
        assert ap != null;
        
        Lookup context = Lookups.fixed(new Object[0]);
        Properties p = new Properties();
        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, p);
        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() throws IOException
    {
        project.getAntProjectHelper().notifyDeleted();
    }
    
    public List<FileObject> getMetadataFiles()
    {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, "build.xml", files); // NOI18N
        addFile(projectDirectory, "manifest.mf", files); // NOI18N
        addFile(projectDirectory, projectDirectory.getName(), files); //NOI18N
        
        return files;
    }
    
    public List<FileObject> getDataFiles()
    {
        List<FileObject> files = new ArrayList<FileObject>();
        FileObject projectDirectory = project.getProjectDirectory();
        FileObject srcDirFO = SEPluginProjectProperties.getSourceDirectory(project);
        files.add(srcDirFO);
        return files;
    }
    
    public void notifyCopying() throws IOException
    {
        // do nothing.
        // This does copy the old distribution file over though, which is
        // probably OK because "ant clean" will clean it up.
    }
    
    public void notifyCopied(Project original, File originalPath, String newName) throws IOException
    {
        if (original == null)
        {
            // do nothing for the original project.
            return ;
        }
        
        project.getReferenceHelper().fixReferences(originalPath);
        
        String oldName = project.getName();
        project.setName(newName);
    }
    
    public void notifyMoving() throws IOException
    {
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, String newName) throws IOException
    {
        if (original == null)
        {
            project.getAntProjectHelper().notifyDeleted();
            return ;
        }
        String oldName = project.getName();
        project.setName(newName);
        project.getReferenceHelper().fixReferences(originalPath);
    }
    
    private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result)
    {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null)
        {
            result.add(file);
        }
    }
    
}
