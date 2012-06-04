/*
 * JBIComponentTestClient.java
 */

package com.sun.jbi.sample.component.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

/**
 * This class implements the base framework for the testing the JBI components.
 * Testing the JBI component involves may require deploying a service to the
 * component(s) to enable the service provided by the test component and then
 * invoking the service provided by the test component. If the test component is
 * a binding component, then a service provider component such as sample service
 * engine is required to test the component end to end. If the test component is a
 * service engine, then the service on the test component can be invoked via
 * soap/http binding component.
 *
 * This class provides the utilites to read the test properties, read the input
 * message from the file and save the output to the file. The testService
 * method implemented here can be executed as the test for the component. The
 * testService method calls the invokeService method with the input document and
 * expects a output document or error that will be saved to the output file.
 * The method invokeService should be implemented by the extended test classes to
 * inplement a suitable service consumer implemenation for the test compoent.
 * See JMXBindingTestClient.java or SOAPBidningTestClient.java for more details
 * on how to implement a partucular test service consumer.
 *
 *  @author chikkala
 */
public abstract class JBIComponentTestClient {
    
    public static final String TEST_PROPS_FILE_PROP = "test.properties.file";
    public static final String TEST_RESULTS_DIR_PROP = "test.results.dir";
    public static final String TEST_SRC_DIR_PROP = "test.src.dir";
    
    public static final String TEST_PACKAGE_PROP = "test.package.name";
    
    public static final String INPUT_FILE_PROP = "input.file";
    public static final String EXPECTED_FILE_PROP = "expected.file";
    public static final String OUTPUT_FILE_PROP = "output.file";
    
    public static final String DEF_TEST_PROP_FILE = "test.properties";
    public static final String DEF_INPUT_FILE = "Input.xml";
    public static final String DEF_OUTPUT_FILE = "Output.xml";
    public static final String DEF_TEST_RESULTS_DIR = "test-results";
    
    public JBIComponentTestClient() {
    }
    /**
     * returns the absolute resource path w.r.t. the packagePath if the resource name
     * is relative else return the resourceName as it is.
     * @param package name ( dot separated )
     * @param resourcName dot separated name or a absolute resource path
     * @return abosolute resource path with path separator
     */
    public static String resolveResourcePath(String packageName, String resourceName) {
        String resourcePath = resourceName;
        if ( !resourceName.startsWith("/")) {
            // it is relative resource file. resolve it w.r.t. testPackage
            String pkgDir = packageName.trim().replace('.', '/');
            if ( pkgDir.length() != 0 ) {
                pkgDir += "/";
            }
            resourcePath = "/" + pkgDir + resourceName;
        }
        return resourcePath;
    }
    /**
     * loads the resource file as properties.
     * @param testPackage package name where this test belongs
     * @param testPropPath resource path relative to testPackage or absolute resource path
     */
    public Properties loadTestProperties(String testPackage, String testPropPath) throws IOException {
        String propsResourcePath = resolveResourcePath(testPackage, testPropPath);
        
        InputStream testPropIS = null;
        try {
            testPropIS = this.getClass().getResourceAsStream(propsResourcePath);
            Properties testProps = new Properties();
            testProps.load(testPropIS);
            testProps.setProperty(TEST_PACKAGE_PROP, testPackage);
            return testProps;
        } finally {
            if ( testPropIS != null ) {
                try {
                    testPropIS.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    /**
     * load default test properties file in the testClass package.
     * @param testClass Class where to look for the default test properties
     * @param testPropPath resource path relative to testPackage or absolute resource path
     * @return Properties test properties
     */
    public Properties loadTestProperties(Class testClass, String testPropPath) throws IOException {
        return loadTestProperties(testClass.getPackage().getName(), testPropPath );
    }
    /**
     *  load default test properties file in the testClass package.
     * @param testClass Class where to look for the default test properties
     * @return Properties test properties
     */
    public Properties loadTestProperties(Class testClass) throws IOException {
        return loadTestProperties(testClass.getPackage().getName(), DEF_TEST_PROP_FILE );
    }
    /**
     * loads the resource file to string bugger
     * @param inputFile resource file path
     */
    public StringBuffer loadResourceFile(String resourcePath) throws FileNotFoundException, IOException {
        
        InputStream inputIS = null;
        InputStreamReader inputReader = null;
        BufferedReader reader = null;
        StringWriter strWriter = null;
        PrintWriter writer = null;
        try {
            inputIS = this.getClass().getResourceAsStream(resourcePath);
            inputReader = new InputStreamReader(inputIS);
            reader = new BufferedReader(inputReader);
            strWriter = new StringWriter();
            writer = new PrintWriter(strWriter);
            for ( String line = null; (line = reader.readLine()) != null ; ) {
                writer.println(line);
            }
            writer.close();
            strWriter.close();
            return strWriter.getBuffer();
        } finally {
            if ( inputIS != null ) {
                try {
                    inputIS.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * reads data from the reader and saves to file
     * @param reader reader from which to read the data and save to file
     * @param outputFilePath absolute file path
     */
    public void saveOutputToFile(Reader reader, String outputFilePath) throws IOException {
        
        BufferedReader buff = null;
        FileWriter fileWriter = null;
        PrintWriter writer = null;
        try {
            buff = new BufferedReader(reader);
            fileWriter = new FileWriter(outputFilePath);
            writer = new PrintWriter(fileWriter);
            for ( String line = null; (line = buff.readLine()) != null ; ) {
                writer.println(line);
            }
        } finally {
            if ( writer != null ) {
                writer.close();
            }
            if ( fileWriter != null ) {
                fileWriter.close();
            }
        }
    }
    
    /**
     *  resource path.
     */
    public String getInputFileResourcePath(Properties testProps) {
        
        String testPkg = testProps.getProperty(TEST_PACKAGE_PROP, "");
        String inputFile = testProps.getProperty(INPUT_FILE_PROP, DEF_INPUT_FILE);
        String resourcePath = resolveResourcePath(testPkg, inputFile);
        return resourcePath;
    }
    /**
     *  return resource path
     */
    public String getExpectedFileResourcePath(Properties testProps) {
        
        String testPkg = testProps.getProperty(TEST_PACKAGE_PROP, "");
        String resourcePath = null;
        String expFile = testProps.getProperty(EXPECTED_FILE_PROP, null);
        if ( expFile != null ) {
            resourcePath = resolveResourcePath(testPkg, expFile);
        }
        return resourcePath;
    }
    /**
     * return the absolute path to the output file
     */
    public String getOutputFilePath(Properties testProps) {
        
        String defPackage = this.getClass().getPackage().getName();
        String testPackage = testProps.getProperty(TEST_PACKAGE_PROP, defPackage);
        String testPackageDir = testPackage.replace('.','/');
        String outputFile = testProps.getProperty(OUTPUT_FILE_PROP, DEF_OUTPUT_FILE);
        
        String userHomeDir = System.getProperty("user.home", "");
        String userDir = System.getProperty("user.dir", userHomeDir);
        String defResultsDir = userDir + "/" + DEF_TEST_RESULTS_DIR;
        String sysResultDir = System.getProperty(TEST_RESULTS_DIR_PROP, defResultsDir);
        String resultDir = testProps.getProperty(TEST_RESULTS_DIR_PROP, sysResultDir);
        
        File outputDir = new File(resultDir, testPackageDir);
        // System.out.println("Creating the test results output dir " + outputDir);
        outputDir.mkdirs();
        return (new File(outputDir, outputFile)).getAbsolutePath();
    }
    
    /**
     * This is the method where the actual service invocation code based on the
     * type of client will be implemented. testService method calls this method
     * after preparing the test input and then processes the output returned by
     * this method to complte the test.
     * @param inputDoc
     * @param testProps
     * @throws java.lang.Exception
     * @return
     */
    public abstract StringBuffer invokeService(StringBuffer inputDoc, Properties testProps) throws Exception;
    
    /**
     * abstract method implemented by the extended classes to compare the output
     * document with the expected output to determine the test is a failure or
     * success.
     * @param outputDoc
     * @param expectedDoc
     * @param testProps
     * @throws java.lang.Exception
     */
    public abstract void compareWithExpectedOutput(StringBuffer outputDoc,
        StringBuffer expectedDoc, Properties testProps) throws Exception;
    
    /**
     * This is the main test method that a test case will call to test the
     * service. The standard steps that required to invoke the service and
     * process the output will be done in this method. Each test case creates
     * the test Properties required for that test and executes this method for
     * testing the service by passing the test properties to it. This method
     * prepares the input and executes the invokeService method to invoke a
     * service with the prepared input. It then processes the return value from
     * the invokeService to complete the test.
     *
     * @param testProps
     * @throws java.lang.Exception
     */
    public void testService(Properties testProps) throws Exception {
        
        String inFilePath = getInputFileResourcePath(testProps);
        String outFilePath = getOutputFilePath(testProps);
        String expFilePath = getExpectedFileResourcePath(testProps);
        File outDir = (new File(outFilePath)).getParentFile();
        outDir.mkdirs();
        
        StringBuffer outputDoc = new StringBuffer();
        
        try {
            StringBuffer inputDoc = loadResourceFile(inFilePath);
            outputDoc = invokeService(inputDoc, testProps);
            if ( expFilePath != null ) {
                StringBuffer expOutputDoc = loadResourceFile(expFilePath);
                compareWithExpectedOutput(inputDoc, expOutputDoc, testProps);
            }
        } catch (Exception ex) {
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            outputDoc.append(out.getBuffer());
            throw ex;
        } finally {
            if ( outputDoc != null ) {
                System.out.println(outputDoc);
                this.saveOutputToFile(new StringReader(outputDoc.toString()), outFilePath);
            }
        }
    }
    
}
