package com.anty;

import com.anty.model.XSDParser;


public class SwaggerYAMLConverter
{
    public static void main(String[] args) {

        if (args.length > 0){
            XSDParser parser = new XSDParser();
            parser.convertXSDFileToYAML(args[0]);
        }
    }
}
