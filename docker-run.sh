#!/bin/bash

docker container run -d --rm \
  -v $(pwd)/app/tomcat/logs:/usr/share/tomcat/logs \
  --name tokyomap-oauth \
  --net network_tokyomap \
  --ip 192.168.56.110 \
  tokyomap.oauth:dev
