#!/bin/sh
docker build . -t todddo-openapi-java
echo
echo
echo "To run the docker container execute:"
echo "    $ docker run -p 8080:8080 todddo-openapi-java"
