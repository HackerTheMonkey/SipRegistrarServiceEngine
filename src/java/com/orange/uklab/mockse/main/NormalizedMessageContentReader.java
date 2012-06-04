/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.uklab.mockse.main;

import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author hasanein
 */
public class NormalizedMessageContentReader
{
    private DOMSource normalizedMessageContent = null;
    private Document mainDocument = null;
    private Element rootElement = null;

    public NormalizedMessageContentReader(DOMSource normalizedMessageSource)
    {
        this.normalizedMessageContent = normalizedMessageSource;
        init();
    }

    private void init()
    {
        mainDocument = (Document) normalizedMessageContent.getNode();
        rootElement = mainDocument.getDocumentElement();
    }

    public String getHeaderFieldContents(String headerFieldElementName)
    {

        NodeList partsNodeList = rootElement.getChildNodes();
        for (int i = 0; i < partsNodeList.getLength(); i++)
        {
            /**
             * Get the part list
             */
            Node partNode = partsNodeList.item(i);            
            if(partNode.getFirstChild().getNodeName().equals(headerFieldElementName))
            {
                /**
                 * Return the text content of the enclosed
                 * element
                 */
                return partNode.getFirstChild().getTextContent();                
            }
        }
        return null;
    }

    public void setHeaderFieldTextContent(String headerFieldElementName, String newTextContent)
    {
        NodeList partsNodeList = rootElement.getChildNodes();
        for (int i = 0; i < partsNodeList.getLength(); i++)
        {
            /**
             * Get the part list
             */
            Node partNode = partsNodeList.item(i);
            Element partNodeElement = (Element) partNode.getFirstChild();
            if(partNodeElement.getNodeName().equals(headerFieldElementName))
            {
                partNodeElement.setTextContent(newTextContent);
            }
        }
    }

    public void replaceHeader(String oldHeader, String newHeader, String newHeaderTextContent)
    {
        NodeList partsNodeList = rootElement.getChildNodes();
        for (int i = 0; i < partsNodeList.getLength(); i++)
        {
            /**
             * Get the part list
             */
            Node partNode = partsNodeList.item(i);
            /**
             * Search for the elements inside the part
             */
            NodeList partContentsNodeList = partNode.getChildNodes();
            for (int j = 0; j < partContentsNodeList.getLength(); j++)
            {
                Node partNodeElement = partContentsNodeList.item(j);
                if (partNodeElement.getNodeName().equals(oldHeader))
                {                    
                    partNode.removeChild(partNodeElement);                    
                    /**
                     * Add a new StatusLine instead
                     */                    
                    Node statusLineNode = mainDocument.createElement(newHeader);
                    statusLineNode.setTextContent(newHeaderTextContent);
                    partNode.appendChild(statusLineNode);
                }
            }
        }
    }
}
