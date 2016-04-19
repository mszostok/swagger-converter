package com.anty.model;


import com.anty.service.ConverterService;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CliHandler {
    private static final Logger LOGGER = LogManager.getLogger(CliHandler.class);

    public static final String WADL = "wadl";
    public static final Option wadlOpt =
            OptionBuilder.hasArg(true)
                    .isRequired(true)
                    .withLongOpt(WADL)
                    .withArgName("wadlFile")
                    .withDescription("WADL file path")
                    .create(WADL.substring(0,1));

    public static final String XSD = "xsd";
    public static final Option xsdOpt =
            OptionBuilder.hasArg(true)
                    .withLongOpt(XSD)
                    .isRequired(true)
                    .withArgName("xsdFile")
                    .withDescription("XSD file path")
                    .create(XSD.substring(0,1));

    public static final String help = "help";
    public static final Option helpOpt =
            OptionBuilder.hasArg(false)
                    .withLongOpt(help)
                    .isRequired(false)
                    .withDescription("show help")
                    .create(help.substring(0,1));

    private String[] args;
    private final Options options;
    private String wadlFilePath;
    private String xsdFilePath;

    public CliHandler(String[] args) {
        this.args = args;
        options = new Options();

        options.addOption(helpOpt);
        options.addOption(wadlOpt);
        options.addOption(xsdOpt);
    }

    private void help() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("swagger-converter", options);
        System.exit(0);
    }

    public void parse() {
        CommandLineParser parser = new BasicParser();

        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                help();

            if (cmd.hasOption(XSD) && cmd.hasOption(WADL)) {
                wadlFilePath = cmd.getOptionValue(WADL);
                xsdFilePath = cmd.getOptionValue(XSD);

                LOGGER.debug("Using cli argument -wadl=" + wadlFilePath +
                        "-xsd" + xsdFilePath);

            } else {
                LOGGER.error("Missing options");
                help();
            }

        } catch (ParseException e) {
            LOGGER.error("Failed to parse command line properties", e);
            help();
        }
    }
    public void executeParsedCmd(){

        LOGGER.debug("Execute converter service with wadl file:" + wadlFilePath +
                "\nxsd file:" + xsdFilePath);

        ConverterService converterService = new ConverterService();

        converterService.setWADLFile(wadlFilePath);
        converterService.setXSDFile(xsdFilePath);

        converterService.execute();

        System.out.println(converterService.getYAMLFileResult());
    }
}
