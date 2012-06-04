/*
 * SEPluginProjectNode.java
 *
 */

package sipregistrarserviceengine.project.node;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import sipregistrarserviceengine.project.SEPluginProjectProperties;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author chikkala
 */
public class SEPluginProjectNode extends AbstractNode
{
    
    private Project project;
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private SubprojectProvider spp;
    private ReferenceHelper resolver;
    
    private Action brokenLinksAction;
    private boolean broken;
    
    public SEPluginProjectNode(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver)
    {
        super(new SEPluginProjectNodeChildren(project, helper, evaluator), Lookups.singleton(project));
        setIconBaseWithExtension(SEPluginProjectProperties.SE_PLUGIN_PROJECT_ICON_PATH);
        super.setName(ProjectUtils.getInformation(project).getDisplayName());
        
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
        
        if (hasBrokenLinks(helper, resolver))
        {
            broken = true;
            brokenLinksAction = new BrokenLinksAction();
        }
    }
    
    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
        // TODO: new HelpCtx("se_deploy_plugin_project_about"); // NOI18N
    }
    
    @Override
    public Action[] getActions(boolean context)
    {
        if (context)
        {
            return super.getActions(true);
        }
        else
        {
            return getAdditionalActions();
        }
    }
    
    @Override
    public boolean canRename()
    {
        return true;
    }
    
    @Override
    public void setName(String s)
    {
        DefaultProjectOperations.performDefaultRenameOperation(project, s);
    }
    
    private String[] getBreakableProperties()
    {
        String[] breakableProps = new String[0];
        return breakableProps;
    }
    
    public boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver)
    {
        return BrokenReferencesSupport.isBroken(helper, resolver, getBreakableProperties(), new String[]{SEPluginProjectProperties.DEFAULT_PLATFORM});
    }
    
    private Action[] getAdditionalActions()
    {
        
        ResourceBundle bundle = NbBundle.getBundle(SEPluginProjectNode.class);
        
        List<Action> actions = new ArrayList<Action>();
        
        actions.add(CommonProjectActions.newFileAction());
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null)); // NOI18N
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null)); // NOI18N
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
        //TODO:  add additional plugin specific actions
        actions.add(null);
        actions.add(CommonProjectActions.setAsMainProjectAction());
        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(CommonProjectActions.renameProjectAction());
        actions.add(CommonProjectActions.moveProjectAction());
        actions.add(CommonProjectActions.copyProjectAction());
        actions.add(CommonProjectActions.deleteProjectAction());
        actions.add(null);
        actions.add(SystemAction.get(FindAction.class));
        // add actions from layer filesystem configuration
        addFromLayers(actions, "Projects/Actions"); //NOI18N
        actions.add(null);
        actions.add(brokenLinksAction);
        actions.add(CommonProjectActions.customizeProjectAction());
        
        return actions.toArray(new Action[actions.size()]);
    }
    
    private void addFromLayers(List<Action> actions, String path)
    {
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class))
        {
            if (next instanceof Action)
            {
                actions.add((Action) next);
            }
            else if (next instanceof JSeparator)
            {
                actions.add(null);
            }
        }
    }
    
    
    /** This action is created only when project has broken references.
     * Once these are resolved the action is disabled.
     */
    private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener
    {
        
        public BrokenLinksAction()
        {
            evaluator.addPropertyChangeListener(this);
            putValue(Action.NAME, NbBundle.getMessage(SEPluginProjectNode.class, "LBL_Fix_Broken_Links_Action"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            /*
            BrokenReferencesSupport.showCustomizer(helper, resolver, BREAKABLE_PROPERTIES, new String[]{SEPluginProjectProperties.DEFAULT_PLATFORM});
            if (!hasBrokenLinks(helper, resolver)) {
            disable();
            }
             */
            // do nothing...
        }
        
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (!broken)
            {
                disable();
                return;
            }
            broken = hasBrokenLinks(helper, resolver);
            if (!broken)
            {
                disable();
            }
        }
        
        private void disable()
        {
            broken = false;
            setEnabled(false);
            evaluator.removePropertyChangeListener(this);
            fireIconChange();
            fireOpenedIconChange();
        }
    }
    
    private static final class SEPluginProjectNodeChildren extends Children.Keys implements FileChangeListener
    {
        
        private static final String KEY_SU_JBI_XML = "SU_JBI_XML_Key"; // NOI18N
        private static final String KEY_SOURCE_DIR = "SourceDirKey"; // NOI18N
        private Project project;
        private AntProjectHelper helper;
        private PropertyEvaluator evaluator;
        
        public SEPluginProjectNodeChildren(Project project, AntProjectHelper helper, PropertyEvaluator evaluator)
        {
            this.project = project;
            assert project != null;
            this.helper = helper;
            assert helper != null;
            this.evaluator = evaluator;
            assert evaluator != null;
        }
        
        private FileObject getSourceFolder()
        {
            String srcDir = helper.getStandardPropertyEvaluator().getProperty(SEPluginProjectProperties.SRC_DIR); // NOI18N
            return helper.resolveFileObject(srcDir);
        }
        
        private FileObject getJbiXml(FileObject srcDirFO)
        {
            FileObject jbiXmlFO = null;
            if (srcDirFO == null)
            {
                return null;
            }
            jbiXmlFO = srcDirFO.getFileObject(SEPluginProjectProperties.SU_JBI_XML_PATH);
            if (jbiXmlFO == null)
            {
                // create default jbi.xml
                try
                {
                    jbiXmlFO = SEPluginProjectProperties.createDefaultSUDescriptor(srcDirFO);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            return jbiXmlFO;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected void addNotify()
        {
            super.addNotify();
            FileObject projectDirFO = project.getProjectDirectory();
            if (projectDirFO != null)
            {
                projectDirFO.addFileChangeListener(this);
            }
            FileObject srcDirFO = getSourceFolder();
            if (srcDirFO != null)
            {
                srcDirFO.addFileChangeListener(this);
            }
            setKeys(getKeys());
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected void removeNotify()
        {
            
            setKeys(Collections.emptySet());
            
            FileObject projectDirFO = project.getProjectDirectory();
            if (projectDirFO != null)
            {
                projectDirFO.removeFileChangeListener(this);
            }
            FileObject srcDirFO = getSourceFolder();
            if (srcDirFO != null)
            {
                srcDirFO.removeFileChangeListener(this);
            }
            
            super.removeNotify();
        }
        
        
        private Collection<Object> getKeys()
        {
            //when the project is deleted externally do not try to create children, the source groups
            //are not valid
            if (project.getProjectDirectory() == null || !project.getProjectDirectory().isValid())
            {
                return Collections.emptyList();
            }
            
            List<Object> result = new ArrayList<Object>();
            
            FileObject srcDirFO = null;
            FileObject jbiXmlFO = null;
            
            srcDirFO = getSourceFolder();
            if (srcDirFO != null)
            {
                jbiXmlFO = getJbiXml(srcDirFO);
            }
            if (jbiXmlFO != null)
            {
                result.add(KEY_SU_JBI_XML);
            }
            
            if (srcDirFO != null)
            {
                result.add(KEY_SOURCE_DIR);
            }
            
            //TODO: add any other top level node keys here.
            return result;
        }
        
        protected Node createNode(FileObject fo)
        {
            try
            {
                DataObject dataObj = DataObject.find(fo);
                return new FilterNode(dataObj.getNodeDelegate());
            }
            catch (Exception ex)
            {
                return null;
            }
        }
        
        protected Node[] createNodes(Object key)
        {
            List<Node> newNodes = new ArrayList<Node>();
            if (KEY_SU_JBI_XML.equals(key))
            {
                FileObject srcDirFO = getSourceFolder();
                FileObject jbiXmlFO = getJbiXml(srcDirFO);
                newNodes.add(createNode(jbiXmlFO));
            }
            else if (KEY_SOURCE_DIR.equals(key))
            {
                FileObject srcDirFO = getSourceFolder();
                newNodes.add(createNode(srcDirFO));
            }
            return newNodes.toArray(new Node[newNodes.size()]);
        }
        
        @SuppressWarnings("unchecked")
        public void fileFolderCreated(FileEvent evt)
        {
            setKeys(getKeys());
        }
        
        @SuppressWarnings("unchecked")
        public void fileDataCreated(FileEvent evt)
        {
            setKeys(getKeys());
        }
        
        public void fileChanged(FileEvent evt)
        {
            
        }
        
        @SuppressWarnings("unchecked")
        public void fileDeleted(FileEvent evt)
        {
            setKeys(getKeys());
        }
        
        @SuppressWarnings("unchecked")
        public void fileRenamed(FileRenameEvent evt)
        {
            setKeys(getKeys());
        }
        
        public void fileAttributeChanged(FileAttributeEvent evt)
        {
            
        }
    }
}
