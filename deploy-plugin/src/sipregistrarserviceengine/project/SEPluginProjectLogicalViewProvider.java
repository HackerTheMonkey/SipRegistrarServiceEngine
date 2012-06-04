/*
 * SEPluginProjectLogicalViewProvider.java
 *
 */

package sipregistrarserviceengine.project;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import sipregistrarserviceengine.project.node.SEPluginProjectNode;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author chikkala
 */
public class SEPluginProjectLogicalViewProvider  implements LogicalViewProvider
{
    
    private final Project mProject;
    private final AntProjectHelper mHelper;
    private final PropertyEvaluator mEvaluator;
    private final SubprojectProvider mSpp;
    private final ReferenceHelper mResolver;
    
    public SEPluginProjectLogicalViewProvider(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver)
    {
        this.mProject = project;
        assert project != null;
        this.mHelper = helper;
        assert helper != null;
        this.mEvaluator = evaluator;
        assert evaluator != null;
        this.mSpp = spp;
        assert spp != null;
        this.mResolver = resolver;
    }
    
    public Node createLogicalView()
    {
        return new SEPluginProjectNode(this.mProject, this.mHelper, this.mEvaluator, this.mSpp, this.mResolver);
    }
    
    public Node findPath(Node root, Object target)
    {
        Project project = root.getLookup().lookup(Project.class);
        if (project == null)
        {
            return null;
        }
        
        if (target instanceof FileObject)
        {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner))
            {
                return null; // Don't waste time if project does not own the fo
            }
            
            Node[] nodes = root.getChildren().getNodes(true);
            for (int i = 0; i < nodes.length; i++)
            {
                Node result = PackageView.findPath(nodes[i], target);
                if (result != null)
                {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    private String[] getBreakableProperties()
    {
        String[] breakableProps = new String[0];
        return breakableProps;
    }
    
    public boolean hasBrokenLinks()
    {
        return BrokenReferencesSupport.isBroken(mHelper, mResolver, getBreakableProperties(), new String[]{SEPluginProjectProperties.DEFAULT_PLATFORM});
    }
    
}
