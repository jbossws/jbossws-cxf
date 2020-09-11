#!/bin/bash

set -ex

SERVER_VERSION=$1
ELYTRON=$2
mvn -s .travis-settings.xml -B -V -fae verify -P${SERVER_VERSION} ${ELYTRON:+-Delytron}
