package com.anty.model;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class WADLParser {

    private DocumentBuilderFactory docBuilderFactory;
    private DocumentBuilder docBuilder;
    private Document doc;
    private StringBuilder stringBuilder;
    private File wadlFile;

    public WADLParser() {
        try {
            stringBuilder = new StringBuilder();
            docBuilderFactory = DocumentBuilderFactory.newInstance();

            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }


    private NodeList getNodeListByTag(Node rootNode, String tagName) {
        NodeList nodeList = ((Element) rootNode.getChildNodes())
                .getElementsByTagName(tagName);
        return nodeList;
    }

    private void processMethodResponses(NodeList response) {

        stringBuilder.append("      responses:\n");

        for (int i = 0; i < response.getLength(); ++i) {
            Element responseStatus = (Element) response.item(i);
            NodeList representation = getNodeListByTag(response.item(i), "representation");

            stringBuilder.append("        " + responseStatus.getAttribute("status") + ":\n");
            stringBuilder.append("          description: " + " \n ");

            if (representation.getLength() > 0) {
                stringBuilder.append("          schema:\n");
                stringBuilder.append("            $ref: '#/definitions/");
                Element representationElem = ((Element) representation.item(0));
                String elemDef = representationElem.getAttribute("element");
                if (elemDef.startsWith("v1:")) {
                    elemDef = elemDef.substring(3);
                }
                stringBuilder.append(elemDef + "\n");
            }
        }
    }

    private void processResourceMethods(NodeList resourceMethods) {

        for (int i = 0; i < resourceMethods.getLength(); ++i) {

            Element methodID = (Element) resourceMethods.item(i);

            stringBuilder.append("\tMethod id = " + methodID.getAttribute("id") + "\n");

            NodeList response = getNodeListByTag(resourceMethods.item(i), "response");

            processMethodResponses(response);
        }
    }

    private void processResourceTag(NodeList resourceRoot) {
        for (int i = 0; i < resourceRoot.getLength(); ++i) {

            Element resElem = (Element) resourceRoot.item(i);

            stringBuilder.append("Resource path = /" + resElem.getAttribute("path") + "\n");

            NodeList resourceMethods = getNodeListByTag(resourceRoot.item(i), "method");

            processResourceMethods(resourceMethods);
        }
    }

    public void processMethodResponse(String fileName) {
        try {
            wadlFile = new File(fileName);
            doc = docBuilder.parse(wadlFile);

            NodeList resourceRoot = doc.getElementsByTagName("resource");

            NodeList resourceRootChildNodes = getNodeListByTag(resourceRoot.item(0), "resource");

            processResourceTag(resourceRootChildNodes);

            System.out.print(stringBuilder.toString());
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException ed) {
            ed.printStackTrace();
        }

    }

    public String getMethodResponse(){
        return stringBuilder.toString();
    }
}
