/*
 * SOAPBindingTestClient.java
 */

package com.sun.jbi.sample.component.test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *  This class extends the JBIComponentTestClient and implements the invokeService
 *  method to test the Service Engine via SOAP Binding by invoking the service using
 *  SOAP Client that sends the message to the service provider via soaphttp binding.
 *
 *  @author chikkala
 */
public class SOAPBindingTestClient  extends JBIComponentTestClient {
    
    private static final String ADDRESS_PROP = "soap.binding.inbound.endpoint.address.location";
    private static final String SOAP_ACTION_PROP = "soap.binding.soapaction";
    private static final String FAIL_ON_SOAP_FAULT_PROP = "fail.on.soap.fault";    
    
    private static MessageFactory messageFactory;
    private static SOAPConnectionFactory soapConnFactory;
    private static SOAPConnection connection;
    
    public SOAPBindingTestClient() throws SOAPException {
        init();
    }
    /** initializes SOAP client  */
    private synchronized void init() throws SOAPException {
        if ( messageFactory == null ) {
            messageFactory = MessageFactory.newInstance();
        }
        if ( soapConnFactory == null ) {
            soapConnFactory = SOAPConnectionFactory.newInstance();
        }
        if ( connection == null ) {
            connection = soapConnFactory.createConnection();
        }
    }
    
    /**
     * read in a soap message from the given input file
     */
    private static SOAPMessage loadMessage(Reader inReader) throws SOAPException, IOException {
        //Create and populate the message from a file
        SOAPMessage message = messageFactory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        StreamSource preppedMsgSrc = new StreamSource(inReader);
        soapPart.setContent(preppedMsgSrc);
        message.saveChanges();
        return message;
    }
    /** saves the SOAP message as xml text to a writer */
    private static void saveMessage(SOAPMessage response, Writer outWriter)
            throws TransformerConfigurationException, SOAPException, TransformerException {
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", new Integer(2));
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        
        SOAPPart replySOAPPart = response.getSOAPPart();
        Source sourceContent = replySOAPPart.getContent();
        StreamResult result = new StreamResult(outWriter);
        try {
            transformer.transform(sourceContent, result);
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }
    }    
    /**
     * Send a soap message
     * @param destination URL to send to
     * @param message message to send
     * @param expectedHttpStatus expected http status code or null if success is expected
     * @return reply soap message
     */
    private static SOAPMessage sendMessage(String destination, SOAPMessage message, String soapAction) throws SOAPException {
        
        // Add soapAction if not null
        if (soapAction != null) {
            MimeHeaders hd = message.getMimeHeaders();
            hd.setHeader("SOAPAction", soapAction);
        }
        // Send the message and get a reply
        SOAPMessage reply = null;
        reply = connection.call(message, destination);
        return reply;
    }
    /** check the xml text in the StringBuffer passed contains SOAP Fault. */
    public boolean isSOAPFault(StringBuffer msgBuff) {
        SOAPFault soapFault = null;
        try {
            SOAPMessage inMsg = loadMessage(new StringReader(msgBuff.toString()));
            soapFault = inMsg.getSOAPBody().getFault();
        } catch (Exception ex) {
            // any exception, means either no fault elem or invalid xml
        }
        return (soapFault != null);
    }
    /** 
     * invokes the service ny sending the input message and return the output 
     * message returned by the service.
     */
    public StringBuffer invokeService(StringBuffer inputDoc, Properties testProps) throws Exception {
        
        String soapAction = testProps.getProperty(SOAP_ACTION_PROP);
        String destination = testProps.getProperty(ADDRESS_PROP);
        SOAPMessage inMsg = loadMessage(new StringReader(inputDoc.toString()));
        SOAPMessage outMsg = sendMessage(destination, inMsg, soapAction);
        StringWriter out = new StringWriter();
        saveMessage(outMsg, out);
        StringBuffer outDoc = out.getBuffer();
        Boolean failOnSoapFault = Boolean.valueOf(testProps.getProperty(FAIL_ON_SOAP_FAULT_PROP, "false"));
        if ( failOnSoapFault ) {
            if (isSOAPFault(outDoc)) {
                StringBuffer errBuff = new StringBuffer("########## SOAP FAULT ############ \n");
                errBuff.append(outDoc);
                throw new Exception(errBuff.toString());
            }
        }
        return outDoc;
    }
    /** comparing the received output document with the expected document. throw
     * exception if the docs are not same. throwing the exception in this method
     * fails the test.
     */
    public void compareWithExpectedOutput(StringBuffer outputDoc,
            StringBuffer expectedDoc,
            Properties testProps) throws Exception {
        //  throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
