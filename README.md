# springdoc-protobuf-schema

Fixing the protobuf message schema shown on Swagger UI. 

Please note that this only fixes the schema shown after you have integrated protobuf with springdoc. 

You will need [innogames's springfox-protobuf](https://github.com/innogames/springfox-protobuf) and [jackson-datatype-protobuf](https://github.com/HubSpot/jackson-datatype-protobuf) for the actual integration itself.

Here is an [example project](https://github.com/EternalWind/springdoc-protobuf-example) for your reference.

## What's fixed?
* Makes enum list fields correctly shown as a list of enum strings rather than a list of integers.
* Removes the extra 'UNRECOGNIZED' value from enum fields.
* Removes default value 'string' for string fields.

## How to use it?
Simply add this project as a dependency to your project that utilizes springdoc and protobuf.
