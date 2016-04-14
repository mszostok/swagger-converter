package com.anty;

import com.anty.model.XSDConverter;


public class SwaggerYAMLConverter
{
    public static void main(String[] args) {

        if (args.length > 0){
            XSDConverter parser = new XSDConverter();
            parser.convertXSDFileToYAML(args[0]);
        }
    }
}
