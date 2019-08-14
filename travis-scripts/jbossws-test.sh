#!/bin/bash

set -ev
java -version

# uncomment to debug on non zero exit; be sure to print the correct log file
#function finish {
#  if [[ $USE_WFLY_MASTER = "true" ]]; then
#     WFLY_TARGET=`pwd`"/wfly/wildfly/dist/target/"
#     WFLY_HOME=$(find $WFLY_TARGET -name \wildfly\* -type d -maxdepth 1 -print | head -n1)
#     echo "############ ERROR  dumping server.log #####################"
#     cat $WFLY_HOME"/standalone/log/server.log"
#  fi
#}
#
#trap finish EXIT


SERVER_VERSION=$1
USE_WFLY_MASTER=$2
if [[ $USE_WFLY_MASTER = "true" ]]; then

  MYPWD=`pwd`

  rm -rf wfly
  mkdir wfly
  cd wfly
  git clone https://github.com/wildfly/wildfly.git
  cd wildfly

  # compile in silence.  The bld output is too much for travis log
  mvn --quiet clean install -DskipTests -Denforcer.skip -Dcheckstyle.skip -Prelease
  cd $MYPWD
fi
mvn -s .travis-settings.xml -B -fae -P${SERVER_VERSION} integration-test

