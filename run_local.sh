#!/bin/bash

sbt "run 9993 \
  -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes \
  -Dauditing.enabled=false \
  -Dmetrics.enabled=false \
"
