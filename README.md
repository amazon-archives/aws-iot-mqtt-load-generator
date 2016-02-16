# aws-iot-mqtt-load-generator
Configurable load generator for MQTT messages

# LoadConfiguration
A load configuration is the entity that as a unit can be started and stopped. A load configuration contains:
- Template
- Rate (messages per minute)
- List of functions

A running load configuration is called a metrics series.


## Topic
The topic that the metrics series are published to. The topic can contain the following variables that will be replaced in runtime:
* $clientid
* $configid

## Templates
Two kinds of templates are supported by the load generator. JSON and CSV.

The templates cannot currently be created or edited in the GUI, but can be visualized.

The templates can contain a number of variables that will be replaced when instantiated. The form of a template parameter is a ”$” followed by the template variable. The following template parameters are supported:
* tick - The number of samples
* elapsedtime - Milliseconds since start of run
* timestamp - Unix epoch milliseconds
* seriesid - The identify of the running load configuration generating the template
* `<variables from metrics series>` - The variable from each metrics series is also available in the template for subsitution.
	

## Control topic
A running load generator can be controlled through a control topic.
The commands can either be a string with the command in capital letters, or a json with an attribute called "action" and the value being a string in all caps with the command.

The following commands are supported:
* STOP - The the specific metrics series is stopped

The following variables are supported in the control-topic:
* `$configid`
* `$clientid`

## Functions
Each function has a function type which will specifify the types of values and the configuration parameters supported. A function will generate a single output value based on the following input parameters:
* Tick - The number of samples that have been taken from the metricsserie
* Elapsed time - The amount of time that has passed since the metrics series configuration was started, in milliseconds

Each function specifies the output variable name, that can be used in templates.

### RANDOM
Genarate a random value, either of Integer or float type.

The parameters supported are:
* type - INT or FLOAT the supported values, defaults to INT
* minvalue -The min value, inclusive, to be generated, defaults to 0
* maxvalue - The maximum value (non-inclusive) to be generated, defaults to 100

### TEXT
A function which will generate a single text value, from a configured set. It can be randomly selected through a set of weights. The sum of all the weights are added and a random number is generated to select the string randomly with probability according to the weights.

The supported parameters are:
* texts - A ”/” separated list of text values to select from
* weights - A ”/” separated list of weight values. The weight value for a string is at the same postion as the ”texts” parameter

### EXPR
Algorithmic expression evaluation function where the alogirhtmic language supported by the exp4j library http://www.objecthunter.net/exp4j/. For example complex algorithmic expressions "X^3+4+sin(x)*x^2” can be expressed and generate interesting behaviors over time.

In the expressions the variables ”tick” and ”elapsed” are available for modelling. Elapsed is in milliseconds.

The parameters supported by the EXPR function are:
- expression - The math expression language supported by the exp4j (http://www.objecthunter.net/exp4j/) library.
- elapsedscalefactor - Scale factor to apply to the elapsetimestamp, e.g. with a scale factor of 1000 and 20000 ms elapsedtimestamp the variable ”elapsed” would have the value 20 when evaluated in the expressions
- elapsedoffset (optional) - A fixed offset of the "raw" elapsedtime in milliseconds, essentially simulating being ahead or behind the actual elapsed time
- tickscalefactor (optional)- Scale factor to apply to the ”tick” variable, e.g. with a scale factor of 10 and tick of 200 the variable ”tick” would have the value 20 when evaluated in the expression.
- tickoffset (optional) - A fixed offset of the actual ticks, this can for example allow the values to move to a more interesting area of the graph

In addition to the standard functinality of exp4j two custom functions have been added:
- max - Returning the maximum of 2 operands
- min - Returning the minimum of 2 operands

# API
There is a REST-API, the resource endpoint is <host>:<port>/mqttloadapp/webresources. The API only supports application/json as content type.

The API resources are
````
/config
	- GET - Return a list of load config ids as well as their "running" status
	- POST - Create a new Load configuration representation with the content of the POST body
	/{load config id}
		- GET - Return the detailed Load config object
		- PUT - Updates the representation of the load config object
		- POST - Start a metrics series based on the config
		- DELETE - Yupp, you guessed it, it deletes the load config

/series
	- GET - Returns a list of running metrics series
	/{load config id}
		- DELETE - Deletes the metrics series and stops the process generating load
		- GET - Retrieve information about the running metrics series

/template
	GET - Returns a list of templates
	POST - Create template
	/{template-id}
		GET - Returns the template object
		DELETE - Delete the template
````		
## Examples
### Create Load Configuration
Create a load configuration with 2 functions, publishing values to the "foo/expr" topic at a rate of 5 per minute. One with random int values between 10 and 100 assigned to variable y. Another one with calculating the cosine of the scaled number of ticks, by a factor of 10. So that the first value will have a value of cos(0.1), second one cos(0.2) and the 100th will have cos(10.9), this function value will be assigned variable when instantiating the template.
````
{
    "functions": [
        {
            "function": "RANDOM",
            "variable": "y",
            "parameters": [
                {
                    "type": "INT",
                    "maxvalue" : 1000,
                    "minvalue" : 10
                }
            ]
        },
        {
            "function": "EXPR",
            "variable": "x",
            "parameters": [
                {
                    "expression": "cos(tick) * elapsed^2 - tick^3",
					"tickscalefactor" : 10
                }
            ]
        }
    ],
    "rate": 5,
    "templateid": "test",
    "topic": "foo/expr"
}
````

# Web Interface
The web interface is built using AngularJS and is completly stand-alone, it could be delivered as a separate deployment unit and served from S3, but given scope and the fact that a server needs to be available to run the load, the web interface is delivered from the packaged WAR-application

# Server
The server is currently built and developed and tested using Glassfish 4 but should run a container supporting JAX-RS. A small tweak to the configuration had to be done to ellinimate a problem with a version of Guava in Glassfish "leaking" into the application and interfered with Guava functionality used in the application.

# Configuration
Configuration locations can be either in a local file system or in S3. All S3 locations are on the form `s3://<bucket>/<key>`. When configuring the root, then all objects are accessed relative to that.
	
The values of the XYZ_root variables can either be done through java system properties or environment variables, in that order.

## config_root
An absolute path to a local file system or an S3 object key on the format `s3://<bucketname>/<s3-folder-prefix>`.

### mqtt.properties
Contains properties for establishing the connection to the MQTT Gateway of Icebreaker.
The following properties need to be provided for the connection to the MQTT-broker
* brokerurl - The url and port to the broker gateway, e.g. `tcp:<MQTT broker host>:<broker port>`
* clientid - The clientid to be used in the MQTT-connection, if left blank will be a string representation of the local IP-address for the server
* cafile - The certificate file with the Root CA authorizing the authenticity of the server side of the connection. The format is PEM.
* cert - The private certificate that is registered with the IoT platform. The format is PEM.
* privkey - The private key for the public key contained in the certicate. The format is PEM.

## template_root
Template root can be either local filesystem or S3. By detault it's ${config_root}/templates.
Template-files are stored in this location, files can have 2 extensions, ".csv" or ".json" indicating the formats supported.
The name of a template is the file/object key infront of the extension.

## loadconfig_root
Can be either local filesystem or S3, by default it's ${config_root}/loadconfig_root.

# Getting started
## Installation
There is a AWS CloudFormation template available that will spin up an AWS ElasticBeanstalk environment, in the default VPC of the account. The Beanstalk environment will be a single-instance configuration to keep the cost to a minimum. The template will have the following parameters:
* location of the built version of the application
* config_root - Specify the s3 folder location under which the mqtt.properties configuration file can be found
* template_root - using default value, leave blank
* loadconfig_root - using default value, leave blank

Output parameters will be the URL at which the environment is accessible

## Create a LoadConfiguration

## Start a load configuration

## Stop a running metrics series
