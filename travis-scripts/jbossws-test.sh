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
SECURITY_MGR=$2
USE_WFLY_MASTER=$3

D_SERVER_HOME=""

if [[ $USE_WFLY_MASTER = "true" ]]; then

  MYPWD=`pwd`

  rm -rf wfly
  mkdir wfly
  cd wfly
  git clone https://github.com/wildfly/wildfly.git
  cd wildfly

  # compile in silence.  The bld output is too much for travis log
  mvn --quiet clean install -DskipTests -Denforcer.skip -Dcheckstyle.skip -Prelease

  WFLY_TARGET=`pwd`"/dist/target/"
  WFLY_HOME=$(find $WFLY_TARGET -maxdepth 1 -name \wildfly\* -type d -print | head -n1)
  D_SERVER_HOME="-Dserver.home="$WFLY_HOME
  cd $MYPWD
  mvn -s .travis-settings.xml -B -fae -DSECMGR=${SECURITY_MGR} -P${SERVER_VERSION} ${D_SERVER_HOME} integration-test
else 
  mvn -s .travis-settings.xml -B -fae -DSECMGR=${SECURITY_MGR} -P${SERVER_VERSION} integration-test
fi
