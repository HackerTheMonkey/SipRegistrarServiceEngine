/*
 * SEPluginProjectActionProvider.java
 *
 */

package sipregistrarserviceengine.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action provider of the SE Plugin projects.
 * @author chikkala
 */
public class SEPluginProjectActionProvider implements ActionProvider
{
    
    // Commands available from SE Plugin Project
    private static final String[] supportedActions = {COMMAND_BUILD, COMMAND_CLEAN, COMMAND_REBUILD, COMMAND_DELETE, COMMAND_COPY, COMMAND_MOVE, COMMAND_RENAME};
    
    private SEPluginProject project;
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
    
    /** Map from commands to ant targets */
    private Map<String, String[]> commands;
    
    public SEPluginProjectActionProvider(SEPluginProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper)
    {
        commands = new HashMap<String, String[]>();
        commands.put(COMMAND_BUILD, new String[]{"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[]{"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[]{"clean", "dist"}); // NOI18N
        //TODO:  Add any other ant targets to commands map.
        //commands.put(COMMAND_DEPLOY, new String[] {"run"}); // NOI18N
        this.antProjectHelper = antProjectHelper;
        this.project = project;
        this.refHelper = refHelper;
    }
    
    public String[] getSupportedActions()
    {
        return supportedActions;
    }
    
    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException
    {
        
        if (COMMAND_COPY.equals(command))
        {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return;
        }
        
        if (COMMAND_MOVE.equals(command))
        {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return;
        }
        
        if (COMMAND_RENAME.equals(command))
        {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return;
        }
        if (COMMAND_DELETE.equals(command))
        {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return;
        }
        
        Runnable action = new Runnable()
        {
            public void run()
            {
                Properties p = new Properties();
                String[] targetNames;
                
                targetNames = getTargetNames(command, context, p);
                if (targetNames == null)
                {
                    return;
                }
                if (targetNames.length == 0)
                {
                    targetNames = null;
                }
                if (p.keySet().size() == 0)
                {
                    p = null;
                }
                try
                {
                    FileObject buildFo = findBuildXml();
                    if (buildFo == null || !buildFo.isValid())
                    {
                        //The build.xml was deleted after the isActionEnabled was called
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SEPluginProjectActionProvider.class,
                                "LBL_No_Build_XML_Found"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                    else
                    {
                        ActionUtils.runTarget(buildFo, targetNames, p);
                    }
                }
                catch (IOException e)
                {
                    ErrorManager.getDefault().notify(e);
                }
            }
        };
        //TODO: add code if needed that requires the execution to wait for some other task to complete.
        action.run(); // execute the task to invoke the ant target for the command
        
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException
    {
        if (findBuildXml() == null)
        {
            return false;
        }
        //TODO: Add any commands enabling check logic if required.
        return true;
    }
    
    
    public FileObject findBuildXml()
    {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    public String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException
    {
        String[] targetNames =  commands.get(command);
        //TODO: add any special code that requires adding new target names or setting the
        // external properties p passed during the ant exection if required.
        return targetNames;
    }
    
}
