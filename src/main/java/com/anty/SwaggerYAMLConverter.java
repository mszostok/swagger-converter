package com.anty;

import com.anty.model.CliHandler;
import com.anty.service.ConverterService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SwaggerYAMLConverter {

    private static final Logger LOGGER = LogManager.getLogger(SwaggerYAMLConverter.class);

    public static void main(String[] args) {
        LOGGER.trace("Running  application");

        CliHandler cliHandler = new CliHandler(args);

        cliHandler.parse();
        cliHandler.executeParsedCmd();
        
        LOGGER.trace("Exit application");

    }
}
