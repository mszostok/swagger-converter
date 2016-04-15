# swagger-converter
[![Build Status](https://travis-ci.org/mszostok/swagger-converter.svg?branch=master)](https://travis-ci.org/mszostok/swagger-converter)

## Usage

First of all, we need to have installed api-spec-converter - https://github.com/lucybot/api-spec-converter with ***Command***  ***Line*** option.

Now we can clone our project and import it to our IDE (via Maven project).
Next step is to create the JAR file by exectue Maven Goal with the following command
```bash
 clean compile assembly:single
```

### Execute .jar via command line  
Example:
```bash
$ java -jar SwaggerYAMLConverter.jar fileName.wadl fileName.xsd > swagger.yaml

```
