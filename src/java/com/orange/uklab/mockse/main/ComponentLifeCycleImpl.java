/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.orange.uklab.mockse.main;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

/**
 *
 * @author hasanein
 */
public class ComponentLifeCycleImpl implements ComponentLifeCycle
{
    private ComponentContext componentContext;
    
    @Override
    public ObjectName getExtensionMBeanName()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(ComponentContext componentContext) throws JBIException
    {
        this.componentContext = componentContext;
        System.out.println("Component Context initialized...");
    }

    @Override
    public void shutDown() throws JBIException
    {
        
    }

    @Override
    public void start() throws JBIException
    {
        try
        {
            defineServiceEndpoint();
            doStart();
        }
        catch (Exception ex)
        {
            Logger.getLogger(ComponentLifeCycleImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() throws JBIException
    {
        
    }

    private void doStart() throws Exception
    {
        /**
         * Start a new thread to accept NMR messages from the delivery channel
         */
        NmrListener nmrListener01 = new NmrListener(componentContext);
        Thread thread01 = new Thread(nmrListener01);
        thread01.start();

        NmrListener nmrListener02 = new NmrListener(componentContext);
        Thread thread02 = new Thread(nmrListener02);
        thread02.start();
    }

    private void defineServiceEndpoint() throws Exception
    {
        QName serviceName = new QName("http://www.orange.com/uklab/b2bbc/Consumer", "MockSE01SipConsumerService");
        String endpointName = "MockSE01SipConsumerServiceEndpoint";
        ServiceEndpoint serviceEndpoint = componentContext.activateEndpoint(serviceName, endpointName);
        componentContext.getLogger(this.getClass().getName(), null).info("__________________________________________________________________");
        componentContext.getLogger(this.getClass().getName(), null).info(serviceEndpoint.getEndpointName() + " has been defined in the JBI");
        componentContext.getLogger(this.getClass().getName(), null).info("__________________________________________________________________");
    }

}
