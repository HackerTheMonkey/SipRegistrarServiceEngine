/*
 * SEPluginProjectCustomizerModel.java
 *
 */

package sipregistrarserviceengine.project.customizer;

import java.io.IOException;
import javax.swing.ButtonModel;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import sipregistrarserviceengine.project.SEPluginProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author chikkala
 */
public class SEPluginProjectCustomizerModel
{
    
    private Project mProject;
    private AntProjectHelper mAntPrjHelper;
    private ReferenceHelper mRefHelper;
    
    private StoreGroup mPrjPropsStore;
    
    private Document mSUTargetModel;
    private Document mSUNameModel;
    private Document mSUDescModel;
    
    private Document mSUZipModel;
    private ButtonModel mSUZipCompressModel;
    private Document mBuildFilesExcludesModel;
    
    /** Creates a new instance of Customizer UI Model and initializes it */
    public SEPluginProjectCustomizerModel(Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper)
    {
        this.mProject = project;
        this.mAntPrjHelper = antProjectHelper;
        this.mRefHelper = refHelper;
        this.mPrjPropsStore = new StoreGroup();
        init();
    }
    public Document getSUTargetModel()
    {
        return this.mSUTargetModel;
    }
    public Document getSUNameModel()
    {
        return this.mSUNameModel;
    }
    public Document getSUDescriptionModel()
    {
        return this.mSUDescModel;
    }
    
    public Document getSUZipModel()
    {
        return this.mSUZipModel;
    }
    
    public ButtonModel getJarCompressModel()
    {
        return this.mSUZipCompressModel;
    }
    
    public Document getBuildFilesExcludesModel()
    {
        return this.mBuildFilesExcludesModel;
    }
    
    /** Initializes the visual models
     */
    private void init()
    {
        // initialize visual models from project properties
        PropertyEvaluator evaluator = this.mAntPrjHelper.getStandardPropertyEvaluator();
        // cutomizer-general
        this.mSUTargetModel = this.mPrjPropsStore.createStringDocument(evaluator, SEPluginProjectProperties.JBI_SU_TARGET_NAME);
        this.mSUNameModel = this.mPrjPropsStore.createStringDocument(evaluator, SEPluginProjectProperties.JBI_SU_NAME);
        this.mSUDescModel = this.mPrjPropsStore.createStringDocument(evaluator, SEPluginProjectProperties.JBI_SU_DESCRIPTION);
        // customizer-package
        this.mSUZipModel = this.mPrjPropsStore.createStringDocument(evaluator, SEPluginProjectProperties.JBI_SU_ZIP);
        this.mSUZipCompressModel = this.mPrjPropsStore.createToggleButtonModel( evaluator, SEPluginProjectProperties.JAR_COMPRESS );
        this.mBuildFilesExcludesModel = this.mPrjPropsStore.createStringDocument(evaluator, SEPluginProjectProperties.BUILD_FILES_EXCLUDES);
    }
    /** Save visual models to project properties and other metadata
     */
    public void save()
    {
        
        try
        {
            // Store properties
            @SuppressWarnings("unchecked")
            Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction()
            {
                final FileObject projectDir = mAntPrjHelper.getProjectDirectory();
                public Object run() throws IOException
                {
                    //TODO: regenreate any project build script and project metadata if required.
                    // store project properties.
                    storeProperties();
                    return Boolean.TRUE;
                }
            });
            // and save project if required.
            if (result == Boolean.TRUE)
            {
                ProjectManager.getDefault().saveProject(mProject);
            }
        }
        catch (MutexException e)
        {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
        catch ( IOException ex )
        {
            ErrorManager.getDefault().notify( ex );
        }
        
    }
    
    private void storeProperties() throws IOException
    {
        EditableProperties projectProperties = mAntPrjHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        this.mPrjPropsStore.store(projectProperties);
    }
}
