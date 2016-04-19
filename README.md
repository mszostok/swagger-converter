# swagger-converter
[![Build Status](https://travis-ci.org/mszostok/swagger-converter.svg?branch=master)](https://travis-ci.org/mszostok/swagger-converter)

## Usage

First of all, we need to have installed api-spec-converter - https://github.com/lucybot/api-spec-converter with ***Command***  ***Line*** option.

Now we can clone our project and import it to our IDE (via Maven project).
Next step is to create the JAR file by exectue Maven Goal with the following command
```bash
 clean compile assembly:single
```

## Execute .jar via command line  

####Command Line

```bash
$ java -jar SwaggerYAMLConverter.jar -h

usage: swagger-converter [-h] -w <wadlFile> -x <xsdFile>

Convert API description from WADL to YAML with concatenating it with xsd
file
 -h,--help              show help
 -w,--wadl <wadlFile>   WADL file path
 -x,--xsd <xsdFile>     XSD file path

```

Example:
```bash
$ java -jar SwaggerYAMLConverter.jar --wadl fileName.wadl --xsd fileName.xsd > swagger.yaml
```