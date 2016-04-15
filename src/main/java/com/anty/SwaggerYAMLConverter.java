package com.anty;

import com.anty.service.ConverterService;

public class SwaggerYAMLConverter {
    public static void main(String[] args) {

        if(args.length > 1) {
            ConverterService converterService = new ConverterService();
            converterService.setWADLFile(args[0]);
            converterService.setXSDFile(args[1]);
            converterService.execute();
            System.out.println(converterService.getYAMLFileResult());
        }

    }
}
