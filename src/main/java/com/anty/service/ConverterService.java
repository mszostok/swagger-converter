package com.anty.service;

import com.anty.model.JSONConverter;
import com.anty.model.WADLConverter;
import com.anty.model.WADLParser.PathMethods;
import com.anty.model.WADLParser.WADLParser;
import com.anty.model.XSDConverter;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import java.io.IOException;
import java.util.Map;

/**
 * Created by indianer on 15.04.2016.
 */
public class ConverterService {

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
    }

    private void concatYAMLFile(String yaml, String yamlFromJson) {
    }

    public void replaceMethodResponseInApiPath() {

        WADLParser.processMethodResponse(WADLFile);

        TreeNode paths = apiPathInJSON.get("paths");

        for (PathMethods pathMethods : WADLParser.getProcessedPathMethod()) {

            TreeNode currentPath = paths.get(pathMethods.getPath());

            for (Map.Entry<String, JsonNode> entry : pathMethods.getAllMethods().entrySet()) {
                //System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                TreeNode currentPathMethod = currentPath.get(entry.getKey());
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode json = mapper.readTree("{\"phonetype\":\"N95\",\"cat\":\"WP\"}");

                ((ObjectNode) currentPathMethod).put("responses", entry.getValue());
                TreeNode currentPathMethodResponse = currentPathMethod.get("responses");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

//            pathMethods.getAllMethods().forEach((k, v) -> {
//                TreeNode currentPathMethod = currentPath.get(k);
//                ((ObjectNode) currentPathMethod).put("responses", v);
//                //TreeNode currentPathMethodResponse = currentPathMethod.get("responses");
//            });


        }

    }

    public void execute() {
        try {
            apiModelInYAML = XSDConverter.convertXSDFileToYAML(XSDFile);
            apiPathInJSON = WADLConverter.convertWADLToJSON(WADLFile);

            replaceMethodResponseInApiPath();


            //concatYAMLFile(XSDConverter.getYAML(), JSONConverter.getYamlFromJson(WADLConverter.getJSON()));
            //System.out.println(xsdConverter.getYAML());

            String yamlOutput = Yaml.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(apiPathInJSON);

            System.out.println(yamlOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getYAMLFileResult() {
        return null;
    }

    public void setWADLFile(String WADLFile) {
        this.WADLFile = WADLFile;
    }

    public void setXSDFile(String XSDFile) {
        this.XSDFile = XSDFile;
    }
}
