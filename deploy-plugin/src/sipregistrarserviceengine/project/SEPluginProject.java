/*
 * SEPluginProject.java
 *
 */

package sipregistrarserviceengine.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntArtifact;
import sipregistrarserviceengine.project.customizer.SEPluginProjectCustomizerProvider;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author chikkala
 */
public final class SEPluginProject implements Project, AntProjectListener
{
    private static final Icon PROJECT_ICON = new ImageIcon(Utilities.loadImage(SEPluginProjectProperties.SE_PLUGIN_PROJECT_ICON_PATH)); // NOI18N
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;
    private GeneratedFilesHelper genFilesHelper;
    private Lookup lookup;
    
    public SEPluginProject(AntProjectHelper helper) throws IOException
    {
        this.helper = helper;
        this.evaluator = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        this.refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        this.genFilesHelper = new GeneratedFilesHelper(helper);
        this.lookup = createLookup(aux);
        helper.addAntProjectListener(this);
    }
    
    @Override
    public String toString()
    {
        return "SEPluginProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public FileObject getProjectDirectory()
    {
        return helper.getProjectDirectory();
    }
    
    public Lookup getLookup()
    {
        return lookup;
    }
    
    public void configurationXmlChanged(AntProjectEvent event)
    {
        if (event.getPath().equals(AntProjectHelper.PROJECT_XML_PATH))
        {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
        //TODO: add other configuration xml change event handling code.
    }
    
    public void propertiesChanged(AntProjectEvent event)
    {
        //TODO: add property change event processing.
    }
    
    public AntProjectHelper getAntProjectHelper()
    {
        return helper;
    }
    
    public ReferenceHelper getReferenceHelper()
    {
        return this.refHelper;
    }
    
    public PropertyEvaluator getEvaluator()
    {
        return this.evaluator;
    }
    
    /** Return configured project name. */
    @SuppressWarnings(value = "unchecked")
    public String getName()
    {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action()
        {
            public Object run()
            {
                Element data = helper.getPrimaryConfigurationData(true);
                
                NodeList nl = data.getElementsByTagNameNS(SEPluginProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                if (nl.getLength() == 1)
                {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE)
                    {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }
    
    /** Store configured project name. */
    @SuppressWarnings(value = "unchecked")
    public void setName(final String name)
    {
        ProjectManager.mutex().writeAccess(new Mutex.Action()
        {
            
            public Object run()
            {
                Element data = helper.getPrimaryConfigurationData(true);
                
                NodeList nl = data.getElementsByTagNameNS(SEPluginProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1)
                {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0)
                    {
                        nameEl.removeChild(deadKids.item(0));
                    }
                }
                else
                {
                    nameEl = data.getOwnerDocument().createElementNS(SEPluginProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    private PropertyEvaluator createEvaluator()
    {
        //TODO: might need to use a custom evaluator to handle active platform substitutions...
        return helper.getStandardPropertyEvaluator();
    }
    
    private FileBuiltQueryImplementation createFileBuiltQuery()
    {
        return helper.createGlobFileBuiltQuery(getEvaluator(),
                new String[] {"${src.dir}/*.java"}, // NOI18N
                new String[] {"${build.classes.dir}/*.class"} // NOI18N
        );
    }
    
    private SharabilityQueryImplementation createSharabilityQuery()
    {
        return helper.createSharabilityQuery(getEvaluator(),
                new String[] {"${src.dir}"}, // NOI18N
                new String[] {"${build.dir}"} // NOI18N
        );
    }
    
    private Sources getSources()
    {
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, getEvaluator());
        //TODO:  add pricipal and typed source roots if required.
        String srcLabel = NbBundle.getMessage(SEPluginProject.class, "LBL_Node_Sources"); //NOI18N
        
        String srcLoc = "${" + SEPluginProjectProperties.SRC_DIR + "}";
        sourcesHelper.addPrincipalSourceRoot(srcLoc, srcLabel, null, null);
        sourcesHelper.addTypedSourceRoot(srcLoc, SEPluginProjectProperties.SOURCES_TYPE_XML,
                srcLabel, null, null);
        ProjectManager.mutex().postWriteRequest(new Runnable()
        {
            
            public void run()
            {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return sourcesHelper.createSources();
    }
    
    private Lookup createLookup(AuxiliaryConfiguration aux)
    {
        
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        return Lookups.fixed(new Object[] {
            this, // to lookup this project from externally obtained Project
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new Info(),
            new SEPluginProjectActionProvider(this, helper, refHelper),
            new SEPluginProjectLogicalViewProvider(this, helper, getEvaluator(), spp, refHelper),
            new SEPluginProjectCustomizerProvider(this, helper, refHelper),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new RecommendedTemplatesImpl(),
            new SEPluginProjectOperations(this),
            getSources(),
            createSharabilityQuery(),
            createFileBuiltQuery()
        });
    }
    // Private inner classes -------------------------------------------------------
    
    /**
     * @see org.netbeans.api.project.ProjectInformation
     */
    private final class Info implements ProjectInformation
    {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info()
        {
        }
        
        void firePropertyChange(String prop)
        {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName()
        {
            return SEPluginProject.this.getName();
        }
        
        public String getDisplayName()
        {
            return SEPluginProject.this.getName();
        }
        
        public Icon getIcon()
        {
            return PROJECT_ICON;
        }
        
        public Project getProject()
        {
            return SEPluginProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.removePropertyChangeListener(listener);
        }
    }
    
    /**
     * @see org.netbeans.spi.project.support.ant.ProjectXmlSavedHook
     */
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook
    {
        
        ProjectXmlSavedHookImpl()
        {}
        
        protected void projectXmlSaved() throws IOException
        {
            genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    SEPluginProject.class.getResource("resources/build-impl.xsl"),
                    false);
            genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    SEPluginProject.class.getResource("resources/build.xsl"),
                    false);
        }
    }
    
    /**
     * @see org.netbeans.spi.project.ui.ProjectOpenedHook
     */
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook
    {
        
        // TODO m
        ProjectOpenedHookImpl()
        { }
        
        @SuppressWarnings("unchecked")
        protected void projectOpened()
        {
            
            try
            {
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        SEPluginProject.class.getResource("resources/build-impl.xsl"),
                        true);
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        SEPluginProject.class.getResource("resources/build.xsl"),
                        true);
            }
            catch (IOException e)
            {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action()
            {
                public Object run()
                {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    //TODO: add any other resolved private properties. for example, the project paths and lib paths
                    try
                    {
                        ProjectManager.getDefault().saveProject(SEPluginProject.this);
                    }
                    catch (IOException e)
                    {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            
            SEPluginProjectLogicalViewProvider logicalViewProvider =
                    SEPluginProject.this.getLookup().lookup(SEPluginProjectLogicalViewProvider.class);
            if (logicalViewProvider != null &&  logicalViewProvider.hasBrokenLinks())
            {
                BrokenReferencesSupport.showAlert();
            }
        }
        
        protected void projectClosed()
        {
            // Probably unnecessary, but just in case:
            try
            {
                ProjectManager.getDefault().saveProject(SEPluginProject.this);
            }
            catch (IOException e)
            {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    /**
     * @see org.netbeans.spi.project.ui.RecommendedTemplates
     * @see org.netbeans.spi.project.ui.PrivilegedTemplates
     */
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates
    {
        
        // List of primarily supported templates
        
        private static final String[] TYPES = new String[] {
            /* TODO: add any other recommended templates
            "java-classes",        // NOI18N
            "ejb-types",            // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "simple-files"          // NOI18N
             */
            "SOA",
            "XML",                  // NOI18N
            "simple-files"          // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            /* TODO: add any other privileged names
            "Templates/Classes/Class.java",    // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java" // NOI18N
             */
            "Templates/XML/XmlDocument.xml",    // NOI18N
            "Templates/XML/XmlSchema.xsd",    // NOI18N
            "Templates/XML/WSDL.wsdl",    // NOI18N
            "Templates/Other/properties.properties"    // NOI18N
        };
        
        public String[] getRecommendedTypes()
        {
            return TYPES;
        }
        
        public String[] getPrivilegedTemplates()
        {
            return PRIVILEGED_NAMES;
        }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     *
     * @see org.netbeans.spi.project.ant.AntArtifactProvider
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider
    {
        
        public AntArtifact[] getBuildArtifacts()
        {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(
                        SEPluginProjectProperties.ARTIFACT_TYPE_JBI_SU_PREFIX +
                        helper.getStandardPropertyEvaluator().getProperty(SEPluginProjectProperties.JBI_SU_TARGET_NAME),
                        SEPluginProjectProperties.JBI_SU_ZIP,
                        helper.getStandardPropertyEvaluator(),
                        SEPluginProjectProperties.BUILD_TARGET_DIST,
                        SEPluginProjectProperties.BUILD_TARGET_CLEAN),
                        
                        helper.createSimpleAntArtifact(SEPluginProjectProperties.ARTIFACT_TYPE_JAR,
                        SEPluginProjectProperties.JBI_SU_ZIP,
                        helper.getStandardPropertyEvaluator(),
                        SEPluginProjectProperties.BUILD_TARGET_DIST,
                        SEPluginProjectProperties.BUILD_TARGET_CLEAN)
            };
        }
        
    }
    
}

