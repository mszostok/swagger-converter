package com.anty.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WADLConverter {
    private static final Logger LOGGER = LogManager.getLogger(WADLConverter.class);

    private String fileName;
    private StringBuffer output;
    StringBuffer outputErr;
    private String command;

    public WADLConverter() {
        output = new StringBuffer();
        outputErr = new StringBuffer();
        command = "cmd /c api-spec-converter {fileName} --from=wadl --to=swagger_2";
    }

    private String executeCommand() throws Exception {

        Process process;
        String line, lineErr = "";

        LOGGER.debug("Replace command file parameter with: " + fileName);
        command = command.replace("{fileName}", fileName);

        try {
            LOGGER.info("Execute api-spec-converter command");
            process = Runtime.getRuntime().exec(command);

            BufferedReader error =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));


            while ((line = input.readLine()) != null || (lineErr = error.readLine()) != null ) {
                output.append(line + "\n");
                outputErr.append(lineErr + "\n");
            }

            if(!outputErr.toString().trim().isEmpty()){
                throw new RuntimeException(outputErr.toString());
            }

            input.close();
        } catch (Exception ex) {
            LOGGER.error("api-spec-converter execution module error", ex);
            throw ex;
        }

        LOGGER.info("Return captured api-spec-converter program output");
        return output.toString();
    }

    public JsonNode convertWADLToJSON(String fileName) throws Exception {
        this.fileName = fileName;
        ObjectMapper mapper = new ObjectMapper();


        executeCommand();

        LOGGER.debug("Map api-spec-converter program output to JSON");
        JsonNode json = mapper.readTree(output.toString());

        return json;
    }

    public String getJSONString() {
        return output.toString();
    }
}
