package com.anty.model.WADLParser;


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
import java.util.LinkedList;
import java.util.List;

public class WADLParser {
    private String globalPathPrefix;

    private DocumentBuilderFactory docBuilderFactory;
    private DocumentBuilder docBuilder;
    private Document doc;
    private final StringBuilder stringBuilder;
    private File wadlFile;
    private final List<PathMethods> processedMethod;

    public WADLParser() {
        stringBuilder = new StringBuilder();
        processedMethod = new LinkedList<>();

        try {

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

    private String processMethodResponses(NodeList response) {

        StringBuilder responseBody = new StringBuilder();

        for (int i = 0; i < response.getLength(); ++i) {
            responseBody.append("      responses:\n");

            Element responseStatus = (Element) response.item(i);
            NodeList representation = getNodeListByTag(response.item(i), "representation");

            responseBody.append("        " + responseStatus.getAttribute("status") + ":\n");
            responseBody.append("          description: " + " \n ");

            if (representation.getLength() > 0) {
                responseBody.append("          schema:\n");
                responseBody.append("            $ref: '#/definitions/");
                Element representationElem = ((Element) representation.item(0));
                String elemDef = representationElem.getAttribute("element");
                if (elemDef.startsWith("v1:")) {
                    elemDef = elemDef.substring(3);
                }
                responseBody.append(elemDef + "\n");
            }
        }
        return responseBody.toString();
    }

    private PathMethods processResourceMethods(NodeList resourceMethods) {
        PathMethods pathMethods = new PathMethods();

        for (int i = 0; i < resourceMethods.getLength(); ++i) {

            Element methodID = (Element) resourceMethods.item(i);

            stringBuilder.append("\tMethod id = " + methodID.getAttribute("id") + "\n");

            NodeList response = getNodeListByTag(resourceMethods.item(i), "response");

           String responseBody = processMethodResponses(response);

            pathMethods.addNewMethod(methodID.getAttribute("name").toLowerCase(), responseBody);
        }
        return pathMethods;
    }

    private void processResourceTag(NodeList resourceRoot) {
        for (int i = 0; i < resourceRoot.getLength(); ++i) {

            Element resElem = (Element) resourceRoot.item(i);

            stringBuilder.append("Resource path = /" + resElem.getAttribute("path") + "\n");

            NodeList resourceMethods = getNodeListByTag(resourceRoot.item(i), "method");

            PathMethods method = processResourceMethods(resourceMethods);
            method.setPath(globalPathPrefix + resElem.getAttribute("path"));
            processedMethod.add(method);
        }
    }

    public void processMethodResponse(String fileName) {
        try {
            wadlFile = new File(fileName);
            doc = docBuilder.parse(wadlFile);

            NodeList resourceRoot = doc.getElementsByTagName("resource");

            globalPathPrefix = "/" + ((Element)resourceRoot.item(0)).getAttribute("path") + "/";

            NodeList resourceRootChildNodes = getNodeListByTag(resourceRoot.item(0), "resource");

            processResourceTag(resourceRootChildNodes);

            System.out.print(stringBuilder.toString());
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException ed) {
            ed.printStackTrace();
        }

    }

    public List<PathMethods> getProcessedPathMethod() {
        return processedMethod;
    }

    public String getMethodResponse(){
        return stringBuilder.toString();
    }
}
