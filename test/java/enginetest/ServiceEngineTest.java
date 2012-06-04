/*
 * ServiceEngineTest.java
 */

package enginetest;

import com.sun.jbi.sample.component.test.JBIComponentTestClient;
import com.sun.jbi.sample.component.test.SOAPBindingTestClient;
import java.util.Properties;
 import junit.framework.TestCase;

/**
 * The test method in this testcase uses the SOAPBindingTestClient to send the
 * input document to the echo service provided by service engine via soap binding
 * component and receives the output document which will be placed in test results
 * directory under the same package as this test case.
 * @see com.sun.jbi.sample.component.test.SOAPBindingTestClinet
 *  @author chikkala
 */
public class ServiceEngineTest 
        extends TestCase 
{    
    public ServiceEngineTest(String testName) {
        super(testName);
    }
        
    public void test1() throws Exception {
        String testPropertiesPath = "test1.properties";
        JBIComponentTestClient testClient = new SOAPBindingTestClient();
        Properties testProps = testClient.loadTestProperties(this.getClass(), testPropertiesPath);
        testClient.testService(testProps);
    }
    
    public void test2() throws Exception {
        String testPropertiesPath = "test2.properties";
        JBIComponentTestClient testClient = new SOAPBindingTestClient();
        Properties testProps = testClient.loadTestProperties(this.getClass(), testPropertiesPath);
        testClient.testService(testProps);
    }
    
    public static void main(String[] args) {
        try {
            ServiceEngineTest compTest =  new ServiceEngineTest("ServiceEngineTest");
            compTest.test1();
            compTest.test2();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
