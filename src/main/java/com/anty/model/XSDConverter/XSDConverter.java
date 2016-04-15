package com.anty.model.XSDConverter;

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


public class XSDConverter {
    private static final Logger LOGGER = LogManager.getLogger(XSDConverter.class);

    private final String REF_PATTERN = YAMLPadding.REF_DEF + "$ref: '#/definitions/{Model}'\n";
    private final String MODEL_BODY_HEADER = YAMLPadding.MODEL_BODY + "type: object \n" +
            YAMLPadding.MODEL_BODY + "properties:\n";
    private final String ENUM_BODY_HEADER = YAMLPadding.ENUM_BODY + "type: string\n" +
            YAMLPadding.ENUM_BODY + "enum:\n";
    private final String ENUM_VARIABLE_PREFIX = YAMLPadding.ENUM_PREFIX + "- ";

    private final String YAML_EXTENSION = ".yaml";

    private DocumentBuilderFactory docBuilderFactory;
    private DocumentBuilder docBuilder;
    private Document doc;
    private StringBuilder stringBuilder;
    private File WADLFile;

    public XSDConverter() {
        try {
            stringBuilder = new StringBuilder();
            docBuilderFactory = DocumentBuilderFactory.newInstance();

            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error("Error while creating document builder", e);
        }
    }

    private NodeList getNodeListByTag(Node rootNode, String tagName) {
        LOGGER.debug("Getting node list for :" + tagName);

        NodeList nodeList = ((Element) rootNode.getChildNodes())
                .getElementsByTagName(tagName);

        return nodeList;
    }

    private void parseModelVariables(NodeList elements) {

        stringBuilder.append(MODEL_BODY_HEADER);

        LOGGER.info("Iterate thought all models variables (elements)");
        for (int i = 0; i < elements.getLength(); i++) {
            Node elemNode = elements.item(i);
            Element elem = (Element) elemNode.getChildNodes();

            if (elem.hasAttributes()) {
                String name = elem.getAttribute("name");

                if (!name.isEmpty()) {
                    stringBuilder.append(YAMLPadding.MODEL_VAR + name + ":\n");
                    String type = elem.getAttribute("type");

                    LOGGER.info("Parse xsd type to proper swagger type");
                    if (type.startsWith("xs:"))
                        stringBuilder.append(YAMLPadding.MODEL_TYPE + "type: " + type.substring(3) + "\n");
                    else if (type.startsWith("tns:")) {
                        stringBuilder.append(REF_PATTERN.replace("{Model}", type.substring(4)));
                    }
                }
            }
        }
    }

    private void parseModelsEnum() {

        NodeList simpleTypeList = doc.getElementsByTagName("xs:simpleType");

        LOGGER.info("Iterate thought all simple variables (enum)");
        for (int j = 0; j < simpleTypeList.getLength(); ++j) {

            Element simpleName = (Element) simpleTypeList.item(j);

            if (simpleName.hasAttributes()) {
                String name = simpleName.getAttribute("name");

                stringBuilder.append("  " + name + ":\n");
                stringBuilder.append(ENUM_BODY_HEADER);

                NodeList enumNames = getNodeListByTag(simpleTypeList.item(j), "xs:enumeration");

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
            LOGGER.error("SAX error or warning:" ,e);
        } catch (IOException ioe) {
            LOGGER.error("Error occurred while parsing model or enum from .xsd:" ,ioe);
        }

        return stringBuilder.toString();
    }

}
