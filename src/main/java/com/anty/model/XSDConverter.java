package com.anty.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class XSDConverter {

    //FIXME need replace space with proper padding
    private final String REF_PATTERN = "        $ref: '#/definitions/{Model}'\n";
    private final String MODEL_BODY_HEADER = "    type: object \n" +
                                             "    properties:\n";
    private final String ENUM_BODY_HEADER = "      type: string\n" +
                                            "      enum:\n";
    private final String ENUM_VARIABLE_PREFIX = "        - ";

    private final String YAML_EXTENSION = ".yaml";

    private DocumentBuilderFactory docBuilderFactory;
    private DocumentBuilder docBuilder;
    private Document doc;
    private StringBuilder stringBuilder;
    private File wadlFile;

    public XSDConverter() {
        try {
            stringBuilder = new StringBuilder();
            docBuilderFactory = DocumentBuilderFactory.newInstance();

            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try {
            File file = new File(wadlFile.toPath().getFileName() + YAML_EXTENSION);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(stringBuilder.toString());
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseModels() {

        stringBuilder.append("definitions:\n");

        NodeList complexTypeList = doc.getElementsByTagName("xs:complexType");
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

    private void parseModelVariables(NodeList elements) {

        stringBuilder.append(MODEL_BODY_HEADER);

        for (int i = 0; i < elements.getLength(); i++) {
            Node elemNode = elements.item(i);

            Element elem = (Element) elemNode.getChildNodes();
            if (elem.hasAttributes()) {
                String name = elem.getAttribute("name");
                if (!name.isEmpty()) {
                    stringBuilder.append("      " + name + ":\n");

                    String type = elem.getAttribute("type");
                    if (type.startsWith("xs:"))
                        stringBuilder.append("        type: " + type.substring(3) + "\n");
                    else if (type.startsWith("tns:")) {
                        stringBuilder.append(REF_PATTERN.replace("{Model}", type.substring(4)));
                    }

                }
            }
        }
    }

    private void parseModelsEnum() {

        NodeList simpleTypeList = doc.getElementsByTagName("xs:simpleType");
        for (int j = 0; j < simpleTypeList.getLength(); ++j) {

            Element simpleName = (Element) simpleTypeList.item(j);
            if (simpleName.hasAttributes()) {
                String name = simpleName.getAttribute("name");
                stringBuilder.append("  " + name + ":\n");
            }

            stringBuilder.append(ENUM_BODY_HEADER);

            Node elementsNode = simpleTypeList.item(j);
            NodeList eL = ((Element) elementsNode.getChildNodes())
                    .getElementsByTagName("xs:enumeration");

            for (int i = 0; i < eL.getLength(); i++) {
                Node eN = eL.item(i);
                NodeList eNC = eN.getChildNodes();
                Element eE = (Element) eNC;
                if (eE.hasAttributes()) {
                    String value = eE.getAttribute("value");
                    if (!value.isEmpty()) {
                        stringBuilder.append(ENUM_VARIABLE_PREFIX + "\"" + value + "\"" + "\n");
                    }
                }
            }
        }
    }

    public String  convertXSDFileToYAML(String fileName) {
        try {
            wadlFile = new File(fileName);
            doc = docBuilder.parse(wadlFile);

            parseModels();

            parseModelsEnum();

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException ed) {
            ed.printStackTrace();
        }

        // saveToFile();
        return stringBuilder.toString();
    }

}
