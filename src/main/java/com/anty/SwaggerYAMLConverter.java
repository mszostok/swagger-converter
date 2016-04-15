package com.anty;

import com.anty.service.ConverterService;

public class SwaggerYAMLConverter {
    public static void main(String[] args) {

        final String xsdFile = "Powertrain.xsd";
        final String wadlFile = "PowertrainService.wadl";

        ConverterService converterService = new ConverterService();
        converterService.setWADLFile(wadlFile);
        converterService.setXSDFile(xsdFile);

        converterService.execute();
        System.out.println(converterService.getYAMLFileResult());
    }
}
