package com.anty.model.WADLParser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;


public class PathMethods {

    private String path;
    Map<String, JsonNode> methods;

    public PathMethods() {
        methods = new HashMap<>();
    }

    public void addNewMethod(String type, JsonNode responseJSONFormat) {
        methods.put(type, responseJSONFormat);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, JsonNode> getAllMethods() {
        return methods;
    }
}
