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
    private final List<PathMethods> processedMethod;
    private final Map<String, String> RESPONSE_DESCRIPTION;

    private String globalPathPrefix;
    private DocumentBuilderFactory docBuilderFactory;
    private DocumentBuilder docBuilder;
    private Document doc;
    private File wadlFile;

    public WADLParser() {
        processedMethod = new LinkedList<>();
        RESPONSE_DESCRIPTION = new HashMap<>();

        initResponseHashMap();

        try {

            docBuilderFactory = DocumentBuilderFactory.newInstance();

            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void initResponseHashMap(){
        RESPONSE_DESCRIPTION.put("200", "OK");
        RESPONSE_DESCRIPTION.put("201", "Created");
        RESPONSE_DESCRIPTION.put("204", "No Content");
        RESPONSE_DESCRIPTION.put("304", "Not Modified");
        RESPONSE_DESCRIPTION.put("400", "Bad Request");
        RESPONSE_DESCRIPTION.put("401", "Unauthorized");
        RESPONSE_DESCRIPTION.put("403", "Forbidden");
        RESPONSE_DESCRIPTION.put("404", "Not Found");
        RESPONSE_DESCRIPTION.put("409", "Conflict");
        RESPONSE_DESCRIPTION.put("500", "Internal Server Error");
    }

    private String capitalizeFirstLetter(String str){
        if(str.length() > 2) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str;
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
                    RESPONSE_DESCRIPTION.get(responseStatusCode) +
                    "\" ");

            if (representation.getLength() > 0) {
                Element representationElem = ((Element) representation.item(0));
                String elemDef = representationElem.getAttribute("element");
                if(!elemDef.isEmpty()) { // prevent when representation doesn't contain specified element
                    responseBody.append(",\"schema\": {");
                    responseBody.append("\"$ref\": \"#/definitions/");

                    if (elemDef.startsWith("v1:")) {
                        elemDef = elemDef.substring(3);
                    }
                    responseBody.append(capitalizeFirstLetter(elemDef) + "\"}");
                }
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
