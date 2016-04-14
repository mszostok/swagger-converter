package com.anty.model;

import java.io.BufferedReader;
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

    public void convertWADLToJSON(String fileName){
        this.fileName = fileName;

        executeCommand();
    }
    public String getJSON(){
        return output.toString();
    }
}
