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

import java.io.IOException;

/**
 * Created by indianer on 15.04.2016.
 */
public class ConverterService {
    private String SWAGGER_HEADER;

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

        SWAGGER_HEADER = "swagger: '2.0'\n" +
                "info:\n" +
                "  title: Powertrain\n" +
                "  description: \n" +
                "  version: \"1.0.0\"\n" +
                "produces:\n" +
                "  - application/json\n";

    }

    private void concatPathAndModel() throws JsonProcessingException {
        StringBuilder apiBuilder = new StringBuilder();
        String apiPathYAML = Yaml.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(apiPathInJSON);

        apiBuilder.append(SWAGGER_HEADER);
        apiBuilder.append("# the domain of the service\n" + apiPathYAML.substring(apiPathYAML.indexOf("host:"), apiPathYAML.indexOf("paths")));
        apiBuilder.append("# will be prefixed to all paths\n" + apiPathYAML.substring(apiPathYAML.indexOf("basePath:"), apiPathYAML.indexOf("host")));
        apiBuilder.append(apiPathYAML.substring(apiPathYAML.indexOf("paths:"), apiPathYAML.lastIndexOf("schemes:")));
        apiBuilder.append(apiModelInYAML);

        apiSpecInYAML = apiBuilder.toString();
    }

    public void replaceMethodResponseInApiPath() {

        WADLParser.processMethodResponse(WADLFile);

        TreeNode paths = apiPathInJSON.get("paths");

        for (PathMethods pathMethods : WADLParser.getProcessedURLsMethod()) {

            TreeNode currentPath = paths.get(pathMethods.getPath());

            pathMethods.getAllMethods().forEach((k, v) -> {
                TreeNode currentPathMethod = currentPath.get(k);
                ((ObjectNode) currentPathMethod).put("responses", v);
            });


        }

    }

    public void execute() {
        try {
            apiModelInYAML = XSDConverter.convertXSDFileToYAML(XSDFile);
            apiPathInJSON = WADLConverter.convertWADLToJSON(WADLFile);

            replaceMethodResponseInApiPath();

            concatPathAndModel();
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (Exception e) {
            //e.printStackTrace();
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
