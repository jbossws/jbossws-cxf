#!/bin/bash

set -ev


# debug on non zero exit
function finish {
  if [[ $USE_WFLY_MASTER = "true" ]]; then
     WFLY_TARGET=`pwd`"/wfly/wildfly/dist/target/"
     WFLY_HOME=$(find $WFLY_TARGET -name \wildfly\* -type d -maxdepth 1 -print | head -n1)
     echo "############ ERROR  dumping server.log #####################"
     cat $WFLY_HOME"/standalone/log/server.log"
  fi
}

trap finish EXIT


SERVER_VERSION=$1
USE_WFLY_MASTER=$2
USE_NODEPLOY=$3
SECURITY_MGR=$4

D_SERVER_HOME=""
D_NODEPLOY=""

if [[ $USE_WFLY_MASTER = "true" ]]; then

if [[ $USE_NODEPLOY = "true" ]]; then
   D_NODEPLOY="-Dnodeploy"
fi

  MYPWD=`pwd`

  rm -rf wfly
  mkdir wfly
  cd wfly
  git clone https://github.com/wildfly/wildfly.git
  cd wildfly

  # compile in silence.  The bld output is too much for travis log
  mvn --quiet clean install -DskipTests

  WFLY_TARGET=`pwd`"/dist/target/"
  WFLY_HOME=$(find $WFLY_TARGET -name \wildfly\* -type d -maxdepth 1 -print | head -n1)
  D_SERVER_HOME="-Dserver.home="$WFLY_HOME
  cd $MYPWD
fi

mvn -s .travis-settings.xml -B -fae -DSECMGR=${SECURITY_MGR} ${D_NODEPLOY} -P${SERVER_VERSION} ${D_SERVER_HOME} integration-test
