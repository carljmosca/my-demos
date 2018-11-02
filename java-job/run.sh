#!/bin/bash

docker stop openshift-job-demo
docker rm openshift-job-demo
docker run -d \
       --name openshift-job-demo \
       carljmosca/openshift-job-demo