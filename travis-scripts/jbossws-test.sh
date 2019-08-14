#!/bin/bash

set -ex

SERVER_VERSION=$1
mvn -s .travis-settings.xml -B -V -fae -P${SERVER_VERSION} verify
