/*
 * SEPluginProjectWizardPanel.java
 */

package sipregistrarserviceengine.project.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
public class SEPluginProjectWizardPanel implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel
{
    
    private WizardDescriptor wizardDescriptor;
    private SEPluginProjectWizardPanelVisual component;
    
    /** Creates a new instance of templateWizardPanel */
    public SEPluginProjectWizardPanel()
    {
    }
    
    public Component getComponent()
    {
        if (component == null)
        {
            component = new SEPluginProjectWizardPanelVisual(this);
            component.setName(NbBundle.getMessage(SEPluginProjectWizardPanel.class, "LBL_CreateProjectStep"));
        }
        return component;
    }
    
    public HelpCtx getHelp()
    {
        return new HelpCtx(SEPluginProjectWizardPanel.class);
    }
    
    public boolean isValid()
    {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1); // or can use ChangeSupport in NB 6.0
    @SuppressWarnings("unchecked")
    public final void addChangeListener(ChangeListener l)
    {
        synchronized (listeners)
        {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l)
    {
        synchronized (listeners)
        {
            listeners.remove(l);
        }
    }
    @SuppressWarnings("unchecked")
    protected final void fireChangeEvent()
    {
        Iterator it;
        synchronized (listeners)
        {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext())
        {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings)
    {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
    }
    
    public void storeSettings(Object settings)
    {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
    }
    
    public boolean isFinishPanel()
    {
        return true;
    }
    
    public void validate() throws WizardValidationException
    {
        getComponent();
        component.validate(wizardDescriptor);
    }
    
}
