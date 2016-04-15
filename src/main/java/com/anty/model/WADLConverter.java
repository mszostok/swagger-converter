package com.anty.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class WADLConverter {

    private String fileName;

    private StringBuffer output;

    private String command = "cmd /c api-spec-converter {fileName} --from=wadl --to=swagger_2";

    public WADLConverter() {
        output = new StringBuffer();
    }

    private String executeCommand() {
        Process process;

        command = command.replace("{fileName}", fileName);

        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader error =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = input.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }

    public JsonNode convertWADLToJSON(String fileName) throws IOException {
        this.fileName = fileName;

        executeCommand();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(output.toString());
        return json;
    }
    public String getJSONString(){
        return output.toString();
    }
}
