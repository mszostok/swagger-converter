package com.anty.model.WADLParser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WADLParser {
    private String globalPathPrefix;

    private DocumentBuilderFactory docBuilderFactory;
    private DocumentBuilder docBuilder;
    private Document doc;
    private File wadlFile;
    private final List<PathMethods> processedMethod;
    private final Map<String, String> RESPONSE_DECRIPTION;
    public WADLParser() {
        processedMethod = new LinkedList<>();
        RESPONSE_DECRIPTION = new HashMap<>();

        initResponseHashMap();
        try {

            docBuilderFactory = DocumentBuilderFactory.newInstance();

            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void initResponseHashMap(){
        RESPONSE_DECRIPTION.put("200", "OK");
        RESPONSE_DECRIPTION.put("201", "Created");
        RESPONSE_DECRIPTION.put("204", "No Content");
        RESPONSE_DECRIPTION.put("304", "Not Modified");
        RESPONSE_DECRIPTION.put("400", "Bad Request");
        RESPONSE_DECRIPTION.put("401", "Unauthorized");
        RESPONSE_DECRIPTION.put("403", "Forbidden");
        RESPONSE_DECRIPTION.put("404", "Not Found");
        RESPONSE_DECRIPTION.put("409", "Conflict");
        RESPONSE_DECRIPTION.put("500", "Internal Server Error");
    }

    private NodeList getNodeListByTag(Node rootNode, String tagName) {
        NodeList nodeList = ((Element) rootNode.getChildNodes())
                .getElementsByTagName(tagName);
        return nodeList;
    }

    private JsonNode processMethodResponses(NodeList response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder responseBody = new StringBuilder();
        responseBody.append("{");
        for (int i = 0; i < response.getLength(); ++i) {


            Element responseStatus = (Element) response.item(i);
            NodeList representation = getNodeListByTag(response.item(i), "representation");
            String responseStatusCode = responseStatus.getAttribute("status");
            responseBody.append(i > 0 ? "," : "");
            responseBody.append("\"" + responseStatusCode + "\" : {");
            responseBody.append("\"description\": \"" +
                    RESPONSE_DECRIPTION.get(responseStatusCode) +
                    "\" ");

            if (representation.getLength() > 0) {
                responseBody.append(",\"schema\": {");
                responseBody.append("\"$ref\": \"#/definitions/");
                Element representationElem = ((Element) representation.item(0));
                String elemDef = representationElem.getAttribute("element");
                if (elemDef.startsWith("v1:")) {
                    elemDef = elemDef.substring(3);
                }
                responseBody.append(elemDef + "\"}");
            }

            responseBody.append("}");
        }
        responseBody.append("}");
        JsonNode json = null;
        if(response.getLength() > 0) {
             json = mapper.readTree(responseBody.toString());
        }
        return json;
    }

    private PathMethods processResourceMethods(NodeList resourceMethods) throws IOException {
        PathMethods pathMethods = new PathMethods();

        for (int i = 0; i < resourceMethods.getLength(); ++i) {

            Element methodID = (Element) resourceMethods.item(i);
            String asdf = methodID.getAttribute("name").toLowerCase();
            NodeList response = getNodeListByTag(resourceMethods.item(i), "response");

            JsonNode responseBody = processMethodResponses(response);

            pathMethods.addNewMethod(methodID.getAttribute("name").toLowerCase(), responseBody);
        }
        return pathMethods;
    }

    private void processResourceTag(NodeList resourceRoot) throws IOException {
        for (int i = 0; i < resourceRoot.getLength(); ++i) {

            Element resElem = (Element) resourceRoot.item(i);

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

            globalPathPrefix = "/" + ((Element) resourceRoot.item(0)).getAttribute("path") + "/";

            NodeList resourceRootChildNodes = getNodeListByTag(resourceRoot.item(0), "resource");

            processResourceTag(resourceRootChildNodes);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException ed) {
            ed.printStackTrace();
        }

    }

    public List<PathMethods> getProcessedPathMethod() {
        return processedMethod;
    }

}
