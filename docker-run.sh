#!/bin/bash

docker container run -d --rm \
  -p 8080:8080 \
  -v $(pwd)/app/tomcat/logs:/usr/share/tomcat/logs \
  --name tokyomap-oauth \
  --net network_tokyomap \
  --ip 172.20.0.110 \
  tokyomap.oauth:dev
