package com.anty.model;

import com.anty.model.WADLParser.PathMethods;
import com.anty.model.WADLParser.WADLParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.util.Yaml;

import java.io.IOException;


public class JSONConverter {

    public static String getYamlFromJson(String content) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(content);

        String yamlOutput = Yaml.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

        return yamlOutput;
    }

}
