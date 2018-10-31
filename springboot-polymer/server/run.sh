#!/bin/bash

docker stop springboot-polymer-demo
docker rm springboot-polymer-demo
docker run -d \
       -p 8080:8080 \
       --name=springboot-polymer-demo \
       carljmosca/springboot-polymer-demo