package com.anty;

import com.anty.service.ConverterService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SwaggerYAMLConverter {

    private static final Logger LOGGER = LogManager.getLogger(SwaggerYAMLConverter.class);

    public static void main(String[] args) {
        LOGGER.trace("Running  application");
        if(args.length == 2) {
            String WADLFile = args[0];
            String XSDFile = args[1];

            ConverterService converterService = new ConverterService();

            LOGGER.info("Set wadl file " + WADLFile);
            converterService.setWADLFile(WADLFile);
            LOGGER.info("Set xsd file path to: " + XSDFile);
            converterService.setXSDFile(XSDFile);

            converterService.execute();

            System.out.println(converterService.getYAMLFileResult());
        }
        LOGGER.trace("Exit application");

    }
}
