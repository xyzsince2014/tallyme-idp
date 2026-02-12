#!/bin/bash

docker container run -d --rm \
  -v $(pwd)/app/tomcat/logs:/usr/share/tomcat/logs \
  --name tokyomap-oauth \
  --net network_tokyomap \
  --ip 172.20.0.110 \
  tokyomap.oauth:dev
