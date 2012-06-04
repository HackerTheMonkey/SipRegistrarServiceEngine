/*
 * SEPluginProjectCustomizerProvider.java
 *
 */

package sipregistrarserviceengine.project.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.NbBundle;

/**
 *
 * @author chikkala
 */
public class SEPluginProjectCustomizerProvider implements CustomizerProvider
{
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK"; // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL"; // NOI18N
    
    // Categories
    
    private static final String GENERAL = "General"; // NOI18N
    private static final String BUILD_CATEGORIES = "Build"; // NOI18N
    private static final String COMPILE = "Compile"; // NOI18N
    private static final String PACKAGE = "Package"; // NOI18N
    
    private Project project;
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
    
    private SEPluginProjectCustomizerModel uiModel;
    
    private List<ProjectCustomizer.Category> categories;
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    private static Map<Project, Dialog> project2Dialog = new HashMap<Project, Dialog>();
    
    
    
    public SEPluginProjectCustomizerProvider(Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper)
    {
        this.project = project;
        this.refHelper = refHelper;
        this.antProjectHelper = antProjectHelper;
    }
    
    public void showCustomizer()
    {
        showCustomizer(null);
    }
    
    public void showCustomizer(String preselectedCategory)
    {
        showCustomizer(preselectedCategory, null);
    }
    
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory)
    {
        
        Dialog dialog = project2Dialog.get(project);
        if (dialog != null)
        {
            dialog.setVisible(true);
            return;
        }
        else
        {
            SEPluginProjectCustomizerModel model = new SEPluginProjectCustomizerModel(project, antProjectHelper, refHelper);
            init(model);
            
            OptionListener listener = new OptionListener(project, uiModel);
            
            if (preselectedCategory != null && preselectedSubCategory != null)
            {
                for (ProjectCustomizer.Category category : categories)
                {
                    if (preselectedCategory.equals(category.getName()))
                    {
                        JComponent component = panelProvider.create(category);
                        if (component instanceof SubCategoryProvider)
                        {
                            ((SubCategoryProvider) component).showSubCategory(preselectedSubCategory);
                        }
                        break;
                    }
                }
            }
            dialog = ProjectCustomizer.createCustomizerDialog(
                    categories.toArray(new ProjectCustomizer.Category[categories.size()]),
                    panelProvider, preselectedCategory, listener, null);
            dialog.addWindowListener(listener);
            dialog.setTitle(MessageFormat.format(NbBundle.getMessage(SEPluginProjectCustomizerProvider.class, "LBL_Customizer_Title"), new Object[]{ProjectUtils.getInformation(project).getDisplayName()}));
            
            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }
    private void init(SEPluginProjectCustomizerModel uiModel)
    {
        
        this.uiModel = uiModel;
        categories = new ArrayList<ProjectCustomizer.Category>();
        panelProvider = new PanelProvider(createCategoriesMap());
    }
    
    /**
     * Getter for categories
     */
    protected List<ProjectCustomizer.Category> getCategories()
    {
        return categories;
    }
    
    /**
     * This api is called when showCustomizer is invoked and if customizer
     * provider is not fully initialized.
     * The default implementation creates Project Reference and XML Catalog Categories.
     * Subclasses can override this and may or may not call super depending upon,
     * the categories are desired in project customizer.
     */
    protected Map<ProjectCustomizer.Category,JComponent> createCategoriesMap()
    {
        
        ResourceBundle bundle = NbBundle.getBundle(SEPluginProjectCustomizerProvider.class );
        
        ProjectCustomizer.Category generalNode = ProjectCustomizer.Category.create(
                GENERAL,
                bundle.getString( "LBL_Config_General" ), // NOI18N
                null,
                (ProjectCustomizer.Category[])null);
        ProjectCustomizer.Category packageNode = ProjectCustomizer.Category.create(
                PACKAGE,
                bundle.getString( "LBL_Config_Package" ), // NOI18N
                null,
                (ProjectCustomizer.Category[])null);
        /* //TODO: add new category if required
        ProjectCustomizer.Category compileNode = ProjectCustomizer.Category.create(
            COMPILE,
            bundle.getString( "LBL_Config_Compile" ), // NOI18N
            null,
            (ProjectCustomizer.Category[])null);
         */
        ProjectCustomizer.Category[] buildCategories = new ProjectCustomizer.Category[] {packageNode };
        // TODO: add other categories to build if required.
        // buildCategories = new ProjectCustomizer.Category[] {compileNode, packageNode };
        ProjectCustomizer.Category buildCategoriesNode = ProjectCustomizer.Category.create(
                BUILD_CATEGORIES,
                bundle.getString( "LBL_Config_BuildCategories" ), // NOI18N
                null,
                buildCategories);
        
        getCategories().add(generalNode);
        getCategories().add(buildCategoriesNode);
        
        Map<ProjectCustomizer.Category,JComponent> panels =
                new HashMap<ProjectCustomizer.Category,JComponent>();
        
        panels.put(generalNode, new CustomizerGeneral(uiModel));
        panels.put(packageNode, new CustomizerPackage(uiModel));
        // panels.put(compileNode, new CustomizerCompile(uiModel));
        
        return panels;
    }
    
    /**
     * CategoryComponentProvider provider class.
     * It stores categories and there corresponding UI in a map.
     * An instance of PanelProvider is stored in CustomizerProviderImpl instance
     */
    private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider
    {
        
        private JPanel EMPTY_PANEL = new JPanel();
        
        private Map<ProjectCustomizer.Category,JComponent> panels;
        
        PanelProvider(Map<ProjectCustomizer.Category,JComponent> panels)
        {
            this.panels = panels;
        }
        
        public JComponent create(ProjectCustomizer.Category category)
        {
            JComponent panel = panels.get(category);
            return panel == null ? EMPTY_PANEL : panel;
        }
        
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener
    {
        
        private Project project;
        private SEPluginProjectCustomizerModel uiModel;
        
        OptionListener(Project project, SEPluginProjectCustomizerModel uiModel)
        {
            this.project = project;
            this.uiModel = uiModel;
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed(ActionEvent e)
        {
            // Store the properties into project
            this.uiModel.save();
            // Close & dispose the the dialog
            Dialog dialog = project2Dialog.get(project);
            if (dialog != null)
            {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
        
        // Listening to window events ------------------------------------------
        
        public void windowClosed(WindowEvent e)
        {
            project2Dialog.remove(project);
        }
        
        public void windowClosing(WindowEvent e)
        {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = project2Dialog.get(project);
            if (dialog != null)
            {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
    public static interface SubCategoryProvider
    {
        
        public void showSubCategory(String name);
    }
}
