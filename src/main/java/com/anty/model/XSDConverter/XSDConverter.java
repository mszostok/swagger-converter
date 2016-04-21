package com.anty.model.XSDConverter;

import com.anty.model.util.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class XSDConverter {
    private static final Logger LOGGER = LogManager.getLogger(XSDConverter.class);

    private final String REF_PATTERN = YAMLPadding.REF_DEF + "$ref: '#/definitions/{Model}'\n";
    private final String MODEL_BODY_HEADER = YAMLPadding.MODEL_BODY + "type: object \n" +
            YAMLPadding.MODEL_BODY + "properties:\n";
    private final String ENUM_BODY_HEADER = YAMLPadding.ENUM_BODY + "type: string\n" +
            YAMLPadding.ENUM_BODY + "enum:\n";
    private final String ENUM_VARIABLE_PREFIX = YAMLPadding.ENUM_PREFIX + "- ";

    private final Map<String, String> swaggerDataTypes;
    private final Map<String, Command> attributesCollector;
    private DocumentBuilder docBuilder;
    private Document doc;
    private StringBuilder stringBuilder;
    private File WADLFile;

    public XSDConverter() {
        swaggerDataTypes = new HashMap<>();
        attributesCollector = new HashMap<>();
        stringBuilder = new StringBuilder();

        initSwaggerDataType();
        initVariableCollector();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error("Error while creating document builder", e);
        }
    }

    private void initSwaggerDataType() {
        swaggerDataTypes.put("int", "integer\n" + YAMLPadding.MODEL_ATTR + "format: int32");
        swaggerDataTypes.put("long", "integer\n" + YAMLPadding.MODEL_ATTR + "format: int64");
        swaggerDataTypes.put("float", "number\n" + YAMLPadding.MODEL_ATTR + "format: float");
        swaggerDataTypes.put("base64Binary", "string");
        swaggerDataTypes.put("string", "string");
        swaggerDataTypes.put("boolean", "boolean");
    }

    private void initVariableCollector() {
        attributesCollector.put("type", (param) -> parseType((String) param));
    }

    private NodeList getNodeListByTag(Node rootNode, String tagName) {
        LOGGER.debug("Getting node list for :" + tagName);

        NodeList nodeList = ((Element) rootNode.getChildNodes())
                .getElementsByTagName(tagName);

        return nodeList;
    }

    private void parseType(String value) {
        LOGGER.info("Parse xsd type to proper swagger type");
        if (value.startsWith("xs:")) {
            value = value.substring(3);

            stringBuilder.append(YAMLPadding.MODEL_ATTR + "type: " + swaggerDataTypes.get(value) + "\n");
        } else if (value.startsWith("tns:")) {
            stringBuilder.append(REF_PATTERN.replace("{Model}", value.substring(4)));
        }
    }

    private void collectVariableAttributes(Element currentElement) {
        NamedNodeMap attr = currentElement.getAttributes();

        for (int i = 0; i < attr.getLength(); ++i) {
            String name = attr.item(i).getNodeName();
            if( !"name".equals(name)) {
                String value = attr.item(i).getNodeValue();
                if (attributesCollector.containsKey(name)) {
                    attributesCollector.get(name).collect(value);
                }
            }
        }
    }

    private void parseModelVariables(NodeList elements) {

        stringBuilder.append(MODEL_BODY_HEADER);

        LOGGER.info("Iterate thought all models variables (elements)");
        for (int i = 0; i < elements.getLength(); i++) {
            Node elemNode = elements.item(i);
            Element currentElement = (Element) elemNode.getChildNodes();

            if (currentElement.hasAttributes()) {
                String name = currentElement.getAttribute("name");
                if (!name.isEmpty()) {
                    stringBuilder.append(YAMLPadding.MODEL_VAR + name + ":\n");

                    collectVariableAttributes(currentElement);
                }
            }
        }
    }

    private void parseModelsEnum() {

        NodeList simpleTypeList = doc.getElementsByTagName("xs:simpleType");

        LOGGER.info("Iterate thought all simple variables (enum)");
        for (int j = 0; j < simpleTypeList.getLength(); ++j) {

            Element simpleName = (Element) simpleTypeList.item(j);

            if (simpleName.hasAttributes()) { // add enum only if have name attr
                String name = simpleName.getAttribute("name");
                NodeList enumNames = getNodeListByTag(simpleTypeList.item(j), "xs:enumeration");


                stringBuilder.append("  " + name + ":\n");
                stringBuilder.append(ENUM_BODY_HEADER);


                for (int i = 0; i < enumNames.getLength(); i++) {
                    Element enumElem = ((Element) enumNames.item(i));
                    String value = enumElem.getAttribute("value");

                    if (!value.isEmpty()) {
                        stringBuilder.append(ENUM_VARIABLE_PREFIX + "\"" + value + "\"" + "\n");
                    }
                }
            }
        }

    }

    private void parseModels() {

        stringBuilder.append("definitions:\n");

        NodeList complexTypeList = doc.getElementsByTagName("xs:complexType");
        LOGGER.info("Iterate thought all complex type models");
        for (int i = 0; i < complexTypeList.getLength(); ++i) {

            Element complexName = (Element) complexTypeList.item(i);
            if (complexName.hasAttributes()) {

                String name = complexName.getAttribute("name");
                stringBuilder.append("  " + name + ":\n");

                Node elementsNode = complexTypeList.item(i);
                NodeList elements = ((Element) elementsNode.getChildNodes())
                        .getElementsByTagName("xs:element");

                parseModelVariables(elements);
            }
        }
    }

    public String convertXSDFileToYAML(String fileName) {
        try {
            WADLFile = new File(fileName);
            doc = docBuilder.parse(WADLFile);

            parseModels();

            parseModelsEnum();

        } catch (SAXException e) {
            LOGGER.error("SAX error or warning:", e);
        } catch (IOException ioe) {
            LOGGER.error("Error occurred while parsing model or enum from .xsd:", ioe);
        }

        return stringBuilder.toString();
    }

}
