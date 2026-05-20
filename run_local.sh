#!/bin/bash

sbt "run 9970 \
  -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes \
  -Dauditing.enabled=false \
  -Dmetrics.enabled=false \
"