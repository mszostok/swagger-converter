package com.anty.model.WADLParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by indianer on 15.04.2016.
 */
public class PathMethods {

    private String path;
    Map<String, String> methods;

    public PathMethods() {
        methods = new HashMap<String, String>();
    }

    public void addNewMethod(String type, String responseJSONFormat){
        methods.put(type, responseJSONFormat);
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getAllMethods() {
        return methods;
    }
}
