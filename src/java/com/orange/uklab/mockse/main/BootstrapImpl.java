/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.uklab.mockse.main;

import java.util.logging.Logger;
import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;
import org.w3c.dom.DocumentFragment;

/**
 *
 * @author hasaneinali
 */
public class BootstrapImpl implements Bootstrap
{
    private String logPreamble = "@@@@@@@@@@@@@@@@@@@@@:";
    private InstallationContext installationContext = null;
    private Logger logger = null;

    /**
     * 
     * @param installationContext
     * @throws JBIException
     */
    @Override
    public void init(InstallationContext installationContext) throws JBIException
    {
        /**
         * Store the passed in InstallationContext in a private global
         * variable, so it can be used by other methods in the class.
         */
        this.installationContext = installationContext;
        /**
         * Get the InstallationContext Logger
         */
        getLogger();
        logger.info(logPreamble + this.getClass().getName() + ": init()");
        /**
         * Check if there is any DOM fragment for the ID extension elements
         * in the jbi.xml descriptor of the component.
         */
        DocumentFragment extensionElementsDomFragment = installationContext.getInstallationDescriptorExtension();
        if (extensionElementsDomFragment == null)
        {
            logger.info(logPreamble + this.getClass().getName() + ": init()" + "No Mbean Installation Extensions Found");
        }
        else
        {
            logger.info(logPreamble + this.getClass().getName() + ": init()" + "Mbean Installation Extension Found");
        }        
    }

    /**
     * 
     * @throws JBIException
     */
    @Override
    public void cleanUp() throws JBIException
    {
        System.out.println(logPreamble + "The clean up method has been called...");
    }

    /**
     * This method is to be used to initialize a static RuntimeContext, that
     * RuntimeContext will made certain context information available globally
     * to the component during its lifetime. All the parts of the component
     * will be able to access those runtime information such as the installation
     * root and the logger associated with the component.
     * @param componentContext
     */
    

    /**
     * 
     * @return
     */
    @Override
    public ObjectName getExtensionMBeanName()
    {
        logger.info(logPreamble + "There are no extension MBeans registered");
        return null;
    }

    /**
     * 
     * @throws JBIException
     */
    @Override
    public void onInstall() throws JBIException
    {
        logger.info(logPreamble + this.getClass().getName() + ": onInstall()");
    }

    /**
     * 
     * @throws JBIException
     */
    @Override
    public void onUninstall() throws JBIException
    {
        logger.info(logPreamble + this.getClass().getName() + ": onUnInstall()");
    }

    /**
     * 
     */
    private void getLogger()
    {
        try
        {
            this.logger = installationContext.getContext().getLogger(this.getClass().getName(), null);
        }
        catch(Exception e)
        {
            logger.severe(logPreamble + "Error getting logger: " + e.getMessage());
        }
    }
}
