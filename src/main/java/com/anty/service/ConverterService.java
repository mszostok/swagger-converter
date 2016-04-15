package com.anty.service;

import com.anty.model.WADLConverter;
import com.anty.model.WADLParser.PathMethods;
import com.anty.model.WADLParser.WADLParser;
import com.anty.model.XSDConverter.XSDConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.util.Yaml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class ConverterService {
    private static final Logger LOGGER = LogManager.getLogger(ConverterService.class);

    private final String SWAGGER_HEADER;
    private final String PATHS_HEADER;
    private final String DEFINITION_HEADER;

    private String XSDFile;
    private String WADLFile;
    private XSDConverter XSDConverter;
    private WADLConverter WADLConverter;
    private WADLParser WADLParser;

    private String apiModelInYAML;
    private JsonNode apiPathInJSON;
    private String apiSpecInYAML;

    public ConverterService() {
        XSDConverter = new XSDConverter();
        WADLConverter = new WADLConverter();
        WADLParser = new WADLParser();

        apiSpecInYAML ="";

        SWAGGER_HEADER =
                "swagger: '2.0'\n" +
                "################################################################################\n" +
                "#                              API Information                                 #\n" +
                "################################################################################\n"+
                "info:\n" +
                "  title: Powertrain\n" +
                "  version: \"1.0.0\"\n" +
                "################################################################################\n" +
                "#                  Host, Base Path, Schemes and Content Types                  #\n" +
                "################################################################################\n" +
                "produces:\n" +
                "  - application/json\n";
        PATHS_HEADER =
                "################################################################################\n" +
                "#                                   Paths                                      #\n" +
                "################################################################################\n";
        DEFINITION_HEADER =
                "################################################################################\n" +
                "#                                 Definitions                                  #\n" +
                "################################################################################\n";
    }

    private void concatPathAndModel() throws JsonProcessingException {
        LOGGER.info("URLs API and Model Concatenation");

        StringBuilder apiBuilder = new StringBuilder();
        String apiPathYAML = Yaml.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(apiPathInJSON);

        apiBuilder.append(SWAGGER_HEADER);
        apiBuilder.append("# the domain of the service\n" + apiPathYAML.substring(apiPathYAML.indexOf("host:"), apiPathYAML.indexOf("paths")));
        apiBuilder.append("# will be prefixed to all paths\n" + apiPathYAML.substring(apiPathYAML.indexOf("basePath:"), apiPathYAML.indexOf("host")));
        apiBuilder.append(PATHS_HEADER);
        apiBuilder.append(apiPathYAML.substring(apiPathYAML.indexOf("paths:"), apiPathYAML.lastIndexOf("schemes:")));
        apiBuilder.append(DEFINITION_HEADER);
        apiBuilder.append(apiModelInYAML);

        apiSpecInYAML = apiBuilder.toString();
    }

    public void replaceMethodResponseInApiPath() {

        LOGGER.info("Process all method response from .wadl file");
        WADLParser.processMethodResponse(WADLFile);

        TreeNode paths = apiPathInJSON.get("paths");

        LOGGER.info("Replace all methods response generated with api-spec-converter with own proper parsed methods response");
        for (PathMethods pathMethods : WADLParser.getProcessedURLsMethod()) {

            TreeNode currentPath = paths.get(pathMethods.getPath());
            LOGGER.info("Replace for current URL all possible response");
            pathMethods.getAllMethods().forEach((k, v) -> {
                TreeNode currentPathMethod = currentPath.get(k);
                ((ObjectNode) currentPathMethod).put("responses", v);
            });
        }
    }

    public void execute() {

        try {
            LOGGER.info("Convert xsd file to YAML Swagger string");
            apiModelInYAML = XSDConverter.convertXSDFileToYAML(XSDFile);
            LOGGER.info("Convert WADL file to JSON");
            apiPathInJSON = WADLConverter.convertWADLToJSON(WADLFile);

            replaceMethodResponseInApiPath();

            concatPathAndModel();
        } catch (IOException ioe) {
            LOGGER.error("I/O exception occurred while processing file", ioe);
        } catch (Exception ex) {
            LOGGER.error("Error occurred:", ex);
        }
    }

    public String getYAMLFileResult() {
        return   apiSpecInYAML ;
    }

    public void setWADLFile(String WADLFile) {
        this.WADLFile = WADLFile;
    }

    public void setXSDFile(String XSDFile) {
        this.XSDFile = XSDFile;
    }
}
