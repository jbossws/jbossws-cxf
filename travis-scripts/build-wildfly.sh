#!/bin/bash

set -ex

# uncomment to debug on non zero exit; be sure to print the correct log file
#function finish {
#  if [[ $BUILD_WFLY_MASTER = "true" ]]; then
#     WFLY_TARGET="$TRAVIS_HOME/wildfly/dist/target/"
#     WFLY_HOME=$(find $WFLY_TARGET -name \wildfly\* -type d -maxdepth 1 -print | head -n1)
#     echo "############ ERROR  dumping server.log #####################"
#     cat $WFLY_HOME"/standalone/log/server.log"
#  fi
#}
#
#trap finish EXIT

BUILD_WFLY_MASTER=$1
if [[ "$BUILD_WFLY_MASTER" = "true" ]]; then

  rm -rf data
  # We must clone wildfly outside of project root dir so that maven doesn't complain about unexpected parent project
  git clone --depth=1 --branch=master https://github.com/wildfly/wildfly.git $TRAVIS_HOME/wildfly
  pushd $TRAVIS_HOME/wildfly
  # Compile in silence. The build output is too much for travis log
  mvn -s $TRAVIS_BUILD_DIR/.travis-settings.xml --quiet clean install -DskipTests -Denforcer.skip -Dcheckstyle.skip -Prelease
  popd
fi
