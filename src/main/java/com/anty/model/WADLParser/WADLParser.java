package com.anty.model.WADLParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger LOGGER = LogManager.getLogger(WADLParser.class);

    private final Map<String, String> RESPONSE_DESCRIPTION;
    private final List<PathMethods> processedMethod;

    private String globalPathPrefix;
    private DocumentBuilderFactory docBuilderFactory;
    private DocumentBuilder docBuilder;
    private Document doc;
    private File WADLFile;

    public WADLParser() {

        processedMethod = new LinkedList<>();
        RESPONSE_DESCRIPTION = new HashMap<>();

        initResponseHashMap();

        try {
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error("Error while creating DOM builder", e);
        }
    }

    private void initResponseHashMap() {
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

    private String capitalizeFirstLetter(String str) {
        if (str.length() > 2) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str;
    }

    private NodeList getNodeListByTag(Node rootNode, String tagName) {
        LOGGER.debug("Getting node list for :" + tagName);

        NodeList nodeList = ((Element) rootNode.getChildNodes())
                .getElementsByTagName(tagName);

        return nodeList;
    }

    private JsonNode processResponseDescription(NodeList response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder responseBody = new StringBuilder();

        responseBody.append("{");
        LOGGER.info("Iterate through possible response code");
        for (int i = 0; i < response.getLength(); ++i) {

            Element responseStatus = (Element) response.item(i);
            NodeList representation = getNodeListByTag(response.item(i), "representation");
            String responseStatusCode = responseStatus.getAttribute("status");

            LOGGER.debug("Process response status code" + responseStatusCode);

            responseBody.append(i > 0 ? "," : ""); // add comma if necessary for separate next filed
            responseBody.append("\"" + responseStatusCode + "\" : {");
            responseBody.append("\"description\": \"" +
                    RESPONSE_DESCRIPTION.get(responseStatusCode) +
                    "\" ");

            //get response element type when it's specified
            if (representation.getLength() > 0) {
                Element representationElem = ((Element) representation.item(0));
                String elemDef = representationElem.getAttribute("element");

                if (!elemDef.isEmpty()) { // prevent when representation doesn't contain specified element
                    LOGGER.debug("Add schema for code" + responseStatusCode);

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

        LOGGER.info("Return all parsed responses");
        return mapper.readTree(responseBody.toString());
    }

    private PathMethods processPathMethods(NodeList resourceMethods) throws IOException {
        PathMethods pathMethods = new PathMethods();

        LOGGER.info("Iterate thought all possible response method for current URL");
        for (int i = 0; i < resourceMethods.getLength(); ++i) {

            Element methodElem = (Element) resourceMethods.item(i);
            String methodName = methodElem.getAttribute("name").toLowerCase();

            NodeList response = getNodeListByTag(resourceMethods.item(i), "response");
            LOGGER.debug("Process method: " + methodName);
            JsonNode responseBody = processResponseDescription(response);

            pathMethods.addNewMethod(methodName, responseBody);
        }
        return pathMethods;
    }

    private void processAllURLs(NodeList resourceRoot) throws IOException {

        LOGGER.info("Iterate thought all URLs");
        for (int i = 0; i < resourceRoot.getLength(); ++i) {
            Element resElem = (Element) resourceRoot.item(i);

            NodeList resourceMethods = getNodeListByTag(resourceRoot.item(i), "method");
            String fullPath = globalPathPrefix + resElem.getAttribute("path");

            LOGGER.debug("Process URL: " + fullPath);
            PathMethods method = processPathMethods(resourceMethods);

            method.setPath(fullPath);
            processedMethod.add(method);
        }
    }

    public void processMethodResponse(String fileName) {

        try {
            WADLFile = new File(fileName);
            doc = docBuilder.parse(WADLFile);

            NodeList resourceRoot = doc.getElementsByTagName("resource");

            globalPathPrefix = "/" + ((Element) resourceRoot.item(0)).getAttribute("path") + "/";

            NodeList resourceRootChildNodes = getNodeListByTag(resourceRoot.item(0), "resource");
            LOGGER.debug("Process all possible REST API URLs  ");
            processAllURLs(resourceRootChildNodes);

        } catch (SAXException e) {
            LOGGER.error("SAX error or warning", e);
        } catch (IOException ioe) {
            LOGGER.error("Problem while process resource tag", ioe);
        }

    }

    public List<PathMethods> getProcessedURLsMethod() {
        return processedMethod;
    }

}
