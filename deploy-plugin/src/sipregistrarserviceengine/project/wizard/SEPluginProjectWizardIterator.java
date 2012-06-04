/*
 * SEPluginProjectWizardIterator.java
 */
package sipregistrarserviceengine.project.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import sipregistrarserviceengine.project.SEPluginProjectGenerator;
import sipregistrarserviceengine.project.SEPluginProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public class SEPluginProjectWizardIterator implements WizardDescriptor.InstantiatingIterator
{
    
    public final static String PROJECT_DIR = "projdir";
    public final static String PROJECT_NAME = "name";
    public final static String DEF_PROJECT_NAME_VALUE = "JBIModule";
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;
    
    public SEPluginProjectWizardIterator()
    {}
    
    public static SEPluginProjectWizardIterator createIterator()
    {
        return new SEPluginProjectWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels()
    {
        return new WizardDescriptor.Panel[] {
            new SEPluginProjectWizardPanel()
        };
    }
    
    private String[] createSteps()
    {
        return new String[] {
            NbBundle.getMessage(SEPluginProjectWizardIterator.class, "LBL_CreateProjectStep")
                    
                    
                    
        };
    }
    
    public Set<Object> instantiate(/*ProgressHandle handle*/) throws IOException
    {
        
        Set<Object> resultSet = createSEDeployPluginProject();
        // save the current projects folder
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty(PROJECT_DIR));
        File parent = dirF.getParentFile();
        if (parent != null && parent.exists())
        {
            ProjectChooser.setProjectsFolder(parent);
        }
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz)
    {
        this.wiz = wiz;
        
        try
        {
            FileObject templateFO = Templates.getTemplate(wiz);
            DataObject templateDO = DataObject.find(templateFO);
            this.wiz.putProperty(PROJECT_NAME, templateDO.getName());
        }
        catch ( Exception ex)
        {
            this.wiz.putProperty(PROJECT_NAME, DEF_PROJECT_NAME_VALUE);
        }
        
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++)
        {
            Component c = panels[i].getComponent();
            if (steps[i] == null)
            {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent)
            { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz)
    {
        this.wiz.putProperty(PROJECT_DIR,null);
        this.wiz.putProperty(PROJECT_NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    public String name()
    {
        return MessageFormat.format("{0} of {1}",
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    public boolean hasNext()
    {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious()
    {
        return index > 0;
    }
    
    public void nextPanel()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel()
    {
        if (!hasPrevious())
        {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    public WizardDescriptor.Panel current()
    {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l)
    {}
    public final void removeChangeListener(ChangeListener l)
    {}
    
    protected Set<Object> createSEDeployPluginProject() throws IOException
    {
        
        Set<Object> resultSet = new LinkedHashSet<Object>();
        
        File prjDirFile = FileUtil.normalizeFile((File) wiz.getProperty(PROJECT_DIR));
        String prjName = (String) wiz.getProperty(PROJECT_NAME);
        
        SEPluginProjectGenerator prjGenerator = new SEPluginProjectGenerator();
        AntProjectHelper h = prjGenerator.createProject(prjDirFile, prjName);
        
        FileObject projectDirFO = h.getProjectDirectory();
        resultSet.add(projectDirFO);
        
        Set<Object> defArtifacts = createProjectSpecificArtifacts(projectDirFO);
        resultSet.addAll(defArtifacts);
        
        return resultSet;
    }
    
    private FileObject copyResource(FileObject dirFO, String name, String resourcePath) throws IOException
    {
        FileObject dataFO = null;
        FileLock outLock = null;
        OutputStream outS = null;
        InputStream inS = null;
        
        try
        {
            inS = this.getClass().getResourceAsStream(resourcePath);
            dataFO = FileUtil.createData(dirFO, name);
            outLock = dataFO.lock();
            outS = dataFO.getOutputStream(outLock);
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
        return dataFO;
    }
    
    protected Set<Object> createProjectSpecificArtifacts(FileObject projectDirFO ) throws IOException
    {
        Set<Object> resultSet = new LinkedHashSet<Object>();
        //TODO: create any default service unit artifacts needed.
        try
        {
            
            FileObject srcDirFO = projectDirFO.getFileObject(SEPluginProjectProperties.SRC_DIR_VALUE);
            resultSet.add(srcDirFO);
            
            // create service unit jbi descriptor
            FileObject metaInfDirFO = FileUtil.createFolder(srcDirFO, "META-INF");
            FileObject jbiXmlFO = copyResource(metaInfDirFO, "jbi.xml", "resources/jbi.xml");
            resultSet.add(jbiXmlFO);
            
            // create any service unit artifacts such as wsdl files and configuration files
            
            // list of su artifacts to be created from the resources directory
            String[] suArtifacts = {"xsltmap.properties","goodbye.xsl","hello.xsl","Greetings.wsdl"};
                    
                    for ( String suArtifact : suArtifacts )
                    {
                        FileObject suArtifactFO = copyResource(srcDirFO, suArtifact, "resources/" + suArtifact);
                        resultSet.add(suArtifactFO);
                    }
                    
                    //TODO: add any addtional default service unit artifacts here.
                    // FileObject suArtifactFO = copyResource(srcDirFO, "su-artifact.xml", "resources/su-artifact.xml");
                    // resultSet.add(suArtifactFO);
                    
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return resultSet;
    }
    
}
